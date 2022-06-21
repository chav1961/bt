package chav1961.bt.database.storage.interfaces;

import java.io.IOException;

import chav1961.purelib.basic.SubstitutableProperties;

public interface StoragePool {
	int getPoolId();
	long getPoolInitialSize() throws IOException;
	long getPoolIncrement() throws IOException;
	int getPoolMaxExtents() throws IOException;
	long getPoolCurrentSize() throws IOException;
	int getPoolCurrentExtents() throws IOException;
	SubstitutableProperties getPoolProperties();
	long getFreeSize() throws IOException;
	int getPageSize();
	long getPageCount();	
	long allocate(long pageAmount) throws IOException;
	void free(long displ, long pageAmount) throws IOException;
	boolean expandPool() throws IOException;
	boolean shrinkPool() throws IOException;
}
