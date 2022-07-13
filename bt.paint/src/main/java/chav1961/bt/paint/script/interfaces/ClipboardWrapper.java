package chav1961.bt.paint.script.interfaces;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import chav1961.bt.paint.interfaces.PaintScriptException;

public interface ClipboardWrapper {
	boolean hasImage() throws PaintScriptException;
	ImageWrapper getImage() throws PaintScriptException;
	void setImage(ImageWrapper image) throws PaintScriptException;
	
	ClipboardWrapper singleton = new ClipboardWrapper() {
		private final Clipboard 	clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		
		@Override
		public boolean hasImage() throws PaintScriptException {
			return clipboard.isDataFlavorAvailable(DataFlavor.imageFlavor);
		}

		@Override
		public ImageWrapper getImage() throws PaintScriptException {
			if (!hasImage()) {
				throw new PaintScriptException("System clipboard doesn't contain image");
			}
			else {
				try{return ImageWrapper.of((Image)clipboard.getContents(this).getTransferData(DataFlavor.imageFlavor));
				} catch (UnsupportedFlavorException | IOException e) {
					throw new PaintScriptException(e.getLocalizedMessage(), e);
				}
			}
		}

		@Override
		public void setImage(final ImageWrapper image) throws PaintScriptException {
			if (image == null) {
				throw new NullPointerException("Image to set can't be null");
			}
			else {
				final Transferable		trans = new Transferable() {
											@Override
											public DataFlavor[] getTransferDataFlavors() {
												return new DataFlavor[]{DataFlavor.imageFlavor};
											}
						
											@Override
											public boolean isDataFlavorSupported(final DataFlavor flavor) {
												return DataFlavor.imageFlavor.equals(flavor);
											}
						
											@Override
											public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
												if (!isDataFlavorSupported(flavor)) {
													return null;
												}
												else {
													try{return image.getImage();
													} catch (PaintScriptException e) {
														throw new IOException(e.getLocalizedMessage(), e);
													}
												}
											}
										};
				clipboard.setContents(trans, null);										
			}
		}
	};
}