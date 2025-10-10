package chav1961.bt.svgeditor.parser;

import java.util.List;

import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;

public class DeleteProcessor extends AllLastAndSelectedCommandProcessor {
	public DeleteProcessor(final Object... parameters) throws CommandLineParametersException {
		super(parameters);
	}

	@Override
	protected void process(SVGCanvas canvas, List<PrimitiveWrapper> items) {
		canvas.delete(items.toArray(new PrimitiveWrapper[items.size()]));
	}
}
