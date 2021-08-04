package chav1961.bt.lightrepo;

import java.io.IOException;
import java.net.URI;

import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.fsys.FileSystemOnXMLReadOnly;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class LightRepoUtils {
	public static FileSystemInterface buildLightRepoFileSystem(final URI current, final URI deltas, final URI commits) throws NullPointerException, IllegalArgumentException, IOException {
		if (current == null) {
			throw new NullPointerException("Current file system can't be null");
		}
		else if (deltas == null) {
			throw new NullPointerException("Delta file system can't be null");
		}
		else if (commits == null) {
			throw new NullPointerException("Commits file system can't be null");
		}
		else {
			return buildLightRepoFileSystem(FileSystemFactory.createFileSystem(current), FileSystemFactory.createFileSystem(deltas), FileSystemFactory.createFileSystem(commits));
		}
	}
	
	public static FileSystemInterface buildLightRepoFileSystem(final FileSystemInterface current, final FileSystemInterface deltas, final FileSystemInterface commits) throws NullPointerException, IllegalArgumentException, IOException {
		if (current == null) {
			throw new NullPointerException("Current file system can't be null");
		}
		else if (deltas == null) {
			throw new NullPointerException("Delta file system can't be null");
		}
		else if (commits == null) {
			throw new NullPointerException("Commits file system can't be null");
		}
		else {
			final FileSystemInterface	result = new FileSystemOnXMLReadOnly(URI.create("xmlReadOnly:"+LightRepoUtils.class.getResource("InnerFileSystem.xml")));
			
			result.push(AbstractLightRepo.CURRENT_PATH).mount(current).pop();
			result.push(AbstractLightRepo.DELTAS_PATH).mount(deltas).pop();
			result.push(AbstractLightRepo.COMMITS_PATH).mount(commits).pop();
			return result;
		}
	}
}
