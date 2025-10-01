package chav1961.bt.svgeditor.internal;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;

import chav1961.bt.svgeditor.interfaces.StateChangedListener;
import chav1961.bt.svgeditor.screen.SVGEditor;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.LRUPersistence;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator.ItemDescriptor;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JFileSelectionDialog.FilterCallback;
import chav1961.purelib.ui.swing.useful.JStateString;
import chav1961.purelib.ui.swing.useful.interfaces.FileContentChangedEvent;

public class AppWindow extends JFrame implements LocaleChangeListener, LoggerFacadeOwner, LocalizerOwner, AutoCloseable {
	private static final long serialVersionUID = 6899575079681432788L;

	private static final String		LRU_PREFIX = "lru";

	private static final String		APP_TITLE = "chav1961.bt.svgeditor.Application.title";
	private static final String		APP_HELP_TITLE = "chav1961.bt.svgeditor.Application.help.title";
	private static final String		APP_HELP_CONTENT = "chav1961.bt.svgeditor.Application.help.content";

	private static final String		APP_FILTER_NAME = "chav1961.bt.svgeditor.Application.filter.names";
	public static final String		APP_MESSAGE_READY = "chav1961.bt.svgeditor.Application.message.ready";
	public static final String		APP_MESSAGE_FILE_NOT_EXISTS = "chav1961.bt.svgeditor.Application.message.file.not.exists";
	public static final String		APP_MESSAGE_PRINTING_COMPLETED = "chav1961.bt.svgeditor.Application.message.printing.completed";
	public static final String		APP_CANVAS_STATE = "chav1961.bt.svgeditor.Application.canvas.state";
	
	private static final String		MENU_MAIN_FILE_LOAD_LRU = "menu.main.file.load.lru";
	private static final String		MENU_MAIN_FILE_SAVE = "menu.main.file.save";
	private static final String		MENU_MAIN_FILE_SAVE_AS = "menu.main.file.saveAs";
	private static final String		MENU_MAIN_FILE_PRINT = "menu.main.file.print";
	private static final String		MENU_MAIN_FILE_IMPORT = "menu.main.file.import";
	private static final String		MENU_MAIN_FILE_EXPORT = "menu.main.file.export";
	private static final String		MENU_MAIN_EDIT = "menu.main.edit";
	private static final String		MENU_MAIN_EDIT_UNDO = "menu.main.edit.undo";
	private static final String		MENU_MAIN_EDIT_REDO = "menu.main.edit.redo";
	private static final String		MENU_MAIN_EDIT_CUT = "menu.main.edit.cut";
	private static final String		MENU_MAIN_EDIT_COPY = "menu.main.edit.copy";
	private static final String		MENU_MAIN_EDIT_PASTE = "menu.main.edit.paste";
	private static final String		MENU_MAIN_EDIT_FIND = "menu.main.edit.find";
	private static final String		MENU_MAIN_EDIT_REPLACE = "menu.main.edit.replace";
	private static final String		MENU_MAIN_INSERT = "menu.main.insert";
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
	private final JLabel					canvasState;	
	private final SVGEditor					editor;
	private final CountDownLatch			latch = new CountDownLatch(1);
	private boolean		anyContentExists = false;
	
	public AppWindow(final Localizer parentLocalizer, final SubstitutableProperties props) throws NullPointerException, IllegalArgumentException, IOException {
		this.mdi = ContentModelFactory.forXmlDescription(getClass().getResourceAsStream("menus.xml"));
		this.parentLocalizer = parentLocalizer;
		this.props = props;
		this.localizer = LocalizerFactory.getLocalizer(mdi.getRoot().getLocalizerAssociated());
		
		parentLocalizer.push(this.localizer);
		parentLocalizer.addLocaleChangeListener(this);
		
		this.menuBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class);
		this.emm = new JEnableMaskManipulator(getMenus(this), true, menuBar);
		this.state = new JStateString(localizer, 10);
		this.editor = new SVGEditor(localizer);
		this.fsi = FileSystemFactory.createFileSystem(URI.create("fsys:file:/"));
		this.persistence = LRUPersistence.of(props, LRU_PREFIX);
		this.fcm = new JFileContentManipulator("system", fsi, localizer, editor, editor, persistence, lruFiles);
		this.fcm.setOwner(this);
		this.fcm.setCurrentFileSupport(this.fcmIndex = this.fcm.appendNewFileSupport());
		this.fcm.addFileContentChangeListener((e)->processLRU(e));
		this.fcm.setFilters(FilterCallback.of(APP_FILTER_NAME, "*.svg"));
		this.fcm.setProgressIndicator(state);
		
