package chav1961.bt.mnemort.entities;

import java.awt.geom.AffineTransform;

public class Location {
	private float			x = 0, y = 0;
	private float			scaleX = 1, scaleY = 1;
	private float			angle = 0;
	private AffineTransform	at = new AffineTransform();
	
	public Location() {
	}

	public float getX() {
		return x;
	}

	public void setX(final float x) {
		this.x = x;
		refreshAffineTransform();
	}

	public float getY() {
		return y;
	}

	public void setY(final float y) {
		this.y = y;
		refreshAffineTransform();
	}

	public float getScaleX() {
		return scaleX;
	}

	public void setScaleX(final float scaleX) {
		this.scaleX = scaleX;
		refreshAffineTransform();
	}

	public float getScaleY() {
		return scaleY;
	}

	public void setScaleY(final float scaleY) {
		this.scaleY = scaleY;
		refreshAffineTransform();
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(final float angle) {
		this.angle = angle;
		refreshAffineTransform();
	}

	public AffineTransform toAffineTransform() {
		return at;
	}
	
	private void refreshAffineTransform() {
		final AffineTransform	at = new AffineTransform();
		
		at.translate(x, y);
		at.scale(scaleX, scaleY);
		at.rotate(angle);
		this.at = at;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Float.floatToIntBits(angle);
		result = prime * result + Float.floatToIntBits(scaleX);
		result = prime * result + Float.floatToIntBits(scaleY);
		result = prime * result + Float.floatToIntBits(x);
		result = prime * result + Float.floatToIntBits(y);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Location other = (Location) obj;
		if (Float.floatToIntBits(angle) != Float.floatToIntBits(other.angle)) return false;
		if (Float.floatToIntBits(scaleX) != Float.floatToIntBits(other.scaleX)) return false;
		if (Float.floatToIntBits(scaleY) != Float.floatToIntBits(other.scaleY)) return false;
		if (Float.floatToIntBits(x) != Float.floatToIntBits(other.x)) return false;
		if (Float.floatToIntBits(y) != Float.floatToIntBits(other.y)) return false;
		return true;
	}

	@Override
	public String toString() {
		return "Location [x=" + x + ", y=" + y + ", scaleX=" + scaleX + ", scaleY=" + scaleY + ", angle=" + angle + "]";
	}
}
