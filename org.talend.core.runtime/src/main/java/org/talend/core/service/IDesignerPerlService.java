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
package org.talend.core.service;

import org.apache.log4j.Level;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditor;
import org.talend.core.IService;
import org.talend.core.language.ICodeProblemsChecker;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Property;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;

/**
 * DOC guanglong.du class global comment. Detailled comment
 */
public interface IDesignerPerlService extends IService {

    public String getStandAloneTalendPerlEditorID();

    public AbstractDecoratedTextEditor createNewPerlEditor(IProcess2 process);

    public ISourceViewer getViewer(IEditorPart editor);

    public void perlValidator(IFile file, String sourceCode) throws CoreException;

    public void setSyntaxValidationPreference(boolean isvailda);

    public void placeCursorTo(AbstractDecoratedTextEditor codeEditor, String nodeName);

    public IProcessor createPerlProcessor(IProcess process, Property property, boolean filenameFromLabel);

    public ICodeProblemsChecker createPerlCodeProblemsChecker();

    public IProject getProject() throws CoreException;

    public int perlExec(StringBuffer out, StringBuffer err, IPath absCodePath, String contextName, Level level,
            String perlInterpreterLibOption, String perlModuleDirectoryOption, int statOption, int traceOption,
            String... codeOptions) throws ProcessorException;
}
