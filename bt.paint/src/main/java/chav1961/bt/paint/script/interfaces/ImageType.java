package chav1961.bt.paint.script.interfaces;

import java.awt.image.BufferedImage;

public enum ImageType {
	INT3_BGR(BufferedImage.TYPE_3BYTE_BGR),
	INT_ABGR(BufferedImage.TYPE_4BYTE_ABGR),
	INT_ABGR_PRE(BufferedImage.TYPE_4BYTE_ABGR_PRE),
	BYTE_BINARY(BufferedImage.TYPE_BYTE_BINARY),
	BYTE_GRAY(BufferedImage.TYPE_BYTE_GRAY),
	BYTE_INDEXED(BufferedImage.TYPE_BYTE_INDEXED),
	INT_ARGB(BufferedImage.TYPE_INT_ARGB),
	INT_ARGB_PRE(BufferedImage.TYPE_INT_ARGB_PRE),
	INT_BGR(BufferedImage.TYPE_INT_BGR),
	INT_RGB(BufferedImage.TYPE_INT_RGB),
	USHORT_555_RGB(BufferedImage.TYPE_USHORT_555_RGB),
	USHORT_565_RGB(BufferedImage.TYPE_USHORT_565_RGB),
	USHORT_GRAY(BufferedImage.TYPE_USHORT_GRAY);

	private final int	type;
	
	private ImageType(final int type) {
		this.type = type;
	}
	
	public int getType() {
		return type;
	}
	
	public static ImageType valueOf(final int type) {
		for (ImageType item : values()) {
			if (item.getType() == type) {
				return item;
			}
		}
		throw new IllegalArgumentException("Image type ["+type+"] not found"); 
	}
}