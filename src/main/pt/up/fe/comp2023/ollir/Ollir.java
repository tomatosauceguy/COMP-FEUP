package pt.up.fe.comp2023.ollir;

import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp2023.analysis.MySymbolTable;

import java.util.Collections;

public class Ollir implements JmmOptimization {


    @Override
    public OllirResult toOllir(JmmSemanticsResult jmmSemanticsResult) {

        OllirGenerator ollirGenerator = new OllirGenerator((MySymbolTable) jmmSemanticsResult.getSymbolTable());

        ollirGenerator.visit(jmmSemanticsResult.getRootNode());

        String code = ollirGenerator.getOllir();

        System.out.println(code);

        return new OllirResult(jmmSemanticsResult, code, Collections.emptyList());
    }
}

