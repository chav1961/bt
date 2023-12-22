package chav1961.bt.jj.starter;

import java.util.Arrays;

public class AnnotationItem {
	private final int				type;
	private final AnnotationValue[]	values;
	
	public AnnotationItem(final int type, final AnnotationValue... values) {
		this.type = type;
		this.values = values;
	}

	public int getType() {
		return type;
	}

	public AnnotationValue[] getValues() {
		return values;
	}

	@Override
	public String toString() {
		return "AnnotationItem [type=" + type + ", values=" + Arrays.toString(values) + "]";
	}

	public static class AnnotationValue {
		private final char	type;
		private final int	ref1;
		private final int	ref2;
		private final AnnotationValue[]	values;
		
		public AnnotationValue(final char type, final int ref) {
			this.type = type;
			this.ref1 = ref;
			this.ref2 = -1;
			this.values = null;
		}

		public AnnotationValue(final char type, final int ref1, final int ref2) {
			this.type = type;
			this.ref1 = ref1;
			this.ref2 = ref2;
			this.values = null;
		}

		public AnnotationValue(final char type, final AnnotationValue... refs) {
			this.type = type;
			this.ref1 = -1;
			this.ref2 = -1;
			this.values = refs;
		}

		public char getType() {
			return type;
		}

		public int getRef1() {
			return ref1;
		}

		public int getRef2() {
			return ref2;
		}

		public AnnotationValue[] getValues() {
			return values;
		}

		@Override
		public String toString() {
			return "AnnotationValue [type=" + type + ", ref1=" + ref1 + ", ref2=" + ref2 + ", values=" + Arrays.toString(values) + "]";
		}
	}
}
