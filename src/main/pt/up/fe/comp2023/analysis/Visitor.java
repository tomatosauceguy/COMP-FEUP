package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public class Visitor extends AJmmVisitor<String, String> {
    private final MySymbolTable table;

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
        addVisit("MainMethod", this::dealWithMain);
        addVisit("RegularMethod", this::dealWithRegularMethod);
    }

    private String dealWithProgram(JmmNode node, String s){
        StringBuilder ret = new StringBuilder();
        for ( JmmNode child : node.getChildren ()){
            ret.append(visit(child, ""));
            ret.append("\n");
        }
        return ret.toString();
    }

    private Symbol dealWithType(JmmNode node){
        String typeName = node.get("name");
        var isArray=(Boolean) node.getObject("isArray");
        Type type = new Type(typeName, isArray);
        var varName = node.get("varname");
        return new Symbol(type,varName);
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
        for(JmmNode child : node.getChildren()){
            if(child.getKind().equals("VarDeclaration") ) {
                Symbol field = dealWithType(child.getJmmChild(0));
                table.addField(field);
            }else{
                visit(child, "");
            }
        }

        return s + "CLASS";
    }

    private String dealWithMain(JmmNode node,String space){

        String methodName="main";
        Type returnType = new Type("void", false);
        table.addMethod(methodName, returnType);

        Type paramType = new Type("String", true);
        String paramName = "args";
        Symbol paramSymbol = new Symbol(paramType, paramName);


        MySymbolTableMethod method = table.getCurrentMethod();
        method.addParameter(paramSymbol);
        for(JmmNode child : node.getChildren()){
            if(child.getKind().equals("VarDeclaration")){
                Symbol paramVar = dealWithType(child.getJmmChild(0));
                method.addLocalVariable(paramVar);
            }
        }
        return "";
    }
    private String dealWithRegularMethod(JmmNode node, String space) {

        List<JmmNode> children = node.getChildren();
        Symbol methodType = dealWithType(node.getJmmChild(0));

        MySymbolTableMethod method = table.addMethod(methodType.getName(), methodType.getType());

        for(int i = 1 ; i < children.size(); i++){
            if(children.get(i).getKind().equals("VarDeclaration") ){
                Symbol paramVar = dealWithType(children.get(i).getJmmChild(0));
                method.addLocalVariable(paramVar);
            }else if(children.get(i).getKind().equals("Type")){
                Symbol param = dealWithType(children.get(i));
                method.addParameter(param);
            }
        }
        return null;
    }
}
