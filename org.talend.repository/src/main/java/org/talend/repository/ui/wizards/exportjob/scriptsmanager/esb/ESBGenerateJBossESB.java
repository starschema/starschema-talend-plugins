// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.exportjob.scriptsmanager.esb;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ESBGenerateJBossESB {

    private String path;

    public ESBGenerateJBossESB(String path) {
        this.path = path;
    }

    public void saveProjectSettings(HashMap<String, String> nodeMap) {
        if (path == null) {
            return;
        }

        // support actually only export job one by one.
        String jobName = nodeMap.keySet().iterator().next();
        String jobWithPackageName = nodeMap.get(jobName);
        String jobAlias = jobWithPackageName.replace(".", "_"); //$NON-NLS-1$ //$NON-NLS-2$

        File xmlFile = new File(path);

        try {
            final DocumentBuilderFactory fabrique = DocumentBuilderFactory.newInstance();

            DocumentBuilder analyseur = fabrique.newDocumentBuilder();
            analyseur.setErrorHandler(new ErrorHandler() {

                public void error(final SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void fatalError(final SAXParseException exception) throws SAXException {
                    throw exception;
                }

                public void warning(final SAXParseException exception) throws SAXException {
                    throw exception;
                }

            });

            Document document = analyseur.newDocument();
            Element root = document.createElement("jbossesb"); //$NON-NLS-1$
            document.appendChild(root);
            Attr xmlns = document.createAttribute("xmlns");//$NON-NLS-1$
            xmlns.setNodeValue("http://anonsvn.labs.jboss.com/labs/jbossesb/trunk/product/etc/schemas/xml/jbossesb-1.0.1.xsd");//$NON-NLS-1$
            Attr parameterReloadSecs = document.createAttribute("parameterReloadSecs");//$NON-NLS-1$
            parameterReloadSecs.setNodeValue("5");//$NON-NLS-1$
            root.setAttributeNode(xmlns);
            root.setAttributeNode(parameterReloadSecs);

            Element providers = document.createElement("providers");//$NON-NLS-1$
            root.appendChild(providers);

            Element jmsprovider = document.createElement("jms-provider");//$NON-NLS-1$
            providers.appendChild(jmsprovider);
            Attr name = document.createAttribute("name");//$NON-NLS-1$
            name.setNodeValue("JBossMQ");//$NON-NLS-1$
            Attr factory = document.createAttribute("connection-factory");//$NON-NLS-1$
            factory.setNodeValue("ConnectionFactory");//$NON-NLS-1$
            jmsprovider.setAttributeNode(name);
            jmsprovider.setAttributeNode(factory);

            Element jmsBusGw = document.createElement("jms-bus");//$NON-NLS-1$
            jmsprovider.appendChild(jmsBusGw);
            Attr busisGW = document.createAttribute("busid");//$NON-NLS-1$
            busisGW.setNodeValue(jobAlias + "GwChannel");//$NON-NLS-1$
            factory.setNodeValue("ConnectionFactory");//$NON-NLS-1$
            jmsprovider.setAttributeNode(busisGW);

            //            Element jmsMessageFilterGw = document.createElement("jms-message-filter");//$NON-NLS-1$
            // jmsprovider.appendChild(jmsBusGw);
            //            Attr busisGW = document.createAttribute("busid");//$NON-NLS-1$
            //            busisGW.setNodeValue(jobAlias + "GwChannel");//$NON-NLS-1$
            //            factory.setNodeValue("ConnectionFactory");//$NON-NLS-1$
            // jmsprovider.setAttributeNode(busisGW);

            Element services = document.createElement("services");//$NON-NLS-1$
            root.appendChild(services);

            Element service = document.createElement("service");//$NON-NLS-1$
            services.appendChild(service);
            Attr category = document.createAttribute("category");//$NON-NLS-1$
            category.setNodeValue("TalendESB");//$NON-NLS-1$
            Attr nameCa = document.createAttribute("name");//$NON-NLS-1$
            nameCa.setNodeValue("SimpleListener");//$NON-NLS-1$
            Attr description = document.createAttribute("description");//$NON-NLS-1$
            description.setNodeValue("Talend ESB Export");//$NON-NLS-1$
            service.setAttributeNode(category);
            service.setAttributeNode(nameCa);
            service.setAttributeNode(description);

            Element listeners = document.createElement("listeners");//$NON-NLS-1$
            service.appendChild(listeners);

            Element listener1 = document.createElement("jms-listener");//$NON-NLS-1$
            listeners.appendChild(listener1);
            Attr nameLis1 = document.createAttribute("name");//$NON-NLS-1$
            nameLis1.setNodeValue("JMS-Gateway");//$NON-NLS-1$
            Attr busidref = document.createAttribute("busidref");//$NON-NLS-1$
            busidref.setNodeValue("quickstartGwChannel");//$NON-NLS-1$
            Attr gateway = document.createAttribute("is-gateway");//$NON-NLS-1$
            gateway.setNodeValue("true");//$NON-NLS-1$
            listener1.setAttributeNode(nameLis1);
            listener1.setAttributeNode(busidref);
            listener1.setAttributeNode(gateway);

            Element listener2 = document.createElement("jms-listener");//$NON-NLS-1$
            listeners.appendChild(listener2);
            Attr nameLis2 = document.createAttribute("name");//$NON-NLS-1$
            nameLis2.setNodeValue("talendESB");//$NON-NLS-1$
            Attr busidref2 = document.createAttribute("busidref");//$NON-NLS-1$
            busidref2.setNodeValue("quickstartEsbChannel");//$NON-NLS-1$
            listener2.setAttributeNode(nameLis2);
            listener2.setAttributeNode(busidref2);

            Element actions = document.createElement("actions");//$NON-NLS-1$
            service.appendChild(actions);
            Attr mep = document.createAttribute("mep");//$NON-NLS-1$
            mep.setNodeValue("OneWay");//$NON-NLS-1$
            actions.setAttributeNode(mep);

            addActionElement(document, actions, nodeMap);

            saveDocumentByEncoding(document, xmlFile);

        } catch (ParserConfigurationException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    private void addActionElement(Document document, Element actions, HashMap<String, String> nodeMap) {
        Iterator iter = nodeMap.keySet().iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            String value = (String) nodeMap.get(key);
            Element action = document.createElement("action");//$NON-NLS-1$
            actions.appendChild(action);

            Attr name = document.createAttribute("name");//$NON-NLS-1$
            name.setNodeValue(key);

            Attr cla = document.createAttribute("class");//$NON-NLS-1$
            cla.setNodeValue(value + "." + key + "Action");//$NON-NLS-1$//$NON-NLS-1$ //$NON-NLS-2$

            Attr process = document.createAttribute("process");//$NON-NLS-1$
            process.setNodeValue(key);
            action.setAttributeNode(name);
            action.setAttributeNode(cla);
            action.setAttributeNode(process);
        }

    }

    private static void saveDocumentByEncoding(Document document, File file) throws IOException {
        if (document == null || file == null) {
            return;
        }
        XMLSerializer serializer = new XMLSerializer();
        OutputFormat outputFormat = new OutputFormat();
        outputFormat.setIndenting(true);
        serializer.setOutputFormat(outputFormat);

        OutputStreamWriter output = new OutputStreamWriter(new FileOutputStream(file), "UTF-8"); //$NON-NLS-1$
        serializer.setOutputCharStream(output);
        serializer.serialize(document);
        output.close();

    }

}
