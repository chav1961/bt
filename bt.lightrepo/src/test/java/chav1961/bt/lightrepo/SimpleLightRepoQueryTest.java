package chav1961.bt.lightrepo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.lightrepo.SimpleLightRepoQuery.ExpressionResult;
import chav1961.bt.lightrepo.SimpleLightRepoQuery.ExpressionResult.ValueType;
import chav1961.bt.lightrepo.SimpleLightRepoQuery.LexType;
import chav1961.bt.lightrepo.SimpleLightRepoQuery.Lexema;
import chav1961.bt.lightrepo.SimpleLightRepoQuery.NodeType;
import chav1961.bt.lightrepo.SimpleLightRepoQuery.Priority;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface;
import chav1961.bt.lightrepo.interfaces.LightRepoQueryInterface;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.ChangesDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.CommitDescriptor;
import chav1961.bt.lightrepo.interfaces.LightRepoInterface.RepoItemDescriptor;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.cdb.SyntaxNode;


public class SimpleLightRepoQueryTest {
	@Test
	public void parseTest() {
		try{Lexema[]	lex = SimpleLightRepoQuery.parseQuery((""+SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);
		
			Assert.assertEquals(1, lex.length);
			Assert.assertEquals(LexType.EOF, lex[0].getType());
			
			lex = SimpleLightRepoQuery.parseQuery(("~,()+->>=<<=...==!=12345\"test\"or and not in"+SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);
			
			Assert.assertEquals(21, lex.length);
			Assert.assertEquals(LexType.LIKE, lex[0].getType());
			Assert.assertEquals(LexType.COMMA, lex[1].getType());
			Assert.assertEquals(LexType.OPENB, lex[2].getType());
			Assert.assertEquals(LexType.CLOSEB, lex[3].getType());
			Assert.assertEquals(LexType.ADD, lex[4].getType());
			Assert.assertEquals(LexType.SUB, lex[5].getType());
			Assert.assertEquals(LexType.GT, lex[6].getType());
			Assert.assertEquals(LexType.GE, lex[7].getType());
			Assert.assertEquals(LexType.LT, lex[8].getType());
			Assert.assertEquals(LexType.LE, lex[9].getType());
			Assert.assertEquals(LexType.RANGE, lex[10].getType());
			Assert.assertEquals(LexType.DOT, lex[11].getType());
			Assert.assertEquals(LexType.EQ, lex[12].getType());
			Assert.assertEquals(LexType.NE, lex[13].getType());
			Assert.assertEquals(LexType.NUMBER, lex[14].getType());
			Assert.assertEquals(LexType.STRING, lex[15].getType());
			Assert.assertEquals(LexType.OR, lex[16].getType());
			Assert.assertEquals(LexType.AND, lex[17].getType());
			Assert.assertEquals(LexType.NOT, lex[18].getType());
			Assert.assertEquals(LexType.IN, lex[19].getType());
			Assert.assertEquals(LexType.EOF, lex[20].getType());

			lex = SimpleLightRepoQuery.parseQuery((LightRepoInterface.EXISTS_FUNC + ' ' 
									+ LightRepoInterface.APPEARS_FUNC + ' '
									+ LightRepoInterface.DISAPPEARS_FUNC + ' '
									+ LightRepoInterface.CREATED_FUNC + ' '
									+ LightRepoInterface.CHANGED_FUNC + ' '
									+ LightRepoInterface.RENAMED_FUNC + ' '
									+ LightRepoInterface.REMOVED_FUNC + ' '
									+ LightRepoInterface.CONTAINS_FUNC + ' '
									+ LightRepoInterface.NOW_FUNC + SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);

			Assert.assertEquals(10, lex.length);
			Assert.assertEquals(LexType.EXISTS, lex[0].getType());
			Assert.assertEquals(LexType.APPEARS, lex[1].getType());
			Assert.assertEquals(LexType.DISAPPEARS, lex[2].getType());
			Assert.assertEquals(LexType.CREATED, lex[3].getType());
			Assert.assertEquals(LexType.CHANGED, lex[4].getType());
			Assert.assertEquals(LexType.RENAMED, lex[5].getType());
			Assert.assertEquals(LexType.REMOVED, lex[6].getType());
			Assert.assertEquals(LexType.CONTAINS, lex[7].getType());
			Assert.assertEquals(LexType.NOW, lex[8].getType());
			Assert.assertEquals(LexType.EOF, lex[9].getType());
			
			lex = SimpleLightRepoQuery.parseQuery((LightRepoInterface.PREV_MOD + ' ' 
									+ LightRepoInterface.NEXT_MOD + ' '
									+ LightRepoInterface.UPPERCASE_MOD + ' '
									+ LightRepoInterface.LOWERCASE_MOD + SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);
			
			Assert.assertEquals(5, lex.length);
			Assert.assertEquals(LexType.PREV, lex[0].getType());
			Assert.assertEquals(LexType.NEXT, lex[1].getType());
			Assert.assertEquals(LexType.UPPERCASE, lex[2].getType());
			Assert.assertEquals(LexType.LOWERCASE, lex[3].getType());
			Assert.assertEquals(LexType.EOF, lex[4].getType());
			
			lex = SimpleLightRepoQuery.parseQuery((LightRepoInterface.FILE_VAR + ' ' 
									+ LightRepoInterface.COMMIT_VAR + SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);

			Assert.assertEquals(3, lex.length);
			Assert.assertEquals(LexType.FILE, lex[0].getType());
			Assert.assertEquals(LexType.COMMIT, lex[1].getType());
			Assert.assertEquals(LexType.EOF, lex[2].getType());
			
			lex = SimpleLightRepoQuery.parseQuery((LightRepoInterface.AUTHOR_F + ' ' 
									+ LightRepoInterface.COMMENT_F + ' '
									+ LightRepoInterface.COMMIT_ID_F + ' '
									+ LightRepoInterface.PATH_F + ' '
									+ LightRepoInterface.CHANGE_F + ' '
									+ LightRepoInterface.TIMESTAMP_F + ' '
									+ LightRepoInterface.PARSEABLE_F + ' '
									+ LightRepoInterface.VERSION_F + ' '
									+ LightRepoInterface.CONTENT_F + SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);
			
			Assert.assertEquals(10, lex.length);
			Assert.assertEquals(LexType.AUTHOR, lex[0].getType());
			Assert.assertEquals(LexType.COMMENT, lex[1].getType());
			Assert.assertEquals(LexType.ID, lex[2].getType());
			Assert.assertEquals(LexType.PATH, lex[3].getType());
			Assert.assertEquals(LexType.CHANGE, lex[4].getType());
			Assert.assertEquals(LexType.TIMESTAMP, lex[5].getType());
			Assert.assertEquals(LexType.PARSEABLE, lex[6].getType());
			Assert.assertEquals(LexType.VERSION, lex[7].getType());
			Assert.assertEquals(LexType.CONTENT, lex[8].getType());
			Assert.assertEquals(LexType.EOF, lex[9].getType());
			
			try{SimpleLightRepoQuery.parseQuery(("?" + SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);
				Assert.fail("Mandatory exception was not detected (unknown lexema)");
			} catch (SyntaxException e) {
			}
			try{SimpleLightRepoQuery.parseQuery(("unknown" + SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);
				Assert.fail("Mandatory exception was not detected (unknown predefined name)");
			} catch (SyntaxException e) {
			}
		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void translateTermCommitTest() {
		try{SyntaxNode<NodeType, SyntaxNode>	root = buildNode("commit.author", Priority.TERM, 3);
			
			Assert.assertEquals(NodeType.VAR, root.getType());
			Assert.assertEquals(LexType.COMMIT, root.cargo);
			Assert.assertEquals(1, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.AUTHOR, root.children[0].cargo);
			
			root = buildNode("commit.author.uppercase", Priority.TERM, 5);
			
			Assert.assertEquals(NodeType.VAR, root.getType());
			Assert.assertEquals(LexType.COMMIT, root.cargo);
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.AUTHOR, root.children[0].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[1].getType());
			Assert.assertEquals(LexType.UPPERCASE, root.children[1].cargo);

			root = buildNode("commit.prev.author.uppercase", Priority.TERM, 7);

			Assert.assertEquals(NodeType.VAR, root.getType());
			Assert.assertEquals(LexType.COMMIT, root.cargo);
			Assert.assertEquals(3, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.PREV, root.children[0].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[1].getType());
			Assert.assertEquals(LexType.AUTHOR, root.children[1].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[2].getType());
			Assert.assertEquals(LexType.UPPERCASE, root.children[2].cargo);

			root = buildNode("commit.next.author.uppercase", Priority.TERM, 7);

			Assert.assertEquals(NodeType.VAR, root.getType());
			Assert.assertEquals(LexType.COMMIT, root.cargo);
			Assert.assertEquals(3, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.NEXT, root.children[0].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[1].getType());
			Assert.assertEquals(LexType.AUTHOR, root.children[1].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[2].getType());
			Assert.assertEquals(LexType.UPPERCASE, root.children[2].cargo);
			
		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void translateTermPathTest() {
		try{SyntaxNode<NodeType, SyntaxNode>	root = buildNode("file.path", Priority.TERM, 3);
			
			Assert.assertEquals(NodeType.VAR, root.getType());
			Assert.assertEquals(LexType.FILE, root.cargo);
			Assert.assertEquals(1, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.PATH, root.children[0].cargo);
			
			root = buildNode("file.path.uppercase", Priority.TERM, 5);
			
			Assert.assertEquals(NodeType.VAR, root.getType());
			Assert.assertEquals(LexType.FILE, root.cargo);
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.PATH, root.children[0].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[1].getType());
			Assert.assertEquals(LexType.UPPERCASE, root.children[1].cargo);

			root = buildNode("file.prev.path.uppercase", Priority.TERM, 7);

			Assert.assertEquals(NodeType.VAR, root.getType());
			Assert.assertEquals(LexType.FILE, root.cargo);
			Assert.assertEquals(3, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.PREV, root.children[0].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[1].getType());
			Assert.assertEquals(LexType.PATH, root.children[1].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[2].getType());
			Assert.assertEquals(LexType.UPPERCASE, root.children[2].cargo);

			root = buildNode("file.next.path.uppercase", Priority.TERM, 7);

			Assert.assertEquals(NodeType.VAR, root.getType());
			Assert.assertEquals(LexType.FILE, root.cargo);
			Assert.assertEquals(3, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.NEXT, root.children[0].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[1].getType());
			Assert.assertEquals(LexType.PATH, root.children[1].cargo);
			Assert.assertEquals(NodeType.VAR, root.children[2].getType());
			Assert.assertEquals(LexType.UPPERCASE, root.children[2].cargo);
			
		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void translateTermTest() {
		try{SyntaxNode<NodeType, SyntaxNode>	root = buildNode("100", Priority.TERM, 1);
			
			Assert.assertEquals(NodeType.NUM_CONST, root.getType());
			Assert.assertEquals(100, root.value);

			root = buildNode("\"100\"", Priority.TERM, 1);
			
			Assert.assertEquals(NodeType.STR_CONST, root.getType());
			Assert.assertArrayEquals("100".toCharArray(), (char[])root.cargo);

		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void translateAddTest() {
		try{SyntaxNode<NodeType, SyntaxNode>	root = buildNode("100+20", Priority.ADD, 3);
			
			Assert.assertEquals(NodeType.ADD, root.getType());
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(0, root.value);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[0].getType());
			Assert.assertEquals(100, root.children[0].value);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[1].getType());
			Assert.assertEquals(20, root.children[1].value);

			root = buildNode("100-20", Priority.ADD, 3);
			
			Assert.assertEquals(NodeType.ADD, root.getType());
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(1, root.value);			
			Assert.assertEquals(NodeType.NUM_CONST, root.children[0].getType());
			Assert.assertEquals(100, root.children[0].value);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[1].getType());
			Assert.assertEquals(20, root.children[1].value);
		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void translateComparisonTest() {
		try{SyntaxNode<NodeType, SyntaxNode>	root = buildNode("100 > 20", Priority.COMP, 3);
			
			Assert.assertEquals(NodeType.COMPARISON, root.getType());
			Assert.assertEquals(LexType.GT, root.cargo);
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[0].getType());
			Assert.assertEquals(100, root.children[0].value);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[1].getType());
			Assert.assertEquals(20, root.children[1].value);

		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void translateInListTest() {
		try{SyntaxNode<NodeType, SyntaxNode>	root = buildNode("100 in 10..20,30..40,50", Priority.COMP, 11);
			
			Assert.assertEquals(NodeType.COMPARISON, root.getType());
			Assert.assertEquals(LexType.IN, root.cargo);
			Assert.assertEquals(4, root.children.length);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[0].getType());
			Assert.assertEquals(100, root.children[0].value);
			Assert.assertEquals(NodeType.RANGE, root.children[1].getType());
			Assert.assertEquals(2, root.children[1].children.length);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[1].children[0].getType());
			Assert.assertEquals(10, root.children[1].children[0].value);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[1].children[1].getType());
			Assert.assertEquals(20, root.children[1].children[1].value);
			Assert.assertEquals(NodeType.RANGE, root.children[2].getType());
			Assert.assertEquals(2, root.children[2].children.length);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[3].getType());
			Assert.assertEquals(50, root.children[3].value);
		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void translateBooleanTest() {
		try{SyntaxNode<NodeType, SyntaxNode>	root = buildNode("not 100", Priority.NOT, 2);
			
			Assert.assertEquals(NodeType.NOT, root.getType());
			Assert.assertEquals(1, root.children.length);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[0].getType());
			Assert.assertEquals(100, root.children[0].value);
			
			root = buildNode("100 and 20", Priority.AND, 3);
			
			Assert.assertEquals(NodeType.AND, root.getType());
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[0].getType());
			Assert.assertEquals(100, root.children[0].value);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[1].getType());
			Assert.assertEquals(20, root.children[1].value);

			root = buildNode("100 or 20", Priority.OR, 3);
			
			Assert.assertEquals(NodeType.OR, root.getType());
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[0].getType());
			Assert.assertEquals(100, root.children[0].value);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[1].getType());
			Assert.assertEquals(20, root.children[1].value);
			
		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	@Test
	public void translateNestingTest() {
		try{SyntaxNode<NodeType, SyntaxNode>	root = buildNode("(100 + 20)", Priority.TERM, 5);
			
			Assert.assertEquals(NodeType.ADD, root.getType());
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[0].getType());
			Assert.assertEquals(100, root.children[0].value);
			Assert.assertEquals(NodeType.NUM_CONST, root.children[1].getType());
			Assert.assertEquals(20, root.children[1].value);

			root = buildNode("now()", Priority.TERM, 3);
			
			Assert.assertEquals(NodeType.FUNCION, root.getType());
			Assert.assertEquals(LexType.NOW, root.cargo);
			Assert.assertEquals(0, root.children.length);

			root = buildNode("appears(file,\"test\")", Priority.TERM, 6);
			
			Assert.assertEquals(NodeType.FUNCION, root.getType());
			Assert.assertEquals(LexType.APPEARS, root.cargo);
			Assert.assertEquals(2, root.children.length);
			Assert.assertEquals(NodeType.VAR, root.children[0].getType());
			Assert.assertEquals(LexType.FILE, root.children[0].cargo);
			Assert.assertEquals(NodeType.STR_CONST, root.children[1].getType());
			Assert.assertArrayEquals("test".toCharArray(), (char[])root.children[1].cargo);
			
		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void instanceTest() {
		final UUID	id1 = UUID.randomUUID(), id2 = UUID.randomUUID();
		
		try{final LightRepoQueryInterface	lrqi = SimpleLightRepoQuery.translateQuery("commit.id in \""+id1+"\",\""+id2+"\" and file.path ~ \"/test*\"", SimpleLightRepoQuery.NULL_ATTRIBUTES);
		
			Assert.assertTrue(lrqi.hasCommits());
			Assert.assertTrue(lrqi.hasExplicitCommits());
			Assert.assertTrue(lrqi.hasRepoItems());
			Assert.assertTrue(lrqi.hasExplicitRepoItems());
			Assert.assertEquals(id1+"|"+id2, lrqi.getCommitsAwaited());
			Assert.assertEquals("/test*", lrqi.getRepoItemsAwaited());
		} catch (SyntaxException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}
	
	@Test
	public void expressionResultTest() throws ContentException, NullPointerException {
		final ExpressionResult	er1 = new ExpressionResult(100), er2 = new ExpressionResult(100), er3 = new ExpressionResult(200), er4 = new ExpressionResult("test"), er5 = new ExpressionResult(true);
		
		Assert.assertEquals(ValueType.INT, er1.type);
		Assert.assertEquals(100, er1.intValue);
		Assert.assertEquals(ValueType.STR, er4.type);
		Assert.assertEquals("test", er4.strValue);
		Assert.assertEquals(ValueType.BOOL, er5.type);
		Assert.assertTrue(er5.boolValue);
		
		Assert.assertEquals(er1, er2);
		Assert.assertEquals(er1.hashCode(), er2.hashCode());
		Assert.assertEquals(er1.toString(), er2.toString());
		
		Assert.assertTrue(er4.compareTo(new ExpressionResult("test1")) < 0);
		Assert.assertTrue(er1.compareTo(er2) == 0);
		Assert.assertTrue(er1.compareTo(er3) < 0);
		Assert.assertTrue(er5.compareTo(new ExpressionResult(false)) > 0);
		
		Assert.assertEquals(ValueType.BOOL, ExpressionResult.convertTo(new ExpressionResult("true"), ValueType.BOOL).type);
		Assert.assertEquals(ValueType.INT, ExpressionResult.convertTo(new ExpressionResult("100"), ValueType.INT).type);
		Assert.assertEquals(ValueType.STR, ExpressionResult.convertTo(new ExpressionResult(100), ValueType.STR).type);
		
		try{ExpressionResult.convertTo(null, ValueType.BOOL);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{ExpressionResult.convertTo(new ExpressionResult("true"), null);
			Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
		} catch (NullPointerException exc) {
		}
	}

	@Test
	public void varExtractionTest() throws ContentException, NullPointerException, IOException {
		final UUID					commitId1 = UUID.randomUUID(), commitId2 = UUID.randomUUID(); 
		final long					timestamp1 = 100, timestamp2 = 200;
		final String				comment1 = "comment 1", comment2 = "comment 2";
		final String				author1 = "author 1", author2 = "author 2";
		final CommitDescriptor[]	ref = new CommitDescriptor[1];
		final String				path = "/test/test.txt";
		final long					version = 2;
		final RepoItemDescriptor	item= new RepoItemDescriptor() {
										@Override public UUID getCommitId() {return ref[0].getCommitId();}
										@Override public CommitDescriptor getCommit() {return ref[0];}
										@Override public Date getTimestamp() {return new Date(timestamp2);}
										@Override public String getPath() {return path;}
										@Override public long getVersion() {return version;}
										@Override public String getAuthor() {return ref[0].getAuthor();}
										@Override public String getComment() {return ref[0].getComment();}
										@Override public InputStream getContent() throws IOException {return null;}
										@Override public ChangesDescriptor[] getChanges() throws IOException {return null;}
										@Override public RepoItemDescriptor getPrevious() throws IOException {return null;}
										@Override public RepoItemDescriptor getNext() throws IOException {return null;}
									}; 
		final CommitDescriptor	descPrev = new CommitDescriptor() {
										@Override public Date getTimestamp() {return new Date(timestamp1);}
										@Override public CommitDescriptor getPrevious() throws IOException {return null;}
										@Override public CommitDescriptor getNext() throws IOException {return null;}
										@Override public UUID getCommitId() {return commitId1;}
										@Override public RepoItemDescriptor[] getCommitContent() {return null;}
										@Override public String getComment() {return comment1;}
										@Override public String getAuthor() {return author1;}
								};
		final CommitDescriptor	desc = new CommitDescriptor() {
									@Override public Date getTimestamp() {return new Date(timestamp2);}
									@Override public CommitDescriptor getPrevious() throws IOException {return descPrev;}
									@Override public CommitDescriptor getNext() throws IOException {return null;}
									@Override public UUID getCommitId() {return commitId2;}
									@Override public RepoItemDescriptor[] getCommitContent() {return new RepoItemDescriptor[] {item};}
									@Override public String getComment() {return comment2;}
									@Override public String getAuthor() {return author2;}
							};
		ref[0] = desc;
		
		ExpressionResult	result = SimpleLightRepoQuery.extractVarValue(desc, buildNode("commit.author", Priority.TERM, -1));
		
		Assert.assertEquals(ValueType.STR, result.type);
		Assert.assertEquals(author2,result.strValue);

		result = SimpleLightRepoQuery.extractVarValue(desc, buildNode("commit.id", Priority.TERM, -1));
		
		Assert.assertEquals(ValueType.STR, result.type);
		Assert.assertEquals(commitId2.toString(),result.strValue);

		result = SimpleLightRepoQuery.extractVarValue(desc, buildNode("commit.comment", Priority.TERM, -1));
		
		Assert.assertEquals(ValueType.STR, result.type);
		Assert.assertEquals(comment2,result.strValue);

		result = SimpleLightRepoQuery.extractVarValue(desc, buildNode("commit.timestamp", Priority.TERM, -1));
		
		Assert.assertEquals(ValueType.INT, result.type);
		Assert.assertEquals(timestamp2,result.intValue);
	}
	
	@Test
	public void commitCalculationTest() throws ContentException, NullPointerException, IOException {
		final UUID				commitId1 = UUID.randomUUID(), commitId2 = UUID.randomUUID(); 
		final long				timestamp1 = 100, timestamp2 = 200;
		final String			comment1 = "comment 1", comment2 = "comment 2";
		final String			author1 = "author 1", author2 = "author 2";
		final CommitDescriptor	descPrev = new CommitDescriptor() {
										@Override public Date getTimestamp() {return new Date(timestamp1);}
										@Override public CommitDescriptor getPrevious() throws IOException {return null;}
										@Override public CommitDescriptor getNext() throws IOException {return null;}
										@Override public UUID getCommitId() {return commitId1;}
										@Override public RepoItemDescriptor[] getCommitContent() {return null;}
										@Override public String getComment() {return comment1;}
										@Override public String getAuthor() {return author1;}
								};
		final CommitDescriptor	desc = new CommitDescriptor() {
									@Override public Date getTimestamp() {return new Date(timestamp2);}
									@Override public CommitDescriptor getPrevious() throws IOException {return descPrev;}
									@Override public CommitDescriptor getNext() throws IOException {return null;}
									@Override public UUID getCommitId() {return commitId2;}
									@Override public RepoItemDescriptor[] getCommitContent() {return null;}
									@Override public String getComment() {return comment2;}
									@Override public String getAuthor() {return author2;}
							};
		ExpressionResult	result = SimpleLightRepoQuery.testCommit(desc, buildNode("commit.author ~ \".*2\" and commit.prev.author.uppercase == \"AUTHOR 1\"", Priority.OR, -1));
		
		Assert.assertEquals(ValueType.BOOL, result.type);
		Assert.assertTrue(result.boolValue);
	}

	@Test
	public void repoItemCalculationTest() throws ContentException, NullPointerException, IOException {
		final UUID					commitId = UUID.randomUUID(); 
		final long					timestamp = 100;
		final String				comment = "comment";
		final String				author = "author";
		final String				pathPrev = "/test/testPrev.txt", path = "/test/test.txt";
		final long					versionPrev = 1, version = 2;
		final RepoItemDescriptor	itemPrev = new RepoItemDescriptor() {
										@Override public UUID getCommitId() {return commitId;}
										@Override public CommitDescriptor getCommit() {return null;}
										@Override public Date getTimestamp() {return new Date(timestamp);}
										@Override public String getPath() {return pathPrev;}
										@Override public long getVersion() {return versionPrev;}
										@Override public String getAuthor() {return author;}
										@Override public String getComment() {return comment;}
										@Override public InputStream getContent() throws IOException {return null;}
										@Override public ChangesDescriptor[] getChanges() throws IOException {return null;}
										@Override public RepoItemDescriptor getPrevious() throws IOException {return null;}
										@Override public RepoItemDescriptor getNext() throws IOException {return null;}
									}; 
		final RepoItemDescriptor	item = new RepoItemDescriptor() {
										@Override public UUID getCommitId() {return commitId;}
										@Override public CommitDescriptor getCommit() {return null;}
										@Override public Date getTimestamp() {return new Date(timestamp);}
										@Override public String getPath() {return path;}
										@Override public long getVersion() {return version;}
										@Override public String getAuthor() {return author;}
										@Override public String getComment() {return comment;}
										@Override public InputStream getContent() throws IOException {return null;}
										@Override public ChangesDescriptor[] getChanges() throws IOException {return null;}
										@Override public RepoItemDescriptor getPrevious() throws IOException {return itemPrev;}
										@Override public RepoItemDescriptor getNext() throws IOException {return null;}
									}; 
		final CommitDescriptor		desc = new CommitDescriptor() {
										@Override public Date getTimestamp() {return new Date(timestamp);}
										@Override public CommitDescriptor getPrevious() throws IOException {return null;}
										@Override public CommitDescriptor getNext() throws IOException {return null;}
										@Override public UUID getCommitId() {return commitId;}
										@Override public RepoItemDescriptor[] getCommitContent() {return new RepoItemDescriptor[] {item};}
										@Override public String getComment() {return comment;}
										@Override public String getAuthor() {return author;}
									};
		ExpressionResult			result = SimpleLightRepoQuery.testRepoItem(item, buildNode("file.path ~ \"/test/.*\" and file.prev.path.uppercase == \"/TEST/TESTPREV.TXT\"", Priority.OR, -1));
		
		Assert.assertEquals(ValueType.BOOL, result.type);
		Assert.assertTrue(result.boolValue);
	}
	
	private static SyntaxNode<NodeType, SyntaxNode> buildNode(final String content, final Priority prty, final int awaitedLex) throws SyntaxException {
		final Lexema[]	lex = SimpleLightRepoQuery.parseQuery((content+SimpleLightRepoQuery.END_OF_QUERY).toCharArray(), 0);
		final SyntaxNode<NodeType, SyntaxNode>	root = new SyntaxNode<NodeType, SyntaxNode>(0, 0, NodeType.OR, 0, null);
		final int 		stop = SimpleLightRepoQuery.translateQuery(prty, lex, 0, new Hashtable<String, Object>(), root);

		if (awaitedLex >= 0) {
			Assert.assertEquals(awaitedLex, stop);
		}
		return root;
	}
}
