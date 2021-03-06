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
package org.talend.designer.core.runprocess;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.talend.commons.exception.SystemException;
import org.talend.commons.ui.runtime.exception.ExceptionHandler;
import org.talend.commons.ui.runtime.exception.MessageBoxExceptionHandler;
import org.talend.core.CorePlugin;
import org.talend.core.language.ECodeLanguage;
import org.talend.core.language.LanguageManager;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.process.ITargetExecutionConfig;
import org.talend.designer.codegen.ICodeGenerator;
import org.talend.designer.core.ISyntaxCheckableEditor;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.runprocess.IEclipseProcessor;
import org.talend.designer.runprocess.IProcessMessageManager;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorException;
import org.talend.designer.runprocess.ProcessorUtilities;
import org.talend.repository.ui.wizards.exportjob.scriptsmanager.JobScriptsManager;

/**
 * DOC nrousseau class global comment. Detailled comment <br/>
 * 
 * $Id: Processor.java 52559 2010-12-13 04:14:06Z nrousseau $
 * 
 * 
 */
public abstract class Processor implements IProcessor, IEclipseProcessor {

    private static Logger log = Logger.getLogger(Processor.class);

    public static final String CTX_ARG = "--context="; //$NON-NLS-1$

    private static final String STAT_PORT_ARG = "--stat_port="; //$NON-NLS-1$

    private static final String TRACE_PORT_ARG = "--trace_port="; //$NON-NLS-1$

    private static boolean externalUse = false;

    protected IContext context;

    private ITargetExecutionConfig targetExecutionConfig;

    private String libraryPath;

    private String interpreter;

    private String codeLocation;

    protected IProcess process;

    protected IProject project;

    /** Path to generated context code. */
    protected IPath contextPath;

    /** Path to generated perl code. */
    protected IPath codePath;

    protected String targetPlatform;

    private boolean codeGenerated; // will say if the code has been generated at

    private String[] proxyParameters;

    // least once

