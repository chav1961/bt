package chav1961.bt.jj.starter;

import java.io.IOException;
import java.io.InputStream;

public class MappingInputStream extends InputStream {
	private final long	startAddress;
	private final long	size;
	private long  		markAddress; 
	private long  		currentAddress; 
	
	public MappingInputStream(final long startAddress, final long size) {
		this.startAddress = startAddress;
		this.size = size;
		this.markAddress = startAddress;
		this.currentAddress = startAddress;
	}

	@Override
	public int read() throws IOException {
		if (currentAddress >= startAddress + size) {
			return -1;
		}
		else {
			return (byte)JJ.unname1(currentAddress++);
		}
	}
	
	@Override
	public void reset() throws IOException {
		currentAddress = markAddress;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public void mark(int readlimit) {
		markAddress = currentAddress;
	}
}
