package chav1961.bt.ocr;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorEvent;
import java.awt.datatransfer.FlavorMap;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPopupMenu;

import com.j256.simplemagic.ContentType;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacadeOwner;
import chav1961.purelib.basic.interfaces.ModuleAccessor;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.SupportedLanguages;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.interfaces.Action;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.SelectionFrameManager;
import chav1961.purelib.ui.swing.useful.interfaces.SelectionFrameListener;
import chav1961.purelib.ui.swing.useful.interfaces.SelectionFrameListener.SelectionStyle;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class OCRWindow extends JBackgroundComponent implements LoggerFacadeOwner, ModuleAccessor {
	private static final long 	serialVersionUID = 7077660700358390745L;
	private static final URI	LOCALIZER_URI = URI.create("root://"+OCRWindow.class.getCanonicalName()+"/chav1961/bt/ocr/i18n/i18n.xml");
	private static final String	KEY_MESSAGE_OCR_FAILED = "";
	
	private final Tesseract				tesseract = new Tesseract();
	private final Localizer				localizer;
	private final LoggerFacade			logger;
	private final boolean				listenClipboard;
	private final JPopupMenu			popup;
	private final SelectionFrameManager	sfm = new SelectionFrameManager(this, true);
	private final Clipboard				cb = Toolkit.getDefaultToolkit().getSystemClipboard();
	private SupportedLanguages			selectedLang = null;
	private Rectangle					selectedArea = null;
	
	public OCRWindow(final Localizer localizer, final LoggerFacade logger, final boolean listenClipboard) {
		super(localizer);
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger facade can't be null");
		}
		else {
			final ContentMetadataInterface	mdi = ContentModelFactory.forXmlDescription(getClass().getResourceAsStream("application.xml"));
			
			this.localizer = localizer;
			this.logger = logger;
			this.listenClipboard = listenClipboard;
			this.popup = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.popupmenu")), JPopupMenu.class);

	        SwingUtils.assignActionListeners(this.popup, this);

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
			sfm.addSelectionFrameListener((style, start, end, parameters)->
				selectedArea = new Rectangle(Math.min(start.x, end.x), Math.min(start.y, end.y), Math.abs(end.x - start.x), Math.abs(end.y - start.y))
			);
		}
	}

	protected void processAsText(final String text) {
		
	}
	
	protected void processAsImage(final Image image) {
		
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
	public void setBackgroundImage(Image image) throws NullPointerException {
		super.setBackgroundImage(image);
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

	@OnAction("action:/processAsText")
	private void processAsText() {
		try {
			processAsText(processOCR(extractImage(), selectedLang != null ? selectedLang : SupportedLanguages.of(getLocale())));
		} catch (IOException e) {
			getLogger().message(Severity.warning, e.getLocalizedMessage());
		}
	}

	@OnAction("action:/processAsImage")
	private void processAsImage() {
		processAsImage(extractImage());
	}
	
	@OnAction("action:builtin:/builtin.languages")
    private void language(final Hashtable<String,String[]> langs) throws LocalizationException {
		selectedLang = SupportedLanguages.valueOf(langs.get("lang")[0]);
	}	
	
	@OnAction("action:/clearLanguage")
	private void clearLanguage() {
		selectedLang = null;
	}
	
	private BufferedImage extractImage() {
        return ((BufferedImage)getBackgroundImage()).getSubimage(selectedArea.x, selectedArea.y, selectedArea.width, selectedArea.height);            
	}
	
	private String processOCR(final BufferedImage image, final SupportedLanguages lang) throws IOException {
		final Cursor		oldCursor = getCursor();
		
		try{
			tesseract.setDatapath("d:/tesseract/tessdata");
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
			tesseract.setPageSegMode(1);
			tesseract.setOcrEngineMode(1);			
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
}
