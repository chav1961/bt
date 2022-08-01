package chav1961.bt.paint.script.intern;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Locale;
import java.util.concurrent.Exchanger;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import chav1961.bt.paint.control.Predefines;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.SyntaxNodeType;

public class DebuggerPanel extends JPanel implements LocaleChangeListener, LocalizerOwner {
	private static final long serialVersionUID = 1L;
	
	private final ContentMetadataInterface	xda;
	private final Localizer					localizer;
	private final Predefines				predef;
	private final JToolBar					toolBar;
	private final JScriptPane				script = new JScriptPane();
	private final JEditorPane				console = new JEditorPane();
	private Thread							executor = null;
	private final Exchanger<Object>			ex = new Exchanger<>();

	public DebuggerPanel(final ContentMetadataInterface xda, final Localizer localizer, final Predefines predef) {
		if (xda == null) {
			throw new NullPointerException("Content metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (predef == null) {
			throw new NullPointerException("Predefines can't be null"); 
		}
		else {
			this.xda = xda;
			this.localizer = localizer;
			this.predef = predef;
			this.toolBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.debugBar")), JToolBar.class);
			
			toolBar.setFloatable(false);
			toolBar.setOrientation(JToolBar.HORIZONTAL);
		    SwingUtils.assignActionListeners(toolBar, this);
			console.setEditable(false);
			
			setLayout(new BorderLayout(5,5));
	
			add(toolBar, BorderLayout.NORTH);
			add(new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(script), new JScrollPane(console)), BorderLayout.CENTER);
		}
	}
	
	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		fillLocalizedStrings();
	}

	@OnAction("action:/cleanScript")
    public void cleanScript() throws IOException {
	}	

	@OnAction("action:/loadScript")
    public void loadScript() throws IOException {
	}	

	@OnAction("action:/storeScript")
    public void storeScript() throws IOException {
	}	
	
	@OnAction("action:/storeScriptAs")
    public void storeScriptAs() throws IOException {
	}	

	@OnAction("action:/startScript")
    public void startScript() throws IOException {
		final SyntaxTreeInterface<Object>	names = new AndOrTree<>();		
		final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>>	tree = ScriptParserUtil.parseScript(new StringReader(script.getText()), names);
		
		if (executor != null) {
			executor.interrupt();
		}
		executor = new Thread(()->executeScript(tree));
		executor.setName("script-executor");
		executor.setDaemon(true);
		refreshTooltBarState();
	}	

	@OnAction("action:/pauseScript")
    public void pauseScript() throws IOException {
	}	

	@OnAction("action:/startScript")
    public void stopScript() throws IOException, InterruptedException {
		if (executor != null) {
			executor.interrupt();
			executor.join(1000);
			executor = null;
		}
	}	

	@OnAction("action:/nextStep")
    public void nextStep() throws IOException {
	}	

	@OnAction("action:/intoStep")
    public void intoStep() throws IOException {
	}	

	@OnAction("action:/outStep")
    public void outStep() throws IOException {
	}	
	
	@OnAction("action:/outStep")
    public void runStep() throws IOException {
	}

	private void executeScript(final SyntaxNode<SyntaxNodeType, SyntaxNode<SyntaxNodeType, ?>> tree) {
		// TODO:
	}

	private void refreshTooltBarState() {
		// TODO Auto-generated method stub
		
	}

	private void fillLocalizedStrings() throws LocalizationException {
		
	}
}
