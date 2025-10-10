package chav1961.bt.svgeditor.parser;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.io.IOException;

import chav1961.bt.svgeditor.parser.AbstractCommandProcessor.Content;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.CharUtils.Mark;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class PasteProcessor extends AbstractCommandProcessor {

	private int	xTo = 0, yTo = 0;
	
	private final Content<?>[]	VARIANT_1 = {
									new Content<Integer>(Integer.class, (c,v)->xTo = v),
									new Content<Integer>(Integer.class, (c,v)->yTo = v),
								};
	
	public PasteProcessor(final Object... parameters) throws CommandLineParametersException {
		prepareProcessor(parameters, VARIANT_1);
	}
	
	@Override
	public void execute(final SVGCanvas canvas) throws CalculationException {
		if (!Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(PrimitiveWrapperTransferable.FLAVOR)) {
			throw new CalculationException("Clipboard doesn't contain anything to paste");
		}
		else {
			try {
				final PrimitiveWrapper[]	content = ((PrimitiveWrapperTransferable)Toolkit.getDefaultToolkit().getSystemClipboard().getData(PrimitiveWrapperTransferable.FLAVOR)).content; 

				for (PrimitiveWrapper item : content) {
					final AffineTransform	at = new AffineTransform();
					
					at.translate(xTo, yTo);
					item.setTransform(at);
					item.commitChanges();
				}
				canvas.add(content);
			} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
				canvas.rollback();
				throw new CalculationException(e);
			}
		}
	}

}
