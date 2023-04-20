package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp2023.analysis.MySymbolTable;

import java.util.stream.Collectors;

public class OllirGenerator extends AJmmVisitor<Boolean, String> {

    private final StringBuilder ollirCode;
    public final MySymbolTable symbolTable;

    public int counter;


    public OllirGenerator(MySymbolTable symbolTable) {
        this.ollirCode = new StringBuilder();
        this.symbolTable = symbolTable;
        this.counter = 0;

    }

    @Override
    protected void buildVisitor() {
        this.setDefaultVisit(this::defaultVisit);
        addVisit("Program", this::visitProgram);
        addVisit("Class", this::visitClass);
        addVisit("RegularMethod", this::visitMethod);
        addVisit("MainMethod", this::visitMainMethod);
        addVisit("Var", this::visitLocalVar);
        //addVisit("BlockStat", this::visitBlock);
        ////addVisit("IfElseStat", this::visitIfElse);
        ////addVisit("WhileStat", this::visitWhile);
        //addVisit("ExpressionStat", this::visitExprStmt);
        addVisit("AssignmentStat", this::visitAssignment);
        //addVisit("ArrayAssigmentStat", this::visitArrayAssignment);
        //addVisit("ParenOp", this::visitParenExpr);
        //addVisit("ArrayAcessOp", this::visitArrayAccess);
        //addVisit("ArrayLengthOp", this::visitArrayLength);
        addVisit("ReturnExpr", this::visitReturnExpr);
        addVisit("MethodCallOp", this::visitMethodCall);
        //addVisit("NotExpression", this::visitUniaryOp);
        addVisit("BinaryOp", this::visitBinaryOp);
        //addVisit("NewIntArrayOp", this::visitNewIntArray);
        //addVisit("NewObjectOp", this::visitNewObject);
        addVisit("IntLiteral", this::visitInteger);
        addVisit("IdOp", this::visitIdentifier);
        addVisit("TrueLiteral", this::visitTrue);
        addVisit("FalseLiteral", this::visitFalse);
        addVisit("ThisOp", this::visitThis);


    }


    private String makeTemp(String type) {
        return "t" + counter++ + "." + type;
    }


    private String visitReturnExpr(JmmNode node, Boolean ok) {
        return visit(node.getChildren().get(0));
    }


    private String defaultVisit(JmmNode node, Boolean ok) {
        for (var child : node.getChildren()) {
            visit(child);
        }
        return null;
    }


    public String getOllir() {
        return ollirCode.toString();
    }

    private String visitProgram(JmmNode node, Boolean ok) {

        for (var importString : symbolTable.getImports()) {
            ollirCode.append("import ").append(importString).append(";\n");
        }

        for (var child : node.getChildren()) {
            visit(child);
        }

        return null;
    }


    private String visitClass(JmmNode node, Boolean ok) {
        ollirCode.append(symbolTable.getClassName());
        if (symbolTable.getSuper() != null){
            ollirCode.append(" extends ").append(symbolTable.getSuper()).append(" {\n");
        } else {
            ollirCode.append(" {\n");
        }

        for (var field : symbolTable.getFields()) {
            ollirCode.append(".field private ");
            ollirCode.append(field.getName()).append(".");

            boolean isArray = node.hasAttribute("isArray");

            if (isArray) {
                ollirCode.append("array.");
                ollirCode.append(OllirUtils.getOllirType(field.getType().getName()));
            } else {
                ollirCode.append(OllirUtils.getOllirType(field.getType().getName()));
            }

            ollirCode.append(";\n");
        }

        ollirCode.append(".construct ").append(symbolTable.getClassName());ollirCode.append("().V {\n");
        ollirCode.append("\tinvokespecial(this, \"<init>\").V;\n");
        ollirCode.append("}\n\n");

        for (var child : node.getChildren()) {
            visit(child);
        }

        ollirCode.append("}\n");
        return null;
    }



