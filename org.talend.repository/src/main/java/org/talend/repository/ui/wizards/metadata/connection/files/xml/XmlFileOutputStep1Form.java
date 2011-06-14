// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.repository.ui.wizards.metadata.connection.files.xml;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.datatools.enablement.oda.xml.util.ui.ATreeNode;
import org.eclipse.emf.common.util.EList;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.talend.commons.exception.ExceptionHandler;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.ui.swt.formtools.Form;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.commons.ui.swt.formtools.LabelledFileField;
import org.talend.commons.ui.utils.PathUtils;
import org.talend.commons.utils.encoding.CharsetToolkit;
import org.talend.commons.xml.XmlUtil;
import org.talend.core.model.general.Project;
import org.talend.core.model.metadata.EMetadataEncoding;
import org.talend.core.model.metadata.builder.connection.ConnectionFactory;
import org.talend.core.model.metadata.builder.connection.MetadataColumn;
import org.talend.core.model.metadata.builder.connection.MetadataTable;
import org.talend.core.model.metadata.builder.connection.XMLFileNode;
import org.talend.core.model.properties.ConnectionItem;
import org.talend.core.model.utils.TalendTextUtils;
import org.talend.cwm.helper.ConnectionHelper;
import org.talend.cwm.helper.PackageHelper;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.ProjectManager;
import org.talend.repository.i18n.Messages;
import org.talend.repository.model.ResourceModelUtils;
import org.talend.repository.ui.swt.utils.AbstractXmlFileStepForm;
import org.talend.repository.ui.utils.ConnectionContextHelper;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.Element;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.treeNode.FOXTreeNode;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.StringUtil;
import org.talend.repository.ui.wizards.metadata.connection.files.xml.util.TreeUtil;
import orgomg.cwm.resource.record.RecordFactory;
import orgomg.cwm.resource.record.RecordFile;

/**
 * wzhang class global comment. Detailled comment
 */
public class XmlFileOutputStep1Form extends AbstractXmlFileStepForm {

    private static Logger log = Logger.getLogger(XmlFileOutputStep1Form.class);

    private Button noFileButton;

    private Button useFileButton;

    private LabelledFileField xmlXsdFilePath;

    private LabelledCombo encodingCombo;

    private Label labelLimitation;

    private String encoding;

    private Text commonNodesLimitation;

    private LabelledFileField outputFilePath;

    private transient Tree availableXmlTree;

    private Text fileContentText;

    private TreePopulator treePopulator;

    private ATreeNode treeNode;

    private boolean creation;

    private boolean valid = true;

    private static final int WIDTH_GRIDDATA_PIXEL = 300;

    private int order = 1;

    private Map<String, Integer> orderMap = new HashMap<String, Integer>();

    private String tempXmlXsdPath;

    private boolean isModifing = true;

    public XmlFileOutputStep1Form(boolean creation, Composite parent, ConnectionItem connectionItem, String[] existingNames) {
        super(parent, connectionItem, existingNames);
        this.creation = creation;
        setupForm(true);
    }

