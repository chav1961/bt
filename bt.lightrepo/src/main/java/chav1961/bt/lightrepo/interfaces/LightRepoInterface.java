package chav1961.bt.lightrepo.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
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
 * <query>::= <orQuery> ['or' <orQuery> ...]
<orQuery>::= <andQuery> ['and' <andQuery> ...]
<andQuery>::=['not']<comparison>
<comparison>::={<expression> {'~'|'='|'<>'|'>'|'>='|'<'|'<='|} <expression> | <expression> 'in' '['<range>[,...]']' | <booleanFunc>}
<range>::=<expression>['..'<expression>]
<expression>::=<term> [{'+' | '-'} <term> ...]
<term>::={<constant>|<nonBooleanFunc>|<variable>|'('<query>')'}
<booleanFunc>::={'exists''('<query>')' | 'contains''('<variable>','<expression>')' | 'appears''('<variable>','<expression>')'  | 'disappears''('<variable>','<expression>')'}
<nonBooleanFunc>::='now'['('')']
<constant>::={<number>|'"'<string>'"'|'''<string>'''}
<variable>::={<commit>|<file>}
<commit>::='commit'[<commitSuffix>]
<file>::='file'[<fileSuffix>]
<commitSuffix>::= { '.'{'author'[<caseSuffix>]|'description'[<caseSuffix>]|'timestamp'<prevNext>|'id'|'content'<fileSuffix> | <prevNextCommit>}]
<fileSuffix>::= [<relative>]{ '.'{'path'[<caseSuffix>]|'change'|'timestamp'|'version'|'content'[<caseSuffix>]|'parseable' | <prevNextFile>}]
<relative>::='('<expression>')'
<prevNextCommit>::={'prev'['('<number>')']|'next'['('<number>')']}[<commitSuffix>]
<prevNextFile>::={'prev'['('<number>')']|'next'['('<number>')']}[<fileSuffix>]
<number>::={'0'|'1'|'2'|'3'|'4'|'5'|'6'|'7'|'8'|'9'|}[<number>]
<string>::={<char>|<escape>}[<string>]
 * </p>
 * <p>You can also make preliminary translation of the query string and store the translation into the repository by calling {@linkplain #translateQuery(String, Hashtable)}
 * method. This method returns translated query ID. You can use this id to call {@linkplain #queryForPath(UUID)} and {@linkplain #queryForCommit(UUID)}} methods. To 
 * manipulate with translated queries use the {@linkplain #getQueriesTranslated()} method.</p> 
 */
public interface LightRepoInterface {
	/**
	 * <p>Predefined variable means any file.</p> 
	 */
	String FILE_VAR = "file";
	
	/**
	 * <p>Predefined variable means anym commit</p>
	 */
	String COMMIT_VAR = "commit";
	
	
	/**
	 * <p>Predefined field means commit id</p>
	 */
	String COMMIT_ID_F = "id";
	
	/**
	 * <p>Predefined variable means commit or file time stamp</p>
	 */
	String TIMESTAMP_F = "timestamp";
	
	/**
	 * <p>Predefine variable means file path</p>
	 */
	String PATH_F = "path";
	
	/**
	 * <p>Predefined variable means file version</p>
	 */
	String VERSION_F = "version";
	
	/**
	 * <p>Predefined variable means commit author</p>
	 */
	String AUTHOR_F = "author";
	
	/**
	 * <p>Predefined variable means commit comment</p>
	 */
	String COMMENT_F = "comment";
	
	/**
	 * <p>Predefined variable means commit or file content</p>
	 */
	String CONTENT_F = "content";
	
	/**
	 * <p>Predefined variable means file changes with another version</p>
	 */
	String CHANGE_F = "change";
	
	/**
	 * <p>Predefined variable means this file is parseable for changes</p>
	 */
	String PARSEABLE_F = "parseable";
	
	
	/**
	 * <p>Predefined function checks existentcy of file or commit</p>
	 */
	String EXISTS_FUNC = "exists";
	
	/**
	 * <p>Predefined function checks appearance of file or it's content between versions</p>
	 */
	String APPEARS_FUNC = "appears";
	
	/**
	 * <p>Predefined function checks disappearance of file or it's content between versions</p>
	 */
	String DISAPPEARS_FUNC = "disappears";
	
	/**
	 * <p>Predefined function checks file was created in the commit</p>
	 */
	String CREATED_FUNC = "created";
	
	/**
	 * <p>Predefined function checks file was changed in the commit</p>
	 */
	String CHANGED_FUNC = "changed";
	
	/**
	 * <p>Predefined function checks file was renamed in the commit</p>
	 */
	String RENAMED_FUNC = "renamed";
	
	/**
	 * <p>Predefined function checks file was removed in the commit</p>
	 */
	String REMOVED_FUNC = "removed";
	
	/**
	 * <p>Predefined function checks regular expression inside content</p>
	 */
	String CONTAINS_FUNC = "contains";
	
	/**
	 * <p>Predefined function returns current timestamp</p>
	 */
	String NOW_FUNC = "now";

	
	/**
	 * <p>Query modifier means previous file version or commit</p>
	 */
	String PREV_MOD = "prev";
	
	/**
	 * <p>Query modifier means next file version or commit</p>
	 */
	String NEXT_MOD = "next";
	
	/**
	 * <p>Query modifier converts content to upper case</p>
	 */
	String UPPERCASE_MOD = "uppercase";
	
	/**
	 * <p>Query modifier converts content to lower case</p>
	 */
	String LOWERCASE_MOD = "lowercase";
	
	
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
		 * <p>Get commit  descriptor.</p>
		 * @return commit descriptor. Can't be null
		 */
		CommitDescriptor getCommit();
		
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
		
		/**
		 * <p>Get changes in the content.</p>
		 * @return changes of the content. Can't be null or zero-legth
		 * @throws IOException on any I/O errors.
		 */
		ChangesDescriptor[] getChanges() throws IOException;
		
		/**
		 * <p>Get previous descriptor for the given item</p>
		 * @return previous description or null, if previous is missing 
		 * @throws IOException on any I/O errors.
		 */
		RepoItemDescriptor getPrevious() throws IOException;

		/**
		 * <p>Get next descriptor for the given item</p>
		 * @return next description or null, if next is missing
		 * @throws IOException on any I/O errors.
		 */
		RepoItemDescriptor getNext() throws IOException;
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
		
		/**
		 * <p>Get description of previous commit</p>
		 * @return previous commit descriptor or null is previous commit is missing
		 * @throws IOException on any I/O errors
		 */
		CommitDescriptor getPrevious() throws IOException; 

		/**
		 * <p>Get description of next commit</p>
		 * @return next commit descriptor or null is next commit is missing
		 * @throws IOException on any I/O errors
		 */
		CommitDescriptor getNext() throws IOException; 
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
	 * <p>This interface describes translated query in the repository</p>
	 */
	public interface TransalatedQuery {
		/**
		 * <p>Get translated query id</p>
		 * @return query id. Can't be null
		 */
		UUID getId();
		
		/**
		 * <p>Get query string</p>
		 * @return query string. Can't be null or empty
		 */
		String getQueryString();
		
		/**
		 * <p>Get query interface associated with the query</p> 
		 * @return query interface. Can't be null
		 */
		LightRepoQueryInterface getQuery();
		
		/**
		 * <p>Get attributes associated with the query string</p>
		 * @return attributes associated. Can be empty but not null. When new attributes put in the returned table, association list must be modified 
		 */
		Hashtable<String, Object> getAttributes();
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
	 * <p>Get descriptors for paths queried</p>
	 * @param query query. Can't be null or empty. See syntax in the {@linkplain LightRepoInterface} description</p>
	 * @return list of content found. Can't be null
	 * @throws SyntaxException on syntax errors in the query
	 * @throws IOException on any I/O errors
	 */
	Iterable<RepoItemDescriptor> queryForPath(String query) throws SyntaxException, IOException;

	/**
	 * <p>Get descriptors for commits queried</p>
	 * @param query query. Can't be null or empty. See syntax in the {@linkplain LightRepoInterface} description</p>
	 * @return list of content found. Can't be null
	 * @throws SyntaxException on syntax errors in the query
	 * @throws IOException on any I/O errors
	 */
	Iterable<CommitDescriptor> queryForCommit(String query) throws SyntaxException, IOException;

	/**
	 * <p>Translate query string to internal form</p>
	 * @param query query to translate. Can't be null or empty. Subsequent call with the same query string must returns the same query UUID
	 * @param attributes attributes associated with query string. Can be null 
	 * @return UUID for string translated. Can't be null
	 * @throws SyntaxException on syntax errors in the query
	 * @see #queryForPath(UUID)
	 * @see #queryForCommit(UUID)
	 */
	UUID translateQuery(String query, Hashtable<String,Object> attributes) throws SyntaxException;

	/**
	 * <p>Get queries translated in the repository</p>
	 * @return list of queries translated. Can be empty but not null
	 * @throws IOException
	 */
	Iterable<TransalatedQuery> getQueriesTranslated() throws IOException;
	
	/**
	 * <p>Get descriptors for paths queried</p>
	 * @param query. Can't be null
	 * @return list of content found. Can't be null
	 * @throws SyntaxException on syntax errors in the query
	 * @throws IOException on any I/O errors
	 * @see #translateQuery(String, Hashtable)
	 */
	Iterable<RepoItemDescriptor> queryForPath(UUID query) throws SyntaxException, IOException;
	
	/**
	 * <p>Get descriptors for commits queried</p>
	 * @param query. Can't be null
	 * @return list of content found. Can't be null
	 * @throws SyntaxException on syntax errors in the query
	 * @throws IOException on any I/O errors
	 * @see #translateQuery(String, Hashtable)
	 */
	Iterable<CommitDescriptor> queryForCommit(UUID query) throws SyntaxException, IOException;
	
	/**
	 * <p>Calculate changes between the given versions of the content </p> 
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
