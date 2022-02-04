package chav1961.bt.lucenewrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.SortedSetDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.util.BytesRef;

import chav1961.bt.lucenewrapper.interfaces.Document2Save;
import chav1961.bt.lucenewrapper.interfaces.DocumentState;
import chav1961.bt.lucenewrapper.interfaces.SearchRepository;
import chav1961.bt.lucenewrapper.interfaces.SearchRepositoryException;
import chav1961.bt.lucenewrapper.interfaces.SearchableDocument;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.fsys.FileSystemFactory;

public class LuceneSearchRepository implements SearchRepository {
	public static final String		LUCENE_DIR_SCHEMA = "lucenedir"; 
	static final UUID				NULL_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
	
	private static final String		F_ID = "id";
	private static final String		F_STATE= "state";
	private static final String		F_TITLE = "title";
	private static final String		F_AUTHOR = "author";
	private static final String		F_ANNOTATION = "annotation";
	private static final String		F_CONTENT = "content";
	private static final String		F_TAGS = "tags";
	private static final String		F_KEYWORDS = "keywords";
	
	private final LoggerFacade		logger;
	private final Directory			directory;
	private final ReentrantReadWriteLock	rwLock = new ReentrantReadWriteLock();
	private final StandardAnalyzer 	standardAnalyzer = new StandardAnalyzer();
	private IndexReader				reader;
	private IndexSearcher			searcher;

	public LuceneSearchRepository(final LoggerFacade logger, final URI directoryType) throws NullPointerException, SearchRepositoryException {
		this(logger, createLuceneDirectory(directoryType));
	}
	
	public LuceneSearchRepository(final LoggerFacade logger, final Directory directory) throws NullPointerException, SearchRepositoryException {
		if (logger == null) {
			throw new NullPointerException("Logger can't be null"); 
		}
		else if (directory == null) {
			throw new NullPointerException("Directory can't be null"); 
		}
		else {
			try(final LoggerFacade	trans = logger.transaction(this.getClass().getSimpleName())){
				this.logger = logger;
				this.directory = directory;
				trans.message(Severity.debug, "Before opening directory...");
				this.reader = DirectoryReader.open(directory);
				trans.message(Severity.debug, "Before opening searcher...");
				this.searcher = new IndexSearcher (reader);
				trans.rollback();
			} catch (IOException e) {
				throw new SearchRepositoryException(e.getLocalizedMessage(), e);
			}
		}
	}

	@Override
	public Document2Save getDocument(final UUID id) throws SearchRepositoryException {
		if (id == null) {
			throw new NullPointerException("Document id can't be null");
		}
		else {
			final ReadLock	lock = rwLock.readLock();
			
			lock.lock();
			try{final TopDocs 	docs = searcher.search(new TermQuery(new Term(F_ID, id.toString())), 1);
			     
				if (docs.totalHits.value > 0) {
					return new Document2SaveImpl(searcher.doc(docs.scoreDocs[0].doc));
				}
				else {
					throw new SearchRepositoryException("Document ["+id+"] not found in the repository");
				}
			} catch (IOException e) {
				throw new SearchRepositoryException(e.getLocalizedMessage(),e);
			} finally {
				lock.unlock();
			}
		}
	}

	@Override
	public DocumentState getDocumentState(final UUID id) throws SearchRepositoryException {
		if (id == null) {
			throw new NullPointerException("Document id can't be null");
		}
		else {
			final ReadLock	lock = rwLock.readLock();
			
			lock.lock();
			try{final TopDocs 	docs = searcher.search(new TermQuery(new Term(F_ID, id.toString())), 1);
			     
				if (docs.totalHits.value > 0) {
					Document 	doc = searcher.doc(docs.scoreDocs[0].doc);
					
					return DocumentState.valueOf(doc.getValues(F_STATE)[0]);
				}
				else {
					return DocumentState.UNKNOWN;
				}
			} catch (IOException e) {
				throw new SearchRepositoryException(e.getLocalizedMessage(),e);
			} finally {
				lock.unlock();
			}
		}
	}

	@Override
	public SearchRepositoryTransaction startTransaction() throws SearchRepositoryException {
		return new SearchRepositoryTransactionImpl();
	}