    @Override
    protected void initialize() {
        getConnection().setInputModel(false);
        this.treePopulator = new TreePopulator(availableXmlTree);
        if (getConnection().getXmlFilePath() != null) {
            xmlXsdFilePath.setText(getConnection().getXmlFilePath().replace("\\\\", "\\"));
            checkFieldsValue();
            String xmlXsdPath = xmlXsdFilePath.getText();
            if (isContextMode()) {
                ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
                xmlXsdPath = TalendTextUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                        xmlXsdFilePath.getText()));
            }
            if (new File(xmlXsdPath).exists()) {
                valid = this.treePopulator.populateTree(xmlXsdPath, treeNode);
            } else if (getConnection().getFileContent() != null && getConnection().getFileContent().length > 0) {
                initFileContent();
            }
        }

        if (getConnection().getEncoding() != null && !getConnection().getEncoding().equals("")) {
            encodingCombo.setText(getConnection().getEncoding());
        } else {
            encodingCombo.select(0);
        }
        encodingCombo.clearSelection();
        if (getConnection().getOutputFilePath() != null) {
            outputFilePath.setText(getConnection().getOutputFilePath());
        }
        adaptFormToEditable();
    }

    @Override
    protected void adaptFormToEditable() {
        noFileButton.setEnabled(!isContextMode());
        useFileButton.setEnabled(!isContextMode());
        encodingCombo.setReadOnly(!isContextMode());
        outputFilePath.setEditable(!isContextMode());
        super.adaptFormToEditable();
    }

    @Override
    protected void addFields() {
        createOutputSettingArea();
        createOutputFile(this, WIDTH_GRIDDATA_PIXEL, 50);

        SashForm sash = new SashForm(this, SWT.HORIZONTAL | SWT.SMOOTH);
        GridData sashData = new GridData(GridData.FILL_BOTH);
        sash.setLayoutData(sashData);
        createFileContentViewer(sash, 400, 100);
        createFileContentText(sash, 400, 100);
    }

    private void createOutputSettingArea() {
        Group group = Form.createGroup(this, 1, "Output Setting", 80);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = 150;
        group.setLayoutData(data);

        Composite compositeButton = Form.startNewDimensionnedGridLayout(group, 1, WIDTH_GRIDDATA_PIXEL, 20);
        noFileButton = new Button(compositeButton, SWT.RADIO);
        noFileButton.setText("Create manually");
        useFileButton = new Button(compositeButton, SWT.RADIO);
        useFileButton.setText("Create from a file");

        Composite compositeOutput = Form.startNewDimensionnedGridLayout(group, 3, WIDTH_GRIDDATA_PIXEL, 20);
        String[] extensions = new String[] { "*.xml;*.xsd", "*.*", "*" };
        xmlXsdFilePath = new LabelledFileField(compositeOutput, "XML or XSD File", extensions);
        xmlXsdFilePath.setText("");

        EMetadataEncoding[] values = EMetadataEncoding.values();
        String[] encodingData = new String[values.length];
        for (int j = 0; j < values.length; j++) {
            encodingData[j] = values[j].getName();
        }
        encodingCombo = new LabelledCombo(compositeOutput, Messages.getString("XmlFileOutputStep1Form.Encording"),
                Messages.getString("XmlFileOutputStep1Form.Encording"), encodingData, 1, true, SWT.NONE);
        encodingCombo.setText("");
        Composite limitation = new Composite(compositeOutput, SWT.NONE);
        limitation.setLayout(new GridLayout(2, false));
        labelLimitation = new Label(limitation, SWT.LEFT);
        labelLimitation.setText("Limit");
        commonNodesLimitation = new Text(limitation, SWT.BORDER);
        GridData gd = new GridData(18, 12);
        commonNodesLimitation.setLayoutData(gd);
        commonNodesLimitation.setText(String.valueOf(TreePopulator.getLimit()));
    }

    private void createOutputFile(final Composite mainComposite, final int width, final int height) {
        Group group = Form.createGroup(mainComposite, 1, "Output File Path", height);
        GridData fileData = new GridData(GridData.FILL_HORIZONTAL);
        fileData.heightHint = 60;
        group.setLayoutData(fileData);
        Composite compositeFile = Form.startNewDimensionnedGridLayout(group, 3, WIDTH_GRIDDATA_PIXEL, height);
        String[] outputExtensions = new String[] { "*.xml" };
        outputFilePath = new LabelledFileField(compositeFile, "Output file", outputExtensions);
        outputFilePath.setText("");
    }

    private void createFileContentViewer(final Composite mainComposite, final int width, final int height) {
        Group group = Form.createGroup(mainComposite, 1, "File Viewer", height);
        Composite compositeFileViewer = Form.startNewDimensionnedGridLayout(group, 1, WIDTH_GRIDDATA_PIXEL, height);

        GridData gridData = new GridData(GridData.FILL_BOTH);

        availableXmlTree = new Tree(compositeFileViewer, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        availableXmlTree.setLayoutData(gridData);
    }

    private void createFileContentText(final Composite mainComposite, final int width, final int height) {
        Group group = Form.createGroup(mainComposite, 1, "File Content", height);
        Composite compositeFileContext = Form.startNewDimensionnedGridLayout(group, 1, WIDTH_GRIDDATA_PIXEL, height);
        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.minimumWidth = WIDTH_GRIDDATA_PIXEL;
        fileContentText = new Text(compositeFileContext, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        fileContentText.setLayoutData(gridData);
        fileContentText.setToolTipText("When Filepath is specified, you can read here the "
                + TreePopulator.getMaximumRowsToPreview() + " first rows of the file.");
        fileContentText.setText("Filepath must be specified to show the Data file");
    }

    private void initNodeOrder(FOXTreeNode node) {
        if (node == null) {
            return;
        }
        FOXTreeNode parent = node.getParent();
        if (parent == null) {
            node.setOrder(1);
            String path = TreeUtil.getPath(node);
            orderMap.put(path, order);
            order++;
        }
        List<FOXTreeNode> childNode = node.getChildren();
        for (FOXTreeNode child : childNode) {
            child.setOrder(order);
            String path = TreeUtil.getPath(child);
            orderMap.put(path, order);
            order++;
            if (child.getChildren().size() > 0) {
                initNodeOrder(child);
            }
        }
    }

    private void updateConnection(String file) {
        if (file == null || "".equals(file)) {
            return;
        }
        List<FOXTreeNode> nodeList = TreeUtil.getFoxTreeNodes(file);
        if (ConnectionHelper.getTables(getConnection()).isEmpty()) {
            MetadataTable table = ConnectionFactory.eINSTANCE.createMetadataTable();
            RecordFile record = (RecordFile) ConnectionHelper.getPackage(getConnection().getName(), getConnection(),
                    RecordFile.class);
            if (record != null) { // hywang
                PackageHelper.addMetadataTable(table, record);
            } else {
                RecordFile newrecord = RecordFactory.eINSTANCE.createRecordFile();
                newrecord.setName(connection.getName());
                ConnectionHelper.addPackage(newrecord, connection);
                PackageHelper.addMetadataTable(table, newrecord);
            }
        }
        EList schemaMetadataColumn = ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0].getColumns();
        schemaMetadataColumn.clear();
        initMetadataTable(nodeList, schemaMetadataColumn);
        if (nodeList.isEmpty()) {
            return;
        }
        FOXTreeNode foxTreeNode = nodeList.get(0);
        EList root = getConnection().getRoot();
        EList loop = getConnection().getLoop();
        EList group = getConnection().getGroup();
        root.clear();
        loop.clear();
        group.clear();
        if (foxTreeNode != null) {
            initNodeOrder(foxTreeNode);
            tableLoader((Element) foxTreeNode, "", root, foxTreeNode.getDefaultValue());
            Element loopNode = (Element) TreeUtil.getLoopNode(foxTreeNode);
            if (loopNode != null) {
                String path = TreeUtil.getPath(loopNode);
                tableLoader(loopNode, path.substring(0, path.lastIndexOf("/")), loop, loopNode.getDefaultValue());
            }
            Element groupNode = (Element) TreeUtil.getGroupNode(foxTreeNode);
            if (groupNode != null) {
                String path = TreeUtil.getPath(groupNode);
                tableLoader(groupNode, path.substring(0, path.lastIndexOf("/")), group, groupNode.getDefaultValue());
            }
        }
    }

    private void initMetadataTable(List<FOXTreeNode> list, EList columnList) {
        for (FOXTreeNode node : list) {
            if (node instanceof Element) {
                String label = node.getLabel();
                if (label != null && !label.equals("")) {
                    String dataType = node.getDataType();
                    MetadataColumn metadataColumn = ConnectionFactory.eINSTANCE.createMetadataColumn();
                    metadataColumn.setLabel(label);
                    metadataColumn.setOriginalField(label);
                    metadataColumn.setTalendType(dataType);
                    columnList.add(metadataColumn);
                }
            }
            if (node.hasChildren()) {
                List<FOXTreeNode> children = node.getChildren();
                initMetadataTable(children, columnList);
            }
        }
    }

    private int getNodeOrder(FOXTreeNode node) {
        if (node != null) {
            String path = TreeUtil.getPath(node);
            return orderMap.get(path);
        }
        return 0;
    }

    private void tableLoader(Element element, String parentPath, List<XMLFileNode> table, String defaultValue) {
        XMLFileNode xmlFileNode = ConnectionFactory.eINSTANCE.createXMLFileNode();
        String currentPath = parentPath + "/" + element.getLabel();
        xmlFileNode.setXMLPath(currentPath);
        xmlFileNode.setRelatedColumn(element.getColumnLabel());
        xmlFileNode.setAttribute(element.isMain() ? "main" : "branch");
        xmlFileNode.setDefaultValue(defaultValue);
        xmlFileNode.setType(element.getDataType());
        xmlFileNode.setOrder(getNodeOrder(element));
        table.add(xmlFileNode);
        for (FOXTreeNode att : element.getAttributeChildren()) {
            xmlFileNode = ConnectionFactory.eINSTANCE.createXMLFileNode();
            xmlFileNode.setXMLPath(att.getLabel());
            xmlFileNode.setRelatedColumn(att.getColumnLabel());
            xmlFileNode.setAttribute("attri");
            xmlFileNode.setDefaultValue(att.getDefaultValue());
            xmlFileNode.setType(att.getDataType());
            xmlFileNode.setOrder(getNodeOrder(att));
            table.add(xmlFileNode);
        }
        for (FOXTreeNode att : element.getNameSpaceChildren()) {
            xmlFileNode = ConnectionFactory.eINSTANCE.createXMLFileNode();
            xmlFileNode.setXMLPath(att.getLabel());
            xmlFileNode.setRelatedColumn(att.getColumnLabel());
            xmlFileNode.setAttribute("ns");
            xmlFileNode.setDefaultValue(att.getDefaultValue());
            xmlFileNode.setType(att.getDataType());
            xmlFileNode.setOrder(getNodeOrder(att));
            table.add(xmlFileNode);
        }
        List<FOXTreeNode> children = element.getElementChildren();
        for (FOXTreeNode child : children) {
            if (!child.isGroup() && !child.isLoop()) {
                tableLoader((Element) child, currentPath, table, child.getDefaultValue());
            }
        }

    }

    @Override
    protected void addFieldsListeners() {
        xmlXsdFilePath.addSelectionListener(new SelectionListener() {

            public void widgetSelected(SelectionEvent event) {
                String text = xmlXsdFilePath.getText();
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                            connectionItem.getConnection(), true);
                    text = TalendTextUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
                }
                // getConnection().setXmlFilePath(PathUtils.getPortablePath(xmlXsdFilePath.getText()));
                File file = new File(text);
                if (file.exists()) {
                    updateConnection(text);
                }

            }

            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        xmlXsdFilePath.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent event) {
                String text = xmlXsdFilePath.getText();
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                            connectionItem.getConnection(), true);
                    text = TalendTextUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
                }
                getConnection().setXmlFilePath(PathUtils.getPortablePath(xmlXsdFilePath.getText()));
                // updateConnection(text);

                StringBuilder fileContent = new StringBuilder();
                BufferedReader in = null;
                File file = null;
                if (tempXmlXsdPath != null && getConnection().getFileContent() != null
                        && getConnection().getFileContent().length > 0 && !isModifing) {
                    file = new File(tempXmlXsdPath);
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e2) {
                            ExceptionHandler.process(e2);
                        }
                        FileOutputStream outStream;
                        try {
                            outStream = new FileOutputStream(file);
                            outStream.write(getConnection().getFileContent());
                            outStream.close();
                        } catch (FileNotFoundException e1) {
                            ExceptionHandler.process(e1);
                        } catch (IOException e) {
                            ExceptionHandler.process(e);
                        }
                    }

                } else {
                    file = new File(text);
                }

                // if (getConnection().getFileContent() == null || getConnection().getFileContent().length <= 0 &&
                // !isModifing) {
                if (!XmlUtil.isXMLFile(file.getPath())) {
                    setFileContent(file);
                }
                // }
                String str;
                try {
                    Charset guessCharset = CharsetToolkit.guessEncoding(file, 4096);
                    in = new BufferedReader(new InputStreamReader(new FileInputStream(file), guessCharset.displayName()));

                    while ((str = in.readLine()) != null) {
                        fileContent.append(str + "\n");
                        // for encoding
                        if (str.contains("encoding")) {
                            String regex = "^<\\?xml\\s*version=\\\"[^\\\"]*\\\"\\s*encoding=\\\"([^\\\"]*)\\\"\\?>$";
                            Perl5Compiler compiler = new Perl5Compiler();
                            Perl5Matcher matcher = new Perl5Matcher();
                            Pattern pattern = null;
                            try {
                                pattern = compiler.compile(regex);
                                if (matcher.contains(str, pattern)) {
                                    MatchResult matchResult = matcher.getMatch();
                                    if (matchResult != null) {
                                        encoding = matchResult.group(1);
                                    }
                                }
                            } catch (MalformedPatternException malE) {
                                ExceptionHandler.process(malE);
                            }
                        }
                    }

                    fileContentText.setText(new String(fileContent));

                } catch (Exception e) {
                    String msgError = Messages.getString("FileStep1.filepath") + " \""
                            + xmlXsdFilePath.getText().replace("\\\\", "\\") + "\"\n";
                    if (e instanceof FileNotFoundException) {
                        msgError = msgError + Messages.getString("FileStep1.fileNotFoundException");
                    } else if (e instanceof EOFException) {
                        msgError = msgError + Messages.getString("FileStep1.eofException"); //$NON-NLS-1$
                    } else if (e instanceof IOException) {
                        msgError = msgError + Messages.getString("FileStep1.fileLocked"); //$NON-NLS-1$
                    } else {
                        msgError = msgError + Messages.getString("FileStep1.noExist"); //$NON-NLS-1$
                    }
                    fileContentText.setText(msgError);
                    if (!isReadOnly()) {
                        updateStatus(IStatus.ERROR, msgError);
                    }
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (Exception exception) {
                        ExceptionHandler.process(exception);
                    }
                }
                if (getConnection().getEncoding() == null || "".equals(getConnection().getEncoding())) {
                    getConnection().setEncoding(encoding);
                    if (encoding != null && !"".equals(encoding)) {
                        encodingCombo.setText(encoding);
                    } else {
                        encodingCombo.setText("UTF-8");
                    }
                }
                if (tempXmlXsdPath != null && getConnection().getFileContent() != null
                        && getConnection().getFileContent().length > 0 && !isModifing) {
                    valid = treePopulator.populateTree(tempXmlXsdPath, treeNode);
                } else {
                    valid = treePopulator.populateTree(text, treeNode);
                }
                checkFieldsValue();
                isModifing = true;
            }
        });

        encodingCombo.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                getConnection().setEncoding(encodingCombo.getText());
                checkFieldsValue();
            }
        });

        commonNodesLimitation.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                String str = commonNodesLimitation.getText();
                if ((!str.matches("\\d+")) || (Integer.valueOf(str) < 0)) {
                    commonNodesLimitation.setText(String.valueOf(treePopulator.getLimit()));
                } else {
                    treePopulator.setLimit(Integer.valueOf(str));
                }
                valid = treePopulator.populateTree(xmlXsdFilePath.getText(), treeNode);
                checkFieldsValue();
            }
        });
        commonNodesLimitation.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {

            }

            public void focusLost(FocusEvent e) {
                commonNodesLimitation.setText(String.valueOf(TreePopulator.getLimit()));
            }

        });

        outputFilePath.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent e) {
                getConnection().setOutputFilePath(PathUtils.getPortablePath(outputFilePath.getText()));
                checkFieldsValue();
            }
        });
    }

    @Override
    protected void addUtilsButtonListeners() {
        noFileButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                xmlXsdFilePath.setEditable(false);
                xmlXsdFilePath.setText("");
                encodingCombo.setEnabled(false);
                commonNodesLimitation.setEditable(false);
                availableXmlTree.setEnabled(false);
                fileContentText.setEnabled(false);
                getConnection().setXmlFilePath("");
                ConnectionHelper.getTables(getConnection()).toArray(new MetadataTable[0])[0].getColumns().clear();
                // ((MetadataTable) getConnection().getTables().get(0)).getColumns().clear();
                getConnection().getRoot().clear();
                getConnection().getLoop().clear();
                getConnection().getGroup().clear();
                checkFieldsValue();
            }
        });
        useFileButton.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                xmlXsdFilePath.setEditable(true);
                String text = xmlXsdFilePath.getText();
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(
                            connectionItem.getConnection(), true);
                    text = TalendTextUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
                }
                getConnection().setXmlFilePath(text);
                updateConnection(text);
                encodingCombo.setEnabled(true);
                commonNodesLimitation.setEditable(true);
                availableXmlTree.setEnabled(true);
                fileContentText.setEnabled(true);
                fileContentText.setEditable(false);
                checkFieldsValue();
            }
        });
    }

    @Override
    protected boolean checkFieldsValue() {
        boolean editable = xmlXsdFilePath.getEditable();
        StringBuffer msgError = new StringBuffer();
        if (creation && !noFileButton.getSelection() && !useFileButton.getSelection()) {
            msgError.append("Should select one model\n");
        }
        if (creation && editable && xmlXsdFilePath.getText() == "") {
            msgError.append("Xml or Xsd filepath must be specified\n");
        }
        if (!valid && creation) {
            String xmlXsdText = xmlXsdFilePath.getText();
            if (xmlXsdText != null && !"".equals(xmlXsdText)) {
                if (isContextMode()) {
                    ContextType contextType = ConnectionContextHelper
                            .getContextTypeForContextMode(connectionItem.getConnection());
                    xmlXsdText = TalendTextUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType,
                            xmlXsdFilePath.getText()));
                }
                msgError.append(xmlXsdText + " is not found or the xml format is incorrect.\n");
            }
        }
        String text = outputFilePath.getText();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection(), true);
            text = TalendTextUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, text));
        }
        if (text != null && !text.equals("") && !text.endsWith("xml")) {
            msgError.append("Output file is not a xml file\n");
        }
        if ("".equals(msgError.toString())) {
            updateStatus(IStatus.OK, null);
            return true;
        }
        updateStatus(IStatus.ERROR, msgError.toString());
        return false;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (getConnection().getEncoding() != null && !getConnection().getEncoding().equals("")) {
                encodingCombo.setText(getConnection().getEncoding());
                isModifing = false;
                xmlXsdFilePath.setText(getConnection().getXmlFilePath());
                outputFilePath.setText(getConnection().getOutputFilePath());
            }
            String xmlFilePath = getConnection().getXmlFilePath();
            boolean enable = (xmlFilePath != null && !xmlFilePath.equals(""));
            if (!creation) {
                noFileButton.setSelection(!enable);
                useFileButton.setSelection(enable);
            }
            if (!isContextMode()) {
                xmlXsdFilePath.setEditable(enable);
                encodingCombo.setEnabled(enable);
                commonNodesLimitation.setEditable(enable);
                availableXmlTree.setEnabled(enable);
                fileContentText.setEnabled(enable);
                outputFilePath.setEditable(true);
            } else {
                // noFileButton.setEnabled(!isContextMode());
                // useFileButton.setEnabled(!isContextMode());
                outputFilePath.setEditable(!isContextMode());
                xmlXsdFilePath.setEditable(!isContextMode());
                encodingCombo.setEnabled(!isContextMode());
                commonNodesLimitation.setEditable(!isContextMode());
                availableXmlTree.setEnabled(!isContextMode());
                fileContentText.setEnabled(!isContextMode());
            }
        }
    }

    @Override
    protected void adaptFormToReadOnly() {

    }

    private void setFileContent(File initFile) {
        InputStream stream = null;
        try {
            stream = initFile.toURL().openStream();
            byte[] innerContent = new byte[stream.available()];
            stream.read(innerContent);
            stream.close();
            getConnection().setFileContent(innerContent);
        } catch (MalformedURLException e) {
            ExceptionHandler.process(e);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
    }

    private void initFileContent() {
        if (getConnection().getXmlFilePath() == null || "".equals(getConnection().getXmlFilePath())) {
            return;
        }
        byte[] bytes = getConnection().getFileContent();
        Project project = ProjectManager.getInstance().getCurrentProject();
        IProject fsProject = null;
        try {
            fsProject = ResourceModelUtils.getProject(project);
        } catch (PersistenceException e2) {
            ExceptionHandler.process(e2);
        }
        if (fsProject == null) {
            return;
        }
        String temPath = fsProject.getLocationURI().getPath() + File.separator + "temp";
        String fileName = "";

        String xmlXsdPath = getConnection().getXmlFilePath();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
            xmlXsdPath = TalendTextUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, xmlXsdPath));
        }
        if (xmlXsdPath != null && XmlUtil.isXMLFile(xmlXsdPath)) {
            fileName = StringUtil.TMP_XML_FILE;
        } else if (xmlXsdPath != null && XmlUtil.isXSDFile(xmlXsdPath)) {
            fileName = StringUtil.TMP_XSD_FILE;
        }
        File temfile = new File(temPath + File.separator + fileName);
        if (!temfile.exists()) {
            try {
                temfile.createNewFile();
            } catch (IOException e) {
                ExceptionHandler.process(e);
            }
        }
        FileOutputStream outStream;
        try {
            outStream = new FileOutputStream(temfile);
            outStream.write(bytes);
            outStream.close();
        } catch (FileNotFoundException e1) {
            ExceptionHandler.process(e1);
        } catch (IOException e) {
            ExceptionHandler.process(e);
        }
        tempXmlXsdPath = temfile.getPath();
        if (isContextMode()) {
            ContextType contextType = ConnectionContextHelper.getContextTypeForContextMode(connectionItem.getConnection());
            tempXmlXsdPath = TalendTextUtils.removeQuotes(ConnectionContextHelper.getOriginalValue(contextType, tempXmlXsdPath));
        }
        valid = this.treePopulator.populateTree(tempXmlXsdPath, treeNode);

    }
}
