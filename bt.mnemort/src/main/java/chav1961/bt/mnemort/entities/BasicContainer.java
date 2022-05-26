package chav1961.bt.mnemort.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper;
import chav1961.purelib.concurrent.LightWeightRWLockerWrapper.Locker;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public abstract class BasicContainer<Canvas extends DrawingCanvas> extends BasicEntity<Canvas> {
	private final LightWeightRWLockerWrapper		lock = new LightWeightRWLockerWrapper();
	private final Map<UUID, BasicEntity<Canvas>>	collection = new HashMap<>();
	
	@FunctionalInterface
	public static interface WalkerCallback<Canvas extends DrawingCanvas> {
		void process(final BasicEntity<Canvas> entity) throws ContentException;
	}
	
	protected BasicContainer(final ContentNodeMetadata meta, final UUID entityId) {
		super(meta, entityId);
	}

	protected abstract void drawBackground(final Canvas canvas, final float width, final float height);
	
	@Override
	public void draw(final Canvas canvas, final float width, final float height) {
		drawBackground(canvas, width, height);
		forEach((e)->drawChild(canvas, e));
	}

	public BasicContainer<Canvas> addEntities(final BasicEntity<Canvas>... entities) {
		if (entities == null || Utils.checkArrayContent4Nulls(entities) >= 0) {
			throw new IllegalArgumentException("Entities list is null or contains nulls inside");
		}
		else {
			try (final Locker	l = lock.lock(false)) {
				for (BasicEntity<Canvas> item : entities) {
					if (collection.containsKey(item.getEntityId())) {
						throw new IllegalArgumentException("Entity id ["+item.getEntityId()+"] is already presents in the collection");
					}
					else {
						collection.put(item.getEntityId(), item);
					}
				}
			}
			return this;
		}
	}
	
	public BasicContainer<Canvas> removeEntities(final UUID... entities) {
		if (entities == null || Utils.checkArrayContent4Nulls(entities) >= 0) {
			throw new IllegalArgumentException("Entities list is null or contains nulls inside");
		}
		else {
			try (final Locker	l = lock.lock(false)) {
				for (UUID item : entities) {
					if (collection.containsKey(item)) {
						throw new IllegalArgumentException("Entity id ["+item+"] is missing in the collection");
					}
					else {
						collection.remove(item);
					}
				}
			}
			return this;
		}
	}
	
	public void forEach(WalkerCallback<Canvas> callback) {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			try (final Locker	l = lock.lock()) {
				for (Entry<UUID, BasicEntity<Canvas>> item : collection.entrySet()) {
					try{
						callback.process(item.getValue());
					} catch (ContentException e) {
					}
				}
			}			
		}
	}

	private void drawChild(final Canvas canvas, final BasicEntity<Canvas> child) {
		try(final Canvas	local = (Canvas) canvas.push(child.getLocation().toAffineTransform())) {
			
			child.draw(local);
		}
	}
}
