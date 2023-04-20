package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.*;

public class MySymbolTable implements SymbolTable {
    private String program;
    private final List<String> imports = new ArrayList<>();
    private String className;
    private String superClassName;
    private List<Symbol> fields =new ArrayList<>();
    private List<MySymbolTableMethod> methods = new ArrayList<>();
    private MySymbolTableMethod currentMethod;

    private String currentMethodName = "";

    public void setCurrentMethodName(String currentMethodName) {
        this.currentMethodName = currentMethodName;
    }

    public String getCurrentMethodName() {
        return currentMethodName;
    }


    public void setClassName(String className) {
        this.className = className;
    }

    public void setSuperClassName(String superClassName) {
        this.superClassName = superClassName;
    }

    public void addImport(String importStatement) {
        imports.add(importStatement);
    }

    public void addField(Symbol field) {
        this.fields.add(field);
    }

    public MySymbolTableMethod addMethod(String name, Type returnType) {
        currentMethod = new MySymbolTableMethod(name, returnType);
        methods.add(currentMethod);
        return currentMethod;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SYMBOL TABLE\n");
        builder.append("Imports").append("\n");
        for (String importStmt : imports)
            builder.append("\t").append(importStmt).append("\n");

        builder.append("Class Name: ").append(className).append(" | Extends: ").append(superClassName).append("\n");

        builder.append("--- Local Variables ---").append("\n");
        for (var field : fields)
            builder.append("\t").append(field).append(" Initialized: ").append(field).append("\n");

        builder.append("--- Methods ---").append("\n");
        for (MySymbolTableMethod method : this.methods) {
            builder.append(method);
            builder.append("---------").append("\n");
        }

        return builder.toString();
    }

    @Override
    public List<String> getImports() {
        return imports;
    }

    @Override
    public String getClassName() { return className; }

    @Override
    public String getSuper() {
        return superClassName;
    }

    @Override
    public List<Symbol> getFields() {
        return this.fields;
    }

    @Override
    public List<String> getMethods() {
        System.out.println("In get methods");
        System.out.println(this.methods);
        List<String> methods = new ArrayList<>();
        for (MySymbolTableMethod method : this.methods) {
            methods.add(method.getName());
        }

        return methods;
    }

    public MySymbolTableMethod getCurrentMethod() {
        return currentMethod;
    }

    @Override
    public Type getReturnType(String methodName) {
        List<Type> params = new ArrayList<>();
        String[] parts = methodName.split("::");
        methodName = parts[0];

        if (parts.length > 1) {
            for (int i = 1; i < parts.length; i++) {
                String[] parts2 = parts[i].split(":");
                params.add(new Type(parts2[0], parts2[1].equals("true")));
            }
        } else {
            for (MySymbolTableMethod method : methods) {
                if(method.getName().equals(methodName)) {
                    return method.getReturnType();
                }
            }
        }

        for (MySymbolTableMethod method : methods) {
            if(method.getName().equals(methodName)) {
                List<Symbol> currentparams = method.getParameters();
                boolean found = true;
                if (currentparams.size() != params.size()) continue;
                for (int i=0; i<params.size(); i++) {
                    if (!currentparams.get(i).getType().equals(params.get(i))) {
                        found = false;
                        break;
                    }
                }
                if (found) return method.getReturnType();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getParameters(String methodName) {
        for (MySymbolTableMethod method : this.methods){
            if (method.getName().equals(methodName)){
                return method.getParameters();
            }
        }
        return null;
    }

    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        return null;
    }

}