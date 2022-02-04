package chav1961.bt.lucenewrapper;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Set;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;

public class LucenePostgreSQLWrapperSirectory extends Directory implements Closeable, SpiService<Directory> {
	public LucenePostgreSQLWrapperSirectory() {
		
	}
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Directory newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] listAll() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteFile(final String name) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long fileLength(final String name) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public IndexOutput createOutput(final String name, final IOContext context) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IndexOutput createTempOutput(final String prefix, final String suffix, final IOContext context) throws IOException {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public IndexInput openInput(final String name, final IOContext context) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lock obtainLock(final String name) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Set<String> getPendingDeletions() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
}
