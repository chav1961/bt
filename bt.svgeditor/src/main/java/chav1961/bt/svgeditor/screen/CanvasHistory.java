package chav1961.bt.svgeditor.screen;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import chav1961.bt.svgeditor.screen.SVGCanvas.CanvasSnapshot;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.concurrent.LightWeightListenerList;
import chav1961.purelib.ui.swing.interfaces.Undoable;
import chav1961.purelib.ui.swing.interfaces.Undoable.UndoEvent;
import chav1961.purelib.ui.swing.interfaces.Undoable.UndoEventType;
import chav1961.purelib.ui.swing.interfaces.Undoable.UndoListener;

public class CanvasHistory implements Undoable<CanvasSnapshot[]> {
	private final List<CanvasSnapshot[]>	commands = new ArrayList<>();
	private final LightWeightListenerList<UndoListener>	listeners = new LightWeightListenerList<>(UndoListener.class);
	private int		cursor = -1;

	public CanvasHistory() {
	}
	
	@Override
	public boolean canUndo() {
		return !commands.isEmpty() && cursor >= 0;
	}

	@Override
	public CanvasSnapshot[] undo() throws IllegalStateException {
		if (!canUndo()) {
			throw new IllegalStateException("Undo can't be done because content is too few");
		}
		else {
			final UndoEvent			ue = new UndoEvent(this, 0, UndoEventType.CHANGE_UNDO);
			final CanvasSnapshot[]	result = commands.get(cursor--);
			
			SwingUtilities.invokeLater(()->listeners.fireEvent((l)->l.undoChanged(ue)));
			return result;
		}
	}

	@Override
	public boolean canRedo() {
		return !commands.isEmpty() && cursor < commands.size()-1;
	}

	@Override
	public CanvasSnapshot[] redo() throws IllegalStateException {
		if (!canRedo()) {
			throw new IllegalStateException("Redo can't be done because content is too few");
		}
		else {
			final UndoEvent			ue = new UndoEvent(this, 0, UndoEventType.CHANGE_UNDO);
			final CanvasSnapshot[]	result = commands.get(++cursor);
			
			SwingUtilities.invokeLater(()->listeners.fireEvent((l)->l.undoChanged(ue)));
			return result;
		}
	}

	@Override
	public void appendUndo(final CanvasSnapshot[] item) throws NullPointerException {
		if (item == null) {
			throw new NullPointerException("Command to append can't be null");
		}
		else {
			final UndoEvent	ue = new UndoEvent(this, 0, UndoEventType.APPEND_UNDO);

			commands.add(item);
			cursor = commands.size()-1;
			listeners.fireEvent((l)->l.undoChanged(ue));
		}
	}

	@Override
	public void clearUndo() {
		final UndoEvent	ue = new UndoEvent(this, 0, UndoEventType.CLEAR_UNDO);
		
		commands.clear();
		cursor = -1;
		listeners.fireEvent((l)->l.undoChanged(ue));
	}

	@Override
	public CanvasSnapshot[] getCurrentItem() throws IllegalStateException {
		if (cursor < 0 || cursor >= commands.size()) {
			throw new IllegalStateException("Current command is not available because content is too few");
		}
		else {
			return commands.get(cursor);
		}
	}

	@Override
	public void addUndoListener(UndoListener l) throws NullPointerException {
		if (l == null) {
			throw new NullPointerException("Listener to add can't be null");
		}
		else {
			listeners.addListener(l);
		}
	}

	@Override
	public void removeUndoListener(UndoListener l) throws NullPointerException {
		if (l == null) {
			throw new NullPointerException("Listener to remove can't be null");
		}
		else {
			listeners.removeListener(l);
		}
	}
	
	public void removeLastSnapshot() {
		if (!commands.isEmpty()) {
			final UndoEvent	ue = new UndoEvent(this, 0, UndoEventType.REMOVE_UNDO);
			
			commands.remove(cursor--);
			listeners.fireEvent((l)->l.undoChanged(ue));
		}
	}
}
