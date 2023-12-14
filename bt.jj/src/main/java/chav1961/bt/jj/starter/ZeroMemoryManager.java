package chav1961.bt.jj.starter;

//
//	Free block:
//		- long: nextFreeBlock or -1 to terminal
//		- long: free block size
//		- any: content
//
//	Used block:
//		- long: block size + 16. Sign bit is used as mark for mark/sweep algorithm
//		- any: content
//		- long: block size + 16. Sign bit is always 0
//
//	Object descriptor:
//		- int: object type and flags
//		- int: array length for array or flags for object
//		- long: class descriptor reference
//		- any: content
//	Object reference points to content, not to object type and flags 
//

public class ZeroMemoryManager {
	private final long	address;
	private final long	minimumSize;
	private final long	maximumSize;
	private long		currentMaximum;
	
	public ZeroMemoryManager(final long address, final long minimumSize, final long maximumSize) {
		this.address = address;
		this.minimumSize = minimumSize;
		this.maximumSize = maximumSize;
		this.currentMaximum = minimumSize;
	}
	
	public void sweep() {
		long	prevReferenceAddress = address;
		long	currentFreeBlock = load8(prevReferenceAddress);
		long	currentUsedBlock = address + JJ.QWORD_SIZE;
		long	currentAddress = address;
		
		while (currentUsedBlock < address + currentMaximum) {	// include free content into free chain
			long	length =  load8(currentUsedBlock);
			
			if ((length & 0x8000000000000000L) != 0) {
				length &= 0x7FFFFFFFFFFFFFFFL;
				
				while (currentUsedBlock > currentFreeBlock) {
					prevReferenceAddress = currentFreeBlock;
					currentFreeBlock = load8(prevReferenceAddress);
				}
				store8(currentUsedBlock, currentFreeBlock);
				store8(currentUsedBlock + JJ.QWORD_SIZE, length);
				store8(prevReferenceAddress, currentUsedBlock);
			}
			currentUsedBlock += length;
		}
		while (currentAddress >= 0) {	// merge sequential free block to one big block 
			long	length = load8(currentAddress + JJ.QWORD_SIZE);
			
			while (currentAddress + length == load8(currentAddress)) {
				store8(currentAddress, load8(currentAddress + length));
				store8(currentAddress + JJ.QWORD_SIZE, length + load8(currentAddress + length + JJ.QWORD_SIZE));
			}
			currentAddress = load8(currentAddress);
		}
	}
	
	public void clearMarks() {
		long	currentFreeBlock = load8(address);
		long	currentUsedBlock = address + JJ.QWORD_SIZE;
		
		while (currentUsedBlock < address + currentMaximum) {
			long	length =  load8(currentUsedBlock);
			
			store8(currentUsedBlock, length | 0x8000000000000000L);
			currentUsedBlock += length;
			
			while (currentUsedBlock == currentFreeBlock) {
				currentUsedBlock += load8(currentFreeBlock + JJ.QWORD_SIZE);
				currentFreeBlock = load8(currentFreeBlock);
			}
		}
		
	}

	public void setMark(final long address) {
		store8(address - JJ.QWORD_SIZE, load8(address - JJ.QWORD_SIZE) & 0x7FFFFFFFFFFFFFFFL);
	}
	
	public long allocateObject(final long classRef, final int size) {
		final long	addr = allocate(size + 2 * JJ.QWORD_SIZE);

		store8(addr, 0);
		store8(addr + JJ.QWORD_SIZE, classRef);
		return addr + 2 * JJ.QWORD_SIZE;
	}

	public long allocateArray(final long classRef, final int arrayType, final int length) {
		final long	addr;
		
		switch (arrayType) {
			case 0 	:
				addr = allocate(length* JJ.QWORD_SIZE + 2 * JJ.QWORD_SIZE);
				break;
			default :
				throw new Error();
		}
		store8(addr, length);
		store8(addr + JJ.QWORD_SIZE, classRef);
		return addr + 2 * JJ.QWORD_SIZE;
	}

	long load8(final long address) {
		return JJ.load8(address);
	}

	void store8(final long address, final long value) {
		JJ.store8(address, value);
	}
	
	synchronized long allocate(final long size) {
		long	actualSize = ((size + 2 * JJ.QWORD_SIZE - 1) >> 4) << 4;
		long	prevReferenceAddress = address;
		long	currentAddr = load8(prevReferenceAddress);
		long	currentSize = load8(prevReferenceAddress + JJ.QWORD_SIZE);
		
		while (currentAddr > 0 && currentSize < actualSize + 2 * JJ.QWORD_SIZE) {
			prevReferenceAddress = currentAddr;
			currentAddr = load8(prevReferenceAddress);
			currentSize = load8(prevReferenceAddress + JJ.QWORD_SIZE);
		}
		if (currentAddr <= 0) {
			return -1;
		}
		else {
			long	newAddress = currentAddr + actualSize;
			
			store8(prevReferenceAddress, newAddress);
			store8(newAddress, currentAddr);
			store8(newAddress + JJ.QWORD_SIZE, currentSize - actualSize);
			store8(currentAddr, actualSize);
			for(long index = 0, maxIndex = actualSize - 2 * JJ.QWORD_SIZE; index < maxIndex; index += JJ.QWORD_SIZE) {
				store8(currentAddr + JJ.QWORD_SIZE + index, 0); 
			}
			store8(currentAddr + actualSize - JJ.QWORD_SIZE, actualSize);
			return currentAddr + JJ.QWORD_SIZE;
		}
	}
}
