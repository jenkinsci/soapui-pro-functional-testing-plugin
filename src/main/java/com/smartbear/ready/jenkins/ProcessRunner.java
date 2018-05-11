package com.smartbear.ready.jenkins;

import hudson.model.AbstractBuild;
import hudson.model.Result;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.servlet.ServletException;
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

class ProcessRunner {
    public final static String READYAPI_REPORT_DIRECTORY = "\\ReadyAPI_report";
    private static final String TESTRUNNER_NAME = "testrunner";
    private static final String COMPOSITE_PROJECT_SETTINGS_FILE_PATH = "\\settings.xml";
    private static final String LAST_ELEMENT_TO_READ = "con:soapui-project";
    private static final String ATTRIBUTE_TO_CHECK = "updated";
    private static final String TERMINATION_STRING = "Please enter absolute path of the license file";
    private static final String SH = ".sh";
    private static final String BAT = ".bat";
    private boolean isSoapUIProProject = false;

    Process run(final PrintStream out, final ParameterContainer params, final AbstractBuild build)
            throws IOException {
        List<String> processParameterList = new ArrayList<>();
        String testrunnerFilePath = buildTestRunnerPath(params.getPathToTestrunner());
        if (StringUtils.isNotBlank(testrunnerFilePath) && new File(testrunnerFilePath).exists()) {
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
        if (StringUtils.isNotBlank(projectFilePath) && new File(projectFilePath).exists()) {
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

        ProcessBuilder pb = new ProcessBuilder(processParameterList);
        pb.redirectErrorStream(true);
        out.println("Starting SoapUI Pro functional test.");
        final Process process = pb.start();

        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        new Thread(new Runnable() {
            public void run() {
                String s;
                try {
                    while ((s = bufferedReader.readLine()) != null) {
                        out.println(s);
                        if (s.contains(TERMINATION_STRING)) {
                            out.println("No license was found! Exiting.");
                            build.doStop();
                            build.setResult(Result.FAILURE);
                            process.destroy();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace(out);
                } catch (ServletException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        return process;
    }

    private String buildTestRunnerPath(String pathToTestrunnerFile) {
        if (!StringUtils.isNotBlank(pathToTestrunnerFile)) {
            return "";
        }
        if (new File(pathToTestrunnerFile).isFile()) {
            return pathToTestrunnerFile;
        }
        if (System.getProperty("os.name").contains("Windows")) {
            return pathToTestrunnerFile + File.separator + TESTRUNNER_NAME + BAT;
        } else {
            return pathToTestrunnerFile + File.separator + TESTRUNNER_NAME + SH;
        }
    }

    private void setReportDirectory(String reportDirectoryPath) {
        File reportDirectoryFile = new File(reportDirectoryPath);
        if (!reportDirectoryFile.exists()) {
            reportDirectoryFile.mkdir();
        }
    }

    private void checkIfSoapUIProProject(String projectFilePath) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        //if composite project, check settings.xml file
        if (new File(projectFilePath).isDirectory()) {
            projectFilePath = projectFilePath + COMPOSITE_PROJECT_SETTINGS_FILE_PATH;
            if (!new File(projectFilePath).exists()) {
                throw new IOException("Missing settings.xml file in the composite project! Can not check if the project " +
                        "is a SoapUI Pro project. Exiting.");
            }
        }
        try {
            saxParser.parse(projectFilePath, new ReadXmlUpToSpecificElementSaxParser(LAST_ELEMENT_TO_READ));
        } catch (MySAXTerminatorException exp) {
            //nothing to do, expected
        }
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
