package org.talend.repository.plsap.extractor.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.talend.repository.plsap.extractor.wizard.NewTableExtractorWizard;

public class NewTableExtractorCommandHandler implements IHandler {

	@Override
	public void addHandlerListener(IHandlerListener handlerListener) {

	}


	@Override
	public void dispose() {

	}


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench workbench = PlatformUI.getWorkbench();

		NewTableExtractorWizard wizard = new NewTableExtractorWizard();
		wizard.init(workbench, null);

		WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
		dialog.create();
		dialog.open();
		return null;
	}


	@Override
	public boolean isEnabled() {
		return true;
	}


	@Override
	public boolean isHandled() {
		return true;
	}


	@Override
	public void removeHandlerListener(IHandlerListener handlerListener) {

	}

}
