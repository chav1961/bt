package chav1961.bt.database.storage.providers;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.URI;
import java.util.UUID;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;

// first storage page:
//- first free page ref (long)
//- magic (int);
//- initial size (long)
//- increment (long)
//- current extents (int)
//- max extents (int)
//- next 'first page' content (long)
//- number of parts inside (int)
//- parts[]
//-- part UUID
//-- part initial size (long)
//-- part increment size (long)
//-- part current extents (int)
//-- part max extents (int)
//-- first part page ref (long)
// next storage page:
//- next 'first page' content (long)
//- continued parts[]
//- ...
//first part page:
//- first free page ref (long)
//- next 'first page' content (long)
//- initial size (long)
//- increment (long)
//- current extents (int)
//- max extents (int)
//- number of pools inside (int)
//- pools[]
//-- pool UUID
//-- pool initial size (long)
//-- pool increment size (long)
//-- pool current extents (int)
//-- pool max extents (int)
//-- first pool page ref (long)
//next part page:
//- next 'first page' content (long)
//- continued pools[]
//- ...
//first pool page:
//- first free page ref inside pool (long)
//- first used page ref inside pool (long)
//free page:
//- next free page ref (long)
//- not used

public class RandomAccessFileProvider extends AbstractIOProvider {
	public static final String	SUBSCHEME = "file";
	public static final URI		SUPPORTED_URI  = URI.create(SCHEME+':'+SUBSCHEME+":/");
	public static final int		PAGE_SIZE = 8192;
	public static final int		MAGIC = 0x12345678;
	public static final long	NOTHING = -1L;

	private final RandomAccessFile	raf;
	private final long				initialSize;
	private final long				increment;
	private final int				maxExtents;
	
	private volatile int			currentExtents;
	private volatile int			partCount;
	private volatile long			freePageRef = NOTHING;
	private volatile PartDesc[]		parts; 
	
	public RandomAccessFileProvider() {
		super(SUPPORTED_URI);
		this.raf = null;
		this.initialSize = 0;
		this.increment = 0;
		this.maxExtents = 0;
	}

	private RandomAccessFileProvider(final URI file) throws IOException {
		super(SUPPORTED_URI);
		this.raf = openFile(file);
		
		final GrowableByteArray	gba = new GrowableByteArray(false);
		final byte[]	buffer = new byte[getPageSize()];  
		long			currentPage = 0;
		int				contentDispl = 20;
		long			forInitialSize = 0, forIncrement = 0; 
		int				forCurrentExtents = 0, forMaxExtents = 0; 
		
		do {loadPage(currentPage, buffer);
			if (currentPage == 0) {
				if (extractInt(buffer, 8) != MAGIC) {
					throw new IOException("Illegal storage magic, file corrupted"); 
				}
				freePageRef = extractLong(buffer, 0);
				forInitialSize = extractLong(buffer, 12);
				forIncrement = extractLong(buffer, 20);
				forCurrentExtents = extractInt(buffer, 28);
				forMaxExtents = extractInt(buffer, 36);
			}
			gba.append(buffer, contentDispl, buffer.length - contentDispl);
			currentPage = extractLong(buffer, contentDispl - 8);
			contentDispl = 8;
		} while (currentPage != NOTHING);

		this.initialSize = forInitialSize;
		this.increment = forIncrement;
		this.currentExtents = forCurrentExtents;
		this.maxExtents = forMaxExtents;
		
		try(final InputStream		is = gba.getInputStream();
			final DataInputStream	dis = new DataInputStream(is)) {
			
			this.partCount = dis.readInt();
			this.parts = new PartDesc[this.partCount];
			
			for(int index = 0, maxIndex = parts.length; index < maxIndex; index++) {
				final UUID	uuid = new UUID(dis.readLong(), dis.readLong());
				final long	initialSize = dis.readLong();
				final long	increment = dis.readLong();
				final int	currentExtents = dis.readInt();
				final int	maxExtents = dis.readInt();
				final long	pageRef = dis.readLong();
				
				this.parts[index] = new PartDesc(uuid, initialSize, increment, currentExtents, maxExtents, pageRef);
			}
		}
	}

