package chav1961.bt.preproc.runtime.interfaces;

public interface CommandRepo {
	long size();
	long currentAddress();
	long addCommand(Command command);
	Command getCommand(long address);
	void removeCommandRange(final long from, final long to);
}