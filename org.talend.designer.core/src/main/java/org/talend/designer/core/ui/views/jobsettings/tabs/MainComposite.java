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
package org.talend.designer.core.ui.views.jobsettings.tabs;

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetWidgetFactory;
import org.talend.core.model.properties.User;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.repository.i18n.Messages;

/**
 * yzhang class global comment. Detailled comment
 */
public class MainComposite extends AbstractTabComposite {

    private boolean enableControl;

    /**
     * yzhang MainComposite constructor comment.
     * 
     * @param parent
     * @param style
     */
    public MainComposite(Composite parent, int style, TabbedPropertySheetWidgetFactory factory, IRepositoryViewObject obj) {
        super(parent, style, factory, obj);

        FormLayout layout = new FormLayout();
        setLayout(layout);

        FormData thisFormData = new FormData();
        thisFormData.left = new FormAttachment(0, 0);
        thisFormData.right = new FormAttachment(100, 0);
        thisFormData.top = new FormAttachment(0, 0);
        thisFormData.bottom = new FormAttachment(100, 0);
        setLayoutData(thisFormData);

        Composite composite = widgetFactory.createFlatFormComposite(this);

        FormData compositeData = new FormData();
        compositeData.left = new FormAttachment(0, 0);
        compositeData.right = new FormAttachment(100, 0);
        compositeData.top = new FormAttachment(0, 0);
        compositeData.bottom = new FormAttachment(100, 0);
        composite.setLayoutData(thisFormData);

        Text nameText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        FormData data = new FormData();
        data.left = new FormAttachment(0, AbstractPropertySection.STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(50, 0);
        data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
        nameText.setLayoutData(data);
        String nameContent = repositoryObject.getLabel();
        nameText.setText(nameContent != null ? nameContent : ""); //$NON-NLS-1$
        nameText.setEnabled(enableControl);

        CLabel nameLabel = widgetFactory.createCLabel(composite, Messages.getString("NameSection.Name")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(nameText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(nameText, 0, SWT.CENTER);
        nameLabel.setLayoutData(data);

        Text authorText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        authorText.setEnabled(false);
        data = new FormData();
        data.left = new FormAttachment(0, AbstractPropertySection.STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(70, 0);
        data.top = new FormAttachment(nameLabel, ITabbedPropertyConstants.VSPACE);
        authorText.setLayoutData(data);
        User user = repositoryObject.getAuthor();
        if (user != null) {
            String author = user.getLogin();
            authorText.setText(author != null ? author : ""); //$NON-NLS-1$
        } else {
            authorText.setText(nameContent);
        }
        authorText.setEnabled(enableControl);

        CLabel authorLabel = widgetFactory.createCLabel(composite, Messages.getString("VersionAuthorSection.authorLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(authorText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(authorText, 0, SWT.CENTER);
        authorLabel.setLayoutData(data);

        Button btnDown = widgetFactory.createButton(composite, "m", SWT.PUSH); //$NON-NLS-1$
        data = new FormData();
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(authorText, 0, SWT.CENTER);
        btnDown.setLayoutData(data);
        btnDown.setEnabled(enableControl);

        Button btnUp = widgetFactory.createButton(composite, "M", SWT.PUSH); //$NON-NLS-1$
        data = new FormData();
        data.right = new FormAttachment(btnDown, 0);
        data.top = new FormAttachment(authorText, 0, SWT.CENTER);
        btnUp.setLayoutData(data);
        btnUp.setEnabled(enableControl);

        Text versionText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        versionText.setEnabled(false);
        data = new FormData();
        data.left = new FormAttachment(authorText, AbstractPropertySection.STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(btnUp, -2);
        data.top = new FormAttachment(authorText, 0, SWT.CENTER);
        versionText.setLayoutData(data);
        String version = repositoryObject.getVersion();
        versionText.setText(version != null ? version : ""); //$NON-NLS-1$
        versionText.setEnabled(enableControl);

        CLabel versionLabel = widgetFactory.createCLabel(composite, Messages.getString("VersionAuthorSection.versionLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(authorText, ITabbedPropertyConstants.HSPACE * 3);
        data.right = new FormAttachment(versionText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(versionText, 0, SWT.CENTER);
        versionLabel.setLayoutData(data);

        Text purposeText = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, AbstractPropertySection.STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(70, 0);
        data.top = new FormAttachment(authorLabel, ITabbedPropertyConstants.VSPACE);
        purposeText.setLayoutData(data);
        String content = repositoryObject.getPurpose();
        purposeText.setText(content != null ? content : ""); //$NON-NLS-1$
        purposeText.setEnabled(enableControl);

        CLabel purposeLabel = widgetFactory.createCLabel(composite, Messages.getString("PurposeStatusSection.purposeLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(purposeText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(purposeText, 0, SWT.CENTER);
        purposeLabel.setLayoutData(data);

        CCombo statusText = widgetFactory.createCCombo(composite, SWT.READ_ONLY | SWT.BORDER); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(purposeText, AbstractPropertySection.STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(authorLabel, ITabbedPropertyConstants.VSPACE);
        statusText.setLayoutData(data);
        String status = repositoryObject.getStatusCode();
        statusText.setText(status != null ? status : ""); //$NON-NLS-1$
        statusText.setEnabled(enableControl);

        CLabel statusLabel = widgetFactory.createCLabel(composite, Messages.getString("PurposeStatusSection.statusLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(purposeText, ITabbedPropertyConstants.HSPACE * 3);
        data.right = new FormAttachment(statusText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(statusText, 0, SWT.CENTER);
        statusLabel.setLayoutData(data);

        Text descriptionText = widgetFactory.createText(composite, "", SWT.MULTI); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, AbstractPropertySection.STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(100, 0);
        data.top = new FormAttachment(statusText, ITabbedPropertyConstants.VSPACE);
        data.height = NB_LINES * descriptionText.getLineHeight();
        descriptionText.setLayoutData(data);

        String description = repositoryObject.getDescription();
        descriptionText.setText(description != null ? description : ""); //$NON-NLS-1$
        descriptionText.setEnabled(enableControl);

        CLabel descriptionLabel = widgetFactory.createCLabel(composite, Messages.getString("DescriptionSection.Label")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(descriptionText, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(descriptionText, 0, SWT.TOP);
        descriptionLabel.setLayoutData(data);

        Text creationDate = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, AbstractPropertySection.STANDARD_LABEL_WIDTH);
        data.right = new FormAttachment(33, 0);
        data.top = new FormAttachment(descriptionText, ITabbedPropertyConstants.VSPACE);
        creationDate.setLayoutData(data);
        creationDate.setEnabled(false);
        Date cDate = repositoryObject.getCreationDate();
        creationDate.setText(cDate != null ? FORMATTER.format(cDate) : ""); //$NON-NLS-1$
        creationDate.setEnabled(enableControl);

        CLabel creationLabel = widgetFactory.createCLabel(composite, Messages.getString("DateSection.creationLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(0, 0);
        data.right = new FormAttachment(creationDate, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(creationDate, 0, SWT.CENTER);
        creationLabel.setLayoutData(data);

        Text modificationDate = widgetFactory.createText(composite, ""); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(creationDate, AbstractPropertySection.STANDARD_LABEL_WIDTH + 15);
        data.right = new FormAttachment(66, 0);
        data.top = new FormAttachment(descriptionText, ITabbedPropertyConstants.VSPACE);
        modificationDate.setLayoutData(data);
        modificationDate.setEnabled(false);
        Date mDate = repositoryObject.getModificationDate();
        modificationDate.setText(mDate != null ? FORMATTER.format(mDate) : ""); //$NON-NLS-1$
        modificationDate.setEnabled(enableControl);

        CLabel modificationLabel = widgetFactory.createCLabel(composite, Messages.getString("DateSection.ModificationLabel")); //$NON-NLS-1$
        data = new FormData();
        data.left = new FormAttachment(creationDate, ITabbedPropertyConstants.HSPACE * 3);
        data.right = new FormAttachment(modificationDate, -ITabbedPropertyConstants.HSPACE);
        data.top = new FormAttachment(modificationDate, 0, SWT.CENTER);
        modificationLabel.setLayoutData(data);

    }
}
