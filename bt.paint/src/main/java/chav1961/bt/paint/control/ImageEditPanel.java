package chav1961.bt.paint.control;



import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import chav1961.bt.paint.control.ImageUtils.DrawingType;
import chav1961.bt.paint.control.ImageUtils.ProcessType;
import chav1961.bt.paint.dialogs.AskImageResize;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.CanvasWrapper;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.ConsoleInterface;
import chav1961.bt.paint.script.interfaces.FontWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper.SetOptions;
import chav1961.bt.paint.script.interfaces.RectWrapper;
import chav1961.bt.paint.script.interfaces.StrokeWrapper;
import chav1961.bt.paint.script.intern.Console;
import chav1961.bt.paint.utils.ApplicationUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.ColorPair;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFontSelectionDialog;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
import chav1961.purelib.ui.swing.useful.interfaces.SelectionFrameListener.SelectionStyle;

public class ImageEditPanel extends JPanel implements LocalizerOwner, LocaleChangeListener, CanvasWrapper, ConsoleInterface {
	private static final long 				serialVersionUID = -8630893532191028731L;
	private static final ContentMetadataInterface	xda;
	
	private static final String				KEY_SELECT_COLOR = "chav1961.bt.paint.editor.ImageEditPanel.selectColor";

	static {
		try(final InputStream				is = ImageEditPanel.class.getResourceAsStream("imageeditpanel.xml")) {

			xda = ContentModelFactory.forXmlDescription(is);
		} catch (IOException | EnvironmentException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}		
	}

	private final LightWeightListenerList<ChangeListener>		listeners = new LightWeightListenerList<>(ChangeListener.class);
	private final LightWeightListenerList<UndoableEditListener>	undoListeners = new LightWeightListenerList<>(UndoableEditListener.class);
	private final Localizer			localizer;
	private final JPanel			topPanel = new JPanel();
	private final JPanel			leftPanel = new JPanel();
	private final ImageEditCanvas	canvas;
	private final EditStateString	state;
	private final Predefines		predef = new Predefines(new String[0]);
	private RectWrapper				selection = null;
	private ResizableTextArea		rta = null;
	private DrawingType				drawingType = DrawingType.UNKNOWN;
	private Rectangle				lastSelection = null;
	private boolean 				foregroundNow = true;
	private boolean 				fillingOn = false;
	private boolean 				waitColorExtraction = false;
	private byte[]					before;
	private boolean					startUndoable = false;
	private String					undoString, redoString;

