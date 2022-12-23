package chav1961.bt.security.keystore;

import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;

public abstract class KeyStoreItem implements NodeMetadataOwner {
	protected static final String	APP_SCHEME = "security";
	
	@Override
	public abstract ContentNodeMetadata getNodeMetadata();
}
