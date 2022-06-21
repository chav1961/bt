package chav1961.bt.database.storage.providers;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ExecutorService;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;

public abstract class AbstractIOProvider implements SpiService<AbstractIOProvider>, Closeable, Flushable {
	public static final String	SCHEME = "iop";

	@Override public abstract boolean canServe(final URI resource);
	@Override public abstract AbstractIOProvider newInstance(final URI resource) throws EnvironmentException;

	public int read(final ExecutorService service, final int partId, final int poolId, final long pageNo, final byte[] target, final boolean readOnly) throws NullPointerException, IllegalArgumentException, IOException {
		return 0;
	}	

	public int read(final int partId, final int poolId, final long pageNo, final byte[] target, final boolean readOnly) throws NullPointerException, IllegalArgumentException, IOException {
		return 0;
	}	
	
	public int write(final ExecutorService service, final byte[] source, final int partId, final int poolId, final long pageNo) throws NullPointerException, IllegalArgumentException, IOException {
		return 0;
	}

	public int write(final byte[] source, final int partId, final int poolId, final long pageNo) throws NullPointerException, IllegalArgumentException, IOException {
		return 0;
	}
	
	@Override
	public void flush() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
}
