package chav1961.bt.openclmatrix.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryBuffer;
import chav1961.bt.openclmatrix.internal.GPUExecutor.TemporaryStore;

class TemporaryStoreImpl implements TemporaryStore {
	private final Consumer<TemporaryStore> 	onCloseCallback;
	private final List<TemporaryBuffer>		buffers = new ArrayList<>();
	private final File						file;
	private final FileChannel				fc;
	
	TemporaryStoreImpl(final File contentDir, final long size, final Consumer<TemporaryStore> onCloseCallback) throws IOException {
		this.onCloseCallback = onCloseCallback;
		this.file = File.createTempFile(InternalUtils.OPENCL_PREFIX+"TMP", ".matrix", contentDir);
		
		try(final RandomAccessFile	raf = new RandomAccessFile(file, "rwd")) {
			raf.seek(size-1);
			raf.writeByte(0);
		}
		this.fc = FileChannel.open(file.toPath(), StandardOpenOption.READ, StandardOpenOption.WRITE);
	}

	@Override
	public void close() throws IOException {
		onCloseCallback.accept(this);
		fc.close();
	}

	@Override
	public TemporaryBuffer getBuffer(long address, int size) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
