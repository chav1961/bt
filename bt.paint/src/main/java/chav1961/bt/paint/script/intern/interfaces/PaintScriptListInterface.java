package chav1961.bt.paint.script.intern.interfaces;

public interface PaintScriptListInterface extends PaintScriptCollectionInterface {
	Object get(int index);
	void set(int index, Object value);
	void append();
	void append(int count);
	void insert(int index, int count);
	void remove(int index);
	void remove(int index, int count);
	void length();
}
