package chav1961.bt.paint.control;


import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
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
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.UndoManager;

import chav1961.bt.paint.control.ImageEditCanvas.DrawingMode;
import chav1961.bt.paint.control.ImageEditCanvas.LineStroke;
import chav1961.bt.paint.dialogs.AskImageResize;
import chav1961.bt.paint.utils.ApplicationUtils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.i18n.interfaces.LocalizerOwner;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.FunctionalMouseListener;
import chav1961.purelib.ui.swing.interfaces.FunctionalMouseListener.EventType;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JFontSelectionDialog;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;

public class ImageEditPanel extends JPanel implements LocalizerOwner, LocaleChangeListener {
	private static final long 				serialVersionUID = -8630893532191028731L;
	private static final ContentMetadataInterface	xda;
	
	private static final String				KEY_SELECT_COLOR = "chav1961.bt.mnemoed.editor.ImageEditPanel.selectColor";
	
	static {
		try(final InputStream				is = ImageEditPanel.class.getResourceAsStream("imageeditpanel.xml")) {

			xda = ContentModelFactory.forXmlDescription(is);
		} catch (IOException | EnvironmentException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}		
	}

	private final LightWeightListenerList<ChangeListener>	listeners = new LightWeightListenerList<>(ChangeListener.class);
	private final Localizer			localizer;
	private final JPanel			topPanel = new JPanel();
	private final JPanel			leftPanel = new JPanel();
	private final ImageEditCanvas	canvas;
	private final EditStateString	state;
	private boolean 				foregroundNow = true;
	private boolean 				waitColorExtraction = false;
	
