package chav1961.bt.lucenewrapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.zip.CRC32;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.streams.byte2byte.PseudoRandomInputStream;

public class LuceneFileSystemWrapperDirectory extends Directory {
	private final FileSystemInterface	fsi;
	
	public LuceneFileSystemWrapperDirectory(final FileSystemInterface fsi) throws NullPointerException {
		if (fsi == null) {
			throw new NullPointerException("File system interface can't be null"); 
		}
		else {
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
		final FileSystemInterface	output = fsi.clone().open("/"+name);
		final OutputStream			os = output.write();
		
		return new IndexOutput(name, name) {
			final CRC32 crc = new CRC32(); 
			long		len = 0;
			
			@Override
			public void writeBytes(byte[] b, int offset, int length) throws IOException {
				crc.update(b,offset,length);
				len += length;
				os.write(b, offset, length);
			}
			
			@Override
			public void writeByte(byte b) throws IOException {
				len++;
				crc.update(b);
				os.write(b);
			}
			
			@Override
			public long getFilePointer() {
				return len;
			}
			
			@Override
			public long getChecksum() throws IOException {
				return crc.getValue();
			}
			
			@Override
			public void close() throws IOException {
				os.close();
				output.close();
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
		final FileSystemInterface		temp = fsi.clone().open("/"+name);
		final InputStream				is = temp.read();
		final PseudoRandomInputStream	pris = new PseudoRandomInputStream(is);
		
		return new InternalIndexInput(name, pris) {
			@Override
			public void close() throws IOException {
				pris.close();
				is.close();
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
		final PseudoRandomInputStream	content;
		final byte[]					buffer = new byte[1];
		
		InternalIndexInput(final String name, final PseudoRandomInputStream content) {
			super(name);
			this.content = content;
		}
		
		@Override
		public void readBytes(byte[] buf, int offset, int length) throws IOException {
			content.read(buf, offset, length);
		}
		
		@Override
		public byte readByte() throws IOException {
			readBytes(buffer,0,buffer.length);
			return buffer[0];
		}
		
		@Override
		public IndexInput slice(final String sliceDescription, long offset, long length) throws IOException {
			return new InternalIndexInput(sliceDescription, new PseudoRandomInputStream(content, offset, length)) {
				@Override
				public void close() throws IOException {
				}
			};
		}
		
		@Override
		public void seek(long pos) throws IOException {
			content.seek(pos);
		}
		
		@Override
		public long length() {
			try{return content.length();
			} catch (IOException e) {
				return -1;
			}
		}
		
		@Override
		public long getFilePointer() {
			try{return content.getFilePointer();
			} catch (IOException e) {
				return -1;
			}
		}
		
		@Override public abstract void close() throws IOException;
	}
}
