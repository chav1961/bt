package chav1961.bt.paint.script.intern;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.Exchanger;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.FunctionalDocumentListener;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.bt.paint.script.intern.interfaces.ExecuteScriptCallback;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.SyntaxNodeType;
import chav1961.bt.paint.script.intern.runtime.ScriptExecutorUtil;

public class DebuggerPanel extends JPanel implements LocaleChangeListener, LocalizerOwner {
	private static final long serialVersionUID = 1L;
	
	private final ContentMetadataInterface	xda;
	private final Localizer					localizer;
	private final Predefines				predef;
	private final JFileContentManipulator	manipulator;
	private final Consumer<DebuggerPanel>	onClose;	
	private final JToolBar					toolBar;
	private final JScriptPane				script = new JScriptPane();
	private final JEditorPane				console = new JEditorPane();
	private Thread							executor = null;
	private final Exchanger<Object>			ex = new Exchanger<>();
	private final DebuggerCallback			dc = new DebuggerCallback((o)->processDebug());

	public DebuggerPanel(final ContentMetadataInterface xda, final Localizer localizer, final Predefines predef, final JFileContentManipulator manipulator, final Consumer<DebuggerPanel> onClose) {
		if (xda == null) {
			throw new NullPointerException("Content metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (predef == null) {
			throw new NullPointerException("Predefines can't be null"); 
		}
		else if (manipulator == null) {
			throw new NullPointerException("Content manipulator can't be null"); 
		}
		else if (onClose == null) {
			throw new NullPointerException("Consumer can't be null"); 
		}
		else {
			this.xda = xda;
			this.localizer = localizer;
			this.predef = predef;
			this.manipulator = manipulator;
			this.onClose = onClose;
			this.toolBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.debugBar")), JToolBar.class);
			
			toolBar.setFloatable(false);
			toolBar.setOrientation(JToolBar.HORIZONTAL);
		    SwingUtils.assignActionListeners(toolBar, this);
			console.setEditable(false);

			script.getDocument().addDocumentListener((FunctionalDocumentListener)(t,e)->{
				manipulator.setModificationFlag();
				refreshSaveToolBarState();
			});
			
			setLayout(new BorderLayout(5,5));
	
			final JSplitPane	split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(script), new JScrollPane(console)); 
			
			add(toolBar, BorderLayout.NORTH);
			add(split, BorderLayout.CENTER);
			split.setDividerLocation(500);
			setPreferredSize(new Dimension(550,200));
			refreshToolBarState();
		}
	}
	
	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(toolBar, oldLocale, newLocale);
		fillLocalizedStrings();
	}

	@OnAction("action:/cleanScript")
    public void cleanScript() throws IOException {
		if (manipulator.newFile()) {
			manipulator.clearModificationFlag();
		}
	}	

	@OnAction("action:/loadScript")
    public void loadScript() throws IOException {
		if (manipulator.openFile()) {
			manipulator.clearModificationFlag();
		}
	}	

	@OnAction("action:/storeScript")
    public void storeScript() throws IOException {
		if (manipulator.saveFile()) {
			manipulator.clearModificationFlag();
		}
	}	
	
	@OnAction("action:/storeScriptAs")
    public void storeScriptAs() throws IOException {
		if (manipulator.saveFileAs()) {
			manipulator.clearModificationFlag();
		}
	}	

	@OnAction("action:/startScript")
    public void startScript() throws IOException {
		try{final SyntaxTreeInterface	names = new AndOrTree<>();		
			final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	tree = ScriptParserUtil.parseScript(new StringReader(script.getText()), names);
			
			if (executor != null) {
				executor.interrupt();
			}
			executor = new Thread(()->{
				try{executeScript(tree, dc);
				} catch (PaintScriptException | InterruptedException e) {
					e.printStackTrace();
				}
			});
			executor.setName("script-executor");
			executor.setDaemon(true);
			refreshDebugTooltBarState();
			dc.start();
			executor.start();
		} catch (SyntaxException exc) {
			throw new IOException(exc);
		}
	}	

	@OnAction("action:/pauseScript")
    public void pauseScript(final Hashtable<String,String[]> modes) throws IOException {
		if (!dc.isSuspended()) {
			dc.suspend();
		}
		else {
			dc.resume();
		}
		refreshDebugTooltBarState();
	}	

	@OnAction("action:/stopScript")
    public void stopScript() throws IOException, InterruptedException {
		dc.stop();
		if (executor != null) {
			executor.interrupt();
			executor.join(1000);
			executor = null;
			refreshDebugTooltBarState();
		}
	}	

	@OnAction("action:/nextStep")
    public void nextStep() throws IOException {
		dc.setModeAndGo(DebuggerCallback.MODE_NEXT);
	}	

	@OnAction("action:/intoStep")
    public void intoStep() throws IOException {
		dc.setModeAndGo(DebuggerCallback.MODE_INTO);
	}	

	@OnAction("action:/outStep")
    public void outStep() throws IOException {
		dc.setModeAndGo(DebuggerCallback.MODE_OUT);
	}	
	
	@OnAction("action:/runStep")
    public void runStep() throws IOException {
		dc.setModeAndGo(DebuggerCallback.MODE_RUN);
	}

	@OnAction("action:/exitScript")
    public void exit() throws IOException {
		if (manipulator.wasChanged()) {
			manipulator.saveFile();
		}
		onClose.accept(this);
	}
	
	private void executeScript(final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> tree, final ExecuteScriptCallback callback) throws PaintScriptException, InterruptedException {
		ScriptExecutorUtil.execute(tree, null, predef, 0, 0, callback);
	}

	private void processDebug() {
		// TODO Auto-generated method stub
	}
	
	private void refreshDebugTooltBarState() {
		final boolean	processing = executor != null && executor.isAlive();

		((JButton)SwingUtils.findComponentByName(toolBar, "debug.execute.start")).setEnabled(!processing);
		((JToggleButton)SwingUtils.findComponentByName(toolBar, "debug.execute.pause")).setEnabled(processing);
		((JButton)SwingUtils.findComponentByName(toolBar, "debug.execute.stop")).setEnabled(processing);
		((JButton)SwingUtils.findComponentByName(toolBar, "debug.execute.step.next")).setEnabled(processing);
		((JButton)SwingUtils.findComponentByName(toolBar, "debug.execute.step.into")).setEnabled(processing);
		((JButton)SwingUtils.findComponentByName(toolBar, "debug.execute.step.out")).setEnabled(processing);
		((JButton)SwingUtils.findComponentByName(toolBar, "debug.execute.step.run")).setEnabled(processing);
	}	
	
	private void refreshSaveToolBarState() {
		((JButton)SwingUtils.findComponentByName(toolBar, "debug.file.store")).setEnabled(manipulator.wasChanged());
		((JButton)SwingUtils.findComponentByName(toolBar, "debug.file.storeAs")).setEnabled(manipulator.wasChanged());
	}

	private void refreshToolBarState() {
		refreshSaveToolBarState();
		refreshDebugTooltBarState();
	}	
	
	private void fillLocalizedStrings() throws LocalizationException {
	}
}
