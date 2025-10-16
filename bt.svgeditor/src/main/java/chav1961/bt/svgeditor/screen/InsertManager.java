package chav1961.bt.svgeditor.screen;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JComponent;

import chav1961.bt.svgeditor.primitives.LineWrapper;
import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.ui.swing.SwingUtils;

public class InsertManager extends MouseManager implements KeyListener {
	private static final Rectangle2D	CURSOR_AREA = new Rectangle2D.Double(0, 0, 1.0, 1.0);
	private static final Color			CURSOR_AREA_BACKGROUND = new Color(0,0,0,0);
	
	private static interface CustomCursor {
		String getName();
		Point getHotSpot();
		void draw(Graphics2D g2d);
		
		static CustomCursor of(final String name, final Point hotSpot, final Consumer<Graphics2D> drawing) {
			if (Utils.checkEmptyOrNullString(name)) {
				throw new IllegalArgumentException("Cursor name can be neither null nor empty");
			}
			else if (hotSpot == null) {
				throw new NullPointerException("Hot spot can't be null");
			}
			else if (drawing == null) {
				throw new NullPointerException("Drawing can't be null");
			}
			else {
				return new CustomCursor() {
					@Override
					public String getName() {
						return name;
					}

					@Override
					public Point getHotSpot() {
						return hotSpot;
					}

					@Override
					public void draw(final Graphics2D g2d) {
						drawing.accept(g2d);
					}
				};
			}
		}
	}

	static enum Terminal {
		POINT,
		COMPLETION,
		UNDOING,
		CANCELLATION
	}
	
	static enum PrimitiveType {
		LINE(CustomCursor.of("SVGEditor.lineCursor", new Point(0,0), (g2d)->drawLine(g2d)),
			 (m)->new LineWrapper((Point)m.items.get("start"), (Point)m.items.get("end")),
			 new AutomatLine(0,Terminal.POINT,"Click start line point",1,(m, p)->{m.items.put("start", (Point)p[0]);}),
			 new AutomatLine(0,Terminal.CANCELLATION,"",2,(m, p)->{m.cancel();}),
			 new AutomatLine(1,Terminal.POINT,"Click end line point",2,(m, p)->{m.items.put("end", (Point)p[0]); m.complete();}),
			 new AutomatLine(1,Terminal.COMPLETION,"",2,(m, p)->{m.items.put("end", (Point)p[0]); m.complete();}),
			 new AutomatLine(1,Terminal.UNDOING,"",0,(m, p)->{m.items.remove("end");}),
			 new AutomatLine(1,Terminal.CANCELLATION,"",2,(m, p)->{m.cancel();})
			);
		
		private final CustomCursor	cc;
		private final Function<InsertManager, PrimitiveWrapper>	complete;
		private final AutomatLine[]	content;
		
		private PrimitiveType(final CustomCursor cc, final Function<InsertManager, PrimitiveWrapper> complete, final AutomatLine... content) {
			this.cc = cc;
			this.complete = complete;
			this.content = content;
		}
		
		public CustomCursor getCustomCursor() {
			return cc;
		}
		
		public AutomatLine[] getContent() {
			return content;
		}
	}

	private final SVGCanvas		canvas;
	private final PrimitiveType	action;
	private final Cursor		oldCursor;
	private final Map<String, Object>	items = new HashMap<>();
	private PrimitiveWrapper	result = null;
	private int		currentState = -1;
	
