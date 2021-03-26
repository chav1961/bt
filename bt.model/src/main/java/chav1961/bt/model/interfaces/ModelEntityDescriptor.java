package chav1961.bt.model.interfaces;

import java.util.UUID;

import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public interface ModelEntityDescriptor<Id> {
	ContentNodeMetadata getNodeModel();
	ContentMetadataInterface getNodeOwnerModel();
	UUID getModelEntityId();
}
