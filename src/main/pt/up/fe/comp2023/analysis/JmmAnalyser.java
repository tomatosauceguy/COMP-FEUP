package pt.up.fe.comp2023.analysis;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.parser.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JmmAnalyser implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        JmmNode node = parserResult.getRootNode();
        MySymbolTable symbolTable = new MySymbolTable();
        List<Report> reports = new ArrayList<>();

        Visitor visitor = new Visitor(symbolTable, reports);
        visitor.visit(node,"");

        return new JmmSemanticsResult(parserResult, symbolTable, reports);
    }
}
