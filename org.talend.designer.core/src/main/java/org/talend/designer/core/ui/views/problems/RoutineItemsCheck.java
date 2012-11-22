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
package org.talend.designer.core.ui.views.problems;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Display;
import org.talend.commons.exception.PersistenceException;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.GlobalServiceRegister;
import org.talend.core.context.Context;
import org.talend.core.context.RepositoryContext;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.model.general.Project;
import org.talend.core.model.properties.Property;
import org.talend.core.model.properties.RoutineItem;
import org.talend.core.model.repository.ERepositoryObjectType;
import org.talend.core.model.repository.IRepositoryViewObject;
import org.talend.core.service.IDesignerPerlService;
import org.talend.designer.codegen.ICodeGeneratorService;
import org.talend.designer.codegen.ITalendSynchronizer;
import org.talend.repository.model.IProxyRepositoryFactory;

/**
 * DOC xhuang class global comment. Detailled comment
 */
public class RoutineItemsCheck {

    private ITalendSynchronizer routineSynchronizer;

    private ECodeLanguage language;

    /*
     * add all routine items compilation errors into problmes views
     */
    public void addAllRoutineProblem() {
        IProxyRepositoryFactory factory = CorePlugin.getDefault().getProxyRepositoryFactory();
        RepositoryContext repositoryContext = (RepositoryContext) CorePlugin.getContext().getProperty(
                Context.REPOSITORY_CONTEXT_KEY);
        if (repositoryContext == null) {
            return;
        }
        Project project = repositoryContext.getProject();
        if (project == null) {
            return;
        }
        language = project.getLanguage();
        if (language == null) {
            return;
        }
        if (language == ECodeLanguage.PERL) {
            try {
                ICodeGeneratorService service = (ICodeGeneratorService) GlobalServiceRegister.getDefault().getService(
                        ICodeGeneratorService.class);
                routineSynchronizer = service.createPerlRoutineSynchronizer();

                List<IRepositoryViewObject> routineObjList = factory.getAll(ERepositoryObjectType.ROUTINES, false);
                for (IRepositoryViewObject repositoryObj : routineObjList) {
                    RoutineItem item = (RoutineItem) repositoryObj.getProperty().getItem();
                    routineSynchronizer.syncRoutine(item, true);
                    IFile file = routineSynchronizer.getFile(item);
                    addProblems(item, item.getProperty(), file);
                }
            } catch (PersistenceException e) {
                ExceptionHandler.process(e);
            } catch (SystemException e) {
                ExceptionHandler.process(e);
            }
            Display.getDefault().asyncExec(new Runnable() {

                public void run() {
                    Problems.refreshProblemTreeView();
                }
            });
        }

    }

    /**
     * add one routine Item errors into problems view.
     * 
     * @param item
     * @param itemLabel
     * @param file
     */

    private void addProblems(RoutineItem item, Property property, IFile file) {

        try {
            String sourceCode = readSourceFile(file.getLocation().toOSString());
            if (GlobalServiceRegister.getDefault().isServiceRegistered(IDesignerPerlService.class)) {
                IDesignerPerlService service = (IDesignerPerlService) GlobalServiceRegister.getDefault().getService(
                        IDesignerPerlService.class);
                service.perlValidator(file, sourceCode);
            }
        } catch (IOException ex) {
            ExceptionHandler.process(ex);
        } catch (CoreException ex) {
            ExceptionHandler.process(ex);
        }
        Problems.addRoutineFile(file, property);

    }

    /**
     * xhuang Comment method "readSourceFile".
     * 
     * @param path
     * @return
     * @throws IOException
     */
    private String readSourceFile(String path) throws IOException {
        BufferedReader in = null;

        try {
            StringWriter sourceCode = new StringWriter();

            char[] buf = new char[1024];
            in = new BufferedReader(new FileReader(path));

            int read = 0;
            while ((read = in.read(buf)) > 0) {
                sourceCode.write(buf, 0, read);
            }
            return sourceCode.toString();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (IOException e) {
                }
        }
    }
}