		setJMenuBar(menuBar);
		getContentPane().add(editor, BorderLayout.CENTER);
		final JPanel	bottomPanel = new JPanel(new BorderLayout());
		
		this.canvasState = new JLabel();
		canvasState.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
		bottomPanel.add(state, BorderLayout.CENTER);
		bottomPanel.add(canvasState, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		
		editor.addStateChangedListener(new StateChangedListener() {
			@Override
			public void scaleChanged(final double oldScale, final double newScale) {
				fillCanvasState();
			}
			
			@Override
			public void locationChanged(final MouseEvent event) {
				fillCanvasState();
			}
		});
		
		
		fillLocalizedStrings();
		
		SwingUtils.centerMainWindow(this, 0.85f);
        SwingUtils.assignActionListeners(menuBar, this);
		SwingUtils.assignExitMethod4MainWindow(this, ()->exit());

		emm.applyMasks();
		state.message(Severity.info, APP_MESSAGE_READY);
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
        final PrintService[] 	services = PrinterJob.lookupPrintServices();

        if (services.length > 0) {
            try {
        		final PrintRequestAttributeSet aset = new HashPrintRequestAttributeSet();
        		
                aset.add(OrientationRequested.PORTRAIT);
                aset.add(new Copies(1));
                aset.add(new JobName("Print SVG", getLocalizer().currentLocale().getLocale()));
                final PrinterJob pj = PrinterJob.getPrinterJob();
                
                // https://docs.oracle.com/javase/8/docs/technotes/guides/jps/spec/JPSTOC.fm.html
                // https://docs.oracle.com/javase/8/docs/technotes/guides/jps/spec/PrintGIF.java
                pj.setPrintable(new Printable() {
        			@Override
        			public int print(final Graphics graphics, final PageFormat pageFormat, final int pageIndex) throws PrinterException {
        				final Graphics2D g2d = (Graphics2D)graphics;
        				// TODO Auto-generated method stub
        				
                        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY()); 
                        g2d.drawString("example string", 250, 250);
                        g2d.setColor(Color.black);
                        g2d.fillRect(0, 0, 200, 200);        				
        				return pageIndex == 0 ? PAGE_EXISTS : NO_SUCH_PAGE;
        			}
                });
                pj.setPrintService(services[0]);
            	
            	if (pj.printDialog(aset)) {
            		pj.print(aset);
        			getLogger().message(Severity.info, APP_MESSAGE_PRINTING_COMPLETED);
            	}
            } catch (PrinterException pe) { 
    			getLogger().message(Severity.error, pe, pe.getLocalizedMessage());
            }
        }
		
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
	
