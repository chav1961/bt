package chav1961.bt.openclmatrix.internal;

import java.io.File;
import java.nio.ByteBuffer;

public class InternalUtils {
	public static final String	OPENCL_PREFIX = "OpenCL_";
	public static final File	TEMP_DIR_LOCATION = new File(System.getProperty("java.io.tmpdir"));
	

	static int toInt(final byte[] buf, final int pos) {
		final int result = ((buf[pos+3] << 24) + (buf[pos+2] << 16) + (buf[pos+1] << 8) + (buf[pos+0] << 0));
		
		return result;
	}

	static long toLong(final byte[] buf, final int pos) {
        return (((long)buf[pos] << 56) +
                ((long)(buf[pos+1] & 255) << 48) +
                ((long)(buf[pos+2] & 255) << 40) +
                ((long)(buf[pos+3] & 255) << 32) +
                ((long)(buf[pos+4] & 255) << 24) +
                ((buf[pos+5] & 255) << 16) +
                ((buf[pos+6] & 255) <<  8) +
                ((buf[pos+7] & 255) <<  0));
	}

	static void fromLong(final byte[] buffer, final int from, final long value) {
        buffer[from] = (byte)(value >>> 56);
        buffer[from+1] = (byte)(value >>> 48);
        buffer[from+2] = (byte)(value >>> 40);
        buffer[from+3] = (byte)(value >>> 32);
        buffer[from+4] = (byte)(value >>> 24);
        buffer[from+5] = (byte)(value >>> 16);
        buffer[from+6] = (byte)(value >>>  8);
        buffer[from+7] = (byte)(value >>>  0);
	}

	static void fromInt(final byte[] buffer, final int from, final int value) {
        buffer[from] = (byte)(value >>> 24);
        buffer[from+1] = (byte)(value >>> 16);
        buffer[from+2] = (byte)(value >>>  8);
        buffer[from+3] = (byte)(value >>>  0);
	}
}
