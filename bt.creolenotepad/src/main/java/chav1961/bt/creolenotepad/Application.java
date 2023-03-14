package chav1961.bt.creolenotepad;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.undo.UndoManager;

import chav1961.purelib.basic.ArgParser;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.enumerations.MarkupOutputFormat;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.streams.char2char.CreoleWriter;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JSimpleSplash;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;

public class Application extends JFrame implements AutoCloseable, NodeMetadataOwner, LocaleChangeListener, LoggerFacadeOwner {
	private static final long 	serialVersionUID = 1L;
	
	public static final String			ARG_PROPFILE_LOCATION = "prop";

	public static final String			KEY_APPLICATION_TITLE = "chav1961.bt.creolenotepad.Application.title";
	public static final String			KEY_APPLICATION_MESSAGE_READY = "chav1961.bt.creolenotepad.Application.message.ready";
	public static final String			KEY_APPLICATION_MESSAGE_FILE_NOT_EXISTS = "chav1961.bt.creolenotepad.Application.message.file.not.exists";
	public static final String			KEY_APPLICATION_HELP_TITLE = "chav1961.bt.creolenotepad.Application.help.title";
	public static final String			KEY_APPLICATION_HELP_CONTENT = "chav1961.bt.creolenotepad.Application.help.content";
	
	private static final String			CARD_EDITOR = "editor";
	private static final String			CARD_VIEWER = "viewer";
	private static final FilterCallback	FILE_FILTER = FilterCallback.of("Creole files", "*.cre");

	private static final String			MENU_FILE_LRU = "menu.main.file.lru";
	private static final String			MENU_FILE_SAVE = "menu.main.file.save";
	private static final String			MENU_FILE_SAVE_AS = "menu.main.file.saveAs";
	private static final String			MENU_EDIT = "menu.main.edit";
	private static final String			MENU_EDIT_CUT = "menu.main.edit.cut";
	private static final String			MENU_EDIT_COPY = "menu.main.edit.copy";
	private static final String			MENU_EDIT_PASTE = "menu.main.edit.paste";
	private static final String			MENU_EDIT_FIND = "menu.main.edit.find";
	private static final String			MENU_EDIT_FIND_REPLACE = "menu.main.edit.findreplace";
	private static final String			MENU_TOOLS_PREVIEW = "menu.main.tools.preview";

	private static final String[]		MENUS = {
											MENU_FILE_LRU,
											MENU_FILE_SAVE,
											MENU_FILE_SAVE_AS,
											MENU_EDIT,
											MENU_EDIT_CUT,
											MENU_EDIT_COPY,
											MENU_EDIT_PASTE,
											MENU_EDIT_FIND,
											MENU_EDIT_FIND_REPLACE,
											MENU_TOOLS_PREVIEW
										};
	
	private static final long 			FILE_LRU = 1L << 0;
	private static final long 			FILE_SAVE = 1L << 1;
	private static final long 			FILE_SAVE_AS = 1L << 2;
	private static final long 			EDIT = 1L << 3;
	private static final long 			EDIT_CUT = 1L << 4;
	private static final long 			EDIT_COPY = 1L << 5;
	private static final long 			EDIT_PASTE = 1L << 6;
	private static final long 			EDIT_FIND = 1L << 7;
	private static final long 			EDIT_FIND_REPLACE = 1L << 8;
	private static final long 			TOOLS_PREVIEW = 1L << 9;
	private static final long 			TOTAL_EDIT = EDIT | EDIT_CUT | EDIT_COPY | EDIT_FIND | EDIT_FIND_REPLACE;
	
	
	private final ContentMetadataInterface	mdi;
	private final CountDownLatch			latch;
	private final File						props;
	private final SubstitutableProperties	properties;
	private final JMenuBar					menuBar;
	private final JToolBar					toolbar;
	private final Localizer					localizer;
	private final JStateString				state;
	private final FileSystemInterface		fsi = FileSystemFactory.createFileSystem(URI.create("fsys:file:/"));
	private final LRUPersistence			persistence;
	private final JFileContentManipulator	fcm;
	private final CardLayout				cardLayout = new CardLayout();
	private final JPanel					card = new JPanel(cardLayout);
	private final UndoManager 				manager = new UndoManager();
	private final JCreoleEditor				editor = new JCreoleEditor();
	private final JEditorPane				viewer = new JEditorPane("text/html", "");
	private final JEnableMaskManipulator	emm;
	
	private boolean 						anyOpened = false;
	private boolean 						contentModified = false;
	private boolean							inPreview = false;
	
