package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class JmmExpressionAnalyser extends AJmmVisitor<Boolean, Map.Entry<String, String>> {

    private final MySymbolTable table;
    private final List<Report> reports;
    private String scope;
    private MySymbolTableMethod currentMethod;

    private final List<String> statements = Arrays.asList("BlockStat", "IfElseStat", "WhileStat", "ExpressionStat", "AssignmentStat", "ArrayAssigmentStat");

    public JmmExpressionAnalyser(MySymbolTable table, List<Report> reports) {
        this.table = table;
        this.reports = reports;
    }

    protected void buildVisitor() {

        addVisit("BinaryOperator", this::dealWithBinaryOperation);
        addVisit("RelationalExpression", this::dealWithRelationalExpression);
        addVisit("AndExpression", this::dealWithAndExpression);
        addVisit("IntLiteral", this::dealWithPrimitive);
        addVisit("BooleanLiteral", this::dealWithPrimitive);

        addVisit("ImportDeclaration", this::dealWithImport);

        addVisit("AssignmentStat", this::dealWithAssignment);
        addVisit("ClassDeclaration", this::dealWithClassDeclaration);
        addVisit("Program", this::dealWithProgram);

        addVisit("NotExpression", this::dealWithNotExpression);
        addVisit("IfElseStat", this::dealConditionalExpression);
        addVisit("WhileStat", this::dealConditionalExpression);


        addVisit("ArrayAssignmentStat", this::dealWithArrayAssign);
        addVisit("NewIntArrayOp", this::dealWithArrayInit);
        addVisit("ArrayAcessOp", this::dealWithArrayAccess);

        addVisit("IdOp", this::dealWithVariable);
        //addVisit("VarDeclaration", this::dealWithVariableDeclaration);

        addVisit("MainMethod", this::dealWithMainDeclaration);
        addVisit("RegularMethod", this::dealWithMethodDeclaration);

        //addVisit("MethodCallOp", this::dealWithMethodCall);
        addVisit("Length", this::dealWithMethodCall);
        addVisit("NewObjectOp", this::dealWithNewObject);
        addVisit("MethodCallOp", this::dealWithAccessExpression);
        addVisit("ExpressionStat", this::dealWtihExpressionStat);

        setDefaultVisit(this::dealWithDefaultVisit);
    }

    private Map.Entry<String, String> dealWithImport(JmmNode jmmNode, Boolean aBoolean) {
        return null;
    }

    private Map.Entry<String, String> dealWithDefaultVisit(JmmNode jmmNode, Boolean aBoolean) {
        return null;
    }

    private Map.Entry<String, String> dealWithProgram(JmmNode node, Boolean data) {
        StringBuilder ret = new StringBuilder();

        for (JmmNode child : node.getChildren()) {
            ret.append(visit(child, false));
            ret.append("\n");
        }
        return Map.entry("", ret.toString());
    }

    public List<Report> getReports() {
        return this.reports;
    }

    private Map.Entry<String, String> dealWithBinaryOperation(JmmNode node, Boolean data) {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);

        Map.Entry<String, String> leftReturn = visit(left, true);
        Map.Entry<String, String> rightReturn = visit(right, true);

        Map.Entry<String, String> dataReturn = Map.entry("int", "null");

        if (!leftReturn.getValue().equals("true") && left.getKind().equals("Variable")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(left.get("lineStart")), Integer.parseInt(left.get("colStart")), "Left Member not initialized: " + left));
            }
        }
        if (!rightReturn.getValue().equals("true") && right.getKind().equals("Variable")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(right.get("lineStart")), Integer.parseInt(right.get("colStart")), "Right Member not initialized: " + right));
            }
        }

        if (!leftReturn.getKey().equals("int") && !leftReturn.getKey().equals("access")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(left.get("lineStart")), Integer.parseInt(left.get("colStart")), "Left Member not integer: " + left));
            }
        }
        if (!rightReturn.getKey().equals("int") && !rightReturn.getKey().equals("access")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(right.get("lineStart")), Integer.parseInt(right.get("colStart")), "Right Member not integer: " + right));
            }
        }

        if (dataReturn.getKey().equals("int")) {
            return dataReturn;
        } else {
            return Map.entry("error", "null");
        }
    }

    private Map.Entry<String, String> dealWithRelationalExpression(JmmNode node, Boolean data) {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);

        Map.Entry<String, String> leftReturn = visit(left, true);
        Map.Entry<String, String> rightReturn = visit(right, true);

        Map.Entry<String, String> dataReturn = Map.entry("boolean", "null");

        if (!leftReturn.getValue().equals("true") && left.getKind().equals("Variable")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(left.get("lineStart")), Integer.parseInt(left.get("colStart")), "Left Member not initialized: " + left));
            }
        }
        if (!rightReturn.getValue().equals("true") && right.getKind().equals("Variable")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(right.get("lineStart")), Integer.parseInt(right.get("colStart")), "Right Member not initialized: " + right));
            }
        }

        if (!leftReturn.getKey().equals("int")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(left.get("lineStart")), Integer.parseInt(left.get("colStart")), "Left Member not integer: " + left));
            }
        }
        if (!rightReturn.getKey().equals("int")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(right.get("lineStart")), Integer.parseInt(right.get("colStart")), "Right Member not integer: " + right));
            }
        }

        if (dataReturn.getKey().equals("boolean")) {
            return dataReturn;
        } else {
            return Map.entry("error", "null");
        }
    }

    private Map.Entry<String, String> dealWithAndExpression(JmmNode node, Boolean data) {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);

        Map.Entry<String, String> leftReturn = visit(left, true);
        Map.Entry<String, String> rightReturn = visit(right, true);

        Map.Entry<String, String> dataReturn = Map.entry("boolean", "null");

        if (!leftReturn.getValue().equals("true") && left.getKind().equals("Variable")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(left.get("lineStart")), Integer.parseInt(left.get("colStart")), "Left Member not initialized: " + left));
            }
        }
        if (!rightReturn.getValue().equals("true") && right.getKind().equals("Variable")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(right.get("lineStart")), Integer.parseInt(right.get("colStart")), "Right Member not initialized: " + right));
            }
        }

        if (!leftReturn.getKey().equals("boolean")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(left.get("lineStart")), Integer.parseInt(left.get("colStart")), "Left Member not boolean: " + left));
            }
        }
        if (!rightReturn.getKey().equals("boolean")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(right.get("lineStart")), Integer.parseInt(right.get("colStart")), "Right Member not boolean: " + right));
            }
        }

        if (dataReturn.getKey().equals("boolean")) {
            return dataReturn;
        } else {
            return Map.entry("error", "null");
        }
    }

    private Map.Entry<String, String> dealWithPrimitive(JmmNode node, Boolean data) {
        String return_type = switch (node.getKind()) {
            case "IntLiteral" -> "int";
            case "BooleanLiteral" -> "boolean";
            default -> "error";
        };

        return Map.entry(return_type, "true");
    }

    private Map.Entry<String, String> dealWithClassDeclaration(JmmNode node, Boolean data) {
        scope = "CLASS";

        for (JmmNode child : node.getChildren()) {
            visit(child, false);
        }
        return null;
    }

    private Map.Entry<String, String> dealWithAssignment(JmmNode node, Boolean space) {
        List<JmmNode> children = node.getChildren();

        if (children.size() == 1) {
            Map.Entry<String, String> assignment = visit(node.getJmmChild(0), true);
            if (assignment.getKey().equals("error")) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Undeclared Variable: " + node.getChildren().get(0)));
                return null;
            }

            Map.Entry<Symbol, Boolean> variable;
            if ((variable = currentMethod.getField(node.get("variable"))) == null) {
                variable = table.getField(node.get("variable"));
            }

            if (assignment.getKey().equals("access")) {
                variable.setValue(true);
                return null;
            }

            String[] parts = assignment.getKey().split(" ");

            if (variable.getKey().getType().getName().equals(parts[0])) {
                if (!(parts.length == 2 && variable.getKey().getType().isArray()) && !(parts.length == 1 && !variable.getKey().getType().isArray())) {
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Mismatched types: " + variable.getKey().getType().getName() + " and " + assignment.getKey()));
                    return null;
                }
            }else if (variable.getKey().getType().getName().equals(table.getSuper()) && assignment.getKey().equals(table.getClassName())){
                return null;
            }else if (table.getImports().contains(variable.getKey().getType().getName()) && table.getImports().contains(assignment.getKey())){
                return null;
            } else {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Mismatched types: " + variable.getKey().getType().getName() + " and " + assignment.getKey()));
                return null;
            }

            if (!currentMethod.initializeField(variable.getKey())) {
                table.initializeField(variable.getKey());
            }

        } else {
            Map.Entry<Symbol, Boolean> array;
            if ((array = currentMethod.getField(node.get("variable"))) == null) {
                array = table.getField(node.get("variable"));
            }

            Map.Entry<Symbol, Boolean> variable;
            if ((variable = currentMethod.getField(node.get("variable"))) == null) {
                variable = table.getField(node.get("variable"));
            }

            if (!array.getKey().getType().equals(variable.getKey().getType())) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Mismatched types: " + variable.getKey().getType() + " and " + array.getKey().getType()));
            } else {
                if (!currentMethod.initializeField(variable.getKey())) {
                    table.initializeField(variable.getKey());
                }
            }
        }

        return null;
    }

    private Map.Entry<String, String> dealWithNotExpression(JmmNode node, Boolean data) {
        JmmNode bool = node.getChildren().get(0);

        Map.Entry<String, String> boolReturn = visit(bool, true);

        Map.Entry<String, String> dataReturn = Map.entry("boolean", "null");

        if (!boolReturn.getValue().equals("true") && bool.getKind().equals("Variable")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(bool.get("lineStart")), Integer.parseInt(bool.get("colStart")), "Member not initialized: " + bool));
            }
        }

        if (!boolReturn.getKey().equals("boolean")) {
            dataReturn = Map.entry("error", "null");
            if (data != null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(bool.get("lineStart")), Integer.parseInt(bool.get("colStart")), "Operator '!' cannot be applied to " + boolReturn.getKey().replace(" ","")));
            }
        }

        if (dataReturn.getKey().equals("boolean")) {
            return dataReturn;
        } else {
            return Map.entry("error", "null");
        }
    }

    private Map.Entry<String, String> dealConditionalExpression(JmmNode node, Boolean data) {
        JmmNode condition = node.getChildren().get(0);
        Map.Entry<String, String> conditionReturn = visit(condition, true);

        Map.Entry<String, String> dataReturn = Map.entry("boolean", "null");

        if (!conditionReturn.getKey().equals("boolean")) {
            dataReturn = Map.entry("error", "null");
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(condition.get("lineStart")), Integer.parseInt(condition.get("colStart")), "Conditional expression not boolean"));
        }

        return dataReturn;
    }

    private Map.Entry<String, String> dealWithArrayInit(JmmNode node, Boolean data) {
        JmmNode size = node.getChildren().get(0);
        Map.Entry<String, String> sizeReturn = visit(size, true);

        if (!sizeReturn.getKey().equals("int")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(size.get("lineStart")), Integer.parseInt(size.get("colStart")), "Array init size is not an Integer: " + size));
            return Map.entry("error", "null");
        }

        return Map.entry("int []", "null");
    }

    private Map.Entry<String, String> dealWithArrayAccess(JmmNode node, Boolean data) {
        JmmNode leftIndex = node.getChildren().get(0);
        JmmNode rigthIndex = node.getChildren().get(1);

        Map.Entry<String, String> leftReturn = visit(leftIndex, true);
        Map.Entry<String, String> rigthReturn = visit(rigthIndex, true);

        if (!leftReturn.getKey().equals("int")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(leftIndex.get("lineStart")), Integer.parseInt(leftIndex.get("colStart")), "Array access index is not an Integer: " + leftIndex));
            return Map.entry("error", "null");
        }

        if (!rigthReturn.getKey().equals("int")) {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(leftIndex.get("lineStart")), Integer.parseInt(leftIndex.get("colStart")), "Index must be of type int: " + leftIndex));
            return Map.entry("error", "null");
        }

        return Map.entry("index", leftReturn.getValue());
    }
    private Map.Entry<String, String> dealWithArrayAssign(JmmNode node, Boolean data){
        //TODO este visitor
        return null;
    }

    private Map.Entry<String, String> dealWithVariable(JmmNode node, Boolean data) {
        Map.Entry<Symbol, Boolean> field = null;

        if (scope.equals("CLASS")) {
            field = table.getField(node.get("val"));
        } else if (scope.equals("METHOD") && currentMethod != null) {
            field = currentMethod.getField(node.get("val"));
            if (field == null) {
                field = table.getField(node.get("val"));
            }
        }

        if (field == null && table.getImports().contains(node.get("val"))) {
            return Map.entry("access", "true");
        } else if (field == null && node.get("val").equals("this")) {
            return Map.entry("method", "true");
        }
        if (field == null) {
            return Map.entry("error", "null");
        } else {
            return Map.entry(field.getKey().getType().getName() + (field.getKey().getType().isArray() ? " []" : ""), field.getValue() ? "true" : "null");
        }
    }
