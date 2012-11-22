
package org.talend.repository.plsap.extractor.wizard;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.talend.repository.plsap.extractor.SAPExtractorPlugin;

import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.DestinationDataProvider;

public class NewTableExtractorWizard extends Wizard implements INewWizard {

	private PLSAPConnectionWizardPage connectionWizardPage;
	private PLSAPTableWizardPage tableWizardPage;
	private final String XML_OUTPUT_FOLDER = "XMLOutputFolder";


	public NewTableExtractorWizard() {
		connectionWizardPage = new PLSAPConnectionWizardPage();
		tableWizardPage = new PLSAPTableWizardPage();
	}


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		connectionWizardPage.host = SAPExtractorPlugin.getDefault().getPreferenceStore().getString(DestinationDataProvider.JCO_ASHOST);
		connectionWizardPage.systemNumber = SAPExtractorPlugin.getDefault().getPreferenceStore().getString(DestinationDataProvider.JCO_SYSNR);
		connectionWizardPage.client = SAPExtractorPlugin.getDefault().getPreferenceStore().getString(DestinationDataProvider.JCO_CLIENT);
		connectionWizardPage.language = SAPExtractorPlugin.getDefault().getPreferenceStore().getString(DestinationDataProvider.JCO_LANG);
		connectionWizardPage.user = SAPExtractorPlugin.getDefault().getPreferenceStore().getString(DestinationDataProvider.JCO_USER);
		connectionWizardPage.password = SAPExtractorPlugin.getDefault().getPreferenceStore().getString(DestinationDataProvider.JCO_PASSWD);

