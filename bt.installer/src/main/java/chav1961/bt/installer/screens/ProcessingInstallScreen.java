package chav1961.bt.installer.screens;

import java.util.Map;
import java.util.Properties;

import javax.swing.JPanel;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class ProcessingInstallScreen extends AbstractWizardStep {

	public ProcessingInstallScreen(String stepId, String caption, String description, String helpId) {
		super(stepId, caption, description, helpId);
	}

	@Override
	public StepType getStepType() {
		return StepType.PROCESSING;
	}

	@Override
	public ScreenType getScreenType() {
		return ScreenType.PROCESSING_INSTALL;
	}

	
	@Override
	public JPanel getContent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPrevStep() {
		// TODO Auto-generated method stub
		return super.getPrevStep();
	}
	
	@Override
	public void beforeShow(Properties content, Map<String, Object> temporary,
			ErrorProcessing<Properties, ErrorType> err)
			throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean validate(Properties content, Map<String, Object> temporary,
			ErrorProcessing<Properties, ErrorType> err)
			throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void afterShow(Properties content, Map<String, Object> temporary, ErrorProcessing<Properties, ErrorType> err)
			throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		
	}

}
