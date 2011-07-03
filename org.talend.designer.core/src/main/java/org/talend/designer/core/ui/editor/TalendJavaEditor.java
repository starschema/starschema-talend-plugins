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
package org.talend.designer.core.ui.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jdt.internal.ui.javaeditor.JavaSourceViewer;
import org.eclipse.jdt.ui.actions.IJavaEditorActionDefinitionIds;
import org.eclipse.jdt.ui.text.IJavaPartitions;
import org.eclipse.jdt.ui.text.JavaSourceViewerConfiguration;
import org.eclipse.jdt.ui.text.JavaTextTools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.IProcess2;
import org.talend.core.model.properties.Information;
import org.talend.core.model.properties.InformationLevel;
import org.talend.core.model.properties.Item;
import org.talend.core.model.properties.Property;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.designer.core.ICamelDesignerCoreService;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.core.ui.views.problems.Problems;

/**
 * DOC yzhang class global comment. Detailled comment <br/>
 * 
 * $Id: TalendJavaEditor.java 1 2007-1-18 下午03:26:08 +0000 (下午03:26:08, 2007-1-18 2007) yzhang $
 * 
 */
public class TalendJavaEditor extends CompilationUnitEditor implements ISyntaxCheckableEditor {

    private org.eclipse.jdt.core.ICompilationUnit unit;

    private boolean disposed = false;

    private String currentSelection;

    private IProcess2 process;

    /**
     * DOC amaumont TalendJavaEditor constructor comment.
     */
    public TalendJavaEditor(IProcess2 process) {
        super();
        this.process = process;
    }

    /*
     * Over write this method force to add complier to this editor. Beacuse by default if the editor is not editable,
     * the complier for error check will not installed.
     * 
     * @see
     * org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        addCompiler();
    }

    /**
     * Add complier for error check in this Java editor.
     * 
     * DOC yzhang Comment method "addCompiler".
     */
    private void addCompiler() {
        unit = (org.eclipse.jdt.core.ICompilationUnit) getInputJavaElement();
    }

    /*
     * Edit is not allowed.
     * 
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#isEditable()
     */
    @Override
    public boolean isEditable() {
        return false;
    }

    /*
     * Check the syntax for java code.
     * 
     * @see org.talend.designer.core.ui.editor.ISyntaxCheckable#validateSyntax()
     */
    public void validateSyntax() {
        ISourceViewer sourceViewer = getSourceViewer();
        if (sourceViewer instanceof JavaSourceViewer) {
            this.getSourceViewer().getTextWidget().getDisplay().asyncExec(new Runnable() {

                public void run() {
                    // selectAndReveal(0, 0);
                    // JavaSourceViewer javaSourceViewer = (JavaSourceViewer) getSourceViewer();
                    // if (javaSourceViewer != null) {
                    // javaSourceViewer.doOperation(ISourceViewer.FORMAT);
                    // }
                    // doSave(null);
                    placeCursorToSelection();
                    Property property = process.getProperty();

                    ITalendSynchronizer synchronizer = CorePlugin.getDefault().getCodeGeneratorService()
                            .createRoutineSynchronizer();

                    try {
                        // try {
                        // ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
                        // } catch (CoreException e) {
                        // ExceptionHandler.process(e);
                        // }
                        if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                            try {
                                CorePlugin.getDefault().getRunProcessService().getJavaProject().getProject()
                                        .build(IncrementalProjectBuilder.AUTO_BUILD, null);
                            } catch (CoreException e) {
                                ExceptionHandler.process(e);
                            }
                        }
                        Item item = property.getItem();
                        if (GlobalServiceRegister.getDefault().isServiceRegistered(ICamelDesignerCoreService.class)) {
                            ICamelDesignerCoreService service = (ICamelDesignerCoreService) GlobalServiceRegister.getDefault()
                                    .getService(ICamelDesignerCoreService.class);
                            if (service.isInstanceofCamel(item)) {
                                synchronizer = CorePlugin.getDefault().getCodeGeneratorService().createCamelBeanSynchronizer();
                            }
                        }
                        List<Information> informations = Problems.addRoutineFile(synchronizer.getFile(item), property, true);

                        // save error status
                        property.getInformations().clear();
                        // add only the errors in the property, not the warnings
                        for (Information info : informations) {
                            if (info.getLevel().equals(InformationLevel.ERROR_LITERAL)) {
                                property.getInformations().add(info);
                            }
                        }
                        Problems.computePropertyMaxInformationLevel(property);
                    } catch (SystemException e) {
                        ExceptionHandler.process(e);
                    }

                    Problems.refreshRepositoryView();
                    Problems.refreshProblemTreeView();
                }
            });
        }

    }

    /**
     * For job item, we only need to display if it is error.
     * 
     * @param informations
     * @return
     */
    private List<Information> collectOnlyErrors(List<Information> informations) {
        List<Information> errors = new ArrayList<Information>();
        for (Information info : informations) {
            if (InformationLevel.ERROR_LITERAL.equals(info.getLevel())) {
                errors.add(info);
            }
        }
        return errors;
    }

    private void placeCursorToSelection() {
        String mainPart = "[" + currentSelection + " main ] start"; //$NON-NLS-1$ //$NON-NLS-2$
        String assignmentPart = "currentComponent = \"" + currentSelection + "\";"; //$NON-NLS-1$ //$NON-NLS-2$
        if (getDocumentProvider() == null) {
            return;
        }
        IDocument doc = getDocumentProvider().getDocument(getEditorInput());
        FindReplaceDocumentAdapter frda = new FindReplaceDocumentAdapter(doc);
        try {
            Region region = (Region) frda.find(0, mainPart, true, false, false, false);
            if (region != null) {
                Region region2 = (Region) frda.find(region.getOffset(), assignmentPart, true, false, false, false);
                if (region2 != null) {
                    selectAndReveal(region2.getOffset(), assignmentPart.length());
                } else {
                    selectAndReveal(region.getOffset(), mainPart.length());
                }
            } else {
                selectAndReveal(0, 0);
            }
        } catch (BadLocationException e) {
            selectAndReveal(0, 0);
        }
    }

    /*
     * Save as is not allowed.
     * 
     * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor#dispose()
     */
    @Override
    public void dispose() {
        this.disposed = true;
        this.unit = null;
        super.dispose();
    }

    /**
     * 
     * 
     * DOC yzhang Comment method "isDisposed".
     * 
     * @return Whether this editor had been disposed.
     */
    public boolean isDisposed() {
        return this.disposed;
    }

    /**
     * Getter for unit.
     * 
     * @return the unit
     */
    public org.eclipse.jdt.core.ICompilationUnit getUnit() {
        return unit;
    }

    /**
     * DOC nrousseau Comment method "placeCursorTo".
     * 
     * @param string
     */
    public void placeCursorTo(String currentSelection) {
        this.currentSelection = currentSelection;
        placeCursorToSelection();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.internal.ui.javaeditor.JavaEditor#createJavaSourceViewerConfiguration()
     */
    @Override
    protected JavaSourceViewerConfiguration createJavaSourceViewerConfiguration() {
        JavaTextTools textTools = JavaPlugin.getDefault().getJavaTextTools();
        return new JavaSourceViewerConfiguration(textTools.getColorManager(), getPreferenceStore(), this,
                IJavaPartitions.JAVA_PARTITIONING);
    }

    protected void createActions() {
        super.createActions();
        getAction(IJavaEditorActionDefinitionIds.SHOW_IN_BREADCRUMB).setEnabled(false);
    }
}
