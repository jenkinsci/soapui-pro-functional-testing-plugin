package com.smartbear.ready.jenkins;

import hudson.FilePath;

public class ParameterContainer {
    private String pathToTestrunner;
    private String pathToProjectFile;
    private String testSuite;
    private String testCase;
    private String testSuiteTags;
    private String testCaseTags;
    private String projectPassword;
    private String environment;
    private String authMethod;
    private String slmLicenceApiHost;
    private String slmLicenceApiPort;
    private String slmLicenceAccessKey;
    private FilePath workspace;
    private String slmLicenseClientId;
    private String slmLicenseClientSecret;


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

    public String getTestSuiteTags() {
        return testSuiteTags;
    }

    public String getTestCaseTags() {
        return testCaseTags;
    }

    public String getProjectPassword() {
        return projectPassword;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getSlmLicenceAccessKey() {
        return slmLicenceAccessKey;
    }

    public FilePath getWorkspace() {
        return workspace;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public String getSlmLicenceApiHost() {
        return slmLicenceApiHost;
    }

    public String getSlmLicenceApiPort() {
        return slmLicenceApiPort;
    }

    public String getSlmLicenseClientId() {
        return this.slmLicenseClientId;
    }

    public String getSlmLicenseClientSecret() {
        return this.slmLicenseClientSecret;
    }

    public static class Builder {
        ParameterContainer parameterContainer = new ParameterContainer();

        public ParameterContainer build() {
            return parameterContainer;
        }

        public Builder withPathToTestrunner(String pathToTestrunner) {
            parameterContainer.pathToTestrunner = pathToTestrunner;
            return this;
        }

        public Builder withPathToProjectFile(String pathToProjectFile) {
            parameterContainer.pathToProjectFile = pathToProjectFile;
            return this;
        }

        public Builder withTestSuite(String testSuite) {
            parameterContainer.testSuite = testSuite;
            return this;
        }

        public Builder withTestCase(String testCase) {
            parameterContainer.testCase = testCase;
            return this;
        }

        public Builder withTestSuiteTags(String tags) {
            parameterContainer.testSuiteTags = tags;
            return this;
        }

        public Builder withTestCaseTags(String tags) {
            parameterContainer.testCaseTags = tags;
            return this;
        }

        public Builder withProjectPassword(String projectPassword) {
            parameterContainer.projectPassword = projectPassword;
            return this;
        }

        public Builder withEnvironment(String environment) {
            parameterContainer.environment = environment;
            return this;
        }

        public Builder withWorkspace(FilePath workspace) {
            parameterContainer.workspace = workspace;
            return this;
        }

        public Builder withAuthMethod(String authMethod) {
            parameterContainer.authMethod = authMethod;
            return this;
        }

        public Builder withSlmLicenceApiHost(String slmLicenceApiHost) {
            parameterContainer.slmLicenceApiHost = slmLicenceApiHost;
            return this;
        }

        public Builder withSlmLicenceApiPort(String slmLicenceApiPort) {
            parameterContainer.slmLicenceApiPort = slmLicenceApiPort;
            return this;
        }

        public Builder withSlmLicenceAccessKey(String slmLicenceAccessKey) {
            parameterContainer.slmLicenceAccessKey = slmLicenceAccessKey;
            return this;
        }

        public Builder withSlmLicenseClientId(String slmLicenseClientId) {
            parameterContainer.slmLicenseClientId = slmLicenseClientId;
            return this;
        }

        public Builder withSlmLicenseClientSecret(String slmLicenseClientSecret) {
            parameterContainer.slmLicenseClientSecret = slmLicenseClientSecret;
            return this;
        }
    }
}
