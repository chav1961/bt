package chav1961.bt.paint.script.interfaces;

public interface PluginLibrary extends AutoCloseable {
	boolean canServe(String name);
	
	default boolean canServe(char[] name) {
		return canServe(new String(name));
	}
	
	Object getContent();
	
	@Override
	void close() throws RuntimeException;
}
