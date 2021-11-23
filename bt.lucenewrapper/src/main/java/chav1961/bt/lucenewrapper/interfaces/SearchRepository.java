package chav1961.bt.lucenewrapper.interfaces;

import java.util.UUID;

import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.exceptions.SyntaxException;

public interface SearchRepository extends AutoCloseable {
	static SubstitutableProperties	EMPTY_PROPS = new SubstitutableProperties();
	
	public interface SearchRepositoryTransaction extends AutoCloseable {
		UUID addDocument(Document2Save doc) throws SyntaxException, SearchRepositoryException;
		UUID replaceDocument(UUID id, Document2Save doc) throws SyntaxException, SearchRepositoryException;
		void removeDocument(UUID id) throws SearchRepositoryException;
		void setDocumentState(UUID id, DocumentState state) throws SearchRepositoryException;
		void commit() throws SearchRepositoryException;
		void close() throws SearchRepositoryException;
	}
	
	Document2Save getDocument(UUID id) throws SearchRepositoryException;
	DocumentState getDocumentState(UUID id) throws SearchRepositoryException;
	
	SearchRepositoryTransaction startTransaction() throws SearchRepositoryException;
	
	Iterable<SearchableDocument> seekDocuments(final String query, final int amount, final SubstitutableProperties options) throws SyntaxException, SearchRepositoryException;
	default Iterable<SearchableDocument> seekDocuments(final String query, final int amount) throws SyntaxException, SearchRepositoryException {
		return seekDocuments(query, amount, EMPTY_PROPS);
	}
	
	void close() throws SearchRepositoryException;
}
