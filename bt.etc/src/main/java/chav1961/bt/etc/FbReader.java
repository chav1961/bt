package chav1961.bt.etc;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.mozilla.universalchardet.UniversalDetector;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import chav1961.purelib.basic.SimpleInitialContextFactory;
import chav1961.purelib.basic.Utils;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class FbReader extends Application  {
   
   // Путь к тестовому файлу FB2
   private static final String EPUB_FILE = "c:/tmp/x.fb2";

   WebEngine 	webEngine;
   FB2Content	fb2Content;
   
   public static void main(String[] args) {
	   System.setProperty("java.naming.factory.initial", SimpleInitialContextFactory.class.getName());
	   launch(args);
   }

   public void start(final Stage stage) throws Exception {
      final WebView 	webView = new WebView();

      webEngine = webView.getEngine();
      
      // Замена встроенных стилей CSS на собственные
      webEngine.setUserStyleSheetLocation("data:, @font-face {font-family: 'Open Sans', "
            + "sans-serif; src: local('Open Sans'), url(fonts/OpenSans-Regular.ttf);} "
            + "body {width: 90% !important; padding-left: 10px; font-size: 14pt; "
            + "font-family: 'Open Sans', sans-serif; line-height: 1.5;} "
            + "img {max-width: 90%; height: auto;}");

      final BorderPane 	borderPane = new BorderPane();
      
      final HBox hbox = new HBox(8); // spacing = 8
      
      hbox.getChildren().addAll(new Label("Name:"), new Text());
      
      final Menu menu1 = new Menu("File");
      final Menu menu2 = new Menu("Help");
      final MenuBar menuBar = new MenuBar();
      final MenuItem menuItem = new MenuItem("Description");
      
      menuItem.setOnAction(new EventHandler<ActionEvent>() {
          @Override 
          public void handle(ActionEvent e) {
              showDescritiption(fb2Content.attr);
          }
      });
      menu2.getItems().addAll(menuItem);
      
      menuBar.getMenus().addAll(menu1, menu2);      
      
      borderPane.setTop(menuBar);
      borderPane.setCenter(webView);
      borderPane.setBottom(hbox);

      // Создание сцены и отображение окна
      final Scene 		scene = new Scene(borderPane);
      
      stage.setTitle("test");
      stage.setScene(scene);
      stage.setHeight(900);
      borderPane.setPrefHeight(5000);
      stage.show();
      
      // Удаление временной папки при закрытии окна
      stage.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {           
  //       Utils.deleteDir(new File(tempDir.toString()));
     });

      fb2Content = loadContent();
      
	   webEngine.loadContent(fb2Content.body);
   }
   
   protected void showDescritiption(final Map<String, Map<String, String>> attr) {
       final Alert alert = new Alert(AlertType.INFORMATION);
       final WebView webView = new WebView();
       final StringBuilder sb = new StringBuilder();
       
       sb.append("<html><table>");
       for (Entry<String, Map<String, String>> item : attr.entrySet()) {
    	   sb.append("<tr><td>").append(item.getKey()).append("</td><td>").append(item.getValue()).append("</td></tr>");
       }
       sb.append("</table></html>");
       webView.getEngine().loadContent(sb.toString());
       webView.setPrefSize(640, 480);
       
       alert.setTitle("Description");
       alert.setHeaderText(attr.get("title-info/book-title").get("text").toString());
       alert.getDialogPane().setContent(webView);       

       alert.showAndWait();
   }

