package chav1961.bt.lightrepo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import chav1961.bt.lightrepo.TransactionalFileSystemOnFile.ChangeLogRecord.Operation;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

class TransactionalFileSystemOnFile extends FileSystemOnFile {
	private final List<ChangeLogRecord>			changeLog;
	private final TransactionalFileSystemOnFile	parent;

	TransactionalFileSystemOnFile() throws IOException {
		this.parent = null;
		this.changeLog = null;
	}
	
	TransactionalFileSystemOnFile(final URI rootPath) throws IOException {
		super(rootPath);
		this.parent = null;
		this.changeLog = new ArrayList<>();
	}
	
	TransactionalFileSystemOnFile(final FileSystemOnFile another) {
		super(another);
		this.parent = another instanceof TransactionalFileSystemOnFile ? (TransactionalFileSystemOnFile)another : null;
		this.changeLog = null;
	}

	public Iterable<ChangeLogRecord> changeLog() {
		return getChangeLog();
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	@Override
	public FileSystemInterface newInstance(final URI resource) throws EnvironmentException {
		try{return new TransactionalFileSystemOnFile(resource);
		} catch (IOException e) {
			throw new EnvironmentException(e.getLocalizedMessage(),e);
		}
	}

	@Override
	public String getPath() throws IOException {
		String			path = super.getPath();
		
		for (int index = getChangeLog().size()-1; index >= 0; index--) {
			final ChangeLogRecord	item = getChangeLog().get(index);
			
			switch (item.operation) {
				case ATTRIBUTES : case WRITE : case REMOVE		: 
					break;
				case CREATE : case MKDIR : 
					if (path.equals(item.newValue)) {
						return path;
					}
					break;
				case RENAME		:
					if (path.equals(item.newValue)) {
						path = item.oldValue;
					}
					break;
				default :
					throw new UnsupportedOperationException("Change log operation ["+item.operation+"] is not supported yet");
			}
		}
		
		return path;
	}
	
	@Override
	public boolean exists() throws IOException {
		String			path = super.getPath();
		
		for (int index = getChangeLog().size()-1; index >= 0; index--) {
			final ChangeLogRecord	item = getChangeLog().get(index);
			
			switch (item.operation) {
				case ATTRIBUTES : case WRITE :
					break;
				case CREATE : case MKDIR : case RENAME		:
					if (path.equals(item.newValue)) {
						return true;
					}
					break;
				case REMOVE		: 
					if (path.equals(item.oldValue)) {
						return false;
					}
					break;
				default :
					throw new UnsupportedOperationException("Change log operation ["+item.operation+"] is not supported yet");
			}
		}
		
		return super.exists();
	}

	@Override
	public FileSystemInterface mkDir() throws IOException {
		super.mkDir();
		if (exists() && isDirectory()) {
			storeChangeLog(new ChangeLogRecord(Operation.MKDIR, null, super.getPath()));
		}
		return this;
	}
	
	@Override
	public FileSystemInterface create() throws IOException {
		super.create();
		if (exists() && isFile()) {
			storeChangeLog(new ChangeLogRecord(Operation.CREATE, null, super.getPath()));
		}
		return this;
	}
	
	@Override
	public FileSystemInterface rename(final String name) throws IOException {
		final String	oldName = super.getPath();
		
		super.rename(name);
		final String	currentName = super.getPath();
		
		if (!currentName.equals(name)) {
			storeChangeLog(new ChangeLogRecord(Operation.RENAME, oldName, currentName));
		}
		return this;
	}

	@Override
	public FileSystemInterface delete() throws IOException {
		super.delete();
		if (!exists()) {
			storeChangeLog(new ChangeLogRecord(Operation.REMOVE, super.getPath(), null));
		}
		return this;
	}

	@Override
	public InputStream read(final Map<String, Object> attributes) throws IOException {
		if (exists()) {
			return super.read(attributes);
		}
		else {
			throw new FileNotFoundException(getPath());
		}
	}

	@Override
	public FileSystemInterface setAttributes(Map<String, Object> attributes) throws IOException {
		if (exists()) {
			super.setAttributes(attributes);
			storeChangeLog(new ChangeLogRecord(Operation.ATTRIBUTES, super.getPath(), null));
			return this;
		}
		else {
			throw new FileNotFoundException(getPath());
		}
	}

	@Override
	public OutputStream write(final Map<String, Object> attributes) throws IOException {
		if (exists()) {
			storeChangeLog(new ChangeLogRecord(Operation.WRITE, super.getPath(), null));
			return super.write(attributes);
		}
		else {
			throw new FileNotFoundException(getPath());
		}
	}

	@Override
	public FileSystemInterface clone() {
		return new TransactionalFileSystemOnFile(this);
	}

	protected List<ChangeLogRecord> getChangeLog() {
		if (changeLog == null) {
			if (parent != null) {
				return parent.getChangeLog();
			}
			else {
				throw new IllegalStateException();
			}
		}
		else {
			return changeLog;
		}
	}
	
	protected void storeChangeLog(final ChangeLogRecord record) {
		getChangeLog().add(record);
	}
	
	public static class ChangeLogRecord {
		public static enum Operation {
			MKDIR,
			CREATE,
			RENAME,
			REMOVE,
			WRITE,
			ATTRIBUTES
		}
		
		public final long		timestamp = System.currentTimeMillis();
		public final Operation	operation;
		public final String		oldValue;
		public final String		newValue;
		
		public ChangeLogRecord(final Operation operation, final String oldValue, final String newValue) {
			if (operation == null) {
				throw new NullPointerException("Operation can't be null");
			}
			else if ((oldValue == null || oldValue.isEmpty()) && (newValue == null || newValue.isEmpty())) {
				throw new IllegalArgumentException("Old and new values can't be both null or empty");
			}
			else {
				this.operation = operation;
				this.oldValue = oldValue;
				this.newValue = newValue;
			}
		}

		@Override
		public String toString() {
			return "ChangeLogRecord [timestamp=" + timestamp + ", operation=" + operation + ", oldValue=" + oldValue + ", newValue=" + newValue + "]";
		}
	}
}