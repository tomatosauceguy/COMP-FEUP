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

    public List<Symbol> getLocalVariables() {
        return new ArrayList<>(this.localVariables.keySet());
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

    public List<Type> getParameterTypes() {
        List<Type> params = new ArrayList<>();

        for (Map.Entry<Symbol, String> parameter : parameters) {
            params.add(parameter.getKey().getType());
        }
        return params;
    }

    public boolean initializeField(Symbol symbol) {
        if (this.localVariables.containsKey(symbol)) {
            this.localVariables.put(symbol, true);
            return true;
        }
        return false;
    }

    public Map.Entry<Symbol, Boolean> getField(String name) {
        for (Map.Entry<Symbol, Boolean> field : this.localVariables.entrySet()) {
            if (field.getKey().getName().equals(name))
                return field;
        }

        for (Map.Entry<Symbol, String> param : this.parameters) {
            if (param.getKey().getName().equals(name))
                return Map.entry(param.getKey(), true);
        }

        return null;
    }

    public static List<Type> parseParameters(String params) {
        if (params.equals("")) return new ArrayList<>();

        String[] typesString = params.split(",");

        List<Type> types = new ArrayList<>();

        for (String s : typesString) {
            String[] aux = s.split(" ");
            types.add(new Type(aux[0], aux.length == 2));
        }

        return types;
    }

    public static boolean matchParameters(List<Type> types1, List<Type> types2) {
        for (int i = 0; i < types1.size(); i++) {
            if (!types1.get(i).equals(types2.get(i))) {
                return false;
            }
        }
        return true;
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
