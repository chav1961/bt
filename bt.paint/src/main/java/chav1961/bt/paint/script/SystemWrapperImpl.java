package chav1961.bt.paint.script;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Set;

import javax.imageio.ImageIO;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.PropertiesWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class SystemWrapperImpl implements SystemWrapper {
	private final SubstitutableProperties	props;
	private final FileSystemInterface		fsi;
	private final URI						home;
	private final LoggerFacade				logger;

	public SystemWrapperImpl(final LoggerFacade logger, final FileSystemInterface fs, final URI homeDir) {
		this(logger, new SubstitutableProperties(System.getProperties()), fs, homeDir);
	}

	public SystemWrapperImpl(final LoggerFacade logger, final SubstitutableProperties settings, final FileSystemInterface fs, final URI homeDir) {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (settings == null) {
			throw new NullPointerException("Settings can't be null");
		}
		else if (fs == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else if (homeDir == null) {
			throw new NullPointerException("Home directory can't be null");
		}
		else {
			this.props = settings;
			this.fsi = fs;
			this.home = homeDir; 
			this.logger = logger;
		}
	}
	
	public SystemWrapperImpl(final FileSystemInterface fs, final URI homeDir) {
		this(new SubstitutableProperties(System.getProperties()), fs, homeDir);
	}
	
	public SystemWrapperImpl(final SubstitutableProperties settings, final FileSystemInterface fs, final URI homeDir) {
		if (settings == null) {
			throw new NullPointerException("Settinga can't be null");
		}
		else if (fs == null) {
			throw new NullPointerException("File system interface can't be null");
		}
		else if (homeDir == null) {
			throw new NullPointerException("Home directory can't be null");
		}
		else {
			this.props = settings;
			this.fsi = fs;
			this.home = homeDir; 
			this.logger = null;
		}
	}

	@Override
	public String[] getPropKeys() throws PaintScriptException {
		final Set<String>	keys = props.availableKeys();
		
		return keys.toArray(new String[keys.size()]);
	}

	@Override
	public boolean contains(final String key) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to test can't be null or empty"); 
		}
		else {
			return props.containsKey(key);
		}
	}

	@Override
	public String get(final String key) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (!contains(key)) {
			throw new PaintScriptException("Key ["+key+"] doesn't exists"); 
		}
		else {
			return props.getProperty(key);
		}
	}

	@Override
	public String get(final String key, final String defaultValue) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (defaultValue == null) {
			throw new NullPointerException("Defauly value can't be null"); 
		}
		else {
			return props.getProperty(key, defaultValue);
		}
	}

	@Override
	public <T> T get(final String key, final Class<T> awaited) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else if (!contains(key)) {
			throw new PaintScriptException("Key ["+key+"] doesn't exists"); 
		}
		else {
			try {
				return props.getProperty(key, awaited);
			} catch (UnsupportedOperationException exc) {
				throw new PaintScriptException(exc.getLocalizedMessage(), exc); 
			}
		}
	}

	@Override
	public <T> T get(final String key, final Class<T> awaited, final T defaultValue) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null"); 
		}
		else if (!contains(key)) {
			return defaultValue; 
		}
		else {
			try {
				return props.getProperty(key, awaited);
			} catch (UnsupportedOperationException exc) {
				throw new PaintScriptException(exc.getLocalizedMessage(), exc); 
			}
		}
	}

	@Override
	public boolean exists(final String file) throws PaintScriptException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File path can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file))) {

				return temp.exists();
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e); 
			}
		}
	}

	@Override
	public boolean isFile(final String file) throws PaintScriptException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File path can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file))) {

				return temp.isFile();
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e); 
			}
		}
	}

	@Override
	public boolean isDirectory(final String file) throws PaintScriptException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File path can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file))) {

				return temp.isDirectory();
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e); 
			}
		}
	}
	
	@Override
	public boolean mkdir(final String file) throws PaintScriptException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File path can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file))) {

				temp.mkDir();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	}

	@Override
	public boolean rm(final String file) throws PaintScriptException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File path can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file))) {

				temp.deleteAll();
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	}

	@Override
	public boolean ren(final String file, final String newFile) throws PaintScriptException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File path can't be null or empty"); 
		}
		else if (newFile == null || newFile.isEmpty()) {
			throw new IllegalArgumentException("New file path can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file))) {

				temp.rename(newFile);
				return true;
			} catch (IOException e) {
				return false;
			}
		}
	}

	@Override
	public String[] list(final String dir) throws PaintScriptException {
		if (dir == null || dir.isEmpty()) {
			throw new IllegalArgumentException("File path can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(dir))) {

				return temp.list();
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e); 
			}
		}
	}
	
	@Override
	public ImageWrapper loadImage(final String file) throws PaintScriptException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File name to load image from can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file))) {
				try(final InputStream		is = temp.read()) {
					final BufferedImage		image = ImageIO.read(is);
					final ImageWrapper		wrapper = new ImageWrapperImpl(image);
					
					wrapper.setName(file);
					wrapper.setFormat("png");
					return wrapper;
				}
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e);
			}
		}
	}


	@Override
	public void storeImage(final ImageWrapper image, final String file) throws PaintScriptException {
		if (image == null) {
			throw new NullPointerException("Image to store can't be null"); 
		}
		else if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File name to store image to can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file)).create()) {
				try(final OutputStream		os = temp.write()) {
					
					ImageIO.write((RenderedImage)image.getImage(),image.getFormat(),os);
				}
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public PropertiesWrapper loadProps(final String file) throws PaintScriptException {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File name to load properties from can't be null or empty"); 
		}
		else {
			final SubstitutableProperties	result = new SubstitutableProperties(props);
			
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file))) {
				try(final InputStream		is = temp.read()) {
					
					result.load(is);
					return PropertiesWrapper.of(result);
				}
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public void storeProps(final PropertiesWrapper props, final String file) throws PaintScriptException {
		if (props == null) {
			throw new NullPointerException("Properties to store can't be null"); 
		}
		else if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("File name to store properties to can't be null or empty"); 
		}
		else {
			try(final FileSystemInterface	temp = fsi.clone().open(toPath(file)).create()) {
				try(final OutputStream		os = temp.write()) {
				
					props.getProperties().store(os, "");
				}
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public void print(final String message) throws PaintScriptException {
		if (message == null) {
			throw new NullPointerException("Message to print can't be null");
		}
		else if (logger != null) {
			logger.message(Severity.info, message);
		}
		else {
			System.err.println(message);
		}
	}

	@Override
	public String console(final String command, final Predefines predef) throws PaintScriptException {
		if (command == null) {
			throw new NullPointerException("Command can't be null");
		}
		else if (predef == null) {
			throw new NullPointerException("Predefined map can't be null");
		}
		else if (!command.isEmpty()) {
			for (String line : command.split("\n")) {
				try{Console.processCommand(line, predef);
				} catch (SyntaxException  e) {
					throw new PaintScriptException(e);
				}
			}
			return "";
		}
		else {
			return "";
		}
	}
	
	private String toPath(final String file) {
		final URI	path = URI.create(file);
		
		if (path.isAbsolute()) {
			return path.toString();
		}
		else {
			return URIUtils.appendRelativePath2URI(home, file).toString();
		}
	}
}
