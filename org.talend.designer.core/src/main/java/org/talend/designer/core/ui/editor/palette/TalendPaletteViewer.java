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
package org.talend.designer.core.ui.editor.palette;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.gef.EditDomain;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.ui.palette.PaletteViewer;
import org.eclipse.gef.ui.parts.PaletteViewerKeyHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.talend.commons.ui.runtime.image.ECoreImage;
import org.talend.commons.ui.runtime.image.EImage;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.commons.utils.threading.ExecutionLimiter;
import org.talend.core.model.components.ComponentUtilities;
import org.talend.designer.core.i18n.Messages;

/**
 * 
 */
public class TalendPaletteViewer extends PaletteViewer {

    private static final String TOOL_TIP = Messages.getString("TalendPaletteViewer.tooltipValue"); //$NON-NLS-1$

    private static final String SEARCH_COMPONENT = Messages.getString("TalendPaletteViewer.searchComponent"); //$NON-NLS-1$

    private static List<Text> filters = new ArrayList<Text>();

    private static Text setTextOnly;

    private static String currentFilterText;

    private ThreadPoolExecutor executor;

    private final ExecutionLimiter expandLimiter = new ExecutionLimiter(500, true) {

        @Override
        public void execute(final boolean isFinalExecution, Object data) {
            final Text text = (Text) data;
            text.getDisplay().asyncExec(new Runnable() {

                public void run() {
                    ExpandPaletteRunnable runnable = (ExpandPaletteRunnable) executor.getQueue().poll();
                    if (runnable != null) {
                        runnable.stopExpand();
                    }
                    filterPalette(text);
                    if (!text.getText().equals("")) { //$NON-NLS-1$
                        executor.execute(new ExpandPaletteRunnable());
                    }
                }
            });
        }
    };

    public TalendPaletteViewer(EditDomain graphicalViewerDomain) {
        setEditDomain(graphicalViewerDomain);
        setKeyHandler(new PaletteViewerKeyHandler(this));
        setEditPartFactory(new TalendPaletteEditPartFactory());
        executor = new ThreadPoolExecutor(1, 2, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(3));
    }

    /**
     * FIXME ggu
     * 
     * Because the method createControl is final in super calss ScrollingGraphicalViewer,
     * 
     * Then, use the reflection function to move to class TalendPaletteViewerProvider.
     */
    // @Override
    // public Control createControl(Composite parent) {
    // FigureCanvas canvas = new TalendFigureCanvas(parent, getLightweightSystem(), this);
    // setControl(canvas);
    // installRootFigure(); // change the parent method to "protected".
    // return canvas;
    // }
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gef.ui.parts.ScrollingGraphicalViewer#creatToolControl(org.eclipse.swt.widgets.Composite)
     */
    public Control creatToolControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginLeft = 2;
        layout.marginRight = 2;
        layout.marginTop = 2;
        layout.marginBottom = 2;
        layout.marginHeight = 0;
        layout.marginWidth = 0;

        container.setLayout(layout);
        final Text text = new Text(container, SWT.BORDER);

        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        filters.add(text);
        initFilterTextControl(text);

        ToolBar toolbar = new ToolBar(container, SWT.NONE);
        GridLayout toolbarLayout = new GridLayout();
        toolbarLayout.marginLeft = 0;
        toolbarLayout.marginRight = 0;
        toolbarLayout.marginTop = 0;
        toolbarLayout.marginBottom = 0;
        toolbarLayout.marginHeight = 0;
        toolbarLayout.marginWidth = 0;
        toolbar.setLayout(toolbarLayout);

        Image clearImage = ImageProvider.getImage(ECoreImage.PALETTE_CLEAR_ICON);
        Image findImage = ImageProvider.getImage(EImage.FIND_ICON);

