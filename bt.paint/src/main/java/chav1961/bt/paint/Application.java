package chav1961.bt.paint;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;

import chav1961.bt.paint.control.ImageEditPanel;
import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.control.ImageUtils.ProcessType;
import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.dialogs.AskImageSize;
import chav1961.bt.paint.dialogs.AskSettings;
import chav1961.bt.paint.dialogs.AskFilterMatrixTable;
import chav1961.bt.paint.dialogs.AskFind;
import chav1961.bt.paint.dialogs.AskFindAndReplace;
import chav1961.bt.paint.dialogs.AskSubstitutionTable;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.CanvasWrapperImpl;
import chav1961.bt.paint.script.ImageWrapperImpl;
import chav1961.bt.paint.script.ScriptNodeType;
import chav1961.bt.paint.script.SystemWrapperImpl;
import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ClipboardWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.bt.paint.script.intern.DebuggerPanel;
import chav1961.bt.paint.script.intern.runtime.ScriptUtils;
import chav1961.bt.paint.utils.ApplicationUtils;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.basic.interfaces.ProgressIndicator;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.nanoservice.NanoServiceFactory;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;

public class Application extends JFrame implements NodeMetadataOwner, LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, AutoCloseable {
	private static final long 		serialVersionUID = 1083999598002477077L;
	public static final String		ARG_COMMAND = "command";
	public static final String		ARG_INPUT_MASK = "inputMask";
	public static final String		ARG_RECURSION_FLAG = "recursion";
	public static final String		ARG_GUI_FLAG = "guiFlag";
	public static final String		ARG_PROPFILE_LOCATION = "prop";
	
	private static final String		KEY_PNG_FILES = "chav1961.bt.paint.Application.filter.pngfiles";
	private static final String		KEY_JPG_FILES = "chav1961.bt.paint.Application.filter.jpgfiles";
	private static final String		KEY_GIF_FILES = "chav1961.bt.paint.Application.filter.giffiles";
	private static final String		KEY_BMP_FILES = "chav1961.bt.paint.Application.filter.bmpfiles";
	private static final String		KEY_CMD_FILES = "chav1961.bt.paint.Application.filter.cmdfiles";
	private static final String		KEY_PSC_FILES = "chav1961.bt.paint.Application.filter.pscfiles";
	private static final String		KEY_APPLICATION_TITLE = "chav1961.bt.paint.Application.title";
	
	private static final String		KEY_APPLICATION_MESSAGE_READY = "chav1961.bt.paint.Application.message.ready";
	private static final String		KEY_APPLICATION_MESSAGE_NEW_NAME = "chav1961.bt.paint.Application.message.new.name";	
	private static final String		KEY_APPLICATION_MESSAGE_NEW_IMAGE_CREATED = "chav1961.bt.paint.Application.message.new.image.created";	
	private static final String		KEY_APPLICATION_MESSAGE_IMAGE_LOADING = "chav1961.bt.paint.Application.message.image.loading";	
	private static final String		KEY_APPLICATION_MESSAGE_IMAGE_LOADED = "chav1961.bt.paint.Application.message.image.loaded";	
	private static final String		KEY_APPLICATION_MESSAGE_IMAGE_STORED = "chav1961.bt.paint.Application.message.image.stored";	
	
	private static final String		KEY_APPLICATION_HELP_TITLE = "chav1961.bt.paint.Application.help.title";
	private static final String		KEY_APPLICATION_HELP_CONTENT = "chav1961.bt.paint.Application.help.content";
	private static final String		KEY_APPLICATION_END_BATCH_TITLE = "chav1961.bt.paint.Application.endbatch.title";
	private static final String		KEY_APPLICATION_END_BATCH_CONTENT = "chav1961.bt.paint.Application.endbatch.content";
	private static final String		KEY_APPLICATION_CUSTOM_FILTER_TYPE_ITEMS = "chav1961.bt.paint.Application.customfilter.typeItems";
	private static final String		KEY_APPLICATION_PLAYER_ENTER_SUBSTITUTIONS = "chav1961.bt.paint.Application.player.enterSubstitutions";

	private static final String		KEY_UNDO_FILTER = "chav1961.bt.paint.editor.ImageEditPanel.undo.filter";
	private static final String		KEY_REDO_FILTER = "chav1961.bt.paint.editor.ImageEditPanel.redo.filter";
	
	public static enum ApplicationMode {
		IN_OUT,
		BATCH,
		GUI
	}
	
	private final ApplicationMode			appMode;
	private final FileSystemInterface		fsi;
	private final ContentMetadataInterface	xda;
	private final Localizer					localizer;
	private final Predefines				predef;
	private final CountDownLatch			latch;
	private final ImageEditPanel			panel;
	private final JStateString				state;
	private final UndoManager				undoMgr = new UndoManager();
	private final StringBuilder				commands = new StringBuilder(); 
	private final ImageManipulator			imageManipulator;
	private final ScriptManipulator			scriptManipulator;
	private final Settings					settings = new Settings(new File("./.bt.paint"));

	private int								helpPort = 0;
	private volatile Exchanger<Object>		batchSource = null;
	private volatile JMenuBar				menuBar;
	private String							lastFile = null;
	private String							lastScript = null;
	private boolean							recordingOn = false, pauseOn = false;
	private DebuggerPanel					debugger = null;
	
