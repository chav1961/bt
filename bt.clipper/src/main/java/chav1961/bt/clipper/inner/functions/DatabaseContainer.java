package chav1961.bt.clipper.inner.functions;

import java.sql.Connection;

import chav1961.bt.clipper.inner.interfaces.ClipperSyntaxEntity;
import chav1961.bt.clipper.inner.interfaces.FunctionsContainer;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class DatabaseContainer implements FunctionsContainer {
	private final Connection	conn;
	
	public DatabaseContainer(final Connection conn) {
		if (conn == null) {
			throw new NullPointerException("Connection can't be null"); 
		}
		else {
			this.conn = conn;
		}
	}
	
	@Override
	public void prepare(SyntaxTreeInterface<ClipperSyntaxEntity> tree) {
		// TODO Auto-generated method stub
		
	}
}
