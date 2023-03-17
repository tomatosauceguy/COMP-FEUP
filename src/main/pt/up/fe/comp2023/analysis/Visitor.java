package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.util.List;

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
        addVisit("Program", this::dealWithProgram);
        addVisit("ImportDeclaration", this::dealWithImport);
        addVisit("ClassDeclaration", this::dealWithClass);
        addVisit("MethodDeclaration", this::dealWithFunction);
    }

    private String dealWithProgram(JmmNode node, String s){
        StringBuilder ret = new StringBuilder();
        for ( JmmNode child : node.getChildren ()){
            ret.append(visit(child, ""));
            ret.append("\n");
        }
        return ret.toString();
    }

    private String dealWithImport(JmmNode node, String s) {
        String ret=s+"import"+node.get("ID");
        table.addImport(node.get("name"));
        return ret + "IMPORT";
    }

    private String dealWithClass(JmmNode node, String s) {

        table.setClassName(node.get("name"));
        try {
            table.setSuperClassName(node.get("sName"));
        } catch (NullPointerException ignored) {

        }

        scope = "CLASS";
        return s + "CLASS";
    }

    private Type dealWithType(JmmNode node){
        String typeName = node.get("name");
        var typename=node.get("name");
        var isArray=(Boolean) node.getObject("isArray");
        return new Type(typename,isArray);
    }

    private String dealWithFunction(JmmNode node, String space) {
        String methodName = node.getJmmChild(0).get("varname");
        Type methodType = dealWithType(node);

        List<Symbol> methodParams = new ArrayList<>();
        List<String> params;
        List<Symbol> localVars = new ArrayList<>();
        List<JmmNode> children = node.getChildren();

        if(methodName.equals("main")){
            Type paramType = new Type("String", true);
            String paramName = node.get("args");
            Symbol paramSymbol = new Symbol(paramType, paramName);
            Type returnType = new Type("void", false);
            table.addMethod(methodName, methodType);

            for(JmmNode child : children){
                if(child.getKind().equals("varDeclaration")){
                   // table.addLocalVariable(); TODO: chamar o addLocalVariable do MySmbolTableMethod somehow :<
                }
            }
        }else{
            params = (List<String>) node.getObject("varname");
            JmmNode childType = node.getJmmChild(0);
            String returnTypeName = childType.get("name");
            Boolean returnIsArray = (Boolean) childType.getObject("isArray");
            Type type = new Type(returnTypeName, returnIsArray);

            table.addMethod(methodName, methodType);
            for(int i = 1 ; i < children.size(); i++){

            }
        }

        scope = "MAIN";

        table.addMethod("main", new Type("void", false));

        node.put("params", "");



        scope = "METHOD";
        table.addMethod(node.get("name"), MySymbolTable.getType(node, "return"));

        node.put("params", "");

        return node.toString();
    }
}
