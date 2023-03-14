package chav1961.bt.database.storage;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import chav1961.bt.database.storage.interfaces.Storage;
import chav1961.bt.database.storage.interfaces.StorageDescriptor;
import chav1961.bt.database.storage.interfaces.StoragePart;
import chav1961.bt.database.storage.interfaces.StoragePool;
import chav1961.bt.database.storage.providers.AbstractIOProvider;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.EnvironmentException;

//	Storage hierarchy:
//		- Storage
//			- StoragePart
//				- StoragePool
//					- page
//	Props:
//		io.providers=...,...	- available storage providers
//		root.io.provider=...	- storage provider for root storage
//		read.threads 			- number of threads to read content
//		write.threads 			- number of threads to write content
//

public class AbstractStoreManager implements Closeable, Flushable {
	public static final String				IO_PROVIDERS = "io.providers";
	public static final String				ROOT_IO_PROVIDER = "root.io.providers";
	public static final String				READ_THREADS = "read.threads";
	public static final String				DEFAULT_READ_THREADS = "3";
	public static final String				WRITE_THREADS = "write.threads";
	public static final String				DEFAULT_WRITE_THREADS = "1";
	public static final String				TERMINATION_TIMEOUT = "termination.timeout";
	public static final String				DEFAULT_TERMINATION_TIMEOUT = "10";
	
	private static final int				STORAGE_MAGIC_NUMBER = 0x12345678;
	private static final int				BOOT_STORAGE_ID = -1;
	private static final int				BOOT_PART_ID = 0;
	private static final int				BOOT_POOL_ID = 0;
	
	private static final String[]			MANDATORIES = {IO_PROVIDERS, ROOT_IO_PROVIDER}; 

	private final SubstitutableProperties	props;
	private final AbstractIOProvider[]		providers;
	private final Map<URI,Integer>			providersMap = new HashMap<>();
	private final AbstractIOProvider		rootProvider;
	private final ExecutorService			readService;
	private final ExecutorService			writeService;
	private final StorageDescriptor			desc;
	
	protected AbstractStoreManager(final SubstitutableProperties props) throws NullPointerException, IllegalArgumentException, IOException {
		if (props == null) {
			throw new NullPointerException("Properties can't be null"); 
		}
		else if (!props.containsAllKeys(MANDATORIES)) {
			throw new IllegalArgumentException("Properties doesn't contain some mandatory keys. At least "+Arrays.toString(MANDATORIES)+" must be present"); 
		}
		else {
			final URI[]	candidates = props.getProperty(IO_PROVIDERS, URI[].class);
			final URI	rootCandidate = props.getProperty(ROOT_IO_PROVIDER, URI.class);
			final int	threadIds[] = {1, 1};
			int			index = 0;
			
			this.props = props;
			this.providers = new AbstractIOProvider[candidates.length]; 
			
loop:		for(URI item : candidates) {
				for (AbstractIOProvider service : ServiceLoader.load(AbstractIOProvider.class)) {
					if (service.canServe(item)) {
						providers[index] = service;
						providersMap.put(item, index++);
						continue loop;
					}
				}
				throw new IOException("No any available I/O provider found for URI ["+item+"]");
			}
			if (!providersMap.containsKey(rootCandidate)) {
				throw new IllegalArgumentException("Root provider URI ["+rootCandidate+"] is missing in the I/O providers list"); 
			}
			else {
				try {
					this.rootProvider = providers[providersMap.get(rootCandidate)].newInstance(rootCandidate);
				} catch (EnvironmentException e) {
					throw new IOException("Root I/O provider can't serve URI ["+rootCandidate+"]");
				}
			}
			
			this.readService = Executors.newFixedThreadPool(props.getProperty(READ_THREADS, int.class, DEFAULT_READ_THREADS), (r)->{
				final Thread	t = new Thread(r);
				
				t.setName("AbstractStoreManager.read."+threadIds[0]++);
				return t;
			});
			this.writeService = Executors.newFixedThreadPool(props.getProperty(WRITE_THREADS, int.class, DEFAULT_WRITE_THREADS), (r)->{
				final Thread	t = new Thread(r);
				
				t.setName("AbstractStoreManager.write."+threadIds[0]++);
				return t;
			});
			
			this.desc = loadRootDescriptor(this.rootProvider);
		}
	}

	@Override
	public void flush() throws IOException {
		for(AbstractIOProvider item : providers) {
			item.flush();
		}
	}
	
