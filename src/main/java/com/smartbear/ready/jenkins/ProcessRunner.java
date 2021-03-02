package com.smartbear.ready.jenkins;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.annotation.Nonnull;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

class ProcessRunner {
    public static final String READYAPI_REPORT_DIRECTORY = "ReadyAPI_report";
    public static final String REPORT_FORMAT = "PDF";
    private static final String TESTRUNNER_NAME = "testrunner";
    private static final String LAST_ELEMENT_TO_READ = "con:soapui-project";
    private static final String ATTRIBUTE_TO_CHECK = "updated";
    private static final String TERMINATION_STRING = "Please enter absolute path of the license file";
    private static final String SH = ".sh";
    private static final String BAT = ".bat";
    private static final String REPORT_CREATED_DETERMINANT = "Created report at";
    private static final String SOAPUI_PRO_TESTRUNNER_DETERMINANT = "com.smartbear.ready.cmd.runner.pro.SoapUIProTestCaseRunner";
    private static final String DEFAULT_PLUGIN_VERSION = "1.0";
    private static final String SOAPUI_PRO_FUNCTIONAL_TESTING_PLUGIN_INFO = "/soapUiProFunctionalTestingPluginInfo.properties";
    private static final String TESTRUNNER_VERSION_DETERMINANT = "ready-api-ui-";
    private static final String PROJECT_REPORT = "Project Report";
    private static final String TESTSUITE_REPORT = "TestSuite Report";
    private static final String TESTCASE_REPORT = "TestCase Report";
    private static final String PLUGIN_NAME_FOR_ANALYTICS = "Jenkins";
    private static final char FOLDER_NAME_SEPARATOR = '-';
    private static final int TESTRUNNER_VERSION_FOR_ANALYTICS_FIRST_NUMBER = 2;
    private static final int TESTRUNNER_VERSION_FOR_ANALYTICS_SECOND_NUMBER = 4;
    private String slaveFileSeparator;
    private String PRINTABLE_REPORT_CREATED_DETERMINATION = "Created report [%s]";
    private boolean isReportCreated;
    private boolean isPrintableReportCreated;
    private boolean isSoapUIProProject = false;
    private VirtualChannel channel;
    private String printableReportPath;
    private String printableReportName;
    private String reportsFolderPath;

