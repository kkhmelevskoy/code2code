package code2code.ui.wizards.generate;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import code2code.core.generator.Generator;
import code2code.core.generator.Template;
import code2code.utils.Console;
import code2code.utils.EclipseGuiUtils;
import code2code.utils.FileUtils;

public class GenerateFilesWizard extends Wizard implements INewWizard {

    private IStructuredSelection selection;
    private GeneratorSelectionPage generatorSelectionPage;
    private GeneratorParametersPage generatorParametersPage;
    private IWorkbench workbench;
    private GenerationCustomizationPage generationCustomizationPage;

    @Override
    public boolean performFinish() {

	Generator selectedGenerator = generatorSelectionPage
		.getSelectedGenerator();

	if (selectedGenerator == null) {
	    return false;
	}

	try {
	    Console.write("Processing generator: "
		    + selectedGenerator.getName());
	    IProject selectedProject = generatorSelectionPage
		    .getSelectedProject();
	    Map<String, String> userConfiguredParams = selectedGenerator
		    .getUserConfiguredParams();
	    String oldValue = userConfiguredParams.put("project",
		    selectedProject.getLocation().toFile().getAbsolutePath());
	    if (oldValue != null) {
		Console.write("Warning: override parameter project=" + oldValue);
	    }
	    selectedGenerator.setUserConfiguredParams(userConfiguredParams);

	    for (Template template : selectedGenerator
		    .calculateChoosenTemplatesToGenerate()) {

		String destinations = template.calculateDestination();

		if (destinations.equals("")) {
		    Console.write("Generating " + template.getTemplateName()
			    + " to console:");
		    Console.write("-------------------------------------------------------------");
		    Console.write(FileUtils.read(template.calculateResult(selectedProject)));
		    Console.write("-------------------------------------------------------------");
		} else {
		    for (String destination : destinations.split(":")) {
			Path destinationPath = new Path(destination);
			if (selectedProject.exists(destinationPath)) {
			    Console.write("File already exists. Skipping: "
				    + destinationPath);
			    continue;
			}

			IFile file = selectedProject.getFile(destinationPath);

			Console.write("Generating: "
				+ template.getTemplateName() + " to "
				+ file.getLocation().toFile().getAbsolutePath());

			FileUtils.createParentFolders(file);

			file.create(template.calculateResult(selectedProject), false, null);
		    }
		}

	    }

	    Console.write("Done");

	} catch (Exception e) {
	    try {
		Console.write("An error ocurred. See Error Log for details");
	    } catch (Exception e2) {
		EclipseGuiUtils.showErrorDialog(workbench
			.getActiveWorkbenchWindow().getShell(), e2);
		throw new RuntimeException(e2);
	    }
	    EclipseGuiUtils.showErrorDialog(workbench
		    .getActiveWorkbenchWindow().getShell(), e);
	    throw new RuntimeException(e);
	} finally {
	    Console.disposeConsole();
	}

	return true;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection) {
	this.workbench = workbench;
	this.selection = selection;
    }

    @Override
    public void addPages() {

	if (selection.size() == 0) {
	    MessageDialog.openError(workbench.getActiveWorkbenchWindow()
		    .getShell(), "Ops", "Ops... No Project Selected");
	    throw new IllegalStateException(
		    "Ops... a project should be selected");
	}

	IProject project = ((IResource) ((IAdaptable) selection
		.getFirstElement()).getAdapter(IResource.class)).getProject();

	generatorSelectionPage = new GeneratorSelectionPage(project);
	generatorParametersPage = new GeneratorParametersPage(
		generatorSelectionPage);
	generationCustomizationPage = new GenerationCustomizationPage(
		generatorParametersPage);

	addPage(generatorSelectionPage);
	addPage(generatorParametersPage);
	addPage(generationCustomizationPage);

    }

}
