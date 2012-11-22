
package org.talend.repository.plsap.extractor;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.talend.repository.plsap.extractor.utils.SapCustomDataProvider;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.ext.Environment;

/**
 * The activator class controls the plug-in life cycle
 */
public class SAPExtractorPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.talend.repository.plsap.extractor"; //$NON-NLS-1$

	// The shared instance
	private static SAPExtractorPlugin plugin;

	private SapCustomDataProvider customDataProvider;
	private JCoDestination jCoDestination;
	private String errorMsg;
	private boolean isSAPConnected = false;


	/**
	 * @param client
	 * @param language
	 * @param sysNumber
	 * @param host
	 * @param userName
	 * @param password
	 * @return
	 * @throws Throwable
	 * 
	 */
	public boolean connectSAPserver(String client, String language, String sysNumber, String host, String userName, String password) throws Throwable {
		registerSAPServerDetails(client, language, sysNumber, host, userName, password);
		try {
			jCoDestination = JCoDestinationManager.getDestination(SapCustomDataProvider.SAP_SERVER);
			isSAPConnected = jCoDestination.getAttributes() != null;
		} catch (Exception exception) {
			isSAPConnected = false;
			throw exception;
		} catch (Throwable throwable) {
			isSAPConnected = false;
			throw throwable;
		}
		return isSAPConnected;
	}


	private void registerSAPServerDetails(String client, String language, String sysNumber, String host, String userName, String password) {
		if (customDataProvider != null) {
			Environment.unregisterDestinationDataProvider(customDataProvider);
		}
		customDataProvider = new SapCustomDataProvider(client, language, sysNumber, host, userName, password);
		Environment.registerDestinationDataProvider(customDataProvider);
	}


	public void checkSAPConnection(final String client, final String language, final String sysNumber, final String host, final String userName, final String password, final Shell shell, final boolean silent) {
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(shell);

		try {
			progressMonitorDialog.run(true, false, new IRunnableWithProgress() {
				public void run(IProgressMonitor progressMonitor) throws InvocationTargetException, InterruptedException {
					try {
						progressMonitor.setTaskName("Connecting to SAP...");
						connectSAPserver(client, language, sysNumber, host, userName, password);
						if (!silent) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openInformation(shell, "Check SAP Connection ", "connection successful.");
								}
							});
						}
					} catch (Throwable throwable) {
						openErrorDialogWithDetail(throwable, shell);
					}
				}

			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}


	private void openErrorDialogWithDetail(Throwable exception, final Shell shell) {
		errorMsg = exception.getMessage();
		if ((exception instanceof Error)) {
			errorMsg = errorMsg + System.getProperty("line.separator") + "Please make sure you have installed native libraries for SAP.";
		}
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				String statusLabelText = "Connection failure." + " " + "You must change the SAP Settings.";
				MessageDialog.openInformation(shell, statusLabelText, errorMsg);
			}
		});
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}


	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SAPExtractorPlugin getDefault() {
		return plugin;
	}


	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}


	/**
	 * Returns string from plug-in's resource bundle
	 * 
	 * @generated
	 */
	public static String getString(String key) {
		return Platform.getResourceString(getDefault().getBundle(), "%" + key); //$NON-NLS-1$
	}


	/**
	 * @generated
	 */
	public void logError(String error) {
		logError(error, null);
	}


	/**
	 * @generated
	 */
	public void logError(String error, Throwable throwable) {
		if (error == null && throwable != null) {
			error = throwable.getMessage();
		}
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, error, throwable));
	}


	/**
	 * @generated
	 */
	public void logInfo(String message) {
		logInfo(message, null);
	}


	/**
	 * @generated
	 */
	public void logInfo(String message, Throwable throwable) {
		if (message == null && throwable != null) {
			message = throwable.getMessage();
		}
		getLog().log(new Status(IStatus.INFO, PLUGIN_ID, IStatus.OK, message, throwable));
	}


	/**
	 * Returns an image for the image file at the given plug-in relative path. Client do not need to dispose this image. Images will be disposed
	 * automatically.
	 * 
	 * @param path
	 *            the path
	 * @return image instance
	 */
	public Image getBundledImage(String path) {
		Image image = getImageRegistry().get(path);
		if (image == null) {
			getImageRegistry().put(path, getBundledImageDescriptor(path));
			image = getImageRegistry().get(path);
		}
		return image;
	}


	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getBundledImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path);
	}


	public JCoDestination getJCoDestination(String client, String host, String user, String password, String sysNumber, String language, Shell shell) throws Throwable {
		if (!isSAPConnected) {
			checkSAPConnection(client, language, sysNumber, host, user, password, shell, true);
		}

		return jCoDestination;
	}


	public boolean isSAPConnected() {
		return isSAPConnected;
	}

}
