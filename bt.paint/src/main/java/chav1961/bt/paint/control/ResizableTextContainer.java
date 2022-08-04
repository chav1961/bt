package chav1961.bt.paint.control;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.util.function.Consumer;

import chav1961.purelib.i18n.interfaces.Localizer;

public class ResizableTextContainer extends ResizableImageContainer<ResizableTextContainer> {
	private final Color		foreground;
	private final Color		background;
	private final Font		font;
	
	public ResizableTextContainer(final Localizer localizer, final Color foreground, final Font font, Consumer<ResizableTextContainer> consumer) {
		super(localizer, buildImage(foreground), consumer);
		if (foreground == null) {
			throw new NullPointerException("Foreground color can't be null");
		}
		else if (font == null) {
			throw new NullPointerException("Font can't be null");
		}
		else {
			this.foreground = foreground;
			this.background = null;
			this.font = font;
		}
	}
	
	public ResizableTextContainer(final Localizer localizer, final Color foreground, final Color background, final Font font, Consumer<ResizableTextContainer> consumer) {
		super(localizer, buildImage(foreground, background), consumer);
		if (foreground == null) {
			throw new NullPointerException("Foreground color can't be null");
		}
		else if (background == null) {
			throw new NullPointerException("Background color can't be null");
		}
		else if (font == null) {
			throw new NullPointerException("Font can't be null");
		}
		else {
			this.foreground = foreground;
			this.background = background;
			this.font = font;
		}
	}

	private static final long serialVersionUID = 1L;

	private static Image buildImage(final Color foreground) {
		return null;
	}
	
	private static Image buildImage(final Color foreground, final Color background) {
		return null;
	}
}
