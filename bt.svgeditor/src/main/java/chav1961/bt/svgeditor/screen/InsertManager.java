package chav1961.bt.svgeditor.screen;

import java.io.InputStream;
import java.util.function.Consumer;

public class InsertManager extends MouseManager {
	
	public static enum PrimitiveType {
		LINE();
		
		private final AutomatLine[]	content;
		
		private PrimitiveType(final AutomatLine... content) {
			this.content = content;
		}
		
		public AutomatLine[] getContent() {
			return content;
		}
	}
	
	private final SVGCanvas	canvas;
	
	public InsertManager(final SVGCanvas canvas) {
		if (canvas == null) {
			throw new NullPointerException("Canvas can't be null");
		}
		else {
			this.canvas = canvas;
			addListeners(canvas);
		}
	}
	
	public void beginInsertion(final PrimitiveType type) {
		if (type == null) {
			throw new NullPointerException("Primitive type can't be null");
		}
		else {
			// TODO:
		}
	}
	
	public void undo() {
		
	}
	
	private static AutomatLine[] load(final InputStream resource) {
		return null;
	}
	
	private static class AutomatLine {
		private final int		currentState;
		private final int		terminal;
		private final String	prompt;
		private final int		newState;
		private final Consumer<?>	action;
		
		private AutomatLine(final int currentState, final int terminal, final String propmt, final int newState, final Consumer<?> action) {
			this.currentState = currentState;
			this.terminal = terminal;
			this.prompt = propmt;
			this.newState = newState;
			this.action = action;
		}
		
		
	}
	
}
