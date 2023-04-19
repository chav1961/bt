package chav1961.bt.lightrepo;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import chav1961.bt.lightrepo.interfaces.LightRepoQueryInterface;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.ChangesDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.CommitDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.RepoItemDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.ChangesDescriptor.ChangeType;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.InputStreamGetter;
import chav1961.purelib.cdb.SyntaxNode;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.json.JsonUtils;
import chav1961.purelib.json.interfaces.JsonNodeType;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;


/*
{"id" : "ZZZ",
 "timestamp" : NNN,
 "author" : "ZZZ",
 "comment" : "ZZZ",
 "content: [
	{"path" : "ZZZ",
	 "version" : NNN,
	 "content" : "ZZZ - path in deltas",
	 "changes" : [
		["ZZZ - type", NNN - in number, "ZZZ - in content", NNN - out number, "ZZZ - out content"], ...
	 ]
	}, ...
 ]
}
 */
public class AbstractCommitDescriptorRepo {
	private static final String		F_ID = "id";
	private static final String		F_TIMESTAMP = "timestamp";
	private static final String		F_AUTHOR = "author";
	private static final String		F_COMMENT = "comment";
	private static final String		F_CONTENT = "content";
	private static final String		F_PATH = "path";
	private static final String		F_VERSION = "version";
	private static final String		F_CHANGES = "changes";
	
	private final FileSystemInterface	commits;
	
	public AbstractCommitDescriptorRepo(final FileSystemInterface commits) throws NullPointerException {
		if (commits == null) {
			throw new NullPointerException("Commits file system can't be null");
		}
		else {
			this.commits = commits;
		}
	}
	
	public boolean exists(final UUID commitId) throws NullPointerException {
		if (commitId == null) {
			throw new NullPointerException("Commit id can't be null");
		}
		else {
			try(final FileSystemInterface	fsi = commits.clone().open("/"+commitId.toString())) {
				
				return fsi.exists() && fsi.isFile();
			} catch (IOException e) {
				return false;
			}
		}
	}
	
	public CommitDescriptor getCommitDescriptor(final UUID commitId) throws NullPointerException, IOException {
		if (commitId == null) {
			throw new NullPointerException("Commit id can't be null");
		}
		else {
			try(final FileSystemInterface	fsi = commits.clone().open("/"+commitId.toString())) {
				
				if (fsi.exists() && fsi.isFile()) {
					try(final Reader	rdr = fsi.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
						
						return loadCommitDescriptor(rdr);
					} catch (SyntaxException e) {
						throw new IOException("Syntax error in the commit descriptor ["+commitId+"]: "+e.getRow()+"/"+e.getCol()+" - "+e.getLocalizedMessage()); 
					}
				}
				else {
					throw new IOException("Commit descriptor ["+commitId+"] not exists or is not a file"); 
				}
			}
		}
	}
	