	@Override
	public void prepareStorage(final URI storage, final SubstitutableProperties props) throws IOException {
		final long	initialSize = props.getProperty(KEY_INITIAL_SIZE, long.class);
		final long	increment = props.getProperty(KEY_INCREMENT, long.class, "0");
		final int	maxExtents = props.getProperty(KEY_MAX_EXTENTS, int.class, "1");
		
		try(final RandomAccessFile		raf = openFile(storage)) {
			try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
				final DataOutputStream		dos = new DataOutputStream(baos)) {
				
				dos.writeInt(MAGIC);
				dos.writeLong(1);
				dos.writeLong(initialSize);
				dos.writeLong(increment);
				dos.writeInt(0);
				dos.writeInt(maxExtents);
				dos.writeLong(NOTHING);
				dos.writeInt(0);
				for(int index = 44; index < getPageSize(); index++) {
					dos.write(0);
				}
				dos.flush();
				raf.write(baos.toByteArray());
			}
			
			final byte[]	buffer = new byte[getPageSize()];
			
			for(long index = 1, maxIndex = (initialSize + getPageSize() - 1) / getPageSize(); index < maxIndex; index++) {
				placeLong(buffer, 0, index == maxIndex - 2 ? NOTHING : index + 1);
				raf.write(buffer);
			}
		}
	}
	
	@Override
	public AbstractIOProvider newInstance(final URI resource) throws EnvironmentException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else if (canServe(resource)) {
			try{final URI	f = URI.create(resource.getRawSchemeSpecificPart());
					
				return new RandomAccessFileProvider(f);
			} catch (IOException e) {
				throw new EnvironmentException(e);  
			}
		}
		else {
			throw new EnvironmentException("URI ["+resource+"] doesn't support by this provider");
		}
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
	protected int getPageSize() {
		return PAGE_SIZE;
	}

	@Override
	protected long toDisplacement(final int partId, final int poolId, final long pageNo) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int toPartId(final long displacement) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected int toPoolId(final long displacement) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected long toPageNo(final long displacement) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int createPart(final SubstitutableProperties props) throws IOException {
		final long	initialSize = props.getProperty(KEY_INITIAL_SIZE, long.class);
		final long	increment = props.getProperty(KEY_INCREMENT, long.class, "0");
		final int	maxExtents = props.getProperty(KEY_MAX_EXTENTS, int.class, "1");
		
		final byte[]	buffer = new byte[8192];
		
		for(long index = 0, maxIndex = (increment + getPageSize() - 1) / getPageSize(); index < maxIndex; index++) {
			final long	freePage = nextFreePage(buffer);

			placeLong(buffer, 0, index == maxIndex -1 ? NOTHING : index + 1);
			if (index == 0) {
				placeLong(buffer, 8, NOTHING);
				placeLong(buffer, 16, initialSize);
				placeLong(buffer, 24, increment);
				placeInt(buffer, 32, 0);
				placeInt(buffer, 36, maxExtents);
				placeInt(buffer, 40, 0);
			}
			storePage(buffer, freePage * getPageSize());
		}
		
		// TODO Auto-generated method stub
		flush();
		return 0;
	}

	@Override
	public void dropPart(final int part) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int createPool(final int part, final SubstitutableProperties props) throws IOException {
		final long	initialSize = props.getProperty(KEY_INITIAL_SIZE, long.class);
		final long	increment = props.getProperty(KEY_INCREMENT, long.class, "0");
		final int	maxExtents = props.getProperty(KEY_MAX_EXTENTS, int.class, "1");
		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void dropPool(final int part, final int pool) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void loadPage(final long displacement, final byte[] target) throws IOException {
		raf.seek(displacement);
		if (raf.read(target) != target.length) {
			throw new IOException("Truncated content at displacement ["+displacement+"]"); 
		}
	}

	@Override
	protected void storePage(byte[] source, long displacement) throws IOException {
		raf.seek(displacement);
		raf.write(source);
	}

	private long nextFreePage(final byte[] buffer) throws IOException {
		ensureFreePages();
		loadPage(freePageRef, buffer);
		return extractLong(buffer,0);
	}
	
	private void ensureFreePages() throws IOException {
		if (freePageRef == NOTHING) {
			if (currentExtents + 1 >= maxExtents) {
				throw new IOException("No free pages in the storage, but ");
			}
			else {
				freePageRef = extendStorage();
				currentExtents++;
			}
		}
	}

	private long extendStorage() throws IOException {
		final byte[]	buffer = new byte[getPageSize()];
		final long		fromLocation = (raf.length() + getPageSize() - 1) / getPageSize();

		raf.seek(raf.length());
		for(long index = 0, maxIndex = (increment + getPageSize() - 1) / getPageSize(); index < maxIndex; index++) {
			placeLong(buffer, 0, index == maxIndex - 1 ? NOTHING : index + fromLocation + 1);
			raf.write(buffer);
		}
		return fromLocation;
	}
	
	private static RandomAccessFile openFile(final URI file) throws IOException {
		final File	f = new File(file.getSchemeSpecificPart());
		
		return new RandomAccessFile(f, "rw");
	}


	
	private static class PartDesc {
		final UUID	uuid;
		final long 	initialSize;
		final long 	increment;
		int			currentExtents;
		final int	maxExtents;
		final long	firstPageRef;
		
		private PartDesc(UUID uuid, long initialSize, long increment, int currentExtents, int maxExtents, long firstPageRef) {
			this.uuid = uuid;
			this.initialSize = initialSize;
			this.increment = increment;
			this.currentExtents = currentExtents;
			this.maxExtents = maxExtents;
			this.firstPageRef = firstPageRef;
		}

		@Override
		public String toString() {
			return "PartDesc [uuid=" + uuid + ", initialSize=" + initialSize + ", increment=" + increment + ", currentExtents=" + currentExtents + ", maxExtents=" + maxExtents + ", firstPageRef=" + firstPageRef + "]";
		}
	}
}
