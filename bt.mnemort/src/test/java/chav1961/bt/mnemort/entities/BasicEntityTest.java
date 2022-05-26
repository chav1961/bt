package chav1961.bt.mnemort.entities;


import java.io.IOException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class BasicEntityTest {
	@Test
	public void basicTest() {
	}

	@Test
	public void staticTest() {
	}
}


class PseudoBasicEntity<Canvas extends DrawingCanvas> extends BasicEntity<Canvas> {
	protected PseudoBasicEntity(ContentNodeMetadata meta, UUID entityId) {
		super(meta, entityId);
	}

	@Override
	public ItemViewType getViewType() {
		return null;
	}

	@Override
	public void fromJson(JsonStaxParser parser) throws SyntaxException, IOException {
	}

	@Override
	public void toJson(JsonStaxPrinter printer) throws PrintingException, IOException {
	}

	@Override
	public void draw(Canvas canvas, float width, float height) {
	}
}