// Метод для копирования файла
   public static void copyFile(File src, File dest) throws IOException {
     Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
   }

   private static FB2Content loadContent() throws IOException, ParserConfigurationException, SAXException {
	      // Создаем временную папку в папке проекта
	      String filePath = new File("").getAbsolutePath();
	      Path tempDir = Files.createTempDirectory(Paths.get(filePath), "temp");
	      
	      // Каталог, в который нужно сохранить все ресурсы
	      String outputDir = String.valueOf(tempDir) + "\\";
	      
	      // Создаем копию файла и переименовываем расширение в xml
	      try {
	         copyFile(new File(EPUB_FILE), new File(outputDir + "temp.txt"));
	      } catch (java.nio.file.NoSuchFileException e) {
	         Alert alert = new Alert(AlertType.WARNING);
	         alert.setTitle("Файл не найден");
	         alert.setHeaderText("Ошибка при открытии файла");
	         alert.setContentText("Такого файла не существует.");

	         alert.showAndWait();

	         File tmpFls = new File(String.valueOf(tempDir));
	         Utils.deleteDir(tmpFls);          
	      }  
	      
	      // Получение всего текста из файла temp.txt
	      String tempFilePath = outputDir + "temp.txt";
	      String content = Utils.fromResource(new FileReader(new File(tempFilePath), detectCharset(tempFilePath)));
	      
	      
	      SAXParserFactory factory = SAXParserFactory.newInstance();
	      SAXParser saxParser = factory.newSAXParser();
	      SaxHandler sax = new SaxHandler();      
	      
	      saxParser.parse(new InputSource(new StringReader(content)), sax);
	    
	      // Получение текста между тегами description
	      int start = content.indexOf("<description>");
	      int end = content.lastIndexOf("</description>");

	      end = end + 14;

	      char[] dest = new char[end - start];
	      content.getChars(start, end, dest, 0);
	      String description = new String(dest);
	      description = description.replace("image l:href=\"#", "img src=\"namingrepo:/images/");
//	      description = description.replace("image l:href=\"#", "img src=\"file://" + tempDir.toString().replace("\\","/") + "/");
	      
	      // Получение текста между тегами body      
	      int startBody = content.indexOf("<body>");
	      int endBody = content.lastIndexOf("</body>");

	      endBody = endBody + 7;

	      char[] dst = new char[endBody - startBody];
	      content.getChars(startBody, endBody, dst, 0);
	      String body = new String(dst);
	      body = body.replace("image l:href=\"#", "img src=\"namingrepo:/images/");
//	      body = body.replace("image l:href=\"#", "img src=\"file://" + tempDir.toString().replace("\\", "/") + "/");
	      
	      // Помещаем весь текстовый контент в одну переменную
	      return new FB2Content(description + "\n" + body, sax.attr);
   }
   
   private static Charset detectCharset(final String filePath) throws IOException {
     try (InputStream inputStream = new FileInputStream(filePath)) {
         final byte[] 				bytes = new byte[4096];
         final UniversalDetector	detector = new UniversalDetector(null);
         
         int nread;
         while ((nread = inputStream.read(bytes)) > 0 && !detector.isDone()) {
             detector.handleData(bytes, 0, nread);
         }
         detector.dataEnd();
         return Charset.forName(detector.getDetectedCharset());
     }
  }

   private static String detectImageType(final String name) throws IOException {
       switch (name.substring(name.lastIndexOf('.')+1)) {
       		case "jpg" : return "jpg"; 
       		case "png" : return "png"; 
       		case "gif" : return "gif";
       		default :
       			throw new IOException("Unsupported image format ["+name.substring(name.lastIndexOf('.')+1)+"]");
       }
   }
   
   private static class SaxHandler extends DefaultHandler {
	   final StringBuilder		sbDescription = new StringBuilder();
	   final StringBuilder		sbContent = new StringBuilder();
	   final StringBuilder		sbBinary = new StringBuilder();
	   final Map<String, Map<String, String>>	attr = new HashMap<>();
	   
	   private final List<String>		tags = new ArrayList<>();
	   private final Map<String, Image>	images = new HashMap<>();
	   private String					imageId;
	   
	   boolean descriptionWasDetected = false;
	   boolean bodyWasDetected = false;
	   boolean binaryWasDetected = false;
	   
	   @Override
	   public void characters(char[] ch, int start, int length) throws SAXException {
		   if (bodyWasDetected) {
			   sbContent.append(ch, start, length);
		   }
		   else if (binaryWasDetected) {
			   sbBinary.append(ch, start, length);
		   }
		   else if (descriptionWasDetected) {
			   sbDescription.append(ch, start, length);
		   }
	   }
		
	   @Override
	   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		   if ("body".equals(qName)) {
			   bodyWasDetected = true;
		   }
		   else if ("binary".equals(qName)) {
			   imageId = attributes.getValue("id");
			   sbBinary.setLength(0);
			   binaryWasDetected = true;
		   }
		   else if ("description".equals(qName)) {
			   descriptionWasDetected = true;
		   }
		   else if (descriptionWasDetected) {
			   final Map<String, String>	props = new HashMap<>();
			   
			   for(int index = 0; index < attributes.getLength(); index++) {
				   props.put(attributes.getQName(index), attributes.getValue(index));
			   }
			   tags.add(qName);
			   attr.put(concat(tags), props);
			   sbDescription.setLength(0);
		   }
	   }
			   
	   private String concat(final List<String> tags) {
		   final StringBuilder sb = new StringBuilder();
		   
		   for(String item : tags) {
			   sb.append('/').append(item);
		   }
		   return sb.isEmpty() ? "" : sb.substring(1);
	   }

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ("body".equals(qName)) {
			bodyWasDetected = false;
		}
		else if ("binary".equals(qName)) {
			binaryWasDetected = false;
			try {
				final BufferedImage 	img = ImageIO.read(new ByteArrayInputStream(Base64.getMimeDecoder().decode(sbBinary.toString())));
	        	   
				images.put(imageId, img);
			} catch (IOException e) {
				throw new SAXException(e);
			}
		}
		else if ("description".equals(qName)) {
			descriptionWasDetected = false;
		}
		else if (descriptionWasDetected) {
			final String	key = concat(tags);
			
			if (attr.containsKey(key) && !sbDescription.toString().trim().isEmpty()) {
				attr.get(key).put("text", sbDescription.toString());
			}
			tags.remove(tags.size()-1);
		}
	   }
	
		@Override
		public void endDocument() throws SAXException {
			try {
		      for (Entry<String, Image> entity : images.entrySet()) {
		          final URL				url = new URL("namingrepo:/images/"+entity.getKey());
		          final URLConnection	conn = url.openConnection();
		          
		          conn.setDoOutput(true);
		          try(final OutputStream	os = conn.getOutputStream()) {
		              ImageIO.write((RenderedImage)entity.getValue(), detectImageType(entity.getKey()), os);
		          }
		      }
			} catch (IOException exc) {
				throw new SAXException(exc); 
			}
		}
   }
   
   private static class FB2Content {
	   final String		body;
	   final Map<String, Map<String, String>>	attr;
	   
	   FB2Content(String body, Map<String, Map<String, String>> attr) {
		   this.body = body;
		   this.attr = attr;
	   }
   }
}


/*
module fb2 {
	requires transitive chav1961.purelib;
	requires java.base;
	requires java.desktop;
	requires java.datatransfer;
	requires jdk.javadoc;
	requires javafx.base;
	requires juniversalchardet;
	requires transitive javafx.graphics;
	requires javafx.controls;
	requires javafx.web;
	requires java.xml;
	
	exports com.example; 
}

     <dependency>
        <groupId>com.googlecode.juniversalchardet</groupId>
        <artifactId>juniversalchardet</artifactId>
        <version>1.0.3</version>
    </dependency>

 */

 