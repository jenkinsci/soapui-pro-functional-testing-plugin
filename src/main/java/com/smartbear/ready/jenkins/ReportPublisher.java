package com.smartbear.ready.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;

import java.io.File;

public class ReportPublisher {

    boolean publish(AbstractBuild build, BuildListener listener) {
        try {
            File reportFile = new File(build.getWorkspace() + "\\ReadyAPI_report\\report.xml");
            if (!reportFile.exists()) {
                throw new Exception("Report file does not exist!");
            }
            synchronized (build) {
                TestResultAction testResultAction = build.getAction(TestResultAction.class);
                boolean testResultActionExists = true;
                if (testResultAction == null) {
                    testResultActionExists = false;
                    TestResult testResult = new TestResult(true);
                    testResult.parse(reportFile);
                    testResultAction = new TestResultAction(build, testResult, listener);
                } else {
                    TestResult testResult = testResultAction.getResult();
                    testResult.parse(reportFile);
                    testResult.tally();
                    testResultAction.setResult(testResult, listener);
                }

                if (!testResultActionExists) {
                    build.getActions().add(testResultAction);
                }
                listener.getLogger().println("JUnit-style report was published.");
            }

        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return false;
        }

        return true;
    }

}