	@OnAction("action:builtin:/builtin.languages")
    public void language(final Hashtable<String,String[]> langs) throws LocalizationException {
		PureLibSettings.PURELIB_LOCALIZER.setCurrentLocale(SupportedLanguages.valueOf(langs.get("lang")[0]).getLocale());
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
	
	private void processLRU(final FileContentChangedEvent<Object> event) {
		switch (event.getChangeType()) {
			case LRU_LIST_REFRESHED			:
				fillLRU(fcm.getLastUsed());
				break;
			case FILE_LOADED 				:
				anyContentExists = true;
				refreshTitle();
				break;
			case FILE_STORED 				:
				fcm.clearModificationFlag();
				break;
			case FILE_STORED_AS 			:
				fcm.clearModificationFlag();
				refreshTitle();
				break;
			case MODIFICATION_FLAG_CLEAR 	:
				emm.applyMasks();
				refreshTitle();
				break;
			case MODIFICATION_FLAG_SET 		:
				emm.applyMasks();
				refreshTitle();
				break;
			case FILE_SUPPORT_ID_CHANGED	:
				break;
			case NEW_FILE_CREATED 			:
				refreshTitle();
				anyContentExists = true;
				emm.applyMasks();
				break;
			default :
				throw new UnsupportedOperationException("Change type ["+event.getChangeType()+"] is not supported yet");
		}
	}
	
	private void fillLRU(final List<String> lastUsed) {
		final JMenu	menu = (JMenu)SwingUtils.findComponentByName(menuBar, MENU_MAIN_FILE_LOAD_LRU);
		
		menu.removeAll();
		for (String file : lastUsed) {
			final JMenuItem	item = new JMenuItem(file);
			
			item.addActionListener((e)->loadLRU(item.getText()));
			menu.add(item);
		}
		emm.applyMasks();
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
			getLogger().message(Severity.warning, APP_MESSAGE_FILE_NOT_EXISTS, path);
		}
	}
	
	private boolean hasAnyLRU() {
		final JMenu	menu = (JMenu)SwingUtils.findComponentByName(menuBar, MENU_MAIN_FILE_LOAD_LRU);
		
		return menu.getMenuComponentCount() > 0;
	}
	
	private ItemDescriptor[] getMenus(final AppWindow item) {
		int	index = 0;
		
		return new ItemDescriptor[] {
				new ItemDescriptor(MENU_MAIN_FILE_LOAD_LRU, 1L << index++, ()->hasAnyLRU()),
				new ItemDescriptor(MENU_MAIN_FILE_SAVE, 1L << index++, ()->fcm.wasChanged()),
				new ItemDescriptor(MENU_MAIN_FILE_SAVE_AS, 1L << index++, ()->!fcm.isFileNew()),
				new ItemDescriptor(MENU_MAIN_FILE_PRINT, 1L << index++, ()->anyContentExists && !editor.isEmpty() && PrinterJob.lookupPrintServices().length > 0),
				new ItemDescriptor(MENU_MAIN_FILE_IMPORT, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_FILE_EXPORT, 1L << index++, ()->anyContentExists && !editor.isEmpty()),
				new ItemDescriptor(MENU_MAIN_EDIT, 1L << index++, ()->anyContentExists),
				new ItemDescriptor(MENU_MAIN_EDIT_UNDO, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_REDO, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_CUT, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_COPY, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_PASTE, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_FIND, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_EDIT_REPLACE, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_INSERT, 1L << index++, ()->anyContentExists),
				new ItemDescriptor(MENU_MAIN_TOOLS_PLAYERBAR_RECORDING, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_PLAYERBAR_PAUSE, 1L << index++, ()->true, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_PLAYERBAR_PLAY_LAST, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_PLAYERBAR_PLAY_FILE, 1L << index++, ()->true),
				new ItemDescriptor(MENU_MAIN_TOOLS_RECALC, 1L << index++, ()->true)
		};
	}
	
	private void refreshTitle() {
		setTitle(String.format(getLocalizer().getValue(APP_TITLE), 
				(fcm.wasChanged() ? "*" : " ") + fcm.getCurrentNameOfTheFile()));
	}
	
	private void fillLocalizedStrings() {
		refreshTitle();
		fillCanvasState();
	}
	
	private void fillCanvasState() {
		final double	scale = editor.currentScale();
		
		canvasState.setText(getLocalizer().getValue(APP_CANVAS_STATE, 
				(int)(editor.currentMousePoint().getY() * scale), 
				(int)(editor.currentMousePoint().getX() * scale), 
				scale));
	}
}