    private String visitMethod(JmmNode node, Boolean ok) {
        var methodName = node.get("methodName");
        symbolTable.setCurrentMethodName(methodName);
        ollirCode.append(".method public ").append(methodName).append("(");

        var parameters = symbolTable.getParameters(methodName);

        //this will iterate through the parameters, get their TAC equivalents, and add them to paramenterCode, a big string containing comma separated arguments of the method
        var parametersCode = parameters.stream()
                .map(OllirUtils::getCode)
                .collect(Collectors.joining(", "));

        ollirCode.append(parametersCode);
        ollirCode.append(").");
        ollirCode.append(OllirUtils.getCode(symbolTable.getReturnType(methodName)));
        ollirCode.append(" {\n");

        for (int i = 0; i < node.getChildren().size() - 1; i++) {
            visit(node.getChildren().get(i));
        }

        String childReturn = visit(node.getChildren().get(node.getChildren().size() - 1));
        if (childReturn != null) {
            ollirCode.append("ret.");
            ollirCode.append(OllirUtils.getCode(symbolTable.getReturnType(methodName)));
            ollirCode.append(" ").append(childReturn);
            ollirCode.append(";\n");
        }else{
            ollirCode.append("ret.");
            ollirCode.append(OllirUtils.getCode(symbolTable.getReturnType(methodName)));
            ollirCode.append(";\n");
        }

        ollirCode.append("}\n");
        return null;
    }

    private String visitMainMethod(JmmNode node, Boolean ok) {
        var methodName = "main";
        symbolTable.setCurrentMethodName(methodName);
        ollirCode.append(".method public static main(");

        var parameters = symbolTable.getParameters(methodName);

        //this will iterate through the parameters, get their TAC equivalents, and add them to paramenterCode, a big string containing comma separated arguments of the method
        var parametersCode = parameters.stream()
                .map(OllirUtils::getCode)
                .collect(Collectors.joining(", "));

        ollirCode.append(parametersCode);
        ollirCode.append(").V");
        ollirCode.append(" {\n");

        for (var child: node.getChildren()){
            visit(child);
        }

        ollirCode.append("}\n");
        return null;
    }

    //a.i32 :=.i32 0.i32
    private String visitLocalVar(JmmNode node, Boolean ok) {
        String name = node.get("name");
        // i have but a theory of what exactly this does, but we'll see when testing :thumbsup:
        String type = node.getJmmChild(0).get("name");

        ollirCode.append(name).append(".");

        boolean isArray = node.hasAttribute("isArray");

        if (isArray) {
            ollirCode.append("array.");
            ollirCode.append(OllirUtils.getOllirType(type));
        } else {
            ollirCode.append(OllirUtils.getOllirType(type));
        }

        ollirCode.append(" :=.");
        ollirCode.append(OllirUtils.getOllirType(type));
        ollirCode.append(" 0.");
        ollirCode.append(OllirUtils.getOllirType(type));


        ollirCode.append(";\n");
        return null;
    }

    public String visitAssignment(JmmNode node, Boolean ok) {
        String name = node.get("variable");
        String type = OllirUtils.getOllirType(symbolTable.getParameters(name).toString()); //findType
        String value = visit(node.getJmmChild(0));

        ollirCode.append(name).append(" :=.").append(type).append(" ").append(value).append(".i32;\n");

        return null;
    }


   private String visitMethodCall(JmmNode Node, Boolean ok) {

        return null;
    }

    private String visitBinaryOp(JmmNode node, Boolean ok) {
        String op = node.get("op");
        String left = visit(node.getJmmChild(0));
        String right = visit(node.getJmmChild(1));

        StringBuilder sb = new StringBuilder();

        String temp = makeTemp("i32");

        sb.append(temp).append(" :=.i32 ").append(left).append(".i32 ").append(op).append(".i32 ").append(right).append(".i32\n");

        return sb.toString();
    }


    private String visitInteger(JmmNode node, Boolean ok)  {

        StringBuilder sb = new StringBuilder();

        boolean isArray = node.getJmmParent().hasAttribute("isArray");

        if (isArray) {
            String temp = makeTemp("i32");
            sb.append(temp).append(" :=.i32 ").append(node.get("val")).append(".i32\n");
        }else{
            sb.append(node.get("val")).append(".i32");
        }

        return sb.toString();
    }


    //does not cover all cases, not exploring parents of parents
    private String visitIdentifier(JmmNode node, Boolean ok) {
        String name = node.get("val");

        StringBuilder sb = new StringBuilder();

        String type = OllirUtils.getCode(symbolTable.getReturnType(name));
        sb.append(name).append(".").append(type);

        return sb.toString();
    }


    private String visitTrue(JmmNode node, Boolean ok) {
        StringBuilder sb = new StringBuilder();
        sb.append("1").append(".bool");

        return sb.toString();
    }

    private String visitFalse(JmmNode node, Boolean ok) {
        StringBuilder sb = new StringBuilder();
        sb.append("0").append(".bool");

        return sb.toString();
    }

    private String visitThis(JmmNode node, Boolean ok) {
        StringBuilder sb = new StringBuilder();
        sb.append("this");

        return sb.toString();
    }

}