	public ImageEditPanel(final Localizer localizer) throws NullPointerException {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.localizer = localizer;
			this.canvas = new ImageEditCanvas(localizer);
			this.state = new EditStateString(localizer);

			predef.putPredefined(Predefines.PREDEF_CANVAS, this);
			
			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

			leftPanel.add(prepareModeToolBar());
			leftPanel.add(prepareActionToolBar());
			
			topPanel.add(prepareColorToolBar());
			topPanel.add(prepareSettingsToolBar());
	        
	        add(topPanel, BorderLayout.NORTH);
	        add(leftPanel, BorderLayout.WEST);
	        add(new JScrollPane(canvas), BorderLayout.CENTER);
	        add(state, BorderLayout.SOUTH);

	        canvas.addChangeListener((l)->{
	        	listeners.fireEvent((ls)->ls.stateChanged(l));
	        	state.refreshSettings(canvas);
	        });
	        canvas.addMouseMotionListener(new MouseMotionListener() {
				@Override public void mouseMoved(MouseEvent e) {state.refreshCoordinates(canvas, e.getX(), e.getY());}
				@Override public void mouseDragged(MouseEvent e) {state.refreshCoordinates(canvas, e.getX(), e.getY());}
			});
	        canvas.getSelectionManager().addSelectionFrameListener((style, start, end, parameters)->{
	        	try{processSelection(style, start, end, parameters);
				} catch (SyntaxException | PaintScriptException exc) {
					SwingUtils.getNearestLogger(ImageEditPanel.this).message(Severity.error, exc, exc.getLocalizedMessage());
				}
	        });
	        
        	state.refreshSettings(canvas);
	        fillLocalizedStrings();
		}
	}

	@Override
	public Localizer getLocalizer() {
		return localizer;
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		SwingUtils.refreshLocale(leftPanel, oldLocale, newLocale);
		SwingUtils.refreshLocale(topPanel, oldLocale, newLocale);
		SwingUtils.refreshLocale(state, oldLocale, newLocale);
		SwingUtils.refreshLocale(canvas, oldLocale, newLocale);
        fillLocalizedStrings();
	}

	public void clearCommandString() {
		state.clearCommandString();
	}
	
	@Override
	public void open() throws PaintScriptException {
		// TODO Auto-generated method stub
	}

	@Override
	public void clear() throws PaintScriptException {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean hasImage() throws PaintScriptException {
		return canvas.getBackgroundImage() != null;
	}

	@Override
	public ImageWrapper getImage() throws PaintScriptException {
		return ImageWrapper.of((BufferedImage)canvas.getBackgroundImage());
	}

	@Override
	public void setImage(final ImageWrapper image) throws PaintScriptException {
		if (image == null) {
			throw new NullPointerException("Image to set can't be null");
		}
		else {
			canvas.setBackgroundImage(image.getImage());
		}
	}

	@Override
	public ImageWrapper getImage(final RectWrapper rect) throws PaintScriptException {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null"); 
		}
		else if (!hasImage()) {
			throw new IllegalStateException("Canvas doesn't have any image to use this method"); 
		}
		else {
			return ImageWrapper.of((BufferedImage)ImageUtils.cropImage((BufferedImage)canvas.getBackgroundImage(), rect.getRect(), null));
		}
	}

	@Override
	public void setImage(final RectWrapper rect, final ImageWrapper image, final SetOptions... options) throws PaintScriptException {
		if (rect == null) {
			throw new NullPointerException("Rectangle can't be null"); 
		}
		else if (image == null) {
			throw new NullPointerException("Image can't be null"); 
		}
		else if (!hasImage()) {
			throw new IllegalStateException("Canvas doesn't have any image to use this method"); 
		}
		else {
			ImageUtils.insertImage((BufferedImage)canvas.getBackgroundImage(), rect.getRect(), (BufferedImage)image.getImage(), null);
		}
	}

	@Override
	public RectWrapper getSelection() throws PaintScriptException {
		return selection;
	}

	@Override
	public void setSelection(final RectWrapper rect) throws PaintScriptException {
		selection = RectWrapper.of(rect.getRect());
	}

	@Override
	public void clearSelection() throws PaintScriptException {
		selection = null;
	}

	@Override
	public void close() throws PaintScriptException {
		// TODO Auto-generated method stub
	}

	@Override
	public FontWrapper getCanvasFont() {
		return FontWrapper.of(canvas.getFont());
	}

	@Override
	public void setCanvasFont(final FontWrapper font) {
		if (font == null) {
			throw new NullPointerException("Font to set can't be null");
		}
		else {
			canvas.setFont(font.getFont());
		}
	}

	@Override
	public ColorWrapper getCanvasForeground() {
		return ColorWrapper.of(canvas.getForeground());
	}

	@Override
	public void setCanvasForeground(final ColorWrapper color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			canvas.setForeground(color.getColor());
		}
	}

	@Override
	public ColorWrapper getCanvasBackground() {
		return ColorWrapper.of(canvas.getBackground());
	}

	@Override
	public void setCanvasBackground(final ColorWrapper color) {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else {
			canvas.setBackground(color.getColor());
		}
	}

	@Override
	public StrokeWrapper getCanvasStroke() {
		return StrokeWrapper.of(ImageUtils.buildStroke(canvas.getLineThickness(), canvas.getLineStroke(), canvas.getLineCaps(), canvas.getLineJoin()));
	}

	@Override
	public void setCanvasStroke(final StrokeWrapper stroke) {
		if (stroke == null) {
			throw new NullPointerException("Stroke to set can't be null");
		}
		else {
			canvas.setLineThickness((int)((BasicStroke)stroke.getStroke()).getLineWidth());
			canvas.setLineStroke(StrokeWrapper.LineStroke.valueOf(((BasicStroke)stroke.getStroke()).getDashArray()));
			canvas.setLineCaps(StrokeWrapper.LineCaps.valueOf(((BasicStroke)stroke.getStroke()).getEndCap()));
			canvas.setLineJoin(StrokeWrapper.LineJoin.valueOf(((BasicStroke)stroke.getStroke()).getLineJoin()));
		}
	}

	@Override
	public String console(final String command, final Predefines predef) throws SyntaxException, PaintScriptException {
		if (command == null || command.isEmpty()) {
			throw new IllegalArgumentException("Command to process can't be null or empty");
		}
		else if (predef == null) {
			throw new NullPointerException("Predefines can't be null");
		}
		else {
			return Console.processCommand(command, predef);
		}
	}

	@Override
	public void startImageAction(final String beforeActionType, final String afterActionType) {
		try{if (canvas.getBackgroundImage() != null) {
				before = ImageUndoEdit.packImage(canvas.getBackgroundImage());
			}
			else {
				before = null;
			}
			undoString = beforeActionType;
			redoString = afterActionType;
			startUndoable = true;
		} catch (IOException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
		}
	}

	@Override
	public void endImageAction(final String beforeActionType, final String afterActionType) {
		if (startUndoable) {
			try{if (before != null && canvas.getBackgroundImage() != null) {
					fireUndo(new ImageUndoEdit(beforeActionType, afterActionType, before, ImageUndoEdit.packImage(canvas.getBackgroundImage()), (i)->canvas.setBackgroundImage(i)));
				}
			} catch (IOException exc) {
				SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
			} finally {
				startUndoable = false;
				before = null;
				undoString = null;
				redoString = null;
			}
		}
	}

	@Override
	public void startPropertyAction(final String beforeActionType, final String afterActionType) {
		// TODO Auto-generated method stub
	}

	@Override
	public void endPropertyAction(final String beforeActionType, final String afterActionType) {
		// TODO Auto-generated method stub
	}
	
	public void addChangeListener(final ChangeListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(l);
		}
	}

	public void removeChangeListener(final ChangeListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(l);
		}
	}

	public void addUndoableEditListener(final UndoableEditListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			undoListeners.addListener(l);
		}
	}

	public void removeChangeListener(final UndoableEditListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			undoListeners.removeListener(l);
		}
	}

	public void addActionListener(final ActionListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			state.forCommand.addActionListener(l);
		}
	}

	public void removeActionListener(final ActionListener l) {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			state.forCommand.removeActionListener(l);
		}
	}
	
	@OnAction("action:/chooseColor")
    public void chooseColor(final Hashtable<String,String[]> colors) {
		try{switch (colors.get("color")[0]) {
				case "black"	: setColor(Color.BLACK);		break;
				case "blue"		: setColor(Color.BLUE);			break;
				case "cyan"		: setColor(Color.CYAN);			break;
				case "darkgray"	: setColor(Color.DARK_GRAY);	break;
				case "gray"		: setColor(Color.GRAY);			break;
				case "green"	: setColor(Color.GREEN);		break;
				case "lightgray": setColor(Color.LIGHT_GRAY);	break;
				case "magenta"	: setColor(Color.MAGENTA);		break;
				case "orange"	: setColor(Color.ORANGE);		break;
				case "pink"		: setColor(Color.PINK);			break;
				case "red"		: setColor(Color.RED);			break;
				case "white"	: setColor(Color.WHITE);		break;
				case "yellow"	: setColor(Color.YELLOW);		break;
				case "choose"	: chooseColor();				break;
				case "extract"	: extractColor();				break;
				default : throw new UnsupportedOperationException("Color type ["+colors.get("color")[0]+"] is not supported yet"); 
			}
		} catch (PaintScriptException | SyntaxException exc) {
			SwingUtils.getNearestLogger(this).message(Severity.error, exc, exc.getLocalizedMessage());
		}
    }

	@OnAction("action:/switchColor")
    public void switchColor(final Hashtable<String,String[]> modes) {
		foregroundNow = !foregroundNow;
	}
	
	@OnAction("action:/chooseMode")
    public void chooseMode(final Hashtable<String,String[]> modes) throws IOException {
		removeAnyChild();
		switch (drawingType = DrawingType.valueOf(modes.get("mode")[0])) {
			case BRUSH		:
				break;
			case ELLIPSE	:
				canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
				canvas.getSelectionManager().enableSelection(true);
				canvas.getSelectionManager().setVisible(true);
				break;
			case FILL		:
				break;
			case LINE		:
				canvas.getSelectionManager().setSelectionStyle(SelectionStyle.LINE);
				canvas.getSelectionManager().enableSelection(true);
				canvas.getSelectionManager().setVisible(true);
				break;
			case PEN		:
				canvas.getSelectionManager().setSelectionStyle(SelectionStyle.PATH);
				canvas.getSelectionManager().enableSelection(true);
				canvas.getSelectionManager().setVisible(true);
				break;
			case RECT		:
				canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
				canvas.getSelectionManager().enableSelection(true);
				canvas.getSelectionManager().setVisible(true);
				break;
			case SELECT		:
				canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
				canvas.getSelectionManager().enableSelection(true);
				canvas.getSelectionManager().setVisible(true);
				break;
			case TEXT		:
				canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
				canvas.getSelectionManager().enableSelection(true);
				canvas.getSelectionManager().setVisible(true);
				break;
			case ERASE		:
				canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
				canvas.getSelectionManager().enableSelection(true);
				canvas.getSelectionManager().setVisible(true);
				break;
			case UNKNOWN	:
				break;
			default :
				throw new UnsupportedOperationException("Drawing type ["+drawingType+"] is not supported yet"); 
		}
    }
	
	@OnAction("action:/crop")
	public void crop() throws IOException {
		if (lastSelection != null) {
			final Image			current = canvas.getBackgroundImage();

			removeAnyChild();
			canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
			canvas.getSelectionManager().enableSelection(true);
			canvas.getSelectionManager().setVisible(true);
			canvas.setBackgroundImage(ImageUtils.cropImage((BufferedImage) current, lastSelection, null));
			lastSelection = null; 
		}
	}
	
	@OnAction("action:/resize")
	public void resize() throws IOException {
		try{final AskImageResize	air = new AskImageResize(SwingUtils.getNearestLogger(this));
		
			if (ApplicationUtils.ask(air, getLocalizer(), 300, 145)) {
				final Image			current = canvas.getBackgroundImage();
				
				removeAnyChild();
				canvas.setBackgroundImage(ImageUtils.resizeImage((BufferedImage) current, air.width, air.height, canvas.getBackground(), air.stretchContent, air.fromCenter, null));
			}
		} catch (ContentException e) {
			SwingUtils.getNearestLogger(this).message(Severity.error, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/rotate")
	public void rotate() throws IOException {
		final Image			current = canvas.getBackgroundImage();
		
		removeAnyChild();
		canvas.setBackgroundImage(ImageUtils.rotateImage((BufferedImage) current, false, null));
	}
	
	@OnAction("action:/reflectVert")
	public void reflectV() throws IOException {
		final Image			current = canvas.getBackgroundImage();

		removeAnyChild();
		canvas.setBackgroundImage(ImageUtils.mirrorImage((BufferedImage) current, false, null));
	}
	
	@OnAction("action:/reflectHor")
	public void reflectH() throws IOException {
		final Image			current = canvas.getBackgroundImage();
		
		removeAnyChild();
		canvas.setBackgroundImage(ImageUtils.mirrorImage((BufferedImage) current, true, null));
	}

	@OnAction("action:/toGrayScale")
	public void toGrayScale() throws IOException {
		final Image			current = canvas.getBackgroundImage();
		
		removeAnyChild();
		canvas.setBackgroundImage(ImageUtils.grayScaleImage((BufferedImage) current, null));
	}
	
	@OnAction("action:/transparency")
	public void makeTransparent() throws IOException {
		final Image			current = canvas.getBackgroundImage();
		
		removeAnyChild();
		if (foregroundNow) {
			canvas.setBackgroundImage(ImageUtils.transparentImage((BufferedImage) current, canvas.getForeground(), false, null));
		}
		else {
			canvas.setBackgroundImage(ImageUtils.transparentImage((BufferedImage) current, canvas.getBackground(), true, null));
		}
	}
	
	@OnAction("action:/settings.font")
	public void setFont() {
		final JFontSelectionDialog	dlg = new JFontSelectionDialog(localizer); 
		
		dlg.setSelectedFont(canvas.getFont());
		if (dlg.selectFont(this)) {
			canvas.setFont(dlg.getSelectedFont());
		}
	}
	
	@OnAction("action:/settings.thickness")
	public void setThickness(final Hashtable<String,String[]> modes) {
		canvas.setLineThickness(Integer.valueOf(modes.get("width")[0]));
	}

	@OnAction("action:/settings.stroke")
	public void setStroke(final Hashtable<String,String[]> modes) {
		if (modes.containsKey("style")) {
			canvas.setLineStroke(StrokeWrapper.LineStroke.valueOf(modes.get("style")[0]));
		}
		else if (modes.containsKey("cap")) {
			canvas.setLineCaps(StrokeWrapper.LineCaps.valueOf(modes.get("caps")[0]));
		}
		else if (modes.containsKey("join")) {
			canvas.setLineJoin(StrokeWrapper.LineJoin.valueOf(modes.get("join")[0]));
		}
	}

	@OnAction("action:/settings.filling")
	public void setFilling(final Hashtable<String,String[]> modes) {
		fillingOn = !fillingOn;
	}

	public void processFilter(final float[] filter) throws PaintScriptException {
		final Rectangle		rect = new Rectangle(0, 0, getImage().getImage().getWidth(), getImage().getImage().getHeight()); 
			
		ImageUtils.filterImage((BufferedImage)getImage().getImage(), rect, filter, null);
		refreshContent();
	}
	
	public void setImage(final Image image) {
		if (image == null) {
			throw new NullPointerException("Image to set can't be null");
		}
		else {
			removeAnyChild();
			canvas.setBackgroundImage(image);
		}
	}
	
	public DrawingType getCurrentDrawingMode() {
		return drawingType;
	}

	public boolean hasSelection() {
		return lastSelection != null;
	}
	
	public Image getSelectedImage() {
		if (hasSelection()) {
			return ImageUtils.cropImage((BufferedImage)canvas.getBackgroundImage(), lastSelection, null);
		}
		else {
			return null;
		}
	}

	public Image cutSelectedImage() {
		if (hasSelection()) {
			final Image	image = ImageUtils.cropImage((BufferedImage)canvas.getBackgroundImage(), lastSelection, null);
			
			ImageUtils.fillImage((BufferedImage)canvas.getBackgroundImage(), lastSelection, fillingOn ? canvas.getBackground() : new Color(0), null);
			return image;
		}
		else {
			return null;
		}
	}
	
	public void pasteImage(final Image image) {
		final String						us = undoString, rs = redoString;
		final ResizableImageContainer<?>	ric = new ResizableImageContainer<>(localizer, image, (t)->{
												try{startImageAction(us, rs);
													ImageUtils.insertImage((BufferedImage)getImage().getImage(), t.getBounds(), (BufferedImage)t.getBackgroundImage(), null);
													endImageAction(us, rs);
												} catch (PaintScriptException exc) {
													SwingUtils.getNearestLogger(t).message(Severity.error, exc, exc.getLocalizedMessage());
												} finally {
													refreshContent();
												}
											});
		canvas.add(ric);
		ric.setLocation(0, 0);
		ric.setVisible(true);
		SwingUtilities.invokeLater(()->ric.requestFocusInWindow());
		canvas.revalidate();
		refreshContent();
		startUndoable = false;
	}

	public void refreshContent() {
		canvas.repaint();
	}
	
	private void processSelection(final SelectionStyle style, final Point start, final Point end, final Object... parameters) throws SyntaxException, PaintScriptException {
		if (waitColorExtraction && style == SelectionStyle.POINT) {
			if (foregroundNow) {
				console("fore "+end.x+","+end.y, predef);
			}
			else {
				console("back "+end.x+","+end.y, predef);
			}
    		canvas.getSelectionManager().popSelectionStyle();
    		turnOffExtractColorButton();
    		waitColorExtraction = false;
		}
		else {
			final Image		current = canvas.getBackgroundImage();
			
			switch (getCurrentDrawingMode()) {
				case BRUSH	:
					break;
				case ELLIPSE:
					if (fillingOn) {
						ImageUtils.ellipseDraw((BufferedImage)canvas.getBackgroundImage(), (Rectangle)parameters[0], new ColorPair(canvas.getForeground(), canvas.getBackground())
							, ImageUtils.buildStroke(canvas.getLineThickness(), canvas.getLineStroke(), canvas.getLineCaps(), canvas.getLineJoin()), null);
					}
					else {
						ImageUtils.ellipseDraw((BufferedImage)canvas.getBackgroundImage(), (Rectangle)parameters[0], canvas.getForeground()
							, ImageUtils.buildStroke(canvas.getLineThickness(), canvas.getLineStroke(), canvas.getLineCaps(), canvas.getLineJoin()), null);
					}
					SwingUtilities.invokeLater(()->{
							canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
							canvas.getSelectionManager().enableSelection(true);
					});
					break;
				case FILL	:
					break;
				case LINE	:
					ImageUtils.lineDraw((BufferedImage)canvas.getBackgroundImage(), start,end, canvas.getForeground()
							, ImageUtils.buildStroke(canvas.getLineThickness(), canvas.getLineStroke(), canvas.getLineCaps(), canvas.getLineJoin()), null);
					SwingUtilities.invokeLater(()->{
							canvas.getSelectionManager().setSelectionStyle(SelectionStyle.LINE);
							canvas.getSelectionManager().enableSelection(true);
					});
					break;
				case PEN	:
					ImageUtils.pathDraw((BufferedImage)canvas.getBackgroundImage(), (GeneralPath)parameters[0], canvas.getForeground()
							, ImageUtils.buildStroke(canvas.getLineThickness(), canvas.getLineStroke(), canvas.getLineCaps(), canvas.getLineJoin()), null);
					SwingUtilities.invokeLater(()->{
							canvas.getSelectionManager().setSelectionStyle(SelectionStyle.PATH);
							canvas.getSelectionManager().enableSelection(true);
					});
					break;
				case RECT	:
					if (fillingOn) {
						ImageUtils.rectDraw((BufferedImage)canvas.getBackgroundImage(), (Rectangle)parameters[0], new ColorPair(canvas.getForeground(), canvas.getBackground())
							, ImageUtils.buildStroke(canvas.getLineThickness(), canvas.getLineStroke(), canvas.getLineCaps(), canvas.getLineJoin()), null);
					}
					else {
						ImageUtils.rectDraw((BufferedImage)canvas.getBackgroundImage(), (Rectangle)parameters[0], canvas.getForeground()
							, ImageUtils.buildStroke(canvas.getLineThickness(), canvas.getLineStroke(), canvas.getLineCaps(), canvas.getLineJoin()), null);
					}
					SwingUtilities.invokeLater(()->{
							canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
							canvas.getSelectionManager().enableSelection(true);
					});
					break;
				case SELECT	:
					lastSelection = new Rectangle((Rectangle)parameters[0]);
					refreshMenuState();
					break;
				case TEXT	:
					this.rta = new ResizableTextArea(canvas.getForeground(), canvas.getFont(), (Rectangle)parameters[0]);

					canvas.add(rta);
					SwingUtilities.invokeLater(()->{
							canvas.getSelectionManager().setSelectionStyle(SelectionStyle.RECTANGLE);
							canvas.getSelectionManager().enableSelection(true);
					});
					break;
				case UNKNOWN:
					break;
				default:
					break;
			}
		}
	}
	
	private void refreshMenuState() {
		// TODO Auto-generated method stub
		final ChangeEvent	ce = new ChangeEvent(this);
		
		listeners.fireEvent((l)->l.stateChanged(ce));
	}

	private void removeAnyChild() {
		if (rta != null) {
			canvas.remove(rta);
			rta = null;
		}
	}
	
	private void chooseColor() throws SyntaxException, PaintScriptException {
        final JColorChooser	chooser = new  JColorChooser(canvas.getForeground());
        final Color[]		temp = new Color[] {canvas.getForeground()}; 
        
        chooser.getSelectionModel().addChangeListener((e)->temp[0] = chooser.getColor());
        if (new JLocalizedOptionPane(localizer).confirm(null, chooser, KEY_SELECT_COLOR, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        	setColor(temp[0]);
        }
	}

	private void extractColor() {
		if (canvas.getBackgroundImage() != null) {
			waitColorExtraction = true;
			canvas.getSelectionManager().pushSelectionStyle(SelectionStyle.POINT);
			canvas.getSelectionManager().enableSelection(true);
			canvas.getSelectionManager().setVisible(true);
		}
		else {
			turnOffExtractColorButton();
			waitColorExtraction = false;
		}
	}
	
	private void setColor(final Color color) throws SyntaxException, PaintScriptException {
		if (foregroundNow) {
			console("fore #"+Integer.toHexString(color.getRGB() & 0xFFFFFF), predef);
		}
		else {
			console("back #"+Integer.toHexString(color.getRGB() & 0xFFFFFF), predef);
		}
	}
	
	private JToolBar prepareModeToolBar() {
        final JToolBar	result = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.modeBar")), JToolBar.class);
        
        result.setFloatable(false);
        result.setOrientation(JToolBar.VERTICAL);
        SwingUtils.assignActionListeners(result, this);
        return result;
	}

	private JToolBar prepareActionToolBar() {
        final JToolBar	result = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.actionBar")), JToolBar.class);
        
        result.setFloatable(false);
        result.setOrientation(JToolBar.VERTICAL);
        SwingUtils.assignActionListeners(result, this);
        return result;
	}
	
	private JToolBar prepareColorToolBar() {
	    final JToolBar	result = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.colorBar")), JToolBar.class);
	
	    result.setFloatable(false);
	    result.setOrientation(JToolBar.HORIZONTAL);
	    SwingUtils.assignActionListeners(result, this);
	    return result;
	}

	private JToolBar prepareSettingsToolBar() {
	    final JToolBar	result = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.settingsBar")), JToolBar.class);
	
	    result.setFloatable(false);
	    result.setOrientation(JToolBar.HORIZONTAL);
	    SwingUtils.assignActionListeners(result, this);
	    return result;
	}

	private void fireUndo(final ImageUndoEdit edit) {
		final UndoableEditEvent	ee = new UndoableEditEvent(this, edit);
		
		undoListeners.fireEvent((l)->l.undoableEditHappened(ee));
	}

	private static class EditStateString extends JPanel implements LocaleChangeListener {
		private static final long serialVersionUID = 1L;

		private static final String		KEY_PROMPT = "chav1961.bt.paint.bgeditor.ImageEditPanel.EditStateString.prompt";
		private static final String		KEY_COMMAND_TT = "chav1961.bt.paint.bgeditor.ImageEditPanel.EditStateString.path.tt"; 		
		private static final String		KEY_COORD_TT = "chav1961.bt.paint.bgeditor.ImageEditPanel.EditStateString.coord.tt"; 		
		private static final String		KEY_SIZE_TT = "chav1961.bt.paint.bgeditor.ImageEditPanel.EditStateString.size.tt"; 		
		private static final String		KEY_FONT_TT = "chav1961.bt.paint.bgeditor.ImageEditPanel.EditStateString.font.tt"; 		
		private static final String		KEY_SETTINGS_TT = "chav1961.bt.paint.bgeditor.ImageEditPanel.EditStateString.settings.tt"; 		
		
		private final Localizer			localizer;
		private final JLabel			forPrompt = new JLabel();
		private final JCommandField		forCommand = new JCommandField();
		private final JLabel			coord = new JLabel();
		private final JLabel			size = new JLabel();
		private final JLabel			fontSettings = new JLabel();
		private final SettingsVisual	settings = new SettingsVisual();
		
		private EditStateString(final Localizer localizer) {
			super(new BorderLayout(5,5));
			
			this.localizer = localizer;

			final JPanel	coordinates = new JPanel(new GridLayout(1, 2, 5, 0)); 
			final JPanel	states = new JPanel(new BorderLayout(5, 0)); 
			
			coord.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			size.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			fontSettings.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			settings.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
			
			coordinates.add(coord);
			coordinates.add(size);
			states.add(coordinates, BorderLayout.WEST);
			states.add(fontSettings, BorderLayout.CENTER);
			states.add(settings, BorderLayout.EAST);
			add(forPrompt, BorderLayout.WEST);
			add(forCommand, BorderLayout.CENTER);
			add(states, BorderLayout.EAST);

			fillLocalizedStrings();
		}

		@Override
		public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
			fillLocalizedStrings();
		}

		private void clearCommandString() {
			forCommand.setText("");
			forCommand.requestFocusInWindow();
		}
		
		private void refreshCoordinates(final ImageEditCanvas canvas, final int x, final int y) {
			final Image		image = canvas.getBackgroundImage();
			final String	text = x + " x " + y;
			
			coord.setText(text);
			if (image == null || x >= image.getWidth(null) || y >= image.getHeight(null)) {
				coord.setForeground(Color.LIGHT_GRAY);
			}
			else {
				coord.setForeground(Color.BLACK);
			}
		}
		
		private void refreshSettings(final ImageEditCanvas canvas) {
			final Image		image = canvas.getBackgroundImage();
			final String	aboutSize = image != null ? image.getWidth(null) + " x " + image.getHeight(null) : "unknown";
			final String	aboutFont = canvas.getFont().getFamily()+" "+canvas.getFont().getSize()+"pt"; 

			size.setText(aboutSize);
			fontSettings.setText(aboutFont);
			settings.refreshSettings(canvas);
		}
		
		private void fillLocalizedStrings() {
			forPrompt.setText(localizer.getValue(KEY_PROMPT));
			forCommand.setToolTipText(localizer.getValue(KEY_COMMAND_TT));
			coord.setToolTipText(localizer.getValue(KEY_COORD_TT));
			size.setToolTipText(localizer.getValue(KEY_SIZE_TT));
			fontSettings.setToolTipText(localizer.getValue(KEY_FONT_TT));
			settings.setToolTipText(localizer.getValue(KEY_SETTINGS_TT));
		}
	}
	
	private static class SettingsVisual extends JComponent {
		private static final long serialVersionUID = 1L;

		private int							thickness = 1;
		private StrokeWrapper.LineStroke	stroke = StrokeWrapper.LineStroke.SOLID;
		private StrokeWrapper.LineCaps		caps = StrokeWrapper.LineCaps.BUTT;
		private StrokeWrapper.LineJoin		join = StrokeWrapper.LineJoin.MITER;
		
		private SettingsVisual() {
			setPreferredSize(new Dimension(48,16));
		}
		
		private void refreshSettings(final ImageEditCanvas canvas) {
			setForeground(canvas.getForeground());
			setBackground(canvas.getBackground());
			thickness = canvas.getLineThickness();
			stroke = canvas.getLineStroke();
			caps = canvas.getLineCaps();
			join = canvas.getLineJoin();
			repaint();
		}
		
		@Override
		protected void paintComponent(final Graphics g) {
			final Graphics2D	g2d = (Graphics2D)g;
			final Color			oldColor = g2d.getColor();
			final Stroke		oldStroke = g2d.getStroke();
	
			g2d.setColor(getBackground());
			g2d.setBackground(getBackground());
			g2d.fillRect(0, 0, getWidth(), getHeight());
			g2d.setColor(getForeground());
			g2d.setStroke(ImageUtils.buildStroke(thickness, stroke, caps, join));
			g2d.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
			g2d.setStroke(oldStroke);
			g2d.setColor(oldColor);
		}
	}

	private void turnOffExtractColorButton() {
		((JToggleButton)SwingUtils.findComponentByName(topPanel, "toolbar.colorbar.extract")).setSelected(false);
	}
	
	private void fillLocalizedStrings() {
	}

}
