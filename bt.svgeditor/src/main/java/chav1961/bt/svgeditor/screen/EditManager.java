package chav1961.bt.svgeditor.screen;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas.ItemDescriptor;
import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.CommandLineParametersException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.ui.swing.SwingUtils;

class EditManager extends MouseManager {
	private static enum Actions {
		NONE,
		MOVE,
		ROTATE,
		SCALE,
		SELECT
	}
	
	private final SVGCanvas		canvas;
	private final double		mouseWheelSpeed;
	private Point2D				startMousePoint = new Point(0,0);
	private Point2D				currentMousePoint = new Point(0,0);
	private double				currentEntityScale = 1;
	private Actions				currentAction = Actions.NONE;
	private PrimitiveWrapper	currentWrapper = null;
	
	public EditManager(final SVGCanvas canvas, final double mouseWheelSpeed) {
		if (canvas == null) {
			throw new NullPointerException("Canvas can't be null");
		}
		else {
			this.canvas = canvas;
			this.mouseWheelSpeed = mouseWheelSpeed;
			addListeners(canvas);
		}
	}

	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		switch (currentAction) {
			case NONE	:
				switch (e.getScrollType()) {
					case MouseWheelEvent.WHEEL_UNIT_SCROLL :
						canvas.scaleCanvas(e.getPreciseWheelRotation());
						break;
					case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
						canvas.scaleCanvas(e.getPreciseWheelRotation() * e.getScrollAmount());
						break;
					default:
						throw new UnsupportedOperationException("Scroll type ["+e.getScrollType()+"] is not supported yet");
				}
				break;
			case ROTATE	:
				break;
			case MOVE	:
				currentAction = Actions.SCALE;
			case SCALE	:
				switch (e.getScrollType()) {
					case MouseWheelEvent.WHEEL_UNIT_SCROLL :
						scaleEntity(currentWrapper, startMousePoint, e.getPreciseWheelRotation());
						break;
					case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
						scaleEntity(currentWrapper, startMousePoint, e.getPreciseWheelRotation() * e.getScrollAmount());
						break;
					default:
						throw new UnsupportedOperationException("Scroll type ["+e.getScrollType()+"] is not supported yet");
				}
				break;
			default :
				throw new UnsupportedOperationException("Action ["+currentAction+"] is not supported yet");
		}
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
		final Point2D		p = canvas.toScaledPoint(e.getPoint());

