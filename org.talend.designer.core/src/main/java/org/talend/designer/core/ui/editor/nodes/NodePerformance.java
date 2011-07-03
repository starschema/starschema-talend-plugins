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
package org.talend.designer.core.ui.editor.nodes;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.talend.core.model.process.Element;
import org.talend.core.model.process.IElementParameter;
import org.talend.designer.core.DesignerPlugin;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.runprocess.IPerformanceData;
import org.talend.designer.runprocess.IRunProcessService;

/**
 * Element for the performance, linked to a NodeContainer. <br/>
 * 
 * $Id: NodePerformance.java 54939 2011-02-11 01:34:57Z mhirt $
 * 
 */
public class NodePerformance extends Element {

    private NodeContainer nodeContainer;

    private String label = ""; //$NON-NLS-1$

    private Dimension size = new Dimension();

    public NodePerformance(NodeContainer nodeContainer) {
        super();

        this.nodeContainer = nodeContainer;
    }

    /**
     * Format performance to a simple HTML.
     * 
     * @param data Performance data to be rendered.
     * @return HTML string.
     */
    private String htmlFromPerformance(String data) {
        StringBuffer html = new StringBuffer();

        IRunProcessService service = DesignerPlugin.getDefault().getRunProcessService();
        IPerformanceData perf = service.createPerformanceData(data);

        if (IPerformanceData.ACTION_PERF.equals(perf.getAction())) {
            final String perfPattern = "<font color=''#4477BB''>" + "{0, number,#} rows - {1,number,#.##}s<br>" //$NON-NLS-1$ //$NON-NLS-2$
                    + "<b>{2,number,#.##} rows/s</b>" + "</font>"; //$NON-NLS-1$ //$NON-NLS-2$
            long lineCount = perf.getLineCount();
            long processingTime = perf.getProcessingTime();
            double avg = processingTime > 0 ? (double) lineCount * 1000d / (double) processingTime : 0d;
            MessageFormat mf = new MessageFormat(perfPattern, Locale.US);
            html.append(mf.format(new Object[] { new Long(lineCount), new Double((double) processingTime / 1000d),
                    new Double(avg) }));
        } else if (IPerformanceData.ACTION_START.equals(perf.getAction())) {
            final String perfPattern = "<font color='#AA3322'>" + "<i>Starting</i>" + "</font>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            html.append(perfPattern);
        } else if (IPerformanceData.ACTION_STOP.equals(perf.getAction())) {
            final String perfPattern = "<font color=''#229922''>" + "{0, number,#} rows in {1,number,#.##}s<br>" //$NON-NLS-1$ //$NON-NLS-2$
                    + "<i>{2,number,#.##} rows/s</i>" + "</font>"; //$NON-NLS-1$ //$NON-NLS-2$
            long lineCount = perf.getLineCount();
            long processingTime = perf.getProcessingTime();
            double avg = processingTime > 0 ? (double) lineCount * 1000d / (double) processingTime : 0d;
            MessageFormat mf = new MessageFormat(perfPattern);
            html.append(mf.format(new Object[] { new Long(lineCount), new Double((double) processingTime / 1000d),
                    new Double(avg) }));
        }

        return html.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.Element#getElementName()
     */
    @Override
    public String getElementName() {
        return nodeContainer.getNode().getElementName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.Element#getElementParameters()
     */
    @Override
    public List<? extends IElementParameter> getElementParameters() {
        return nodeContainer.getNode().getElementParameters();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.Element#getPropertyValue(java.lang.Object)
     */
    @Override
    public Object getPropertyValue(String id) {
        return nodeContainer.getNode().getPropertyValue(id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.core.ui.editor.Element#setPropertyValue(java.lang.Object, java.lang.Object)
     */
    @Override
    public void setPropertyValue(String id, Object value) {
        nodeContainer.getNode().setPropertyValue(id, value);
    }

    public void setNodeContainer(NodeContainer nodeContainer) {
        this.nodeContainer = nodeContainer;
    }

    public NodeContainer getNodeContainer() {
        return this.nodeContainer;
    }

    public Dimension getSize() {
        return this.size;
    }

    public void setSize(Dimension size) {
        this.size = size;
    }

    /**
     * DOC nrousseau Comment method "setPerfData".
     * 
     * @param perfData
     */
    public void setPerfData(String perfData) {
        setLabel(htmlFromPerformance(perfData));
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Point getLocation() {
        Point loc = this.getNodeContainer().getNode().getLocation().getCopy();
        Point offset = this.getNodeContainer().getNodeLabel().getOffset();
        Point textOffset = new Point();

        textOffset.y = this.getNodeContainer().getNode().getSize().height + 16;
        textOffset.x = (this.getNodeContainer().getNode().getSize().width - size.width) / 2;
        loc.translate(textOffset.x + offset.x, textOffset.y + offset.y);
        return loc;
    }

    public boolean isReadOnly() {
        return nodeContainer.isReadOnly();
    }

    public void setReadOnly(boolean readOnly) {
    }
}
