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
package org.talend.core.service;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.talend.core.IService;
import org.talend.core.model.process.INode;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface ICorePerlService extends IService {

    public void setExecutablePreference(String perlInterpreter);

    public ISourceViewer createViewer(Composite composite, int styles, boolean checkCode);

    public ISourceViewer createViewer(Composite composite, int styles, boolean checkCode, final INode node);

}
