package chav1961.bt.lightrepo.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

/**
 * <p>This interface describes light implementation of the code repository. It can be useful for hand-made repositories in your program.
 * The repository always has the only branch and must merge content correctly in it. Repository looks like a {@linkplain FileSystemInterface} instance and can be
 * accessed freely in the read-only mode my calling {@linkplain #getFileSystem()} method. When any changes are required, you must call 
 * {@linkplain #beginTransaction(String, String, String)} method in this interface. This method must returns an instance of {@linkplain TransactionDescriptor}
 * After you got it, use method {@linkplain TransactionDescriptor#getFileSystem()} method to get mutable {@linkplain FileSystemInterface} instance. 
 * Subsequent call this method with the same commit id must returns the same instance of the {@linkplain FileSystemInterface}. All changes 
 * you made by this instance will be fixed as a new commit in the repository. You must close this {@linkplain FileSystemInterface} instance after 
 * the end of changes. After closing this instance, you must call {@linkplain TransactionDescriptor#commit()} method to fix the commit in the repository.
 * If this method wasn't called, any changes in the repository must be cancelled.</p>
 * <p>Typical use of the interface is:</p>
 * <code>
 * LightRepoInterface lri = ...<br>
 * try (TransactionDescriptor td = lri.beginTransaction("...","...","...")) {<br>
 * 	try (FileSystemInterface fsi = td.getFileSystem()) {<br>
 * 		// Do something<br>
 * 	}<br>
 *  td.commit();<br>
 * }<br>
 * </code>
 * <p>You can use {@linkplain #queryForCommit(String)} method to query data from commit history and {@linkplain #queryForPath(String)} method to
 * query data from file history. In both cases, parameter of the method contains a string query. This string has the following syntax:</p>
 * <p>
 * </p>
 */
public interface LightRepoInterface {
	String COMMIT_ID_VAR = "commitId";
	String TIMESTAMP_VAR = "timestamp";
	String PATH_VAR = "path";
	String VERSION_VAR = "version";
	String AUTHOR_VAR = "author";
	String COMMENT_VAR = "comment";
	String CONTENT_VAR = "content";
	
	String APPEARS_FUNC = "appeared";
	String DISAPPEARS_FUNC = "disappeared";
	String CREATED_FUNC = "created";
	String CHANGED_FUNC = "changed";
	String RENAMED_FUNC = "renamed";
	String REMOVED_FUNC = "removed";

	/**
	 * <p>This interface describes repository item in the commit list</p>
	 */
	public interface RepoItemDescriptor {
		/**
		 * <p>Get commit Id</p>
		 * @return commit ID. Can't be null
		 */
		UUID getCommitId();
		
		/**
		 * <p>Get timestamp of the commit</p>
		 * @return timestamp of the commit. Can't be null
		 */
		Date getTimestamp();
		
		/**
		 * <p>Get commit item path in the repository file system.</p>
		 * @return commit path. Can't be null
		 */
		String getPath();
		
		/**
		 * <p>Get version of the committed content.</p>
		 * @return zero-based version of the committed content. The same first appearance of the content in the repository  has version 0.
		 */
		long getVersion();
		
		/**
		 * <p>Get author of the commit.</p>
		 * @return Author of the commit. Can't be neither null nor empty
		 */
		String getAuthor();
		
		/**
		 * <p>Get commit comment</p>
		 * @return commit comment. Can't be neither null nor empty
		 */
		String getComment();
		
		/**
		 * <p>Get content stream. Subsequent call of the method must return the same instance of the stream</p>
		 * @return content stream. Can't be null
		 * @throws IOException on any I/O errors.
		 */
		InputStream getContent() throws IOException;
	}

	/**
	 * <p>This interface describes commit item in the commit list</p> 
	 *
	 */
	public interface CommitDescriptor {
		/**
		 * <p>Get commit ID</p>
		 * @return commit id. Can't be null
		 */
		UUID getCommitId();
		
		/**
		 * <p>Get timestamp of the commit</p>
		 * @return timestamp of the commit. Can't be null
		 */
		Date getTimestamp();
		
