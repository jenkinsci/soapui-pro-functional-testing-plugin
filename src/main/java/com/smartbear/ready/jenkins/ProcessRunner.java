package com.smartbear.ready.jenkins;

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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

class ProcessRunner {
    public static final String READYAPI_REPORT_DIRECTORY = File.separator + "ReadyAPI_report";
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
    private boolean isReportCreated;
    private boolean isSoapUIProProject = false;
    private VirtualChannel channel;

    Proc run(final ParameterContainer params, @Nonnull final Run<?, ?> run, @Nonnull Launcher launcher, @Nonnull TaskListener listener)
            throws IOException, InterruptedException {
        final PrintStream out = listener.getLogger();
        List<String> processParameterList = new ArrayList<>();
        channel = launcher.getChannel();
        String testrunnerFilePath = buildTestRunnerPath(params.getPathToTestrunner());
        if (StringUtils.isNotBlank(testrunnerFilePath) && new FilePath(channel, testrunnerFilePath).exists()) {
            try {
                if (!isSoapUIProTestrunner(testrunnerFilePath)) {
                    out.println("The testrunner file is not correct. Please confirm it's the testrunner for SoapUI Pro. Exiting.");
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

        String reportDirectoryPath = params.getWorkspace() + READYAPI_REPORT_DIRECTORY;
        setReportDirectory(reportDirectoryPath);
        processParameterList.addAll(Arrays.asList("-f", reportDirectoryPath));

        processParameterList.add("-r");
        processParameterList.add("-j");
        processParameterList.add("-J");

        if (StringUtils.isNotBlank(params.getTestSuite())) {
            processParameterList.addAll(Arrays.asList("-s", params.getTestSuite()));
        }
        if (StringUtils.isNotBlank(params.getTestCase())) {
            processParameterList.addAll(Arrays.asList("-c", params.getTestCase()));
        }
        if (StringUtils.isNotBlank(params.getProjectPassword())) {
            processParameterList.addAll(Arrays.asList("-x", params.getProjectPassword()));
        }
        if (StringUtils.isNotBlank(params.getEnvironment())) {
            processParameterList.addAll(Arrays.asList("-E", params.getEnvironment()));
        }

        String projectFilePath = params.getPathToProjectFile();
        if (StringUtils.isNotBlank(projectFilePath) && new FilePath(channel, projectFilePath).exists()) {
            try {
                checkIfSoapUIProProject(projectFilePath);
            } catch (Exception e) {
                e.printStackTrace(out);
                return null;
            }
            if (!isSoapUIProProject) {
                out.println("The project is not a SoapUI Pro project! Exiting.");
                return null;
            }
            processParameterList.add(projectFilePath);
        } else {
            out.println("Failed to load the project file [" + projectFilePath + "]");
            return null;
        }
        Properties properties = new Properties();
        properties.load(ProcessRunner.class.getResourceAsStream(SOAPUI_PRO_FUNCTIONAL_TESTING_PLUGIN_INFO));
        String version = properties.getProperty("version", DEFAULT_PLUGIN_VERSION);
        //for statistics
        processParameterList.addAll(Arrays.asList("-q", version));

        isReportCreated = false;
        Launcher.ProcStarter processStarter = launcher.launch().cmds(processParameterList).envs(run.getEnvironment(listener)).readStdout().quiet(true);
        out.println("Starting SoapUI Pro functional test.");
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
                    }
                } catch (IOException e) {
                    e.printStackTrace(out);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return process;
    }

    private String buildTestRunnerPath(String pathToTestrunnerFile) throws IOException, InterruptedException {
        if (!StringUtils.isNotBlank(pathToTestrunnerFile)) {
            return "";
        }
        if (!new FilePath(channel, pathToTestrunnerFile).isDirectory()) {
            return pathToTestrunnerFile;
        }
        if (System.getProperty("os.name").contains("Windows")) {
            return pathToTestrunnerFile + File.separator + TESTRUNNER_NAME + BAT;
        } else {
            return pathToTestrunnerFile + File.separator + TESTRUNNER_NAME + SH;
        }
    }

    private boolean isSoapUIProTestrunner(String testrunnerFilePath) throws IOException, InterruptedException {
        return new FilePath(channel, testrunnerFilePath).readToString().contains(SOAPUI_PRO_TESTRUNNER_DETERMINANT);
    }

    private void setReportDirectory(String reportDirectoryPath) throws IOException, InterruptedException {
        FilePath reportDirectoryFile = new FilePath(channel, reportDirectoryPath);
        if (!reportDirectoryFile.exists()) {
            reportDirectoryFile.mkdirs();
        }
    }

    private void checkIfSoapUIProProject(String projectFilePath) throws ParserConfigurationException, SAXException, IOException, InterruptedException {
        //if project is composite, it is SoapUI Pro project also
        if (new FilePath(channel, projectFilePath).isDirectory()) {
            isSoapUIProProject = true;
            return;
        }
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        try {
            saxParser.parse(projectFilePath, new ReadXmlUpToSpecificElementSaxParser(LAST_ELEMENT_TO_READ));
        } catch (MySAXTerminatorException exp) {
            //nothing to do, expected
        }
    }

    protected boolean isReportCreated() {
        return isReportCreated;
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
