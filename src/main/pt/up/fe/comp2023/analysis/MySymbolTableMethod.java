package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.*;

public class MySymbolTableMethod {
    private String name;
    private Type returnType;
    private final List<Map.Entry<Symbol, String>> parameters = new ArrayList<>();
    private final Map<Symbol, Boolean> localVariables = new HashMap<>();

    public MySymbolTableMethod(String name, Type returnType) {
        this.name = name;
        this.returnType = returnType;
    }

    public void addLocalVariable(Symbol variable) {
        localVariables.put(variable, false);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void addParameter(Symbol param) {
        this.parameters.add(Map.entry(param, "param"));
    }
    public List<Symbol> getParameters() {
        List<Symbol> params = new ArrayList<>();
        for (Map.Entry<Symbol, String> param : this.parameters) {
            params.add(param.getKey());
        }
        return params;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("ast.JmmMethod").append("\n");

        builder.append("Name: ").append(name).append(" | Return: ").append(returnType).append("\n");

        builder.append("Parameters").append("\n");
        for (Map.Entry<Symbol, String> param : this.parameters)
            builder.append("\t").append(param.getKey()).append("\n");

        builder.append("Local Variables").append("\n");
        for (Map.Entry<Symbol, Boolean> localVariable : this.localVariables.entrySet()) {
            builder.append("\t").append(localVariable.getKey()).append(" Initialized: ").append(localVariable.getValue()).append("\n");
        }

        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MySymbolTableMethod jmmMethod = (MySymbolTableMethod) o;
        return Objects.equals(name, jmmMethod.name) && Objects.equals(returnType, jmmMethod.returnType) && Objects.equals(parameters, jmmMethod.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, returnType, parameters);
    }
}
