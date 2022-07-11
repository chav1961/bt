package chav1961.bt.paint.control;

import java.awt.Image;

public class ImageUtils {
	public static enum ProcessType {
		CROP, RESIZE, SCALE, ROTATE_CLOCKWISE, ROTATE_COUNTERCLOCKWISE, MIRROR_HORIZONTAL, MIRROR_VERTICAL, TO_GRAYSCALE, TO_TRANSPARENT 
	}
	
	public static Image process(final ProcessType type, final Image source, final Object... parameters) {
		if (type == null) {
			throw new NullPointerException("Process type can't be null");
		}
		else if (source == null) {
			throw new NullPointerException("Image to process can't be null");
		}
		else {
			switch (type) {
				case CROP				:
					break;
				case MIRROR_HORIZONTAL	:
					break;
				case MIRROR_VERTICAL:
					break;
				case RESIZE:
					break;
				case ROTATE_CLOCKWISE:
					break;
				case ROTATE_COUNTERCLOCKWISE:
					break;
				case SCALE:
					break;
				case TO_GRAYSCALE:
					break;
				case TO_TRANSPARENT:
					break;
				default:
					break;
			}
			return null;
		}
	}
}
