// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.expressionbuilder;

import org.eclipse.swt.widgets.Composite;
import org.talend.commons.ui.expressionbuilder.IExpressionBuilderDialogController;
import org.talend.commons.ui.expressionbuilder.IExpressionDataBean;
import org.talend.core.IService;
import org.talend.core.model.process.INode;

/**
 * yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: IExpressionBuilderDialogService.java 下午04:26:06 2007-8-1 +0000 (2007-8-1) yzhang $
 * 
 */
public interface IExpressionBuilderDialogService extends IService {

    public IExpressionBuilderDialogController getExpressionBuilderInstance(Composite parent, IExpressionDataBean dataBean,
            INode component);

}