	public void storeCommitDescriptor(final UUID commitId, final CommitDescriptor desc) throws NullPointerException, IllegalArgumentException, IOException {
		if (commitId == null) {
			throw new NullPointerException("Commit id can't be null");
		}
		else if (desc == null) {
			throw new NullPointerException("Commit descriptor can't be null");
		}
		else if (exists(commitId)) {
			throw new IllegalArgumentException("Attempt to replace existent commit ["+commitId+"]");
		}
		else {
			try(final FileSystemInterface	fsi = commits.clone().open("/"+commitId.toString()).create()) {
				
				try(final Writer	wr = fsi.charWrite(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
					
					storeCommitDescriptor(wr, desc);
					wr.flush();
				}
			}
		}
	}

	public Iterable<CommitDescriptor> getCommits(final LightRepoQueryInterface query) throws NullPointerException, IllegalArgumentException, IOException {
		if (query == null) {
			throw new NullPointerException("Query interface can't be null");
		}
		else {
			final List<CommitDescriptor>	result = new ArrayList<>();
			
			try(final FileSystemInterface	fsi = commits.clone()) {
				if (query.hasCommits()) {
					if (query.hasExplicitCommits()) {
						fsi.list(query.getCommitsAwaited(), (fs)-> {
							if (fsi.exists() && fsi.isFile()) {
								try(final Reader	rdr = fsi.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
									final CommitDescriptor	cd = loadCommitDescriptor(rdr);
									
									if (query.testCommit(cd)) {
										result.add(cd);
									}
								} catch (SyntaxException e) {
								}
							}
							return ContinueMode.CONTINUE;
						});
					}
					else {
						fsi.list((fs)-> {
							if (fsi.exists() && fsi.isFile()) {
								try(final Reader	rdr = fsi.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
									final CommitDescriptor	cd = loadCommitDescriptor(rdr);
									
									if (query.testCommit(cd)) {
										result.add(cd);
									}
								} catch (SyntaxException e) {
								}
							}
							return ContinueMode.CONTINUE;
						});
					}
					return null;
				}
				else if (query.hasRepoItems()) {
					fsi.list((fs)-> {
						if (fsi.exists() && fsi.isFile()) {
							try(final Reader	rdr = fsi.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
								final CommitDescriptor	cd = loadCommitDescriptor(rdr);
								
								if (query.testCommit(cd)) {
									result.add(cd);
								}
							} catch (SyntaxException e) {
							}
						}
						return ContinueMode.CONTINUE;
					});
				}
				else {
					fsi.list((fs)-> {
						if (fsi.exists() && fsi.isFile()) {
							try(final Reader	rdr = fsi.charRead(PureLibSettings.DEFAULT_CONTENT_ENCODING)) {
								
								result.add(loadCommitDescriptor(rdr));
							} catch (SyntaxException e) {
							}
						}
						return ContinueMode.CONTINUE;
					});
				}
			}
			
			return result;
		}
	}
	
	static CommitDescriptor loadCommitDescriptor(final Reader rdr) throws IOException, SyntaxException {
		try(final JsonStaxParser	parser = new JsonStaxParser(rdr)) {
			parser.next();
			
			final JsonNode			root = JsonUtils.loadJsonTree(parser);
			return toCommitDescriptor(root);
		}
	}

	private static CommitDescriptor toCommitDescriptor(final JsonNode root) throws SyntaxException {
		// TODO Auto-generated method stub
		final StringBuilder	sb = new StringBuilder();
		
		if (!JsonUtils.checkJsonMandatories(root, sb, F_ID, F_TIMESTAMP, F_AUTHOR, F_COMMENT, F_CONTENT)) {
			throw new SyntaxException(0, 0, "Mandatory fields ["+sb+"] are missing in the commit descriptor");
		}
		else if (!JsonUtils.checkJsonFieldTypes(root, sb, F_ID+"/"+JsonUtils.JSON_TYPE_STR+JsonUtils.JSON_TYPE_NOT_NULL, 
				F_TIMESTAMP+"/"+JsonUtils.JSON_TYPE_INTEGER+JsonUtils.JSON_TYPE_NOT_NULL, 
				F_AUTHOR+"/"+JsonUtils.JSON_TYPE_STR+JsonUtils.JSON_TYPE_NOT_NULL, 
				F_COMMENT+"/"+JsonUtils.JSON_TYPE_STR+JsonUtils.JSON_TYPE_NOT_NULL, 
				F_CONTENT+"/"+JsonUtils.JSON_TYPE_ARR+JsonUtils.JSON_TYPE_NOT_NULL)) {
			throw new SyntaxException(0, 0, "Fields ["+sb+"] contains illegal values of illegal data type");
		}
		else {
			final UUID		commitId = UUID.fromString(root.getChild(F_ID).getStringValue());
			final long		timestamp = root.getChild(F_TIMESTAMP).getLongValue();
			final String	author = root.getChild(F_AUTHOR).getStringValue();
			final String	comment = root.getChild(F_COMMENT).getStringValue();
			final List<RepoItemDescriptor>	list = new ArrayList<>();
			
			for (JsonNode item : root.getChild(F_CONTENT).children()) {
				if (!JsonUtils.checkJsonMandatories(item, sb, F_PATH, F_VERSION, F_CONTENT, F_CHANGES)) {
					throw new SyntaxException(0, 0, "Mandatory fields ["+sb+"] are missing in the commit descriptor field ["+F_CONTENT+"]");
				}
				else if (!JsonUtils.checkJsonFieldTypes(item, sb, F_PATH+"/"+JsonUtils.JSON_TYPE_STR+JsonUtils.JSON_TYPE_NOT_NULL, 
						F_VERSION+"/"+JsonUtils.JSON_TYPE_INTEGER+JsonUtils.JSON_TYPE_NOT_NULL, 
						F_CONTENT+"/"+JsonUtils.JSON_TYPE_STR+JsonUtils.JSON_TYPE_NOT_NULL, 
						F_CHANGES+"/"+JsonUtils.JSON_TYPE_ARR+JsonUtils.JSON_TYPE_NOT_NULL)) {
					throw new SyntaxException(0, 0, "Fields ["+sb+"] contains illegal values of illegal data type in the commit descriptor field ["+F_CONTENT+"]");
				}
				else {
					final String	path = item.getChild(F_PATH).getStringValue();
					final long		version = root.getChild(F_TIMESTAMP).getLongValue();
					final String	content = item.getChild(F_CONTENT).getStringValue();
					final List<ChangesDescriptor>	desc = new ArrayList<>();
					
					for (JsonNode change : item.getChild(F_CHANGES).children()) {
						final JsonNode[]	changeDesc = change.children();
						
						desc.add(new ChangeDescriptorImpl(ChangeType.valueOf(changeDesc[0].getStringValue()), changeDesc[1].getLongValue(), changeDesc[2].getStringValue(), changeDesc[3].getLongValue(), changeDesc[4].getStringValue()));
					}
					list.add(new RepoItemDescriptorImpl(commitId, timestamp, path, version, author, comment, null, desc.toArray(new ChangesDescriptor[desc.size()])));
				}
			}
			return new CommitDescriptorImpl(commitId, timestamp, author, comment, list.toArray(new RepoItemDescriptor[list.size()]));
		}
	}

	static void storeCommitDescriptor(final Writer wr, final CommitDescriptor desc) throws IOException {
		try(final JsonStaxPrinter	printer = new JsonStaxPrinter(wr)) {
			printer.startObject();
			printer.name(F_ID).value(desc.getCommitId().toString()).splitter();
			printer.name(F_TIMESTAMP).value(desc.getTimestamp().getTime()).splitter();
			printer.name(F_AUTHOR).value(desc.getAuthor()).splitter();
			printer.name(F_COMMENT).value(desc.getComment()).splitter();
			printer.name(F_CONTENT).startArray();
			
			for (RepoItemDescriptor item : desc.getCommitContent()) {
				printer.startObject();
				printer.name(F_PATH).value(item.getPath()).splitter();
				printer.name(F_VERSION).value(item.getVersion()).splitter();
				printer.name(F_CONTENT).value(item.getPath()).splitter();
				printer.name(F_CHANGES).startArray();
				for (ChangesDescriptor change : item.getChanges()) {
					printer.value(change.getChangeType().toString()).value(change.getFirstLine()).value(change.getFirstLineContent()).value(change.getSecondLine()).value(change.getSecondLineContent());
				}
				printer.endArray().endObject();
			}
			printer.endArray().endObject().flush();
		}		
	}
	
	private static class ChangeDescriptorImpl implements ChangesDescriptor {
		private final ChangeType	changeType;
		private final long			first, second;
		private final String		firstLine, secondLine;

		private ChangeDescriptorImpl(final ChangeType changeType, final long first, final String firstLine, final long second, final String secondLine) {
			this.changeType = changeType;
			this.first = first;
			this.second = second;
			this.firstLine = firstLine;
			this.secondLine = secondLine;
		}

		@Override
		public long getFirstLine() {
			return first;
		}

		@Override
		public String getFirstLineContent() {
			return firstLine;
		}

		@Override
		public long getSecondLine() {
			return second;
		}

		@Override
		public String getSecondLineContent() {
			return secondLine;
		}

		@Override
		public ChangeType getChangeType() {
			return changeType;
		}

		@Override
		public String toString() {
			return "ChangeDescriptorImpl [changeType=" + changeType + ", first=" + first + ", second=" + second + ", firstLine=" + firstLine + ", secondLine=" + secondLine + "]";
		}
	}
	
	private static class RepoItemDescriptorImpl implements RepoItemDescriptor {
		private final UUID					commitId;
		private final long					timestamp;
		private final String				path;
		private final long					version;
		private final String				author;
		private final String				comment;
		private final InputStreamGetter		contentGetter;
		private final ChangesDescriptor[]	changes;
		
		private RepoItemDescriptorImpl(final UUID commitId, final long timestamp, final String path, final long version, final String author, final String comment, final InputStreamGetter contentGetter, final ChangesDescriptor[] changes) {
			this.commitId = commitId;
			this.timestamp = timestamp;
			this.path = path;
			this.version = version;
			this.author = author;
			this.comment = comment;
			this.contentGetter = contentGetter;
			this.changes = changes;
		}

		@Override
		public UUID getCommitId() {
			return commitId;
		}

		@Override
		public Date getTimestamp() {
			return new Date(timestamp);
		}

		@Override
		public String getPath() {
			return path;
		}

		@Override
		public long getVersion() {
			return version;
		}

		@Override
		public String getAuthor() {
			return author;
		}

		@Override
		public String getComment() {
			return comment;
		}

		@Override
		public InputStream getContent() throws IOException {
			return contentGetter.getOutputContent();
		}

		@Override
		public ChangesDescriptor[] getChanges() throws IOException {
			return changes.clone();
		}

		@Override
		public String toString() {
			return "RepoItemDescriptorImpl [commitId=" + commitId + ", timestamp=" + timestamp + ", path=" + path + ", version=" + version + ", author=" + author + ", comment=" + comment + ", changes=" + Arrays.toString(changes) + "]";
		}

		@Override
		public CommitDescriptor getCommit() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RepoItemDescriptor getPrevious() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public RepoItemDescriptor getNext() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
	}
	
	private static class CommitDescriptorImpl implements CommitDescriptor {
		private final UUID					commitId;
		private final long					timestamp;
		private final String				author;
		private final String				comment;
		private final RepoItemDescriptor[]	desc;

		private CommitDescriptorImpl(UUID commitId, long timestamp, String author, String comment, RepoItemDescriptor[] desc) {
			this.commitId = commitId;
			this.timestamp = timestamp;
			this.author = author;
			this.comment = comment;
			this.desc = desc;
		}

		@Override
		public UUID getCommitId() {
			return commitId;
		}

		@Override
		public Date getTimestamp() {
			return new Date(timestamp);
		}

		@Override
		public String getAuthor() {
			return author;
		}

		@Override
		public String getComment() {
			return comment;
		}

		@Override
		public RepoItemDescriptor[] getCommitContent() {
			return desc.clone();
		}

		@Override
		public String toString() {
			return "CommitDescriptorImpl [commitId=" + commitId + ", timestamp=" + timestamp + ", author=" + author + ", comment=" + comment + ", desc=" + Arrays.toString(desc) + "]";
		}

		@Override
		public CommitDescriptor getPrevious() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public CommitDescriptor getNext() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
