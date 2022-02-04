package chav1961.bt.lucenewrapper;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.zip.CRC32;
import java.util.Set;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.Lock;

import chav1961.bt.lucenewrapper.LuceneFileSystemWrapperDirectory.InternalIndexInput;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.interfaces.SpiService;
import chav1961.purelib.streams.byte2byte.PseudoRandomInputStream;

public class LuceneDatabaseWrapperDirectory extends Directory implements Closeable, SpiService<Directory> {
	private static final URI			SUPPORTED_URI = URI.create(LuceneSearchRepository.LUCENE_DIR_SCHEME+":jdbc:/"); 
	private static final long			ROOT_ID = -1L;
	private static final String			COL_ID = "id";
	private static final String			COL_PARENT = "parent";
	private static final String			COL_NAME = "name";
	private static final String			COL_SIZE = "size";
	private static final String			COL_ATTR = "attr";
	private static final String			COL_CONTENT = "content";
	private static final Set<String>	COLUMNS = Set.of(COL_ID, COL_PARENT, COL_NAME, COL_SIZE, COL_ATTR, COL_CONTENT);
	
	private static final String			CREATE_TABLE = "create table if not exists \"%1\" (" + COL_ID + " bigint primary key,"
													+ COL_PARENT + " bigint referenced to \"%1\" (" + COL_ID + "),"
													+ COL_NAME + " nvarchar(250) not null,"
													+ COL_SIZE + " bigint default 0,"
													+ COL_ATTR + " nvarchar(250),"
													+ COL_CONTENT + " blob)";
	private static final String			CREATE_SEQUENCE = "create sequence if not exists \"%1_seq\"";
	private static final String			INSERT_ROOT = "insert into \"%1\" (" + COL_ID + ", " + COL_NAME + ") " 
													+ "values (" + ROOT_ID + ", 'root')";
	private static final String			DROP_TABLE = "drop table \"%1\"";
	private static final String			DROP_SEQUENCE = "drop sequence \"%1_seq\"";

	private static enum SQLS {
		SQL_LIST_ALL("select * from \"%1\" where " + COL_PARENT + " = " + ROOT_ID),
		SQL_FILE_EXISTS("select count(*) from from \"%1\" where " + COL_NAME + " = ?", Types.NVARCHAR),
		SQL_NEW_FILE("insert into \"%1\" (" + COL_ID + ", " + COL_PARENT + ", " + COL_NAME + ", " + COL_SIZE + ", " + COL_CONTENT + ") " 
					+ " values (nextval('%1_seq'), -1, ?, ?, ?)", Types.NVARCHAR, Types.BIGINT, Types.BLOB),
		SQL_DELETE_FILE("delete from \"%1\" where " + COL_NAME + " = ?", Types.NVARCHAR),
		SQL_GET_CONTENT("select " + COL_CONTENT + " from \"%1\" where " + COL_NAME + " = ?", Types.BLOB, Types.NVARCHAR),
		SQL_STORE_CONTENT("update \"%1\" set " + COL_SIZE + " = ?, " + COL_CONTENT + " = ? where " + COL_NAME + " = ?", Types.BIGINT, Types.BLOB, Types.NVARCHAR),
		SQL_RENAME_FILE("update \"%1\" set " + COL_NAME + " = ? where " + COL_NAME + " = ?", Types.NVARCHAR, Types.NVARCHAR),
		SQL_LOCK_FILE("select 1 from \"%1\" where " + COL_NAME + " = ? for update", Types.NVARCHAR);
		
		private final String	sql;
		private final int[]		types;
		
		private SQLS(final String sql, final int... types) {
			this.sql = sql;
			this.types = types;
		}
		
		public String getSql() {
			return sql;
		}
		
		public int[] getTypes() {
			return types;
		}
	}

	@FunctionalInterface
	private interface ResultSetCallback {
		void process(ResultSet rs) throws SQLException;
	}
	
	private final Connection	conn;
	private final String		tableName;
	private final EnumMap<SQLS, PreparedStatement>	stmts = new EnumMap<>(SQLS.class);

	public LuceneDatabaseWrapperDirectory() throws IOException {
		this.conn = null;
		this.tableName = null;
	}	
	
	@Override
	public boolean canServe(final URI resource) throws NullPointerException {
		if (resource == null) {
			throw new NullPointerException("Resource URI can't be null");
		}
		else {
			return URIUtils.canServeURI(resource, SUPPORTED_URI);
		}
	}

