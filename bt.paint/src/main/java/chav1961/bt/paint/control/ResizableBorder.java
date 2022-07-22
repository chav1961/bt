package chav1961.bt.paint.control;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

import javax.swing.border.Border;

public class ResizableBorder implements Border {
	public static final int	DEFAULT_BORDER_WIDTH = 8;

	@FunctionalInterface
	private static interface Calculator {
	    Rectangle calculate(int x, int y, int w, int h, int borderWidth);
	}
	
	private static enum Location {
		INSIDE(Cursor.MOVE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> null),
		OUTSIDE(Cursor.DEFAULT_CURSOR, (int x, int y, int w, int h, int borderWidth) -> null),
		NORTH(Cursor.N_RESIZE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> new Rectangle(x + w / 2 - borderWidth / 2, y, borderWidth, borderWidth)),
		NORTH_EAST(Cursor.NE_RESIZE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> new Rectangle(x + w - borderWidth, y, borderWidth, borderWidth)),
		EAST(Cursor.E_RESIZE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> new Rectangle(x + w - borderWidth, y + h / 2 - borderWidth / 2, borderWidth, borderWidth)),
		SOUTH_EAST(Cursor.SE_RESIZE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> new Rectangle(x + w - borderWidth, y + h - borderWidth, borderWidth, borderWidth)),
		SOUTH(Cursor.S_RESIZE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> new Rectangle(x + w / 2 - borderWidth / 2, y + h - borderWidth, borderWidth, borderWidth)),
		SOUTH_WEST(Cursor.SW_RESIZE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> new Rectangle(x, y + h - borderWidth, borderWidth, borderWidth)),
		WEST(Cursor.W_RESIZE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> new Rectangle(x, y + h / 2 - borderWidth / 2, borderWidth, borderWidth)),
		NORTH_WEST(Cursor.NW_RESIZE_CURSOR, (int x, int y, int w, int h, int borderWidth) -> new Rectangle(x, y, borderWidth, borderWidth));
		
		private final int			cursorType;
		private final Calculator	calc;
		
		private Location(final int cursorType, final Calculator calc) {
			this.cursorType = cursorType;
			this.calc = calc;
		}
		
		int getCursorType() {
			return cursorType;
		}

	    Rectangle getRectangle(int x, int y, int w, int h, int borderWidth) {
	    	return calc.calculate(x, y, w, h, borderWidth);
	    }
	}
	
    private final int borderWidth;

    public ResizableBorder(final Component owner) {
        this(owner, DEFAULT_BORDER_WIDTH);
    }
    
    public ResizableBorder(final Component owner, final int borderWidth) {
        this.borderWidth = borderWidth;
    }

    @Override
    public Insets getBorderInsets(Component component) {
        return new Insets(borderWidth, borderWidth, borderWidth, borderWidth);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }

    @Override
    public void paintBorder(final Component component, final Graphics g, final int x, final int y, final int w, final int h) {
    	final Color	oldColor = g.getColor();
    	
        g.setColor(Color.black);
        g.drawRect(x + borderWidth / 2, y + borderWidth / 2, w - borderWidth, h - borderWidth);

        if (component.hasFocus()) {
            for (Location location : Location.values()) {
                Rectangle rect = location.getRectangle(x, y, w, h, borderWidth);

                if (rect != null) {
                    g.setColor(location == Location.NORTH_WEST ? Color.BLACK : Color.WHITE);
                    g.fillRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
                    g.setColor(Color.BLACK);
                    g.drawRect(rect.x, rect.y, rect.width - 1, rect.height - 1);
                }
            }
        }
        g.setColor(oldColor);
    }

    public int getCursor(final MouseEvent me) {
        final Component	c = me.getComponent();
        final int 		w = c.getWidth();
        final int 		h = c.getHeight();

        for (Location location : Location.values()) {
            Rectangle rect = location.getRectangle(0, 0, w, h, borderWidth);

            if (rect != null && rect.contains(me.getPoint())) {
            	return location.getCursorType();
            }
        }
        return Cursor.MOVE_CURSOR;
    }
}