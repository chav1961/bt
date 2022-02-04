package chav1961.bt.lucenewrapper;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.exceptions.EnvironmentException;

public class LucenePostgreSQLWrapperDirectory extends LuceneDatabaseWrapperDirectory {
	private static final URI			SUPPORTED_URI = URI.create(LuceneSearchRepository.LUCENE_DIR_SCHEME+":postgres:/");
	
	public LucenePostgreSQLWrapperDirectory() throws IOException {
	}

	public LucenePostgreSQLWrapperDirectory(final Connection conn, final String tableName) throws IOException {
		super(conn, tableName);
		try{getConnection().setAutoCommit(false);
		} catch (SQLException e) {
			throw new IOException();
		}
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

	@Override
	public IndexInput openInput(final String name, final IOContext context) throws IOException {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else if (context == null) {
			throw new NullPointerException("Context can't be null"); 
		}
		else {
//			final InputStream[]				is = new InputStream[1];
//			
//			forEach((rs)->is[0] = rs.getBinaryStream(COL_CONTENT), SQLS.SQL_GET_CONTENT, name);
//			final PseudoRandomInputStream	pris = new PseudoRandomInputStream(is[0]);
			
			return new PostgreSQLInternalIndexInput(name) {
				@Override
				public void close() throws IOException {
//					pris.close();
//					is[0].close();
				}
			}; 
		}
	}

	@Override
	public void close() throws IOException {
		super.close();
	}

	
	abstract static class PostgreSQLInternalIndexInput extends IndexInput {
		PostgreSQLInternalIndexInput(final String resourceDescription) {
			super(resourceDescription);
		}

//		@Override
//		public void readBytes(byte[] buf, int offset, int length) throws IOException {
//			content.read(buf, offset, length);
//		}
//		
//		@Override
//		public byte readByte() throws IOException {
//			readBytes(buffer,0,buffer.length);
//			return buffer[0];
//		}
//		
//		@Override
//		public IndexInput slice(final String sliceDescription, long offset, long length) throws IOException {
//			return new InternalIndexInput(sliceDescription, new PseudoRandomInputStream(content, offset, length)) {
//				@Override
//				public void close() throws IOException {
//				}
//			};
//		}
//		
//		@Override
//		public void seek(long pos) throws IOException {
//			content.seek(pos);
//		}
//		
//		@Override
//		public long length() {
//			try{return content.length();
//			} catch (IOException e) {
//				return -1;
//			}
//		}
//		
//		@Override
//		public long getFilePointer() {
//			try{return content.getFilePointer();
//			} catch (IOException e) {
//				return -1;
//			}
//		}
		
		@Override public abstract void close() throws IOException;

		@Override
		public long getFilePointer() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public void seek(long pos) throws IOException {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public long length() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public byte readByte() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public void readBytes(byte[] arg0, int arg1, int arg2) throws IOException {
			// TODO Auto-generated method stub
			
		}
	}	
	
	
//	// All LargeObject API calls must be within a transaction
//	con.setAutoCommit(false);
//
//	// Get the Large Object Manager to perform operations with
//	LargeObjectManager lobj =
//	   ((org.postgresql.Connection)con).getLargeObjectAPI();
//
//	// create a new large object
//	int oid = lobj.create(LargeObjectManager.READ | LargeObjectManager.WRITE);
//
//	// open the large object for write
//	LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE);
//
//	// Now open the file
//	File file = new File("myimage.gif");
//	FileInputStream fis = new FileInputStream(file);
//
//	// copy the data from the file to the large object
//	byte buf[] = new byte[2048];
//	int s, tl = 0;
//	while ((s = fis.read(buf, 0, 2048)) > 0)
//	{
//	   obj.write(buf, 0, s);
//	   tl += s;
//	}
//
//	// Close the large object
//	obj.close();
//
//	// Now insert the row into imagesLO
//	PreparedStatement pstmt =
//	   con.prepareStatement("INSERT INTO images VALUES (?, ?)");
//	pstmt.setString(1, file.getName());
//	pstmt.setInt(2, oid);
//	pstmt.executeUpdate();
//	pstmt.close();
//	fis.close();
//
//
//
//	con.setAutoCommit(false);
//
//	// Get the Large Object Manager to perform operations with
//	LargeObjectManager lobj =
//	   ((org.postgresql.Connection)con).getLargeObjectAPI();
//
//	PreparedStatement pstmt =
//	   con.prepareStatement("SELECT img FROM images WHERE imgname=?");
//	pstmt.setString(1, "myimage.gif");
//	ResultSet rs = pstmt.executeQuery();
//
//	if (rs != null) {
//	   while(rs.next()) {
//	      // open the large object for reading
//	      int oid = rs.getInt(1);
//	      LargeObject obj = lobj.open(oid, LargeObjectManager.READ);
//
//	      // read the data
//	      byte buf[] = new byte[obj.size()];
//	      obj.read(buf, 0, obj.size());
//
//	      // do something with the data read here
//
//	      // Close the object
//	      obj.close();
//	   }
//	   rs.close();
//	}
//	pstmt.close();
//	
//	
//	
}
