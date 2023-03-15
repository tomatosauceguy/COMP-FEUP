package pt.up.fe.comp2023.analysis.analysers;

import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.comp2023.analysis.SemanticAnalyser;
import pt.up.fe.comp2023.analysis.SymbolTableBuilder;

import java.util.ArrayList;
import java.util.List;

public abstract class SemanticAnalyserVisitor extends AJmmVisitor<SymbolTableBuilder, Boolean> implements SemanticAnalyser {
    private List<Report> reports;

    public SemanticAnalyserVisitor() {
        this.reports = new ArrayList<>();
    }

    @Override
    public List<Report> getReports() {
        return this.reports;
    }

    @Override
    public void addReport(JmmNode node, String message) {
        this.reports.add(new Report(ReportType.ERROR, Stage.SEMANTIC, Integer.parseInt(node.get("line")), Integer.parseInt(node.get("col")) , message));
    }
}
