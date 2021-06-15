package chav1961.bt.mnemoed.canvas;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.geom.AffineTransform;

import chav1961.bt.mnemort.entities.interfaces.EntityInterface;
import chav1961.bt.mnemort.interfaces.EntitiesView;
import chav1961.bt.mnemort.interfaces.ItemView;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class SwingCanvas<T> implements EntitiesView<Graphics,T> {
	private Background	background;
	
	public Background getBackground() {
		return background;
	}
	
	public void setBackground(final Background background) {
		if (background == null) {
			throw new NullPointerException("Background to set can't be null");
		}
		else {
			this.background = background;
		}
	}
	
	@Override
	public void draw(final Graphics canvas, final int width, final int height, final int windowWidth, final int windowHeight, @SuppressWarnings("unchecked") final EntityInterface<T>... entities) throws NullPointerException {
		if (canvas == null) {
			throw new NullPointerException("Canvas can't be null");
		}
		else if (width <= 0) {
			throw new IllegalArgumentException("Width ["+width+"] must be greater than 0");
		}
		else if (height <= 0) {
			throw new IllegalArgumentException("Height ["+height+"] must be greater than 0");
		}
		else if (windowWidth <= 0) {
			throw new IllegalArgumentException("Window width ["+windowWidth+"] must be greater than 0");
		}
		else if (windowHeight <= 0) {
			throw new IllegalArgumentException("Window height ["+windowHeight+"] must be greater than 0");
		}
		else if (entities == null) {
			throw new NullPointerException("Entites list can't be null");
		}
		else {
			final Graphics2D		g2d = (Graphics2D)canvas;
			final AffineTransform	current = g2d.getTransform();
			
			g2d.setTransform(calculateTransform(g2d, width, height, windowWidth, windowHeight));
			fillBackground(g2d, 0, 0, width, height);
			for (EntityInterface<?> item : entities) {
				try{item.walkDown((mode,node)->{
						if (mode == NodeEnterMode.ENTER) {
							process(g2d, node, width, height);
						}
						return ContinueMode.CONTINUE;
					});
				} catch (ContentException e) {
				}
			}
			g2d.setTransform(current);
		}
	}

	private AffineTransform calculateTransform(final Graphics2D g2d, final int width, final int height, final int windowWidth, final int windowHeight) {
		final AffineTransform	at = g2d.getTransform();
		
		at.scale(windowWidth, windowHeight);
		return at;
	}

	private void fillBackground(final Graphics2D g2d, final int x, final int y, final int width, final int height) {
		switch (background.getBgType()) {
			case COLOR			:
				final Color		oldColor = g2d.getColor();
				
				g2d.setColor(background.getBgColor());
				g2d.fillRect(x, y, width, height);
				g2d.setColor(oldColor);
				break;
			case GRADIENTPAINT	:
				final Paint		oldPaint = g2d.getPaint();
				
				g2d.setPaint(background.getBgPaint());
				g2d.fillRect(x, y, width, height);
				g2d.setPaint(oldPaint);
				break;
			case IMAGE			:
				final float				imgWidth = background.getBgImage().getWidth(null), imgHeight = background.getBgImage().getHeight(null);
				final AffineTransform	at = new AffineTransform(g2d.getTransform());
				
				at.scale(width/imgWidth, height/imgHeight);
				g2d.drawImage(background.getBgImage(), at, null);
				break;
			case TRANSPARENT	:
				break;
			default				:
				throw new UnsupportedOperationException("Background type ["+background.getBgType()+"] is not supported yet");
		}
	}

	private void process(final Graphics2D canvas, final EntityInterface<?> item, final int width, final int height) {
		final Object	cargo = item.getCargo();
		
		if (cargo instanceof ItemView<?>) {
			((ItemView<Graphics2D>)cargo).draw(canvas, 0, 0);
		}
	}

	public static class Background {
		public enum BackgroundType {
			TRANSPARENT, COLOR, GRADIENTPAINT, IMAGE
		}
		
		private final BackgroundType 	bgType;
		private final Color				bgColor;
		private final Paint				bgPaint;
		private final Image				bgImage;
		
		public Background() throws NullPointerException {
			this(BackgroundType.TRANSPARENT, null, null, null);
		}
		
		public Background(final Color color) throws NullPointerException {
			this(BackgroundType.COLOR, color, null, null);
		}
		
		public Background(final Paint paint) throws NullPointerException {
			this(BackgroundType.GRADIENTPAINT, null, paint, null);
		}

		public Background(final Image image) throws NullPointerException {
			this(BackgroundType.IMAGE, null, null, image);
		}
		
		protected Background(final BackgroundType bgType, final Color bgColor, final Paint bgPaint, final Image bgImage) throws NullPointerException {
			switch (bgType) {
				case COLOR			:
					if (bgColor == null) {
						throw new NullPointerException("Color can't be null for ["+bgType+"] background type");
					}
					break;
				case GRADIENTPAINT	:
					if (bgPaint == null) {
						throw new NullPointerException("Paint can't be null for ["+bgType+"] background type");
					}
					break;
				case IMAGE			:
					if (bgImage == null) {
						throw new NullPointerException("Image can't be null for ["+bgType+"] background type");
					}
					break;
				case TRANSPARENT	:
					break;
				default				:
					throw new UnsupportedOperationException("Background type ["+bgType+"] is not supported yet");
			}
			this.bgType = bgType;
			this.bgColor = bgColor;
			this.bgPaint = bgPaint;
			this.bgImage = bgImage;
		}

		public BackgroundType getBgType() {
			return bgType;
		}

		public Color getBgColor() {
			return bgColor;
		}

		public Paint getBgPaint() {
			return bgPaint;
		}

		public Image getBgImage() {
			return bgImage;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bgColor == null) ? 0 : bgColor.hashCode());
			result = prime * result + ((bgImage == null) ? 0 : bgImage.hashCode());
			result = prime * result + ((bgPaint == null) ? 0 : bgPaint.hashCode());
			result = prime * result + ((bgType == null) ? 0 : bgType.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Background other = (Background) obj;
			if (bgColor == null) {
				if (other.bgColor != null) return false;
			} else if (!bgColor.equals(other.bgColor)) return false;
			if (bgImage == null) {
				if (other.bgImage != null) return false;
			} else if (!bgImage.equals(other.bgImage)) return false;
			if (bgPaint == null) {
				if (other.bgPaint != null) return false;
			} else if (!bgPaint.equals(other.bgPaint)) return false;
			if (bgType != other.bgType) return false;
			return true;
		}

		@Override
		public String toString() {
			return "Background [bgType=" + bgType + ", bgColor=" + bgColor + ", bgPaint=" + bgPaint + ", bgImage=" + bgImage + "]";
		}
	}
}
