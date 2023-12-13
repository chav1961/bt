package chav1961.bt.jj.starter;

public class InternalUtils {
	public static void memcpy(final long from, final long to, final long len) {
		for(long index = 0; index < len; index++) {
			JJ.store1(to+index, JJ.load1(from+index));
		}
	}
	
	public static int compareTo(final char[] left, final char[] right) {
		for(int index = 0, maxIndex = left.length > right.length ? right.length : left.length; index < maxIndex; index++) {
			final int	delta = right[index] - left[index];
			
			if (delta != 0) {
				return delta;
			}
		}
		return right.length - left.length;
	}
}
