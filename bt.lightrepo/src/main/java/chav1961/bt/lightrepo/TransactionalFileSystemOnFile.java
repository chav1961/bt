package chav1961.bt.lightrepo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

class TransactionalFileSystemOnFile extends FileSystemOnFile {
	private final Set<String>			removed = new HashSet<>();
	private final Map<String, String>	renamed = new HashMap<>();

	TransactionalFileSystemOnFile() throws IOException {
	}
	
	TransactionalFileSystemOnFile(final URI rootPath) throws IOException {
		super(rootPath);
	}
	
	protected TransactionalFileSystemOnFile(final FileSystemOnFile another) {
		super(another);
	}
	
	public Iterable<String> removed() {
		return removed;
	}
	
	public Iterable<Map.Entry<String, String>> renamed() {
		return renamed.entrySet();
	}
	
	@Override
	public void close() throws IOException {
		super.close();
	}

	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		return false;
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
		return super.getPath();
	}
	
	@Override
	public boolean exists() throws IOException {
		if (removed.contains(getPath())) {
			return false;
		}
		else {
			return super.exists();
		}
	}

	@Override
	public FileSystemInterface mkDir() throws IOException {
		super.mkDir();
		removed.remove(getPath());
		return this;
	}
	
	@Override
	public FileSystemInterface create() throws IOException {
		super.create();
		removed.remove(getPath());
		return this;
	}
	
	@Override
	public FileSystemInterface rename(final String name) throws IOException {
		removed.add(getPath());
		renamed.put(name, getPath());
		
		return this;
	}

	@Override
	public FileSystemInterface delete() throws IOException {
		removed.add(getPath());
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
			return super.setAttributes(attributes);
		}
		else {
			throw new FileNotFoundException(getPath());
		}
	}

	@Override
	public OutputStream write(final Map<String, Object> attributes) throws IOException {
		if (exists()) {
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
}