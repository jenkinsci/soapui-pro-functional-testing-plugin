package com.smartbear.ready.jenkins;

import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.console.ModelHyperlinkNote;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.Secret;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

@SuppressWarnings("unused")
public class JenkinsSoapUIProTestRunner extends Builder implements SimpleBuildStep {
    private String pathToTestrunner;
    private String pathToProjectFile;
    private String testSuite;
    private String testCase;
    private String testSuiteTags;
    private String testCaseTags;
    private Secret projectPassword;
    private String environment;

    @DataBoundConstructor
    public JenkinsSoapUIProTestRunner(String pathToTestrunner,
                                      String pathToProjectFile) {
        this.pathToTestrunner = pathToTestrunner;
        this.pathToProjectFile = pathToProjectFile;
    }

    public String getPathToTestrunner() {
        return pathToTestrunner;
    }

    public String getPathToProjectFile() {
        return pathToProjectFile;
    }

    public String getTestSuite() {
        return testSuite;
    }

    @DataBoundSetter
    public void setTestSuite(String testSuite) {
        this.testSuite = testSuite;
    }

    public String getTestCase() {
        return testCase;
    }

    @DataBoundSetter
    public void setTestCase(String testCase) {
        this.testCase = testCase;
    }

    public String getTestSuiteTags() {
        return testSuiteTags;
    }

    @DataBoundSetter
    public void setTestSuiteTags(String tags) {
        this.testSuiteTags = tags;
    }

    public String getTestCaseTags() {
        return testCaseTags;
    }

    @DataBoundSetter
    public void setTestCaseTags(String tags) {
        this.testCaseTags = tags;
    }

    public String getProjectPassword() {
        return Secret.toString(projectPassword);
    }

    @DataBoundSetter
    public void setProjectPassword(String projectPassword) {
        this.projectPassword = Secret.fromString(projectPassword);
    }

    public String getEnvironment() {
        return environment;
    }

    @DataBoundSetter
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath workspace, @Nonnull Launcher launcher, @Nonnull TaskListener listener) throws InterruptedException, IOException {
        Proc process = null;
        ProcessRunner processRunner = new ProcessRunner();
        final PrintStream out = listener.getLogger();
        try {
            process = processRunner.run(new ParameterContainer.Builder()
                    .withPathToTestrunner(pathToTestrunner)
                    .withPathToProjectFile(pathToProjectFile)
                    .withTestSuite(testSuite)
                    .withTestCase(testCase)
                    .withTestSuiteTags(testSuiteTags)
                    .withTestCaseTags(testCaseTags)
                    .withProjectPassword(getProjectPassword())
                    .withEnvironment(environment)
                    .withWorkspace(workspace)
                    .build(), run, launcher, listener);
            if (process == null) {
                throw new AbortException("Could not start SoapUI Pro functional testing.");
            } else {

                if (process.join() != 0) {
                    run.setResult(Result.FAILURE);
                }

                if (processRunner.isReportCreated()) {
                    boolean published = new JUnitReportPublisher().publish(run, listener, launcher, processRunner.getReportsFolderPath());
                    if (!published) {
                        out.println("JUnit-style report was not published!");
                    }
                }

                if (processRunner.isPrintableReportCreated()) {
                    String printableReportName = processRunner.getPrintableReportName();

                    FilePath printableReportFileOnSlave = new FilePath(launcher.getChannel(), processRunner.getReportsFolderPath() +
                            processRunner.getPrintableReportPath() + printableReportName);
                    File printableReportFileOnMaster = new File(run.getRootDir().getAbsolutePath() +
                            File.separator + printableReportName);

                    if (copyReportToBuildDir(printableReportFileOnSlave, new FilePath(printableReportFileOnMaster), listener)) {
                        boolean printableReportPublished = new PrintableReportPublisher().publish(run, printableReportFileOnMaster, listener);
                        if (printableReportPublished) {
                            addPrintableReportLinkToConsoleOutput(out, run, printableReportName);
                        } else {
                            out.println("Printable report was not published!");
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace(out);
            throw new AbortException("Could not start SoapUI Pro functional testing.");

        } finally {
            if (process != null) {
                process.kill();
            }
        }
    }

    private boolean copyReportToBuildDir(FilePath fromFile, FilePath toFile, TaskListener listener) {
        try {
            if (fromFile.exists()) {
                toFile.copyFrom(fromFile);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace(listener.getLogger());
        }
        return false;
    }

    private void addPrintableReportLinkToConsoleOutput(PrintStream out, Run<?, ?> run, String printableReportName) {
        String printableReportLink = "/" + run.getUrl() + SoapUIProTestResultsAction.PLUGIN_NAME + "/dynamic/" + printableReportName;
        out.println(ModelHyperlinkNote.encodeTo(printableReportLink, "Click here to view detailed report"));
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    /**
     * Descriptor for {@link JenkinsSoapUIProTestRunner}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     */
    @Extension
    @Symbol("SoapUIPro")
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckPathToTestrunner(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please, set path to testrunner");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckPathToProjectFile(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Please, set path to the SoapUI Pro project");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTestSuite(@QueryParameter String value, @QueryParameter String testCase) {
            if (value.length() == 0 && testCase.length() != 0) {
                return FormValidation.error("Please, enter a test suite for the specified test case");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "SoapUI Pro: Run Functional Test";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }
    }

}