	@Override
	public Iterable<SearchableDocument> seekDocuments(final String query, final int amount, final SubstitutableProperties options) throws SyntaxException, SearchRepositoryException {
		if (query == null || query.isEmpty()) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else if (amount < 0) {
			throw new IllegalArgumentException("Amount value ["+amount+"] can'tbe negative");
		}
		else if (options == null) {
			throw new NullPointerException("Options can't be null");
		}
		else {
			final QueryParser	parser = new QueryParser(F_CONTENT, standardAnalyzer);
			
			try{final Query 	queryParsed = parser.parse(query);
				final ReadLock	lock = rwLock.readLock();
				
				lock.lock();
				try{final TopDocs 	docs = searcher.search(queryParsed, amount);
				     
				 	if (docs.totalHits.value > 0) {
				 		final List<SearchableDocument>	result = new ArrayList<>();
				 		
				 		for (ScoreDoc item : docs.scoreDocs) {
				 			result.add(new SearchableDocumentImpl(searcher.doc(item.doc), item.score));
				 		}
						return result;
					}
					else {
						return Arrays.asList();
					}
				} catch (IOException e) {
					 throw new SearchRepositoryException(e.getLocalizedMessage(),e);
				} finally {
					 lock.unlock();
				}
			} catch (ParseException exc) {
				if (exc.currentToken != null) {
					throw new SyntaxException(exc.currentToken.beginLine, exc.currentToken.beginColumn, exc.getLocalizedMessage(), exc);
				}
				else {
					throw new SyntaxException(0, 0, exc.getLocalizedMessage(), exc);
				}
			}
		}
	}

	@Override
	public void close() throws SearchRepositoryException {
		final StringBuilder	sb = new StringBuilder();
		
		if (reader != null) {
			try{reader.close();
			} catch (IOException e) {
				sb.append("IndexReader: ").append(e.getLocalizedMessage()).append('\n');
			}
		}
		if (!sb.isEmpty()) {
			throw new SearchRepositoryException(sb.toString());
		}
	}

	public static Directory createLuceneDirectory(final URI directoryType) throws SearchRepositoryException, NullPointerException, IllegalArgumentException {
		if (directoryType == null) {
			throw new NullPointerException("Directory typeURI can't be null"); 
		}
		else {
			return null;
		}
	}
	
	public static void prepareDirectory(final Directory directory) throws IOException {
		if (directory == null) {
			throw new NullPointerException("Directory to prepare can't be null");
		}
		else {
			 final StandardAnalyzer		standardAnalyzer = new StandardAnalyzer();
			 final IndexWriterConfig	config = new IndexWriterConfig(standardAnalyzer);
			 
			 try(IndexWriter writer = new IndexWriter(directory, config)) {
				 Document document = new Document ();
				 document.add(new StringField(F_ID, NULL_UUID.toString(), Field.Store.YES));
				 document.add(new StringField(F_STATE, DocumentState.NEW.name(), Field.Store.YES));
				 document.add(new TextField(F_CONTENT, "test", Field.Store.YES));
				 writer.addDocument(document);
			 }
		}
	}
	
	private class SearchRepositoryTransactionImpl implements SearchRepositoryTransaction {
		private final List<Term>			toRemove = new ArrayList<>();
		private final List<Document>		toAdd = new ArrayList<>();
		private final List<TermAndState>	toChange = new ArrayList<>();

		@Override
		public UUID addDocument(final Document2Save doc) throws SyntaxException, SearchRepositoryException {
			if (doc == null) {
				throw new NullPointerException("Document to add can't be null"); 
			}
			else {
				final UUID			id = UUID.randomUUID();
				final Document		document = createDocument(id, doc);

				toAdd.add(document);
				return id;
			}
		}

		@Override
		public UUID replaceDocument(final UUID id, final Document2Save doc) throws SyntaxException, SearchRepositoryException {
			if (id == null) {
				throw new NullPointerException("Document id can't be null");
			}
			else if (doc == null) {
				throw new NullPointerException("Document to replace can't be null"); 
			}
			else {
				toRemove.add(new Term(F_ID,id.toString()));
				toAdd.add(createDocument(id, doc));
				return id;
			}
		}

		@Override
		public void removeDocument(final UUID id) throws SearchRepositoryException {
			if (id == null) {
				throw new NullPointerException("Document id can't be null");
			}
			else {
				toRemove.add(new Term(F_ID,id.toString()));
			}
		}

		@Override
		public void setDocumentState(final UUID id, final DocumentState state) throws SearchRepositoryException {
			if (id == null) {
				throw new NullPointerException("Document id can't be null");
			}
			else if (state == null) {
				throw new NullPointerException("Document state can't be null");
			}
			else {
				toChange.add(new TermAndState(id, state, getDocument(id)));
			}
		}

		@Override
		public void commit() throws SearchRepositoryException {
			final WriteLock			lock = rwLock.writeLock();
			final IndexWriterConfig	config = new IndexWriterConfig(standardAnalyzer); 
			
			lock.lock();
			try{reader.close();
			
				try(IndexWriter writer = new IndexWriter(directory, config)) {
					writer.deleteDocuments(toRemove.toArray(new Term[toRemove.size()]));
					writer.addDocuments(toAdd);
					for (TermAndState item : toChange) {
						final Document	doc = createDocument(item.id, item.doc); 
						
						doc.removeField(F_STATE);
						doc.add(new StringField(F_STATE, item.state.name(), Field.Store.YES));
						writer.deleteDocuments(new Term(F_ID, item.id.toString()));
						writer.addDocument(doc);
					}
					writer.commit();
				}
			} catch (IOException | SyntaxException e) {
				throw new SearchRepositoryException(e.getLocalizedMessage());			
			} finally {
				try{reader = DirectoryReader.open(directory);
				} catch (IOException e) {
					throw new SearchRepositoryException(e.getLocalizedMessage());			
				}
				searcher = new IndexSearcher(reader);
				lock.unlock();
			}
		}

