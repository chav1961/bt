package chav1961.bt.paint;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
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
import java.net.URI;
import java.util.Hashtable;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.EtchedBorder;

import chav1961.bt.paint.control.ImageEditPanel;
import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.dialogs.AskImageSize;
import chav1961.bt.paint.script.ScriptNodeType;
import chav1961.bt.paint.script.runtime.ScriptUtils;
import chav1961.bt.paint.utils.ApplicationUtils;
import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
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
import chav1961.purelib.ui.interfaces.FormManager;
import chav1961.purelib.ui.swing.AutoBuiltForm;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.JStateString;

public class Application extends JFrame implements NodeMetadataOwner, LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner {
	public static final String		ARG_COMMAND = "command";
	public static final String		ARG_INPUT_MASK = "inputMask";
	public static final String		ARG_RECURSION_FLAG = "recursion";
	public static final String		ARG_GUI_FLAG = "guiFlag";
	public static final String		ARG_PROPFILE_LOCATION = "prop";
	
	private static final long 		serialVersionUID = 1083999598002477077L;
	private static final int		MAX_UNDO_LENGTH = 10;
	private static final String		KEY_PNG_FILES = "chav1961.bt.paint.Application.filter.pngfiles";
	private static final String		KEY_JPG_FILES = "chav1961.bt.paint.Application.filter.jpgfiles";
	private static final String		KEY_UNSAVED_CHANGES = "chav1961.bt.paint.Application.unsavedChanges";
	private static final String		KEY_UNSAVED_CHANGES_TITLE = "chav1961.bt.paint.Application.unsavedChanges.title";
	private static final String		KEY_APPLICATION_TITLE = "chav1961.bt.paint.Application.title";
	private static final String		KEY_APPLICAITON_MESSAGE_READY = "chav1961.bt.paint.Application.message.ready";
	private static final String		KEY_APPLICATION_HELP_TITLE = "chav1961.bt.paint.Application.help.title";
	private static final String		KEY_APPLICATION_HELP_CONTENT = "chav1961.bt.paint.Application.help.content";

	private final FileSystemInterface		fsi;
	private final ContentMetadataInterface	xda;
	private final Localizer					localizer;
	private final CountDownLatch			latch;
	private final JMenuBar					menuBar;
	private final ImageEditPanel			panel;
	private final JStateString				state;
	private final FilterCallback			filterCallbackPNG;
	private final FilterCallback			filterCallbackJPG;
		
	private String							lastFile = null;
	
