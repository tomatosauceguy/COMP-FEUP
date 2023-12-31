package pt.up.fe.comp2023.jasmin;

import org.specs.comp.ollir.ClassUnit;
import org.specs.comp.ollir.OllirErrorException;
import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackendStage implements JasminBackend {
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();
        try{
            ollirClass.checkMethodLabels();

            String JasminCode = new JasminGenerator(ollirClass).dealWithClass();

            List<Report> reports = new ArrayList<>();
            return new JasminResult(ollirResult, JasminCode, reports);
        } catch (OllirErrorException f){
            return new JasminResult(ollirClass.getClassName(), null, List.of(Report.newError(Stage.GENERATION, -1, -1,
                    "Expection during Jasmin Generator", f)));
        }
    }
}
