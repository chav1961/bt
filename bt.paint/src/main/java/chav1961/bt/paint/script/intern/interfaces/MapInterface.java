package chav1961.bt.paint.script.intern.interfaces;

public interface MapInterface<T> {
	int length();
	T get(String index);
	boolean exists(String index);
	void set(String index, T value);
	void remove(String index);
	String[] indices();
}
