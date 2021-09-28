package chav1961.bt.lucenewrapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.zip.CRC32;
import java.util.zip.CRC32C;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

import chav1961.purelib.basic.Utils;
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
		return fsi.open("/").list();
	}

	@Override
	public void deleteFile(final String name) throws IOException {
		fsi.open("/"+name).delete();
	}

	@Override
	public long fileLength(final String name) throws IOException {
		return fsi.open(name).size();
	}

	@Override
	public IndexOutput createOutput(final String name, final IOContext context) throws IOException {
		try (final FileSystemInterface	temp = fsi.clone().open("/"+name)) {
			if (temp.exists() && temp.isDirectory()) {
				temp.deleteAll();
			}
			if (!temp.exists()) {
				temp.create();
			}
		}
		final OutputStream	os = fsi.clone().open("/"+name).write();
		final long[]		info = {0, 0};
		
		return new IndexOutput(name,name) {
			final CRC32 crc = new CRC32(); 
			
			@Override
			public void writeBytes(byte[] b, int offset, int length) throws IOException {
				crc.update(b,offset,length);
				info[0] += length;
				os.write(b, offset, length);
			}
			
			@Override
			public void writeByte(byte b) throws IOException {
				info[0]++;
				crc.update(b);
				os.write(b);
			}
			
			@Override
			public long getFilePointer() {
				return info[0];
			}
			
			@Override
			public long getChecksum() throws IOException {
				return crc.getValue();
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
		fsi.open("/"+source).rename(dest);
	}

	@Override
	public IndexInput openInput(final String name, final IOContext context) throws IOException {
		// TODO Auto-generated method stub
		final FileSystemInterface		temp = fsi.clone().open("/"+name);
		final byte[]					content;
		
		try(final InputStream			is = temp.read();
			final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			
			Utils.copyStream(is, baos);
			content = baos.toByteArray();
		}
		
		return new InternalIndexInput(name,content) {
			@Override
			public void close() throws IOException {
				temp.close();
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
	
	private abstract static class InternalIndexInput extends IndexInput {
		final byte[]	buffer = new byte[1];
		final byte[]	content;
		int				pos = 0;
		
		InternalIndexInput(final String name, final byte[]	content) {
			super(name);
			this.content = content;
		}
		
		
		@Override
		public void readBytes(byte[] buf, int offset, int length) throws IOException {
			System.arraycopy(content, pos, buf, offset, length);
			pos += length;
		}
		
		@Override
		public byte readByte() throws IOException {
			readBytes(buffer,0,buffer.length);
			return buffer[0];
		}
		
		@Override
		public IndexInput slice(final String sliceDescription, long offset, long length) throws IOException {
			return new InternalIndexInput(sliceDescription, Arrays.copyOfRange(content, (int)offset, (int)(offset+length))) {
				@Override
				public void close() throws IOException {
				}
			};
		}
		
		@Override
		public void seek(long pos) throws IOException {
			this.pos = (int)pos;
		}
		
		@Override
		public long length() {
			return content.length;
		}
		
		@Override
		public long getFilePointer() {
			return pos;
		}
		
		@Override public abstract void close() throws IOException;
	}
}
