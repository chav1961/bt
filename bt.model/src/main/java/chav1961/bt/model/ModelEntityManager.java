package chav1961.bt.model;

import java.util.UUID;

import chav1961.bt.model.interfaces.ORMModelMapper;
import chav1961.bt.model.interfaces.UIModelMapper;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class ModelEntityManager {
	public UUID registerModelEntity(final Class<?> modelClass, final ContentNodeMetadata metadata) {
		return null;
	}

	public void unregisterModelEntity(UUID modelEntityId) {
	}
	
	public ORMModelMapper getORMMapperFor(UUID modelEntityId) {
		return null;
	}

	public UIModelMapper getUIMapperFor(UUID modelEntityId) {
		return null;
	}
	
	public <T> T getSharedEntity(UUID entityId) {
		return null;
	}
	
	public <T> void freeSharedEntity(T entity) {
		
	}
}