	public Application(final ContentMetadataInterface mdi, final CountDownLatch latch, final File props) throws IOException {
		if (mdi == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (latch == null) {
			throw new NullPointerException("Countdown latch can't be null");
		}
		else if (props == null) {
			throw new NullPointerException("Properties file can't be null");
		}
		else {
			this.mdi = mdi;
			this.latch = latch;
			this.props = props;
			this.properties = props.isFile() && props.canRead() ? SubstitutableProperties.of(props) : new SubstitutableProperties();
			this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
			this.menuBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
			this.toolbar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.toolbarmenu")), JToolBar.class);
			this.emm = new JEnableMaskManipulator(MENUS, menuBar, toolbar);
			
			PureLibSettings.PURELIB_LOCALIZER.push(localizer);
			PureLibSettings.PURELIB_LOCALIZER.addLocaleChangeListener(this);

			editor.getDocument().addUndoableEditListener((e)->manager.addEdit(e.getEdit()));
			viewer.setBackground(Color.LIGHT_GRAY);
			
			this.state = new JStateString(localizer);
			this.persistence = LRUPersistence.of(props, "lru.");
			this.fcm = new JFileContentManipulator(fsi, localizer, editor, persistence);
			this.fcm.setFilters(FILE_FILTER);
			this.fcm.addFileContentChangeListener((e)->processLRU(e));
			
			setJMenuBar(menuBar);
			SwingUtils.assignActionKey(editor, SwingUtils.KS_UNDO, (e)->undo(), SwingUtils.ACTION_UNDO);
			SwingUtils.assignActionKey(editor, SwingUtils.KS_REDO, (e)->redo(), SwingUtils.ACTION_REDO);
			SwingUtils.assignActionKey(editor, SwingUtils.KS_FIND, (e)->find(), SwingUtils.ACTION_FIND);
			SwingUtils.assignActionKey(editor, SwingUtils.KS_FIND_REPLACE, (e)->findReplace(), SwingUtils.ACTION_FIND_REPLACE);
			SwingUtils.assignActionKey(editor, SwingUtils.KS_PRINT, (e)->previewProject(null), SwingUtils.ACTION_PRINT);
			SwingUtils.assignActionKey(viewer, SwingUtils.KS_PRINT, (e)->previewProject(null), SwingUtils.ACTION_PRINT);
			
			card.add(new JScrollPane(editor), CARD_EDITOR);
			card.add(new JScrollPane(viewer), CARD_VIEWER);
			cardLayout.show(card, CARD_EDITOR);
			toolbar.setFloatable(false);
			getContentPane().add(toolbar, BorderLayout.NORTH);
			getContentPane().add(card, BorderLayout.CENTER);
			getContentPane().add(state, BorderLayout.SOUTH);
			
			state.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

	        SwingUtils.assignActionListeners(menuBar, this);
			SwingUtils.assignExitMethod4MainWindow(this, ()->exit());
			SwingUtils.centerMainWindow(this, 0.85f);
	        fillLRU(fcm.getLastUsed());

	        editor.setEditable(false);
	        viewer.setEditable(false);
	        
	        Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener((e)->clipboardChanged());	        
			
			fillLocalizedStrings();
		}
	}


	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return mdi.getRoot();
	}

