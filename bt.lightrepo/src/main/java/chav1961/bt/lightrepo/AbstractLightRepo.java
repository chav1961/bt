package chav1961.bt.lightrepo;


import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import chav1961.bt.lightrepo.TransactionalFileSystemOnFile.ChangeLogRecord;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.ChangesDescriptor.ChangeType;
import chav1961.bt.lightrepo.interfaces.LightRepoQueryInterface;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.CharUtils.Prescription;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.growablearrays.GrowableCharArray;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.basic.interfaces.OutputStreamGetter;
import chav1961.purelib.fsys.FileSystemOnFile;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class AbstractLightRepo implements LightRepoInterface, Closeable {
	public static final String					CURRENT_PATH = "current";
	public static final String					DELTAS_PATH = "deltas";
	public static final String					COMMITS_PATH = "commits";
	
	private final FileSystemInterface			nested;
	private final InputStreamGetter				inGetter;
	private final OutputStreamGetter			outGetter;
	private final Map<UUID,TransalatedQuery>	queries = new ConcurrentHashMap<>();
	private final TransactionalFileSystemOnFile	tfs;
	
	public AbstractLightRepo(final FileSystemInterface nested, final InputStreamGetter inGetter, final OutputStreamGetter outGetter) throws IOException {
		if (nested == null) {
			throw new NullPointerException("Nested file system can't be null");
		}
		else if (inGetter == null) {
			throw new NullPointerException("Input stream getter can't be null");
		}
		else if (outGetter == null) {
			throw new NullPointerException("Output stream getter can't be null");
		}
		else {
			this.nested = nested;
			this.inGetter = inGetter;
			this.outGetter = outGetter;
			this.tfs = new TransactionalFileSystemOnFile();
		}
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Iterable<RepoItemDescriptor> queryForPath(final String query) throws SyntaxException, IOException {
		if (query == null || query.isEmpty()) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else {
			final UUID	queryId = translateQuery(query, SimpleLightRepoQuery.NULL_ATTRIBUTES);
			
			try {
				return queryForPath(queryId);
			} finally {
				removeQuery(queryId);
			}
		}
	}

	@Override
	public Iterable<CommitDescriptor> queryForCommit(final String query) throws SyntaxException, IOException {
		if (query == null || query.isEmpty()) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else {
			final UUID	queryId = translateQuery(query, SimpleLightRepoQuery.NULL_ATTRIBUTES);
			
			try {
				return queryForCommit(queryId);
			} finally {
				removeQuery(queryId);
			}
		}
	}

	@Override
	public UUID translateQuery(final String query, final Hashtable<String, Object> attributes) throws SyntaxException {
		if (query == null || query.isEmpty()) {
			throw new IllegalArgumentException("Query string can't be null or empty");
		}
		else if (attributes == null) {
			throw new NullPointerException("Attributes can't be null");
		}
		else {
			for (Entry<UUID, TransalatedQuery> item : queries.entrySet()) {
				if (item.getValue().getQueryString().equals(query)) {
					return item.getValue().getId();
				}
			}
			final LightRepoQueryInterface	root = SimpleLightRepoQuery.translateQuery(query, attributes);
			final TransalatedQuery			tq = new TransalatedQueryImpl(query, attributes, root);
			
			queries.put(tq.getId(), tq);
			return tq.getId();
		}
	}

	@Override
	public Iterable<TransalatedQuery> getQueriesTranslated() throws IOException {
		return queries.values();
	}

	@Override
	public Iterable<RepoItemDescriptor> queryForPath(final UUID query) throws SyntaxException, IOException {
		if (query == null) {
			throw new NullPointerException("Query ID can't be null");
		}
		else {
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Iterable<CommitDescriptor> queryForCommit(final UUID query) throws SyntaxException, IOException {
		if (query == null) {
			throw new NullPointerException("Query ID can't be null");
		}
		else {
			// TODO Auto-generated method stub
			
			return null;
		}
	}

	@Override
	public Iterable<ChangesDescriptor> calculateChanges(final String path, final long firstVersion, final long secondVersion) throws IOException {
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("Path can't be null or empty");
		}
		else {
			return calculateChanges(getContent(path,firstVersion),getContent(path,secondVersion));
		}
	}

	@Override
	public FileSystemInterface getFileSystem() throws IOException {
		return nested;
	}

	@Override
	public TransactionDescriptor beginTransaction(final String author, final String comment) throws IOException {
		if (author == null || author.isEmpty()) {
			throw new IllegalArgumentException("Author can't be null or empty");
		}
		else if (comment == null || comment.isEmpty()) {
			throw new IllegalArgumentException("Comment can't be null or empty");
		}
		else {
			final UUID	id = UUID.randomUUID();
			final File	transDir = new File(System.getProperty("java.io.tmpdir"), ""+id);
			
			try{final TransactionalFileSystemOnFile	fsof = (TransactionalFileSystemOnFile) tfs.newInstance(transDir.toURI());
				final FileSystemInterface			fsi = nested.clone().open("/current").joinedList()[0];
				
				return new AbstractTransactionDescriptor(id, fsi, fsof, author, comment) {
							@Override
							public void commit() throws IOException {
								// TODO Auto-generated method stub
								if (!commitProcessed) {
									commitProcessed = true;
									
									fixCommit(fsi, fsof, getCommitId(), author, comment);
								}
							}
						};
			} catch (IOException e) {
				Utils.deleteDir(transDir);
				throw e;
			} catch (EnvironmentException e) {
				Utils.deleteDir(transDir);
				throw new IOException(e);
			}
		}
	}
	
	protected static void fixCommit(final FileSystemInterface fsi, final TransactionalFileSystemOnFile fsof, final UUID commitId, final String author,final String comment) throws IOException {
		try{fsi.lock("/", false);

			for (String item : buildNewContent(fsof)) {
				try(final FileSystemInterface	left = fsi.clone().open(item);
					final FileSystemInterface	right = fsof.clone().open(item)) {
					
					if (right.isDirectory()) {
						left.mkDir();
					}
					else {
						right.copy(left.open("../"));
					}
				}
			}
			
			for (String[] item : buildChangedContent(fsof)) {
				try(final FileSystemInterface	left = fsi.clone().open(item[0]);
					final FileSystemInterface	right = fsof.clone().open(item[1])) {
	
					if (requireDeltas(item[0], item[1])) {
						try(final Reader				leftReader = left.charRead();
							final Reader				rightReader = right.charRead()) {
							
							for (ChangesDescriptor desc : calculateChanges(leftReader, rightReader)) {
								
							}
						}
					}
					moveChanges2Repo(right, left);
				}
			}

			for (String[] item : buildRenamedContent(fsof)) {
				try(final FileSystemInterface	left = fsi.clone().open(item[0]).rename(item[0])) {
				}
			}

			for (String item : buildRemovedContent(fsof)) {
				try(final FileSystemInterface	left = fsi.clone().open(item).deleteAll()) {
				}
			}
		} finally {
			fsi.unlock("/", false);
		}
	}

	protected void removeQuery(final UUID queryId) {
		queries.remove(queryId);
	}

	protected Reader getContent(final String path, final long version) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	protected static Iterable<ChangesDescriptor> calculateChanges(final Reader left, final Reader right) throws IOException {
		final GrowableCharArray<?>	gLeft = new GrowableCharArray<>(false), gRight = new GrowableCharArray<>(false);
		
		gLeft.append(left);
		gRight.append(right);
		
		final Prescription			pre = CharUtils.calcLevenstain(gLeft.extract(), gRight.extract());
		final ChangesDescriptor[]	desc = new ChangesDescriptor[pre.route.length];
		
		for (int index = 0, maxIndex = desc.length; index < maxIndex; index++) {
			switch (pre.route[index][0]) {
				case Prescription.LEV_INSERT 	:
					desc[index] = new ChangesDescriptorImpl(ChangeType.INSERTED, pre.route[index][1], "", pre.route[index][1], "");
					break;
				case Prescription.LEV_DELETE 	:
					desc[index] = new ChangesDescriptorImpl(ChangeType.REMOVED, pre.route[index][1], "", pre.route[index][1], "");
					break;
				case Prescription.LEV_REPLACE	:
					desc[index] = new ChangesDescriptorImpl(ChangeType.CHANGED, pre.route[index][1], "", pre.route[index][1], "");
					break;
			}
		}
		return Arrays.asList(desc);
	}

	private static String[] buildNewContent(final TransactionalFileSystemOnFile fsof) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static String[][] buildChangedContent(final TransactionalFileSystemOnFile fsof) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String[][] buildRenamedContent(final TransactionalFileSystemOnFile fsof) {
		// TODO Auto-generated method stub
		return null;
	}

	private static String[] buildRemovedContent(final TransactionalFileSystemOnFile fsof) {
		// TODO Auto-generated method stub
		return null;
	}

	private static void moveChanges2Repo(final FileSystemInterface from, final FileSystemInterface to) throws IOException {
		from.copy(to);
	}

	private static boolean requireDeltas(final String nameLeft, final String nameRight) {
		// TODO Auto-generated method stub
		return false;
	}

	private static class TransalatedQueryImpl implements TransalatedQuery {
		private final UUID						uniqueId = UUID.randomUUID();
		private final String					query;
		private final Hashtable<String, Object>	attributes;
		private final LightRepoQueryInterface	translated;
		
		private TransalatedQueryImpl(final String query, final Hashtable<String, Object> attributes, final LightRepoQueryInterface translated) {
			this.query = query;
			this.attributes = attributes;
			this.translated = translated;
		}

		@Override
		public UUID getId() {
			return uniqueId;
		}

		@Override
		public String getQueryString() {
			return query;
		}

		@Override
		public Hashtable<String, Object> getAttributes() {
			return attributes;
		}
		
		@Override
		public LightRepoQueryInterface getQuery() {
			return translated;
		}

		@Override
		public String toString() {
			return "TransalatedQueryImpl [uniqueId=" + uniqueId + ", query=" + query + ", attributes=" + attributes + ", translated=" + translated + "]";
		}
	}
	
	private static class ChangesDescriptorImpl implements ChangesDescriptor {
		private final ChangeType	changeType;
		private final long			firstLine, secondLine;
		private final String		first, second;
		
		private ChangesDescriptorImpl(final ChangeType changeType, final long firstLine, final String first, final long secondLine, final String second) {
			this.changeType = changeType;
			this.firstLine = firstLine;
			this.first = first;
			this.secondLine = secondLine;
			this.second = second;
		}

		@Override
		public long getFirstLine() {
			return firstLine;
		}

		@Override
		public String getFirstLineContent() {
			return first;
		}

		@Override
		public long getSecondLine() {
			return secondLine;
		}

		@Override
		public String getSecondLineContent() {
			return second;
		}

		@Override
		public ChangeType getChangeType() {
			return changeType;
		}

		@Override
		public String toString() {
			return "ChangesDescriptorImpl [changeType=" + changeType + ", firstLine=" + firstLine + ", secondLine=" + secondLine + ", first=" + first + ", second=" + second + "]";
		}
	}
	
	static abstract class AbstractTransactionDescriptor implements TransactionDescriptor {
		final String	author;
		final String	comment;

		private final FileSystemInterface 	joinedPoint;
		private final FileSystemInterface 	joinedFS;
		private final UUID					commitId;
		protected boolean 					commitProcessed = false;
		
		AbstractTransactionDescriptor(final UUID commitId, final FileSystemInterface joinedPoint, final FileSystemInterface joinedFS, final String author, final String comment) throws IOException {
			this.joinedPoint = joinedPoint;
			this.joinedFS = joinedFS;
			this.commitId = commitId;
			this.author = author;
			this.comment = comment;
			joinedPoint.join(joinedFS);
		}

		@Override public abstract void commit() throws IOException;
		
		@Override
		public void close() throws IOException {
			if (!commitProcessed) {
				joinedPoint.unjoin();
			}
			commitProcessed = true;
		}

		@Override
		public UUID getCommitId() {
			return commitId;
		}

		@Override
		public FileSystemInterface getFileSystem() throws IOException {
			if (commitProcessed) {
				throw new IOException("Attempt to get file system after calling commit()");
			}
			else {
				return joinedFS;
			}
		}
	}
}
