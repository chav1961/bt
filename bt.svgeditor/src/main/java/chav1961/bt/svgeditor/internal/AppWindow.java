package chav1961.bt.svgeditor.internal;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JMenuBar;

import chav1961.bt.svgeditor.screen.SVGEditor;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator.ItemDescriptor;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.interfaces.OnAction;

public class AppWindow extends JFrame implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, AutoCloseable {
	private static final long serialVersionUID = 6899575079681432788L;

	private static final String		LRU_PREFIX = "lru";

	private static final String		APP_TITLE = "chav1961.bt.svgeditor.Application.title";
	private static final String		APP_HELP_TITLE = "chav1961.bt.svgeditor.Application.help.title";
	private static final String		APP_HELP_CONTENT = "chav1961.bt.svgeditor.Application.help.content";

	private static final String		APP_FILTER_NAME = "chav1961.bt.svgeditor.Application.filter.names";
	
	private static final String		MENU_MAIN_FILE_LOAD_LRU = "menu.main.file.load.lru";
	private static final String		MENU_MAIN_FILE_SAVE = "menu.main.file.save";
	private static final String		MENU_MAIN_FILE_SAVE_AS = "menu.main.file.saveAs";
	private static final String		MENU_MAIN_FILE_PRINT = "menu.main.file.print";
	private static final String		MENU_MAIN_FILE_EXPORT = "menu.main.file.export";
	private static final String		MENU_MAIN_EDIT = "menu.main.edit";
	private static final String		MENU_MAIN_EDIT_UNDO = "menu.main.edit.undo";
	private static final String		MENU_MAIN_EDIT_REDO = "menu.main.edit.redo";
	private static final String		MENU_MAIN_EDIT_CUT = "menu.main.edit.cut";
	private static final String		MENU_MAIN_EDIT_COPY = "menu.main.edit.copy";
	private static final String		MENU_MAIN_EDIT_PASTE = "menu.main.edit.paste";
	private static final String		MENU_MAIN_EDIT_FIND = "menu.main.edit.find";
	private static final String		MENU_MAIN_EDIT_REPLACE = "menu.main.edit.replace";
	private static final String		MENU_MAIN_TOOLS_PLAYERBAR_RECORDING = "menu.main.tools.playerbar.recording";
	private static final String		MENU_MAIN_TOOLS_PLAYERBAR_PAUSE = "menu.main.tools.playerbar.pause";
	private static final String		MENU_MAIN_TOOLS_PLAYERBAR_PLAY_LAST = "menu.main.tools.playerbar.play.last";
	private static final String		MENU_MAIN_TOOLS_PLAYERBAR_PLAY_FILE = "menu.main.tools.playerbar.play.file";
	private static final String		MENU_MAIN_TOOLS_RECALC = "menu.main.tools.recalc";
	
	private final ContentMetadataInterface	mdi;
	private final SubstitutableProperties	props; 
	private final Localizer					parentLocalizer;
	private final Localizer					localizer;
	private final JMenuBar					menuBar;
	private final JEnableMaskManipulator	emm;
	private final FileSystemInterface		fsi;
	private final List<String>				lruFiles = new ArrayList<>();
	private final LRUPersistence			persistence;
	private final JFileContentManipulator	fcm;
	private final int						fcmIndex;
	private final JStateString				state;
	private final SVGEditor					editor;
	private final CountDownLatch			latch = new CountDownLatch(1);
	
	public AppWindow(final Localizer parentLocalizer, final SubstitutableProperties props) throws NullPointerException, IllegalArgumentException, IOException {
		this.mdi = ContentModelFactory.forXmlDescription(getClass().getResourceAsStream("menus.xml"));
		this.parentLocalizer = parentLocalizer;
		this.props = props;
		this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
		
		parentLocalizer.push(this.localizer);
		parentLocalizer.addLocaleChangeListener(this);
		
		this.menuBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
		this.emm = new JEnableMaskManipulator(getMenus(this), true, menuBar);
		this.state = new JStateString(localizer);
		this.editor = new SVGEditor(localizer);
		this.fsi = FileSystemFactory.createFileSystem(URI.create("fsys:file:/"));
		this.persistence = LRUPersistence.of(props, LRU_PREFIX);
		this.fcm = new JFileContentManipulator("system", fsi, localizer, editor, editor, persistence, lruFiles);
		this.fcm.addFileContentChangeListener((e)->processLRU(e));
		this.fcm.setOwner(this);
		this.fcmIndex = this.fcm.appendNewFileSupport();
		this.fcm.setFilters(FilterCallback.of(APP_FILTER_NAME, "*.svg"));
		this.fcm.setProgressIndicator(state);
		
		setJMenuBar(menuBar);
		getContentPane().add(editor, BorderLayout.CENTER);
		getContentPane().add(state, BorderLayout.SOUTH);
		fillLocalizedStrings();
		
		SwingUtils.centerMainWindow(this, 0.85f);
        SwingUtils.assignActionListeners(menuBar, this);
		SwingUtils.assignExitMethod4MainWindow(this, ()->exit());

		state.message(Severity.info, "Ready");
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
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(menuBar, oldLocale, newLocale);
		SwingUtils.refreshLocale(editor, oldLocale, newLocale);
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
		fillLocalizedStrings();
	}

	@Override
	public void close() throws RuntimeException {
		// TODO Auto-generated method stub
		parentLocalizer.pop();
		dispose();
	}

	public void awaitingExit() throws InterruptedException {
		latch.await();
	}

