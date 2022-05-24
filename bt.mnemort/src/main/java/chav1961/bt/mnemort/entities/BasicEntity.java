package chav1961.bt.mnemort.entities;

import java.util.UUID;

import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.bt.mnemort.interfaces.ItemView;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;

public class BasicEntity<Canvas extends DrawingCanvas> implements ItemView<Canvas>, NodeMetadataOwner {
	private final ContentNodeMetadata	meta;
	private final UUID					entityId;
	private float						x = 0, y = 0, width = 1.0f, height = 1.0f;
	
	protected BasicEntity(final ContentNodeMetadata meta, final UUID entityId) {
		this.meta = meta;
		this.entityId = entityId;
	}

	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return meta;
	}
	
	public UUID getEntityId() {
		return entityId;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}

	@Override
	public void draw(final Canvas canvas) {
		draw(canvas, getX(), getY(), getWidth(), getHeight());
	}
	
	@Override
	public void draw(final Canvas canvas, final float width, final float height) {
		draw(canvas, getX(), getY(), width, height);
	}
	
	@Override
	public void draw(final Canvas canvas, final float x, final float y, final float width, final float height) {
		// TODO Auto-generated method stub
	}
}
