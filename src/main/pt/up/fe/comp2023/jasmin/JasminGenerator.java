package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashMap;
import java.util.Map;

public class JasminGenerator {
    private ClassUnit classUnit;
    private int counterStack;
    private int counterMax;
    private int conditional;


    public JasminGenerator(ClassUnit classUnit){
        this.classUnit = classUnit;
    }

    public String dealWithClass(){
        StringBuilder string = new StringBuilder();

        string.append(".class ").append(classUnit.getClassName()).append("\n");

        if(classUnit.getSuperClass() != null){
            string.append(".super ").append(classUnit.getSuperClass()).append("\n");
        }
        else string.append(".super java/lang/Object\n");

        for (Field f : classUnit.getFields()){
            string.append(".field '").append(f.getFieldName()).append("' ").append(this.convertType(f.getFieldType())).append("\n");
        }

        for (Method method : classUnit.getMethods()) {
            this.counterMax = 0;
            this.counterStack = 0;

            string.append(this.dealWithMethodHeader(method));
            String instructions = this.dealWithMethodIntructions(method);
            if (!method.isConstructMethod()) {
                string.append(this.dealWithMethodLimits(method));
                string.append(instructions);
            }
        }

        return string.toString();
    }

    private String dealWithMethodHeader(Method method){
        if(method.isConstructMethod()) {
            String classSuper = "java/lang/Object";

            if (classUnit.getSuperClass() != null)
                classSuper = classUnit.getSuperClass();

            return "\n.method public <init>()V\naload_0\ninvokespecial " + classSuper +  ".<init>()V\nreturn\n.end method\n";
        }

        StringBuilder string = new StringBuilder("\n.method").append(" ").append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");

        if(method.isStaticMethod())
            string.append("static ");
        if(method.isFinalMethod())
            string.append("final ");

        string.append(method.getMethodName()).append("(");
        for(Element l : method.getParams())
                string.append(convertType(l.getType()));

        string.append(")").append(this.convertType(method.getReturnType())).append("\n");

        return string.toString();
    }

    // going to be modified later because of values
    private String dealWithMethodLimits(Method method){
        StringBuilder string = new StringBuilder();
        int localCount = method.getVarTable().size();
        if (!method.isStaticMethod()) {
            localCount++;
        }
        string.append(".limit locals ").append(localCount).append("\n");
        string.append(".limit stack ").append(counterMax).append("\n");

        return string.toString();
    }


