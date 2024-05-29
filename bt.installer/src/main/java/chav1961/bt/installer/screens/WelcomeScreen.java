package chav1961.bt.installer.screens;

import java.net.URI;
import java.util.Map;
import java.util.Properties;

import javax.swing.JPanel;

import chav1961.bt.installer.interfaces.ErrorType;
import chav1961.bt.installer.screens.AbstractWizardStep.WizardStepOption;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class WelcomeScreen extends AbstractWizardStep {
	private final WizardStepOption[]	options;
	
	public WelcomeScreen(final String stepId, final String caption, final String description, final String helpId, final WizardStepOption... options) {
		super(stepId, caption, description, helpId);
		this.options = options;
	}

	@Override
	public StepType getStepType() {
		return StepType.INITIAL;
	}

	@Override
	public ScreenType getScreenType() {
		return ScreenType.WELCOME;
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
		return true;
	}

	@Override
	public void afterShow(final Properties content, final Map<String, Object> temporary, final ErrorProcessing<Properties, ErrorType> err) throws FlowException, LocalizationException, NullPointerException {
		// TODO Auto-generated method stub
		
	}
	
	public static class SiteReference extends WizardStepOption {
		private final URI		siteReference;
		private final String	title;
		
		public SiteReference(final URI siteReference, final String title) {
			this.siteReference = siteReference;
			this.title = title;
		}

		@Override
		public String toString() {
			return "SiteReference [siteReference=" + siteReference + ", title=" + title + "]";
		}
	}
}