/*
    private Map.Entry<String, String> dealWithVariableDeclaration(JmmNode node, Boolean data) {
        JmmNode left = node.getChildren().get(0);
        JmmNode right = node.getChildren().get(1);


        return null;
    }
 */
    private Map.Entry<String, String> dealWithMainDeclaration(JmmNode node, Boolean data) {
        scope = "METHOD";

        try {
            currentMethod = table.getMethod("main", Arrays.asList(new Type("String", true)), new Type("void", false));
        } catch (Exception e) {
            currentMethod = null;
            e.printStackTrace();
        }

        return null;
    }

    private Symbol dealWithType(JmmNode node){
        String typeName = node.get("name");
        var isArray=(Boolean) node.getObject("isArray");
        Type type = new Type(typeName, isArray);
        var varName = node.get("varname");
        return new Symbol(type,varName);
    }

    private Map.Entry<String, String> dealWithMethodDeclaration(JmmNode node, Boolean data) {
        scope = "METHOD";

        List<Type> params = new ArrayList<>();
        List<JmmNode> children = node.getChildren();
        Symbol returnSymbol = dealWithType(node.getJmmChild(0));

        for(int i = 1 ; i < children.size(); i++){
            if(children.get(i).getKind().equals("Type")){
                Symbol param = dealWithType(children.get(i));
                params.add(param.getType());
            }
        }

        try {
            currentMethod = table.getMethod(returnSymbol.getName(), params, returnSymbol.getType());
        } catch (Exception e) {
            currentMethod = null;
            e.printStackTrace();
        }
        for(int i = 0 ; i < children.size(); i++){
            if (statements.contains(children.get(i).getKind())){
                visit(children.get(i), true);
            }else if(!children.get(i).getKind().equals("Type") && !children.get(i).getKind().equals("VarDeclaration")){
                dealWithReturn(children.get(i), true);
            }
        }
        return null;
    }

    private Map.Entry<String, String> dealWithReturn(JmmNode node, Boolean space) {
        String returnType = visit(node, true).getKey();

        if (returnType.equals("access")) {
            return null;
        }

        String[] parts = returnType.split(" ");

        if (parts.length == 2 && currentMethod.getReturnType().isArray()) {
            if (!parts[0].equals(currentMethod.getReturnType().getName())) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Return type mismatch 1"));
            }
            return null;
        } else if (parts.length == 1 && !currentMethod.getReturnType().isArray()) {
            if (!parts[0].equals(currentMethod.getReturnType().getName())) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Return type mismatch 2"));
            }
            return null;
        } else {
            reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Return type mismatch 3"));
        }

        return null;
    }

    private Map.Entry<String, String> dealWithNewObject(JmmNode node, Boolean data) {
        return Map.entry(node.get("name"), "object");
    }

    private Map.Entry<String, String> dealWithMethodCall(JmmNode node, Boolean space) {
        if (node.getKind().equals("Length")) {
            return Map.entry("length", "null");
        }

        List<JmmNode> children = node.getChildren();
        List<Type> params = getParametersList(children);

        String method = node.get("value");
        if (params.size() > 0) {
            for (Type param : params) {
                method += "::" + param.getName() + ":" + (param.isArray() ? "true" : "false");
            }
        }

        Type returnType = table.getReturnType(method);

        try {
            table.getMethod(node.get("value"), params, returnType);
        } catch (Exception e) {
            if (this.table.getSuper() == null) {
                return Map.entry("error", "noSuchMethod");
            } else {
                return Map.entry("method", "access");
            }
        }

        return Map.entry("method", returnType.getName() + (returnType.isArray() ? " []" : ""));
    }

    private List<Type> getParametersList(List<JmmNode> children) {
        List<Type> params = new ArrayList<>();
        for (JmmNode child : children) {
            switch (child.getKind()) {
                case "IntegerLiteral":
                    params.add(new Type("int", false));
                    break;
                case "BooleanLiteral":
                    params.add(new Type("boolean", false));
                    break;
                case "Variable":
                case "AccessExpression":
                case "BinaryOperation":
                    Map.Entry<String, String> var = visit(child, true);
                    String[] type = var.getKey().split(" ");
                    params.add(new Type(type[0], type.length == 2));
                    break;
                default:
                    break;
            }
        }
        return params;
    }

    private Map.Entry<String, String> dealWithAccessExpression(JmmNode node, Boolean requested) {
        JmmNode target = node.getChildren().get(0);
        Map.Entry<String, String> targetReturn = visit(target, true);

        if(node.getChildren().size()>1) {
            JmmNode method = node.getChildren().get(1);
            Map.Entry<String, String> methodReturn = visit(method, true);
            if (targetReturn.getKey().equals("error")) {
                if (requested != null || !node.getJmmParent().getKind().equals("Assignment"))
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Invalid Access"));
                return Map.entry("error", "null");
            }

            if (methodReturn.getKey().equals("error")) {
                if (targetReturn.getKey().equals("access")) {
                    return Map.entry("access", "null");
                } else {
                    if (requested != null || !node.getJmmParent().getKind().equals("Assignment"))
                        reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "No such method"));
                    return Map.entry("error", "null");
                }
            } else if (targetReturn.getKey().equals("method") || targetReturn.getKey().equals(table.getClassName())) {
                return Map.entry(methodReturn.getKey(), "null");
            } else if (targetReturn.getKey().equals("access")) {
                return Map.entry("access", "null");
            } else if (targetReturn.getKey().equals("int") || targetReturn.getKey().equals("boolean")) {
                if (requested != null)
                    reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Target cannot be primitive"));
                return Map.entry("error", "null");
            } else if (targetReturn.getKey().equals("int []")) {
                if (methodReturn.getKey().equals("length")) {
                    return Map.entry("int", "length");
                }
                if (methodReturn.getKey().equals("index")) {
                    return Map.entry("int", "index");
                }
            } else if (target.get("val").equals(table.getClassName())) {
                return Map.entry(methodReturn.getValue(), "index");
            } else if (table.getImports().contains(targetReturn.getKey())) {
                return Map.entry("access", "null");
            }
        }
        else if (targetReturn.getKey().equals("access")) {
            return Map.entry("access", "null");
        }
        else if (targetReturn.getKey().equals("int") || targetReturn.getKey().equals("boolean")) {
            if (requested != null)
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Target cannot be primitive"));
            return Map.entry("error", "null");

            } else if (table.getImports().contains(targetReturn.getKey())) {
                return Map.entry("access", "null");
            } else if (!table.getImports().contains(targetReturn.getKey()) && table.getSuper() == null) {
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Class not imported"));
            return Map.entry("error", "null");
        }
        else if (!this.table.getMethods().contains(node.get("name"))) {
            if(this.table.getSuper() == null){
                reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("lineStart")), Integer.parseInt(node.get("colStart")), "Method name doesnt exist"));
            }

            return Map.entry("error", "null");
        }

        return Map.entry("error", "null");
    }

    private Map.Entry<String, String> dealWtihExpressionStat(JmmNode node, Boolean requested) {
        for (JmmNode child : node.getChildren()) {
            visit(child, true);
        }
        return null;
    }
}

