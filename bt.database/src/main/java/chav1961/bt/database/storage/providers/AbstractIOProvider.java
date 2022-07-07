package chav1961.bt.database.storage.providers;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.net.URI;

import chav1961.purelib.basic.LongIdMap;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;

public abstract class AbstractIOProvider implements SpiService<AbstractIOProvider>, Closeable, Flushable {
	public static final String	SCHEME = "ioprovider";

	public static final String	KEY_INITIAL_SIZE = "initialSize"; 
	public static final String	KEY_INCREMENT = "increment"; 
	public static final String	KEY_MAX_EXTENTS = "maxExtents";
	
	private final URI							template;
	private final LongIdMap<PageDescriptor>		pages = new LongIdMap<>(PageDescriptor.class);
	private final Object						ioLocker = new Object();
	
	protected AbstractIOProvider(final URI template) {
		this.template = template;
	}

	@Override public abstract AbstractIOProvider newInstance(final URI resource) throws EnvironmentException;
	@Override public abstract void flush() throws IOException;
	@Override public abstract void close() throws IOException;

	public abstract void prepareStorage(URI storage, SubstitutableProperties props) throws IOException;
	public abstract int createPart(SubstitutableProperties props) throws IOException;
	public abstract void dropPart(int part) throws IOException;
	public abstract int createPool(int part, SubstitutableProperties props) throws IOException;
	public abstract void dropPool(int part, int pool) throws IOException;
	
	protected abstract int getPageSize();
	protected abstract long toDisplacement(int partId, int poolId, long pageNo) throws IOException;
	protected abstract int toPartId(long displacement) throws IOException;
	protected abstract int toPoolId(long displacement) throws IOException;
	protected abstract long toPageNo(long displacement) throws IOException;
	protected abstract void loadPage(long displacement, byte[] target) throws IOException;
	protected abstract void storePage(byte[] source, long displacement) throws IOException;
	
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

	public byte[] getPage(final int partId, final int poolId, long pageNo, boolean exclusive) throws IOException, InterruptedException {
		synchronized (pages) {
			if (contains(partId, poolId, pageNo)) {
				while (!lockPage(partId, poolId, pageNo, exclusive)) {
					pages.wait();
				}
				return getFromCache(partId, poolId, pageNo);
			}
		}
		
		final byte[]	result;
		
		synchronized (ioLocker) {
			final byte[]			content = createPage(); 
			
			loadPage(toDisplacement(partId, poolId, pageNo), content);
			synchronized (pages) {
				putToCache(partId, poolId, pageNo, content, exclusive);
			}
			result = content;
		}
		return result;
	}

	public void freePage(final int partId, final int poolId, long pageNo) throws IOException, InterruptedException {
		synchronized (pages) {
			if (contains(partId, poolId, pageNo)) {
				unlockPage(partId, poolId, pageNo);
				pages.notifyAll();
				return;
			}
		}
		throw new IOException("Page ["+partId+","+poolId+","+pageNo+"] to free is missing in the page cache");
	}

	protected byte[] createPage() {
		return new byte[getPageSize()];
	}

	protected void freePage(final byte[] page) {
	}
	
	protected boolean contains(final int partId, final int poolId, long pageNo) throws IOException {
		return pages.contains(toDisplacement(partId, poolId, pageNo));
	}

	protected byte[] getFromCache(final int partId, final int poolId, long pageNo) throws IOException {
		final PageDescriptor	desc = pages.get(toDisplacement(partId, poolId, pageNo));
		final long				time = System.nanoTime(); 
		
		desc.lastUsed = time;
		return desc.content;
	}
	
	protected void putToCache(final int partId, final int poolId, long pageNo, final byte[] content, final boolean exclusize) throws IOException {
		final PageDescriptor	desc = new PageDescriptor(partId, poolId, pageNo, content);
		final long				displ = toDisplacement(partId, poolId, pageNo);
		final long				timestamp= System.nanoTime();
	
		if (exclusize) {
			desc.exclusive = true;
			desc.lastWrite = timestamp;
		}
		else {
			desc.exclusive = false;
			desc.lastUsed = timestamp;
			desc.sharedCount++;
		}
		pages.put(displ, desc);
	}

