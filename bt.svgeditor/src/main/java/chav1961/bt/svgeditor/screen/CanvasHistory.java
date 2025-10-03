package chav1961.bt.svgeditor.screen;

import java.util.ArrayList;
import java.util.List;

public class CanvasHistory {
	private final List<SVGCanvas.CanvasSnapshot>	snapshots = new ArrayList<>();
	private int	cursor = -1;
	
	public CanvasHistory() {
		
	}
	
	public void clear() {
		snapshots.clear();
		cursor = -1;
	}
	
	public boolean canUndo() {
		return false;
	}
	
	public void undo(final SVGCanvas canvas) {
		if (canvas == null) {
			throw new NullPointerException("Canvas to undo can't be null");
		}
		else {
			
		}
	}

	public boolean canRedo() {
		return false;
	}

	public void redo(final SVGCanvas canvas) {
		if (canvas == null) {
			throw new NullPointerException("Canvas to redo can't be null");
		}
		else {
			
		}
	}
	
	public void appendSnapshot(final SVGCanvas canvas) {
		if (canvas == null) {
			throw new NullPointerException("Canvas to append snapshot can't be null");
		}
		else {
			
		}
	}
	
	public void removeLastSnapshot() {
		
	}
}
