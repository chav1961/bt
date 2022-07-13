package chav1961.bt.paint.script;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.imageio.ImageIO;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.PropertiesWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class SystemWrapperImpl implements SystemWrapper {
	private final SubstitutableProperties	props;
	private final FileSystemInterface		fsi;
	private final URI						home;
	
	public SystemWrapperImpl(final FileSystemInterface fs, final URI homeDir) {
		this(new SubstitutableProperties(), fs, homeDir);
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
		}
	}

	@Override
	public String[] getPropKeys() throws PaintScriptException {
		return props.keySet().toArray(new String[props.size()]);
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
			throw new IllegalArgumentException("Key ["+key+"] doesn't exists"); 
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
			throw new IllegalArgumentException("Awaited class can't be null"); 
		}
		else if (!contains(key)) {
			throw new IllegalArgumentException("Key ["+key+"] doesn't exists"); 
		}
		else {
			return props.getProperty(key, awaited);
		}
	}

	@Override
	public <T> T get(final String key, final Class<T> awaited, final T defaultValue) throws PaintScriptException {
		if (key == null || key.isEmpty()) {
			throw new IllegalArgumentException("Key to get can't be null or empty"); 
		}
		else if (awaited == null) {
			throw new IllegalArgumentException("Awaited class can't be null"); 
		}
		else if (!contains(key)) {
			return defaultValue; 
		}
		else {
			return props.getProperty(key, awaited);
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
					
					return ImageWrapper.of(ImageIO.read(is));
				}
			} catch (IOException e) {
				throw new PaintScriptException(e.getLocalizedMessage(), e);
			}
		}
	}


	@Override
	public void storeImage(final ImageWrapper image, final String file) throws PaintScriptException {
		if (image == null) {
			throw new NullPointerException("Omage to store can't be null"); 
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
	public void print(String message) throws PaintScriptException {
		System.err.println(message);
	}

	private String toPath(final String file) {
		final URI	path = URI.create(file);
		
		if (path.isAbsolute()) {
			return path.toString();
		}
		else {
			return home.relativize(path).toString();
		}
	}
}
