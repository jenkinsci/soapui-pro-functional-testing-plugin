package com.smartbear.ready.jenkins;

import hudson.AbortException;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class JenkinsSoapUIProTestRunner extends Builder {
    private final String pathToTestrunner;
    private final String pathToProjectFile;
    private final String testSuite;
    private final String testCase;
    private final String projectPassword;
    private final String environment;

    // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
    @DataBoundConstructor
    public JenkinsSoapUIProTestRunner(String pathToTestrunner,
                                      String pathToProjectFile,
                                      String testSuite,
                                      String testCase,
                                      String projectPassword,
                                      String environment) {
        this.pathToTestrunner = pathToTestrunner;
        this.pathToProjectFile = pathToProjectFile;
        this.testSuite = testSuite;
        this.testCase = testCase;
        this.projectPassword = projectPassword;
        this.environment = environment;
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

    public String getTestCase() {
        return testCase;
    }

    public String getProjectPassword() {
        return projectPassword;
    }

    public String getEnvironment() {
        return environment;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, final BuildListener listener) throws AbortException {
        Process process = null;
        ProcessRunner processRunner = new ProcessRunner();
        final PrintStream out = listener.getLogger();
        try {
            process = processRunner.run(out, new ParameterContainer.Builder()
                    .withPathToTestrunner(pathToTestrunner)
                    .withPathToProjectFile(pathToProjectFile)
                    .withTestSuite(testSuite)
                    .withTestCase(testCase)
                    .withProjectPassword(projectPassword)
                    .withEnvironment(environment)
                    .withWorkspace(new File(build.getWorkspace().toURI()))
                    .build(), build);
            if (process == null) {
                throw new AbortException("Could not start SoapUI Pro functional testing.");
            }
        } catch (Exception e) {
            e.printStackTrace(out);
            if (process != null) {
                process.destroy();
            }
            throw new AbortException("Could not start SoapUI Pro functional testing.");

        } finally {
            if (process != null) {
                try {
                    process.waitFor();
                    if (build.getResult() != Result.FAILURE && processRunner.isReportCreated()) {
                        boolean published = new ReportPublisher().publish(build, listener);
                        if (!published) {
                            out.println("JUnit-style report was not published!");
                        }
                    }
                } catch (Exception e) {
                    throw new AbortException("Could not start SoapUI Pro functional testing.");
                }
            }
        }

        return true;
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
