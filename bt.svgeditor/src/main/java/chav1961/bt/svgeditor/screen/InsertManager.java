package chav1961.bt.svgeditor.screen;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.util.function.Consumer;

import javax.swing.JLabel;

import chav1961.bt.svgeditor.screen.SVGEditor.InsertAction;

public class InsertManager extends MouseManager implements KeyListener {
	
	public static enum PrimitiveType {
		LINE(new AutomatLine(0,0,"",0,null)
				
			);
		
		private final AutomatLine[]	content;
		
		private PrimitiveType(final AutomatLine... content) {
			this.content = content;
		}
		
		public AutomatLine[] getContent() {
			return content;
		}
	}
	
	private final SVGCanvas		canvas;
	private final InsertAction	action;
	private final JLabel		help = new JLabel();
	
	public InsertManager(final SVGCanvas canvas, final InsertAction action) {
		if (canvas == null) {
			throw new NullPointerException("Canvas can't be null");
		}
		else if (action == null) {
			throw new NullPointerException("Insert action can't be null");
		}
		else {
			this.canvas = canvas;
			this.action = action;
			addListeners(canvas);
			canvas.add(help);
			help.setForeground(Color.white);
			help.setLocation(0, 0);
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
	
	@Override
	public void close() throws RuntimeException {
		super.close();
		canvas.remove(help);
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

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
