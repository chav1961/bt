package chav1961.bt.svgeditor.screen;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import chav1961.bt.svgeditor.dialogs.SettingsDialog;
import chav1961.bt.svgeditor.interfaces.StateChangedListener;
import chav1961.bt.svgeditor.internal.AppWindow;
import chav1961.bt.svgeditor.parser.CommandLineParser;
import chav1961.bt.svgeditor.screen.InsertManager.PrimitiveType;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PureLibException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.CommandHistory;

public class SVGEditor extends JPanel implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, InputStreamGetter, OutputStreamGetter {
	private static final long serialVersionUID = 6904531215744148397L;

	private static final String	APP_COMMAND_PROMPT = "chav1961.bt.svgeditor.screen.SVGEditor.command.prompt";
	private static final String	APP_COMMAND_PROMPT_TT = "chav1961.bt.svgeditor.screen.SVGEditor.command.prompt.tt";
	private static final String	APP_COMMAND_COMPLETED = "chav1961.bt.svgeditor.screen.SVGEditor.command.completed";
	private static final String	APP_COMMAND_ERROR = "chav1961.bt.svgeditor.screen.SVGEditor.command.error";

	private final JLabel			commandLabel = new JLabel();
	private final JTextField		command = new JTextField();
	private final JLayeredPane		pane = new JLayeredPane();
	private final CommandLineParser	parser = new CommandLineParser();
	private final AppWindow			owner;
	private final Localizer			localizer;
	private final SVGCanvas			canvas;
	private boolean	insertionTurnedOn = false;
	private byte[]	temp = new byte[0];

	public static enum InsertAction {
		INSERT_LINE(InsertManager.PrimitiveType.LINE);

		private final PrimitiveType	type;
		
		private InsertAction(final PrimitiveType type) {
			this.type = type;
		}
	}
	
	public SVGEditor(final AppWindow owner, final Localizer localizer) {
		super(new BorderLayout());
		if (owner == null) {
			throw new NullPointerException("Owner can't be null");
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			final JPanel	commandPanel = new JPanel(new BorderLayout());
			
			this.owner = owner;
			this.localizer = localizer;
			this.canvas = new SVGCanvas(localizer);
			owner.getProperties().addPropertyChangeListener(canvas);
			commandPanel.add(commandLabel, BorderLayout.WEST);
			commandPanel.add(command, BorderLayout.CENTER);
			canvas.setBackground(Color.BLACK);
			canvas.setForeground(Color.LIGHT_GRAY);
			SwingUtilities.invokeLater(()->canvas.setGridEnabled(owner.getProperty(SettingsDialog.PropKeys.GRID_MODE)));
			pane.add(canvas, JLayeredPane.FRAME_CONTENT_LAYER);
			add(new JScrollPane(pane), BorderLayout.CENTER);
			add(commandPanel, BorderLayout.SOUTH);
	
			owner.getProperties().addPropertyChangeListener((e)->{
				switch (SettingsDialog.PropKeys.byName(e.getPropertyName())) {
					case GRID_MODE 		:
						canvas.setGridEnabled(Boolean.valueOf(e.getNewValue().toString()));
					case DEFAULT_GRID 	:
						canvas.repaint();
						break;
					default:
						break;
				}
			});
			CommandHistory.of(command,(c)->{executeCommand(c);});
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(canvas, oldLocale, newLocale);
		fillLocalizedStrings();
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}
	
	@Override
	public LoggerFacade getLogger() {
		return SwingUtils.getNearestLogger(getParent());
	}

	public boolean canUndo() {
		return canvas.getUndoable().canUndo();
	}
	
	public void undo() {
		if (canUndo()) {
			canvas.restoreSnapshot(canvas.getUndoable().undo()[0]);
		}
	}

	public boolean canRedo() {
		return canvas.getUndoable().canRedo();
	}

	public void redo() {
		if (canRedo()) {
			canvas.restoreSnapshot(canvas.getUndoable().redo()[1]);
		}
	}
	
	public void clearHistory() {
		canvas.getUndoable().clearUndo();
	}
	
	public void setFocus() {
		command.requestFocusInWindow();
	}
	
	public boolean isEmpty() {
		return canvas.getItemCount() == 0;
	}
	
	public boolean isAnySelected() {
		return canvas.isAnySelected();
	}
	
	public <T> T execute(final CharSequence seq, final Class<T> awaited) throws SyntaxException {
		return null;
	}

	public void startInsertion(final InsertAction action) {
		if (action == null) {
			throw new NullPointerException("Insert action can't be null");
		}
		else {
			if (isInInsertionNow()) {
				endInsertion(false);
			}
			canvas.pushMouseManager(new InsertManager(canvas, action.type));
			canvas.requestFocusInWindow();
			insertionTurnedOn = true;
		}
	}
	
	public boolean isInInsertionNow() {
		return insertionTurnedOn;
	}
	
	public void endInsertion(final boolean commit) {
		try(final InsertManager	im = (InsertManager)canvas.popMouseManager()) {
			if (commit) {
				canvas.add(im.getResult());
			}
		}
		setFocus();
		insertionTurnedOn = false;
	}
	
	public void addStateChangedListener(final StateChangedListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			canvas.addStateChangedListener(l);
		}
	}

	public void removeStateChangedListener(final StateChangedListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			canvas.removeStateChangedListener(l);
		}
	}
	
	public Point2D currentMousePoint() {
		return canvas.currentMousePoint();
	}

	public double currentScale() {
		return canvas.currentScale();
	}
	
	@Override
	public OutputStream getOutputContent() throws IOException {
		// TODO Auto-generated method stub
		return new ByteArrayOutputStream() {
			@Override
			public void close() throws IOException {
				super.close();
				temp = toByteArray();
				getLogger().message(Severity.note, "Loading successful");
			}
		};
	}

	@Override
	public InputStream getInputContent() throws IOException {
		// TODO Auto-generated method stub
		return new ByteArrayInputStream(temp) {
			@Override
			public void close() throws IOException {
				super.close();
				getLogger().message(Severity.note, "Storing successful");
			}
		};
	}
	
	public void executeCommand(final String command) throws CommandLineParametersException, CalculationException {
		try{
			System.err.println("Cmd="+command);
			parser.parse(command, canvas);
			getLogger().message(Severity.info, APP_COMMAND_COMPLETED);
		} catch (CommandLineParametersException | CalculationException exc) {
			getLogger().message(Severity.severe, exc, APP_COMMAND_ERROR, exc.getLocalizedMessage());
			throw exc;
		}
	}

	private void fillLocalizedStrings() {
		commandLabel.setText(getLocalizer().getValue(APP_COMMAND_PROMPT));
		command.setToolTipText(getLocalizer().getValue(APP_COMMAND_PROMPT_TT));
	}
}
