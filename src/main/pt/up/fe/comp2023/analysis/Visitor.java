package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.utilities.StringLines;

import java.util.List;
import java.util.stream.Collectors;

public class Visitor extends AJmmVisitor<String, String> {
    private final MySymbolTable table;
    private String scope;
    private final List<Report> reports;

    public Visitor(MySymbolTable table, List<Report> reports) {
        super();
        this.table = table;
        this.reports = reports;
    }

    protected void buildVisitor() {
        addVisit("ProgramDec", this::dealWithProgram);
        addVisit("ImportDec", this::dealWithImport);
        addVisit("ClassDec", this::dealWithClass);
        addVisit("VarDec", this::dealWithVarDeclaration);
        addVisit("FunctionDeclaration", this::dealWithFunction);
        addVisit("MainDeclaration", this::dealWithMain);
    }

    private String dealWithProgram(JmmNode node, String s){
        return null;
    }

    private String dealWithImport(JmmNode node, String s) {
        table.addImport(node.get("value"));
        return s + "IMPORT";
    }

    private String dealWithClass(JmmNode node, String s) {
        table.setClassName(node.get("name"));
        try {
            table.setSuperClassName(node.get("extends"));
        } catch (NullPointerException ignored) {

        }

        scope = "CLASS";
        return s + "CLASS";
    }

    private String dealWithVarDeclaration(JmmNode node, String s) {
        Symbol field = new Symbol(MySymbolTable.getType(node, "type"), node.get("identifier"));

        if (scope.equals("CLASS")) {
            if (table.fieldExists(field.getName())) {
                this.reports.add(new Report(
                        ReportType.ERROR, Stage.SEMANTIC,
                        Integer.parseInt(node.get("line")),
                        Integer.parseInt(node.get("col")),
                        "Variable already declared: " + field.getName()));
                return s + "ERROR";
            }
            table.addField(field);
        } else {
            if (table.getCurrentMethod().fieldExists(field.getName())) {
                this.reports.add(new Report(
                        ReportType.ERROR,
                        Stage.SEMANTIC,
                        Integer.parseInt(node.get("line")),
                        Integer.parseInt(node.get("col")),
                        "Variable already declared: " + field.getName()));
                return s + "ERROR";
            }
            table.getCurrentMethod().addLocalVariable(field);
        }

        return s + "VARDECLARATION";
    }

    private String dealWithFunction(JmmNode node, String space) {
        scope = "METHOD";
        table.addMethod(node.get("name"), MySymbolTable.getType(node, "return"));

        node.put("params", "");

        return node.toString();
    }

    private String dealWithMain(JmmNode node, String space) {
        scope = "MAIN";

        table.addMethod("main", new Type("void", false));

        node.put("params", "");

        return node.toString();
    }
}
