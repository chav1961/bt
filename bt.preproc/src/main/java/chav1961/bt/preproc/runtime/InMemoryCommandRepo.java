package chav1961.bt.preproc.runtime;

import java.util.ArrayList;
import java.util.List;

import chav1961.bt.preproc.runtime.interfaces.Command;
import chav1961.bt.preproc.runtime.interfaces.CommandRepo;

public class InMemoryCommandRepo implements CommandRepo {
	private final List<Command>	commands = new ArrayList<>();
	
	public InMemoryCommandRepo() {
		
	}

	@Override
	public long size() {
		return commands.size();
	}

	@Override
	public long currentAddress() {
		return size();
	}

	@Override
	public long addCommand(final Command command) {
		commands.add(command);
		return size() - 1;
	}

	@Override
	public Command getCommand(long address) {
		return commands.get((int)address);
	}

	@Override
	public void removeCommandRange(long from, long to) {
		for(int count = 0, maxCount = (int) (to - from); count < maxCount; count++) {
			commands.remove((int)from);
		}
	}

}