    Proc run(final ParameterContainer params, @Nonnull final Run<?, ?> run, @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws IOException, InterruptedException {
        final PrintStream out = listener.getLogger();
        List<String> processParameterList = new ArrayList<>();
        channel = launcher.getChannel();
        setSlaveFileSeparator(launcher);
        EnvVars envVars = run.getEnvironment(listener);
        String testrunnerFilePath = buildTestRunnerPath(envVars.expand(params.getPathToTestrunner()), launcher);
        FilePath testrunnerFile = new FilePath(channel, testrunnerFilePath);
        if (StringUtils.isNotBlank(testrunnerFilePath) && testrunnerFile.exists() && testrunnerFile.length() != 0) {
            try {
                if (!isSoapUIProTestrunner(testrunnerFile)) {
                    out.println("The testrunner file is not correct. Please confirm it's the testrunner for ReadyAPI Test. Exiting.");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace(out);
                return null;
            }
            processParameterList.add(testrunnerFilePath);
        } else {
            out.println("Failed to load testrunner file [" + testrunnerFilePath + "]");
            return null;
        }

        reportsFolderPath = params.getWorkspace() + slaveFileSeparator + READYAPI_REPORT_DIRECTORY + slaveFileSeparator;
        setReportDirectory(reportsFolderPath);
        processParameterList.addAll(Arrays.asList("-f", reportsFolderPath));

        processParameterList.add("-r");
        processParameterList.add("-j");
        processParameterList.add("-J");
        processParameterList.addAll(Arrays.asList("-F", REPORT_FORMAT));
        boolean isPrintableReportTypeSet = false;
        String testSuite = params.getTestSuite();
        String testCase = params.getTestCase();
        if (StringUtils.isNotBlank(testCase)) {
            if (StringUtils.isNotBlank(testSuite)) {
                processParameterList.addAll(Arrays.asList("-c", testCase));
                processParameterList.addAll(Arrays.asList("-R", TESTCASE_REPORT));
                setPrintableReportParams(createFolderName(testSuite) + slaveFileSeparator +
                        createFolderName(testCase) + slaveFileSeparator, TESTCASE_REPORT);
                isPrintableReportTypeSet = true;
            } else {
                out.println("Enter a testsuite for the specified testcase. Exiting.");
                return null;
            }
        }
        if (StringUtils.isNotBlank(testSuite)) {
            processParameterList.addAll(Arrays.asList("-s", testSuite));
            if (!isPrintableReportTypeSet) {
                processParameterList.addAll(Arrays.asList("-R", TESTSUITE_REPORT));
                setPrintableReportParams(createFolderName(testSuite) + slaveFileSeparator,
                        TESTSUITE_REPORT);
                isPrintableReportTypeSet = true;
            }
        }
        if (StringUtils.isNotBlank(params.getTestSuiteTags())) {
            processParameterList.addAll(Arrays.asList("-T", "TestSuite " + params.getTestSuiteTags()));
        }
        if (StringUtils.isNotBlank(params.getTestCaseTags())) {
            processParameterList.addAll(Arrays.asList("-T", "TestCase " + params.getTestCaseTags()));
        }
        if (StringUtils.isNotBlank(params.getProjectPassword())) {
            processParameterList.addAll(Arrays.asList("-x", params.getProjectPassword()));
        }
        if (StringUtils.isNotBlank(params.getEnvironment())) {
            processParameterList.addAll(Arrays.asList("-E", params.getEnvironment()));
        }

        String projectFilePath = envVars.expand(params.getPathToProjectFile());
        FilePath projectFile = new FilePath(channel, projectFilePath);
        if (StringUtils.isNotBlank(projectFilePath) && projectFile.exists() && (projectFile.isDirectory() || projectFile.length() != 0)) {
            try {
                checkIfSoapUIProProject(projectFile);
            } catch (Exception e) {
                e.printStackTrace(out);
                return null;
            }
            if (!isSoapUIProProject) {
                out.println("The project is not a ReadyAPI project! Exiting.");
                return null;
            }
            processParameterList.add(projectFilePath);
        } else {
            out.println("Failed to load the project file [" + projectFilePath + "]");
            return null;
        }

        if (!isPrintableReportTypeSet) {
            processParameterList.addAll(Arrays.asList("-R", PROJECT_REPORT));
            setPrintableReportParams("", PROJECT_REPORT);
            isPrintableReportTypeSet = true;
        }

        if (shouldSendAnalytics(testrunnerFile)) {
            Properties properties = new Properties();
            properties.load(ProcessRunner.class.getResourceAsStream(SOAPUI_PRO_FUNCTIONAL_TESTING_PLUGIN_INFO));
            String version = properties.getProperty("version", DEFAULT_PLUGIN_VERSION);
            processParameterList.addAll(Arrays.asList("-q", PLUGIN_NAME_FOR_ANALYTICS + "-" + version));
        }

        isReportCreated = false;
        isPrintableReportCreated = false;
        Launcher.ProcStarter processStarter = launcher.launch().cmds(processParameterList).envs(run.getEnvironment(listener)).readStdout().quiet(true);
        out.println("Starting ReadyAPI functional test.");

        final Proc process = processStarter.start();
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getStdout()));
        new Thread(new Runnable() {
            public void run() {
                String s;
                try {
                    while ((s = bufferedReader.readLine()) != null) {
                        out.println(s);
                        if (s.contains(TERMINATION_STRING)) {
                            out.println("No license was found! Exiting.");
                            run.setResult(Result.FAILURE);
                            process.kill();
                            return;
                        }
                        if (s.contains(REPORT_CREATED_DETERMINANT)) {
                            isReportCreated = true;
                        }
                        if (s.contains(PRINTABLE_REPORT_CREATED_DETERMINATION)) {
                            isPrintableReportCreated = true;
                        }
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace(out);
                }
            }
        }).start();

        return process;
    }

    public String getReportsFolderPath() {
        return reportsFolderPath;
    }

    private void setSlaveFileSeparator(Launcher launcher) {
        slaveFileSeparator = launcher.isUnix() ? "/" : "\\";
    }

    private String buildTestRunnerPath(String pathToTestrunnerFile, Launcher launcher) throws IOException, InterruptedException {
        if (!StringUtils.isNotBlank(pathToTestrunnerFile)) {
            return "";
        }
        if (!new FilePath(channel, pathToTestrunnerFile).isDirectory()) {
            return pathToTestrunnerFile;
        }
        if (launcher.isUnix()) {
            return pathToTestrunnerFile + slaveFileSeparator + TESTRUNNER_NAME + SH;
        } else {
            return pathToTestrunnerFile + slaveFileSeparator + TESTRUNNER_NAME + BAT;
        }
    }

    private boolean isSoapUIProTestrunner(FilePath testrunnerFile) throws IOException, InterruptedException {
        return testrunnerFile.readToString().contains(SOAPUI_PRO_TESTRUNNER_DETERMINANT);
    }

    private void setReportDirectory(String reportDirectoryPath) throws IOException, InterruptedException {
        FilePath reportDirectoryFile = new FilePath(channel, reportDirectoryPath);
        if (!reportDirectoryFile.exists()) {
            reportDirectoryFile.mkdirs();
        }
    }

    private void checkIfSoapUIProProject(FilePath projectFile) throws Exception {
        //if project is composite, it is ReadyAPI project also
        if (projectFile.isDirectory()) {
            isSoapUIProProject = true;
            return;
        }
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        try {
            saxParser.parse(projectFile.read(), new ReadXmlUpToSpecificElementSaxParser(LAST_ELEMENT_TO_READ));
        } catch (MySAXTerminatorException exp) {
            //nothing to do, expected
        }
    }

    private boolean shouldSendAnalytics(FilePath testrunnerFile) throws IOException, InterruptedException {
        String testrunnerFileToString = testrunnerFile.readToString();
        if (testrunnerFileToString.contains(TESTRUNNER_VERSION_DETERMINANT)) {
            int startFromIndex = testrunnerFileToString.indexOf(TESTRUNNER_VERSION_DETERMINANT) + TESTRUNNER_VERSION_DETERMINANT.length();
            int firstVersionNumber = Character.getNumericValue(testrunnerFileToString.charAt(startFromIndex));
            if (firstVersionNumber >= TESTRUNNER_VERSION_FOR_ANALYTICS_FIRST_NUMBER) {
                int secondVersionIndex = Character.getNumericValue(testrunnerFileToString.charAt(startFromIndex + 2));
                if (secondVersionIndex >= TESTRUNNER_VERSION_FOR_ANALYTICS_SECOND_NUMBER) {
                    return true;
                }
            }
        }
        return false;
    }

    private void setPrintableReportParams(String printableReportPath, String printableReportType) {
        this.printableReportPath = printableReportPath;
        this.printableReportName = printableReportType + "." + REPORT_FORMAT.toLowerCase();
        PRINTABLE_REPORT_CREATED_DETERMINATION = String.format(PRINTABLE_REPORT_CREATED_DETERMINATION, printableReportType);
    }

    private static String createFolderName(String str) {
        StringBuilder result = new StringBuilder();

        for (int c = 0; c < str.length(); c++) {
            char ch = str.charAt(c);

            if (Character.isWhitespace(ch)) {
                result.append(FOLDER_NAME_SEPARATOR);
            } else if (Character.isLetterOrDigit(ch)) {
                result.append(ch);
            } else if (ch == FOLDER_NAME_SEPARATOR) {
                result.append(ch);
            }
        }

        return result.toString();
    }

    protected boolean isReportCreated() {
        return isReportCreated;
    }

    protected boolean isPrintableReportCreated() {
        return isPrintableReportCreated;
    }

    protected String getPrintableReportName() {
        return this.printableReportName;
    }

    protected String getPrintableReportPath() {
        return this.printableReportPath;
    }

    private class ReadXmlUpToSpecificElementSaxParser extends DefaultHandler {
        private final String lastElementToRead;

        ReadXmlUpToSpecificElementSaxParser(String lastElementToRead) {
            this.lastElementToRead = lastElementToRead;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws MySAXTerminatorException {
            if (lastElementToRead.equals(qName)) {
                String value = attributes.getValue(ATTRIBUTE_TO_CHECK);
                if (value != null) {
                    isSoapUIProProject = !value.isEmpty();
                }
                throw new MySAXTerminatorException();
            }
        }
    }

    private class MySAXTerminatorException extends SAXException {
    }

}