        ToolItem findItem = new ToolItem(toolbar, SWT.NONE);
        findItem.setImage(findImage);
        findItem.setToolTipText("Search");
        findItem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                startFiltering(text);
            }

            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
            }
        });

        final ToolItem clearItem = new ToolItem(toolbar, SWT.NONE);
        clearItem.setImage(clearImage);
        clearItem.addSelectionListener(new SelectionAdapter() {

            public void widgetSelected(SelectionEvent e) {
                text.setText(""); //$NON-NLS-1$
                // Reset to default palette
                startFiltering(text);
            }
        });
        return container;
    }

    /**
     * yzhang Comment method "initFilterTextControl".
     * 
     * @param control
     */
    private void initFilterTextControl(Text filterText) {
        if (currentFilterText != null) {
            filterText.setText(currentFilterText);
        } else {
            filterText.setText(SEARCH_COMPONENT);
        }

        filterText.setToolTipText(TOOL_TIP);
        configListeners(filterText);

    }

    /**
     * yzhang Comment method "configListeners".
     * 
     * @param text
     */
    private void configListeners(final Text text) {
        text.addMouseListener(new MouseListener() {

            public void mouseDoubleClick(MouseEvent e) {

            }

            public void mouseDown(MouseEvent e) {
                if (text.getText().equals(SEARCH_COMPONENT)) {
                    text.setText(""); //$NON-NLS-1$
                }
            }

            public void mouseUp(MouseEvent e) {

            }
        });

        text.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                text.setText(""); //$NON-NLS-1$
            }

            public void focusLost(FocusEvent e) {
                if (text.getText() == "") { //$NON-NLS-1$
                    text.setText(SEARCH_COMPONENT);
                }
            }
        });

        text.addSelectionListener(new SelectionAdapter() {

            public void widgetDefaultSelected(SelectionEvent e) {
                startFiltering(text);
            }

        });
    }

    private void startFiltering(final Text text) {
        if (!text.getText().equals(SEARCH_COMPONENT)) {
            expandLimiter.resetTimer();
            expandLimiter.startIfExecutable(true, text);
        }
    }

    /**
     * yzhang TalendPaletteViewer class global comment. Detailled comment
     */
    private class ExpandPaletteRunnable implements Runnable {

        private volatile boolean stop;

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Runnable#run()
         */
        public void run() {
            Display display = Display.getDefault();
            if (display == null) {
                return;
            }
            display.syncExec(new Runnable() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see java.lang.Runnable#run()
                 */
                public void run() {
                    List children = ComponentUtilities.getPaletteRoot().getChildren();
                    int counter = 0;
                    for (Object obj : children) {
                        if (stop || counter > 3) {
                            return;
                        }
                        if (obj instanceof PaletteDrawer) {
                            ((PaletteDrawer) obj).setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
                            counter++;
                            PaletteDrawer subDrawer = getSubDrawer((PaletteDrawer) obj);
                            if (subDrawer != null) {
                                subDrawer.setInitialState(PaletteDrawer.INITIAL_STATE_OPEN);
                                counter++;
                            }
                        }

                    }
                }
            });

        }

        public void stopExpand() {
            stop = true;
        }
    }

    private PaletteDrawer getSubDrawer(PaletteDrawer parent) {
        for (Object obj : parent.getChildren()) {
            if (obj instanceof PaletteDrawer) {
                return (PaletteDrawer) obj;
            }
        }
        return null;
    }

    private void filterPalette(Text text) {
        if (setTextOnly == text) {
            return;
        }

        List<Text> disposed = new ArrayList<Text>();
        for (Text otherText : filters) {

            if (text == otherText) {
                continue;
            }

            if (otherText.isDisposed()) {
                disposed.add(otherText);
                continue;
            }
            setTextOnly = otherText;
            otherText.setText(text.getText());
            setTextOnly = null;
        }

        currentFilterText = text.getText();
        if (!currentFilterText.equals(SEARCH_COMPONENT)) {
            ComponentUtilities.filterPalette(currentFilterText.trim());
        }

        filters.removeAll(disposed);
    }

}
