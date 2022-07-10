package chav1961.bt.paint.control;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEdit;

class ImageUndoEdit implements UndoableEdit {
	private final String			undo;
	private final String			redo;
	private final Consumer<Image>	consumer; 
	private byte[]					image;

	public ImageUndoEdit(final String undo, final Image image, final Consumer<Image> consumer) throws IOException {
		this.undo = undo;
		this.redo = null;
		this.image = packImage(image);
		this.consumer = consumer;
	}
	
	public ImageUndoEdit(final String undo, final String redo, final Image image, final Consumer<Image> consumer) throws IOException {
		this.undo = undo;
		this.redo = redo;
		this.image = packImage(image);
		this.consumer = consumer;
	}

	@Override
	public void undo() throws CannotUndoException {
		consumer.accept(unpackImage(image));
	}

	@Override
	public boolean canUndo() {
		return image != null;
	}

	@Override
	public void redo() throws CannotRedoException {
		consumer.accept(unpackImage(image));
	}

	@Override
	public boolean canRedo() {
		return image != null && redo != null;
	}

	@Override
	public void die() {
		image = null;
	}

	@Override
	public boolean addEdit(final UndoableEdit anEdit) {
		return false;
	}

	@Override
	public boolean replaceEdit(final UndoableEdit anEdit) {
		return false;
	}

	@Override
	public boolean isSignificant() {
		return true;
	}

	@Override
	public String getPresentationName() {
		return "Image";
	}

	@Override
	public String getUndoPresentationName() {
		return undo;
	}

	@Override
	public String getRedoPresentationName() {
		return redo;
	}

	@Override
	public String toString() {
		return "ImageUndoEdit [undo=" + undo + ", redo=" + redo + "]";
	}
	
	private byte[] packImage(final Image image) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final GZIPOutputStream		gzos = new GZIPOutputStream(baos)) {
			
			ImageIO.write((RenderedImage) image, "png", gzos);
			gzos.finish();
			return baos.toByteArray();
		}
	}
	
	private Image unpackImage(final byte[] content) {
		try(final ByteArrayInputStream	bais = new ByteArrayInputStream(content);
			final GZIPInputStream		gzis = new GZIPInputStream(bais)) {
			
			return ImageIO.read(gzis);
		} catch (IOException e) {
			return null;
		}
	}
}
