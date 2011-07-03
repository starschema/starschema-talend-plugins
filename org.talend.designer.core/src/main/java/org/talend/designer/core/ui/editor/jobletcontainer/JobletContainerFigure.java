package org.talend.designer.core.ui.editor.jobletcontainer;

import org.eclipse.draw2d.ActionEvent;
import org.eclipse.draw2d.ActionListener;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.ui.utils.image.ColorUtils;
import org.talend.commons.ui.utils.workbench.gef.SimpleHtmlFigure;
import org.talend.core.model.process.IElementParameter;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.designer.core.model.components.EParameterName;
import org.talend.designer.core.ui.AbstractMultiPageTalendEditor;
import org.talend.designer.core.ui.editor.cmd.PropertyChangeCommand;
import org.talend.designer.core.ui.editor.nodes.Node;

public class JobletContainerFigure extends Figure {

    private JobletContainer jobletContainer;

    private RoundedRectangle outlineFigure;

    private SimpleHtmlFigure titleFigure;

    private RoundedRectangle rectFig;

    private JobletCollapseFigure collapseFigure;

    private RGB mainColor;

    private boolean showTitle;

    private String title;

    private RGB subjobTitleColor;

    /**
     * DOC hwang JobletContainerFigure constructor comment.
     * 
     * @param model
     */
    public JobletContainerFigure(final JobletContainer jobletContainer) {
        setLayoutManager(new FreeformLayout());
        this.jobletContainer = jobletContainer;

        outlineFigure = new RoundedRectangle();
        rectFig = new RoundedRectangle();
        titleFigure = new SimpleHtmlFigure();
        titleFigure.setOpaque(true);
        collapseFigure = new JobletCollapseFigure();

        collapseFigure.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {
                doCollapse();
            }
        });

        initSubJobTitleColor();

        updateData();

        initializejobletContainer(jobletContainer.getJobletContainerRectangle());
    }

    public void doCollapse() {
        if (jobletContainer != null && !jobletContainer.isReadOnly()) {
            PropertyChangeCommand ppc = new PropertyChangeCommand(jobletContainer, EParameterName.COLLAPSED.getName(),
                    !jobletContainer.isCollapsed());
            IProcess ipro = jobletContainer.getNode().getProcess();
            ppc.execute();
            reSelection();
        }
    }

    /**
     * hwang Comment method "initSubJobTitleColor".
     */
    private void initSubJobTitleColor() {
        IElementParameter colorParameter = jobletContainer.getElementParameter(EParameterName.SUBJOB_TITLE_COLOR.getName());
        // Color titleColor = ColorUtils.SUBJOB_TITLE_COLOR;
        //        if (jobletContainer.getJobletStartNode().getComponent().getName().equals("tPrejob") //$NON-NLS-1$
        //                || jobletContainer.getJobletStartNode().getComponent().getName().equals("tPostjob")) { //$NON-NLS-1$
        // // titleColor = ColorUtils.SPECIAL_SUBJOB_TITLE_COLOR;
        // }
        // RGB defaultSubjobColor =
        // DesignerColorUtils.getPreferenceSubjobRGB(DesignerColorUtils.SUBJOB_TITLE_COLOR_NAME,
        // DesignerColorUtils.SUBJOB_TITLE_COLOR);
        // if (colorParameter.getValue() == null) {
        // subjobTitleColor = defaultSubjobColor;
        // String colorValue = ColorUtils.getRGBValue(subjobTitleColor);
        // colorParameter.setValue(colorValue);
        // } else {
        // String strRgb = (String) colorParameter.getValue();
        // subjobTitleColor = ColorUtils.parseStringToRGB(strRgb, defaultSubjobColor);
        // }
    }

    private void reSelection() {
        // select the start node.
        if (jobletContainer.isCollapsed()) {
            IProcess2 process = jobletContainer.getProcess();
            // this.get
            AbstractMultiPageTalendEditor editor = (AbstractMultiPageTalendEditor) process.getEditor();
            Node startNode = jobletContainer.getJobletStartNode();
            if (startNode != null && editor != null) {
                editor.selectNode(startNode);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.draw2d.Figure#paint(org.eclipse.draw2d.Graphics)
     */
    @Override
    public void paint(Graphics graphics) {
        graphics.setAlpha(100);
        super.paint(graphics);
    }

    public void initializejobletContainer(Rectangle rectangle) {
        disposeColors();
        Point location = this.getLocation();
        collapseFigure.setCollapsed(jobletContainer.isCollapsed());
        titleFigure.setText("<b> " + title + "</b>"); //$NON-NLS-1$ //$NON-NLS-2$
        Dimension preferedSize = titleFigure.getPreferredSize();
        preferedSize = preferedSize.getExpanded(0, 3);
        rectangle.width += 32;

        collapseFigure.setLocation(new Point(location.x, location.y));
        collapseFigure.setSize(preferedSize.height, preferedSize.height);

        titleFigure.setSize(preferedSize.width, preferedSize.height - 2);
        titleFigure.setLocation(new Point((rectangle.width - preferedSize.height) / 2 + location.x, location.y + 1));
        titleFigure.setVisible(showTitle);

        outlineFigure.setLocation(new Point(location.x, location.y));
        outlineFigure.setVisible(showTitle);
        outlineFigure.setBackgroundColor(new Color(Display.getDefault(), new RGB(130, 240, 100)));
        outlineFigure.setForegroundColor(new Color(Display.getDefault(), new RGB(220, 120, 120)));
        outlineFigure.setSize(rectangle.width, preferedSize.height);

        // collapseFigure.setBackgroundColor(new Color(null, 50, 50, 250));

        rectFig.setLocation(new Point(location.x, /* preferedSize.height + */location.y));
        rectFig.setSize(new Dimension(rectangle.width, rectangle.height /*- preferedSize.height*/));
        rectFig.setBackgroundColor(new Color(Display.getDefault(), new RGB(130, 240, 100)));
        rectFig.setForegroundColor(new Color(Display.getDefault(), new RGB(220, 120, 120)));
    }

    public void disposeColors() {
        if (rectFig.getForegroundColor() != null && !rectFig.getForegroundColor().isDisposed()) {
            rectFig.getForegroundColor().dispose();
        }
        if (rectFig.getBackgroundColor() != null && !rectFig.getBackgroundColor().isDisposed()) {
            rectFig.getBackgroundColor().dispose();
        }
        if (outlineFigure.getForegroundColor() != null && !outlineFigure.getForegroundColor().isDisposed()) {
            outlineFigure.getForegroundColor().dispose();
        }
        if (outlineFigure.getBackgroundColor() != null && !outlineFigure.getBackgroundColor().isDisposed()) {
            outlineFigure.getBackgroundColor().dispose();
        }
    }

    /**
     * hwang Comment method "updateSubJobTitleColor".
     */
    public void updateSubJobTitleColor() {
        String rgb = (String) jobletContainer.getPropertyValue(EParameterName.SUBJOB_TITLE_COLOR.getName());
        if (rgb != null && !"".equals(rgb)) { //$NON-NLS-1$
            subjobTitleColor = ColorUtils.parseStringToRGB(rgb);
        } else {
            initSubJobTitleColor();
        }
        updateData();
    }

    /**
     * hwang Comment method "updateData".
     */
    public void updateData() {

        showTitle = !jobletContainer.isCollapsed();

        title = (String) jobletContainer.getPropertyValue(EParameterName.SUBJOB_TITLE.getName());

        this.getChildren().remove(outlineFigure);
        this.getChildren().remove(rectFig);
        outlineFigure.getChildren().clear();
        rectFig.getChildren().clear();

        if (showTitle) {
            outlineFigure.add(titleFigure);
            outlineFigure.add(collapseFigure);
            add(rectFig, null, 0);
            add(outlineFigure, null, 1);
        } else {
            outlineFigure.add(titleFigure);
            rectFig.add(collapseFigure);
            add(outlineFigure, null, 0);
            add(rectFig, null, 1);
        }
    }
}
