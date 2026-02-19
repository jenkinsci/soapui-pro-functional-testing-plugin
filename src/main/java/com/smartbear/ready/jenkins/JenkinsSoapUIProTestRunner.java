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
import hudson.util.ListBoxModel;
import hudson.util.Secret;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

@SuppressWarnings("unused")
public class JenkinsSoapUIProTestRunner extends Builder implements SimpleBuildStep {

    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;
    private static final String SLM_LICENCE_ACCESS_KEY = "Access Key";
    private static final String API_HOST = "API Host";
    private static final String API_PORT = "API Port";
    private static final String API_KEY = "API KEY";
    private static final String CLIENT_ID = "Client Id";
    private static final String CLIENT_SECRET = "Client Secret";
    private static final String CLIENT_CREDENTIALS = "Client Credentials";
    private static final String ERROR_INFO = "Please, enter valid SLM %s for %s authentication method";
    private static final String NOT_EMPTY_VALUE_ERROR_INFO = "Field '%s' must be empty for %s authentication method";
    private static final String API_KEY_WARNING_MESSAGE = "Required when using license from SLM Onpremise License Server. Please enter valid %s for %s Authentication Method";
    private final String pathToTestrunner;
    private final String pathToProjectFile;

    private final Secret projectPassword;
    private final Secret slmLicenceAccessKey;
    private final Secret slmLicenseClientSecret;

    private String testSuite;
    private String testCase;
    private String testSuiteTags;
    private String testCaseTags;
    private String environment;
    private String authMethod;
    private String slmLicenceApiHost;
    private String slmLicenceApiPort;
    private String slmLicenseClientId;

    @DataBoundConstructor
    public JenkinsSoapUIProTestRunner(String pathToTestrunner,
                                      String pathToProjectFile,
                                      String projectPassword,
                                      String slmLicenceAccessKey,
                                      String slmLicenseClientSecret) {
        this.pathToTestrunner = pathToTestrunner;
        this.pathToProjectFile = pathToProjectFile;
        this.projectPassword = buildSecret(projectPassword);
        this.slmLicenceAccessKey = buildSecret(slmLicenceAccessKey);
        this.slmLicenseClientSecret = buildSecret(slmLicenseClientSecret);
    }

