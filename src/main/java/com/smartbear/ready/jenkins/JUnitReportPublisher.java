package com.smartbear.ready.jenkins;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;

import javax.annotation.Nonnull;
import java.io.File;

public class JUnitReportPublisher {
    private static final String JUNIT_REPORT_NAME = "report.xml";

    boolean publish(@Nonnull Run<?, ?> run, TaskListener listener, @Nonnull Launcher launcher, String reportsFolderPath) {
        File junitReportTempFileOnMaster = new File(run.getRootDir().getAbsolutePath() + File.separator + JUNIT_REPORT_NAME);
        try {
            FilePath junitReportFileOnSlave = new FilePath(launcher.getChannel(), reportsFolderPath + JUNIT_REPORT_NAME);
            if (!junitReportFileOnSlave.exists()) {
                throw new Exception("Report file does not exist!");
            }
            new FilePath(junitReportTempFileOnMaster).copyFrom(junitReportFileOnSlave);

            synchronized (run) {
                TestResultAction testResultAction = run.getAction(TestResultAction.class);
                boolean testResultActionExists = true;
                if (testResultAction == null) {
                    testResultActionExists = false;
                    TestResult testResult = new TestResult(true);
                    testResult.parse(junitReportTempFileOnMaster);
                    testResultAction = new TestResultAction(run, testResult, listener);
                } else {
                    TestResult testResult = testResultAction.getResult();
                    testResult.parse(junitReportTempFileOnMaster);
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
        } finally {
            if (junitReportTempFileOnMaster.exists()) {
                junitReportTempFileOnMaster.delete();
            }
        }

        return true;
    }

}
