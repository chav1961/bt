package chav1961.bt.svgeditor.screen;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.bt.svgeditor.screen.SVGCanvas.ItemDescriptor;

class EditManager extends MouseManager {
	private static enum Actions {
		NONE,
		MOVE,
		ROTATE,
		SCALE
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
				moveEntity(currentWrapper, startMousePoint, p);
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
		final Point2D	p = canvas.toScaledPoint(e.getPoint());
		
		startMousePoint = p;
		currentWrapper = null;
		currentAction = Actions.NONE;
				
		for(ItemDescriptor item : canvas.getDescriptors()) {
			if (item.wrapper.isAbout(p, canvas.getDelta())) {
				currentWrapper = item.wrapper;
				currentAction = (e.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0 ? Actions.ROTATE : Actions.MOVE;
				currentEntityScale = 1;				
				currentWrapper.startDrag(p);
				break;
			}
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		switch (currentAction) {
			case MOVE	:
				currentWrapper.endDrag();
				currentWrapper.commitChanges();
				currentWrapper.clearTransform();
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
			default:
				throw new UnsupportedOperationException("Action ["+currentAction+"] is not supported yet");
		}
		highlightSelections(e);
		currentAction = Actions.NONE;
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
	
	private void moveEntity(final PrimitiveWrapper entity, final Point2D anchor, final Point2D current) {
		final AffineTransform	at = new AffineTransform();
		
		at.translate(canvas.getEffectiveX(current.getX()) - anchor.getX(), 
				canvas.getEffectiveY(current.getY()) - anchor.getY());
		entity.setTransform(at);
		canvas.repaint();
	}

	private void rotateEntity(final PrimitiveWrapper entity, final Point2D anchor, double angle) {
		final AffineTransform	at = new AffineTransform();
		
		at.translate(anchor.getX(), anchor.getY());
		at.rotate(angle);
		at.translate(-anchor.getX(), -anchor.getY());
		
		entity.setTransform(at);
		canvas.repaint();
	}
	
	private void scaleEntity(final PrimitiveWrapper entity, final Point2D anchor, final double scale) {
		final AffineTransform	at = new AffineTransform();
		
		currentEntityScale = canvas.getEffectiveScale(calculateScale(currentEntityScale, scale));
		at.translate(anchor.getX(), anchor.getY());
		at.scale(currentEntityScale, currentEntityScale);
		at.translate(-anchor.getX(), -anchor.getY());
		
		entity.setTransform(at);
		canvas.repaint();
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

