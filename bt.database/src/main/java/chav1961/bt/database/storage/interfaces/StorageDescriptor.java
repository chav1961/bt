package chav1961.bt.database.storage.interfaces;

import java.io.IOException;
import java.net.URI;

public interface StorageDescriptor extends Iterable<Storage> {
	int getStorageCount();
	int appendStorage(final URI storage) throws IOException;
}
