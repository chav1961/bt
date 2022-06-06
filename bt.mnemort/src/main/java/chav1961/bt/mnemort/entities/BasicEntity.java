package chav1961.bt.mnemort.entities;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.UUID;

import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.bt.mnemort.interfaces.ItemView;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.json.interfaces.JsonSerializable;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.model.interfaces.NodeMetadataOwner;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public abstract class BasicEntity<Canvas extends DrawingCanvas, Self> implements ItemView<Canvas>, NodeMetadataOwner, JsonSerializable<Self> {
	public static final String			F_UUID = "uuid";
	public static final String			F_WIDTH = "width";
	public static final String			F_HEIGHT = "height";
	public static final String			F_LOCATION = "location";
	
	private final ContentNodeMetadata	meta;
	private final Location				location = new Location();
	private UUID						entityId;
	private float						width = 1.0f, height = 1.0f;

	
	protected static enum FillMode {
		FILL, PROPORTIONAL;
	}
	
	protected BasicEntity(final ContentNodeMetadata meta, final UUID entityId) {
		this.meta = meta;
		this.entityId = entityId;
	}

	@Override public abstract void draw(final Canvas canvas, final float width, final float height);
	@Override public abstract URI getViewURI();
	
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entityId == null) ? 0 : entityId.hashCode());
		result = prime * result + Float.floatToIntBits(height);
		result = prime * result + ((location == null) ? 0 : location.hashCode());
		result = prime * result + Float.floatToIntBits(width);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		BasicEntity other = (BasicEntity) obj;
		if (entityId == null) {
			if (other.entityId != null) return false;
		} else if (!entityId.equals(other.entityId)) return false;
		if (Float.floatToIntBits(height) != Float.floatToIntBits(other.height)) return false;
		if (location == null) {
			if (other.location != null) return false;
		} else if (!location.equals(other.location)) return false;
		if (Float.floatToIntBits(width) != Float.floatToIntBits(other.width)) return false;
		return true;
	}
	
	protected void setEntityId(final UUID uuid) {
		this.entityId = uuid;
	}
	
	protected static void testDuplicate(final JsonStaxParser parser, final String field, final FieldNamesCollection collection) throws SyntaxException {
		if (!collection.add(field)) {
			throw new SyntaxException(parser.row(), parser.col(), "Duplicate field name ["+field+"]");
		}
	}

	protected static String checkAndExtractString(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
			switch (parser.next()) {
				case STRING_VALUE 	: return parser.stringValue();
				default : throw new SyntaxException(parser.row(), parser.col(), "String value is missing");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
		}
	}
	
	protected static UUID checkAndExtractUUID(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
			switch (parser.next()) {
				case STRING_VALUE 	:
					try{return UUID.fromString(parser.stringValue());
					} catch (IllegalArgumentException exc) {
						throw new SyntaxException(parser.row(), parser.col(), "Illegal UUID ("+exc.getLocalizedMessage()+")");
					}
				default : throw new SyntaxException(parser.row(), parser.col(), "String value is missing");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
		}
	}

	protected static long checkAndExtractLong(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
			switch (parser.next()) {
				case INTEGER_VALUE 	: return parser.intValue();
				default : throw new SyntaxException(parser.row(), parser.col(), "Integer value is missing");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
		}
	}
	
	protected static float checkAndExtractFloat(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
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

	protected static boolean checkAndExtractBoolean(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
			switch (parser.next()) {
				case BOOLEAN_VALUE 	: return parser.booleanValue();
				default : throw new SyntaxException(parser.row(), parser.col(), "Boolean value is missing");
			}
		}
		else {
			throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
		}
	}

	protected static Rectangle2D fillRectangle(final FillMode mode, final Rectangle2D area, final Rectangle2D control) {
		switch (mode) {
			case FILL			:
				return	area.createIntersection(control);
			case PROPORTIONAL	:
				return	area.createIntersection(control);
			default	:
				throw new UnsupportedOperationException("Fill mode ["+mode+"] is not supported yet");
		}
	}
	
	protected static class FieldNamesCollection {
		private final String[]	names;
		private boolean[]		bits = null;
		
		public FieldNamesCollection(final String... names) {
			this(false, names);
		}		
		
		private FieldNamesCollection(boolean clone, final String... names) {
			this.names = names;
			if (clone) {
				this.bits = new boolean[names.length];
			}
			else {
				Arrays.sort(this.names);
			}
		}

		public FieldNamesCollection newInstance() {
			return new FieldNamesCollection(true, names);
		}
		
		public boolean add(final String name) {
			final int	where = Arrays.binarySearch(names, name);
			
			if (where > 0) {
				final boolean	result = bits[where];
				
				bits[where] = true;
				return result; 
			}
			else {
				return false;
			}
		}
		
		public boolean areSomeFieldsMissing() {
			for (boolean item : bits) {
				if (!item) {
					return true;
				}
			}
			return false;
		}
		
		public String getMissingNames() {
			final StringBuilder	sb = new StringBuilder();
			
			for(int index = 0; index < names.length; index++) {
				sb.append(',').append(names[index]);
			}
			return sb.substring(1);
		}

		@Override
		public String toString() {
			return "FieldNamesCollection [names=" + Arrays.toString(names) + ", bits=" + Arrays.toString(bits) + "]";
		}
	}
	
	
}
