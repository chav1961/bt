package chav1961.bt.mnemort.entities.library;

import java.awt.Color;
import java.io.IOException;
import java.util.UUID;

import chav1961.bt.mnemort.entities.BasicEntity;
import chav1961.bt.mnemort.entities.Location;
import chav1961.bt.mnemort.interfaces.DrawingCanvas;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.ui.ColorPair;

public class StaticCircle<Canvas extends DrawingCanvas> extends BasicEntity<Canvas> {
	public static final String	F_WIDTH = "width";
	public static final String	F_HEIGHT = "height";
	public static final String	F_COLORS = "colors";
	public static final String	F_THICKNESS = "thickness";
	public static final String	F_FILLED = "filled";
	
	private ColorPair	colors = new ColorPair(Color.BLACK, Color.WHITE);
	private float		thickness = 0.1f;	
	private boolean		filled = true;
	
	public StaticCircle(final ContentNodeMetadata meta, final UUID entityId) {
		super(meta, entityId);
	}

	@Override
	public ItemViewType getViewType() {
		return ItemViewType.STATIC;
	}
	
	@Override
	public void fromJson(JsonStaxParser parser) throws SyntaxException, IOException {
		// TODO Auto-generated method stub
		if (parser == null) {
			throw new NullPointerException("Json parser can't be null");
		}
		else {
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
				.name(F_FILLED).value(filled).splitter();
			
			printer.name(Location.F_LOCATION);
			getLocation().toJson(printer);
			printer.splitter();
			
			printer.name(F_COLORS);
			colors.toJson(printer);
			printer.endObject();
		}
	}

	@Override
	public void draw(Canvas canvas, float width, float height) {
		// TODO Auto-generated method stub
		
	}
}
