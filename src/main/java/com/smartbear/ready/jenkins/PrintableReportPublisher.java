package com.smartbear.ready.jenkins;

import hudson.model.Run;
import hudson.model.TaskListener;

import javax.annotation.Nonnull;
import java.io.File;

public class PrintableReportPublisher {
    boolean publish(@Nonnull Run<?, ?> run, File printableReportFile, TaskListener listener) {
        try {
            synchronized (run) {
                SoapUIProTestResultsAction currentAction = run.getAction(SoapUIProTestResultsAction.class);
                if (currentAction == null) {
                    currentAction = new SoapUIProTestResultsAction(run, printableReportFile);
                    run.addAction(currentAction);
                }
                listener.getLogger().println("ReadyAPI Test Results were published!");
            }
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
            return false;
        }

        return true;
    }
}