    private Secret buildSecret(String value) {
        return StringUtils.isNotEmpty(value) && !"********".equals(value) ? Secret.fromString(value) : null;
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

    public Secret getProjectPassword() {
        return projectPassword;
    }

    public String getEnvironment() {
        return environment;
    }

    @DataBoundSetter
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Secret getSlmLicenceAccessKey() {
        return slmLicenceAccessKey;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    @DataBoundSetter
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getSlmLicenceApiHost() {
        return slmLicenceApiHost;
    }

    @DataBoundSetter
    public void setSlmLicenceApiHost(String slmLicenceApiHost) {
        this.slmLicenceApiHost = slmLicenceApiHost;
    }

    public String getSlmLicenceApiPort() {
        return slmLicenceApiPort;
    }

    @DataBoundSetter
    public void setSlmLicenceApiPort(String slmLicenceApiPort) {
        this.slmLicenceApiPort = slmLicenceApiPort;
    }

    public String getSlmLicenseClientId() {
        return this.slmLicenseClientId;
    }

    @DataBoundSetter
    public void setSlmLicenseClientId(String slmLicenseClientId) {
        this.slmLicenseClientId = slmLicenseClientId;
    }

    public Secret getSlmLicenseClientSecret() {
        return this.slmLicenseClientSecret;
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
                    .withProjectPassword(Secret.toString(getProjectPassword()))
                    .withEnvironment(environment)
                    .withAuthMethod(authMethod)
                    .withSlmLicenceApiHost(slmLicenceApiHost)
                    .withSlmLicenceApiPort(slmLicenceApiPort)
                    .withSlmLicenceAccessKey(Secret.toString(slmLicenceAccessKey))
                    .withWorkspace(workspace)
                    .withSlmLicenseClientId(slmLicenseClientId)
                    .withSlmLicenseClientSecret(Secret.toString(slmLicenseClientSecret))
                    .build(), run, launcher, listener);
            if (process == null) {
                throw new AbortException("Could not start ReadyAPI functional testing.");
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
            throw new AbortException("Could not start ReadyAPI functional testing.");

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

        public ListBoxModel doFillAuthMethodItems() {
            ListBoxModel items = new ListBoxModel();

            items.add(AuthMethod.API_KEY.getDisplayName());
            items.add(AuthMethod.ACCESS_FOR_EVERYONE.getDisplayName());
            items.add(AuthMethod.CLIENT_CREDENTIALS.getDisplayName());

            return items;
        }

        public FormValidation doCheckPathToTestrunner(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Please, set path to testrunner");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckPathToProjectFile(@QueryParameter String value) {
            if (value.length() == 0) {
                return FormValidation.error("Please, set path to the ReadyAPI project");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckTestSuite(@QueryParameter String value, @QueryParameter String testCase) {
            if (value.length() == 0 && testCase.length() != 0) {
                return FormValidation.error("Please, enter a test suite for the specified test case");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckAuthMethod(@QueryParameter String value, @QueryParameter String slmLicenceApiHost) {
            return FormValidation.ok();
        }

        public FormValidation doCheckSlmLicenceApiHost(@QueryParameter String value, @QueryParameter String authMethod) {
            final AuthMethod slmAuthMethod = AuthMethod.getValue(authMethod);
            if (StringUtils.isEmpty(value)) {
                switch (slmAuthMethod) {
                    case ACCESS_FOR_EVERYONE:
                        return FormValidation.error(String.format(ERROR_INFO, API_HOST, AuthMethod.ACCESS_FOR_EVERYONE.getDisplayName()));
                    case CLIENT_CREDENTIALS:
                        return FormValidation.error(String.format(ERROR_INFO, API_HOST, AuthMethod.CLIENT_CREDENTIALS.getDisplayName()));
                    case API_KEY:
                        return FormValidation.warning(String.format(API_KEY_WARNING_MESSAGE, API_HOST, AuthMethod.API_KEY.getDisplayName()));
                }
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckSlmLicenceApiPort(@QueryParameter String value, @QueryParameter String authMethod) {
            final AuthMethod slmAuthMethod = AuthMethod.getValue(authMethod);
            if (!isValidPort(value)) {
                switch (slmAuthMethod) {
                    case ACCESS_FOR_EVERYONE:
                        return FormValidation.error(String.format(ERROR_INFO, API_PORT, AuthMethod.ACCESS_FOR_EVERYONE.getDisplayName()));
                    case CLIENT_CREDENTIALS:
                        return FormValidation.error(String.format(ERROR_INFO, API_PORT, AuthMethod.CLIENT_CREDENTIALS.getDisplayName()));
                    case API_KEY:
                        return FormValidation.warning(String.format(API_KEY_WARNING_MESSAGE, API_PORT, AuthMethod.API_KEY.getDisplayName()));
                }
            }
            return FormValidation.ok();
        }

        private boolean isValidPort(String port) {
            return StringUtils.isNotEmpty(port) && hasValidPortContent(port);
        }

        private boolean hasValidPortContent(String port) {
            try {
                final int portNumber = Integer.parseInt(port);
                return portNumber > MIN_PORT && portNumber <= MAX_PORT;
            } catch (NumberFormatException e) {
                return false;
            }
        }

        public FormValidation doCheckSlmLicenceAccessKey(@QueryParameter String value, @QueryParameter String authMethod) {
            final AuthMethod slmAuthMethod = AuthMethod.getValue(authMethod);
            if (StringUtils.isNotEmpty(value)) {
                switch (slmAuthMethod) {
                    case ACCESS_FOR_EVERYONE:
                        return FormValidation.error(String.format(NOT_EMPTY_VALUE_ERROR_INFO, API_KEY, slmAuthMethod.getDisplayName()));
                    case CLIENT_CREDENTIALS:
                        return FormValidation.error(String.format(NOT_EMPTY_VALUE_ERROR_INFO, API_KEY, slmAuthMethod.getDisplayName()));
                }
            }
            return validateEmptyValue(value, AuthMethod.API_KEY, authMethod,
                CLIENT_ID);
        }

        public FormValidation doCheckSlmLicenseClientId(@QueryParameter String value, @QueryParameter String authMethod) {
            final AuthMethod slmAuthMethod = AuthMethod.getValue(authMethod);
            if (StringUtils.isNotEmpty(value)) {
                switch (slmAuthMethod) {
                    case API_KEY:
                        return FormValidation.error(String.format(NOT_EMPTY_VALUE_ERROR_INFO, CLIENT_ID, slmAuthMethod.getDisplayName()));
                    case ACCESS_FOR_EVERYONE:
                        return FormValidation.error(String.format(NOT_EMPTY_VALUE_ERROR_INFO, CLIENT_ID, slmAuthMethod.getDisplayName()));
                }
            }
            return validateEmptyValue(value, AuthMethod.CLIENT_CREDENTIALS, authMethod,
                    CLIENT_ID);
        }

        public FormValidation doCheckSlmLicenseClientSecret(@QueryParameter String value, @QueryParameter String authMethod) {
            final AuthMethod slmAuthMethod = AuthMethod.getValue(authMethod);
            if (StringUtils.isNotEmpty(value)) {
                switch (slmAuthMethod) {
                    case API_KEY:
                        return FormValidation.error(String.format(NOT_EMPTY_VALUE_ERROR_INFO, CLIENT_SECRET, slmAuthMethod.getDisplayName()));
                    case ACCESS_FOR_EVERYONE:
                        return FormValidation.error(String.format(NOT_EMPTY_VALUE_ERROR_INFO, CLIENT_SECRET, slmAuthMethod.getDisplayName()));
                }
            }
            return validateEmptyValue(value, AuthMethod.CLIENT_CREDENTIALS, authMethod,
                    CLIENT_SECRET);
        }

        private FormValidation validateEmptyValue(final String value, final AuthMethod authMethod,
                                                  final String selectedAuthMethod, final String fieldName) {
            final AuthMethod slmAuthMethod = AuthMethod.getValue(selectedAuthMethod);
            if (StringUtils.isEmpty(value) && authMethod.equals(slmAuthMethod)) {
                return FormValidation.error(String.format(ERROR_INFO, fieldName, slmAuthMethod.getDisplayName()));
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "ReadyAPI Test: Run Functional Test";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }
    }

}
