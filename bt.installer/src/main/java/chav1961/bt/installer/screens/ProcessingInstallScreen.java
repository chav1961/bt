package chav1961.bt.installer.screens;

import java.awt.Image;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;

import chav1961.bt.installer.interfaces.ErrorType;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.ui.interfaces.ErrorProcessing;

public class ProcessingInstallScreen extends AbstractWizardStep {
	private static final long serialVersionUID = 1L;
	private final WizardStepOption[]	options;

	public ProcessingInstallScreen(final String stepId, final String caption, final String description, final String helpId, final WizardStepOption... options) {
		super(stepId, caption, description, helpId);
		this.options = options;
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
		final JList<ExecutionStep>	steps = new JList<>();
		final JSplitPane			split = new JSplitPane();
		return null;
	}

	@Override
	public String getPrevStep() {
		// TODO Auto-generated method stub
		return super.getPrevStep();
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

	private static class ExecutionStep {
		private static final Image	IMAGE_BEFORE = loadImage("");
		private static final Image	IMAGE_PROCESSING = loadImage("");
		private static final Image	IMAGE_FINISHED = loadImage("");
		private static final Image	IMAGE_COMPLETED = loadImage("");
		
		private static final JProgressBar	bar = new JProgressBar();
		
		private static Image loadImage(final String resourceName) {
			try {
				return ImageIO.read(ExecutionStep.class.getResourceAsStream(resourceName));
			} catch (IOException e) {
				throw new EnvironmentException(e);
			}
		}
	}
}