	public Application(final ContentMetadataInterface xda, final Localizer localizer, final CountDownLatch latch) throws IOException {
		if (xda == null) {
			throw new NullPointerException("Metadata can't be null"); 
		}
		else if (localizer == null) {
			throw new NullPointerException("Localizer can't be null"); 
		}
		else if (latch == null) {
			throw new NullPointerException("Latch can't be null"); 
		}
		else {
			this.xda = xda;
			this.localizer = localizer;
			this.latch = latch;
			this.fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"));
			this.filterCallbackPNG = FilterCallback.of(localizer.getValue(KEY_PNG_FILES), "*.png");
			this.filterCallbackJPG = FilterCallback.of(localizer.getValue(KEY_JPG_FILES), "*.jpg");
			
	        this.menuBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
	        SwingUtils.assignActionListeners(menuBar, this);
			
	        this.panel = new ImageEditPanel(localizer, MAX_UNDO_LENGTH);
	        this.state = new JStateString(localizer);
	        
	        state.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

	        getContentPane().add(menuBar, BorderLayout.NORTH);
	        getContentPane().add(panel, BorderLayout.CENTER);
	        getContentPane().add(state, BorderLayout.SOUTH);
	        
	        panel.addChangeListener((e)->refreshMenuState());
	        refreshMenuState();
	        localizer.addLocaleChangeListener(this);
	        
			SwingUtils.assignExitMethod4MainWindow(this,()->exit());
			SwingUtils.centerMainWindow(this, 0.85f);
			fillLocalizedStrings();
			state.message(Severity.info, KEY_APPLICAITON_MESSAGE_READY);
		}
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
			
			if (ask(ais,240,80)) {
		    	panel.setImage(new BufferedImage(ais.width, ais.height, BufferedImage.TYPE_INT_ARGB));
		    	lastFile = null;
				refreshMenuState();
				fillTitle();
			}
		}
    }
    
	@OnAction("action:/loadImage")
    public void loadImage() {
		if (checkUnsavedChanges()) {
			try{for (String item : JFileSelectionDialog.select(this, getLocalizer(), fsi, JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_FILE_MUST_EXISTS | JFileSelectionDialog.OPTIONS_FOR_OPEN, filterCallbackPNG, filterCallbackJPG)) {
					try(final FileSystemInterface	temp = fsi.clone().open(item);
						final InputStream			is = temp.read()) {
						
						panel.setImage(ImageIO.read(is));
						lastFile = item;
						refreshMenuState();
						fillTitle();
						return;
					}
				}
			} catch (IOException e) {
				getLogger().message(Severity.error,e.getLocalizedMessage());
			}
		}
    }
    
	@OnAction("action:/saveImage")
    public void saveImage() {
		try(final FileSystemInterface	temp = fsi.clone().open(lastFile)) {
			if (temp.exists() && temp.isFile()) {
				try(final OutputStream	os = temp.write()) {
					ImageIO.write((RenderedImage) panel.getImage(), lastFile.endsWith(".png") ? "png" : "jpeg", os);
				}
				panel.getUndoManager().discardAllEdits();
				refreshMenuState();
			}
		} catch (IOException e) {
			getLogger().message(Severity.error,e.getLocalizedMessage());
		}
    }
    
	@OnAction("action:/saveImageAs")
    public void saveImageAs() {
		try{for (String item : JFileSelectionDialog.select(this, getLocalizer(), fsi, JFileSelectionDialog.OPTIONS_CAN_SELECT_FILE | JFileSelectionDialog.OPTIONS_ALLOW_MKDIR | JFileSelectionDialog.OPTIONS_ALLOW_DELETE | JFileSelectionDialog.OPTIONS_CONFIRM_REPLACEMENT | JFileSelectionDialog.OPTIONS_FOR_SAVE, filterCallbackPNG, filterCallbackJPG)) {
				try(final FileSystemInterface	temp = fsi.clone().open(item)) {
					if (!temp.exists()) {
						temp.create();
					}
					try(final OutputStream	os = temp.write()) {
						ImageIO.write((RenderedImage) panel.getImage(), item.endsWith(".png") ? "png" : "jpeg", os);
					}
					panel.getUndoManager().discardAllEdits();
					lastFile = item;
					refreshMenuState();
					fillTitle();
					return;
				}
			}
		} catch (IOException e) {
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
	}	
	
	@OnAction("action:/redo")
    public void redo() {
	}	

	@OnAction("action:/cut")
    public void cut() {
	}	

	@OnAction("action:/copy")
    public void copy() {
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
	
	private void refreshMenuState() {
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.file.save")).setEnabled(lastFile != null);
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.file.saveAs")).setEnabled(panel.getImage() != null);
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit")).setEnabled(panel.getImage() != null);
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.undo")).setEnabled(panel.getUndoManager().canUndo());
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.undo")).setText(panel.getUndoManager().getUndoPresentationName());
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.redo")).setEnabled(panel.getUndoManager().canUndo());
		((JMenuItem)SwingUtils.findComponentByName(menuBar, "menu.main.edit.redo")).setText(panel.getUndoManager().getRedoPresentationName());
	}

	private boolean checkUnsavedChanges() {
		if (panel.getUndoManager().canUndoOrRedo()) {
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
	
	private void fillTitle() {
		setTitle(String.format(localizer.getValue(KEY_APPLICATION_TITLE),panel.getImage() != null ? (lastFile == null ? "(*)" : "("+lastFile+")"): ""));
	}
	
	private void fillLocalizedStrings() {
		fillTitle();
	}
	
	public static void main(String[] args) {
		final ArgParser					parser = new ApplicationArgParser();
		
		try{final ArgParser				parsed = parser.parse(args);
			try(final InputStream				is = ImageEditPanel.class.getResourceAsStream("imageeditpanel.xml")) {
				final ContentMetadataInterface	xda = ContentModelFactory.forXmlDescription(is);
				final Localizer					localizer = Localizer.Factory.newInstance(xda.getRoot().getLocalizerAssociated());
				
				PureLibSettings.PURELIB_LOCALIZER.push(localizer);
				
				if (args.length == 0) {
					startGUI(xda, localizer);
				}
				else if (!parsed.isTyped(ARG_COMMAND)) {
					throw new CommandLineParametersException("Mandatory parameter ["+ARG_COMMAND+"] is missing");
				}
				else {
					final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>>	root;
					
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
							
							processImage(image, root);
							startGUI(xda, localizer);
						}
						else {
							processImage(System.in, root, System.out);
						}
					}
				}
				
				PureLibSettings.PURELIB_LOCALIZER.pop(localizer);
			} catch (SyntaxException e) {
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
	
	private static void walk(final File current, final Pattern mask, final boolean recursive, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType, ?>> root) throws IOException {
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

	private static void processImage(final InputStream is, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>> root, final OutputStream os) throws IOException {
		final GrowableByteArray	gba = new GrowableByteArray(false);
		final byte[]			signature = new byte[4];
		final BufferedImage		image;
		
		gba.append(is);
		gba.read(0, signature);
		
		try(final InputStream	tmp = gba.getInputStream()) {
			image = ImageIO.read(gba.getInputStream());
		}
		processImage(image, root);
		ImageIO.write(image, defineImageFormat(signature), os);
		os.flush();
	}

	private static String defineImageFormat(final byte[] signature) {
		if (signature[0] == 0xFF && signature[1] == 0xD8 && signature[2] == 0xFF) {
			return "jpeg";
		}
		else if (signature[0] == 0x42F && signature[1] == 0x4D) {
			return "bmp";
		}
		else if (signature[0] == 0x47 && signature[1] == 0x49 && signature[2] == 0x46 && signature[3] == 0x38) {
			return "gif";
		}
		else if (signature[0] == 0x89 && signature[1] == 0x50 && signature[2] == 0x4E && signature[3] == 0x47) {
			return "png";
		}
		else {
			return "webmp";
		}
	}

	private static void processImage(final Image image, final SyntaxNode<ScriptNodeType, SyntaxNode<ScriptNodeType,?>> root) throws IOException {
	}	
	
	private static void startGUI(final ContentMetadataInterface xda, final Localizer localizer) throws InterruptedException, IOException {
		final CountDownLatch			latch = new CountDownLatch(1);
		final Application				app = new Application(xda, localizer, latch);
		
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
}
