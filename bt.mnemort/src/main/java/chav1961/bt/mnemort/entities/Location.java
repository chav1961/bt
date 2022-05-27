package chav1961.bt.mnemort.entities;

import java.awt.geom.AffineTransform;
import java.io.IOException;

import chav1961.bt.mnemort.entities.BasicEntity.FieldNamesCollection;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.json.interfaces.JsonSerializable;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class Location implements JsonSerializable<Location> {
	public static final String	F_X = "x";
	public static final String	F_Y = "y";
	public static final String	F_SCALE_X = "scaleX";
	public static final String	F_SCALE_Y = "scaleY";
	public static final String	F_ANGLE = "angle";

	private static FieldNamesCollection	fieldsCollection = new FieldNamesCollection(F_X, F_Y, F_SCALE_X, F_SCALE_Y, F_ANGLE); 
	
	private float			x = 0, y = 0;
	private float			scaleX = 1, scaleY = 1;
	private float			angle = 0;
	private AffineTransform	at = new AffineTransform();
	
	public Location() {
	}

	public float getX() {
		return x;
	}

	public void setX(final float x) {
		this.x = x;
		refreshAffineTransform();
	}

	public float getY() {
		return y;
	}

	public void setY(final float y) {
		this.y = y;
		refreshAffineTransform();
	}

	public float getScaleX() {
		return scaleX;
	}

	public void setScaleX(final float scaleX) {
		this.scaleX = scaleX;
		refreshAffineTransform();
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(final float scaleY) {
		this.scaleY = scaleY;
		refreshAffineTransform();
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(final float angle) {
		this.angle = angle;
		refreshAffineTransform();
	}

	public AffineTransform toAffineTransform() {
		return at;
	}

	@Override
	public void fromJson(final JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Json parser can't be null");
		}
		else {
			final FieldNamesCollection	coll = fieldsCollection.newInstance();
			float		_x = 0, _y = 0, _scaleX = 1, _scaleY = 1, _angle = 0;
			
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
loop:			for(JsonStaxParserLexType item : parser) {
					switch (item) {
						case NAME 		:
							BasicEntity.testDuplicate(parser, parser.name(), coll);
							switch (parser.name()) {
								case F_X		:
									_x = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_Y		:
									_y = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_SCALE_X	:
									_scaleX = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_SCALE_Y	:
									_scaleY = BasicEntity.checkAndExtractFloat(parser);
									break;
								case F_ANGLE	:
									_angle = BasicEntity.checkAndExtractFloat(parser);
									break;
								default :
									throw new SyntaxException(parser.row(), parser.col(), "Unsupported name ["+parser.name()+"]");
							}
							break;
						case LIST_SPLITTER :
							break;
						case END_OBJECT	:
							break loop;
						default :
							throw new SyntaxException(parser.row(), parser.col(), "Name or '}' awaited");
					}
				}
				parser.next();
				if (coll.areSomeFieldsMissing()) {
					throw new SyntaxException(parser.row(), parser.col(), "Mandatory field(s) ["+coll.getMissingNames()+"] are missing");
				}
				else {
					x = _x;				y = _y;
					scaleX = _scaleX;	scaleY = _scaleY;
					angle = _angle;
					refreshAffineTransform();
				}
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "Missing '{'");
			}
		}
	}

	@Override
	public void toJson(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Json printer can't be null");
		}
		else {
			printer.startObject().name(F_X).value(x).splitter()
				.name(F_Y).value(y).splitter()
				.name(F_SCALE_X).value(scaleX).splitter()
				.name(F_SCALE_Y).value(scaleY).splitter()
				.name(F_ANGLE).value(angle).endObject();
		}
	}

	@Override
	public void assignFrom(final Location other) {
		if (other == null) {
			throw new NullPointerException("Other location can't be null"); 
		}
		else {
			this.x = other.getX();
			this.y = other.getY();
			this.scaleX = other.getScaleX();
			this.scaleY = other.getScaleY();
			this.angle = other.getAngle();
			refreshAffineTransform();
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(angle);
		result = prime * result + Float.floatToIntBits(scaleX);
		result = prime * result + Float.floatToIntBits(scaleY);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Location other = (Location) obj;
		if (Float.floatToIntBits(angle) != Float.floatToIntBits(other.angle)) return false;
		if (Float.floatToIntBits(scaleX) != Float.floatToIntBits(other.scaleX)) return false;
		if (Float.floatToIntBits(scaleY) != Float.floatToIntBits(other.scaleY)) return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "Location [x=" + x + ", y=" + y + ", scaleX=" + scaleX + ", scaleY=" + scaleY + ", angle=" + angle + "]";
	}

	private void refreshAffineTransform() {
		final AffineTransform	at = new AffineTransform();
		
		at.translate(x, y);
		at.scale(scaleX, scaleY);
		at.rotate(angle);
		this.at = at;
	}
}
