package pt.up.fe.comp2023.analysis.AnalysisUtils;

import pt.up.fe.comp.jmm.analysis.table.Type;

public class Java {
    static public Type buildType(String typeSignature) {
        if (typeSignature.equals("int[]")) {
            return new Type("int", true);
        } else if (typeSignature.equals("String[]")) {
            return new Type("String", true);
        } else {
            return new Type(typeSignature, false);
        }
    }
}
