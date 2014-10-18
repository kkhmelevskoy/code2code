package code2code.ui.wizards.generate;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import code2code.core.generator.Template;
import code2code.utils.EclipseGuiUtils;
import code2code.utils.FileUtils;


public class PreviewDialog extends TrayDialog {
	
	private StyledText previewText;
	private final Template result;
	private final IProject selectedProject;

	
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		
		EclipseGuiUtils.scaleShellToClientArea(newShell, 0.7);
	}
	
	public PreviewDialog(Shell shell, Template result, IProject selectedProject) {
		super(shell);
		
		this.result = result;
		this.selectedProject = selectedProject;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		
		Composite dialogArea= (Composite) super.createDialogArea(parent);
		
		FillLayout fillLayout = new FillLayout();
		dialogArea.setLayout(fillLayout);
		
		previewText = new StyledText(dialogArea, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		
		try {
			previewText.setText(FileUtils.read(result.calculateResult(selectedProject)));
		} catch (Exception e) {
			EclipseGuiUtils.showErrorDialog(getShell(), e);
			throw new RuntimeException(e);
		}
		
		return dialogArea;
	}
	
	
	@Override
	protected void okPressed() {
		
		result.setUserChoosenResult(previewText.getText());
		
		super.okPressed();
	}
	

	
	
}