	public ImageEditPanel(final Localizer localizer, final int editHistoryLength) throws NullPointerException {
		super(new BorderLayout());
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (editHistoryLength <= 0) {
			throw new IllegalArgumentException("Edit history length ["+editHistoryLength+"] must be positive");
		}
		else {
			this.localizer = localizer;
			this.canvas = new ImageEditCanvas(localizer, editHistoryLength);
			this.state = new EditStateString(localizer);

			topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
			leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

			leftPanel.add(prepareModeToolBar());
			leftPanel.add(prepareActionToolBar());
			
			topPanel.add(prepareColorToolBar());
			topPanel.add(prepareSettingsToolBar());
			topPanel.add(preparePlayerToolBar());
	        
	        add(topPanel, BorderLayout.NORTH);
	        add(leftPanel, BorderLayout.WEST);
	        add(new JScrollPane(canvas), BorderLayout.CENTER);
	        add(state, BorderLayout.SOUTH);

	        canvas.addChangeListener((l)->{
	        	listeners.fireEvent((ls)->ls.stateChanged(l));
	        	state.refreshSettings(canvas);
	        });
	        canvas.addMouseMotionListener(new MouseMotionListener() {
				@Override public void mouseMoved(MouseEvent e) {state.refreshCoordinates(e.getX(), e.getY());}
				@Override public void mouseDragged(MouseEvent e) {state.refreshCoordinates(e.getX(), e.getY());}
			});
	        canvas.addMouseListener((FunctionalMouseListener)(ct, e)->{
	        	if (ct == EventType.CLICKED && waitColorExtraction && getImage() != null) {
	        		setColor(new Color(((BufferedImage)getImage()).getRGB(e.getX(), e.getY())));
	        		turnOffExtractColorButton();
	        		waitColorExtraction = false;
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
	
	@OnAction("action:/chooseColor")
    public void chooseColor(final Hashtable<String,String[]> colors) {
		switch (colors.get("color")[0]) {
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
    }

	@OnAction("action:/switchColor")
    public void switchColor(final Hashtable<String,String[]> modes) {
		foregroundNow = !foregroundNow;
	}
	
	@OnAction("action:/chooseMode")
    public void chooseMode(final Hashtable<String,String[]> modes) throws IOException {
		canvas.setCurrentDrawMode(DrawingMode.valueOf(modes.get("mode")[0]));
    }
	
	@OnAction("action:/crop")
	public void crop() {
		if (canvas.getSelection() != null) {
			canvas.setBackgroundImage(ImageUtils.cropImage((BufferedImage) canvas.getBackgroundImage(), canvas.getSelection(), null));
		}
	}
	
	@OnAction("action:/resize")
	public void resize() {
		try{final AskImageResize	air = new AskImageResize(SwingUtils.getNearestLogger(this));
		
			if (ApplicationUtils.ask(air, getLocalizer(), 300, 145)) {
				canvas.setBackgroundImage(ImageUtils.resizeImage((BufferedImage) canvas.getBackgroundImage(), air.width, air.height, canvas.getBackground(), air.stretchContent, air.fromCenter, null));
			}
		} catch (ContentException e) {
			SwingUtils.getNearestLogger(this).message(Severity.error, e.getLocalizedMessage());
		}
	}
	
	@OnAction("action:/rotate")
	public void rotate() {
		canvas.setBackgroundImage(ImageUtils.rotateImage((BufferedImage) canvas.getBackgroundImage(), false, null));
	}
	
	@OnAction("action:/reflectVert")
	public void reflectV() {
		canvas.setBackgroundImage(ImageUtils.mirrorImage((BufferedImage) canvas.getBackgroundImage(), false, null));
	}
	
	@OnAction("action:/reflectHor")
	public void reflectH() {
		canvas.setBackgroundImage(ImageUtils.mirrorImage((BufferedImage) canvas.getBackgroundImage(), true, null));
	}

	@OnAction("action:/toGrayScale")
	public void toGrayScale() {
		canvas.setBackgroundImage(ImageUtils.grayScaleImage((BufferedImage) canvas.getBackgroundImage(), null));
	}
	
	@OnAction("action:/transparency")
	public void makeTransparent() {
		if (foregroundNow) {
			canvas.setBackgroundImage(ImageUtils.transparentImage((BufferedImage) canvas.getBackgroundImage(), canvas.getForeground(), false,null));
		}
		else {
			canvas.setBackgroundImage(ImageUtils.transparentImage((BufferedImage) canvas.getBackgroundImage(), canvas.getBackground(), true, null));
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
		canvas.setLineStroke(LineStroke.valueOf(modes.get("style")[0]));
	}

	@OnAction("action:/settings.filling")
	public void setFilling(final Hashtable<String,String[]> modes) {
	}

	@OnAction("action:/player.recording")
	public void recording(final Hashtable<String,String[]> modes) {
	}	

	@OnAction("action:/player.pause")
	public void pause(final Hashtable<String,String[]> modes) {
	}	

	@OnAction("action:/player.play")
	public void play(final Hashtable<String,String[]> modes) {
	}	
	
	public UndoManager getUndoManager() {
		return canvas.getUndoManager();
	}
	
	public Image getImage() {
		return canvas.getBackgroundImage();
	}
	
	public void setImage(final Image image) {
		if (image == null) {
			throw new NullPointerException("Image to set can't be null");
		}
		else {
			canvas.setBackgroundImage(image);
		}
	}
	
	public DrawingMode getCurrentDrawingMode() {
		return canvas.getCurrentDrawMode();
	}
	
	public boolean isImageAreaSelected() {
		return false;
	}
	
	public Image getSelectedImage() {
		return null;
	}
	
	public void pasteImage(final Image image) {
		
	}
	
	private void chooseColor() {
        final JColorChooser	chooser = new  JColorChooser(canvas.getForeground());
        final Color[]		temp = new Color[] {canvas.getForeground()}; 
        
        chooser.getSelectionModel().addChangeListener((e)->temp[0] = chooser.getColor());
        if (new JLocalizedOptionPane(localizer).confirm(null, chooser, KEY_SELECT_COLOR, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        	setColor(temp[0]);
        }
	}

	private void extractColor() {
		if (getImage() != null) {
			waitColorExtraction = true;
		}
		else {
			turnOffExtractColorButton();
			waitColorExtraction = false;
		}
	}
	
	private void setColor(final Color color) {
		if (foregroundNow) {
			canvas.setForeground(color);
		}
		else {
			canvas.setBackground(color);
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

	private JToolBar preparePlayerToolBar() {
	    final JToolBar	result = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.playerBar")), JToolBar.class);
	
	    result.setFloatable(false);
	    result.setOrientation(JToolBar.HORIZONTAL);
	    SwingUtils.assignActionListeners(result, this);
	    return result;
	}
	
	private static class EditStateString extends JPanel implements LocaleChangeListener {
		private static final long serialVersionUID = 1L;

		private static final String		KEY_PROMPT = "chav1961.bt.mnemoed.bgeditor.ImageEditPanel.EditStateString.prompt";
		private static final String		KEY_COMMAND_TT = "chav1961.bt.mnemoed.bgeditor.ImageEditPanel.EditStateString.path.tt"; 		
		private static final String		KEY_COORD_TT = "chav1961.bt.mnemoed.bgeditor.ImageEditPanel.EditStateString.coord.tt"; 		
		private static final String		KEY_SIZE_TT = "chav1961.bt.mnemoed.bgeditor.ImageEditPanel.EditStateString.size.tt"; 		
		private static final String		KEY_FONT_TT = "chav1961.bt.mnemoed.bgeditor.ImageEditPanel.EditStateString.font.tt"; 		
		private static final String		KEY_SETTINGS_TT = "chav1961.bt.mnemoed.bgeditor.ImageEditPanel.EditStateString.settings.tt"; 		
		
		private final Localizer			localizer;
		private final JLabel			forPrompt = new JLabel();
		private final JTextField		forPath = new JTextField();
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
			add(forPath, BorderLayout.CENTER);
			add(states, BorderLayout.EAST);

			fillLocalizedStrings();
		}

		@Override
		public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
			fillLocalizedStrings();
		}

		private void refreshCoordinates(final int x, final int y) {
			final String	text = x + " x " + y;
			
			coord.setText(text);
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
			forPath.setToolTipText(localizer.getValue(KEY_COMMAND_TT));
			coord.setToolTipText(localizer.getValue(KEY_COORD_TT));
			size.setToolTipText(localizer.getValue(KEY_SIZE_TT));
			fontSettings.setToolTipText(localizer.getValue(KEY_FONT_TT));
			settings.setToolTipText(localizer.getValue(KEY_SETTINGS_TT));
		}
	}
	
	private static class SettingsVisual extends JComponent {
		private static final long serialVersionUID = 1L;

		private int			thickness = 1;
		private LineStroke	stroke = LineStroke.SOLID;
		
		private SettingsVisual() {
			setPreferredSize(new Dimension(48,16));
		}
		
		private void refreshSettings(final ImageEditCanvas canvas) {
			setForeground(canvas.getForeground());
			setBackground(canvas.getBackground());
			thickness = canvas.getLineThickness();
			stroke = canvas.getLineStroke();
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
			switch (stroke) {
				case DASHED	:
					g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, thickness, new float[] {3 * thickness}, 0));
					break;
				case DOTTED	:
					g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, thickness, new float[] {thickness}, 0));
					break;
				case SOLID	: 
					g2d.setStroke(new BasicStroke(thickness)); 		
					break;
				default:
					break;
			}
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
