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
    private FilePath workspace;

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

    public FilePath getWorkspace() {
        return workspace;
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

    }
}
