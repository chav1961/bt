package chav1961.bt.database.storage.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public class RandomAccessFileProvider extends AbstractIOProvider {
	public static final String	SUBSCHEME = "raf";
	public static final URI		SUPPORTED_URI  = URI.create(SCHEME+':'+SUBSCHEME+":/");

	private final RandomAccessFile	raf;
	
	public RandomAccessFileProvider() {
		this.raf = null;
	}

	private RandomAccessFileProvider(final File file) throws FileNotFoundException {
		this.raf = new RandomAccessFile(file, "rw");
	}
	
	@Override
	public boolean canServe(final URI resource) {
		return URIUtils.canServeURI(resource, SUPPORTED_URI);
	}
	
	@Override
	public AbstractIOProvider newInstance(final URI resource) throws EnvironmentException {
		try{return new RandomAccessFileProvider(new File(""));
		} catch (FileNotFoundException e) {
			throw new EnvironmentException(e);  
		}
	}

	@Override
	public void flush() throws IOException {
		super.flush();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
		
		if (raf != null) {
			raf.close();
		}
	}
}