	public Application(final ApplicationMode mode, final ContentMetadataInterface xda, final Localizer localizer, final Predefines predef, final CountDownLatch latch) throws IOException, PaintScriptException {
		if (mode == null) {
			throw new NullPointerException("Application mode can't be null"); 
		}
		else if (xda == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (predef == null) {
			throw new NullPointerException("Predefines can't be null"); 
		}
		else if (latch == null) {
			throw new NullPointerException("Latch can't be null"); 
		}
		else {
			this.appMode = mode;
			this.xda = xda;
			this.localizer = localizer;
			this.predef = predef;
			this.latch = latch;
			this.fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"));
			
			switch (appMode) {
				case IN_OUT	:
			        this.menuBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.inoutmenu")), JMenuBar.class); 
					break;
				case BATCH	:
			        this.menuBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.batchmenu")), JMenuBar.class); 
					break;
				case GUI	:
			        this.menuBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
					break;
				default :
					throw new UnsupportedOperationException("Application mode ["+appMode+"] is not supported yet");
			}
	        
	        this.panel = new ImageEditPanel(localizer);
	        this.state = new JStateString(localizer, 100);

			this.imageManipulator = new ImageManipulator(this.state, this.fsi, this.localizer
								,()->{return new InputStream() {@Override public int read() throws IOException {return -1;}};}
								,()->{return new OutputStream() {@Override public void write(int b) throws IOException {}};},
								settings
								);
			this.imageManipulator.addFileContentChangeListener((e)->processImageChanges(e));
			this.scriptManipulator = new ScriptManipulator(this.state, this.fsi, this.localizer
								,()->{return new InputStream() {@Override public int read() throws IOException {return -1;}};}
								,()->{return new OutputStream() {@Override public void write(int b) throws IOException {}};},
								settings
								);
			this.imageManipulator.addFileContentChangeListener((e)->processScriptChanges(e));
	        
	        this.panel.addUndoableEditListener((e)->processUndoEvents(e));
	        this.panel.addActionListener((e)->processCommand(e));
	        SwingUtils.assignActionListeners(menuBar, this);
	        
	        state.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

	        getContentPane().add(menuBar, BorderLayout.NORTH);
	        getContentPane().add(panel, BorderLayout.CENTER);
	        getContentPane().add(state, BorderLayout.SOUTH);

			this.predef.putPredefined(Predefines.PREDEF_SYSTEM, new SystemWrapperImpl(this.fsi, new File("./").getAbsoluteFile().toURI()));
			this.predef.putPredefined(Predefines.PREDEF_CANVAS, panel);
	        
	        panel.addChangeListener((e)->{
	        	try {refreshMenuState();
	        	} catch (PaintScriptException exc) {
	    			state.message(Severity.error, exc, exc.getLocalizedMessage());
	        	}
	        });
			fillImageLRU(this.imageManipulator.getLastUsed());
			fillScriptLRU(this.scriptManipulator.getLastUsed());
	        refreshMenuState();
	        localizer.addLocaleChangeListener(this);
	        
	        predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).addChangeListener((e)->{
	    		try{((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.paste")).setEnabled(predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).hasImage());
				} catch (PaintScriptException exc) {
					getLogger().message(Severity.error, exc.getLocalizedMessage());
				}
	        });
	        
			SwingUtils.assignExitMethod4MainWindow(this,()->exit());
			SwingUtils.assignActionKey((JComponent)getContentPane(), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, SwingUtils.KS_EXIT, (e)->panel.clearCommandString(), SwingUtils.ACTION_EXIT); 
			SwingUtils.centerMainWindow(this, 0.85f);
			fillLocalizedStrings();
			state.message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		dispose();
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(menuBar, oldLocale, newLocale);
		SwingUtils.refreshLocale(panel, oldLocale, newLocale);
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
		fillLocalizedStrings();
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public LoggerFacade getLogger() {
		return state;
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return xda.getRoot();
	}

	public int getHelpPort() {
		return helpPort;
	}
	
	public void setHelpPort(final int helpPort) {
		this.helpPort = helpPort;
	}
	
	
	@OnAction("action:/newImage")
    public void newImage() {
		try{if (imageManipulator.newFile()) {
				final BufferedImage	img = (BufferedImage)imageManipulator.image.getImage();
						
				getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_NEW_IMAGE_CREATED, img.getWidth(), img.getHeight());
			}
		} catch (IOException | PaintScriptException exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
    }

	@OnAction("action:/nextBatch")
    public void nextBatch() throws IOException {
		if (batchSource == null) {
			throw new IllegalStateException("Batch source is not assigned yet"); 
		}
		else {
			try{getLogger().message(Severity.warning, KEY_APPLICATION_MESSAGE_IMAGE_LOADING);
				final ImageWrapper	iw = (ImageWrapper) batchSource.exchange(null);
				
				if (iw != null) {
					try{panel.setImage(iw);
						getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_IMAGE_LOADED, iw.getName());
					} catch (PaintScriptException e) {
						getLogger().message(Severity.error, e, e.getLocalizedMessage());
					}
				}
				else {
					switch (new JLocalizedOptionPane(localizer).confirm(this, KEY_APPLICATION_END_BATCH_CONTENT, KEY_APPLICATION_END_BATCH_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION)) {
						case JOptionPane.YES_OPTION :
							latch.countDown();
							close();
							break;
						case JOptionPane.NO_OPTION :
							getContentPane().remove(menuBar);
					        menuBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
					        getContentPane().add(menuBar, BorderLayout.NORTH);
					        SwingUtils.assignActionListeners(menuBar, this);
					        batchSource = null;
					        refreshMenuState();
					        break;
					}
				}
			} catch (InterruptedException | PaintScriptException e) {
				latch.countDown();
				close();
			}
		}
	}	
	
	@OnAction("action:/loadImage")
    public void loadImage() {
		imageManipulator.setFilters(	// Refresh every call because of possibly language changes
				FilterCallback.of(localizer.getValue(KEY_PNG_FILES), "*.png"), 
				FilterCallback.of(localizer.getValue(KEY_JPG_FILES), "*.jpg"),
				FilterCallback.of(localizer.getValue(KEY_GIF_FILES), "*.gif"),
				FilterCallback.of(localizer.getValue(KEY_BMP_FILES), "*.bmp")
		);
		
		try{if (imageManipulator.openFile()) {
				final BufferedImage	img = (BufferedImage)imageManipulator.image.getImage();
				
				getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_IMAGE_LOADED, imageManipulator.image.getName(), img.getWidth(), img.getHeight());
			}
		} catch (IOException | PaintScriptException exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
    }

    public void loadLRUImage(final String file) {
		imageManipulator.setFilters(	// Refresh every call because of possibly language changes
				FilterCallback.of(localizer.getValue(KEY_PNG_FILES), "*.png"), 
				FilterCallback.of(localizer.getValue(KEY_JPG_FILES), "*.jpg"),
				FilterCallback.of(localizer.getValue(KEY_GIF_FILES), "*.gif"),
				FilterCallback.of(localizer.getValue(KEY_BMP_FILES), "*.bmp")
		);
		
		try{if (imageManipulator.openLRUFile(file)) {
				final BufferedImage	img = (BufferedImage)imageManipulator.image.getImage();
				
				getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_IMAGE_LOADED, imageManipulator.image.getName(), img.getWidth(), img.getHeight());
			}
		} catch (IOException | PaintScriptException exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
    }
    
	@OnAction("action:/saveImage")
    public void saveImage() {
		imageManipulator.setFilters(	// Refresh every call because of possibly language changes
				FilterCallback.of(localizer.getValue(KEY_PNG_FILES), "*.png"), 
				FilterCallback.of(localizer.getValue(KEY_JPG_FILES), "*.jpg"),
				FilterCallback.of(localizer.getValue(KEY_GIF_FILES), "*.gif"),
				FilterCallback.of(localizer.getValue(KEY_BMP_FILES), "*.bmp")
		);
		
		try{imageManipulator.image = panel.getImage();
			
			if (imageManipulator.saveFile()) {
				getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_IMAGE_STORED, lastFile);
			}
		} catch (IOException | PaintScriptException  exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
    }

	@OnAction("action:/saveBatch")
    public void saveBatch() {
	}	

	@OnAction("action:/saveBatch")
    public void saveOut() {
	}	
	
	@OnAction("action:/saveImageAs")
    public void saveImageAs() {
		imageManipulator.setFilters(	// Refresh every call because of possibly language changes
				FilterCallback.of(localizer.getValue(KEY_PNG_FILES), "*.png"), 
				FilterCallback.of(localizer.getValue(KEY_JPG_FILES), "*.jpg"),
				FilterCallback.of(localizer.getValue(KEY_GIF_FILES), "*.gif"),
				FilterCallback.of(localizer.getValue(KEY_BMP_FILES), "*.bmp")
		);
		
		try{imageManipulator.image = panel.getImage();
			
			if (imageManipulator.saveFileAs()) {
				getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_IMAGE_STORED, lastFile);
			}
		} catch (IOException | PaintScriptException  exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
    }

	@OnAction("action:/exit")
    public void exit() {
		switch (appMode) {
			case IN_OUT	:
				break;
			case BATCH	:
				break;
			case GUI	:
				try{imageManipulator.close();
					scriptManipulator.close();
					latch.countDown();
				} catch (UnsupportedOperationException exc) {
				} catch (IOException e) {
					latch.countDown();
				}
				break;
			default :
				throw new UnsupportedOperationException("Application mode ["+appMode+"] is not supported yet"); 
		}
    }

	@OnAction("action:/undo")
    public void undo() {
		undoMgr.undo();
	}	
	
	@OnAction("action:/redo")
    public void redo() {
		undoMgr.redo();
	}	

	@OnAction("action:/cut")
    public void cut() {
		try{predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).setImage(ImageWrapper.of((BufferedImage)panel.cutSelectedImage()));
		} catch (PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
	}	

	@OnAction("action:/copy")
    public void copy() {
		try{predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).setImage(ImageWrapper.of((BufferedImage)panel.getSelectedImage()));
		} catch (PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
	}	

	@OnAction("action:/paste")
    public void paste() {
		try{panel.pasteImage(predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).getImage().getImage());
		} catch (PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
	}	

	@OnAction("action:/pasteSvg")
    public void pasteSvg() {
		try{panel.pasteImage(predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).getImage().getImage());
		} catch (PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
	}	
	
	@OnAction("action:/filters")
    public void filters(final Hashtable<String,String[]> matrix) throws IOException {
		if (matrix.containsKey("matrix")) {
			final String[]	items = matrix.get("matrix")[0].split("\\,");
			final float[]	floatItems = new float[items.length];
			
			for (int index = 0; index < floatItems.length; index++) {
				floatItems[index] = Float.valueOf(items[index].trim());
			}
			processFilter(floatItems);
		}
		else {
			getLogger().message(Severity.error,"Mandatory parameter 'matrix' is missing");
		}
	}	
	
	@OnAction("action:/customfilters")
    public void customFilters() throws PaintScriptException {
		final AskFilterMatrixTable	fmt = new AskFilterMatrixTable(3);
		
		fmt.setPreferredSize(new Dimension(300, 60));
		fmt.requestFocusInWindow();
		switch (new JLocalizedOptionPane(localizer).confirm(this, fmt, KEY_APPLICATION_CUSTOM_FILTER_TYPE_ITEMS, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION)) {
			case JOptionPane.OK_OPTION :
				processFilter(fmt.getMaxtrix());
				break;
			default :
				break;
		}		
	}	
	
	@OnAction("action:/find")
    public void find() {
		final AskFind	af = new AskFind(getLogger());
		
		try{if (ApplicationUtils.ask(af, getLocalizer(), 300, 300)) {
				// TODO
			}
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}	
	
	@OnAction("action:/replace")
    public void replace() {
		final AskFindAndReplace	af = new AskFindAndReplace(getLogger());
		
		try{if (ApplicationUtils.ask(af, getLocalizer(), 300, 500)) {
				// TODO
			}
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}

	@OnAction("action:/player.recording")
	public void recording(final Hashtable<String,String[]> modes) throws IOException {
		recordingOn = !recordingOn;
		pauseOn = false;
		refreshPlayerMenuState();
		
		if (!recordingOn) {
			if (!commands.isEmpty()) {
				scriptManipulator.sb.append(commands);
				scriptManipulator.setFilters(	// Refresh every call because of possibly language changes
					FilterCallback.of(localizer.getValue(KEY_CMD_FILES), "*.cmd")
				);
				scriptManipulator.saveFileAs();
				commands.setLength(0);
			}
		}
		else {
			scriptManipulator.newFile();
		}
	}	

	@OnAction("action:/player.pause")
	public void pause(final Hashtable<String,String[]> modes) {
		pauseOn = !pauseOn;
	}	

	@OnAction("action:/player.play")
	public void play() throws IOException {
		try{scriptManipulator.setFilters(	// Refresh every call because of possibly language changes
				FilterCallback.of(localizer.getValue(KEY_CMD_FILES), "*.cmd"), 
				FilterCallback.of(localizer.getValue(KEY_PSC_FILES), "*.psc")
			);
			
			if (scriptManipulator.openFile()) {
				processScript(scriptManipulator.file, scriptManipulator.sb.toString());
			}
		} catch (IOException | PaintScriptException exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}	

	public void playRecent(final String file) {
		try{if (scriptManipulator.openLRUFile(file)) {
				processScript(scriptManipulator.file, scriptManipulator.sb.toString());
			}
		} catch (IOException | PaintScriptException exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}	

	@OnAction("action:/debugger")
	public void debugger() {
		if (this.debugger == null) {
			this.debugger = new DebuggerPanel(xda, localizer, predef, scriptManipulator, (p)->{
												SwingUtilities.invokeLater(()->{
													getContentPane().remove(debugger);
													debugger = null;
													pack();
													((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.tools.debugger")).setEnabled(true);
												});
											});
			getContentPane().add(debugger, BorderLayout.EAST);
			pack();
			((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.tools.debugger")).setEnabled(false);
		}
	}	
	
	@OnAction("action:/settings")
    public void settings() {
		final AskSettings	as = new AskSettings(getLogger());
		
		as.fillFrom(settings.getProps());
		try{if (ApplicationUtils.ask(as, getLocalizer(), 640, 80)) {
				as.saveTo(settings.getProps());
			}
		} catch (ContentException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}	

	
	@OnAction("action:builtin:/builtin.languages")
    public void language(final Hashtable<String,String[]> langs) throws LocalizationException {
		getLocalizer().setCurrentLocale(SupportedLanguages.valueOf(langs.get("lang")[0]).getLocale());
	}	

	@OnAction("action:/overview")
	public void overview() {
		if (Desktop.isDesktopSupported()) {
			try{Desktop.getDesktop().browse(URI.create("http://localhost:"+getHelpPort()+"/static/index.cre"));
			} catch (IOException exc) {
				exc.printStackTrace();
			}
		}
	}
	
	@OnAction("action:/about")
	public void about() {
		SwingUtils.showAboutScreen(this, localizer, KEY_APPLICATION_HELP_TITLE, KEY_APPLICATION_HELP_CONTENT, URI.create("root://chav1961.bt.paint.Application/chav1961/bt/paint/avatar.jpg"), new Dimension(640, 400));
	}

	private Settings getSettings() {
		return settings;
	}
	
	private void processUndoEvents(final UndoableEditEvent e) {
		undoMgr.undoableEditHappened(e);
		refreshUndoMenuState();
	}

	private void processCommand(final ActionEvent e) {
		try{final String	command = ((JTextField)e.getSource()).getText();
				
			SwingUtils.getNearestLogger(this).message(Severity.info,predef.getPredefined(Predefines.PREDEF_SYSTEM, SystemWrapper.class).console(command, predef));
			if (recordingOn && !pauseOn) {
				commands.append(command).append('\n');
			}
			((JTextField)e.getSource()).setText("");
		} catch (PaintScriptException exc) {
			if (exc.getCause() instanceof SyntaxException) {
				((JTextField)e.getSource()).setCaretPosition((int)((SyntaxException)exc.getCause()).getCol()-1);
				SwingUtils.getNearestLogger(this).message(Severity.error, exc.getCause(), exc.getCause().getLocalizedMessage());
			}
			else {
				((JTextField)e.getSource()).setCaretPosition(0);
				SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
			}
		} catch (SyntaxException exc) {
			((JTextField)e.getSource()).setCaretPosition((int)exc.getCol()-1);
			SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
		} finally {
			panel.refreshContent();
		}
	}

	private void refreshPlayerMenuState() {
		JMenuItem	record = ((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.tools.playerbar.recording"));
		JMenu		play = ((JMenu)SwingUtils.findComponentByName(menuBar, "menu.main.tools.playerbar.play"));
		JMenuItem	pause = ((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.tools.playerbar.pause"));

		if (!record.isSelected()) {
			play.setEnabled(true);
			pause.setEnabled(false);
			pause.setSelected(false);
		}
		else {
			play.setEnabled(false);
			pause.setSelected(false);
			pause.setEnabled(true);
		}
	}
	
	private void refreshClipboardMenuState() {
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.cut")).setEnabled(panel.hasSelection());
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.copy")).setEnabled(panel.hasSelection());
		
		try{
			((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.paste")).setEnabled(ClipboardWrapper.singleton.hasImage());
		} catch (PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
	}	
	
	private void refreshUndoMenuState() {
		final JMenuItem	undo = ((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.undo"));
		final JMenuItem	redo = ((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.redo"));
		
		if (undoMgr.canUndo()) {
			undo.setEnabled(true);
			undo.setText(localizer.getValue(undoMgr.getUndoPresentationName()));
			imageManipulator.setModificationFlag();
		}
		else {
			undo.setEnabled(false);
			undo.setText(localizer.getValue(((NodeMetadataOwner)undo).getNodeMetadata().getLabelId()));
			imageManipulator.clearModificationFlag();
		}
		if (undoMgr.canRedo()) {
			redo.setEnabled(true);
			redo.setText(localizer.getValue(undoMgr.getRedoPresentationName()));
		}
		else {
			redo.setEnabled(false);
			redo.setText((((NodeMetadataOwner)redo).getNodeMetadata().getLabelId()));
		}
	}
	
	private void refreshMenuState() throws PaintScriptException {
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.file.save")).setEnabled(lastFile != null);
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.file.saveAs")).setEnabled(panel.hasImage());
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit")).setEnabled(panel.hasImage());
		refreshUndoMenuState();
		refreshClipboardMenuState();
		refreshPlayerMenuState();
	}

	private void fillTitle() throws LocalizationException, PaintScriptException {
		setTitle(String.format(localizer.getValue(KEY_APPLICATION_TITLE), 
				(imageManipulator.wasChanged() ? "*" : "") + 
				(panel.hasImage() ? (lastFile == null ? localizer.getValue(KEY_APPLICATION_MESSAGE_NEW_NAME) : "("+lastFile+")"): "")));
	}

	private void fillImageLRU(final List<String> lastUsed) {
		final JMenu	menu = ((JMenu)SwingUtils.findComponentByName(menuBar, "menu.main.file.load.lru"));
		
		if (lastUsed.isEmpty()) {
			menu.setEnabled(false);
		}
		else {
			menu.removeAll();
			for (String file : lastUsed) {
				final JMenuItem	item = new JMenuItem(file);
				
				item.addActionListener((e)->loadLRUImage(item.getText()));
				menu.add(item);
			}
			menu.setEnabled(true);
		}
	}
	
	private void fillScriptLRU(final List<String> lastUsed) {
		final JMenu	menu = ((JMenu)SwingUtils.findComponentByName(menuBar, "menu.main.tools.playerbar.play"));
		
		if (!lastUsed.isEmpty()) {
			for (int index = menu.getMenuComponentCount()-1; index > 1; index--) {
				menu.remove(index);
			}
			menu.addSeparator();
			for (String file : lastUsed) {
				final JMenuItem	item = new JMenuItem(file);
				
				item.addActionListener((e)->playRecent(item.getText()));
				menu.add(item);
			}
			menu.setEnabled(true);
		}
	}
	
	private void processImageChanges(final FileContentChangedEvent<?> e) {
		try{switch (e.getChangeType()) {
				case FILE_LOADED			:
					panel.setImage(imageManipulator.image);
					undoMgr.discardAllEdits();
					lastFile = imageManipulator.image.getName();
					refreshMenuState();
					fillTitle();
					break;
				case FILE_STORED			:
					undoMgr.discardAllEdits();
					refreshMenuState();
					fillTitle();
					break;
				case FILE_STORED_AS			:
					undoMgr.discardAllEdits();
					lastFile = imageManipulator.getCurrentNameOfTheFile();
					refreshMenuState();
					fillTitle();
					break;
				case LRU_LIST_REFRESHED		:
					fillImageLRU(imageManipulator.getLastUsed());
					break;
				case MODIFICATION_FLAG_CLEAR:
					fillTitle();
					break;
				case MODIFICATION_FLAG_SET	:
					fillTitle();
					break;
				case NEW_FILE_CREATED		:
					if (imageManipulator.fillRequired) {
						final BufferedImage	img = (BufferedImage)imageManipulator.image.getImage();
						
				    	panel.setImage(ImageUtils.process(ProcessType.FILL, img, null, new Rectangle(0, 0, img.getWidth(), img.getHeight()), panel.getCanvasBackground().getColor()));
					}
					else {
				    	panel.setImage(imageManipulator.image);
					}
					undoMgr.discardAllEdits();
			    	lastFile = null;
					refreshMenuState();
					fillTitle();
					break;
				default :
					throw new UnsupportedOperationException("Change type ["+e.getChangeType()+"] is not supported yet");
			}
		} catch (PaintScriptException exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}
	
	private void processScriptChanges(final FileContentChangedEvent<?> e) {
		try{switch (e.getChangeType()) {
				case FILE_LOADED			:
					lastScript = scriptManipulator.getCurrentNameOfTheFile();
					break;
				case FILE_STORED			:
					break;
				case FILE_STORED_AS			:
					lastScript = scriptManipulator.getCurrentNameOfTheFile();
					refreshMenuState();
					break;
				case LRU_LIST_REFRESHED		:
					fillScriptLRU(scriptManipulator.getLastUsed());
					break;
				case MODIFICATION_FLAG_CLEAR:
					break;
				case MODIFICATION_FLAG_SET	:
					break;
				case NEW_FILE_CREATED		:
			    	lastScript = null;
					refreshMenuState();
					break;
				default :
					throw new UnsupportedOperationException("Change type ["+e.getChangeType()+"] is not supported yet");
			}
		} catch (PaintScriptException exc) {
			getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}

	private void processScript(final String fileName, final String script) throws PaintScriptException {
		if (fileName.endsWith(".cmd")) {
			processConsoleScript(script);
		}
		else if (fileName.endsWith(".psc")) {
			processPaintScript(script);
		}
		else {
			getLogger().message(Severity.warning, "Unknown file extension");
		}
	}
	
	private void processConsoleScript(final String script) throws PaintScriptException {
		final SubstitutableProperties	props = new SubstitutableProperties();
		
		if (script.contains("${")) {	// Extract all keys if exist
			CharUtils.substitute("", script, (k)->{props.setProperty(k, ""); return "";});
			
			final AskSubstitutionTable		table = new AskSubstitutionTable(localizer, props);
			
			table.setPreferredSize(new Dimension(300, 60));
			table.requestFocusInWindow();
			switch (new JLocalizedOptionPane(localizer).confirm(this, table, KEY_APPLICATION_PLAYER_ENTER_SUBSTITUTIONS, JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION)) {
				case JOptionPane.OK_OPTION :
					break;
				default :
					return;
			}		
		}
		
		final Thread	t = new Thread(()->{
							for (String item : script.split("\n")) {
								try{
									getLogger().message(Severity.info, item); 
									getLogger().message(Severity.info, predef.getPredefined(Predefines.PREDEF_SYSTEM, SystemWrapper.class).console(CharUtils.substitute("script", item, (k)->props.getProperty(k)), predef));
								} catch (PaintScriptException | SyntaxException exc) {
									getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
								}
							}
							panel.refreshContent();
						});
		t.setDaemon(true);
		t.setName("script-executor");
		t.start();
	}

	private void processPaintScript(final String script) {
		final Thread	t = new Thread(()->{
							for (String item : script.split("\n")) {
								try{
									predef.getPredefined(Predefines.PREDEF_SYSTEM, SystemWrapper.class).console(item, predef);
								} catch (PaintScriptException | SyntaxException exc) {
									getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
								}
							}
						});
		t.setDaemon(true);
		t.setName("script-executor");
		t.start();
	}

	private void assignBatchSource(final Exchanger<Object> ex) {
		this.batchSource = ex;
	}

	private void processFilter(final float[] filter) {
		try{panel.startImageAction(KEY_UNDO_FILTER, KEY_REDO_FILTER);
			panel.processFilter(filter);
			panel.endImageAction(KEY_UNDO_FILTER, KEY_REDO_FILTER);	
		} catch (PaintScriptException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}
	
	private void fillLocalizedStrings() {
		try{
			fillTitle();
		} catch (PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
	}

	public static void main(String[] args) {
		System.exit(callMain(args));
	}
	
	static int callMain(String[] args) {
		final ArgParser					parser = new ApplicationArgParser();
		
		try{final ArgParser				parsed = parser.parse(args);
			try(final InputStream				is = ImageEditPanel.class.getResourceAsStream("imageeditpanel.xml");
				final Predefines				predef = new Predefines(args)) {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				final Localizer					localizer = Localizer.Factory.newInstance(xda.getRoot().getLocalizerAssociated());
				
				PureLibSettings.PURELIB_LOCALIZER.push(localizer);
				
				if (args.length == 0) {
					startGUI(ApplicationMode.GUI, xda, localizer, predef);
				}
				else if (!parsed.isTyped(ARG_COMMAND)) {
					throw new CommandLineParametersException("Mandatory parameter ["+ARG_COMMAND+"] is missing");
				}
				else {
					final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>>	root;
					final String	commands;	
					
					try(final FileSystemInterface	fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"))){
						
						predef.putPredefined(Predefines.PREDEF_SYSTEM, new SystemWrapperImpl(fsi, new File("./").getAbsoluteFile().toURI()));
						predef.putPredefined(Predefines.PREDEF_CANVAS, new CanvasWrapperImpl());
						
						if (parsed.getValue(ARG_COMMAND, String.class).startsWith("@")) {
							try(final Reader	rdr = new FileReader(parsed.getValue(ARG_COMMAND, String.class).substring(1))) {
								if (parsed.getValue(ARG_COMMAND, String.class).endsWith(".psc")) {
									root = ScriptUtils.compile(rdr);
									commands = null;
								}
								else {
									root = null;
									commands = Utils.fromResource(rdr);
								}
							}
						}
						else {
							root = null;
							commands = parsed.getValue(ARG_COMMAND, String.class);
						}
	
						if (parsed.isTyped(ARG_INPUT_MASK)) {
							final String			mask = parsed.getValue(ARG_INPUT_MASK, String.class);
							final int				index = mask.lastIndexOf('/');
							final Exchanger<Object>	ex = new Exchanger<>();
							final Thread			t, current = Thread.currentThread();
							

							if (parsed.getValue(ARG_GUI_FLAG, boolean.class)) {
								//startGUI(ApplicationMode.BATCH, xda, localizer, predef);
								t = null;
								
							}
							else {
								t = new Thread(()->{
									try{
										for(;;) {
											final ImageWrapper iw = (ImageWrapper) ex.exchange(null);
											
											try(final FileOutputStream	fos = new FileOutputStream(iw.getName())) {
												ImageIO.write((RenderedImage) iw.getImage(), iw.getFormat(), fos);
												fos.flush();
											} catch (IOException | PaintScriptException exc) {
												System.err.println("Error saving ["+iw.getName()+"]: "+exc.getLocalizedMessage());
											}
										}
									} catch (InterruptedException e) {
									} catch (Exception exc) {
										current.interrupt();
									}
								});
							}
				
							try {
								if (index > 0) {
									walk(new File(mask.substring(0,index)), Pattern.compile(Utils.fileMask2Regex(mask.substring(index+1))), parsed.getValue(ARG_RECURSION_FLAG, boolean.class), root, commands, predef, ex);
								}
								else {
									walk(new File("./"), Pattern.compile(Utils.fileMask2Regex(mask)), parsed.getValue(ARG_RECURSION_FLAG, boolean.class), root, commands, predef, ex);
								}
								ex.exchange(null);
								t.join();
							} catch (InterruptedException exc) {
							}
						}
						else {
							if (parsed.getValue(ARG_GUI_FLAG, boolean.class)) {
								final Image	image = ImageIO.read(System.in);
								
								if (root != null) {
									processImage(ImageWrapper.of((BufferedImage)image), root, predef);
								}
								else {
									processImage(ImageWrapper.of((BufferedImage)image), commands, predef);
								}
								startGUI(ApplicationMode.IN_OUT, xda, localizer, predef);
							}
							else {
								if (root != null) {
									processImage(System.in, root, predef, System.out);
								}
								else {
									processImage(System.in, commands, predef, System.out);
								}
							}
						}
					}
				}
				
				PureLibSettings.PURELIB_LOCALIZER.pop(localizer);
				return 0;
			} catch (SyntaxException | PaintScriptException e) {
				e.printStackTrace();
				return 129;
			} catch (IOException | EnvironmentException | InterruptedException e) {
				e.printStackTrace();
				return 129;
			}	
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.err.println(parser.getUsage("bt.paint"));
			return 128;
		}
	}

	private static boolean walk(final File current, final Pattern mask, final boolean recursive, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType, ?>> root, final String commands, final Predefines predef, final Exchanger<Object> ex) throws IOException, PaintScriptException, InterruptedException {
		if (current.isFile()) {
			try(final InputStream	is = new FileInputStream(current)) {
				final ImageWrapper	iw = new ImageWrapperImpl(is);
				
				if (root != null) {
					processImage(iw, root, predef);
				}
				else {
					processImage(iw, commands, predef);
				}
				final Object	answer = ex.exchange(iw);
				
				return answer == null;
			}
		}
		else {
			final File[]	content = current.listFiles();
			
			if (content != null) {
				for (File item : content) {
					if (item.isFile() && mask.matcher(item.getName()).matches()) {
						if (!walk(item, mask, recursive, root, commands, predef, ex)) {
							return false;
						}
					}
				}
				if (recursive) {
					for (File item : content) {
						if (item.isDirectory()) {
							if (walk(item, mask, recursive, root, commands, predef, ex)) {
								return false;
							}
						}
					}
				}
			}
			return true;
		}
	}

	private static void processImage(final InputStream is, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>> root, final Predefines predef, final OutputStream os) throws IOException, PaintScriptException {
		final ImageWrapper		iw = new ImageWrapperImpl(is);

		processImage(iw, root, predef);
		ImageIO.write((RenderedImage) iw.getImage(), iw.getFormat(), os);
		os.flush();
	}

	private static void processImage(final InputStream is, final String commands, final Predefines predef, final OutputStream os) throws IOException, PaintScriptException {
		final ImageWrapper		iw = new ImageWrapperImpl(is);

		iw.setFormat("png");
		processImage(iw, commands, predef);
		ImageIO.write((RenderedImage) predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).getImage().getImage(), iw.getFormat(), os);
		os.flush();
	}
	
	private static void processImage(final ImageWrapper image, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>> root, final Predefines predef) throws IOException {
	}	

	private static void processImage(final ImageWrapper image, final String commands, final Predefines predef) throws IOException {
		try{predef.getPredefined(Predefines.PREDEF_CANVAS, CanvasWrapper.class).setImage(image);
		
			for (String item : commands.split("\n")) {
				predef.getPredefined(Predefines.PREDEF_SYSTEM, SystemWrapper.class).console(item, predef);
			}
		} catch (SyntaxException | PaintScriptException exc) {
			throw new IOException(exc);
		}
	}	
	
	private static void startGUI(final ApplicationMode mode, final ContentMetadataInterface xda, final Localizer localizer, final Predefines predef) throws InterruptedException, IOException, PaintScriptException {
		final CountDownLatch	latch = new CountDownLatch(1);
		
		try(final Application			app = new Application(mode, xda, localizer, predef, latch);
			final NanoServiceFactory	service = new NanoServiceFactory(PureLibSettings.SYSTEM_ERR_LOGGER, app.getSettings().getProps())) {

			service.start();
			app.setHelpPort(service.getServerAddress().getPort());
			app.setVisible(true);
			latch.await();
			service.stop();
		} catch (ContentException exc) {
			throw new PaintScriptException(exc);
		}
	}
	
	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new StringArg(ARG_COMMAND, false, true, "Command to process content"),
			new StringArg(ARG_INPUT_MASK, false, false, "Mask for input files. If typed, all content will be get from file system instead of stdin"),
			new BooleanArg(ARG_RECURSION_FLAG, false, "Process input mask recursively, if types", false),
			new BooleanArg(ARG_GUI_FLAG, false, "Start GUI after processing. If any arguments are missing, starts GUI automatically", false),
			new FileArg(ARG_PROPFILE_LOCATION, false, "Property file location", "./.bt.paint.properties"),
			new IntegerArg(NanoServiceFactory.NANOSERVICE_PORT, false, "Process input mask recursively, if types", 0),
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}
	
	private static class Settings implements LRUPersistence {
		private final SubstitutableProperties	props;
		private final File						file;
		
		private Settings(final File config) throws IOException {
			this.file = config;
			this.props = new SubstitutableProperties();
			
			props.setProperty(NanoServiceFactory.NANOSERVICE_PORT, "0");
			props.setProperty(NanoServiceFactory.NANOSERVICE_ROOT, FileSystemInterface.FILESYSTEM_URI_SCHEME+":xmlReadOnly:root://"+Application.class.getCanonicalName()+"/chav1961/bt/paint/helptree.xml");
			props.setProperty(NanoServiceFactory.NANOSERVICE_CREOLE_PROLOGUE_URI, Application.class.getResource("prolog.cre").toString()); 
			props.setProperty(NanoServiceFactory.NANOSERVICE_CREOLE_EPILOGUE_URI, Application.class.getResource("epilog.cre").toString());
			
			if (config.exists() && config.isFile() && config.canRead()) {
				try(final InputStream	is = new FileInputStream(config)) {
					props.load(is);
				}
			}
		}

		@Override
		public void loadLRU(final List<String> lru) throws IOException {
			throw new UnsupportedOperationException("Don't use this call, use loadLRU(String,List<String>)"); 
		}

		@Override
		public void loadLRU(final String name, final List<String> lru) throws IOException {
			for (int index = 1; props.containsKey(name+'.'+index); index++) {
				lru.add(props.getProperty(name+'.'+index));
			}
		}
	 
		@Override
		public void saveLRU(final List<String> lru) throws IOException {
			throw new UnsupportedOperationException("Don't use this call, use saveLRU(String,List<String>)"); 
		}

		@Override
		public void saveLRU(final String name, final List<String> lru) throws IOException {
			for (int index = 1; index <= lru.size(); index++) {
				props.setProperty(name+'.'+index, lru.get(index-1));
			}
			try(final OutputStream	os = new FileOutputStream(file)) {
				props.store(os, null);
				os.flush();
			}
		}
		
		public SubstitutableProperties getProps() {
			return props; 
		}
	}
	
	private static class ImageManipulator extends JFileContentManipulator implements LoggerFacadeOwner {
		private final LoggerFacade	logger;
		private ImageWrapper		image;
		private boolean				fillRequired;
		
		public ImageManipulator(final LoggerFacade logger, FileSystemInterface fsi, Localizer localizer, InputStreamGetter getterIn, OutputStreamGetter getterOut, LRUPersistence persistence) throws NullPointerException {
			super("image", fsi, localizer, getterIn, getterOut, persistence);
			this.logger = logger;
		}

		@Override
		public LoggerFacade getLogger() {
			return logger;
		}
		
		@Override
		protected boolean processNew(final ProgressIndicator progress) throws IOException {
			final AskImageSize	ais = new AskImageSize(getLogger());
			
			try{if (ApplicationUtils.ask(ais, getLocalizer(), 240,100)) {
					image = ImageWrapper.of(new BufferedImage(ais.width, ais.height, BufferedImage.TYPE_INT_ARGB));
					fillRequired = ais.fillBackgroung;
					return true;
				}
				else {
					return false;
				}
			} catch (ContentException exc) {
				getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
				return false;
			}
		}

		@Override
		protected boolean processLoad(final String fileName, final InputStream source, final ProgressIndicator progress) throws IOException {
			image = new ImageWrapperImpl(source);
			image.setName(fileName);
			return true;
		}
		
		@Override
		protected boolean processStore(final String fileName, final OutputStream target, final ProgressIndicator progress) throws IOException {
			try{ImageIO.write((RenderedImage)image.getImage(), image.getFormat(), target);
				return true;
			} catch (PaintScriptException e) {
				throw new IOException(e); 
			}
		}
	}

	private static class ScriptManipulator extends JFileContentManipulator implements LoggerFacadeOwner {
		private final LoggerFacade	logger;
		private final StringBuilder	sb = new StringBuilder();
		private String				file;
		
		public ScriptManipulator(final LoggerFacade logger, FileSystemInterface fsi, Localizer localizer, InputStreamGetter getterIn, OutputStreamGetter getterOut, LRUPersistence persistence) throws NullPointerException {
			super("script", fsi, localizer, getterIn, getterOut, persistence);
			this.logger = logger;
		}

		@Override
		public LoggerFacade getLogger() {
			return logger;
		}
		
		@Override
		protected boolean processNew(final ProgressIndicator progress) throws IOException {
			sb.setLength(0);
			file = "";
			return true;
		}

		@Override
		protected boolean processLoad(final String fileName, final InputStream source, final ProgressIndicator progress) throws IOException {
			sb.setLength(0);
			sb.append(Utils.fromResource(new InputStreamReader(source, PureLibSettings.DEFAULT_CONTENT_ENCODING)));
			file = fileName;
			return true;
		}
		
		@Override
		protected boolean processStore(final String fileName, final OutputStream target, final ProgressIndicator progress) throws IOException {
			final Writer	wr = new OutputStreamWriter(target, PureLibSettings.DEFAULT_CONTENT_ENCODING);
			
			wr.write(sb.toString());
			wr.flush();
			return true;
		}
	}
}
