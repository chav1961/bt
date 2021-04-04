package chav1961.bt.model.impl;

import java.io.IOException;
import java.util.Map;

import chav1961.bt.model.interfaces.UIModelMapper;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.interfaces.JComponentInterface;

/**
 * 
 * @param <Data> entity model content to download and upload
 */

public class HttpUIModelMapper<Data> implements UIModelMapper<Data, String, StringBuilder> {
	public HttpUIModelMapper(final ContentNodeMetadata metadata, final String screenTemplate) {
		if (metadata == null) {
			throw new IllegalArgumentException("Metadata can't be null"); 
		}
		else if (screenTemplate == null) {
			throw new IllegalArgumentException("Screen template can't be null"); 
		}
		else {
			
		}
	}

	@Override
	public void download(final String source, final Data target) throws ContentException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void upload(final Data source, final StringBuilder target) throws ContentException, IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean process(final MonitorEvent event, final ContentNodeMetadata metadata, final JComponentInterface component, final Object... parameters) throws ContentException {
		// TODO Auto-generated method stub
		return false;
	}
	
	public static String buildSimpleScreenTemplate(final ContentNodeMetadata metadata, final Map<String, Object> options) {
		return null;
	}
}
