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
package org.talend.repository.ui.wizards.metadata;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.talend.commons.ui.image.ImageProvider;
import org.talend.commons.ui.swt.formtools.LabelledCombo;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IContextManager;
import org.talend.core.model.properties.ContextItem;
import org.talend.core.ui.images.ECoreImage;
import org.talend.designer.core.model.utils.emf.talendfile.ContextType;
import org.talend.repository.i18n.Messages;
import org.talend.repository.ui.utils.ConnectionContextHelper;

/**
 * DOC ggu class global comment. Detailled comment
 */
public class ContextSetsSelectionDialog extends SelectionDialog {

    private static final String DEFAULT_FLAG = "(default)"; //$NON-NLS-1$

    private static final String TITLE = Messages.getString("ContextSetsSelectionDialog.Title"); //$NON-NLS-1$

    private Object source;

    private LabelledCombo combo;

    private List<String> contextSetsList = new ArrayList<String>();

    private String defalutContext = ConnectionContextHelper.EMPTY;

    private String selectedContext = null;

    private boolean canCancel = false;

    public ContextSetsSelectionDialog(Shell parentShell, Object source, boolean canCancel) {
        super(parentShell == null ? calcShell() : parentShell);
        this.source = source;
        this.canCancel = canCancel;
        setDefaultImage(ImageProvider.getImage(ECoreImage.CONTEXT_ICON));
        setHelpAvailable(false);
        setTitle(TITLE);
        setMessage(Messages.getString("ContextSetsSelectionDialog.Messages")); //$NON-NLS-1$
        initSets();
    }

    public ContextSetsSelectionDialog(ContextItem contextItem) {
        this(calcShell(), contextItem, false);
    }

    /**
     * DOC zli Comment method "calcShell".
     * 
     * @return
     */
    protected static Shell calcShell() {
        Shell shell = null;
        // from Workbench
        IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
        if (activeWorkbenchWindow != null) {
            shell = activeWorkbenchWindow.getShell();
        }
        // from display
        if (shell == null) {
            final Shell[] shells = new Shell[1];
            Display dis = Display.getDefault();
            if (dis == null) {
                dis = Display.getCurrent();
            }
            if (dis != null) {
                final Display tmp = dis;
                dis.syncExec(new Runnable() {

                    public void run() {
                        Shell activeShell = tmp.getActiveShell();

                        if (activeShell != null) {
                            shells[0] = activeShell;
                        } else {
                            Shell shell = new Shell();
                            shells[0] = shell;
                        }
                    }
                });
            }
            shell = shells[0];
        }
        // at last, new one
        if (shell != null) {

            return shell;
        }
        return null;
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private void initSets() {
        if (source != null) {
            if (source instanceof ContextItem) {
                ContextItem contextItem = (ContextItem) source;
                defalutContext = contextItem.getDefaultContext();
                for (ContextType type : (List<ContextType>) contextItem.getContext()) {
                    String name = type.getName();
                    if (name.equals(defalutContext)) {
                        name = name + DEFAULT_FLAG;
                    }
                    contextSetsList.add(name);
                }
            } else if (source instanceof IContextManager) {
                IContextManager contextManager = (IContextManager) (source);
                for (IContext context : contextManager.getListContext()) {
                    String name = context.getName();
                    if (name.equals(defalutContext)) {
                        name = name + DEFAULT_FLAG;
                    }
                    contextSetsList.add(name);
                }
            }
        }
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        createMessageArea(composite);

        createSourceArea(composite);

        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createSelectionArea(composite);
        return composite;
    }

    private void createSourceArea(Composite parent) {
        if (source != null && source instanceof ContextItem) {
            ContextItem contextItem = (ContextItem) source;

            Composite inner = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginHeight = 0;
            inner.setLayout(gridLayout);
            inner.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Label label = new Label(inner, SWT.NONE);
            label.setText(Messages.getString("ContextSetsSelectionDialog.Source")); //$NON-NLS-1$

            Label source = new Label(inner, SWT.NONE);
            source.setText(contextItem.getProperty().getLabel() + " " + contextItem.getProperty().getVersion()); //$NON-NLS-1$
            source.setForeground(new Color(null, 255, 0, 0));
        }
    }

    private Control createSelectionArea(Composite parent) {
        Composite inner = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginHeight = 0;
        inner.setLayout(gridLayout);
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.minimumWidth = 220;
        gridData.minimumHeight = 150;
        inner.setLayoutData(gridData);

        Group group = new Group(inner, SWT.NONE);
        group.setText(Messages.getString("ContextSetsSelectionDialog.ContextSets")); //$NON-NLS-1$
        gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 10;
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_BOTH));

        combo = new LabelledCombo(group, null, null, contextSetsList, true);
        combo.setText(defalutContext + DEFAULT_FLAG);
        return inner;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
        if (canCancel) {
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        }
    }

    @Override
    protected void okPressed() {
        selectedContext = combo.getItem(combo.getSelectionIndex());
        super.okPressed();
    }

    public String getSelectedContext() {
        if (selectedContext == null) {
            if (!canCancel) {
                return this.defalutContext;
            }
        } else {
            if (selectedContext.endsWith(DEFAULT_FLAG)) {
                return this.defalutContext;
            } else {
                return selectedContext;
            }
        }
        return null;
    }
}
