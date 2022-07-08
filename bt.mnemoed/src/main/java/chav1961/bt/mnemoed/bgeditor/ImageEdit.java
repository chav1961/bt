package chav1961.bt.mnemoed.bgeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import  javax.swing.filechooser.FileFilter;

import chav1961.bt.mnemoed.interfaces.DrawingMode;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.ContentModelFactory;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JLocalizedOptionPane;
 
public class ImageEdit {
	static final ContentMetadataInterface	xda;
	
	private static final String				KEY_SELECT_COLOR = "chav1961.bt.mnemoed.editor.ImageEdit.selectColor";
	
	static {
		try(final InputStream				is = ImageEdit.class.getResourceAsStream("application.xml")) {

			xda = ContentModelFactory.forXmlDescription(is);
		} catch (IOException | EnvironmentException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}		
	}
	
//    private DrawingMode	currentDrawMode = DrawingMode.PEN;
    private Color 		currentColor;
	
    int  xPad;
    int  xf;
    int  yf;
    int  yPad;
    int  thickness;
    boolean pressed=false;
    // текущий цвет
    JFrame f;
    final ImageEditCanvas japan;
    // поверхность рисования
    BufferedImage imag;
    // если мы загружаем картинку
    boolean loading=false;
    String fileName;
    final Localizer	localizer;
    public ImageEdit() {
        f = new JFrame("Графический редактор");
        
        this.localizer = Localizer.Factory.newInstance(xda.getRoot().getLocalizerAssociated()); 
        PureLibSettings.PURELIB_LOCALIZER.push(localizer);
        
        f.setSize(640,480);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        currentColor=Color.black;
        final JMenuBar menuBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
		
        SwingUtils.assignActionListeners(menuBar, this);
        		
        japan = new ImageEditCanvas(PureLibSettings.PURELIB_LOCALIZER, 10);
        f.add(new JScrollPane(japan), BorderLayout.CENTER);
         
        f.setJMenuBar(menuBar);
        
        final JToolBar	modeToolbar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.modeBar")), JToolBar.class);
        
        modeToolbar.setFloatable(false);
        modeToolbar.setOrientation(JToolBar.VERTICAL);
        SwingUtils.assignActionListeners(modeToolbar, this);

        final JPanel	leftPanel = new JPanel();
        
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.add(modeToolbar);
        
        f.add(leftPanel, BorderLayout.WEST);

        final JToolBar	colorToolbar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.colorBar")), JToolBar.class);

        colorToolbar.setFloatable(false);
        colorToolbar.setOrientation(JToolBar.HORIZONTAL);
        SwingUtils.assignActionListeners(colorToolbar, this);
        
        final JPanel	topPanel = new JPanel();
        
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.add(colorToolbar);
        
        f.add(topPanel, BorderLayout.NORTH);
           
        f.setVisible(true);
    }

	@OnAction("action:/newImage")
    public void newImage() {
    	japan.setBackgroundImage(new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR));
    	fileName = null;
    }
    
	@OnAction("action:/loadImage")
    public void loadImage() {
        final JFileChooser jf= new  JFileChooser();
        
        jf.addChoosableFileFilter(new ImageFileFilter(".png"));
        jf.addChoosableFileFilter(new ImageFileFilter(".jpg"));
        
        if(jf.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            try {
                 fileName = jf.getSelectedFile().getAbsolutePath();
                 loading = true;
                 
                 japan.setBackgroundImage(ImageIO.read(new File(fileName)));
            } catch (IOException ex) {
            	JOptionPane.showMessageDialog(f, "�?сключение ввода-вывода");
            }
        }
    }
    
	@OnAction("action:/saveImage")
    public void saveImage() {
		if (fileName == null) {
			saveImageAs();
		}
		else {
			try {
	            if(fileName.endsWith(".png")) {
	                ImageIO.write(imag, "png", new  File(fileName+".png"));
	            }
	            else {
	            	ImageIO.write(imag, "jpeg", new  File(fileName+".jpg"));
	            }             
			} catch(IOException ex) {
				JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
			}
		}
    }
    
	@OnAction("action:/saveImageAs")
    public void saveImageAs() {
        try {
            final JFileChooser 	jf = new JFileChooser();
            
            jf.addChoosableFileFilter(new ImageFileFilter(".png"));
            jf.addChoosableFileFilter(new ImageFileFilter(".jpg"));
            
            if(jf.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
                fileName = jf.getSelectedFile().getAbsolutePath();
                
                if(fileName.endsWith(".png")) {
                    ImageIO.write(imag, "png", new  File(fileName+".png"));
                }
                else {
                	ImageIO.write(imag, "jpeg", new  File(fileName+".jpg"));
                }             
            }
        } catch(IOException ex) {
           JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
        }
    }

	@OnAction("action:/exit")
    public void exit() {
		System.exit(0);
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
			default : throw new UnsupportedOperationException("Color type ["+colors.get("color")[0]+"] is not supported yet"); 
		}
    }

	private void chooseColor() {
        final JColorChooser	chooser = new  JColorChooser(currentColor);
        final Color[]		temp = new Color[] {currentColor}; 
        
        chooser.getSelectionModel().addChangeListener((e)->temp[0] = chooser.getColor());
        if (new JLocalizedOptionPane(localizer).confirm(null, chooser, KEY_SELECT_COLOR, JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        	setColor(temp[0]);
        }
	}

	@OnAction("action:/chooseMode")
    public void chooseMode(final Hashtable<String,String[]> modes) throws IOException {
		japan.setCurrentDrawMode(DrawingMode.valueOf(modes.get("mode")[0]));
    }
	
	private void setColor(final Color color) {
		japan.setBorder(new LineBorder(currentColor = color, 3));
		japan.setForeground(color);
	}
	
	private static class ImageFileFilter extends FileFilter {
	     private String ext;
	     
	     public ImageFileFilter(String ext) {
	         this.ext=ext;
	     }
	     
	     public boolean accept(File file) {
	    	 if (file.isDirectory()) {
	    		 return true;
	    	 }
	    	 else {
	    		 return (file.getName().endsWith(ext));
	    	 }
	     }
	     
	     public String getDescription() {
	          return "*"+ext;
	     }
	}

    public static void main(String[] args) {
    	SwingUtilities.invokeLater(()->new ImageEdit());        
    }
}