	@Override
	public LoggerFacade getLogger() {
		return state;
	}
	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(menuBar, oldLocale, newLocale);
		SwingUtils.refreshLocale(toolbar, oldLocale, newLocale);
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
		fillLocalizedStrings();
	}
	
	@Override
	public void close() throws IOException {
		fsi.close();
		PureLibSettings.PURELIB_LOCALIZER.pop(localizer);
		PureLibSettings.PURELIB_LOCALIZER.removeLocaleChangeListener(this);
	}

	@OnAction("action:/newProject")
	public void newProject() {
		try{fcm.newFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/openProject")
	public void openProject() {
		try{fcm.openFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveProject")
	public void saveProject() {
		try{fcm.saveFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveProjectAs")
	public void saveProjectAs() {
		try{fcm.saveFileAs();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/exit")
	public void exit() {
		try{if (fcm.commit()) {
				latch.countDown();
			}
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/undo")
	public void undo() {
		if (manager.canUndo()) {
			manager.undo();
		}
	}
	
	@OnAction("action:/redo")
	public void redo() {
		if (manager.canRedo()) {
			manager.redo();
		}
	}
	
	@OnAction("action:/find")
	public void find() {
	}
	
	@OnAction("action:/findreplace")
	public void findReplace() {
	}
	
	@OnAction("action:/previewProject")
	public void previewProject(final Hashtable<String,String[]> modes) {
		inPreview = !inPreview;
		emm.setCheckMaskTo(TOOLS_PREVIEW, inPreview);
		if (inPreview) {
			try(final StringWriter	wr = new StringWriter();
				final CreoleWriter	cw = new CreoleWriter(wr, MarkupOutputFormat.XML2HTML)) {

				cw.write(editor.getText());
				cw.write("\n\n");
				cw.flush();
				viewer.setText(wr.toString());
			} catch (IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			} finally {
				emm.setEnableMaskOff(TOTAL_EDIT);
				cardLayout.show(card, CARD_VIEWER);
				SwingUtilities.invokeLater(()->viewer.requestFocusInWindow());
			}
		}
		else {
			emm.setEnableMaskOn(TOTAL_EDIT);
			cardLayout.show(card, CARD_EDITOR);
			SwingUtilities.invokeLater(()->editor.requestFocusInWindow());
		}
	}
	
	@OnAction("action:/settings")
	public void settings() {
	}
	
	@OnAction("action:builtin:/builtin.languages")
    public void language(final Hashtable<String,String[]> langs) throws LocalizationException {
		PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(SupportedLanguages.valueOf(langs.get("lang")[0]).getLocale());
	}	
	
	@OnAction("action:/about")
	public void about() {
		SwingUtils.showAboutScreen(this, localizer, KEY_APPLICATION_HELP_TITLE, KEY_APPLICATION_HELP_CONTENT, URI.create("root://"+getClass().getCanonicalName()+"/chav1961/bt/creolenotepad/avatar.jpg"), new Dimension(640, 400));
	}

	void loadLRU(final String path) {
		final File	f = new File(path);
		
		if (f.exists() && f.isFile() && f.canRead()) {
			try{fcm.openFile(path);
			} catch (IOException e) {
				getLogger().message(Severity.error, e, e.getLocalizedMessage());
			}
		}
		else {
			fcm.removeFileNameFromLRU(path);
			getLogger().message(Severity.warning, KEY_APPLICATION_MESSAGE_FILE_NOT_EXISTS, path);
		}
	}

	private void processLRU(final FileContentChangedEvent<?> event) {
		switch (event.getChangeType()) {
			case LRU_LIST_REFRESHED			:
				fillLRU(fcm.getLastUsed());
				break;
			case FILE_LOADED 				:
		        editor.setEditable(true);
				emm.setEnableMaskOn(FILE_SAVE_AS | TOTAL_EDIT | TOOLS_PREVIEW);
				anyOpened = true;
				contentModified = false;
				fillTitle();
				break;
			case FILE_STORED 				:
				fcm.clearModificationFlag();
				contentModified = false;
				break;
			case FILE_STORED_AS 			:
				fcm.clearModificationFlag();
				contentModified = false;
				fillTitle();
				break;
			case MODIFICATION_FLAG_CLEAR 	:
				emm.setEnableMaskOff(FILE_SAVE);
				contentModified = false;
				fillTitle();
				break;
			case MODIFICATION_FLAG_SET 		:
				emm.setEnableMaskOn(FILE_SAVE_AS | FILE_SAVE);
				contentModified = true;
				fillTitle();
				break;
			case NEW_FILE_CREATED 			:
		        editor.setEditable(true);
				emm.setEnableMaskOn(FILE_SAVE_AS | TOTAL_EDIT | TOOLS_PREVIEW);
				anyOpened = true;
				contentModified = false;
				fillTitle();
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+event.getChangeType()+"] is not supported yet");
		}
	}
	
	private void fillLRU(final List<String> lastUsed) {
		if (lastUsed.isEmpty()) {
			emm.setEnableMaskOff(FILE_LRU);
		}
		else {
			final JMenu	menu = (JMenu)SwingUtils.findComponentByName(menuBar, MENU_FILE_LRU);
			
			menu.removeAll();
			for (String file : lastUsed) {
				final JMenuItem	item = new JMenuItem(file);
				
				item.addActionListener((e)->loadLRU(item.getText()));
				menu.add(item);
			}
			emm.setEnableMaskOn(FILE_LRU);
		}
	}

	private void clipboardChanged() {
		try{if (Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.plainTextFlavor) || Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DataFlavor.plainTextFlavor)) {
				emm.setEnableMaskTo(EDIT_PASTE, anyOpened);
			}
			else {
				emm.setEnableMaskOff(EDIT_PASTE);
			}
		} catch (IllegalStateException exc) {
			emm.setEnableMaskOff(EDIT_PASTE);
		}
	}
	
	private void fillTitle() {
		setTitle(localizer.getValue(KEY_APPLICATION_TITLE, (contentModified ? "* " : "") + fcm.getCurrentPathOfTheFile()));
	}
	
	private void fillLocalizedStrings() {
		fillTitle();
	}
	
	public static void main(String[] args) {
		final ArgParser	parser = new ApplicationArgParser();
		int				retcode = 0;
		
		try(final JSimpleSplash		jss = new JSimpleSplash()) {
			final ArgParser			parsed = parser.parse(args);
			final CountDownLatch	latch = new CountDownLatch(1);
			final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(Application.class.getResourceAsStream("application.xml"));
			
			jss.start("load", 2);
			try(final Application	app = new Application(mdi, latch, parsed.getValue(ARG_PROPFILE_LOCATION, File.class))) {
				
				app.setVisible(true);
				app.getLogger().message(Severity.info, KEY_APPLICATION_MESSAGE_READY);
				
				latch.await();
			} catch (InterruptedException e) {
			}
		} catch (CommandLineParametersException | IOException e) {
			System.err.println(e.getLocalizedMessage());
			System.err.println(parser.getUsage("creolenotepad"));
			retcode = 128;
		}
		System.exit(retcode);
	}

	private static class ApplicationArgParser extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new FileArg(ARG_PROPFILE_LOCATION, false, "Property file location", "./.bt.creolenotepad.properties")
		};
		
		private ApplicationArgParser() {
			super(KEYS);
		}
	}





}
