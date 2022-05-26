package chav1961.bt.mnemort.entities;

import java.io.IOException;
import java.util.UUID;

import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.bt.mnemort.interfaces.ItemView;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.json.interfaces.JsonSerializable;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public abstract class BasicEntity<Canvas extends DrawingCanvas> implements ItemView<Canvas>, NodeMetadataOwner, JsonSerializable {
	public static final String			F_UUID = "uuid";
	public static final String			F_WIDTH = "width";
	public static final String			F_HEIGHT = "height";
	
	private final ContentNodeMetadata	meta;
	private final Location				location = new Location();
	private UUID						entityId;
	private float						width = 1.0f, height = 1.0f;
	
	protected BasicEntity(final ContentNodeMetadata meta, final UUID entityId) {
		this.meta = meta;
		this.entityId = entityId;
	}

	@Override public abstract void draw(final Canvas canvas, final float width, final float height);
	
	@Override
	public ContentNodeMetadata getNodeMetadata() {
		return meta;
	}
	
	public UUID getEntityId() {
		return entityId;
	}

	public Location getLocation() {
		return location;
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
		draw(canvas, getWidth(), getHeight());
	}

	protected static float checkAndExtractFloat(final JsonStaxParser parser, final String field, final boolean thisPresents) throws SyntaxException, IOException {
		if (thisPresents) {
			throw new SyntaxException(parser.row(), parser.col(), "Duplicate field name ["+field+"]");
		}
		else if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
			switch (parser.next()) {
				case INTEGER_VALUE 	: return parser.intValue();
				case REAL_VALUE 	: return (float) parser.realValue();
				default : throw new SyntaxException(parser.row(), parser.col(), "Numeric value is missing");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
		}
	}

	protected static boolean checkAndExtractBoolean(final JsonStaxParser parser, final String field, final boolean thisPresents) throws SyntaxException, IOException {
		if (thisPresents) {
			throw new SyntaxException(parser.row(), parser.col(), "Duplicate field name ["+field+"]");
		}
		else if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
			switch (parser.next()) {
				case BOOLEAN_VALUE 	: return parser.booleanValue();
				default : throw new SyntaxException(parser.row(), parser.col(), "Boolean value is missing");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
		}
	}
}
