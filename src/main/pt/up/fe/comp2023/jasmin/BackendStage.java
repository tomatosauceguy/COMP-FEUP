package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp2023.jasmin.JasminGenerator;

import java.util.ArrayList;
import java.util.List;

public class BackendStage implements JasminBackend {
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();

        String jasminCode = new JasminGenerator(ollirClass).dealWithClass();

        List<Report> reports = new ArrayList<>();

        return new JasminResult(ollirResult, jasminCode, reports);
    }
}
