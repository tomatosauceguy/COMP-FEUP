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

        if (TestUtils.getNumReports(parserResult.getReports(), ReportType.ERROR) > 0) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but there are errors from previous stage");
            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errorReport));
        }

        if (parserResult.getRootNode() == null) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but AST root node is null");
            return new JmmSemanticsResult(parserResult, null, Arrays.asList(errorReport));
        }

        JmmNode node = parserResult.getRootNode();
        MySymbolTable symbolTable = new MySymbolTable();
        List<Report> reports = new ArrayList<>();

        System.out.println("Visitor - Filling Symbol Table...");
        Visitor visitor = new Visitor(symbolTable, reports);
        visitor.visit(node,"");
        System.out.println("Symbol Table Filled!");

        System.out.println("Visitor - Semantic Analysis...");
        JmmExpressionAnalyser expressionsAnalyser = new JmmExpressionAnalyser(symbolTable, reports);
        expressionsAnalyser.visit(node, null);
        System.out.println("Semantic Analysis Done!");


        return new JmmSemanticsResult(parserResult, symbolTable, expressionsAnalyser.getReports());
    }
}
