package chav1961.bt.mnemoed.entities;

import java.awt.Color;
import java.net.URI;

import chav1961.purelib.ui.AnchorPoint;
import chav1961.purelib.ui.AnchorPointPair;
import chav1961.purelib.ui.ColorPair;

public class Background {
	public enum BackgroundType {
		TRANSPARENT, COLOR, LIN_GRAD, RAD_GRAD, IMAGE
	}
	
	private final BackgroundType	type;
	private Color					color = Color.WHITE;
	private ColorPair				linColorPair = new ColorPair(Color.BLACK, Color.WHITE);
	private AnchorPointPair			linPointPair = new AnchorPointPair(); 
	private ColorPair				radColorPair = new ColorPair(Color.BLACK, Color.WHITE);
	private AnchorPoint				radPoint = new AnchorPoint();
	private double					radius = 0;
	private URI						imageUri = URI.create("file:./null");
	
	public Background(final BackgroundType type) {
		if (type == null) {
			throw new NullPointerException("Background type can't be null");
		}
		else {
			this.type = type;
		}
	}
	
	public BackgroundType getType() {
		return type;
	}

	public Color getColor() throws IllegalStateException {
		if (getType() == BackgroundType.COLOR) {
			return color;
		}
		else {
			throw new IllegalStateException("Calling getColor() can't be used for type ["+getType()+"]");
		}
	}

	public void setColor(final Color color) throws NullPointerException, IllegalStateException {
		if (color == null) {
			throw new NullPointerException("Color to set can't be null");
		}
		else if (getType() == BackgroundType.COLOR) {
			this.color = color;
		}
		else {
			throw new IllegalStateException("Calling setColor(Color) can't be used for type ["+getType()+"]");
		}
	}

	public ColorPair getLinColorPair() throws IllegalStateException {
		if (getType() == BackgroundType.LIN_GRAD) {
			return linColorPair;
		}
		else {
			throw new IllegalStateException("Calling getLinColorPair() can't be used for type ["+getType()+"]");
		}
	}

	public void setLinColorPair(final ColorPair linColorPair) throws NullPointerException, IllegalStateException {
		if (linColorPair == null) {
			throw new NullPointerException("Color pair to set can't be null");
		}
		else if (getType() == BackgroundType.LIN_GRAD) {
			this.linColorPair = linColorPair;
		}
		else {
			throw new IllegalStateException("Calling setLinColorPair(ColorPair) can't be used for type ["+getType()+"]");
		}
	}

	public AnchorPointPair getLinPointPair() throws IllegalStateException {
		if (getType() == BackgroundType.LIN_GRAD) {
			return linPointPair;
		}
		else {
			throw new IllegalStateException("Calling getLinPointPair() can't be used for type ["+getType()+"]");
		}
	}

	public void setLinPointPair(AnchorPointPair linPointPair) throws NullPointerException, IllegalStateException {
		if (linPointPair == null) {
			throw new NullPointerException("Point pair to set can't be null");
		}
		else if (getType() == BackgroundType.LIN_GRAD) {
			this.linPointPair = linPointPair;
		}
		else {
			throw new IllegalStateException("Calling setLinPointPair(AnchorPointPair) can't be used for type ["+getType()+"]");
		}
	}

	public ColorPair getRadColorPair() {
		if (getType() == BackgroundType.RAD_GRAD) {
			return radColorPair;
		}
		else {
			throw new IllegalStateException("Calling getRadColorPair() can't be used for type ["+getType()+"]");
		}
	}

	public void setRadColorPair(final ColorPair radColorPair) {
		if (radColorPair == null) {
			throw new NullPointerException("Color pair to set can't be null");
		}
		else if (getType() == BackgroundType.RAD_GRAD) {
			this.radColorPair = radColorPair;
		}
		else {
			throw new IllegalStateException("Calling setRadColorPair(ColorPair) can't be used for type ["+getType()+"]");
		}
	}

	public AnchorPoint getRadPoint() {
		if (getType() == BackgroundType.RAD_GRAD) {
			return radPoint;
		}
		else {
			throw new IllegalStateException("Calling getRadPoint() can't be used for type ["+getType()+"]");
		}
	}