		tableWizardPage.outputFolder = SAPExtractorPlugin.getDefault().getPreferenceStore().getString(XML_OUTPUT_FOLDER);
	}


	@Override
	public boolean performFinish() {
		FileWriter fileWriter;
		PrintWriter printWriter = null;
		boolean isAccepted = false;
		try {
			File outputFile = new File(tableWizardPage.outputFolder + File.separator + tableWizardPage.tableName + ".xml");
			if (!outputFile.exists()) {
				JCoDestination destination = SAPExtractorPlugin.getDefault().getJCoDestination(connectionWizardPage.client, connectionWizardPage.host, connectionWizardPage.user, connectionWizardPage.password, connectionWizardPage.systemNumber, connectionWizardPage.language, getShell());
				if (SAPExtractorPlugin.getDefault().isSAPConnected()) {
					fileWriter = new FileWriter(outputFile);
					printWriter = new PrintWriter(fileWriter);
					SAPSchemaExtractorPMD sapProgressMonitorDialog = new SAPSchemaExtractorPMD(NewTableExtractorWizard.this.getShell(), outputFile);
					RFCJob rfcJob = new RFCJob(tableWizardPage.tableName, printWriter, destination);
					sapProgressMonitorDialog.run(true, true, rfcJob);
					isAccepted = true;

					SAPExtractorPlugin.getDefault().getPreferenceStore().setValue(DestinationDataProvider.JCO_ASHOST, connectionWizardPage.host);
					SAPExtractorPlugin.getDefault().getPreferenceStore().setValue(DestinationDataProvider.JCO_SYSNR, connectionWizardPage.systemNumber);
					SAPExtractorPlugin.getDefault().getPreferenceStore().setValue(DestinationDataProvider.JCO_CLIENT, connectionWizardPage.client);
					SAPExtractorPlugin.getDefault().getPreferenceStore().setValue(DestinationDataProvider.JCO_LANG, connectionWizardPage.language);
					SAPExtractorPlugin.getDefault().getPreferenceStore().setValue(DestinationDataProvider.JCO_USER, connectionWizardPage.user);
					SAPExtractorPlugin.getDefault().getPreferenceStore().setValue(DestinationDataProvider.JCO_PASSWD, connectionWizardPage.password);

					SAPExtractorPlugin.getDefault().getPreferenceStore().setValue(XML_OUTPUT_FOLDER, tableWizardPage.outputFolder);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return isAccepted;
	}


	@Override
	public void addPages() {
		setWindowTitle("SAP Table Extraction");//$NON-NLS-1$
		setDefaultPageImageDescriptor(SAPExtractorPlugin.getBundledImageDescriptor("/icons/connection_wiz.png"));

		connectionWizardPage.setTitle("New SAP Table Extraction - Step 1/2"); //$NON-NLS-1$
		connectionWizardPage.setDescription("Define the connection parameters"); //$NON-NLS-1$
		connectionWizardPage.setPageComplete(false);

		tableWizardPage.setTitle("New SAP Table Extraction - Step 2/2"); //$NON-NLS-1$
		tableWizardPage.setDescription("Define the Table Name & Output Folder"); //$NON-NLS-1$
		tableWizardPage.setPageComplete(false);

		addPage(connectionWizardPage);
		addPage(tableWizardPage);
	}

	class RFCJob implements IRunnableWithProgress {
		String table;
		PrintWriter printWriter;
		JCoDestination destination;


		public RFCJob(String table, PrintWriter printWriter, JCoDestination destination) {
			this.table = table;
			this.printWriter = printWriter;
			this.destination = destination;
		}


		@Override
		public void run(final IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			HashMap<String, String> oracleTypesFields = new HashMap<String, String>();
			oracleTypesFields.put("P", "id_Double");
			oracleTypesFields.put("C", "id_String");
			oracleTypesFields.put("D", "id_Date");
			oracleTypesFields.put("F", "id_Double");
			oracleTypesFields.put("I", "id_Integer");
			oracleTypesFields.put("b", "id_Double");
			oracleTypesFields.put("s", "id_Double");
			oracleTypesFields.put("N", "id_String");
			oracleTypesFields.put("S", "id_String");
			oracleTypesFields.put("T", "id_String");
			oracleTypesFields.put("X", "id_String");

			HashMap<String, String> keyFlag = new HashMap<String, String>();
			keyFlag.put(" ", "false");
			keyFlag.put("X", "true");
			keyFlag.put("", "false");

			JCoFunction function;
			try {
				monitor.beginTask("Extracting SAP Table Schema...", IProgressMonitor.UNKNOWN);
				function = destination.getRepository().getFunction("DDIF_FIELDINFO_GET");

				if (function == null) {
					throw new RuntimeException("DDIF_FIELDINFO_GET not found in SAP.");
				}
				function.getImportParameterList().setValue("TABNAME", table); // table name
				function.getImportParameterList().setValue("LANGU", "EN"); // the lanugage, what defined in the connection
				function.execute(destination);

				JCoTable data = function.getTableParameterList().getTable("DFIES_TAB");

				printWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?><schema>");

				data.firstRow();
				for (int i = 0; i < data.getNumRows(); i++, data.nextRow()) {
					monitor.subTask("processing SAP Table Columns ");

					printWriter.println(" <column comment=\"" + data.getString("FIELDTEXT").replace("\"", "") + "\" default=\"\" key=\"" + keyFlag.get(data.getString("KEYFLAG")) + "\" label=\"" + data.getString("FIELDNAME") + "\"");
					printWriter.println("         length=\"" + Integer.toString(Integer.parseInt(data.getString("LENG"))) + "\" nullable=\"true\" originalDbColumnName=\"" + data.getString("FIELDNAME") + "\"");
					printWriter.print("         pattern=\"");
					if (data.getString("INTTYPE").charAt(0) == 'D') {
						printWriter.print("&quot;yyyyMMdd&quot;");
					}
					printWriter.println("\" precision=\"-1\" talendType=\"" + oracleTypesFields.get(data.getString("INTTYPE")) + "\" type=\"\"/>");
				}
				printWriter.println("</schema>");

			} catch (JCoException e) {
				e.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			} finally {
				printWriter.close();
			}
		}
	}

	class SAPSchemaExtractorPMD extends ProgressMonitorDialog {
		File outputFile;


		public SAPSchemaExtractorPMD(Shell parent, File file) {
			super(parent);
			outputFile = file;
		}


		@Override
		protected void cancelPressed() {
			super.cancelPressed();
			outputFile.delete();
			getProgressMonitor().subTask("SAP Table Extraction, Cancel in progress...");
		}
	}

}
