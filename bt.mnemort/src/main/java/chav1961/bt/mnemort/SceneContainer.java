package chav1961.bt.mnemort;

import java.awt.Color;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.geom.Rectangle2D;
import java.util.UUID;

import chav1961.bt.mnemort.canvas.swing.SwingCanvas;
import chav1961.bt.mnemort.entities.BasicContainer;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class SceneContainer extends BasicContainer<SwingCanvas>{
	public SceneContainer(final ContentNodeMetadata meta, final UUID entityId) {
		super(meta, entityId);
		setX(0);
		setY(0);
		setWidth(100);
		setHeight(100);
	}

	@Override
	public void fromJson(JsonStaxParser parser) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void toJson(JsonStaxPrinter printer) throws PrintingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void drawBackground(final SwingCanvas canvas, float x, float y, float width, float height) {
		final RadialGradientPaint rgp = new RadialGradientPaint(0.0f, 0.0f
											, (float)(0.75f*Math.max(width, height))
											, new float[]{0.0f, 1.0f}
											, new Color[]{Color.YELLOW, Color.BLACK});
		final Rectangle2D.Double	r2d = new Rectangle2D.Double(
												-width/2,-height/2
												,width, height);
		final Paint					oldPaint = canvas.getNativeGraphics().getPaint();
		
		canvas.getNativeGraphics().setPaint(rgp);
		canvas.getNativeGraphics().fill(r2d);
		canvas.getNativeGraphics().setPaint(oldPaint);
	}
}
