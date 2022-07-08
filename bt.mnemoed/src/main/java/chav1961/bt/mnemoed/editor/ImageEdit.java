package chav1961.bt.mnemoed.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import  javax.swing.filechooser.FileFilter;

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
	
	static {
		try(final InputStream				is = ImageEdit.class.getResourceAsStream("application.xml")) {

			xda = ContentModelFactory.forXmlDescription(is);
		} catch (IOException | EnvironmentException e) {
			throw new PreparationException(e.getLocalizedMessage(), e);
		}		
	}
	
    // Режим рисования 
	public enum DrawMode {
		UNKNOWN, PEN, BRUSH, ERASE, TEXT, LINE, ELLIPSE, RECT, FILL 
	}
	private DrawMode	currentDrawMode = DrawMode.PEN;
    private Color 		currentColor;
	
    int  xPad;
    int  xf;
    int  yf;
    int  yPad;
    int  thickness;
    boolean pressed=false;
    // текущий цвет
    MyFrame f;
    MyPanel japan;
    // поверхность рисования
    BufferedImage imag;
    // если мы загружаем картинку
    boolean loading=false;
    String fileName;
    Localizer	localizer = PureLibSettings.PURELIB_LOCALIZER;
    public ImageEdit() {
        f = new MyFrame("Графический редактор");
        
        f.setSize(640,480);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        currentColor=Color.black;
         
        final JMenuBar menuBar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")), JMenuBar.class); 
		
        SwingUtils.assignActionListeners(menuBar, this);
        		
        japan = new  MyPanel();
        japan.setBounds(30,30,260,260);
        japan.setBackground(Color.white);
        japan.setOpaque(true);
        f.add(japan);
        
        f.setJMenuBar(menuBar);
        
        final JToolBar	modeToolbar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.modeBar")), JToolBar.class);
        
        modeToolbar.setFloatable(false);
        modeToolbar.setOrientation(JToolBar.VERTICAL);
        modeToolbar.setBounds(0, 0, 32, 300);
        SwingUtils.assignActionListeners(modeToolbar, this);

        f.add(modeToolbar);

        final JToolBar	colorToolbar = SwingUtils.toJComponent(xda.byUIPath(URI.create("ui:/model/navigation.top.colorBar")), JToolBar.class);

        colorToolbar.setFloatable(false);
        colorToolbar.setOrientation(JToolBar.HORIZONTAL);
        colorToolbar.setBounds(32, 0, 550, 32);
        SwingUtils.assignActionListeners(colorToolbar, this);
        

        f.add(colorToolbar);
           
          japan.addMouseMotionListener(new  MouseMotionAdapter()
                  {
                      public void mouseDragged(MouseEvent e) 
                      { 
                          if (pressed==true)
                          {
                          Graphics g = imag.getGraphics();
                          Graphics2D g2 = (Graphics2D)g;
                          // установка цвета
                          g2.setColor(currentColor);
                          switch (currentDrawMode)
                          {
                              // карандаш
                              case LINE:
                                  g2.drawLine(xPad, yPad, e.getX(), e.getY());
                                  break;
                              // кисть
                              case BRUSH:
                                  g2.setStroke(new  BasicStroke(3.0f));
                                  g2.drawLine(xPad, yPad, e.getX(), e.getY());
                                  break;
                               // ластик
                              case ERASE:
                                   g2.setStroke(new  BasicStroke(3.0f));
                                   g2.setColor(Color.WHITE);
                                   g2.drawLine(xPad, yPad, e.getX(), e.getY());
                                    break;
                          }
                          xPad=e.getX();
                          yPad=e.getY();
                          }
                          japan.repaint();
                      }
                  });
          japan.addMouseListener(new  MouseAdapter()
                  {
                     public void mouseClicked(MouseEvent e) {
                           
                     Graphics g = imag.getGraphics();
                     Graphics2D g2 = (Graphics2D)g;
                     // установка цвета
                          g2.setColor(currentColor);
                          switch (currentDrawMode)
                          {
                              // карандаш
                              case LINE:
                                  g2.drawLine(xPad, yPad, xPad+1, yPad+1);
                                  break;
                              // кисть
                              case BRUSH:
                                  g2.setStroke(new  BasicStroke(3.0f));
                                  g2.drawLine(xPad, yPad, xPad+1, yPad+1);
                                  break;
                               // ластик
                              case ERASE:
                                  g2.setStroke(new  BasicStroke(3.0f));
                                   g2.setColor(Color.WHITE);
                                   g2.drawLine(xPad, yPad, xPad+1, yPad+1);
                              break;
                              // текст
                              case TEXT:
                                  // устанавливаем фокус для панели,
                                  // чтобы печатать на ней текст
                                  japan.requestFocus();
                              break;       
                          }
                          xPad=e.getX();
                          yPad=e.getY();
                           
                          pressed=true;
                          japan.repaint();      
                   }
                     public void mousePressed(MouseEvent e) {
                         xPad=e.getX();
                          yPad=e.getY();
                          xf=e.getX();
                          yf=e.getY();
                          pressed=true;
                        }
                    public void mouseReleased(MouseEvent e) {
                         
                        Graphics g = imag.getGraphics();
                        Graphics2D g2 = (Graphics2D)g;
                        // установка цвета
                          g2.setColor(currentColor);
                        // Общие рассчеты для овала и прямоугольника
                        int  x1=xf, x2=xPad, y1=yf, y2=yPad;
                                  if(xf>xPad)
                                  {
                                     x2=xf; x1=xPad; 
                                  }
                                  if(yf>yPad)
                                  {
                                     y2=yf; y1=yPad; 
                                  }
                        switch(currentDrawMode)
                        {
                             // линия
                              case LINE:
                                 g.drawLine(xf, yf, e.getX(), e.getY());
                                  break;
                              // круг
                              case ELLIPSE:
                                  g.drawOval(x1, y1, (x2-x1), (y2-y1));
                                  break;
                                  // прямоугольник
                              case RECT:
                                  g.drawRect(x1, y1, (x2-x1), (y2-y1));
                                  break;
                        }
                        xf=0; yf=0;
                        pressed=false;
                        japan.repaint();
                    }
                  });
        japan.addKeyListener(new  KeyAdapter()
                {
                    public void keyReleased(KeyEvent e)
                    {
                        // устанавливаем фокус для панели,
                       // чтобы печатать на ней текст
                        japan.requestFocus();
                    }
                    public void keyTyped(KeyEvent e) 
                    {
                        if(currentDrawMode==DrawMode.TEXT){
                        Graphics g = imag.getGraphics();
                        Graphics2D g2 = (Graphics2D)g;
                        // установка цвета
                          g2.setColor(currentColor);
                        g2.setStroke(new  BasicStroke(2.0f));
                         
                         String str = new  String("");
                         str+=e.getKeyChar();
                        g2.setFont(new  Font("Arial", 0, 15));
                        g2.drawString(str, xPad, yPad);
                        xPad+=10;
                        // устанавливаем фокус для панели,
                        // чтобы печатать на ней текст
                        japan.requestFocus();
                        japan.repaint();
                        }
                    }
                });
        f.addComponentListener(new  ComponentAdapter() {
                public void componentResized(java.awt.event.ComponentEvent evt) {
                    // если делаем загрузку, то изменение размеров формы
                    // отрабатываем в коде загрузки
                   if(loading==false)
                   {
                    japan.setSize(f.getWidth()-40, f.getHeight()-80);
                    BufferedImage tempImage = new  BufferedImage(japan.getWidth(), japan.getHeight(), BufferedImage.TYPE_INT_RGB);
                             Graphics2D d2 = (Graphics2D) tempImage.createGraphics();
                        d2.setColor(Color.white);
                        d2.fillRect(0, 0, japan.getWidth(), japan.getHeight());
                    tempImage.setData(imag.getRaster());
                    imag=tempImage;
                    japan.repaint();
                   }
                     loading=false;
                }
                });
        f.setLayout(null);
        setColor(Color.BLACK);
        f.setVisible(true);
    }

	@OnAction("action:/newImage")
    public void newImage() {
    	
    }
    
	@OnAction("action:/loadImage")
    public void loadImage() {
        JFileChooser jf= new  JFileChooser();
        int  result = jf.showOpenDialog(null);
         if(result==JFileChooser.APPROVE_OPTION)
          {
            try
            {
                // при выборе изображения подстраиваем размеры формы
                // и панели под размеры данного изображения
                 fileName = jf.getSelectedFile().getAbsolutePath();
                 File iF= new  File(fileName);
                 jf.addChoosableFileFilter(new  TextFileFilter(".png"));
                 jf.addChoosableFileFilter(new  TextFileFilter(".jpg"));
                 imag = ImageIO.read(iF);
                 loading=true;
                 f.setSize(imag.getWidth()+40, imag.getWidth()+80);
                 japan.setSize(imag.getWidth(), imag.getWidth());
                  japan.repaint();
              } catch (FileNotFoundException ex) {
                  JOptionPane.showMessageDialog(f, "Такого файла не существует");
              } 
              catch (IOException ex) {
                  JOptionPane.showMessageDialog(f, "Исключение ввода-вывода");
              }
            catch (Exception ex) {
              }
          }
    }
    
	@OnAction("action:/saveImage")
    public void saveImage() {
        try
        {
            JFileChooser jf= new  JFileChooser();
            // Создаем фильтры  файлов
            TextFileFilter pngFilter = new TextFileFilter(".png");
            TextFileFilter jpgFilter = new TextFileFilter(".jpg");
            if(fileName==null)
            {
                // Добавляем фильтры
                 jf.addChoosableFileFilter(pngFilter);
                 jf.addChoosableFileFilter(jpgFilter);
                int  result = jf.showSaveDialog(null);
                if(result==JFileChooser.APPROVE_OPTION)
                {
                    fileName = jf.getSelectedFile().getAbsolutePath();
                }
                }
            // Смотрим какой фильтр выбран
            if(jf.getFileFilter()==pngFilter)
            {
                 ImageIO.write(imag, "png", new  File(fileName+".png"));
            }
            else
            {
                ImageIO.write(imag, "jpeg", new  File(fileName+".jpg"));
            }     
        }
        catch(IOException ex)
        {
           JOptionPane.showMessageDialog(f, "Ошибка ввода-вывода");
        }
    }
    
	@OnAction("action:/saveImageAs")
    public void saveImageAs() {
        try
        {
            JFileChooser jf= new  JFileChooser();
            // Создаем фильтры для файлов
            TextFileFilter pngFilter = new  TextFileFilter(".png");
            TextFileFilter jpgFilter = new  TextFileFilter(".jpg");
                // Добавляем фильтры
                 jf.addChoosableFileFilter(pngFilter);
                  jf.addChoosableFileFilter(jpgFilter);
                int  result = jf.showSaveDialog(null);
                if(result==JFileChooser.APPROVE_OPTION)
                {
                    fileName = jf.getSelectedFile().getAbsolutePath();
                }
          // Смотрим какой фильтр выбран
          if(jf.getFileFilter()==pngFilter)
            {
                 ImageIO.write(imag, "png", new  File(fileName+".png"));
            }
         else
            {
                ImageIO.write(imag, "jpeg", new  File(fileName+".jpg"));
            }             
        }
        catch(IOException ex)
        {
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
        if (new JLocalizedOptionPane(localizer).confirm(null, chooser, "?", JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
        	setColor(temp[0]);
        }
	}

	@OnAction("action:/chooseMode")
    public void chooseMode(final Hashtable<String,String[]> modes) {
		currentDrawMode = DrawMode.valueOf(modes.get("mode")[0]);
    }
	
	private void setColor(final Color color) {
		japan.setBorder(new LineBorder(currentColor = color, 3));
	}
	
    public static void main(String[] args) {
    	SwingUtilities.invokeLater(()->new ImageEdit());        
    }
     
     class MyFrame extends JFrame
     {
         public void paint(Graphics g)
         {
             super.paint(g);
         }
         public MyFrame(String title)
         {
             super(title);
         }
     }
 
     class MyPanel extends JPanel
     {
         public MyPanel()
         { }
       public void paintComponent (Graphics g)
          {
            if(imag==null)
             {
                 imag = new  BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_RGB);
                 Graphics2D d2 = (Graphics2D) imag.createGraphics();
                 d2.setColor(Color.white);
                 d2.fillRect(0, 0, this.getWidth(), this.getHeight());
             }
             super.paintComponent(g);
             g.drawImage(imag, 0, 0,this);      
          }
     }
     // Фильтр картинок
     class TextFileFilter extends FileFilter 
     {
         private String ext;
         public TextFileFilter(String ext)
         {
             this.ext=ext;
         }
         public boolean accept(java.io.File file) 
         {
              if (file.isDirectory()) return true;
              return (file.getName().endsWith(ext));
         }
         public String getDescription() 
         {
              return "*"+ext;
         }
     }
}