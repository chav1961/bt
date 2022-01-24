package chav1961.bt.lightrepo;

import chav1961.bt.lightrepo.interfaces.LightRepoInterface;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.TransactionDescriptor;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.Writer;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ScenarioTest {

	@Test
	@Ignore
	public void basicScenarioTest() throws IOException {
		try(final FileSystemInterface		current = new FileSystemOnFile(new File("./src/test/resources/chav1961/bt/lightrepo/fs1/").toURI());
				final FileSystemInterface	deltas = new FileSystemOnFile(new File("./src/test/resources/chav1961/bt/lightrepo/fs2/").toURI());
				final FileSystemInterface	commits = new FileSystemOnFile(new File("./src/test/resources/chav1961/bt/lightrepo/fs3/").toURI());
				final FileSystemInterface	fsi = LightRepoUtils.buildLightRepoFileSystem(current, deltas, commits)) {
				final LightRepoInterface	lri = new AbstractLightRepo(fsi, ()->null, ()->null);
			
				try(final TransactionDescriptor 	td = lri.beginTransaction("author", "comment");
					final FileSystemInterface		tfs = td.getFileSystem().clone().open("/current/test.txt").create()) {
					
					try(final Writer	wr = tfs.charWrite(PureLibSettings.DEFAULT_CONTENT_ENCODING))  {
						Utils.copyStream(new StringReader("test"), wr);
					}
					td.commit();
				}
		}
	}
}