		currentMousePoint = p;
		switch (currentAction) {
			case MOVE	:
				drawMoveEntity(currentWrapper, startMousePoint, p);
				canvas.fireEvent((l)->l.locationChanged(e));
				break;
			case NONE	:
				break;
			case ROTATE	:
				final double angle	= Math.atan2(canvas.getEffectiveY(p.getY()-startMousePoint.getY()), 
												 canvas.getEffectiveX(p.getX()-startMousePoint.getX()));
				
				rotateEntity(currentWrapper, startMousePoint, canvas.getEffectiveAngle(angle));				
				break;
			case SCALE	:
				break;
			case SELECT :
				final Rectangle2D area = new Rectangle2D.Double(
												Math.min(startMousePoint.getX(), p.getX()),
												Math.min(startMousePoint.getY(), p.getY()),
												Math.abs(p.getX() - startMousePoint.getX()),
												Math.abs(p.getY() - startMousePoint.getY()));
				for(ItemDescriptor item : canvas.getDescriptors()) {
					item.wrapper.setHighlight(item.wrapper.isInside(area));
				}
				canvas.repaint();
				break;
			default :
				throw new UnsupportedOperationException("Action ["+currentAction+"] is not supported yet");
		}
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
		highlightSelections(e);
		currentMousePoint = canvas.toScaledPoint(e.getPoint());
		canvas.fireEvent((l)->l.locationChanged(e));
	}

	@Override
	public void mousePressed(final MouseEvent e) {
		if (!e.isConsumed()) {
			final Point2D	p = canvas.toScaledPoint(e.getPoint());
			
			startMousePoint = p;
			currentWrapper = null;
			currentAction = Actions.NONE;
			e.consume();
					
			for(ItemDescriptor item : canvas.getDescriptors()) {
				if (item.wrapper.isAbout(p, canvas.getDelta())) {
					currentWrapper = item.wrapper;
					currentAction = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0 ? Actions.ROTATE : Actions.MOVE;
					currentEntityScale = 1;				
					currentWrapper.startDrag(p);
					return;
				}
			}
			currentAction = Actions.SELECT;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		final Point2D	p = canvas.toScaledPoint(e.getPoint());
		
		switch (currentAction) {
			case MOVE	:
				currentWrapper.endDrag();
				currentWrapper.clearTransform();
				executeCommand(canvas, String.format("move %1$d,%2$d to %3$d,%4$d",
						(int)startMousePoint.getX(), (int)startMousePoint.getY(), 
						(int)canvas.getEffectiveX(p.getX()), (int)canvas.getEffectiveY(p.getY()))
				);
				currentWrapper = null;
				break;
			case NONE	:
				break;
			case ROTATE	:
				currentWrapper.endDrag();
				currentWrapper.commitChanges();
				currentWrapper.clearTransform();
				currentWrapper = null;
				break;
			case SCALE	:
				currentWrapper.endDrag();
				currentWrapper.commitChanges();
				currentWrapper.clearTransform();
				currentWrapper = null;
				break;
			case SELECT	:
				executeCommand(canvas, String.format("select + window %1$d,%2$d to %3$d,%4$d", 
						(int)startMousePoint.getX(), (int)startMousePoint.getY(),
						(int)p.getX(), (int)p.getY()));
				break;
			default:
				throw new UnsupportedOperationException("Action ["+currentAction+"] is not supported yet");
		}
		highlightSelections(e);
		currentAction = Actions.NONE;
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
		if (currentAction == Actions.NONE) {
			final Point2D	p = canvas.toScaledPoint(e.getPoint());
			
			for(ItemDescriptor item : canvas.getDescriptors()) {
				if (item.wrapper.isAbout(p, canvas.getDelta())) {
					executeCommand(canvas, String.format("select %1$c at %2$d,%3$d",
							canvas.isSelected(item.wrapper) ? '-' : '+',
							(int)p.getX(), (int)p.getY()));
					break;
				}
			}
		}
	}
	
	private void highlightSelections(final MouseEvent e) {
		final Point2D		p = canvas.toScaledPoint(e.getPoint());
		PrimitiveWrapper	oldItem = null, newItem = null;
		Cursor	c = Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
		
		for(ItemDescriptor item : canvas.getDescriptors()) {
			if (item.wrapper.isHighlight()) {
				oldItem = item.wrapper;
				break;
			}
		}
		
		for(ItemDescriptor item : canvas.getDescriptors()) {
			if (item.wrapper.isAbout(p, canvas.getDelta())) {
				c = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
				newItem = item.wrapper;
				break;
			}
		}
		canvas.setCursor(c);
		if (oldItem != newItem) {
			if (oldItem != null) {
				oldItem.setHighlight(false);
			}
			if (newItem != null) {
				newItem.setHighlight(true);
			}
			canvas.repaint();
		}
	}
	
	private void drawMoveEntity(final PrimitiveWrapper entity, final Point2D anchor, final Point2D current) {
		final AffineTransform	at = new AffineTransform();
		
		at.translate(canvas.getEffectiveX(current.getX()) - anchor.getX(), 
					 canvas.getEffectiveY(current.getY()) - anchor.getY());
		entity.setTransform(at);
		canvas.repaint();
	}

	private void rotateEntity(final PrimitiveWrapper entity, final Point2D anchor, double angle) {
		final AffineTransform	at = new AffineTransform();
		
		canvas.beginTransaction("Rotate ["+entity.getClass().getSimpleName()+"]");
		at.translate(anchor.getX(), anchor.getY());
		at.rotate(angle);
		at.translate(-anchor.getX(), -anchor.getY());
		
		entity.setTransform(at);
		canvas.commit();
	}
	
	private void scaleEntity(final PrimitiveWrapper entity, final Point2D anchor, final double scale) {
		final AffineTransform	at = new AffineTransform();
		
		currentEntityScale = canvas.getEffectiveScale(calculateScale(currentEntityScale, scale));
		canvas.beginTransaction("Scale ["+entity.getClass().getSimpleName()+"]");
		at.translate(anchor.getX(), anchor.getY());
		at.scale(currentEntityScale, currentEntityScale);
		at.translate(-anchor.getX(), -anchor.getY());
		
		entity.setTransform(at);
		canvas.commit();
	}
	
	private double calculateScale(final double currentScale, final double step) {
		double	scale = currentScale;
		
		if (step < 0) {
			scale /= (1 - step/mouseWheelSpeed);
		}
		else if (step == 0) {
			scale = 1;
		}
		else {
			scale *= (1 + step/mouseWheelSpeed);
		}
		if (scale < 0.001) {
			scale = 0.001;
		}
		else if (scale > 10) {
			scale = 10;
		}
		return scale;
	}
}

