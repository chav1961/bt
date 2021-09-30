package chav1961.bt.lightrepo;


import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.lightrepo.TransactionalFileSystemOnFile.ChangeLogRecord;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class TransactionFSTest {
	@Test
	public void basicTest() {
		try(final TransactionalFileSystemOnFile		tfof = new TransactionalFileSystemOnFile()) {
			Assert.assertTrue(tfof.canServe(URI.create(FileSystemInterface.FILESYSTEM_URI_SCHEME+":file:./")));

			final URI	dirUri = new File(System.getProperty("java.io.tmpdir")).toURI();
			
			try(final TransactionalFileSystemOnFile	inner = (TransactionalFileSystemOnFile) tfof.newInstance(URI.create(dirUri.toString()))) {
				Assert.assertEquals(dirUri, inner.getAbsoluteURI());
				
				try(final Writer	wr = inner.clone().open("/test.txt").create().charWrite(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
					Utils.copyStream(new StringReader("test"), wr);
				}
				for (ChangeLogRecord item : inner.changeLog()) {
					System.err.println(item);
				}
			}
			Assert.assertTrue(new File(dirUri.getSchemeSpecificPart(),"test.txt").exists());
		} catch (IOException | EnvironmentException e) {
			e.printStackTrace();
			Assert.fail("Unwaited error while testing");
		}
	}
}
