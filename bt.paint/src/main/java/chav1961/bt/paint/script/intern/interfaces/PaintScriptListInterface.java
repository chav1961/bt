package chav1961.bt.paint.script.intern.interfaces;

import java.util.ArrayList;
import java.util.List;

public interface PaintScriptListInterface extends PaintScriptCollectionInterface {
	Object get(int index);
	void set(int index, Object value);
	void append();
	void append(int count);
	void insert(int index, int count);
	void remove(int index);
	void remove(int index, int count);
	int length();
	
	public static class Factory {
		public static PaintScriptListInterface newInstance(final Class<Object> collectionItemType) {
			return new PaintScriptListInterface() {
				final List<Object>	delegate = new ArrayList<>();
				
				@Override
				public Class<Object> getCollectionItemType() {
					return collectionItemType;
				}

				@Override
				public Object get(int index) {
					return delegate.get(index);
				}

				@Override
				public void set(int index, Object value) {
					delegate.set(index, value);
				}

				@Override
				public void append() {
					delegate.add(null);
				}

				@Override
				public void append(int count) {
					for (int index = 0; index < count; index++) {
						append();
					}
				}

				@Override
				public void insert(int index, int count) {
					// TODO Auto-generated method stub
					for (int i = 0; i < count; i++) {
						delegate.add(index, null);
					}
				}

				@Override
				public void remove(int index) {
					// TODO Auto-generated method stub
					delegate.remove(index);
				}

				@Override
				public void remove(int index, int count) {
					// TODO Auto-generated method stub
					for (int i = 0; i < count; i++) {
						delegate.remove(index);
					}
				}

				@Override
				public int length() {
					return delegate.size();
				}
			};
		}
	}
}
