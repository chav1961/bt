package chav1961.bt.installer.screens;

import java.util.Map;
import java.util.Properties;

import javax.swing.JPanel;

import chav1961.bt.installer.interfaces.ErrorType;
import chav1961.bt.installer.screens.AbstractWizardStep.WizardStepOption;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class InstallationFailedScreen extends AbstractWizardStep {
	private final WizardStepOption[]	options;
	
	public InstallationFailedScreen(final String stepId, final String caption, final String description, final String helpId, final WizardStepOption... options) {
		super(stepId, caption, description, helpId);
		this.options = options;
	}

	@Override
	public StepType getStepType() {
		return StepType.TERM_FAILURE;
	}

	@Override
	public ScreenType getScreenType() {
		return ScreenType.INSTALLATION_FAILED;
	}

	@Override
	public String getPrevStep() {
		// TODO Auto-generated method stub
		return super.getPrevStep();
	}
	
	@Override
	public JPanel getContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void beforeShow(final Properties content, final Map<String, Object> temporary, final ErrorProcessing<Properties, ErrorType> err) throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validate(final Properties content, final Map<String, Object> temporary, final ErrorProcessing<Properties, ErrorType> err) throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterShow(final Properties content, final Map<String, Object> temporary, final ErrorProcessing<Properties, ErrorType> err) throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		
	}
}
