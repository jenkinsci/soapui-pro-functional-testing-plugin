package com.smartbear.ready.jenkins.tools;

import hudson.FilePath;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * tool class to validate project file structure.
 */
public class ProjectFileValidator {
    private static final String LAST_ELEMENT_TO_READ = "con:soapui-project";
    private static final String ATTRIBUTE_TO_CHECK = "updated";

    private static boolean isCompositeProject(FilePath projectFile) throws Exception {
        if (projectFile.isDirectory()) {
            return true;
        }
        return false;
    }

    private static String getNodeAttribute(Node node, String attributeName, String defaultValue) {
        if (node == null || attributeName == null) {
            return defaultValue;
        }
        NamedNodeMap attributesMap = node.getAttributes();
        Node attributeNode = attributesMap.getNamedItem(attributeName);
        if (attributeNode == null) {
            return defaultValue;
        }
        String value = attributeNode.getNodeValue();
        return value;
    }

    private static boolean isUsualProject(FilePath projectFile) throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(projectFile.read());

        NodeList nodeList = doc.getElementsByTagName(LAST_ELEMENT_TO_READ);
        if (nodeList == null) {
            return false;
        }
        if (nodeList.getLength() == 0) {
            return false;
        }
        Node rootProjectNode = nodeList.item(0);
        String attributeValue = getNodeAttribute(rootProjectNode, ATTRIBUTE_TO_CHECK, null);
        if (attributeValue == null) {
            return false;
        }
        return !attributeValue.isEmpty();
    }

    public static boolean isValidProjectPath(FilePath projectFile) throws Exception {
        if (isCompositeProject(projectFile)) {
            return true;
        }
        return isUsualProject(projectFile);
    }
}
