package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.*;

import java.util.HashMap;
import java.util.Map;

public class JasminGenerator {
    private final ClassUnit classUnit;
    private int counterStack;
    private int counterMax;
    private int conditional;


    public JasminGenerator(ClassUnit classUnit){
        this.classUnit = classUnit;
    }

    public String dealWithClass(){
        StringBuilder string = new StringBuilder();

        string.append(".class ").append(classUnit.getClassName()).append("\n");

        if (classUnit.getSuperClass() != null) {
            string.append(".super ").append(classUnit.getSuperClass()).append("\n");
        } else {
            string.append(".super java/lang/Object\n");
        }

        for (Field f : classUnit.getFields()) {
            string.append(".field '").append(f.getFieldName()).append("' ").append(this.convertType(f.getFieldType())).append("\n");
        }

        for (Method method : classUnit.getMethods()) {
            this.counterStack = 0;
            this.counterMax = 0;

            string.append(this.dealWithMethodHeader(method));
            String instructions = this.dealWithMethodInstructions(method);
            if (!method.isConstructMethod()) {
                string.append(this.dealWithMethodLimits(method));
                string.append(instructions);
            }
        }

        return string.toString();
    }

    private String dealWithMethodHeader(Method method) {
        if (method.isConstructMethod()) {
            String classSuper = "java/lang/Object";

            if (classUnit.getSuperClass() != null) {
                classSuper = classUnit.getSuperClass();
            }

            return "\n.method public <init>()V\naload_0\ninvokespecial " + classSuper + ".<init>()V\nreturn\n.end method\n";
        }

        StringBuilder string = new StringBuilder("\n.method").append(" ").append(method.getMethodAccessModifier().name().toLowerCase()).append(" ");
        if (method.isStaticMethod()) {
            string.append("static ");
        } else if (method.isFinalMethod()) {
            string.append("final ");
        }
        // Parameters type
        string.append(method.getMethodName()).append("(");
        for (Element element : method.getParams()) {
            string.append(convertType(element.getType()));
        }
        // Return type
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


    private String dealWithMethodInstructions(Method method){
        StringBuilder string = new StringBuilder();
        method.getVarTable();
        for (Instruction instruction : method.getInstructions()) {
            string.append(dealWithInstruction(instruction, method.getVarTable(), method.getLabels()));
            if (instruction instanceof CallInstruction && ((CallInstruction) instruction).getReturnType().getTypeOfElement() != ElementType.VOID) {
                string.append("pop\n");
                this.incrementStackCounter(-1);
            }
        }
        string.append("\n.end method\n");
        return string.toString();
    }

    private String dealWithInstruction(Instruction instruction, HashMap<String, Descriptor> table, HashMap<String, Instruction> methodLabels){
        StringBuilder string = new StringBuilder();
        for (Map.Entry<String, Instruction> entry : methodLabels.entrySet()) {
            if (entry.getValue().equals(instruction)) {
                string.append(entry.getKey()).append(":\n");
            }
        }
        return switch (instruction.getInstType()) {
            case ASSIGN ->
                    string.append(dealWithAssignment((AssignInstruction) instruction, table)).toString();
            case NOPER ->
                    string.append(dealWithSingleOpInstruction((SingleOpInstruction) instruction, table)).toString();
            case BINARYOPER ->
                    string.append(dealWithBinaryOpInstruction((BinaryOpInstruction) instruction, table)).toString();
            case UNARYOPER ->
                    string.append(dealWithUnaryOperationInstruction((UnaryOpInstruction) instruction, table)).toString();
            case BRANCH ->
                    string.append(dealWithCondBranchInstruction((CondBranchInstruction) instruction, table)).toString();
            case CALL ->
                    string.append(dealWithCallInstruction((CallInstruction) instruction, table)).toString();
            case GOTO ->
                    string.append(dealWithGotoInstruction((GotoInstruction) instruction)).toString();
            case PUTFIELD ->
                    string.append(dealWithPutFieldInstruction((PutFieldInstruction) instruction, table)).toString();
            case GETFIELD ->
                    string.append(dealWithGetFieldInstruction((GetFieldInstruction) instruction, table)).toString();
            case RETURN ->
                    string.append(dealWithReturnInstruction((ReturnInstruction) instruction, table)).toString();
        };
    }

    private String dealWithAssignment(AssignInstruction instruction, HashMap<String, Descriptor> table){
        String string = "";
        Operand operand = (Operand) instruction.getDest();

        if (operand instanceof ArrayOperand arraop) {
            string += String.format("aload%s\n", this.getVirtualReg(arraop.getName(), table));
            this.incrementStackCounter(1);
            string += loadElement(arraop.getIndexOperands().get(0), table);
        }
        string += dealWithInstruction(instruction.getRhs(), table, new HashMap<>());
        if(!(operand.getType().getTypeOfElement().equals(ElementType.OBJECTREF) && instruction.getRhs() instanceof CallInstruction)) {
            string += this.storeElement(operand, table);
        }
        return string;
    }

    private String dealWithCallInstruction(CallInstruction instruction, HashMap<String, Descriptor> table){
        String string = "";
        CallType callType = instruction.getInvocationType();

        switch (callType) {
            case invokespecial, invokevirtual ->
                    string += this.dealWithInvoke(instruction, table, callType, ((ClassType) instruction.getFirstArg().getType()).getName());
            case invokestatic ->
                    string += this.dealWithInvoke(instruction, table, callType, ((Operand) instruction.getFirstArg()).getName());
            case arraylength -> {
                string += this.loadElement(instruction.getFirstArg(), table);
                string += "arraylength\n";
            }
            case NEW -> string += this.dealWithNewObject(instruction, table);
            default -> {
                return "Erro in CallInstruction";
            }
        }
        return string;
    }

    private String dealWithInvoke(CallInstruction instruction, HashMap<String, Descriptor> table, CallType call, String classN){
        StringBuilder string = new StringBuilder();
        String function = ((LiteralElement) instruction.getSecondArg()).getLiteral();
        StringBuilder params = new StringBuilder();

        if (!function.equals("\"<init>\"")) {
            string.append(this.loadElement(instruction.getFirstArg(), table));
        }

        int nParams = 0;
        for (Element element : instruction.getListOfOperands()) {
            string.append(this.loadElement(element, table));
            params.append(this.convertType(element.getType()));
            nParams++;
        }

        if (!instruction.getInvocationType().equals(CallType.invokestatic)) {
            nParams += 1;
        }
        this.incrementStackCounter(-nParams);
        if (instruction.getReturnType().getTypeOfElement() != ElementType.VOID) {
            this.incrementStackCounter(1);
        }
        string.append(call.name()).append(" ").append(this.getObjectClassName(classN)).append(".").append(function.replace("\"", "")).append("(").append(params).append(")").append(this.convertType(instruction.getReturnType())).append("\n");
        if (function.equals("\"<init>\"") && !classN.equals("this")) {
            string.append(this.storeElement((Operand) instruction.getFirstArg(), table));
        }
        return string.toString();
    }

    private String dealWithNewObject(CallInstruction instruction, HashMap<String, Descriptor> table){
        Element elem = instruction.getFirstArg();
        String string = "";

        if (elem.getType().getTypeOfElement().equals(ElementType.ARRAYREF)) {
            string += this.loadElement(instruction.getListOfOperands().get(0), table);
            string += "newarray int\n";
        }
        else if (elem.getType().getTypeOfElement().equals(ElementType.OBJECTREF)){
            this.incrementStackCounter(2);
            string += "new " + this.getObjectClassName(((Operand)elem).getName()) + "\ndup\n";
        }
        return string;
    }

    private String dealWithGotoInstruction(GotoInstruction instruction) {
        return String.format("goto %s\n", instruction.getLabel());
    }

    private String dealWithReturnInstruction(ReturnInstruction instruction, HashMap<String, Descriptor> table){
        if(!instruction.hasReturnValue()) return "return";
        String string = "";

        switch (instruction.getOperand().getType().getTypeOfElement()) {
            case VOID -> string = "return";
            case INT32, BOOLEAN -> {
                string = loadElement(instruction.getOperand(), table);
                this.incrementStackCounter(-1);
                string += "ireturn";
            }
            case ARRAYREF, OBJECTREF -> {
                string = loadElement(instruction.getOperand(), table);
                this.incrementStackCounter(-1);
                string += "areturn";
            }
            default -> {
            }
        }
        return string;
    }

    private String dealWithUnaryOperationInstruction(UnaryOpInstruction instruction, HashMap<String, Descriptor> varTable) {
        StringBuilder string = new StringBuilder();

        string.append(this.getLoadToStack(instruction.getOperand(), varTable))
                .append("\t").append(this.getOperation(instruction.getOperation()));

        if (instruction.getOperation().getOpType() == OperationType.NOTB) {
            string.append(this.getBooleanOperationToStack());
        } else {
            string.append("Error in dealWithUnaryOperationInstruction");
        }

        string.append("\n");
        return string.toString();
    }

    private String dealWithCondBranchInstruction(CondBranchInstruction condInstruction, HashMap<String, Descriptor> table) {
        StringBuilder string = new StringBuilder();

        Instruction cond;
        if (condInstruction instanceof OpCondInstruction opCondInstruction) {
            cond = opCondInstruction.getCondition();

        } else if (condInstruction instanceof SingleOpCondInstruction singleOpCondInstruction) {
            cond = singleOpCondInstruction.getCondition();

        } else {
            return "Error in dealWithCondBranchInstruction\n";
        }

        String op;
        switch (cond.getInstType()) {
            case UNARYOPER -> {
                UnaryOpInstruction unaryOpInstruction = (UnaryOpInstruction) cond;
                if (unaryOpInstruction.getOperation().getOpType() == OperationType.NOTB) {
                    string.append(this.getLoadToStack(unaryOpInstruction.getOperand(), table));
                    op = "ifeq";
                } else {
                    string.append("Error invalid unaryOperation\n");
                    string.append(this.dealWithInstruction(cond, table, new HashMap<>()));
                    op = "ifne";
                }
            }
            case BINARYOPER -> {
                BinaryOpInstruction binaryOpInstruction = (BinaryOpInstruction) cond;
                switch (binaryOpInstruction.getOperation().getOpType()) {
                    case LTH -> {
                        Element left = binaryOpInstruction.getLeftOperand();
                        Element right = binaryOpInstruction.getRightOperand();

                        Integer parsed = null;
                        Element other = null;
                        op = "if_icmplt";

                        if (left instanceof LiteralElement) {
                            String literal = ((LiteralElement) left).getLiteral();
                            parsed = Integer.parseInt(literal);
                            other = right;
                            op = "ifgt";

                        } else if (right instanceof LiteralElement) {
                            String literal = ((LiteralElement) right).getLiteral();
                            parsed = Integer.parseInt(literal);
                            other = left;
                            op = "iflt";
                        }

                        if (parsed != null && parsed == 0) {
                            string.append(this.getLoadToStack(other, table));

                        } else {
                            string.append(this.getLoadToStack(left, table))
                                    .append(this.getLoadToStack(right, table));

                            op = "if_icmplt";
                        }

                    }
                    case ANDB -> {
                        string.append(this.dealWithInstruction(cond, table, new HashMap<>()));
                        op = "ifne";
                    }
                    default -> {
                        string.append("Error invalid binaryOperation\n");
                        string.append(this.dealWithInstruction(cond, table, new HashMap<>()));
                        op = "ifne";
                    }
                }
            }
            default -> {
                string.append(this.dealWithInstruction(cond, table, new HashMap<>()));
                op = "ifne";
            }
        }

        string.append("\t").append(op).append(" ").append(condInstruction.getLabel()).append("\n");

        if (op.equals("if_icmplt")) {
            this.incrementStackCounter(-2);
        } else {
            this.incrementStackCounter(-1);
        }

        return string.toString();
    }

    private String getLoadToStack(Element element, HashMap<String, Descriptor> varTable) {
        StringBuilder string = new StringBuilder();

        if (element instanceof LiteralElement) {
            String literal = ((LiteralElement) element).getLiteral();

            if (element.getType().getTypeOfElement() == ElementType.INT32
                    || element.getType().getTypeOfElement() == ElementType.BOOLEAN) {

                int parsedInt = Integer.parseInt(literal);

                if (parsedInt >= -1 && parsedInt <= 5) {
                    string.append("\ticonst_");
                } else if (parsedInt >= -128 && parsedInt <= 127) {
                    string.append("\tbipush ");
                } else if (parsedInt >= -32768 && parsedInt <= 32767) {
                    string.append("\tsipush ");
                } else {
                    string.append("\tldc ");
                }

                if (parsedInt == -1) {
                    string.append("m1");
                } else {
                    string.append(parsedInt);
                }

            } else {
                string.append("\tldc ").append(literal);
            }

            this.incrementStackCounter(1);

        } else if (element instanceof ArrayOperand) {
            ArrayOperand operand = (ArrayOperand) element;

            string.append("\taload").append(this.getVirtualReg(operand.getName(), varTable)).append("\n");
            this.incrementStackCounter(1);

            string.append(getLoadToStack(operand.getIndexOperands().get(0), varTable));
            string.append("\tiaload");

            this.incrementStackCounter(-1);
        } else if (element instanceof Operand) {
            Operand operand = (Operand) element;
            switch (operand.getType().getTypeOfElement()) {
                case INT32, BOOLEAN -> string.append("\tiload").append(this.getVirtualReg(operand.getName(), varTable));
                case OBJECTREF, STRING, ARRAYREF -> string.append("\taload").append(this.getVirtualReg(operand.getName(), varTable));
                case THIS -> string.append("\taload_0");
                default -> string.append("Error getting operand in getLoadToStack()");
            }

            this.incrementStackCounter(1);
        } else {
            string.append("Error invalid element");
        }

        string.append("\n");
        return string.toString();
    }

    private String dealWithPutFieldInstruction(PutFieldInstruction instruction, HashMap<String, Descriptor> table){
        String string = "";

        Operand obj = (Operand)instruction.getFirstOperand();
        Operand var = (Operand)instruction.getSecondOperand();
        Element value = instruction.getThirdOperand();

        string += this.loadElement(obj, table);
        string += this.loadElement(value, table);

        this.incrementStackCounter(-2);
        return string + "putfield " + classUnit.getClassName() + "/" + var.getName() + " " + convertType(var.getType()) + "\n";
    }

    private String dealWithGetFieldInstruction(GetFieldInstruction instruction, HashMap<String, Descriptor> table){
        String string = "";

        Operand obj = (Operand)instruction.getFirstOperand();
        Operand var = (Operand)instruction.getSecondOperand();
        string += this.loadElement(obj, table);

        return string + "getfield " + classUnit.getClassName() + "/" + var.getName() + " " + convertType(var.getType()) +  "\n";
    }

    private String dealWithBinaryOpInstruction(BinaryOpInstruction instruction, HashMap<String, Descriptor> table){
        return switch (instruction.getOperation().getOpType()) {
            case ADD, SUB, MUL, DIV -> this.dealWithIntOperation(instruction, table);
            case LTH, GTE, ANDB, NOTB -> this.dealWithBooleanOperation(instruction, table);
            default -> "Error in dealWithBinaryOpInstruction";
        };
    }

    private String dealWithIntOperation(BinaryOpInstruction instruction, HashMap<String, Descriptor> table){
        String leftOp = loadElement(instruction.getLeftOperand(), table);
        String rightOp = loadElement(instruction.getRightOperand(), table);
        String operator;
        switch (instruction.getOperation().getOpType()) {
            case ADD -> operator = "iadd\n";
            case SUB -> operator = "isub\n";
            case MUL -> operator = "imul\n";
            case DIV -> operator = "idiv\n";
            default -> {
                return "Error in IntOperation\n";
            }
        }
        this.incrementStackCounter(-1);
        return leftOp + rightOp + operator;
    }

    private String dealWithBooleanOperation(BinaryOpInstruction instruction, HashMap<String, Descriptor> table) {
        OperationType opt = instruction.getOperation().getOpType();
        StringBuilder string = new StringBuilder();
        switch (instruction.getOperation().getOpType()) {
            case LTH, GTE -> {
                String leftOperand = loadElement(instruction.getLeftOperand(), table);
                String rightOperand = loadElement(instruction.getRightOperand(), table);
                string.append(leftOperand)
                        .append(rightOperand)
                        .append(this.dealWithRelationalOperation(opt, this.getTrueLabel()))
                        .append("iconst_1\n")
                        .append("goto ").append(this.getEndIfLabel()).append("\n")
                        .append(this.getTrueLabel()).append(":\n")
                        .append("iconst_0\n")
                        .append(this.getEndIfLabel()).append(":\n");
                this.incrementStackCounter(-1);
            }
            case ANDB -> {
                String ifeq = "ifeq " + this.getTrueLabel() + "\n";
                string.append(loadElement(instruction.getLeftOperand(), table)).append(ifeq);
                this.incrementStackCounter(-1);
                string.append(loadElement(instruction.getRightOperand(), table)).append(ifeq);
                this.incrementStackCounter(-1);
                string.append("iconst_1\n")
                        .append("goto ").append(this.getEndIfLabel()).append("\n")
                        .append(this.getTrueLabel()).append(":\n")
                        .append("iconst_0\n")
                        .append(this.getEndIfLabel()).append(":\n");
                this.incrementStackCounter(1);
            }
            case NOTB -> {
                String operand = loadElement(instruction.getLeftOperand(), table);
                string.append(operand)
                        .append("ifne ").append(this.getTrueLabel()).append("\n")
                        .append("iconst_1\n")
                        .append("goto ").append(this.getEndIfLabel()).append("\n")
                        .append(this.getTrueLabel()).append(":\n")
                        .append("iconst_0\n")
                        .append(this.getEndIfLabel()).append(":\n");
            }
            default -> {
                return "Error in BooleansOperations\n";
            }
        }
        this.conditional++;
        return string.toString();
    }

    private String dealWithSingleOpInstruction(SingleOpInstruction instruction, HashMap<String, Descriptor> table) {
        return loadElement(instruction.getSingleOperand(), table);
    }

    // Maybe put this in a utils file

    private String getOperation(Operation operation) {
        return switch (operation.getOpType()) {
            case LTH -> "if_icmplt";
            case ANDB -> "iand";
            case NOTB -> "ifeq";

            case ADD -> "iadd";
            case SUB -> "isub";
            case MUL -> "imul";
            case DIV -> "idiv";

            default -> "Error in getOperation";
        };
    }

    private String getBooleanOperationToStack() {
        return " TRUE" + this.conditional + "\n"
                + "\ticonst_0\n"
                + "\tgoto NEXT" + this.conditional + "\n"
                + "TRUE" + this.conditional + ":\n"
                + "\ticonst_1\n"
                + "NEXT" + this.conditional++ + ":";
    }

    private String convertType(Type type) {
        ElementType elem = type.getTypeOfElement();
        String string = "";

        if (elem == ElementType.ARRAYREF) {
            elem = ((ArrayType) type).getTypeOfElements();
            string += "[";
        }

        switch (elem) {
            case INT32 -> {
                return string + "I";
            }
            case BOOLEAN -> {
                return string + "Z";
            }
            case STRING -> {
                return string + "Ljava/lang/String;";
            }
            case OBJECTREF -> {
                String className = ((ClassType) type).getName();
                return string + "L" + this.getObjectClassName(className) + ";";
            }
            case CLASS -> {
                return "CLASS";
            }
            case VOID -> {
                return "V";
            }
            default -> {
                return "Error converting ElementType";
            }
        }
    }

    private String getObjectClassName(String className) {
        for (String _import : classUnit.getImports()) {
            if (_import.endsWith("." + className)) {
                return _import.replaceAll("\\.", "/");
            }
        }
        return className;
    }

    private String getVirtualReg(String varName, HashMap<String, Descriptor> table) {
        int virtualReg = table.get(varName).getVirtualReg();
        if (virtualReg > 3) {
            return " " + virtualReg;
        }
        return "_" + virtualReg;
    }

    private String loadElement(Element element, HashMap<String, Descriptor> table){
        if (element instanceof LiteralElement) {
            String num = ((LiteralElement) element).getLiteral();
            this.incrementStackCounter(1);
            return this.selectConstType(num) + "\n";
        }
        else if (element instanceof ArrayOperand arrayop) {
            String string = String.format("aload%s\n", this.getVirtualReg(arrayop.getName(), table));
            this.incrementStackCounter(1);
            string += loadElement(arrayop.getIndexOperands().get(0), table);
            this.incrementStackCounter(-1);
            return string + "iaload\n";
        }
        else if (element instanceof Operand op) {
            switch (op.getType().getTypeOfElement()) {
                case THIS -> {
                    this.incrementStackCounter(1);
                    return "aload_0\n";
                }
                case INT32, BOOLEAN -> {
                    this.incrementStackCounter(1);
                    return String.format("iload%s\n", this.getVirtualReg(op.getName(), table));
                }
                case OBJECTREF, ARRAYREF -> {
                    this.incrementStackCounter(1);
                    return String.format("aload%s\n", this.getVirtualReg(op.getName(), table));
                }
                case CLASS -> {
                    return "";
                }
                default -> {
                    return "Error in operand loadElements\n";
                }
            }
        }
        System.out.println(element);
        return "Error in loadElement\n";
    }

    private String storeElement(Operand op, HashMap<String, Descriptor> table){
        if(op instanceof ArrayOperand) {
            this.incrementStackCounter(-3);
            return "iastore\n";
        }

        switch (op.getType().getTypeOfElement()) {
            case INT32, BOOLEAN -> {
                this.incrementStackCounter(-1);
                return String.format("istore%s\n", this.getVirtualReg(op.getName(), table));
            }
            case OBJECTREF, ARRAYREF -> {
                this.incrementStackCounter(-1);
                return String.format("astore%s\n", this.getVirtualReg(op.getName(), table));
            }
            default -> {
                return "Error in storeElements";
            }
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
        return switch (opt) {
            case LTH -> String.format("if_icmpge %s\n", string);
            case GTE -> String.format("if_icmplt %s\n", string);
            default -> "Error in RelationalOperations\n";
        };
    }

    private String getTrueLabel() {
        return "myTrue" + this.conditional;
    }

    private String getEndIfLabel() {
        return "myEndIf" + this.conditional;
    }

    private void incrementStackCounter(int i) {
        this.counterStack += i;
        if (this.counterStack > this.counterMax) {
            this.counterMax = counterStack;
        }
    }
}
