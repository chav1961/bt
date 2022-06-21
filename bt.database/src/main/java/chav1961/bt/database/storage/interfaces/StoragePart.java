package chav1961.bt.database.storage.interfaces;

import java.io.IOException;

import chav1961.purelib.basic.SubstitutableProperties;

public interface StoragePart extends Iterable<StoragePool> {
	int getPartId();
	long getPartInitialSize();
	long getPartIncrement();
	int getPartMaxExtents();
	long getPartCurrentSize();
	int getPartCurrentExtents();
	int getPoolCount();
	StoragePool getPool(int poolId) throws IOException;
	int createPool(long size, SubstitutableProperties poolProps) throws IOException;
	int createPool(long initialSize, long increment, int maxExtents, SubstitutableProperties poolProps) throws IOException;
	void dropPool(int poolId) throws IOException;
}
