package com.smartbear.ready.jenkins;

import hudson.model.Action;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class DynamicReportAction implements Action {

    private final static String DOWNLOAD_FILE_NAME = "SoapUI Pro Test Results";

    private final File printableReportFile;

    DynamicReportAction(File printableReportFile) {

        this.printableReportFile = printableReportFile;
    }

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return null;
    }

    public void doDynamic(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {

        if (!req.getMethod().equals("GET")) {
            rsp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        String path = req.getRestOfPath();

        if (path.length() == 0 || path.contains("..") || path.length() < 1) {
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        path = path.substring(1);
        String[] parts = path.split("/");
        if (parts.length == 0) {
            rsp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (!printableReportFile.exists() || !printableReportFile.isFile() || !printableReportFile.canRead()) {
            rsp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        FileInputStream fis = null;

        try {
            fis = new FileInputStream(printableReportFile);
            rsp.setHeader("Content-Disposition", "filename=\"" + DOWNLOAD_FILE_NAME + "." + ProcessRunner.REPORT_FORMAT.toLowerCase() + "\"");
            rsp.serveFile(req, fis, printableReportFile.lastModified(), 0, printableReportFile.length(), "mime-type:application/pdf");
        } catch (IOException e) {
            rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }


}
