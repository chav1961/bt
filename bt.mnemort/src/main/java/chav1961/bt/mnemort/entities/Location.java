package chav1961.bt.mnemort.entities;

import java.awt.geom.AffineTransform;
import java.io.IOException;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.json.interfaces.JsonSerializable;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;

public class Location implements JsonSerializable {
	public static final String	F_LOCATION = "location";
	
	public static final String	F_X = "x";
	public static final String	F_Y = "y";
	public static final String	F_SCALE_X = "scaleX";
	public static final String	F_SCALE_Y = "scaleY";
	public static final String	F_ANGLE = "angle";
	
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
			float		_x = 0, _y = 0, _scaleX = 1, _scaleY = 1, _angle = 0;
			boolean		xPresents = false, yPresents = false, xScalePresents = false, yScalePresents = false, anglePresents = false;
			
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
loop:			for(JsonStaxParserLexType item : parser) {
					switch (item) {
						case NAME 		:
							switch (parser.name()) {
								case F_X		:
									_x = BasicEntity.checkAndExtractFloat(parser, F_X, xPresents);
									xPresents = true;
									break;
								case F_Y		:
									_y = BasicEntity.checkAndExtractFloat(parser, F_Y, yPresents);
									yPresents = true;
									break;
								case F_SCALE_X	:
									_scaleX = BasicEntity.checkAndExtractFloat(parser, F_SCALE_X, xScalePresents);
									xScalePresents = true;
									break;
								case F_SCALE_Y	:
									_scaleY = BasicEntity.checkAndExtractFloat(parser, F_SCALE_Y, yScalePresents);
									yScalePresents = true;
									break;
								case F_ANGLE	:
									_angle = BasicEntity.checkAndExtractFloat(parser, F_ANGLE, anglePresents);
									anglePresents = true;
									break;
								default :
									throw new SyntaxException(parser.row(), parser.col(), "Unsupported name ["+parser.name()+"]");
							}
						case END_OBJECT	:
							break loop;
						default :
							throw new SyntaxException(parser.row(), parser.col(), "Name or '}' awaited");
					}
				}
				parser.next();
				if (!xPresents || !yPresents || !xScalePresents || !yScalePresents || !anglePresents) {
					final StringBuilder	sb = new StringBuilder();
					
					if (!xPresents) {
						sb.append(',').append(F_X);
					}
					if (!yPresents) {
						sb.append(',').append(F_Y);
					}
					if (!xScalePresents) {
						sb.append(',').append(F_SCALE_X);
					}
					if (!yScalePresents) {
						sb.append(',').append(F_SCALE_Y);
					}
					if (!anglePresents) {
						sb.append(',').append(F_ANGLE);
					}
					throw new SyntaxException(parser.row(), parser.col(), "Mandatory field(s) ["+sb.substring(1)+"] are missing");
				}
				else {
					x = _x;				y = _y;
					scaleX = _scaleX;	scaleY = _scaleY;
					angle = _angle;
					refreshAffineTransform();
				}
			}
		}
	}

	@Override
	public void toJson(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Json printer can't be null");
		}
		else {
			printer.startObject().name(F_X).value(x).name(F_Y).value(y)
				.name(F_SCALE_X).value(scaleX).name(F_SCALE_Y).value(scaleY)
				.name(F_ANGLE).value(angle).endObject();
		}
	}
	
	private void refreshAffineTransform() {
		final AffineTransform	at = new AffineTransform();
		
		at.translate(x, y);
		at.scale(scaleX, scaleY);
		at.rotate(angle);
		this.at = at;
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
}
