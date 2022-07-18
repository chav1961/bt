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
	private final boolean			significant;
	private final byte[]			imageBefore;
	private final byte[]			imageAfter;

	public ImageUndoEdit(final String undo, final String redo, final byte[] imageBefore, final byte[] imageAfter, final Consumer<Image> consumer) throws IOException {
		this(undo, redo, imageBefore, imageAfter, true, consumer);
	}
	
	public ImageUndoEdit(final String undo, final String redo, final byte[] imageBefore, final byte[] imageAfter, final boolean significant, final Consumer<Image> consumer) throws IOException {
		this.undo = undo;
		this.redo = redo;
		this.imageBefore = imageBefore;
		this.imageAfter = imageAfter;
		this.significant = significant;
		this.consumer = consumer;
	}

	@Override
	public void undo() throws CannotUndoException {
		consumer.accept(unpackImage(imageBefore));
	}

	@Override
	public boolean canUndo() {
		return imageBefore != null;
	}

	@Override
	public void redo() throws CannotRedoException {
		consumer.accept(unpackImage(imageAfter));
	}

	@Override
	public boolean canRedo() {
		return imageAfter != null && redo != null;
	}

	@Override
	public void die() {
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
		return significant;
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
	
	public static byte[] packImage(final Image image) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream();
			final GZIPOutputStream		gzos = new GZIPOutputStream(baos)) {
			
			ImageIO.write((RenderedImage) image, "png", gzos);
			gzos.finish();
			return baos.toByteArray();
		}
	}
	
	public static Image unpackImage(final byte[] content) {
		try(final ByteArrayInputStream	bais = new ByteArrayInputStream(content);
			final GZIPInputStream		gzis = new GZIPInputStream(bais)) {
			
			return ImageIO.read(gzis);
		} catch (IOException e) {
			return null;
		}
	}
}
