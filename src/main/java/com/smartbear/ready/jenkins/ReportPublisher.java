package com.smartbear.ready.jenkins;

import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;

import javax.annotation.Nonnull;
import java.io.File;

public class ReportPublisher {

    boolean publish(@Nonnull Run<?, ?> run, TaskListener listener, @Nonnull FilePath workspace) {
        try {
            File reportFile = new File(workspace + ProcessRunner.READYAPI_REPORT_DIRECTORY + File.separator + "report.xml");
            if (!reportFile.exists()) {
                throw new Exception("Report file does not exist!");
            }
            synchronized (run) {
                TestResultAction testResultAction = run.getAction(TestResultAction.class);
                boolean testResultActionExists = true;
                if (testResultAction == null) {
                    testResultActionExists = false;
                    TestResult testResult = new TestResult(true);
                    testResult.parse(reportFile);
                    testResultAction = new TestResultAction(run, testResult, listener);
                } else {
                    TestResult testResult = testResultAction.getResult();
                    testResult.parse(reportFile);
                    testResult.tally();
                    testResultAction.setResult(testResult, listener);
                }

                if (!testResultActionExists) {
                    run.addAction(testResultAction);
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