	@OnAction("action:/newImage")
	private void newImage() {
		try {
			fcm.newFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/loadImage")
	private void loadImage() {
		try {
			fcm.openFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveImage")
	private void saveImage() {
		try {
			fcm.saveFile();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/saveImageAs")
	private void saveImageAs() {
		try {
			fcm.saveFileAs();
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/printImage")
	private void printImage() {
	}
	
	@OnAction("action:/importSTL")
	private void importSTL() {
	}
	
	@OnAction("action:/importDXF")
	private void importDXF() {
	}
	
	@OnAction("action:/exportSTL")
	private void exportSTL() {
	}
	
	@OnAction("action:/exportDXF")
	private void exportDXF() {
	}
	
	@OnAction("action:/exportPNG")
	private void exportPNG() {
	}
	
	@OnAction("action:/exportGIF")
	private void exportGIF() {
	}
	
	@OnAction("action:/exportBMP")
	private void exportBMP() {
	}
	
	@OnAction("action:/exit")
	private void exit() {
		try {
			if (fcm.commit()) {
				latch.countDown();
			}
		} catch (IOException e) {
			getLogger().message(Severity.error, e, e.getLocalizedMessage());
			latch.countDown();
		}
	}
	
	@OnAction("action:/undo")
	private void undo() {
	}
	
	@OnAction("action:/redo")
	private void redo() {
	}
	
	@OnAction("action:/cut")
	private void cut() {
	}
	
	@OnAction("action:/copy")
	private void copy() {
	}
	
	@OnAction("action:/paste")
	private void paste() {
	}
	
	@OnAction("action:/delete")
	private void delete() {
	}
	
	@OnAction("action:/duplicate")
	private void duplicate() {
	}
	
	@OnAction("action:/array")
	private void array() {
	}
	
	@OnAction("action:/grid")
	private void grid() {
	}
	
	@OnAction("action:/ortho")
	private void ortho() {
	}
	
	@OnAction("action:/find")
	private void find() {
	}
	
	@OnAction("action:/replace")
	private void replace() {
	}
	
	@OnAction("action:/InsertLine")
	private void insertLine() {
	}
	
	@OnAction("action:/InsertPolyline")
	private void insertPolyline() {
	}
	
	@OnAction("action:/InsertText")
	private void insertText() {
	}
	
	@OnAction("action:/InsertRectangle")
	private void insertRectangle() {
	}
	
	@OnAction("action:/InsertRoundedRectangle")
	private void insertRoundedRectangle() {
	}
	
	@OnAction("action:/InsertCircle")
	private void insertCircle() {
	}
	
	@OnAction("action:/InsertEllipse")
	private void insertEllipse() {
	}
	
	@OnAction("action:/InsertRhumb")
	private void insertRhumb() {
	}
	
	@OnAction("action:/InsertArrow.direct")
	private void insertArrowDirect() {
	}
	
	@OnAction("action:/InsertArrow.ortho")
	private void insertArrowOrtho() {
	}
	
	@OnAction("action:/InsertArrow.arc")
	private void insertArrowArc() {
	}
	
	@OnAction("action:/InsertTooltip.rectangle")
	private void insertTooltipRectangle() {
	}
	
	@OnAction("action:/InsertTooltip.roundedrectangle")
	private void insertTooltipRoundedRectangle() {
	}
	
	@OnAction("action:/InsertTooltip.ellipse")
	private void insertTooltipEllipse() {
	}
	
	@OnAction("action:/player.recording")
	private void playerRecording() {
	}
	
	@OnAction("action:/player.pause")
	private void playerPause() {
	}
	
	@OnAction("action:/player.play.last")
	private void playerPlayLast() {
	}
	
	@OnAction("action:/player.play.file")
	private void playerPlayFile() {
	}
	
	@OnAction("action:/recalculate")
	private void recalculate() {
	}
	
	@OnAction("action:/builtin.languages")
	private void builtinLang() {
	}
	
	@OnAction("action:/settings")
	private void settings() {
	}
	
	@OnAction("action:/overview")
	private void overview() {
	}
	
	@OnAction("action:/about")
	private void about() {
		SwingUtils.showAboutScreen(this, getLocalizer(), APP_HELP_TITLE, APP_HELP_CONTENT, URI.create("root://"+getClass().getCanonicalName()+"/chav1961/bt/svgeditor/internal/avatar.jpg"), new Dimension(640, 400));
	}
	
	private void processLRU(final FileContentChangedEvent<Object> e) {
		// TODO Auto-generated method stub
		switch (e.getChangeType()) {
			case FILE_LOADED:
				break;
			case FILE_STORED:
				break;
			case FILE_STORED_AS:
				break;
			case FILE_SUPPORT_ID_CHANGED:
				break;
			case LRU_LIST_REFRESHED:
				break;
			case MODIFICATION_FLAG_CLEAR:
				break;
			case MODIFICATION_FLAG_SET:
				break;
			case NEW_FILE_CREATED:
				break;
			default:
				throw new UnsupportedOperationException("Change type ["+e.getChangeType()+"] is not supported yet");
		}
	}
	
	private ItemDescriptor[] getMenus(final AppWindow item) {
		int	index = 0;
		
		return new ItemDescriptor[] {
				new ItemDescriptor(MENU_MAIN_FILE_LOAD_LRU, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_FILE_SAVE, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_FILE_SAVE_AS, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_FILE_PRINT, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_FILE_EXPORT, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_UNDO, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_REDO, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_CUT, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_COPY, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_PASTE, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_FIND, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_REPLACE, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_PLAYERBAR_RECORDING, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_PLAYERBAR_PAUSE, 1L << index++, ()->true, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_PLAYERBAR_PLAY_LAST, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_PLAYERBAR_PLAY_FILE, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_RECALC, 1L << index++, ()->true)
		};
	}
	
	private void refreshTitle() {
		setTitle(getLocalizer().getValue(APP_TITLE));
	}
	
	private void fillLocalizedStrings() {
		refreshTitle();
	}
}