	@Override
	public Directory newInstance(final URI resource) throws EnvironmentException, NullPointerException, IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public LuceneDatabaseWrapperDirectory(final Connection conn, final String tableName) throws IOException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else if (tableName == null || tableName.isEmpty()) {
			throw new IllegalArgumentException("Tabe name can't be null or empty"); 
		}
		else {
			try {
				if (!isStructurePrepared(conn, tableName)) {
					throw new IllegalArgumentException("Database is not prepared for using this class. Call prepareStructure(...) method before"); 
				}
				else {
					this.conn = conn;
					this.tableName = tableName;
				}
			} catch (SQLException e) {
				throw new IOException(e);
			}
		}
	}
	
	@Override
	public String[] listAll() throws IOException {
		final Set<String>	result = new HashSet<>();

		forEach((rs)->result.add(rs.getString(COL_NAME)), SQLS.SQL_LIST_ALL);
		return result.toArray(new String[result.size()]);
	}

	@Override
	public void deleteFile(final String name) throws IOException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else if (!exists(SQLS.SQL_FILE_EXISTS, name)) {
			throw new FileNotFoundException(name); 
		}
		else {
			update(SQLS.SQL_DELETE_FILE, name);
		}
	}

	@Override
	public long fileLength(final String name) throws IOException {
		if (!exists(SQLS.SQL_FILE_EXISTS, name)) {
			throw new FileNotFoundException(name); 
		}
		else {
			final long[]	result = new long[1];
			
			forEach((rs)->result[0] = rs.getLong(COL_SIZE), SQLS.SQL_LIST_ALL);
			return result[0];
		}
	}

	@Override
	public IndexOutput createOutput(final String name, final IOContext context) throws IOException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else if (context == null) {
			throw new NullPointerException("Context can't be null"); 
		}
		else {
			if (!exists(SQLS.SQL_FILE_EXISTS, name)) {
				update(SQLS.SQL_NEW_FILE, name, 0L, new InputStream() {@Override public int read() throws IOException {return -1;}});
			}
			
			final File			temp = File.createTempFile("lucenewrapper", ".tmp");
			final OutputStream	os = new FileOutputStream(temp);
			
			return new IndexOutput(name, name) {
				final CRC32 crc = new CRC32(); 
				long		len = 0;
				
				@Override
				public void writeBytes(byte[] b, int offset, int length) throws IOException {
					crc.update(b,offset,length);
					len += length;
					os.write(b, offset, length);
				}
				
				@Override
				public void writeByte(byte b) throws IOException {
					len++;
					crc.update(b);
					os.write(b);
				}
				
				@Override
				public long getFilePointer() {
					return len;
				}
				
				@Override
				public long getChecksum() throws IOException {
					return crc.getValue();
				}
				
				@Override
				public void close() throws IOException {
					os.flush();
					os.close();
					try(final InputStream	is = new FileInputStream(temp)) {
						update(SQLS.SQL_NEW_FILE, name, temp.length(), is);
					} 
					finally {
						temp.delete();
					}
				}
			};
		}
	}

	@Override
	public IndexOutput createTempOutput(final String prefix, final String suffix, final IOContext context) throws IOException {
		if (prefix == null || prefix.isEmpty()) {
			throw new IllegalArgumentException("File prefix can't be null or empty"); 
		}
		else if (suffix == null || suffix.isEmpty()) {
			throw new IllegalArgumentException("File suffix can't be null or empty"); 
		}
		else if (context == null) {
			throw new NullPointerException("Context can't be null"); 
		}
		else {
			final long	first = System.currentTimeMillis(), second = (long) (100000 * Math.random());

			return createOutput(prefix+first+second+suffix, context);
		}
	}

	@Override
	public void sync(final Collection<String> names) throws IOException {
	}

	@Override
	public void syncMetaData() throws IOException {
	}

	@Override
	public void rename(final String source, final String dest) throws IOException {
		if (source == null || source.isEmpty()) {
			throw new IllegalArgumentException("Source file name can't be null or empty"); 
		}
		else if (dest == null || dest.isEmpty()) {
			throw new IllegalArgumentException("Destination file name can't be null or empty"); 
		}
		else if (!exists(SQLS.SQL_FILE_EXISTS, source)) {
			throw new FileNotFoundException(source); 
		}
		else if (exists(SQLS.SQL_FILE_EXISTS, dest)) {
			throw new IOException("Duplicate name ["+dest+"]"); 
		}
		else {
			update(SQLS.SQL_RENAME_FILE, dest, source);
		}
	}

	@Override
	public IndexInput openInput(final String name, final IOContext context) throws IOException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else if (context == null) {
			throw new NullPointerException("Context can't be null"); 
		}
		else {
			final InputStream[]				is = new InputStream[1];
			
			forEach((rs)->is[0] = rs.getBinaryStream(COL_CONTENT), SQLS.SQL_GET_CONTENT, name);
			final PseudoRandomInputStream	pris = new PseudoRandomInputStream(is[0]);
			
			return new InternalIndexInput(name, pris) {
				@Override
				public void close() throws IOException {
					pris.close();
					is[0].close();
				}
			}; 
		}
	}

	@Override
	public Lock obtainLock(final String name) throws IOException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else {
			update(SQLS.SQL_LOCK_FILE, name);
			
			return new Lock() {
				@Override
				public void ensureValid() throws IOException {
				}
				
				@Override
				public void close() throws IOException {
					try {
						getConnection().commit();
					} catch (SQLException e) {
						throw new IOException(e);
					}
				}
			};
		}
	}

	@Override
	public void close() throws IOException {
		for (Entry<SQLS, PreparedStatement> item : stmts.entrySet()) {
			try {
				item.getValue().close();
			} catch (SQLException exc) {
			}
		}
		try{
			getConnection().commit();
		} catch (SQLException e) {
			throw new IOException(e); 
		}
	}

	@Override
	public Set<String> getPendingDeletions() throws IOException {
		return Set.of();
	}

	protected Connection getConnection() {
		return conn;
	}
	
	protected String getTableName() {
		return tableName;
	}
	
	private synchronized boolean exists(final SQLS queryType, final Object... parameters) throws IOException {
		try(final ResultSet	rs = bindParameters(getStmt(queryType), queryType, parameters).executeQuery()) {
			
			return rs.next() && rs.getLong(1) > 0;
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}

	private synchronized void forEach(final ResultSetCallback callback, final SQLS queryType, final Object... parameters) throws IOException {
		try(final ResultSet	rs = bindParameters(getStmt(queryType), queryType, parameters).executeQuery()) {
			
			while (rs.next()) {
				callback.process(rs);
			}
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	private synchronized void update(final SQLS queryType, final Object... parameters) throws IOException {
		try {
			bindParameters(getStmt(queryType), queryType, parameters).executeUpdate();
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	private PreparedStatement getStmt(final SQLS queryType) throws SQLException {
		if (!stmts.containsKey(queryType)) {
			stmts.put(queryType, getConnection().prepareStatement(String.format(queryType.getSql(), getTableName())));
		}
		return stmts.get(queryType);
	}

	private PreparedStatement bindParameters(final PreparedStatement stmt, final SQLS queryType, final Object... parameters) throws SQLException {
		final int[]	types = queryType.getTypes();
		
		if (types.length != parameters.length) {
			throw new IllegalArgumentException("Number of types [" + types.length + "] differ with number of parameters [" + parameters.length + "]");
		}
		else {
			stmt.clearParameters();
			for (int index = 0; index < types.length; index++) {
				if (parameters[index] == null) {
					stmt.setNull(index + 1, types[index]);
				}
				else {
					switch (types[index]) {
						case Types.NVARCHAR :
							stmt.setString(index + 1, parameters[index].toString());
							break;
						case Types.BIGINT :
							stmt.setLong(index + 1, (Long)parameters[index]);
							break;
						case Types.BLOB :
							stmt.setBinaryStream(index + 1, (InputStream)parameters[index]);
							break;
						default :
							throw new UnsupportedOperationException("Parameter type [" + types[index] + "] is njot supported yet");
					}
				}
			}
			return stmt;
		}
	}
	
	public static void prepareStructure(final Connection conn, final String tableName) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else if (tableName == null || tableName.isEmpty()) {
			throw new IllegalArgumentException("Tabe name can't be null or empty"); 
		}
		else {
			try(final Statement	stmt = conn.createStatement()) {
				
				stmt.execute(String.format(CREATE_SEQUENCE, tableName));
				stmt.execute(String.format(CREATE_TABLE, tableName));
				stmt.execute(String.format(INSERT_ROOT, tableName));
			}
		}
	}

	public static boolean isStructurePrepared(final Connection conn, final String tableName) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else if (tableName == null || tableName.isEmpty()) {
			throw new IllegalArgumentException("Tabe name can't be null or empty"); 
		}
		else {
			final Set<String>	names = new HashSet<>();
			
			try(final ResultSet	rs = conn.getMetaData().getColumns(null, null, tableName, "%")) {
				while (rs.next()) {
					names.add(rs.getString("COLUMN_NAME"));
				}
			}
			if (!COLUMNS.equals(names)) {
				return false;
			}
			try(final ResultSet	rs = conn.getMetaData().getTables(null, null, tableName+"_seq", new String[] {"SEQUENCE"})) {
				return rs.next();
			}
		}
	}

	public static void unprepareStructure(final Connection conn, final String tableName) throws SQLException {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else if (tableName == null || tableName.isEmpty()) {
			throw new IllegalArgumentException("Tabe name can't be null or empty"); 
		}
		else {
			try(final Statement	stmt = conn.createStatement()) {
				
				stmt.execute(String.format(DROP_TABLE, tableName));
				stmt.execute(String.format(DROP_SEQUENCE, tableName));
			}
		}
	}

}
