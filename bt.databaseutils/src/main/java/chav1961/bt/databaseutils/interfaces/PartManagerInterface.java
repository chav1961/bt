package chav1961.bt.databaseutils.interfaces;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;

@FunctionalInterface
public interface PartManagerInterface extends Closeable {
	OutputStream getStream(final String name) throws IOException;
	
	@Override
	default void close() throws IOException {
	}
}
