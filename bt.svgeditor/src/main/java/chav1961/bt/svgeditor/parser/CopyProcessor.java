package chav1961.bt.svgeditor.parser;

import java.awt.Toolkit;
import java.util.List;

import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class CopyProcessor extends AllLastAndSelectedCommandProcessor {

	private final boolean	deleteAfterCopy;
	
	public CopyProcessor(final boolean deleteAfterCopy, final Object... parameters) throws CommandLineParametersException {
		super(parameters);
		this.deleteAfterCopy = deleteAfterCopy;
	}

	@Override
	protected void process(final SVGCanvas canvas, final List<PrimitiveWrapper> items) {
		final PrimitiveWrapper[]	content = items.toArray(new PrimitiveWrapper[items.size()]);
		
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
				new PrimitiveWrapperTransferable(content)
				, null);
		if (deleteAfterCopy) {
			canvas.delete(content);
		}
	}
}