		@Override
		public void close() throws SearchRepositoryException {
			toRemove.clear();
			toAdd.clear();
		}
		
		private Document createDocument(final UUID id, final Document2Save doc) throws SyntaxException, SearchRepositoryException {
			final StringBuilder	sb = new StringBuilder();
			final Document 		document = new Document ();

			document.add(new StringField(F_ID, id.toString(), Field.Store.YES));
			document.add(new StringField(F_STATE, DocumentState.NEW.name(), Field.Store.YES));
			document.add(new TextField(F_TITLE, doc.getTitle(), Field.Store.YES));
			document.add(new TextField(F_AUTHOR, doc.getAuthor(), Field.Store.YES));
			document.add(new TextField(F_ANNOTATION, doc.getAnnotation(), Field.Store.YES));
			document.add(new TextField(F_CONTENT, doc.getText(), Field.Store.YES));
			
			sb.setLength(0);
			for (String item : doc.getTags()) {
				document.add(new SortedSetDocValuesField(F_TAGS, new BytesRef(item)));
				sb.append('#').append(item).append(' ');
			}
			document.add(new StoredField(F_TAGS, sb.toString()));

			sb.setLength(0);
			for (Map.Entry<String,String> item : doc.getKeywords().entrySet()) {
				document.add(new SortedSetDocValuesField(F_KEYWORDS, new BytesRef(item.getKey())));
				document.add(new SortedSetDocValuesField(F_KEYWORDS, new BytesRef(item.getValue())));
				sb.append(item.getKey()).append('=').append(item.getValue()).append(';');
			}
			document.add(new StoredField(F_KEYWORDS, sb.toString()));
			
			return document;
		}
	}
	
	private static class TermAndState {
		private final UUID			id;
		private final DocumentState	state;
		private final Document2Save	doc;
		
		TermAndState(UUID id, DocumentState state, Document2Save doc) {
			this.id = id;
			this.state = state;
			this.doc = doc;
		}

		@Override
		public String toString() {
			return "TermAndState [id=" + id + ", state=" + state + "]";
		}
	}
	
	private static class Document2SaveImpl implements Document2Save {
		private final String				title;
		private final String				author;
		private final String				annotation;
		private final String				content;
		private final Set<String>			tags = new HashSet<>();
		private final Map<String,String>	keywords = new HashMap<>();
		
		Document2SaveImpl(final Document document) {
			this.title = extractValue(document, F_TITLE);
			this.author = extractValue(document, F_AUTHOR);
			this.annotation = extractValue(document, F_ANNOTATION);
			this.content = extractValue(document, F_CONTENT);
			for (String item : extractValue(document, F_TAGS).split("#")) {
				final String	val = item.trim();
				
				if (!val.isEmpty()) {
					tags.add(val);
				}
			}
			for (String item : extractValue(document, F_KEYWORDS).split(";")) {
				final String	val = item.trim();
				
				if (!val.isEmpty() && val.contains("=")) {
					final String[] parts = val.split("=");
					
					keywords.put(parts[0], parts[1]);
				}
			}
		}
		
		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getAuthor() {
			return author;
		}

		@Override
		public String getAnnotation() {
			return annotation;
		}

		@Override
		public String getText() {
			return content;
		}

		@Override
		public InputStream getContent() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Date created() {
			return new Date(System.currentTimeMillis());
		}

		@Override
		public Set<String> getTags() {
			return tags;
		}

		@Override
		public Map<String, String> getKeywords() {
			return keywords;
		}
		
		static String extractValue(final Document doc,final String fieldName) {
			final String[] 	vals = doc.getValues(fieldName);
			
			return vals == null || vals.length == 0 ? "" : vals[0];
		}
	}

	private static class SearchableDocumentImpl extends Document2SaveImpl implements SearchableDocument {
		private final UUID			id;
		private final DocumentState	state;
		private final double		score;
		
		SearchableDocumentImpl(final Document document, final double score) {
			super(document);
			final String	id = extractValue(document, F_ID), state = extractValue(document, F_STATE); 
			
			this.id = id.isEmpty() ? NULL_UUID : UUID.fromString(id);
			this.state = state.isEmpty() ? DocumentState.UNKNOWN : DocumentState.valueOf(state);
			this.score = score;
		}

		@Override
		public UUID getId() {
			return id;
		}

		@Override
		public DocumentState getDocumentState() {
			return state;
		}

		@Override
		public Date lastModified() {
			return created();
		}

		@Override
		public Iterable<Highlights> highlights() {
			// TODO Auto-generated method stub
			return Arrays.asList();
		}

		@Override
		public double getScore() {
			return score;
		}
	}
}
