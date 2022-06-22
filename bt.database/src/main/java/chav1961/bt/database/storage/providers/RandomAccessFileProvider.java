package chav1961.bt.database.storage.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public class RandomAccessFileProvider extends AbstractIOProvider {
	public static final String	SUBSCHEME = "file";
	public static final URI		SUPPORTED_URI  = URI.create(SCHEME+':'+SUBSCHEME+":/");
	public static final int		PAGE_SIZE = 8192;

	private final RandomAccessFile	raf;
	
	public RandomAccessFileProvider() {
		super(SUPPORTED_URI);
		this.raf = null;
	}

	private RandomAccessFileProvider(final URI file) throws FileNotFoundException {
		super(SUPPORTED_URI);
		
		final File	f = new File(file.getSchemeSpecificPart());
		
		this.raf = new RandomAccessFile(f, "rw");
	}
	
	@Override
	public AbstractIOProvider newInstance(final URI resource) throws EnvironmentException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else if (canServe(resource)) {
			try{final URI	f = URI.create(resource.getRawSchemeSpecificPart());
					
				return new RandomAccessFileProvider(f);
			} catch (FileNotFoundException e) {
				throw new EnvironmentException(e);  
			}
		}
		else {
			throw new EnvironmentException("URI ["+resource+"] doesn't support by this provider");
		}
	}

	@Override
	public int read(int partId, int poolId, long pageNo, byte[] target) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int write(byte[] source, int partId, int poolId, long pageNo) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void flush() throws IOException {
	}
	
	@Override
	public void close() throws IOException {
		if (raf != null) {
			raf.close();
		}
	}

	@Override
	public byte[] read(int partId, int poolId, long pageNo) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getPageSize() {
		return PAGE_SIZE;
	}

	@Override
	protected long toDisplacement(final int partId, final int poolId, final long pageNo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected long toPartId(final long displacement) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected long toPoolId(final long displacement) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected long toPageNo(final long displacement) {
		// TODO Auto-generated method stub
		return 0;
	}
}
