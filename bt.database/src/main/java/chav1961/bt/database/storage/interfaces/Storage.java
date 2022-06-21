package chav1961.bt.database.storage.interfaces;

import java.io.IOException;

public interface Storage extends Iterable<StoragePart> {
	int getStorageId();
	int getPartCount();
	StoragePart getPart(int partId) throws IOException;
	int createPart(long size) throws IOException;
	int createPart(long initialSize, long increment, int maxExtends) throws IOException;
	int dropPart(int partId) throws IOException;
}
