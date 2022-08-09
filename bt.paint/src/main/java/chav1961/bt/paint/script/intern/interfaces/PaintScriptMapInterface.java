package chav1961.bt.paint.script.intern.interfaces;

public interface PaintScriptMapInterface extends PaintScriptCollectionInterface {
	Object get(char[] index);
	void set(char[] index, Object value);
	void insert(char[] index);
	void remove(char[] index);
	boolean contains(char[] index);
	void length();
}
