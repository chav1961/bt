package chav1961.bt.mnemoed.editor;

import java.io.IOException;

import javax.swing.undo.UndoManager;

import chav1961.bt.mnemoed.editor.ImageEdit.DrawMode;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;

public class ImageEditCanvas extends JBackgroundComponent {
	private static final long serialVersionUID = 3367119258812876786L;
	
	private final UndoManager	mgr = new UndoManager();
	private final 
	private DrawMode			currentDrawMode = DrawMode.UNKNOWN;
	private String				prevComment = null, currentComment = "";
	
	public ImageEditCanvas(final Localizer localizer) {
		super(localizer);
	}

	public void setCurrentDrawMode(final DrawMode mode) throws IOException {
		if (mode == null) {
			throw new NullPointerException("Drawmode can't be null");
		}
		else {
			switch (getCurrentDrawMode()) {
			
			}
			currentDrawMode = mode;
			switch (getCurrentDrawMode()) {
			
			}
			if (prevComment == null) {
				getUndoManager().addEdit(new ImageUndoEdit(currentComment, getBackgroundImage(), (i)->super.setBackgroundImage(i)));
			}
			else {
				getUndoManager().addEdit(new ImageUndoEdit(currentComment, prevComment, getBackgroundImage(), (i)->super.setBackgroundImage(i)));
			}
		}
	}
	
	public DrawMode getCurrentDrawMode() {
		return currentDrawMode;
	}
	
	public UndoManager getUndoManager() {
		return mgr;
	}
}