	@Override
	public void close() throws IOException {
		flush();
		try{writeService.shutdown();
			writeService.awaitTermination(props.getProperty(TERMINATION_TIMEOUT, int.class, DEFAULT_TERMINATION_TIMEOUT), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		try{readService.shutdown();
			readService.awaitTermination(props.getProperty(TERMINATION_TIMEOUT, int.class, DEFAULT_TERMINATION_TIMEOUT), TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			throw new IOException(e);
		}
		for (AbstractIOProvider item : providers) {
			item.close();
		}
	}

	public byte[] read(final int storageId, final int partId, final int poolId, final long pageNo) throws NullPointerException, IllegalArgumentException, IOException {
		if (storageId >= 0 && storageId < providers.length) {
			return providers[storageId].read(partId, poolId, pageNo);
		}
		else if (storageId == BOOT_STORAGE_ID) {
			return rootProvider.read(partId, poolId, pageNo);
		}
		else {
			throw new IllegalArgumentException("Storage id ["+storageId+"] out of range 0.."+(providers.length-1));
		}
	}
	
	public int read(final int storageId, final int partId, final int poolId, final long pageNo, final byte[] target) throws NullPointerException, IllegalArgumentException, IOException {
		if (storageId >= 0 && storageId < providers.length) {
			return providers[storageId].read(partId, poolId, pageNo, target);
		}
		else if (storageId == BOOT_STORAGE_ID) {
			return rootProvider.read(partId, poolId, pageNo, target);
		}
		else {
			throw new IllegalArgumentException("Storage id ["+storageId+"] out of range 0.."+(providers.length-1));
		}
	}

	public int write(final byte[] source, final int storageId, final int partId, final int poolId, final long pageNo) throws NullPointerException, IllegalArgumentException, IOException {
		if (storageId >= 0 && storageId < providers.length) {
			return providers[storageId].write(source, partId, poolId, pageNo);
		}
		else if (storageId == BOOT_STORAGE_ID) {
			return rootProvider.write(source, partId, poolId, pageNo);
		}
		else {
			throw new IllegalArgumentException("Storage id ["+storageId+"] out of range 0.."+(providers.length-1));
		}
	}

	public StorageDescriptor getDescriptor() {
		return desc;
	}
	
	private StorageDescriptor loadRootDescriptor(final AbstractIOProvider provider) throws IOException {
		final byte[]	buffer = new byte[8];
		
		read(BOOT_STORAGE_ID, BOOT_PART_ID, BOOT_POOL_ID, 0, buffer);
		
		try(final InputStream		is = new ByteArrayInputStream(buffer);
			final DataInputStream	dis = new DataInputStream(is)) {
			
			if (dis.readInt() != STORAGE_MAGIC_NUMBER) {
				throw new IOException("Root storage format error: magic number invalid"); 
			}
			else {
				final int	currentSize = dis.readInt();

				if (currentSize <= 0) {
					throw new IOException("Root storage format error: illegal conent size"); 
				}
				else {
					final byte[]	content = new byte[currentSize];

					read(BOOT_STORAGE_ID, BOOT_PART_ID, BOOT_POOL_ID, 0, content);
					
					try(final InputStream		isC = new ByteArrayInputStream(buffer);
						final DataInputStream	disC = new DataInputStream(isC)) {
						
						return new StorageDescriptorImpl(disC); 
					}					
				}
			}
		}
	}

	//	Content of root record: 
	//	- magic (4)
	//  - current size (4) including magic and current size field memory
	//	- number of storages (4)
	//	- strings with URI[number of storages]
	private class StorageDescriptorImpl implements StorageDescriptor {
		private final List<URIAndProvider>	storages = new ArrayList<>();
		
		private StorageDescriptorImpl(final DataInputStream dis) throws IOException {
			final int	magic = dis.readInt(), currentSize = dis.readInt(), numberOfStorages = dis.readInt();
			
loop:		for (int index = 0; index < numberOfStorages; index++) {
				final String	s = dis.readUTF();
				
				try{final URI	storageURI = URI.create(s);
					
					for (AbstractIOProvider item : providers) {
						if (item.canServe(storageURI)) {
							storages.add(new URIAndProvider(storages.size(), storageURI, item.newInstance(storageURI)));
							continue loop;
						}						
					}
					throw new IOException("Root storage error: storage URI ["+s+"] doesn't support by any provider");
				} catch (IllegalArgumentException exc) {
					throw new IOException("Root storage error: illegal storage URI ["+s+"]");
				} catch (EnvironmentException e) {
					throw new IOException("Root storage error: storage URI ["+s+"] provider error: "+e.getLocalizedMessage(), e);
				}
			}
		}

		@Override
		public Iterator<Storage> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getStorageCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int appendStorage(final URI storage) throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}
		
	}
	
	private static class URIAndProvider implements StoragePart {
		private final int					id;
		private final URI					uri;
		private final AbstractIOProvider	provider;
		
		public URIAndProvider(final int id, final URI uri, final AbstractIOProvider provider) {
			this.id = id;
			this.uri = uri;
			this.provider = provider;
		}

		@Override
		public Iterator<StoragePool> iterator() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getPartId() {
			return id;
		}

		@Override
		public long getPartInitialSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getPartIncrement() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getPartMaxExtents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public long getPartCurrentSize() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getPartCurrentExtents() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public int getPoolCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public StoragePool getPool(int poolId) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int createPool(long size, SubstitutableProperties poolProps) throws IOException {
			return createPool(size, 0, 1, poolProps);
		}

		@Override
		public int createPool(long initialSize, long increment, int maxExtents, SubstitutableProperties poolProps) throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void dropPool(int poolId) throws IOException {
			// TODO Auto-generated method stub
			
		}
	}
}
