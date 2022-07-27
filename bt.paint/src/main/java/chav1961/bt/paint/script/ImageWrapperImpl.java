package chav1961.bt.paint.script;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import javax.imageio.ImageIO;

import chav1961.bt.paint.control.ImageUtils;
import chav1961.bt.paint.control.ImageUtils.DrawingType;
import chav1961.bt.paint.control.ImageUtils.ProcessType;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.ColorWrapper;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.RectWrapper;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;

public class ImageWrapperImpl implements ImageWrapper {
	private static final Set<String>	FORMATS_SUPPORTED = Set.of("png","jpeg","gif","bmp");
	
	private BufferedImage	image;
	private String			format = "png";
	private String			name = "unknown.png";
	
	public ImageWrapperImpl() {
		this.image = new BufferedImage(1, 1, BufferedImage.TYPE_4BYTE_ABGR);
	}

	public ImageWrapperImpl(final InputStream is) throws IOException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null"); 
		}
		else {
			final GrowableByteArray	gba = new GrowableByteArray(false);
			final byte[]			signature = new byte[4];
			
			gba.append(is);
			gba.read(0, signature);
			
			try(final InputStream	tmp = gba.getInputStream()) {
				image = ImageIO.read(tmp);
			}
			format = defineImageFormat(signature);
		}
	}
	
	public ImageWrapperImpl(final BufferedImage image) {
		if (image == null) {
			throw new NullPointerException("Image to wrap can't be null"); 
		}
		else {
			this.image = image;
		}
	}
	
	@Override
	public Image getImage() throws PaintScriptException {
		return image;
	}

	@Override
	public ImageWrapper getImage(final RectWrapper wrapper) throws PaintScriptException {
		if (wrapper == null) {
			throw new NullPointerException("Rectangle to get image can't be null"); 
		}
		else {
			return new ImageWrapperImpl((BufferedImage) ImageUtils.process(ImageUtils.ProcessType.CROP, getImage(), null, wrapper.getRect()));
		}
	}

	@Override
	public ImageWrapper setImage(final RectWrapper rectWrapper, final ImageWrapper imageWrapper, final SetOptions... options) throws PaintScriptException {
		if (rectWrapper == null) {
			throw new NullPointerException("Image wrapper can't be null");
		}
		else if (imageWrapper == null) {
			throw new NullPointerException("Image wrapper can't be null");
		}
		else {
			final Rectangle			rect = rectWrapper.getRect();
			final AffineTransform	at = new AffineTransform();
			
			at.translate(rect.x, rect.y);
			fillImage(image, (BufferedImage)imageWrapper.getImage(), at);
			return this;
		}
	}

	@Override
	public String getFormat() throws PaintScriptException {
		return format;
	}

	@Override
	public ImageWrapper setFormat(final String format) throws PaintScriptException {
		if (format == null || format.isEmpty()) {
			throw new IllegalArgumentException("Format to set is null or empty");
		}
		else if (!FORMATS_SUPPORTED.contains(format)) {
			throw new IllegalArgumentException("Format to set ["+format+"] is not supported");
		}
		else {
			this.format = format;
			return this;
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ImageWrapper setName(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name to set is null or empty");
		}
		else {
			this.name = name;
			return this;
		}
	}

	@Override
	public int getType() throws PaintScriptException {
		return image.getType();
	}

	@Override
	public ImageWrapper setType(final int type) throws PaintScriptException {
		final BufferedImage	result = new BufferedImage(image.getWidth(), image.getHeight(), type);
		
		fillImage(result, image, new AffineTransform());
		image = result;
		return this;
	}

	@Override
	public ImageWrapper cloneThis() throws PaintScriptException {
		try{final ImageWrapperImpl	result = (ImageWrapperImpl) super.clone();
		
			result.image = (BufferedImage) ImageUtils.process(ImageUtils.ProcessType.CROP, getImage(), null, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
			return result;
		} catch (CloneNotSupportedException exc) {
			throw new PaintScriptException(exc.getLocalizedMessage());			
		}
	}

	@Override
	public ImageWrapper clear(final ColorWrapper color) throws PaintScriptException {
		if (color == null) {
			throw new NullPointerException("Color wrapper to clear can't be null"); 
		}
		else {
			ImageUtils.draw(DrawingType.BRUSH, image, null, new Rectangle(0, 0, image.getWidth(), image.getHeight()), color.getColor());
			return this;
		}
	}

	@Override
	public ImageWrapper convert(final ProcessType op, final Object... parameters) throws PaintScriptException {
		if (op == null) {
			throw new NullPointerException("Process type can't be null"); 
		}
		else if (parameters == null || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Process parameters is null or contains nulls inside"); 
		}
		else {
			this.image = (BufferedImage) ImageUtils.process(op, image, null, parameters);
			return this;
		}
	}

	@Override
	public ImageWrapper draw(final DrawingType op, final Object... parameters) throws PaintScriptException {
		if (op == null) {
			throw new NullPointerException("Process type can't be null"); 
		}
		else if (parameters == null || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Process parameters is null or contains nulls inside"); 
		}
		else {
			ImageUtils.draw(op, image, null, parameters);
			return this;
		}
	}

	@Override
	public ImageWrapper fill(final DrawingType op, final Object... parameters) throws PaintScriptException {
		if (op == null) {
			throw new NullPointerException("Process type can't be null"); 
		}
		else if (parameters == null || Utils.checkArrayContent4Nulls(parameters) >= 0) {
			throw new IllegalArgumentException("Process parameters is null or contains nulls inside"); 
		}
		else {
			ImageUtils.draw(op, image, null, parameters);
			return this;
		}
	}
	
	private static void fillImage(final BufferedImage source, final BufferedImage fill, final AffineTransform trans) {
		final Graphics2D	g2d = (Graphics2D) source.getGraphics();
		
		g2d.drawImage(fill, trans, null);
		g2d.dispose();
	}

	private static String defineImageFormat(final byte[] signature) {
		if (signature[0] == 0xFF && signature[1] == 0xD8 && signature[2] == 0xFF) {
			return "jpeg";
		}
		else if (signature[0] == 0x42F && signature[1] == 0x4D) {
			return "bmp";
		}
		else if (signature[0] == 0x47 && signature[1] == 0x49 && signature[2] == 0x46 && signature[3] == 0x38) {
			return "gif";
		}
		else if (signature[0] == 0x89 && signature[1] == 0x50 && signature[2] == 0x4E && signature[3] == 0x47) {
			return "png";
		}
		else {
			return "webmp";
		}
	}
}