	public void setRadPoint(final AnchorPoint radPoint) {
		if (radPoint == null) {
			throw new NullPointerException("Point to set can't be null");
		}
		else if (getType() == BackgroundType.RAD_GRAD) {
			this.radPoint = radPoint;
		}
		else {
			throw new IllegalStateException("Calling setRadPoint(AnchorPoint) can't be used for type ["+getType()+"]");
		}
	}

	public double getRadius() {
		if (getType() == BackgroundType.RAD_GRAD) {
			return radius;
		}
		else {
			throw new IllegalStateException("Calling getRadPoint() can't be used for type ["+getType()+"]");
		}
	}

	public void setRadius(final double radius) {
		if (radius < 0) {
			throw new IllegalArgumentException("Radius to set ["+radius+"] can't be negative");
		}
		else if (getType() == BackgroundType.RAD_GRAD) {
			this.radius = radius;
		}
		else {
			throw new IllegalStateException("Calling setRadius(double) can't be used for type ["+getType()+"]");
		}
	}
	
	public URI getImageUri() {
		if (getType() == BackgroundType.IMAGE) {
			return imageUri;
		}
		else {
			throw new IllegalStateException("Calling getRadPoint() can't be used for type ["+getType()+"]");
		}
	}

	public void setImageUri(final URI imageUri) {
		if (imageUri == null) {
			throw new NullPointerException("Image to set can't be null");
		}
		else if (getType() == BackgroundType.IMAGE) {
			this.imageUri = imageUri;
		}
		else {
			throw new IllegalStateException("Calling setImageUri(URI) can't be used for type ["+getType()+"]");
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((color == null) ? 0 : color.hashCode());
		result = prime * result + ((imageUri == null) ? 0 : imageUri.hashCode());
		result = prime * result + ((linColorPair == null) ? 0 : linColorPair.hashCode());
		result = prime * result + ((linPointPair == null) ? 0 : linPointPair.hashCode());
		result = prime * result + ((radColorPair == null) ? 0 : radColorPair.hashCode());
		result = prime * result + ((radPoint == null) ? 0 : radPoint.hashCode());
		long temp;
		temp = Double.doubleToLongBits(radius);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Background other = (Background) obj;
		if (color == null) {
			if (other.color != null) return false;
		} else if (!color.equals(other.color)) return false;
		if (imageUri == null) {
			if (other.imageUri != null) return false;
		} else if (!imageUri.equals(other.imageUri)) return false;
		if (linColorPair == null) {
			if (other.linColorPair != null) return false;
		} else if (!linColorPair.equals(other.linColorPair)) return false;
		if (linPointPair == null) {
			if (other.linPointPair != null) return false;
		} else if (!linPointPair.equals(other.linPointPair)) return false;
		if (radColorPair == null) {
			if (other.radColorPair != null) return false;
		} else if (!radColorPair.equals(other.radColorPair)) return false;
		if (radPoint == null) {
			if (other.radPoint != null) return false;
		} else if (!radPoint.equals(other.radPoint)) return false;
		if (Double.doubleToLongBits(radius) != Double.doubleToLongBits(other.radius)) return false;
		if (type != other.type) return false;
		return true;
	}

	@Override
	public String toString() {
		switch (getType()) {
			case COLOR			: return toStringColor();
			case IMAGE			: return toStringImage();
			case LIN_GRAD		: return toStringLinear();
			case RAD_GRAD		: return toStringRadial();
			case TRANSPARENT	: return toStringTransparent();
			default				: return super.toString();
		}
	}

	private String toStringTransparent() {
		return "Background [type=" + type + "]";
	}

	private String toStringColor() {
		return "Background [type=" + type + ", color=" + color + "]";
	}

	private String toStringLinear() {
		return "Background [type=" + type + ", linColorPair=" + linColorPair + ", linPointPair=" + linPointPair + "]";
	}

	private String toStringRadial() {
		return "Background [type=" + type + ", radColorPair=" + radColorPair + ", radPoint=" + radPoint + ", radius=" + radius + "]";
	}

	private String toStringImage() {
		return "Background [type=" + type + ", imageUri=" + imageUri + "]";
	}
}
