package chav1961.bt.mnemort;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.UUID;

import chav1961.bt.mnemort.canvas.swing.SwingCanvas;
import chav1961.bt.mnemort.entities.BasicEntity;
import chav1961.bt.mnemort.interfaces.CanvasWrapper;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class CircleItem extends BasicEntity<SwingCanvas>{
	protected CircleItem(final ContentNodeMetadata meta, final UUID entityId) {
		super(meta, entityId);
		// TODO Auto-generated constructor stub
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
	public void draw(final SwingCanvas canvas, final float x, final float y, final float width, final float height) {
		final RadialGradientPaint	rgp = new RadialGradientPaint(0.0f, 0.0f, (float)(0.75f*width), new float[]{0.0f, (float)width}, new Color[]{Color.WHITE, Color.GRAY});
		final Color					oldColor = canvas.getNativeGraphics().getColor();
		final Stroke				oldStroke = canvas.getNativeGraphics().getStroke();
		final Ellipse2D.Double		ell = new Ellipse2D.Double(-width/2,-height/2,width,height);
		final Paint					oldPaint = canvas.getNativeGraphics().getPaint();
		final AffineTransform		newAt = new AffineTransform();

		newAt.translate(x, y);
//		canvas.transform(newAt);
		canvas.with(CanvasWrapper.of(Color.GREEN), CanvasWrapper.of(new BasicStroke(0.01f)), CanvasWrapper.of(rgp)).draw(CanvasWrapper.of(ell));
		canvas.getNativeGraphics().setPaint(rgp);
		canvas.getNativeGraphics().fill(ell);
		canvas.getNativeGraphics().setColor(Color.GREEN);
		canvas.getNativeGraphics().setStroke(new BasicStroke(0.01f));
		canvas.getNativeGraphics().draw(ell);
		canvas.getNativeGraphics().setPaint(oldPaint);
		canvas.getNativeGraphics().setStroke(oldStroke);
		canvas.getNativeGraphics().setColor(oldColor);
	}
}
