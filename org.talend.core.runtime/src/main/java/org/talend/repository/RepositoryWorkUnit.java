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
package org.talend.repository;

import org.talend.commons.exception.LoginException;
import org.talend.commons.exception.PersistenceException;
import org.talend.core.model.general.Project;

/**
 * @param <T>
 */
public abstract class RepositoryWorkUnit<T> {

    private String name;

    private Project project;

    protected T result;

    private LoginException loginException;

    private PersistenceException persistenceException;

    private Object startingPoint;

    private boolean forceTransaction;

    private boolean avoidUnloadResourcesBeforeRun;

    private boolean unloadResourcesAfterRun;

    public Object getStartingPoint() {
        return startingPoint;
    }

    public T getResult() {
        return result;
    }

    public RepositoryWorkUnit(String name) {
        this.name = name;
    }

    public RepositoryWorkUnit(String name, Object startingPoint) {
        this(name);
        this.startingPoint = startingPoint;
    }

    public RepositoryWorkUnit(Project project, String name) {
        this.project = project;
        this.name = name;
    }

    public Project getProject() {
        return project;
    }

    public String getName() {
        return name;
    }

    public void setLoginException(LoginException loginException) {
        this.loginException = loginException;
    }

    public void setPersistenceException(PersistenceException persistenceException) {
        this.persistenceException = persistenceException;
    }

    protected abstract void run() throws LoginException, PersistenceException;

    public void executeRun() {
        try {
            run();
        } catch (LoginException e) {
            setLoginException(e);
        } catch (PersistenceException e) {
            setPersistenceException(e);
        }
    }

    public void throwLoginExceptionIfAny() throws LoginException {
        if (loginException != null) {
            throw loginException;
        }
    }

    public void throwPersistenceExceptionIfAny() throws PersistenceException {
        if (persistenceException != null) {
            throw persistenceException;
        }
    }

    public boolean isForceTransaction() {
        return this.forceTransaction;
    }

    public void setForceTransaction(boolean forceTransaction) {
        this.forceTransaction = forceTransaction;
    }

    /**
     * Getter for avoidUnloadResources.
     * 
     * @return the avoidUnloadResources
     */
    public boolean isAvoidUnloadResources() {
        return this.avoidUnloadResourcesBeforeRun;
    }

    /**
     * Sets the avoidUnloadResources.
     * 
     * @param avoidUnloadResources the avoidUnloadResources to set
     */
    public void setAvoidUnloadResources(boolean avoidUnloadResources) {
        this.avoidUnloadResourcesBeforeRun = avoidUnloadResources;
    }

    public boolean isUnloadResourcesAfterRun() {
        return unloadResourcesAfterRun;
    }

    public void setUnloadResourcesAfterRun(boolean unloadResourcesAfterRun) {
        this.unloadResourcesAfterRun = unloadResourcesAfterRun;
    }
}
