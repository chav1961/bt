package chav1961.bt.mnemort.entities.library.statics;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;
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

public class StaticText<Canvas extends DrawingCanvas> extends BasicEntity<Canvas, StaticText<?>> {
	public static final URI		ITEM_URI = URI.create("static:/text");
	public static final String	F_COLORS = "colors";
	public static final String	F_FONT = "font";
	public static final String	F_CONTENT = "content";
	public static final String	F_SIZE = "size";
	public static final String	F_BOLD = "bold";
	public static final String	F_ITALIC = "italic";
	public static final String	F_FILLED = "filled";

	private static FieldNamesCollection	fieldsCollection = new FieldNamesCollection(F_UUID, F_WIDTH, F_HEIGHT, F_LOCATION, F_COLORS, F_FONT, F_CONTENT, F_SIZE, F_BOLD, F_ITALIC, F_FILLED);
			
	private ColorPair	colors = new ColorPair(Color.BLACK, Color.WHITE);
	private String		fontName = "Courier";	
	private String		content = "***";	
	private int			size = 1;	
	private boolean		bold = false;
	private boolean		italic = false;
	private boolean		filled = false;
	private Font		currentFont = buildCurrentFont();
	
	public StaticText(final ContentNodeMetadata meta, final UUID entityId) {
		super(meta, entityId);
	}

	@Override
	public ItemViewType getViewType() {
		return ItemViewType.STATIC;
	}

	@Override
	public URI getViewURI() {
		return ITEM_URI;
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
			String		_font = "";
			String		_content = "";
			int			_size = 1;
			boolean		_bold = false, _italic = false, _filled = false;
			
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
								case F_FONT			:
									_font = checkAndExtractString(parser);									
									break;
								case F_CONTENT		:
									_content = checkAndExtractString(parser);									
									break;
								case F_SIZE			:
									_size = (int)checkAndExtractLong(parser);									
									break;
								case F_BOLD			:
									_bold = checkAndExtractBoolean(parser);									
									break;
								case F_ITALIC		:
									_italic = checkAndExtractBoolean(parser);									
									break;
								case F_FILLED		:
									_filled = checkAndExtractBoolean(parser);									
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
					fontName = _font;
					content = _content;
					size = _size;
					bold = _bold;
					italic = _italic;
					filled = _filled;
					
					currentFont = buildCurrentFont();
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
			printer.startObject().name(F_UUID).value(getEntityId().toString()).splitter()
				.name(F_WIDTH).value(getWidth()).splitter()
				.name(F_HEIGHT).value(getHeight()).splitter();
			
			printer.name(F_LOCATION);
			getLocation().toJson(printer);
			printer.splitter();
			
			printer.name(F_COLORS);
			colors.toJson(printer);
			printer.splitter();

			printer.name(F_FONT).value(fontName).splitter()
				.name(F_CONTENT).value(content).splitter()
				.name(F_SIZE).value(size).splitter()
				.name(F_BOLD).value(bold).splitter()
				.name(F_ITALIC).value(italic).splitter()
				.name(F_FILLED).value(filled).splitter();
				
			printer.endObject();
		}
	}

	@Override
	public void draw(Canvas canvas, float width, float height) {
		final Rectangle2D	rect = new Rectangle2D.Float(-width/2, -height/2, width, height);
		
		if (filled) {
			canvas.with(CanvasWrapper.of(colors.getBackground())).draw(false,true,CanvasWrapper.of(rect));
		}
		canvas.with(CanvasWrapper.of(colors.getForeground()), CanvasWrapper.of(currentFont)).draw(true,false,CanvasWrapper.of(content, rect));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (bold ? 1231 : 1237);
		result = prime * result + ((colors == null) ? 0 : colors.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + (filled ? 1231 : 1237);
		result = prime * result + ((fontName == null) ? 0 : fontName.hashCode());
		result = prime * result + (italic ? 1231 : 1237);
		result = prime * result + size;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!super.equals(obj)) return false;
		if (getClass() != obj.getClass()) return false;
		final StaticText<Canvas> other = (StaticText<Canvas>) obj;
		if (bold != other.bold) return false;
		if (colors == null) {
			if (other.colors != null) return false;
		} else if (!colors.equals(other.colors)) return false;
		if (content == null) {
			if (other.content != null) return false;
		} else if (!content.equals(other.content)) return false;
		if (filled != other.filled) return false;
		if (fontName == null) {
			if (other.fontName != null) return false;
		} else if (!fontName.equals(other.fontName)) return false;
		if (italic != other.italic) return false;
		if (size != other.size) return false;
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "StaticText [colors=" + colors + ", fontName=" + fontName + ", content=" + content + ", size=" + size
				+ ", bold=" + bold + ", italic=" + italic + ", filled=" + filled + ", toString()=" + super.toString()
				+ "]";
	}
	
	private Font buildCurrentFont() {
		return new Font(fontName, (bold ? Font.BOLD : Font.PLAIN) | (italic ? Font.ITALIC : Font.PLAIN), size);
	}
}

