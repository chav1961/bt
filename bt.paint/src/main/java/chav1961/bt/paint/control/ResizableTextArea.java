package chav1961.bt.paint.control;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;

import chav1961.purelib.ui.ColorPair;

class ResizableTextArea extends JTextArea {
	private static final long 		serialVersionUID = 1L;
	private static final int		GUARD_DISTANCE = 50;

	private final ResizableBorder	border = new ResizableBorder(this);
	private final Cursor			defaultCursor = getCursor();
	
	ResizableTextArea(final Color color, final Font font, final Rectangle initialBounds) {
		setForeground(color);
		setFont(font);
		setOpaque(false);
		super.setBorder(border);
		setMinimumSize(new Dimension(font.getSize() + border.getBorderInsets(this).left + border.getBorderInsets(this).right, font.getSize() + border.getBorderInsets(this).top+border.getBorderInsets(this).bottom));
		setPreferredSize(initialBounds.getSize());
		setLineWrap(true);
		setWrapStyleWord(false);
        addMouseListener(resizeListener);
        addMouseMotionListener(resizeListener);
        setBounds(initialBounds);
	}

	ResizableTextArea(final ColorPair colorPair, final Font font, final Rectangle initialBounds) {
		setForeground(colorPair.getForeground());
		setBackground(colorPair.getBackground());
		setFont(font);
		setOpaque(true);
		super.setBorder(border);
		setMinimumSize(new Dimension(font.getSize() + border.getBorderInsets(this).left + border.getBorderInsets(this).right, font.getSize() + border.getBorderInsets(this).top+border.getBorderInsets(this).bottom));
		setPreferredSize(initialBounds.getSize());
		setLineWrap(true);
		setWrapStyleWord(false);
        addMouseListener(resizeListener);
        addMouseMotionListener(resizeListener);
        setBounds(initialBounds);
	}
	
	@Override
	public void setBorder(final Border border) {
		throw new UnsupportedOperationException("THis control doesn't support custom borders");
	}
	
    private void resizeComponent() {
        if (getParent() != null) {
            getParent().revalidate();
        }
    }
	
    private final MouseInputListener resizeListener = new MouseInputAdapter() {
        private Point 	startPos = null;
        private int 	cursor;

        @Override
        public void mouseMoved(MouseEvent me) {
            if (hasFocus()) {
                setCursor(Cursor.getPredefinedCursor(border.getCursor(me)));
            }
        }

        @Override
        public void mouseExited(MouseEvent mouseEvent) {
            setCursor(defaultCursor);
        }

        @Override
        public void mousePressed(MouseEvent me) {
            cursor = border.getCursor(me);
            startPos = me.getPoint();

            requestFocus();
            repaint();
        }

        @Override
        public void mouseDragged(final MouseEvent me) {
            if (startPos != null) {
                final int 	x = getX(), y = getY(), w = getWidth(), h = getHeight();
                final int 	dx = me.getX() - startPos.x, dy = me.getY() - startPos.y;

                switch (cursor) {
                    case Cursor.N_RESIZE_CURSOR :
                        if (!(h - dy < GUARD_DISTANCE)) {
                            setBounds(x, y + dy, w, h - dy);
                            resizeComponent();
                        }
	                   	break;
                    case Cursor.S_RESIZE_CURSOR :
                        if (!(h + dy < GUARD_DISTANCE)) {
                            setBounds(x, y, w, h + dy);
                            startPos = me.getPoint();
                            resizeComponent();
                        }
                        break;
                    case Cursor.W_RESIZE_CURSOR :
                    	if (!(w - dx < GUARD_DISTANCE)) {
                            setBounds(x + dx, y, w - dx, h);
                            resizeComponent();
                        }
                    	break;
                    case Cursor.E_RESIZE_CURSOR :
                        if (!(w + dx < GUARD_DISTANCE)) {
                            setBounds(x, y, w + dx, h);
                            startPos = me.getPoint();
                            resizeComponent();
                        }
                        break;
                    case Cursor.NW_RESIZE_CURSOR :
                        Rectangle bounds = getBounds();
                        bounds.translate(dx, dy);
                        setBounds(bounds);
                        resizeComponent();
//
//                        if (!(w - dx < GUARD_DISTANCE) && !(h - dy < GUARD_DISTANCE)) {
//                            setBounds(x + dx, y + dy, w - dx, h - dy);
//                            resizeComponent();
//                        }
                        break;
                    case Cursor.NE_RESIZE_CURSOR :
                        if (!(w + dx < GUARD_DISTANCE) && !(h - dy < GUARD_DISTANCE)) {
                            setBounds(x, y + dy, w + dx, h - dy);
                            startPos = new Point(me.getX(), startPos.y);
                            resizeComponent();
                        }
                        break;
                    case Cursor.SW_RESIZE_CURSOR :
                        if (!(w - dx < GUARD_DISTANCE) && !(h + dy < GUARD_DISTANCE)) {
                            setBounds(x + dx, y, w - dx, h + dy);
                            startPos = new Point(startPos.x, me.getY());
                            resizeComponent();
                        }
                        break;
                    case Cursor.SE_RESIZE_CURSOR :
                        if (!(w + dx < GUARD_DISTANCE) && !(h + dy < GUARD_DISTANCE)) {
                            setBounds(x, y, w + dx, h + dy);
                            startPos = me.getPoint();
                            resizeComponent();
                        }
                        break;
                    default :
                        setCursor(defaultCursor);
                        return;
//                    case Cursor.MOVE_CURSOR : {
//                        Rectangle bounds = getBounds();
//                        bounds.translate(dx, dy);
//                        setBounds(bounds);
//                        resizeComponent();
//                    }
//                	break;
                }
                setCursor(Cursor.getPredefinedCursor(cursor));
            }
        }

        @Override
        public void mouseReleased(final MouseEvent mouseEvent) {
            startPos = null;
        }
    };
}
