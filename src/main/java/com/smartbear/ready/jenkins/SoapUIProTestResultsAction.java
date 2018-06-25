package com.smartbear.ready.jenkins;

import hudson.model.Action;
import hudson.model.Run;

import java.io.File;

public class SoapUIProTestResultsAction implements Action {
    public static final String PLUGIN_NAME = "soapui-pro-functional-testing";
    private final Run<?, ?> run;
    private final DynamicReportAction dynamic;
    private String printableReportName;

    SoapUIProTestResultsAction(Run<?, ?> run, File printableReportFile) {
        this.run = run;
        this.printableReportName = printableReportFile.getName();
        this.dynamic = new DynamicReportAction(printableReportFile);
    }

    @Override
    public String getIconFileName() {
        return "/plugin/" + PLUGIN_NAME + "/images/logo.png";
    }

    @Override
    public String getDisplayName() {
        return "SoapUI Pro Test Results";
    }

    @Override
    public String getUrlName() {
        return PLUGIN_NAME;
    }

    public Run<?, ?> getBuild() {
        return run;
    }

    public String getPrintableReportName() {
        return printableReportName;
    }

    public DynamicReportAction getDynamic() {
        return dynamic;
    }

}