    private String dealWithMethodIntructions(Method method){
        StringBuilder string = new StringBuilder();
        method.getVarTable();
        for (Instruction instruction : method.getInstructions()) {
            string.append(dealWithInstruction(instruction, method.getVarTable(), method.getLabels()));
            if (instruction instanceof CallInstruction && ((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID) {
                string.append("pop\n");
            }
        }

        string.append("\n.end method\n");
        return string.toString();
    }

    private String dealWithInstruction(Instruction instruction, HashMap<String, Descriptor> table, HashMap<String, Instruction> methodLabels){
        StringBuilder string = new StringBuilder();
        for (Map.Entry<String, Instruction> entry : methodLabels.entrySet()) {
            if (entry.getValue().equals(instruction)){
                string.append(entry.getKey()).append(":\n");
            }
        }

        switch (instruction.getInstType()) {
            case ASSIGN:
                return string.append(dealWithAssignment((AssignInstruction) instruction, table)).toString();
            case CALL:
                return string.append(dealWithCallInstruction((CallInstruction) instruction, table)).toString();
            case GOTO:
                return string.append(dealWithGotoInstruction((GotoInstruction) instruction, table)).toString();
            case BRANCH:
            case RETURN:
                return string.append(dealWithReturnInstruction((ReturnInstruction) instruction, table)).toString();
            case PUTFIELD:
                return string.append(dealWithPutFieldInstruction((PutFieldInstruction) instruction, table)).toString();
            case GETFIELD:
                return string.append(dealWithGetFieldInstruction((GetFieldInstruction) instruction, table)).toString();
            case UNARYOPER:
                return "TODO";
            case BINARYOPER:
                return string.append(dealWithBinaryOpInstruction((BinaryOpInstruction) instruction, table)).toString();
            case NOPER:
                return string.append(dealWithSingleOpInstruction((SingleOpInstruction) instruction, table)).toString();
            default:
                return "Error in dealWithInstruction";
        }
    }

    private String dealWithAssignment(AssignInstruction instruction, HashMap<String, Descriptor> table){
        String string = "";
        Operand op = (Operand) instruction.getDest();

        if(op instanceof ArrayOperand arrayop){
            //Loading array
            string += String.format("aload%s\n", this.getVirtualReg(op.getName(), table));
            //Loading index
            string += loadElement(arrayop.getIndexOperands().get(0), table);
        }

        string += dealWithInstruction(instruction.getRhs(), table, new HashMap<String, Instruction>());
        if(!(op.getType().getTypeOfElement().equals(ElementType.OBJECTREF) && instruction.getRhs() instanceof CallInstruction)){
            string += this.storeElement(op, table);
        }

        return string;
    }

    private String dealWithCallInstruction(CallInstruction instruction, HashMap<String, Descriptor> table){
        String string = "";
        CallType call = instruction.getInvocationType();

        switch (call){
            case invokevirtual, invokespecial:
                string += this.dealWithInvoke(instruction, table, call, ((ClassType)instruction.getFirstArg().getType()).getName());
                break;
            case invokestatic:
                string += this.dealWithInvoke(instruction, table, call, ((Operand)instruction.getFirstArg()).getName());
                break;
            case NEW:
                string += this.dealWithNewObject(instruction, table);
                break;
            case arraylength:
                string += this.loadElement(instruction.getFirstArg(), table);
                string += "arraylength\n";
                break;
            default:
                return "Error in dealWithCallInstruction";
        }
        return string;
    }

    private String dealWithInvoke(CallInstruction instruction, HashMap<String, Descriptor> table, CallType call, String classN){
        String string = "";

        String literal = ((LiteralElement) instruction.getSecondArg()).getLiteral();
        String params = "";

        if(!literal.equals("\"<init>\"")){
            string += this.loadElement(instruction.getFirstArg(), table);
        }

        for(Element element : instruction.getListOfOperands()){
            string += this.loadElement(element, table);
            params += this.convertType(element.getType());
        }

        string += call.name() + " " + this.getOjectClassName(classN) + "/" + literal.replace("\"","") + "(" + params + ")" + this.convertType(instruction.getReturnType()) + "\n";

        if (literal.equals("\"<init>\"") && !classN.equals("this")) {
            string += this.storeElement((Operand) instruction.getFirstArg(), table);
        }

        return string;
    }

    private String dealWithNewObject(CallInstruction instruction, HashMap<String, Descriptor> table){
        Element elem = instruction.getFirstArg();
        String string = "";

        if(elem.getType().getTypeOfElement().equals(ElementType.ARRAYREF)){
            string += this.loadElement(instruction.getListOfOperands().get(0), table);
            string += "newarray int\n";
        }

        if(elem.getType().getTypeOfElement().equals(ElementType.OBJECTREF))
            string += "new " + this.getOjectClassName(((Operand) elem).getName()) + "\ndup\n";

        return string;
    }

    private String dealWithGotoInstruction(GotoInstruction instruction, HashMap<String, Descriptor> table) {
        return String.format("goto %s\n", instruction.getLabel());
    }

    private String dealWithReturnInstruction(ReturnInstruction instruction, HashMap<String, Descriptor> table){
        if(!instruction.hasReturnValue()) return "return";
        String string = "";

        switch (instruction.getOperand().getType().getTypeOfElement()) {
            case VOID:
                string = "return";
                break;
            case INT32:
            case BOOLEAN:
                string = loadElement(instruction.getOperand(), table);
                string += "ireturn";
                break;
            case ARRAYREF:
            case OBJECTREF:
                string = loadElement(instruction.getOperand(), table);
                string += "areturn";
                break;
            default:
                break;
        }

        return string;
    }

    private String dealWithPutFieldInstruction(PutFieldInstruction instruction, HashMap<String, Descriptor> table){
        String string = "";
        Operand object = (Operand) instruction.getFirstOperand();
        Operand variable = (Operand) instruction.getSecondOperand();
        Element value = instruction.getThirdOperand();

        string += this.loadElement(object, table);
        string += this.loadElement(value, table);

        return string + "putfield " + classUnit.getClassName() + "/" + variable.getName() + " " + convertType(variable.getType()) + "\n";
    }

    private String dealWithGetFieldInstruction(GetFieldInstruction instruction, HashMap<String, Descriptor> table){
        String string = "";
        Operand object = (Operand) instruction.getFirstOperand();
        Operand variable = (Operand) instruction.getSecondOperand();

        string += this.loadElement(object, table);

        return string + "getfield " + classUnit.getClassName() + "/" + variable.getName() + " " + convertType(variable.getType()) + "\n";
    }

    private String dealWithBinaryOpInstruction(BinaryOpInstruction instruction, HashMap<String, Descriptor> table){
        switch (instruction.getOperation().getOpType()){
            case ADD:
            case SUB:
            case MUL:
            case DIV:
                return this.dealWithIntOperation(instruction, table);
            case LTH:
            case GTE:
            case ANDB:
            case NOTB:
                return this.dealWithBooleanOperation(instruction, table);
            default:
                return "Error in dealWithBinaryOpInstruction";
        }
    }

    private String dealWithIntOperation(BinaryOpInstruction instruction, HashMap<String, Descriptor> table){
        String leftOp = loadElement(instruction.getLeftOperand(), table);
        String rightOp = loadElement(instruction.getRightOperand(), table);
        String operator;

        switch (instruction.getOperation().getOpType()){
            case ADD:
                operator = "iadd\n";
                break;
            case SUB:
                operator = "isub\n";
                break;
            case MUL:
                operator = "imul\n";
                break;
            case DIV:
                operator = "idiv\n";
                break;
            default:
                return "Error in dealWithIntOperation\n";
        }
        return leftOp + rightOp + operator;
    }

    private String dealWithBooleanOperation(BinaryOpInstruction instruction, HashMap<String, Descriptor> table){
        OperationType opt = instruction.getOperation().getOpType();
        StringBuilder string = new StringBuilder();

        switch (instruction.getOperation().getOpType()){
            case LTH:
            case GTE: {
                String leftOp = loadElement(instruction.getLeftOperand(), table);
                String rightOp = loadElement(instruction.getRightOperand(), table);

                string.append(leftOp)
                      .append(rightOp)
                      .append(this.dealWithRelationalOperation(opt, this.getTrueLabel()))
                      .append("iconst_1\n")
                      .append("goto ").append(this.getEndIfLabel()).append("\n")
                      .append(this.getTrueLabel()).append(":\n")
                      .append("iconst_0\n")
                      .append(this.getEndIfLabel()).append(":\n");
                break;
            }
            case ANDB: {
                String aux = "ifeq " + this.getTrueLabel() + "\n";
                string.append(loadElement(instruction.getLeftOperand(), table)).append(aux);
                string.append(loadElement(instruction.getRightOperand(), table)).append(aux);

                string.append("iconst_1\n")
                      .append("goto ").append(this.getEndIfLabel()).append("\n")
                      .append(this.getTrueLabel()).append(":\n")
                      .append("iconst_0\n")
                      .append(this.getEndIfLabel()).append(":\n");

                break;
            }
            case NOTB: {
                String operand = loadElement(instruction.getLeftOperand(), table);

                string.append(operand)
                      .append("ifne ").append(this.getTrueLabel()).append("\n")
                      .append("iconst_1\n")
                      .append("goto ").append(this.getEndIfLabel()).append("\n")
                      .append(this.getTrueLabel()).append(":\n")
                      .append("iconst_0\n")
                      .append(this.getEndIfLabel()).append(":\n");

                break;
            }
            default:
                return "Error in dealWithBooleanOperation";
        }

        this.conditional++;
        return string.toString();
    }

    private String dealWithSingleOpInstruction(SingleOpInstruction instruction, HashMap<String, Descriptor> table) {
        return loadElement(instruction.getSingleOperand(), table);
    }

    // Maybe put this in a utils file

    private String convertType(Type type) {
        ElementType elem = type.getTypeOfElement();
        String string = "";

        if (elem == ElementType.ARRAYREF) {
            elem = ((ArrayType) type).getTypeOfElements();
            string += "[";
        }

        switch (elem) {
            case INT32:
                return string + "I";
            case BOOLEAN:
                return string + "Z";
            case OBJECTREF:
                String className = ((ClassType) type).getName();
                return string + "L" + this.getOjectClassName(className) + ";";
            case CLASS:
                return "CLASS";
            case STRING:
                return string + "Ljava/lang/String;";
            case VOID:
                return "V";
            default:
                return "Error converting ElementType";
        }
    }

    private String getOjectClassName(String className) {
        for (String _import : classUnit.getImports()) {
            if (_import.endsWith("." + className)) {
                return _import.replaceAll("\\.", "/");
            }
        }
        return className;
    }

    private String getVirtualReg(String varName, HashMap<String, Descriptor> varTable) {
        int virtualReg = varTable.get(varName).getVirtualReg();
        if (virtualReg > 3) {
            return " " + virtualReg;
        }
        return "_" + virtualReg;
    }

    private String loadElement(Element element, HashMap<String, Descriptor> table){
        if(element instanceof LiteralElement){
            String num = ((LiteralElement) element).getLiteral();
            return this.selectConstType(num) + "\n";
        }
        else if(element instanceof ArrayOperand){
            ArrayOperand arrayop = (ArrayOperand) element;

            String string = String.format("aload%s\n", this.getVirtualReg(arrayop.getName(), table));
            string += loadElement(arrayop.getIndexOperands().get(0), table);
            return string + "iaload\n";
        } else if(element instanceof Operand) {
            Operand op = (Operand) element;
            switch (op.getType().getTypeOfElement()){
                case INT32 , BOOLEAN:
                    return String.format("iload%s\n", this.getVirtualReg(op.getName(), table));
                case OBJECTREF , ARRAYREF:
                    return String.format("aload%s\n", this.getVirtualReg(op.getName(), table));
                case THIS:
                    return "aload_0\n";
                case STRING:
                case VOID:
                default:
                    return "Error in operand inside loadElement\n";
            }
        }
        return "Error in loadElement\n";
    }

    private String storeElement(Operand op, HashMap<String, Descriptor> table){
        if(op instanceof ArrayOperand)
            return "iastore\n";

        switch (op.getType().getTypeOfElement()){
            case INT32, BOOLEAN:
                return String.format("istore%s\n", this.getVirtualReg(op.getName(), table));
            case OBJECTREF, ARRAYREF:
                return String.format("astore%s\n", this.getVirtualReg(op.getName(), table));
            default:
                return "Error in storeElement\n";
        }
    }

    private String selectConstType(String literal){
        return Integer.parseInt(literal) < -1 || Integer.parseInt(literal) > 5 ?
                Integer.parseInt(literal) < -128 || Integer.parseInt(literal) > 127 ?
                        Integer.parseInt(literal) < -32768 || Integer.parseInt(literal) > 32767 ?
                                "ldc " + literal :
                                "sipush " + literal :
                        "bipush " + literal :
                "iconst_" + literal;
    }

    private String dealWithRelationalOperation(OperationType opt, String string) {
        switch (opt) {
            case LTH:
                return String.format("if_icmpge %s\n", string);
            case GTE:
                return String.format("if_icmplt %s\n", string);
            default:
                return "Error in RelationalOperations\n";
        }
    }

    private String getTrueLabel() {
        return "myTrue" + this.conditional;
    }

    private String getEndIfLabel() {
        return "myEndIf" + this.conditional;
    }
}
