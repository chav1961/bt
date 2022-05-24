package chav1961.bt.mnemort.entities;

import java.util.UUID;

import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class BasicContainer<Canvas extends DrawingCanvas> extends BasicEntity<Canvas> {
	@FunctionalInterface
	public static interface WalkerCallback<Canvas extends DrawingCanvas> {
		void process(final BasicEntity<Canvas> entity) throws ContentException;
	}
	
	protected BasicContainer(final ContentNodeMetadata meta, final UUID entityId) {
		super(meta, entityId);
		// TODO Auto-generated constructor stub
	}

	public BasicContainer<Canvas> addEntities(final BasicEntity<Canvas>... entities) {
		return this;
	}
	
	public BasicContainer<Canvas> removeEntities(final UUID... entities) {
		return this;
	}
	
	public void forEach(WalkerCallback<Canvas> callback) {
		
	}
}
