package chav1961.bt.lucenewrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class LuceneFileSystemWrapperDirectory extends Directory {
	private final LoggerFacade			logger;
	private final FileSystemInterface	fsi;
	
	public LuceneFileSystemWrapperDirectory(final LoggerFacade logger, final FileSystemInterface fsi) throws NullPointerException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else {
			this.logger = logger;
			this.fsi = fsi;
		}
	}
	
	@Override
	public String[] listAll() throws IOException {
		return fsi.list();
	}

	@Override
	public void deleteFile(final String name) throws IOException {
		fsi.open(name).delete();
	}

	@Override
	public long fileLength(final String name) throws IOException {
		return fsi.open(name).size();
	}

	@Override
	public IndexOutput createOutput(final String name, final IOContext context) throws IOException {
		final OutputStream	os = fsi.clone().open(name).write();
		final long[]		info = {0, 0};
		
		return new IndexOutput(name,name) {
			@Override
			public void writeBytes(byte[] b, int offset, int length) throws IOException {
				for (byte i : b) {
					info[1] += i;
				}
				info[0] += length;
				os.write(b, offset, length);
			}
			
			@Override
			public void writeByte(byte b) throws IOException {
				info[0]++;
				info[1] += b;
				os.write(b);
			}
			
			@Override
			public long getFilePointer() {
				return info[0];
			}
			
			@Override
			public long getChecksum() throws IOException {
				return info[1];
			}
			
			@Override
			public void close() throws IOException {
				os.close();
			}
		};
	}

	@Override
	public IndexOutput createTempOutput(final String prefix, final String suffix, final IOContext context) throws IOException {
		return createOutput(prefix+UUID.randomUUID()+suffix, context);
	}

	@Override
	public void sync(final Collection<String> names) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void syncMetaData() throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public void rename(final String source, final String dest) throws IOException {
		fsi.open(source).rename(dest);
	}

	@Override
	public IndexInput openInput(final String name, final IOContext context) throws IOException {
		// TODO Auto-generated method stub
		final InputStream	is = fsi.clone().open(name).read();
		
		return new IndexInput(name) {
			
			@Override
			public void readBytes(byte[] arg0, int arg1, int arg2) throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public byte readByte() throws IOException {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
				// TODO Auto-generated method stub
				return null;
			}
			
			@Override
			public void seek(long pos) throws IOException {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public long length() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public long getFilePointer() {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public void close() throws IOException {
				is.close();
			}
		}; 
	}

	@Override
	public Lock obtainLock(final String name) throws IOException {
		fsi.lock(name, false);
		
		return new Lock() {
			@Override
			public void ensureValid() throws IOException {
				// TODO Auto-generated method stub
			}
			
			@Override
			public void close() throws IOException {
				fsi.unlock(name, false);
			}
		};
	}

	@Override
	public Set<String> getPendingDeletions() throws IOException {
		return Set.of();
	}
	
	@Override
	public void close() throws IOException {
		fsi.close();
	}
}