	protected void clearCache() throws IOException {
		final boolean[]	bugs = new boolean[] {false, false};
		
		synchronized (pages) {
			pages.walk((id,content)->{
				try{while (!lockPage(toPartId(id), toPoolId(id), toPageNo(id), true)) {
						try{pages.wait();
						} catch (InterruptedException e) {
							bugs[1] = true;
						}
					}
					storePage(content.content, id);
					unlockPage(toPartId(id), toPoolId(id), toPageNo(id));	
					freePage(content.content);
				} catch (IOException exc) {
					bugs[0] = true;
				}
			});
			pages.clear();
		}
		if (bugs[1]) {
			Thread.currentThread().interrupt();
		}
		if (bugs[0]) {
			throw new IOException();
		}
	}

	protected boolean lockPage(final int partId, final int poolId, final long pageNo, boolean exclusive) throws IOException {
		final PageDescriptor	desc = pages.get(toDisplacement(partId, poolId, pageNo));
		
		if (desc.exclusive) {
			return false;
		}
		else if (exclusive) {
			desc.exclusive = true;
			return true;
		}
		else {
			desc.sharedCount++;
			return true;
		}
	}

	protected void unlockPage(int partId, int poolId, long pageNo) throws IOException {
		final PageDescriptor	desc = pages.get(toDisplacement(partId, poolId, pageNo));
		
		if (desc.exclusive) {
			desc.exclusive = false;
		}
		else {
			if (--desc.sharedCount < 0) {
				throw new IOException("Shared count underflow"); 
			}
		}
	}

	protected static long extractLong(final byte[] content, final int displ) {
		return (((long)content[displ + 0] << 56) +
                ((long)(content[displ + 1] & 255) << 48) +
                ((long)(content[displ + 2] & 255) << 40) +
                ((long)(content[displ + 3] & 255) << 32) +
                ((long)(content[displ + 4] & 255) << 24) +
                ((content[displ + 5] & 255) << 16) +
                ((content[displ + 6] & 255) <<  8) +
                ((content[displ + 7] & 255) <<  0));
	}

	protected static int extractInt(final byte[] content, final int displ) {
		return (((int)(content[displ + 0] & 255) << 24) +
                ((content[displ + 1] & 255) << 16) +
                ((content[displ + 2] & 255) <<  8) +
                ((content[displ + 3] & 255) <<  0));
	}
	
	protected static void placeLong(final byte[] content, final int displ, final long value) {
        content[displ + 0] = (byte)(value >>> 56);
        content[displ + 1] = (byte)(value >>> 48);
        content[displ + 2] = (byte)(value >>> 40);
        content[displ + 3] = (byte)(value >>> 32);
        content[displ + 4] = (byte)(value >>> 24);
        content[displ + 5] = (byte)(value >>> 16);
        content[displ + 6] = (byte)(value >>>  8);
        content[displ + 7] = (byte)(value >>>  0);
	}

	protected static void placeInt(final byte[] content, final int displ, final int value) {
        content[displ + 0] = (byte)(value >>> 24);
        content[displ + 1] = (byte)(value >>> 16);
        content[displ + 2] = (byte)(value >>>  8);
        content[displ + 3] = (byte)(value >>>  0);
	}
	
	private static class PageDescriptor {
		final int			partId;	
		final int			poolId;
		final long			pageNo;
		final byte[]		content;
		final long			timestamp = System.nanoTime();
		volatile long		sharedCount = 0;
		volatile boolean	exclusive = false;
		volatile long		lastUsed;
		volatile long		lastWrite;
		
		private PageDescriptor(int partId, int poolId, long pageNo, byte[] content) {
			this.partId = partId;
			this.poolId = poolId;
			this.pageNo = pageNo;
			this.content = content;
		}

		@Override
		public String toString() {
			return "PageDescriptor [partId=" + partId + ", poolId=" + poolId + ", pageNo=" + pageNo + ", timestamp=" + timestamp + ", sharedCount=" + sharedCount + ", exclusive=" + exclusive + ", lastUsed=" + lastUsed + ", lastWrite=" + lastWrite + "]";
		}
	}
}
