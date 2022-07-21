package chav1961.bt.paint.script.intern.interfaces;

public interface ArrayInterface<T> {
	int length();
	T get(int index);
	void set(int index, T value);
	void add(int index, T value);
	void append(T value);
	void remove(int index);
}
