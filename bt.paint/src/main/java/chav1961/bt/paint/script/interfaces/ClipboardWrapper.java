package chav1961.bt.paint.script.interfaces;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.ImageWrapperImpl;
import chav1961.purelib.concurrent.LightWeightListenerList;

public interface ClipboardWrapper extends ContentWrapper<ImageWrapper> {
	boolean hasImage() throws PaintScriptException;
	ImageWrapper getImage() throws PaintScriptException;
	void setImage(ImageWrapper image) throws PaintScriptException;
	void addChangeListener(ChangeListener l);
	void removeChangeListener(ChangeListener l);
	
	ClipboardWrapper singleton = new ClipboardWrapper() {
		private final Clipboard 	clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		private final LightWeightListenerList<ChangeListener>	listeners = new LightWeightListenerList<>(ChangeListener.class);
		
		{
			clipboard.addFlavorListener((e)->{
				final ChangeEvent	ce = new ChangeEvent(clipboard);
				
				listeners.fireEvent((l)->l.stateChanged(ce));
			});
		}
		
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
				try{return new ImageWrapperImpl((BufferedImage)clipboard.getContents(this).getTransferData(DataFlavor.imageFlavor));
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
				final Image				source = image.getImage();
				final BufferedImage		bi = new BufferedImage(source.getWidth(null), source.getHeight(null), BufferedImage.TYPE_INT_RGB);
				final Graphics2D		g2d = (Graphics2D)bi.createGraphics();
				
				g2d.drawImage(source, 0, 0, null);
				g2d.dispose();
				
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
													return bi;
												}
											}
										};
				clipboard.setContents(trans, null);										
			}
		}
		
		public void addChangeListener(final ChangeListener l) {
			if (l == null) {
				throw new NullPointerException("Listener to add can't be null"); 
			}
			else {
				listeners.addListener(l);
			}
		}
		
		public void removeChangeListener(final ChangeListener l) {
			if (l == null) {
				throw new NullPointerException("Listener to remove can't be null"); 
			}
			else {
				listeners.removeListener(l);
			}
		}

		@Override
		public ImageWrapper getContent() throws PaintScriptException {
			return getImage();
		}

		@Override
		public void setContent(final ImageWrapper content) throws PaintScriptException {
			setImage(content);
		}

		@Override
		public Class<ImageWrapper> getContentType() {
			return ImageWrapper.class;
		}
		
		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone();
		}
	};
}