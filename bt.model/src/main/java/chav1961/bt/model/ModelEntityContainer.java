package chav1961.bt.model;

import java.util.Iterator;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class ModelEntityContainer<Attr extends Enum<?>,T extends ModelEntity<Attr>> extends ModelEntity<Attr> implements Iterable<T> {
	private static final long serialVersionUID = 1L;

	@FunctionalInterface
	public interface ModelEntityContainerWalkingCallback<Attr extends Enum<?>> {
		ContinueMode process(NodeEnterMode mode, ModelEntity<Attr> node) throws ContentException;
	}
	
	public ModelEntityContainer<Attr,T> addChild(T item) {
		return this;		
	}

	public int getChildrenCount() {
		return 0;
	}
	
	public T getChild(int index) {
		return null;
	}
	
	public ModelEntityContainer<Attr,T> removeChild(T item) {
		return this;
	}
	
	@Override
	public Iterator<T> iterator() {
		return null;
	}
	
	public static <Attr extends Enum<?>,T extends ModelEntity<Attr>> ContinueMode walkDown(ModelEntityContainer<Attr,T> root, ModelEntityContainerWalkingCallback<Attr> callback) throws ContentException {
		return null;
	}
}
