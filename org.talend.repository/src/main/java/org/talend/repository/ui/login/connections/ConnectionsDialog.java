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
package org.talend.repository.ui.login.connections;

import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.talend.commons.ui.runtime.image.ImageProvider;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.model.general.ConnectionBean;
import org.talend.core.ui.branding.IBrandingService;
import org.talend.repository.i18n.Messages;

/**
 * DOC smallet class global comment. Detailled comment <br/>
 * 
 * $Id: talend.epf 1 2006-09-29 17:06:40 +0000 (ven., 29 sept. 2006) nrousseau $
 * 
 */
public class ConnectionsDialog extends TitleAreaDialog {

    private static final int FORM_COMPOSITE_HEIGHT = 350;

    private static final int FORM_COMPOSITE_WIDTH = 420;

    private static final int LIST_COMPOSITE_WIDTH = 220;

    private ConnectionsListComposite listComposite;

    private ConnectionFormComposite formComposite;

    public static final int STANDARD_LABEL_WIDTH = 102;

    public static final int STANDARD_TEXT_WIDTH = 260;

    public static final int VSPACE = 5;

    public static final int HSPACE = 5;

    public ConnectionsDialog(Shell parentShell) {
        super(parentShell);

        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        ImageDescriptor imgDesc = brandingService.getLoginHImage();
        if (imgDesc != null) {
            setTitleImage(ImageProvider.getImage(imgDesc));
        }
        // RGB rgb = parentShell.getBackground().getRGB();
        // setTitleAreaColor(rgb);
    }

    @Override
    protected void configureShell(final Shell newShell) {
        super.configureShell(newShell);
        IBrandingService brandingService = (IBrandingService) GlobalServiceRegister.getDefault().getService(
                IBrandingService.class);
        newShell.setText(Messages.getString("LoginDialog.title", brandingService.getFullProductName())); //$NON-NLS-1$
    }

    @Override
    protected Control createDialogArea(final Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);

        Composite container = new Composite(composite, SWT.NONE);

        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        container.setLayout(layout);

        listComposite = new ConnectionsListComposite(container, SWT.NONE);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = LIST_COMPOSITE_WIDTH;
        listComposite.setLayoutData(data);

        formComposite = new ConnectionFormComposite(container, SWT.NONE, listComposite, this);
        data = new GridData(GridData.FILL_BOTH);
        data.widthHint = FORM_COMPOSITE_WIDTH;
        data.heightHint = FORM_COMPOSITE_HEIGHT;
        formComposite.setLayoutData(data);
        listComposite.setConnectionsFormComposite(formComposite);

        Label titleBarSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        titleBarSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return composite;
    }

    public List<ConnectionBean> getConnections() {
        return listComposite.getList();
    }

}
