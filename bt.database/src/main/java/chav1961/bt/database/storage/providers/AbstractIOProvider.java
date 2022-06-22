package chav1961.bt.database.storage.providers;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;

public abstract class AbstractIOProvider implements SpiService<AbstractIOProvider>, Closeable, Flushable {
	public static final String	SCHEME = "ioprovider";

	private final URI						template;
	private final Map<UUID,PageDescriptor>	pageCache = new ConcurrentHashMap<>();
	
	protected AbstractIOProvider(final URI template) {
		this.template = template;
	}

	@Override public abstract AbstractIOProvider newInstance(final URI resource) throws EnvironmentException;
	@Override public abstract void flush() throws IOException;
	@Override public abstract void close() throws IOException;

	public abstract int read(int partId, int poolId, long pageNo, byte[] target) throws IOException;
	public abstract int write(byte[] source, int partId, int poolId, long pageNo) throws IOException;
	
	protected abstract int getPageSize();
	protected abstract long toDisplacement(int partId, int poolId, long pageNo);
	protected abstract long toPartId(long displacement);
	protected abstract long toPoolId(long displacement);
	protected abstract long toPageNo(long displacement);
	
	@Override
	public boolean canServe(final URI resource) {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else {
			return URIUtils.canServeURI(resource, template);
		}
	}

	public URI getProviderURITemplate() {
		return template;
	}

	public byte[] read(final int partId, final int poolId, long pageNo) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	private static class PageDescriptor {
		final int			partId;	
		final int			poolId;
		final long			pageNo;
		final byte[]		content;
		final long			timestamp = System.nanoTime();
		volatile boolean	lock = false;
		
		private PageDescriptor(int partId, int poolId, long pageNo, byte[] content) {
			this.partId = partId;
			this.poolId = poolId;
			this.pageNo = pageNo;
			this.content = content;
		}

		@Override
		public String toString() {
			return "PageDescriptor [partId=" + partId + ", poolId=" + poolId + ", pageNo=" + pageNo + ", timestamp=" + timestamp + ", lock=" + lock + "]";
		}
	}
}
