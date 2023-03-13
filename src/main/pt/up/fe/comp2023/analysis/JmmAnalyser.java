package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.parser.JmmParserResult;

public class JmmAnalyser implements JmmAnalysis {
    SymbolTableBuilder symbolTable;

    public JmmAnalyser() {
        this.symbolTable = new SymbolTableBuilder();
    }

    public SymbolTableBuilder getSymbolTable() {
        return this.symbolTable;
    }

    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult jmmParserResult) {
        return null;
    }
}
