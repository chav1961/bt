package chav1961.bt.lightrepo;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class LightRepoUtilsTest {
	@Test
	@Ignore
	public void buildLightRepoFileSystemByFSTest() throws IOException {
		try(final FileSystemInterface	current = new FileSystemOnFile(new File("./src/test/resources/chav1961/bt/lightrepo/fs1/").toURI());
			final FileSystemInterface	deltas = new FileSystemOnFile(new File("./src/test/resources/chav1961/bt/lightrepo/fs2/").toURI());
			final FileSystemInterface	commits = new FileSystemOnFile(new File("./src/test/resources/chav1961/bt/lightrepo/fs3/").toURI());
			final FileSystemInterface	fsi = LightRepoUtils.buildLightRepoFileSystem(current, deltas, commits)) {
			
			try(final FileSystemInterface	test = fsi.clone().open("/current/test.txt")) {
				Assert.assertTrue(test.exists());
				Assert.assertTrue(test.isFile());
				Assert.assertEquals("fs1",loadContent(test));
			}

			try(final FileSystemInterface	test = fsi.clone().open("/deltas/test.txt")) {
				Assert.assertTrue(test.exists());
				Assert.assertTrue(test.isFile());
				Assert.assertEquals("fs2",loadContent(test));
			}

			try(final FileSystemInterface	test = fsi.clone().open("/commits/test.txt")) {
				Assert.assertTrue(test.exists());
				Assert.assertTrue(test.isFile());
				Assert.assertEquals("fs3",loadContent(test));
			}
			
			try{LightRepoUtils.buildLightRepoFileSystem(null, deltas, commits);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{LightRepoUtils.buildLightRepoFileSystem(current, null, commits);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{LightRepoUtils.buildLightRepoFileSystem(current, deltas, null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}

	@Test
	@Ignore
	public void buildLightRepoFileSystemByURITest() throws IOException {
		final URI	current = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":"+new File("./src/test/resources/chav1961/bt/lightrepo/fs1/").toURI().toString());
		final URI	deltas = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":"+new File("./src/test/resources/chav1961/bt/lightrepo/fs2/").toURI().toString());
		final URI	commits = URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":"+new File("./src/test/resources/chav1961/bt/lightrepo/fs3/").toURI().toString());
		
		try(final FileSystemInterface	fsi = LightRepoUtils.buildLightRepoFileSystem(current, deltas, commits)) {
			
			try(final FileSystemInterface	test = fsi.clone().open("/current/test.txt")) {
				Assert.assertTrue(test.exists());
				Assert.assertTrue(test.isFile());
				Assert.assertEquals("fs1",loadContent(test));
			}

			try(final FileSystemInterface	test = fsi.clone().open("/deltas/test.txt")) {
				Assert.assertTrue(test.exists());
				Assert.assertTrue(test.isFile());
				Assert.assertEquals("fs2",loadContent(test));
			}

			try(final FileSystemInterface	test = fsi.clone().open("/commits/test.txt")) {
				Assert.assertTrue(test.exists());
				Assert.assertTrue(test.isFile());
				Assert.assertEquals("fs3",loadContent(test));
			}
			
			try{LightRepoUtils.buildLightRepoFileSystem(null, deltas, commits);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{LightRepoUtils.buildLightRepoFileSystem(current, null, commits);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{LightRepoUtils.buildLightRepoFileSystem(current, deltas, null);
				Assert.fail("Mandatory exception was not detected (null 3-rd argument)");
			} catch (NullPointerException exc) {
			}
		}
	}
	
	private static String loadContent(final FileSystemInterface fsi) throws IOException {
		try(final Reader	rdr = fsi.charRead()) {
			return Utils.fromResource(rdr);
		}
	}
}
