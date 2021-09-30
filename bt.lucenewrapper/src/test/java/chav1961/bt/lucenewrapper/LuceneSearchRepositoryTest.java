package chav1961.bt.lucenewrapper;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.lucene.store.Directory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import chav1961.bt.lucenewrapper.interfaces.Document2Save;
import chav1961.bt.lucenewrapper.interfaces.DocumentState;
import chav1961.bt.lucenewrapper.interfaces.SearchRepository.SearchRepositoryTransaction;
import chav1961.bt.lucenewrapper.interfaces.SearchRepositoryException;
import chav1961.bt.lucenewrapper.interfaces.SearchableDocument;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class LuceneSearchRepositoryTest {
	private final File 	f = new File(new File(System.getProperty("java.io.tmpdir")),"inside");
	
	@Before
	public void prepare() {
		 f.mkdirs();
	}

	@Test
	public void basicTest() throws IOException, SearchRepositoryException, NullPointerException, SyntaxException {
		try(final FileSystemInterface		fsi = new FileSystemOnFile(f.toURI());
			final Directory 				dir = new LuceneFileSystemWrapperDirectory(fsi)) {
			
			LuceneSearchRepository.prepareDirectory(dir);

			try{LuceneSearchRepository.prepareDirectory(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}

			
			try(final LuceneSearchRepository	lsr = new LuceneSearchRepository(PureLibSettings.CURRENT_LOGGER, dir)) {
				int count;

				Assert.assertEquals(DocumentState.NEW, lsr.getDocumentState(LuceneSearchRepository.NULL_UUID));
				Assert.assertEquals(DocumentState.UNKNOWN, lsr.getDocumentState(UUID.randomUUID()));

				try{lsr.getDocumentState(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				
				final Document2Save				d2s = lsr.getDocument(LuceneSearchRepository.NULL_UUID);
				
				Assert.assertEquals("", d2s.getTitle());
				Assert.assertEquals("test", d2s.getText());
				
				try{lsr.getDocument(null);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (NullPointerException exc) {
				}
				try{lsr.getDocument(UUID.randomUUID());
					Assert.fail("Mandatory exception was not detected (document not found)");
				} catch (SearchRepositoryException exc) {
				}
				
				count = 0;
				for (SearchableDocument item : lsr.seekDocuments("t*", 1)) {
					count++;
				}
				Assert.assertEquals(1, count);

				count = 0;
				for (SearchableDocument item : lsr.seekDocuments("f*", 1)) {
					count++;
				}
				Assert.assertEquals(0, count);
				
				try{lsr.seekDocuments(null, 1);
					Assert.fail("Mandatory exception was not detected (null 1-st argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{lsr.seekDocuments("", 1);
					Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
				} catch (IllegalArgumentException exc) {
				}
				try{lsr.seekDocuments("*", 1);
					Assert.fail("Mandatory exception was not detected (illegal 1-st argument syntax)");
				} catch (SyntaxException exc) {
				}
				try{lsr.seekDocuments("f*", 0);
					Assert.fail("Mandatory exception was not detected (illegal 2-nd argument)");
				} catch (IllegalArgumentException exc) {
				}
			}
			
			try{new LuceneSearchRepository(null, dir);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{new LuceneSearchRepository(PureLibSettings.CURRENT_LOGGER, null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
		}		
	}

	@Test
	public void transactionTest() throws IOException, SearchRepositoryException, NullPointerException, SyntaxException {
		try(final FileSystemInterface		fsi = new FileSystemOnFile(f.toURI());
			final Directory 				dir = new LuceneFileSystemWrapperDirectory(fsi)) {
			int count;
			
			LuceneSearchRepository.prepareDirectory(dir);

			try(final LuceneSearchRepository	lsr = new LuceneSearchRepository(PureLibSettings.CURRENT_LOGGER, dir)) {
				final SimpleDocument2Save		doc1 = new SimpleDocument2Save(), doc2 = new SimpleDocument2Save(), doc3 = new SimpleDocument2Save(), doc4 = new SimpleDocument2Save();
				UUID							id1, id2, id3;
				
				doc1.setText("first document");
				doc2.setText("second document");
				doc3.setText("third document");
				doc4.setText("forth document");
				
				try(final SearchRepositoryTransaction	trans = lsr.startTransaction()) {
					
					id1 = trans.addDocument(doc1);
					id2 = trans.addDocument(doc2);
					id3 = trans.addDocument(doc3);
					
					try{trans.addDocument(null);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					trans.commit();
				}
				
				Assert.assertEquals(DocumentState.NEW, lsr.getDocumentState(id1));
				Assert.assertEquals("first document", lsr.getDocument(id1).getText());
				Assert.assertEquals(DocumentState.NEW, lsr.getDocumentState(id2));
				Assert.assertEquals("second document", lsr.getDocument(id2).getText());
				Assert.assertEquals(DocumentState.NEW, lsr.getDocumentState(id3));
				Assert.assertEquals("third document", lsr.getDocument(id3).getText());

				count = 0;
				for (SearchableDocument item : lsr.seekDocuments("fo*", 1)) {
					count++;
				}
				Assert.assertEquals(0, count);
				
				try(final SearchRepositoryTransaction	trans = lsr.startTransaction()) {
					
					trans.setDocumentState(id1, DocumentState.AVAILABLE);
					try{trans.setDocumentState(null, DocumentState.AVAILABLE);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{trans.setDocumentState(id1, null);
						Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
					} catch (NullPointerException exc) {
					}
					
					trans.removeDocument(id2);
					try{trans.removeDocument(null);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					
					trans.replaceDocument(id3, doc4);
					try{trans.replaceDocument(null, doc4);
						Assert.fail("Mandatory exception was not detected (null 1-st argument)");
					} catch (NullPointerException exc) {
					}
					try{trans.replaceDocument(id3, null);
						Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
					} catch (NullPointerException exc) {
					}
					
					trans.commit();
				}

				Assert.assertEquals(DocumentState.AVAILABLE, lsr.getDocumentState(id1));
				Assert.assertEquals(DocumentState.UNKNOWN, lsr.getDocumentState(id2));
				Assert.assertEquals("forth document", lsr.getDocument(id3).getText());
			
				count = 0;
				for (SearchableDocument item : lsr.seekDocuments("fo*", 1)) {
					count++;
				}
				Assert.assertEquals(1, count);
			}
		}		
	}
	
	@After
	public void unprepare() {
		 Utils.deleteDir(f);
	}
	
}