	public InsertManager(final SVGCanvas canvas, final PrimitiveType action) {
		if (canvas == null) {
			throw new NullPointerException("Canvas can't be null");
		}
		else if (action == null) {
			throw new NullPointerException("Insert action can't be null");
		}
		else {
			this.canvas = canvas;
			this.action = action;
			this.oldCursor = canvas.getCursor();
			addListeners(canvas);
			canvas.setCursor(drawCursor(action.getCustomCursor()));
			setAutomatState(0);
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		super.mouseMoved(e);
		canvas.fireEvent((l)->l.locationChanged(e));
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		super.mouseDragged(e);
		canvas.fireEvent((l)->l.locationChanged(e));
	}
	
	@Override
	public void mouseClicked(final MouseEvent e) {
		super.mouseClicked(e);
		if (!e.isConsumed() && e.getClickCount() == 1) {
			try {
				switch (e.getButton()) {
					case MouseEvent.BUTTON1 :
						if (automat(Terminal.POINT, e.getPoint())) {
							e.consume();
						}
						break;
					case MouseEvent.BUTTON3 : 
						if (automat(Terminal.COMPLETION, e.getPoint())) {
							e.consume();
						}
						break;
				}
			} catch (CalculationException exc) {
				SwingUtils.getNearestLogger((Component)e.getSource()).message(Severity.error, exc, exc.getLocalizedMessage());
			}
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (!e.isConsumed()) {
			try {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_U :
						if (automat(Terminal.UNDOING)) {
							e.consume();
						}
						break;
					case KeyEvent.VK_ESCAPE :
						if (automat(Terminal.CANCELLATION)) {
							e.consume();
						}
						break;
				}
			} catch (CalculationException exc) {
				SwingUtils.getNearestLogger((Component)e.getSource()).message(Severity.error, exc, exc.getLocalizedMessage());
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}
	
	@Override
	public void close() throws RuntimeException {
		removeListeners(canvas);
		canvas.setPrompt(0, 0, null);
		canvas.setCursor(oldCursor);
		super.close();
	}

	public PrimitiveWrapper getResult() {
		return result;
	}
	
	protected boolean automat(final Terminal terminal, final Object... parameters) throws CalculationException {
		for(AutomatLine item : action.content) {
			if (item.currentState == currentState && item.terminal == terminal) {
				setAutomatState(item.newState);
				item.action.accept(this, parameters);
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void addListeners(final JComponent owner) {
		super.addListeners(owner);
		owner.addKeyListener(this);
	}
	
	@Override
	public void removeListeners(JComponent owner) {
		owner.removeKeyListener(this);
		super.removeListeners(owner);
	}
	
	private void setAutomatState(final int state) {
		currentState = state;
		for(AutomatLine item : action.content) {
			if (item.currentState == currentState && !Utils.checkEmptyOrNullString(item.prompt)) {
				canvas.setPrompt(0, canvas.getFont().getSize(), item.prompt);
				break;
			}
		}
	}

	private void complete() {
		result = action.complete.apply(this);
		SwingUtils.getNearestOwner(canvas, SVGEditor.class).endInsertion(true);
	}

	private void cancel() {
		SwingUtils.getNearestOwner(canvas, SVGEditor.class).endInsertion(false);
	}
	
	private static class AutomatLine {
		private final int		currentState;
		private final Terminal	terminal;
		private final String	prompt;
		private final int		newState;
		private final BiConsumer<InsertManager, Object[]>	action;
		
		private AutomatLine(final int currentState, final Terminal terminal, final String prompt, final int newState, final BiConsumer<InsertManager, Object[]> action) {
			this.currentState = currentState;
			this.terminal = terminal;
			this.prompt = prompt;
			this.newState = newState;
			this.action = action;
		}
	}

	private static Cursor drawCursor(final CustomCursor cc) {
		final Dimension			dim = Toolkit.getDefaultToolkit().getBestCursorSize(32, 32);
		final BufferedImage		image = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_4BYTE_ABGR);
		final Graphics2D 		g2d = (Graphics2D) image.getGraphics();
		final AffineTransform	at = new AffineTransform();
		
		at.translate(0, dim.getHeight());
		at.scale(dim.getWidth(), -dim.getHeight());
		g2d.setTransform(at);
		g2d.setColor(CURSOR_AREA_BACKGROUND);
		g2d.fill(CURSOR_AREA);
		cc.draw(g2d);
		
		return Toolkit.getDefaultToolkit().createCustomCursor(image, cc.getHotSpot(), cc.getName());
	}

	private static void drawLine(final Graphics2D g2d) {
		final Line2D		l = new Line2D.Double(0,1,1,0); 
		
		g2d.setStroke(new BasicStroke(0.03f));
		g2d.setColor(Color.WHITE);
		g2d.draw(l);
		final Ellipse2D	e1 = new Ellipse2D.Double(0,0.9,0.1,0.1);
		
		g2d.setColor(Color.RED);
		g2d.fill(e1);
		g2d.setColor(Color.RED);
		g2d.draw(e1);
		final Ellipse2D	e2 = new Ellipse2D.Double(0.9,0,0.1,0.1);
		
		g2d.setColor(Color.BLACK);
		g2d.fill(e2);
		g2d.setColor(Color.WHITE);
		g2d.draw(e2);
	}
}
