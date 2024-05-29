package chav1961.bt.installer.screens;

import java.util.Map;
import java.util.Properties;

import javax.swing.JPanel;

import chav1961.bt.installer.interfaces.ErrorType;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class CalculateChoiseScreen extends AbstractWizardStep {
	private final WizardStepOption[]	options;

	public CalculateChoiseScreen(final String stepId, final String caption, final String description, final String helpId, final WizardStepOption... options) {
		super(stepId, caption, description, helpId);
		this.options = options;
	}

	@Override
	public StepType getStepType() {
		return StepType.PROCESSING;
	}

	@Override
	public ScreenType getScreenType() {
		return ScreenType.CALCULATE_CHOISE;
	}
	
	@Override
	public String getNextStep() {
		// TODO Auto-generated method stub
		return super.getNextStep();
	}
	
	@Override
	public JPanel getContent() {
		// TODO Auto-generated method stub
		return null;
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
