package chav1961.bt.ocr;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import com.j256.simplemagic.ContentType;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.JEnableMaskManipulator;
import chav1961.purelib.ui.swing.useful.SelectionFrameManager;
import chav1961.purelib.ui.swing.useful.interfaces.SelectionFrameListener;
import chav1961.purelib.ui.swing.useful.interfaces.SelectionFrameListener.SelectionStyle;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class OCRWindow extends JBackgroundComponent implements LoggerFacadeOwner, ModuleAccessor {
	private static final long 		serialVersionUID = 7077660700358390745L;
	private static final URI		LOCALIZER_URI = URI.create("i18n:xml:root://"+OCRWindow.class.getCanonicalName()+"/chav1961/bt/ocr/i18n/i18n.xml");
	private static final String		KEY_MESSAGE_OCR_FAILED = "";
	
	private static final String		MENU_POPUP_PASTE = "menu.popup.paste";
	private static final String		MENU_POPUP_PARSE_IMAGE = "menu.popup.parse.asImage";
	private static final String		MENU_POPUP_PARSE_TEXT = "menu.popup.parse.asText";
	private static final String		MENU_POPUP_LANG_CURRENT= "menu.popup.parse.lang.current";
	
	private static final String[]	MENUS = {
										MENU_POPUP_PASTE,
										MENU_POPUP_PARSE_IMAGE,
										MENU_POPUP_PARSE_TEXT,
										MENU_POPUP_LANG_CURRENT
									};	
	private static final long 		PASTE_MASK = 1L << 0;
	private static final long		PARSE_IMAGE_MASK = 1L << 1;
	private static final long		PARSE_TEXT_MASK = 1L << 2;
	private static final long		LANG_CURRENT = 1L << 3;
	private static final long		PARSE_MASK = PARSE_IMAGE_MASK | PARSE_TEXT_MASK;
	
	private final Tesseract					tesseract = new Tesseract();
	private final File						tesseractDir;
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final boolean					listenClipboard;
	private final JEnableMaskManipulator	emm;
	private final JPopupMenu				popup;
	private final SelectionFrameManager		sfm = new SelectionFrameManager(this, true);
	private final Clipboard					cb = Toolkit.getDefaultToolkit().getSystemClipboard();
	private SupportedLanguages				selectedLang = null;
	private Rectangle						selectedArea = null;
	
	public OCRWindow(final Localizer localizer, final LoggerFacade logger, final File tesseractDir, final boolean listenClipboard) {
		super(localizer);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else if (tesseractDir == null || !tesseractDir.isDirectory() || !tesseractDir.canRead()) {
			throw new IllegalArgumentException("Tesseract directory ["+tesseractDir+"] is null, is not a directory or is not accessible for you");
		}
		else {
			final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(getClass().getResourceAsStream("application.xml"));
			
			this.localizer = localizer;
			this.logger = logger;
			this.tesseractDir = tesseractDir;
			this.listenClipboard = listenClipboard;
			this.popup = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.popupmenu")), JPopupMenu.class);
			this.emm = new JEnableMaskManipulator(MENUS, popup);

	        SwingUtils.assignActionListeners(this.popup, this);
			SwingUtils.assignActionKey(this, SwingUtils.KS_PASTE, (e)->paste(), SwingUtils.ACTION_PASTE);

			emm.setEnableMaskOn(LANG_CURRENT);
			
	        if (!localizer.containsLocalizerAnywhere(LOCALIZER_URI)) {
				localizer.add(Localizer.Factory.newInstance(LOCALIZER_URI));
			}
			if (listenClipboard) {
				cb.addFlavorListener((e)->listenClipboard(e));
			}
			
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				@Override public void mouseClicked(MouseEvent e) {}
				
				@Override
				public void mousePressed(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						refreshMenu();
						popup.show(OCRWindow.this, e.getX(), e.getY());
					}
				}
			});
			new DropTarget(this, DnDConstants.ACTION_COPY, new DropTargetListener() {
				@Override public void dropActionChanged(final DropTargetDragEvent dtde) {}
				@Override public void dragOver(final DropTargetDragEvent dtde) {}
				@Override public void dragExit(final DropTargetEvent dte) {}
				@Override public void dragEnter(final DropTargetDragEvent dtde) {}
				
				@Override 
				public void drop(final DropTargetDropEvent dtde) {
					try {
						processDrop(dtde);
					} catch (UnsupportedFlavorException | IOException e) {
						getLogger().message(Severity.warning, e.getLocalizedMessage());
						dtde.rejectDrop();
					}
				}
				
			}, true);
			setFillMode(FillMode.ORIGINAL);
			setBackgroundImage(new BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB));
			sfm.setSelectionStyle(SelectionStyle.RECTANGLE);
			sfm.addSelectionFrameListener(new SelectionFrameListener() {
				@Override
				public void selectionCompleted(final SelectionStyle style, final Point start, final Point end, final Object... parameters) {
					selectedArea = new Rectangle(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.abs(end.x - start.x), Math.abs(end.y - start.y));
					refreshMenu();
				}
				
				@Override
				public void selectionCancelled(final SelectionStyle style, final Point start, final Point end, final Object... parameters) {
					selectedArea = null;
					refreshMenu();
				}
			});
		}
	}

	protected void processAsText(final String text) {
		System.err.println("TEXT="+text);
		
	}
	
	protected void processAsImage(final Image image) {
		System.err.println("IMAGE="+image.getWidth(null)+"x"+image.getHeight(null));
	}

	@Override
	public LoggerFacade getLogger() {
		return logger;
	}

	@Override
	public void allowUnnamedModuleAccess(final Module... unnamedModules) {
		for(Module item : unnamedModules) {
			this.getClass().getModule().addExports(this.getClass().getPackageName(), item);
		}
	}

	@Override
	public void setBackgroundImage(final Image image) throws NullPointerException {
		super.setBackgroundImage(image);
		sfm.enableSelection(true);
		sfm.resetCurrentSelection();
		refreshMenu();
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		sfm.paintSelection((Graphics2D) g);
	}

	protected void processDrop(final DropTargetDropEvent dtde) throws UnsupportedFlavorException, IOException {
		if (dtde.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			setBackgroundImage((Image)dtde.getTransferable().getTransferData(DataFlavor.imageFlavor));
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			dtde.dropComplete(true);
		}
		else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			for(File f : (List<File>)dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor)) {
				final ContentType 	type = ContentType.fromFileExtension(f.getName().substring(f.getName().lastIndexOf('.')+1));
				
				if (type.getMimeType().startsWith("image")) {
					setBackgroundImage((Image)ImageIO.read(f));
				}
			}
			dtde.acceptDrop(DnDConstants.ACTION_COPY);
			dtde.dropComplete(true);
		}
		else {
			dtde.rejectDrop();
		}
	}

	@OnAction("action:/paste")
	private void paste() {
		try {
			if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
				setBackgroundImage((Image)cb.getData(DataFlavor.imageFlavor));
			}
		} catch (IOException | UnsupportedFlavorException e) {
			getLogger().message(Severity.warning, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/parseAsText")
	private void processAsText() {
		try {
			processAsText(processOCR(extractImage(), selectedLang != null ? selectedLang : SupportedLanguages.of(getInputContext().getLocale())));
		} catch (IOException e) {
			getLogger().message(Severity.warning, e.getLocalizedMessage());
		} finally {
			sfm.enableSelection(true);
			sfm.resetCurrentSelection();
		}
	}

	@OnAction("action:/parseAsImage")
	private void processAsImage() {
		try {
			processAsImage(extractImage());
		} finally {
			sfm.enableSelection(true);
			sfm.resetCurrentSelection();
		}
	}

	@OnAction("action:/selectCurrentLang")
	private void clearLanguage(final Hashtable<String,String[]> modes) {
		selectedLang = null;
		refreshMenu();
	}
	
	@OnAction("action:builtin:/builtin.languages")
    private void language(final Hashtable<String,String[]> langs) throws LocalizationException {
		selectedLang = SupportedLanguages.valueOf(langs.get("lang")[0]);
		refreshMenu();
	}	
	
	private BufferedImage extractImage() {
		final BufferedImage	image = (BufferedImage)getBackgroundImage();
		final Rectangle		rect = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		
		if (rect.intersects(selectedArea)) {
			final Rectangle		clipped = rect.intersection(selectedArea);
			
	        return image.getSubimage(clipped.x, clipped.y, clipped.width, clipped.height);            
		}
		else {
			return image;
		}
	}
	
	private String processOCR(final BufferedImage image, final SupportedLanguages lang) throws IOException {
		final Cursor		oldCursor = getCursor();
		
		try{
			tesseract.setDatapath(tesseractDir.getAbsolutePath());
			switch (lang) {
				case en	:
					tesseract.setLanguage("eng");
					break;
				case ru	:
					tesseract.setLanguage("rus");
					break;
				default	:
					throw new UnsupportedOperationException("Language ["+lang+"] is not supported yet");
			
			}
			tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO_OSD);
			tesseract.setOcrEngineMode(ITessAPI.TessOcrEngineMode.OEM_LSTM_ONLY);			
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			return tesseract.doOCR(image);
		} catch (TesseractException e) {
			getLogger().message(Severity.warning, KEY_MESSAGE_OCR_FAILED, e.getLocalizedMessage());
			return "<failed>";
		} finally {
			setCursor(oldCursor);
		}
	}
	
	private void listenClipboard(final FlavorEvent event) {
		if (cb.isDataFlavorAvailable(DataFlavor.imageFlavor)) {
			try {
				setBackgroundImage((Image)cb.getData(DataFlavor.imageFlavor));
			} catch (UnsupportedFlavorException | IOException exc) {
				getLogger().message(Severity.warning, exc.getLocalizedMessage());
			}
		}
	}

	private void refreshMenu() {
		emm.setEnableMaskTo(PASTE_MASK, cb.isDataFlavorAvailable(DataFlavor.imageFlavor));
		emm.setEnableMaskTo(PARSE_MASK, sfm.hasSelectionNow());
		emm.setCheckMaskTo(LANG_CURRENT, selectedLang == null);
	}
	
	public static void main(final String[] args) {
		final OCRWindow	w = new OCRWindow(PureLibSettings.PURELIB_LOCALIZER, PureLibSettings.CURRENT_LOGGER, new File("d:/tesseract/tessdata"), true);
		
		w.setPreferredSize(new Dimension(640,480));
		JOptionPane.showMessageDialog(null, w);
	}
}