		/**
		 * <p>Get author of the commit.</p>
		 * @return Author of the commit. Can't be neither null nor empty
		 */
		String getAuthor();
		
		/**
		 * <p>Get commit comment</p>
		 * @return commit comment. Can't be neither null nor empty
		 */
		String getComment();
		
		/**
		 * <p>Get commit content descriptors</p>
		 * @return commit content descriptors. Can't be neither null nor empty array</p>
		 */
		RepoItemDescriptor[] getCommitContent();
	}
	
	/**
	 * <p>This interface describes changes between two versions of the content</p>
	 */
	public interface ChangesDescriptor {
		/**
		 * <p>This enumeration describes type of change in the content</p> 
		 */
		public enum ChangeType {
			INSERTED,
			CHANGED,
			REMOVED
		}
		
		/**
		 * <p>Get source line number in the first content</p> 
		 * @return zero-based first line number
		 */
		long getFirstLine();
		
		/**
		 * <p>Get source line in the first content</p>
		 * @return source line in in the first content. Can't be null
		 */
		String getFirstLineContent();
		
		/**
		 * <p>Get source line number in the second content</p> 
		 * @return zero-based second line number
		 */
		long getSecondLine();
		
		/**
		 * <p>Get source line in the first content</p>
		 * @return source line in in the second content. Can't be null
		 */
		String getSecondLineContent();
		
		/**
		 * <p>Get change type for this record</p>
		 * @return change type. Can't be null
		 */
		ChangeType getChangeType();
	}
	
	/**
	 * <p>This interface describes potential commit in the repository.</p> 
	 */
	public interface TransactionDescriptor extends Closeable {
		/**
		 * <p>Get commit Id</p>
		 * @return commit Id. Can't be null
		 */
		UUID getCommitId();
		
		/**
		 * <p>Get mutable file system to store commit changes</p>
		 * @return mutable file system to store commit changes. Can't be null
		 * @throws IOException on any I/O errors
		 */
		FileSystemInterface getFileSystem() throws IOException;
		
		/**
		 * <p>Make commit. Must be last call before calling {@linkplain #close()} method</p>
		 * @throws IOException on any I/O errors
		 */
		void commit() throws IOException;
	}
	
	/**
	 * <p>Get descriptors for paths</p>
	 * @param query query. Can't be null or empty. See syntax in the {@linkplain LightRepoInterface} description</p>
	 * @return list of content found. Can't be null
	 * @throws SyntaxException on syntax errors in the query
	 * @throws IOException on any I/O errors
	 */
	Iterable<RepoItemDescriptor> queryForPath(String query) throws SyntaxException, IOException;
	
	/**
	 * <p>Get descriptors for commits</p>
	 * @param query query. Can't be null or empty. See syntax in the {@linkplain LightRepoInterface} description</p>
	 * @return list of content found. Can't be null
	 * @throws SyntaxException on syntax errors in the query
	 * @throws IOException on any I/O errors
	 */
	Iterable<CommitDescriptor> queryForCommit(String query) throws SyntaxException, IOException;
	
	/**
	 * <p>Calclulate changes between the given versions of the content </p> 
	 * @param path path to calculate changes for
	 * @param firstVersion first version of the path
	 * @param secondVersion second version of the path
	 * @return list of changes found. Can't be null
	 * @throws IOException on any I/O errors
	 */
	Iterable<ChangesDescriptor> calculateChanges(String path, long firstVersion, long secondVersion) throws IOException;
	
	/**
	 * <p>Get read-only file system to get access to the repository content</p>
	 * @return file system to get access to
	 * @throws IOException on any I/O errors
	 */
	FileSystemInterface getFileSystem() throws IOException;
	
	/**
	 * <p>Prepare transaction to make commit into the repository</p>
	 * @param author commit author. Can't be null or empty
	 * @param comment commit comment. Can't be null or empty
	 * @return transaction descriptor. Can't be null
	 * @throws IOException on any I/O errors
	 */
	TransactionDescriptor beginTransaction(String author, String comment) throws IOException;
}
