package chav1961.bt.paint;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.UndoableEditEvent;
import javax.swing.undo.UndoManager;

import chav1961.bt.paint.control.ImageEditPanel;
import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.control.ImageUtils.ProcessType;
import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.dialogs.AskImageSize;
import chav1961.bt.paint.dialogs.AskScriptSave;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.ImageWrapperImpl;
import chav1961.bt.paint.script.ScriptNodeType;
import chav1961.bt.paint.script.SystemWrapperImpl;
import chav1961.bt.paint.script.interfaces.ClipboardWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.bt.paint.script.intern.runtime.ScriptUtils;
import chav1961.bt.paint.utils.ApplicationUtils;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
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
	private static final String		KEY_UNSAVED_CHANGES = "chav1961.bt.paint.Application.unsavedChanges";
	private static final String		KEY_UNSAVED_CHANGES_TITLE = "chav1961.bt.paint.Application.unsavedChanges.title";
	private static final String		KEY_APPLICATION_TITLE = "chav1961.bt.paint.Application.title";
	private static final String		KEY_APPLICAITON_MESSAGE_READY = "chav1961.bt.paint.Application.message.ready";
	private static final String		KEY_APPLICATION_HELP_TITLE = "chav1961.bt.paint.Application.help.title";
	private static final String		KEY_APPLICATION_HELP_CONTENT = "chav1961.bt.paint.Application.help.content";

	private final FileSystemInterface		fsi;
	private final ContentMetadataInterface	xda;
	private final Localizer					localizer;
	private final Predefines				predef;
	private final CountDownLatch			latch;
	private final JMenuBar					menuBar;
	private final ImageEditPanel			panel;
	private final JStateString				state;
	private final UndoManager				undoMgr = new UndoManager();
	private final StringBuilder				commands = new StringBuilder(); 
	private final JFileContentManipulator	imageManipulator;
	private final JFileContentManipulator	scriptManipulator;
	private final Settings					settings = new Settings(new File("./.bt.paint"));
		
	private String							lastFile = null;
	private boolean							recordingOn = false, pauseOn = false;
	
	public Application(final ContentMetadataInterface xda, final Localizer localizer, final Predefines predef, final CountDownLatch latch) throws IOException, PaintScriptException {
		if (xda == null) {
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
			this.xda = xda;
			this.localizer = localizer;
			this.predef = predef;
			this.latch = latch;
			this.fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"));
			
	        this.menuBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
			this.imageManipulator = new JFileContentManipulator("image", this.fsi, this.localizer
										,()->{return new InputStream() {@Override public int read() throws IOException {return -1;}};}
										,()->{return new OutputStream() {@Override public void write(int b) throws IOException {}};},
										settings
										);
			this.imageManipulator.addFileContentChangeListener((e)->processImageChanges(e));
			this.scriptManipulator = new JFileContentManipulator("script", this.fsi, this.localizer
										,()->{return new InputStream() {@Override public int read() throws IOException {return -1;}};}
										,()->{return new OutputStream() {@Override public void write(int b) throws IOException {}};},
										settings
										);
			this.imageManipulator.addFileContentChangeListener((e)->processScriptChanges(e));
	        
	        this.panel = new ImageEditPanel(localizer);
	        this.state = new JStateString(localizer);
	        
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
	        refreshMenuState();
	        localizer.addLocaleChangeListener(this);
	        
	        predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).addChangeListener((e)->{
	    		try{((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.paste")).setEnabled(predef.getPredefined(Predefines.PREDEF_CLIPBOARD, ClipboardWrapper.class).hasImage());
				} catch (PaintScriptException exc) {
					getLogger().message(Severity.error, exc.getLocalizedMessage());
				}
	        });
	        
			SwingUtils.assignExitMethod4MainWindow(this,()->exit());
			SwingUtils.centerMainWindow(this, 0.85f);
			fillLocalizedStrings();
			state.message(Severity.info, KEY_APPLICAITON_MESSAGE_READY);
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		scriptManipulator.close();
		imageManipulator.close();
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

	@OnAction("action:/newImage")
    public void newImage() {
		if (checkUnsavedChanges()) {
			final AskImageSize	ais = new AskImageSize(getLogger());
			
			if (ask(ais,240,100)) {
				try {
					final BufferedImage	img = new BufferedImage(ais.width, ais.height, BufferedImage.TYPE_INT_ARGB);
					
					if (ais.fillBackgroung) {
				    	panel.setImage(ImageUtils.process(ProcessType.FILL, img, null, new Rectangle(0, 0, ais.width, ais.height), panel.getCanvasBackground()));
					}
					else {
				    	panel.setImage(img);
					}
					undoMgr.discardAllEdits();
			    	lastFile = null;
					refreshMenuState();
					fillTitle();
				} catch (PaintScriptException exc) {
					getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
				}
			}
		}
    }
    
	@OnAction("action:/loadImage")
    public void loadImage() {
		if (checkUnsavedChanges()) {
			try{for (String item : JFileSelectionDialog.select(this, getLocalizer(), fsi, JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS | JFileSelectionDialog.OPTIONS_FOR_OPEN, 
									FilterCallback.of(localizer.getValue(KEY_PNG_FILES), "*.png"), 
									FilterCallback.of(localizer.getValue(KEY_JPG_FILES), "*.jpg"),
									FilterCallback.of(localizer.getValue(KEY_GIF_FILES), "*.gif"),
									FilterCallback.of(localizer.getValue(KEY_BMP_FILES), "*.bmp"))) {
					try(final FileSystemInterface	temp = fsi.clone().open(item);
						final InputStream			is = temp.read()) {
						
						panel.setImage(ImageIO.read(is));
						undoMgr.discardAllEdits();
						lastFile = item;
						refreshMenuState();
						fillTitle();
						return;
					}
				}
			} catch (IOException | PaintScriptException exc) {
				getLogger().message(Severity.error, exc, exc.getLocalizedMessage());
			}
		}
    }
    
	@OnAction("action:/saveImage")
    public void saveImage() {
		try(final FileSystemInterface	temp = fsi.clone().open(lastFile)) {
			if (temp.exists() && temp.isFile()) {
				try(final OutputStream	os = temp.write()) {
					ImageIO.write((RenderedImage) panel.getImage().getImage(), lastFile.endsWith(".png") ? "png" : "jpeg", os);
				}
				undoMgr.discardAllEdits();
				refreshMenuState();
			}
		} catch (IOException| PaintScriptException  e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
    }
    
	@OnAction("action:/saveImageAs")
    public void saveImageAs() {
		try{for (String item : JFileSelectionDialog.select(this, getLocalizer(), fsi, JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_ALLOW_DELETE | JFileSelectionDialog.OPTIONS_CONFIRM_REPLACEMENT | JFileSelectionDialog.OPTIONS_FOR_SAVE, 
								FilterCallback.of(localizer.getValue(KEY_PNG_FILES), "*.png"), 
								FilterCallback.of(localizer.getValue(KEY_JPG_FILES), "*.jpg"),
								FilterCallback.of(localizer.getValue(KEY_GIF_FILES), "*.gif"),
								FilterCallback.of(localizer.getValue(KEY_BMP_FILES), "*.bmp"))) {
				try(final FileSystemInterface	temp = fsi.clone().open(item)) {
					if (!temp.exists()) {
						temp.create();
					}
					try(final OutputStream	os = temp.write()) {
						ImageIO.write((RenderedImage) panel.getImage().getImage(), item.endsWith(".png") ? "png" : "jpeg", os);
					}
					undoMgr.discardAllEdits();
					lastFile = item;
					refreshMenuState();
					fillTitle();
					return;
				}
			}
		} catch (IOException | PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
    }

	@OnAction("action:/exit")
    public void exit() {
		if (checkUnsavedChanges()) {
			latch.countDown();
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
		try{ClipboardWrapper.singleton.setImage(ImageWrapper.of(panel.cutSelectedImage()));
		} catch (PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
	}	

	@OnAction("action:/copy")
    public void copy() {
		try{ClipboardWrapper.singleton.setImage(ImageWrapper.of(panel.getSelectedImage()));
		} catch (PaintScriptException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
	}	

	@OnAction("action:/paste")
    public void paste() {
	}	
	
	@OnAction("action:/find")
    public void find() {
	}	
	
	@OnAction("action:/replace")
    public void replace() {
	}

	@OnAction("action:/player.recording")
	public void recording(final Hashtable<String,String[]> modes) {
		recordingOn = !recordingOn;
		pauseOn = false;
		refreshPlayerMenuState();
		
		if (!recordingOn && !commands.isEmpty()) {
			final AskScriptSave	ascs = new AskScriptSave(state);
			
			try{if (ApplicationUtils.ask(ascs, localizer, 100, 200)) {
					if (ascs.needSave) {
						try(final FileSystemInterface	temp = fsi.clone().open(ascs.store.getAbsolutePath()).create()) {
							try(final Writer	wr = temp.charWrite()) {
								wr.write(commands.toString());
								wr.flush();
							}
						}
						getLogger().message(Severity.info, "stored");
					}
				}
				
			} catch (ContentException | IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			} finally {
				commands.setLength(0);
			}
		}
	}	

	@OnAction("action:/player.pause")
	public void pause(final Hashtable<String,String[]> modes) {
		pauseOn = !pauseOn;
	}	

	@OnAction("action:/player.play")
	public void play() {
	}	

	@OnAction("action:/player.playRecent")
	public void playRecent(final Hashtable<String,String[]> modes) {
	}	
	
	@OnAction("action:/settings")
    public void settings() {
	}	

	@OnAction("action:builtin:/builtin.languages")
    public void language(final Hashtable<String,String[]> langs) throws LocalizationException {
		getLocalizer().setCurrentLocale(SupportedLanguages.valueOf(langs.get("lang")[0]).getLocale());
	}	

	@OnAction("action:/overview")
	public void overview() {
	}
	
	@OnAction("action:/about")
	public void about() {
		SwingUtils.showAboutScreen(this, localizer, KEY_APPLICATION_HELP_TITLE, KEY_APPLICATION_HELP_CONTENT, URI.create("root://chav1961.bt.paint.Application/chav1961/bt/paint/avatar.jpg"), new Dimension(300,300));
	}
	
	private <T> boolean ask(final T instance, final int width, final int height) {
		try{
			return ApplicationUtils.ask(instance, localizer, width, height);
		} catch (ContentException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
			return false;
		} 
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
		} catch (PaintScriptException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
		} finally {
			((JTextField)e.getSource()).setText("");
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
		}
		else {
			undo.setEnabled(false);
			undo.setText(localizer.getValue(((NodeMetadataOwner)undo).getNodeMetadata().getLabelId()));
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

	private boolean checkUnsavedChanges() {
		if (undoMgr.canUndoOrRedo()) {
			switch (new JLocalizedOptionPane(localizer).confirm(this, KEY_UNSAVED_CHANGES, KEY_UNSAVED_CHANGES_TITLE, JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION)) {
				case JOptionPane.YES_OPTION 	:
					if (lastFile != null) {
						saveImage();
					}
					else {
						saveImageAs();
					}
				case JOptionPane.NO_OPTION		:
					return true;
				case JOptionPane.CANCEL_OPTION	:
					return false;
				default :
					throw new UnsupportedOperationException(); 
			}
		}
		else {
			return true;
		}
	}
	
	private void fillTitle() throws LocalizationException, PaintScriptException {
		setTitle(String.format(localizer.getValue(KEY_APPLICATION_TITLE),panel.hasImage() ? (lastFile == null ? "(*)" : "("+lastFile+")"): ""));
	}

	private void processImageChanges(final FileContentChangedEvent<?> e) {
		// TODO Auto-generated method stub
		switch (e.getChangeType()) {
			case FILE_LOADED			:
				break;
			case FILE_STORED			:
				break;
			case FILE_STORED_AS			:
				break;
			case LRU_LIST_REFRESHED		:
				break;
			case MODIFICATION_FLAG_CLEAR:
				break;
			case MODIFICATION_FLAG_SET	:
				break;
			case NEW_FILE_CREATED		:
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+e.getChangeType()+"] is not supported yet");
		}
	}

	private void processScriptChanges(final FileContentChangedEvent<?> e) {
		// TODO Auto-generated method stub
		switch (e.getChangeType()) {
			case FILE_LOADED			:
				break;
			case FILE_STORED			:
				break;
			case FILE_STORED_AS			:
				break;
			case LRU_LIST_REFRESHED		:
				break;
			case MODIFICATION_FLAG_CLEAR:
				break;
			case MODIFICATION_FLAG_SET	:
				break;
			case NEW_FILE_CREATED		:
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+e.getChangeType()+"] is not supported yet");
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
		final ArgParser					parser = new ApplicationArgParser();
		
		try{final ArgParser				parsed = parser.parse(args);
			try(final InputStream				is = ImageEditPanel.class.getResourceAsStream("imageeditpanel.xml");
				final Predefines				predef = new Predefines(args)) {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				final Localizer					localizer = Localizer.Factory.newInstance(xda.getRoot().getLocalizerAssociated());
				
				PureLibSettings.PURELIB_LOCALIZER.push(localizer);
				
				if (args.length == 0) {
					startGUI(xda, localizer, predef);
				}
				else if (!parsed.isTyped(ARG_COMMAND)) {
					throw new CommandLineParametersException("Mandatory parameter ["+ARG_COMMAND+"] is missing");
				}
				else {
					final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>>	root;
					
					try(final FileSystemInterface	fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"))){
						
						predef.putPredefined(Predefines.PREDEF_SYSTEM, new SystemWrapperImpl(fsi, null));
						
						try(final Reader	rdr = loadCommandScript(parsed.getValue(ARG_COMMAND, String.class))) {
							root = ScriptUtils.compile(rdr); 
						}
	
						if (parsed.isTyped(ARG_INPUT_MASK)) {
							final String	mask = parsed.getValue(ARG_INPUT_MASK, String.class);
							final int		index = mask.lastIndexOf('/');
	
							if (index > 0) {
								walk(new File(mask.substring(0,index)), Pattern.compile(Utils.fileMask2Regex(mask.substring(index+1))), parsed.getValue(ARG_RECURSION_FLAG, boolean.class), root);
							}
							else {
								walk(new File("./"), Pattern.compile(Utils.fileMask2Regex(mask)), parsed.getValue(ARG_RECURSION_FLAG, boolean.class), root);
							}
						}
						else {
							if (parsed.getValue(ARG_GUI_FLAG, boolean.class)) {
								final Image	image = ImageIO.read(System.in);
								
								processImage(ImageWrapper.of(image), root);
								startGUI(xda, localizer, predef);
							}
							else {
								processImage(System.in, root, System.out);
							}
						}
					}
				}
				
				PureLibSettings.PURELIB_LOCALIZER.pop(localizer);
			} catch (SyntaxException | PaintScriptException e) {
				e.printStackTrace();
				System.exit(129);
			} catch (IOException | EnvironmentException | InterruptedException e) {
				e.printStackTrace();
				System.exit(129);
			}	
		} catch (CommandLineParametersException exc) {
			System.err.println(exc.getLocalizedMessage());
			System.err.println(parser.getUsage("bt.paint"));
			System.exit(128);
		}
	}

	private static Reader loadCommandScript(final String value) throws SyntaxException, FileNotFoundException {
		if (value.startsWith("@")) {
			final File	f = new File(value.substring(1));
			
			if (!f.exists()) {
				throw new SyntaxException(0, 1, "Script file ["+f.getAbsolutePath()+"] not exists"); 
			}
			else if (f.isDirectory()) {
				throw new SyntaxException(0, 1, "Script ["+f.getAbsolutePath()+"] is directory, not a file"); 
			}
			else if (!f.canRead()) {
				throw new SyntaxException(0, 1, "Script file ["+f.getAbsolutePath()+"] is not accessible for you"); 
			}
			else {
				return new FileReader(f);
			}
		}
		else {
			return new StringReader(value);
		}
	}
	
	private static void walk(final File current, final Pattern mask, final boolean recursive, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType, ?>> root) throws IOException, PaintScriptException {
		if (current.isFile()) {
			try(final InputStream	is = new FileInputStream(current);
				final OutputStream	os = new FileOutputStream(new File(current.getAbsolutePath()+".processed"))) {
				
				processImage(is, root, os);
			}
		}
		else {
			final File[]	content = current.listFiles();
			
			if (content != null) {
				for (File item : content) {
					if (item.isFile() && mask.matcher(item.getName()).matches()) {
						walk(item, mask, recursive, root);
					}
				}
				if (recursive) {
					for (File item : content) {
						if (item.isDirectory()) {
							walk(item, mask, recursive, root);
						}
					}
				}
			}
		}
	}

	private static void processImage(final InputStream is, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>> root, final OutputStream os) throws IOException, PaintScriptException {
		final ImageWrapper		iw = new ImageWrapperImpl(is);

		processImage(iw, root);
		ImageIO.write((RenderedImage) iw.getImage(), iw.getFormat(), os);
		os.flush();
	}

	private static void processImage(final ImageWrapper image, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>> root) throws IOException {
	}	
	
	private static void startGUI(final ContentMetadataInterface xda, final Localizer localizer, final Predefines predef) throws InterruptedException, IOException, PaintScriptException {
		final CountDownLatch			latch = new CountDownLatch(1);
		final Application				app = new Application(xda, localizer, predef, latch);
		
		app.setVisible(true);
		latch.await();
		app.dispose();
	}
	
	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new StringArg(ARG_COMMAND, false, true, "Command to process content"),
			new StringArg(ARG_INPUT_MASK, false, false, "Mask for input files. If typed, all content will be get from file system instead of stdin"),
			new BooleanArg(ARG_RECURSION_FLAG, false, "Process input mask recursively, if types", false),
			new BooleanArg(ARG_GUI_FLAG, false, "Start GUI after processing. If any arguments are missing, starts GUI automatically", false),
			new FileArg(ARG_PROPFILE_LOCATION, false, "Property file location", "./.bt.paint.properties"),
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
	}
}
