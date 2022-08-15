package chav1961.bt.paint.script.intern.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface PaintScriptMapInterface extends PaintScriptCollectionInterface {
	Object get(char[] index);
	void set(char[] index, Object value);
	void insert(char[] index);
	void remove(char[] index);
	boolean contains(char[] index);
	int length();
	
	public static class Factory {
		public static PaintScriptMapInterface newInstance(final Class<Object> collectionItemType) {
			return new PaintScriptMapInterface() {
				final Map<String, Object>	delegate = new HashMap<>();

				@Override
				public Class<?> getCollectionItemType() {
					return collectionItemType;
				}

				@Override
				public Object get(char[] index) {
					return delegate.get(new String(index));
				}

				@Override
				public void set(char[] index, Object value) {
					delegate.put(new String(index), value);
				}

				@Override
				public void insert(char[] index) {
					delegate.put(new String(index), null);
				}

				@Override
				public void remove(char[] index) {
					delegate.remove(new String(index));
				}

				@Override
				public boolean contains(char[] index) {
					return delegate.containsKey(new String(index));
				}

				@Override
				public int length() {
					return delegate.size();
				}
			};
		}
	}
}
