package chav1961.bt.svgeditor.screen;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JTextField;
import javax.swing.KeyStroke;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.ui.swing.SwingUtils;

public class CommandHistory {
	private final int			maxDepth;
	private final List<String>	commands;
	private int		cursor = 0;
	
	public CommandHistory(final int maxDepth) {
		if (maxDepth < 0) {
			throw new IllegalArgumentException("Max command depth ["+maxDepth+"] must be greater or equals than 0");
		}
		else {
			this.maxDepth = maxDepth;
			this.commands = maxDepth == 0 ? new ArrayList<>() : new ArrayList<>(maxDepth);
		}
	}

	public boolean canUndo() {
		return cursor <= commands.size() && cursor > 0;
	}
	
	public String undo() {
		if (!canUndo()) {
			throw new IllegalStateException("Undo can't be done because content is too few");
		}
		else {
			return commands.get(--cursor);
		}
	}

	public boolean canRedo() {
		return cursor >= 0 && cursor < commands.size()-1;
	}
	
	public String redo() {
		if (!canRedo()) {
			throw new IllegalStateException("Redo can't be done because content is too few");
		}
		else {
			return commands.get(++cursor);
		}
	}
	
	public void append(final String command) {
		if (Utils.checkEmptyOrNullString(command)) {
			throw new IllegalArgumentException("Command to append can be neither null nor empty");
		}
		else {
			commands.add(command);
			if (maxDepth != 0) {
				while (commands.size() > maxDepth) {
					commands.remove(0);
				}
			}
			cursor = commands.size();
		}
	}
	
	public String getCurrentCommand() {
		if (cursor >= commands.size()) {
			throw new IllegalStateException("Current command is not available because content is too few");
		}
		else {
			return commands.get(cursor);
		}
	}
	
	public static interface CommandProcessor {
		void process(String command) throws Exception;
	}

	public static CommandHistory of(final JTextField component, final CommandProcessor processor) {
		return of(component, 0, processor);
	}	
	
	public static CommandHistory of(final JTextField component, final int maxDepth, final CommandProcessor processor) {
		if (component == null) {
			throw new NullPointerException("Component can't be null");
		}
		else if (maxDepth < 0) {
			throw new IllegalArgumentException("Max command depth ["+maxDepth+"] must be greater or equals than 0");
		}
		else if (processor == null) {
			throw new NullPointerException("Command processor can't be null");
		}
		else {
			final CommandHistory	history = new CommandHistory(maxDepth);
			
			component.addActionListener((e)->{
				final String	cmd = component.getText();
				
				if (!Utils.checkEmptyOrNullString(cmd)) {
					try{
						processor.process(cmd);
						component.setText("");
						history.append(cmd);
					} catch (Exception exc) {
					}
				}
			});
			SwingUtils.assignActionKey(component, KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), (e)->{
				if (history.canUndo()) {
					component.setText(history.undo());
				}
			}, SwingUtils.ACTION_UNDO);
			SwingUtils.assignActionKey(component, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), (e)->{
				if (history.canRedo()) {
					component.setText(history.redo());
				}
			}, SwingUtils.ACTION_REDO);
			return history;
		}
	}
}
