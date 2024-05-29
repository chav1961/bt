package chav1961.bt.installer.screens;

import java.io.Serializable;
import java.util.Properties;
import javax.swing.JPanel;

import chav1961.bt.installer.interfaces.ErrorType;
import chav1961.purelib.ui.interfaces.WizardStep;

abstract class AbstractWizardStep implements WizardStep<Properties, ErrorType, JPanel>, Serializable {
	private static final long 	serialVersionUID = -3076932844155699652L;
	
	private final String	stepId;
	private final String	caption;
	private final String	description;
	private final String	helpId;	
	
	AbstractWizardStep(final String stepId, final String caption, final String description, final String helpId) {
		this.stepId = stepId;
		this.caption = caption;
		this.description = description;
		this.helpId = helpId;
	}

	public abstract ScreenType getScreenType();
	
	@Override
	public String getStepId() {
		return stepId;
	}

	@Override
	public String getCaption() {
		return caption;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getHelpId() {
		return helpId;
	}
	
	public static abstract class WizardStepOption {
	}
}
