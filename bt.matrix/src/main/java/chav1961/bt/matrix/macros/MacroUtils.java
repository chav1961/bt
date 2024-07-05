package chav1961.bt.matrix.macros;

public class MacroUtils {
	public static boolean equals(final char[] source, final int from, final char[] template) {
		for(int index = 0; index < template.length; index++) {
			if (source[from + index] != template[index]) {
				return false;
			}
		}
		return true;
	}
	
	public static int compareTo(final char[] source, final int from, final int len, final char[] template) {
		final int	minLen = Math.min(len, template.length);
		
		for(int index = 0; index < minLen; index++) {
			final int	delta = source[from + index] - template[index]; 
			
			if (delta != 0) {
				return delta;
			}
		}
		return len - template.length;
	}
}
