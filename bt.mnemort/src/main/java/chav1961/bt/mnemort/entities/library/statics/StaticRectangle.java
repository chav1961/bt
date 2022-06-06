package chav1961.bt.mnemort.entities.library.statics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import chav1961.bt.mnemort.entities.BasicEntity;
import chav1961.bt.mnemort.entities.Location;
import chav1961.bt.mnemort.interfaces.CanvasWrapper;
import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.streams.interfaces.JsonStaxParserLexType;
import chav1961.purelib.ui.ColorPair;

public class StaticRectangle<Canvas extends DrawingCanvas> extends BasicEntity<Canvas, StaticCircle<?>> {
	public static final URI		ITEM_URI = URI.create("static:/rectangle");
	public static final String	F_COLORS = "colors";
	public static final String	F_THICKNESS = "thickness";
	public static final String	F_FILLED = "filled";
	public static final String	F_ROUNDING_RADIUS = "roundingRadius";
	
	private static FieldNamesCollection	fieldsCollection = new FieldNamesCollection(F_UUID, F_WIDTH, F_HEIGHT, F_LOCATION, F_COLORS, F_THICKNESS, F_FILLED, F_ROUNDING_RADIUS); 
	
	private ColorPair	colors = new ColorPair(Color.BLACK, Color.WHITE);
	private float		thickness = 0.1f;	
	private boolean		filled = false;	
	private float		roundingRadius = 0;
	
	public StaticRectangle(final ContentNodeMetadata meta, final UUID entityId) {
		super(meta, entityId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ItemViewType getViewType() {
		return ItemViewType.STATIC;
	}

	@Override
	public void fromJson(JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Json parser can't be null");
		}
		else {
			final FieldNamesCollection	coll = fieldsCollection.newInstance();
			UUID		_uuid = new UUID(0L, 0L);
			float		_width = 1, _height = 1;
			Location	_location = new Location();
			ColorPair	_colors = new ColorPair(Color.BLACK, Color.WHITE);
			float		_thickness = 1, _roundingRadius = 0;	
			boolean		_filled = false;
		
			if (parser.current() == JsonStaxParserLexType.START_OBJECT) {
loop:			for(JsonStaxParserLexType item : parser) {
					switch (item) {
						case NAME 		:
							testDuplicate(parser, parser.name(), coll);
							switch (parser.name()) {
								case F_UUID			:
									_uuid = checkAndExtractUUID(parser);									
									break;
								case F_WIDTH		:
									_width = checkAndExtractFloat(parser);
									break;
								case F_HEIGHT		:
									_height = checkAndExtractFloat(parser);
									break;
								case F_LOCATION		:
									if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
										parser.next();
										_location.fromJson(parser);
									}
									else {
										throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
									}
									break;
								case F_COLORS		:
									if (parser.next() == JsonStaxParserLexType.NAME_SPLITTER) {
										parser.next();
										_colors.fromJson(parser);
									}
									else {
										throw new SyntaxException(parser.row(), parser.col(), "Missing ':'");
									}
									break;
								case F_THICKNESS	:
									_thickness = checkAndExtractFloat(parser);
									break;
								case F_FILLED		:
									_filled = checkAndExtractBoolean(parser);
									break;
								case F_ROUNDING_RADIUS		:
									_roundingRadius = checkAndExtractFloat(parser);
									break;
								default :
									throw new SyntaxException(parser.row(), parser.col(), "Unsupported name ["+parser.name()+"]");
							}
							break;
						case LIST_SPLITTER	:
							break;
						case END_OBJECT	:
							break loop;
						default :
							throw new SyntaxException(parser.row(), parser.col(), "Name or '}' awaited");
					}
				}
			}
			else {
				throw new SyntaxException(parser.row(), parser.col(), "Missing '{'");
			}
			
			if (parser.hasNext()) {
				parser.next();
			}
			if (coll.areSomeFieldsMissing()) {
				throw new SyntaxException(parser.row(), parser.col(), "Mandatory field(s) ["+coll.getMissingNames()+"] are missing");
			}
			else {
				setEntityId(_uuid);
				setWidth(_width);	
				setHeight(_height);
				getLocation().assignFrom(_location);
				colors.assignFrom(_colors);
				thickness = _thickness;
				filled = _filled;
				roundingRadius = _roundingRadius;					
			}
		}
	}

	@Override
	public void toJson(final JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Json printer can't be null");
		}
		else {
			printer.startObject().name(F_UUID).value(getEntityId().toString()).splitter()
				.name(F_WIDTH).value(getWidth()).splitter()
				.name(F_HEIGHT).value(getHeight()).splitter()
				.name(F_THICKNESS).value(thickness).splitter()
				.name(F_FILLED).value(filled).splitter()
				.name(F_ROUNDING_RADIUS).value(roundingRadius).splitter();
			
			printer.name(F_LOCATION);
			getLocation().toJson(printer);
			printer.splitter();
			
			printer.name(F_COLORS);
			colors.toJson(printer);
			printer.endObject();
		}
	}

	@Override
	public void draw(final Canvas canvas, final float width, final float height) {
		final RectangularShape	rect;
		
		if (roundingRadius > 0) {
			rect = new RoundRectangle2D.Float(-width/2, -height/2, width, height, width * roundingRadius, height * roundingRadius);
		}
		else {
			rect = new Rectangle2D.Float(-width/2, -height/2, width, height);
		}
		
		if (filled) {
			canvas.with(CanvasWrapper.of(colors.getBackground())).draw(false,true,CanvasWrapper.of(rect));
		}
		canvas.with(CanvasWrapper.of(colors.getForeground()), CanvasWrapper.of(new BasicStroke(thickness))).draw(true,false,CanvasWrapper.of(rect));
		
	}

	@Override
	public URI getViewURI() {
		return ITEM_URI;
	}
}