    /**
     * Construct a new Processor.
     * 
     * @param process
     * 
     * @param process Process to be run.
     */
    public Processor(IProcess process) {
        super();
        if (ProcessorUtilities.isExportConfig()) {
            setInterpreter(ProcessorUtilities.getInterpreter());
            setLibraryPath(ProcessorUtilities.getLibraryPath());
            setCodeLocation(ProcessorUtilities.getCodeLocation());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#run(int, int, java.lang.String)
     */
    public Process run(int statisticsPort, int tracePort, String watchParam) throws ProcessorException {
        return run(statisticsPort, tracePort, watchParam, null, null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#run(int, int, java.lang.String,
     * org.eclipse.core.runtime.IProgressMonitor, org.talend.designer.runprocess.IProcessMessageManager)
     */
    public Process run(int statisticsPort, int tracePort, String watchParam, IProgressMonitor monitor,
            IProcessMessageManager processMessageManager) throws ProcessorException {
        if (context == null) {
            throw new IllegalStateException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }

        setProcessorStates(STATES_RUNTIME);

        if (!codeGenerated) {
            codeGenerated = ProcessorUtilities.generateCode(process, context, statisticsPort != NO_STATISTICS,
                    tracePort != NO_TRACES, true) != null;

            // if the code can't be generated by the ProcessorUtilities, then it
            // will be generated by this way
            // this will be used for example for the shadow process.
            if (!codeGenerated) {
                generateCode(statisticsPort != NO_STATISTICS, tracePort != NO_TRACES, true);
                if (LanguageManager.getCurrentLanguage() == ECodeLanguage.JAVA) {
                    try {
                        CorePlugin.getDefault().getRunProcessService().getJavaProject().getProject()
                                .build(IncrementalProjectBuilder.AUTO_BUILD, null);
                    } catch (CoreException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        }
        if (watchParam == null) {
            // only works with context name and remove context interpereter
            // option
            return exec(Level.INFO, statisticsPort, tracePort);
        }
        return exec(Level.INFO, statisticsPort, tracePort, watchParam);
    }

    /**
     * Debug the process using a given context.
     * 
     * @param context Context to be used.
     * @return The configuration to be launched in debug mode.
     * @throws ProcessorException Process failed.
     * @throws CoreException
     * @throws ProcessorException
     */
    public ILaunchConfiguration debug() throws ProcessorException {
        if (context == null) {
            throw new IllegalArgumentException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }
        ILaunchConfiguration config = null;
        try {
            setProcessorStates(STATES_EDIT);
            config = (ILaunchConfiguration) saveLaunchConfiguration();
        } catch (CoreException ce) {
            throw new ProcessorException(ce);
        }
        return config;
    }

    /**
     * 
     * DOC xzhang Comment method "getDebugConfiguration". For the bug 5430
     * 
     * @param statOption
     * @param traceOption
     * @param codeOptions
     * @return
     * @throws ProcessorException
     */
    public ILaunchConfiguration getDebugConfiguration(int statOption, int traceOption, String... codeOptions)
            throws ProcessorException {
        if (context == null) {
            throw new IllegalArgumentException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }

        StringBuilder parameterStr = new StringBuilder(" "); //$NON-NLS-1$
        if (codeOptions != null) {
            for (int i = 0; i < codeOptions.length; i++) {
                String string = codeOptions[i];
                if (string != null) {
                    parameterStr.append(string).append(" "); //$NON-NLS-1$
                }
            }
        }
        if (statOption != -1) {
            parameterStr = parameterStr.append(STAT_PORT_ARG + statOption).append(" "); //$NON-NLS-1$
        }
        if (traceOption != -1) {
            parameterStr = parameterStr.append(TRACE_PORT_ARG + traceOption).append(" "); //$NON-NLS-1$
        }

        ILaunchConfiguration config = null;
        try {
            setProcessorStates(STATES_EDIT);
            config = (ILaunchConfiguration) saveLaunchConfigurationWithParam(parameterStr.toString());
        } catch (CoreException ce) {
            throw new ProcessorException(ce);
        }
        return config;
    }

    /**
     * Get the executable commandLine.
     * 
     * @param contextName
     * @param statOption
     * @param traceOption
     * @param codeOptions
     * @return
     */
    public String[] getCommandLine(boolean needContext, boolean externalUse, int statOption, int traceOption,
            String... codeOptions) {
        setExternalUse(externalUse);
        String[] cmd = null;
        try {
            cmd = getCommandLine();

        } catch (ProcessorException e) {
            ExceptionHandler.process(e);
        }
        cmd = addCommmandLineAttch(needContext, cmd, context.getName(), statOption, traceOption, codeOptions);

        // (feature 4258)
        if (Platform.OS_LINUX.equals(getTargetPlatform())) {
            // original is $*
            cmd = (String[]) ArrayUtils.add(cmd, JobScriptsManager.CMDFORUNIX); //$NON-NLS-1$
        } else if (Platform.OS_WIN32.equals(getTargetPlatform())) {
            cmd = (String[]) ArrayUtils.add(cmd, JobScriptsManager.CMDFORWIN); //$NON-NLS-1$
        }
        return cmd;
    }

    /**
     * Add the attchment condition to commmandline .
     * 
     * @param commandLine
     * @param contextName
     * @param statOption
     * @param traceOption
     * @param codeOptions
     * @return
     */
    protected static String[] addCommmandLineAttch(boolean needContext, String[] commandLine, String contextName, int statOption,
            int traceOption, String... codeOptions) {
        String[] cmd = commandLine;
        if (codeOptions != null) {
            for (int i = 0; i < codeOptions.length; i++) {
                String string = codeOptions[i];
                if (string != null) {
                    cmd = (String[]) ArrayUtils.add(cmd, string);
                }
            }
        }
        if (needContext && contextName != null) {
            cmd = (String[]) ArrayUtils.add(cmd, CTX_ARG + contextName);
        }
        if (statOption != -1) {
            cmd = (String[]) ArrayUtils.add(cmd, STAT_PORT_ARG + statOption);
        }
        if (traceOption != -1) {
            cmd = (String[]) ArrayUtils.add(cmd, TRACE_PORT_ARG + traceOption);
        }
        return cmd;
    }

    /**
     * Code Execution, used, when you know where the code stands.
     * 
     * @param Perl Absolute Code Path
     * @param Context Name
     * @param Port Statistics
     * @param Port Trace
     * @return Command Process Launched
     * @throws ProcessorException
     */
    private Process exec(Level level, int statOption, int traceOption, String... codeOptions) throws ProcessorException {

        String[] cmd = getCommandLine(true, false, statOption, traceOption, codeOptions);

        logCommandLine(cmd, level);
        try {
            return Runtime.getRuntime().exec(cmd);
        } catch (IOException ioe) {
            throw new ProcessorException(Messages.getString("Processor.execFailed"), ioe); //$NON-NLS-1$
        }
    }

    public static Thread createProdConsThread(final InputStream input, final boolean isError, final int bufferSize,
            final StringBuffer out, final StringBuffer err) {
        Thread thread = new Thread() {

            public void run() {
                try {
                    BufferedInputStream outStreamProcess = new BufferedInputStream(input);
                    byte[] buffer = new byte[bufferSize];
                    int count = 0;
                    while ((count = outStreamProcess.read(buffer, 0, buffer.length)) != -1) {
                        if (isError) {
                            err.append(new String(buffer, 0, count));
                        } else {
                            out.append(new String(buffer, 0, count));
                        }
                    }
                    outStreamProcess.close();
                } catch (IOException ioe) {
                    ExceptionHandler.process(ioe);
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        ExceptionHandler.process(e);
                    }
                }
            }
        };
        return thread;
    }

    public static void logCommandLine(String[] cmd, Level level) {
        StringBuffer sb = new StringBuffer();
        sb.append(Messages.getString("Processor.commandLineLog")); //$NON-NLS-1$
        for (String s : cmd) {
            sb.append(' ').append(s);
        }
        log.log(level, sb.toString());
    }

    /**
     * Sets the externalUse.
     * 
     * @param externalUse the externalUse to set
     */
    public static void setExternalUse(boolean externalUse) {
        Processor.externalUse = externalUse;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getInterpreter()
     */
    public String getInterpreter() throws ProcessorException {
        return interpreter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setInterpreter(java.lang.String )
     */
    public void setInterpreter(String interpreter) {
        this.interpreter = interpreter;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setLibraryPath(java.lang.String )
     */
    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodeLocation()
     */
    public String getCodeLocation() throws ProcessorException {
        return codeLocation;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setCodeLocation(java.lang.String )
     */
    public void setCodeLocation(String codeLocation) {
        this.codeLocation = codeLocation;
    }

    public abstract void setSyntaxCheckableEditor(ISyntaxCheckableEditor editor);

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#generateCode(org.talend.core .model.process.IContext, boolean,
     * boolean, boolean)
     */
    public void generateCode(boolean statistics, boolean trace, boolean properties) throws ProcessorException {
        if (context == null) {
            throw new IllegalStateException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }

        // Remove the synchronization of the routines when generate the code.
        // This shouldn't be needed anymore.

        // try {
        // DesignerPlugin.getDefault().getCodeGeneratorService().
        // createRoutineSynchronizer().syncAllRoutines();
        // } catch (SystemException e) {
        // throw new ProcessorException(e);
        // }

        codeGenerated = true; // set the flag to true to tell the code has been
        // generated at least once.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#generateCode(org.talend.core .model.process.IContext, boolean,
     * boolean, boolean)
     */
    public void generateCode(boolean statistics, boolean trace, boolean javaProperties, boolean exportAsOSGI)
            throws ProcessorException {
        if (context == null) {
            throw new IllegalStateException("Context is empty, context must be set before call"); //$NON-NLS-1$
        }

        // Remove the synchronization of the routines when generate the code.
        // This shouldn't be needed anymore.

        // try {
        // DesignerPlugin.getDefault().getCodeGeneratorService().
        // createRoutineSynchronizer().syncAllRoutines();
        // } catch (SystemException e) {
        // throw new ProcessorException(e);
        // }

        codeGenerated = true; // set the flag to true to tell the code has been
        // generated at least once.
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodeContext()
     */
    public abstract String getCodeContext();

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodePath()
     */
    public abstract IPath getCodePath();

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getCodeProject()
     */
    public abstract IProject getCodeProject();

    public abstract String[] getCommandLine() throws ProcessorException;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getContextPath()
     */
    public abstract IPath getContextPath();

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getLineNumber(java.lang.String)
     */
    public abstract int getLineNumber(String nodeName);

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getProcessorType()
     */
    public abstract String getProcessorType();

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#getTypeName()
     */
    public abstract String getTypeName();

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#initPaths(org.talend.core.model .process.IContext)
     */
    public abstract void initPaths(IContext context) throws ProcessorException;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#saveLaunchConfiguration()
     */
    public abstract Object saveLaunchConfiguration() throws CoreException;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.Processor#saveLaunchConfigurationWithParam ()
     */
    public abstract Object saveLaunchConfigurationWithParam(String parameterStr) throws CoreException;

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setProcessorStates(java.lang .String)
     */
    public abstract void setProcessorStates(int states);

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#setContext(org.talend.core. model.process.IContext)
     */
    public void setContext(IContext context) {
        if (context == null) {
            // usefull to generate a commandline only (from ProcessorUtilities)
            return;
        }
        try {
            initPaths(context);
        } catch (ProcessorException pe) {
            MessageBoxExceptionHandler.process(pe);
        }
        this.context = context;
    }

    protected void updateContextCode(ICodeGenerator codeGen) throws ProcessorException {
        if (codeGen == null) {
            return;
        }
        try {
            String processContext = "false"; //$NON-NLS-1$
            try {
                processContext = codeGen.generateContextCode(context);
            } catch (SystemException e) {
                throw new ProcessorException(Messages.getString("Processor.generationFailed"), e); //$NON-NLS-1$
            }

            // IFile contextFile = javaProject.getFile(contextPath);
            IFile contextFile = this.project.getProject().getFile(contextPath);
            InputStream contextStream = new ByteArrayInputStream(processContext.getBytes());

            if (!contextFile.exists()) {
                // see bug 0003592, detele file with different case in windows
                deleteFileIfExisted(contextFile);

                contextFile.create(contextStream, true, null);
            } else {
                contextFile.setContents(contextStream, true, false, null);
            }

        } catch (CoreException e1) {
            throw new ProcessorException(Messages.getString("Processor.tempFailed"), e1); //$NON-NLS-1$
        }
    }

    public ITargetExecutionConfig getTargetExecutionConfig() {
        return this.targetExecutionConfig;
    }

    public void setTargetExecutionConfig(ITargetExecutionConfig serverConfiguration) {
        this.targetExecutionConfig = serverConfiguration;
    }

    public IProcess getProcess() {
        return this.process;
    }

    public IContext getContext() {
        return this.context;
    }

    public String getTargetPlatform() {
        return targetPlatform;
    }

    public void setTargetPlatform(String targetPlatform) {
        this.targetPlatform = targetPlatform;
    }

    /**
     * DOC bqian Comment method "replaceSnippet".
     * 
     * @param processCode
     */
    public String replaceSnippet(String processCode) {
        SnippetParser sp = new SnippetParser();
        return sp.convertSnippet(processCode);
    }

    public boolean checkKillAllowed() {
        return true;
    }

    /**
     * Delete file from the file system if there is another file with different case (lowercase or uppercase) which may
     * cause problem in windows system. See bug 0003592 for more detail.
     * 
     * @param codeFile The file that contains source codes that are generated by tos.
     * @throws CoreException
     */
    protected void deleteFileIfExisted(IFile codeFile) throws CoreException {
        File systemFile = codeFile.getLocation().toFile();
        if (systemFile.exists()) {
            systemFile.delete();
            codeFile.getParent().refreshLocal(IResource.DEPTH_INFINITE, null);
        }
    }

    /**
     * Check if the code has been generated at least once. Will be false if the code has never been generated.
     * 
     * @return boolean to tell if any code has been generated already or not for this job.
     */
    public boolean isCodeGenerated() {
        return this.codeGenerated;
    }

    /**
     * Add the possibility to force the flag for the code generated. <br>
     * This can be usefull to force to generate the code.
     * 
     * @param codeGenerated boolean to tell if any code has been generated already or not for this job.
     */
    public void setCodeGenerated(boolean codeGenerated) {
        this.codeGenerated = codeGenerated;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.designer.runprocess.IProcessor#generateContextCode()
     */
    abstract public void generateContextCode() throws ProcessorException;

    public String[] getProxyParameters() {
        return this.proxyParameters;
    }

    public void setProxyParameters(String[] proxyParameters) {
        if (proxyParameters != null) {
            this.proxyParameters = proxyParameters;
        }

    }

}
