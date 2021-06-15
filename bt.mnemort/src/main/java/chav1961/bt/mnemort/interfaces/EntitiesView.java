package chav1961.bt.mnemort.interfaces;

import chav1961.bt.mnemort.entities.interfaces.EntityInterface;

public interface EntitiesView<Canvas,T> {
	void draw(Canvas canvas, int width, int height, int windowWidth, int windowHeight, @SuppressWarnings("unchecked") EntityInterface<T>... entity);
}
