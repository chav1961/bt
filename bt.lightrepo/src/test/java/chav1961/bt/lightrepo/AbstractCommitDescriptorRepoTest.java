package chav1961.bt.lightrepo;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.lightrepo.interfaces.LightRepoInterface.RepoItemDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.ChangesDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.CommitDescriptor;
import chav1961.purelib.fsys.FileSystemInMemory;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class AbstractCommitDescriptorRepoTest {
	@Test
	public void basicTest() throws IOException {
		try(final FileSystemInterface			fsi = new FileSystemInMemory(URI.create("memory:/")).open("/commits").mkDir()) {
			final AbstractCommitDescriptorRepo	acdr = new AbstractCommitDescriptorRepo(fsi);
			
			final UUID		commitId = UUID.randomUUID(), scratchCommitId = UUID.randomUUID(); 
			final Date		date = new Date(0);
			final String	author = "author 1"; 
			final String	comment = "comment 1";
			final String	path = "/test.txt";
			final long		version = 1;
			final String	content = "content 1";
			
			Assert.assertFalse(acdr.exists(commitId));
			
			try{acdr.exists(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			
			final RepoItemDescriptor	rid = new RepoItemDescriptor() {
											@Override public UUID getCommitId() {return commitId;}
											@Override public Date getTimestamp() {return date;}
											@Override public String getPath() {return path;}
											@Override public long getVersion() {return version;}
											@Override public String getAuthor() {return author;}
											@Override public String getComment() {return comment;}
											@Override public InputStream getContent() throws IOException {return new ByteArrayInputStream(content.getBytes());}
											@Override public ChangesDescriptor[] getChanges() throws IOException {return new ChangesDescriptor[0];}
											@Override public CommitDescriptor getCommit() {return null;}	// sad...
											@Override public RepoItemDescriptor getPrevious() throws IOException {return null;}
											@Override public RepoItemDescriptor getNext() throws IOException {return null;}
										};  
			final CommitDescriptor		desc = new CommitDescriptor() {
											@Override public Date getTimestamp() {return date;}
											@Override public UUID getCommitId() {return commitId;}
											@Override public RepoItemDescriptor[] getCommitContent() {return new RepoItemDescriptor[] {rid};}
											@Override public String getComment() {return comment;}
											@Override public String getAuthor() {return author;}
											@Override public CommitDescriptor getPrevious() throws IOException {return null;}
											@Override public CommitDescriptor getNext() throws IOException {return null;}
										};
			
			acdr.storeCommitDescriptor(commitId, desc);
			Assert.assertTrue(acdr.exists(commitId));
			Assert.assertFalse(acdr.exists(scratchCommitId));
			
			try{acdr.storeCommitDescriptor((UUID)null, desc);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{acdr.storeCommitDescriptor(commitId, desc);
				Assert.fail("Mandatory exception was not detected (duplicate 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{acdr.storeCommitDescriptor(commitId, null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			final CommitDescriptor		loaded = acdr.getCommitDescriptor(commitId);
			
			Assert.assertEquals(commitId, loaded.getCommitId());
			
			try{acdr.getCommitDescriptor(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{acdr.getCommitDescriptor(scratchCommitId);
				Assert.fail("Mandatory exception was not detected (1-st argument is not exists)");
			} catch (IOException exc) {
			}
		}
	}
}
