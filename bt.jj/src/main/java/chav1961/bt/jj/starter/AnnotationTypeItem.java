package chav1961.bt.jj.starter;

import java.util.Arrays;

import chav1961.bt.jj.starter.AnnotationItem.AnnotationValue;

public class AnnotationTypeItem {
	private final int					type;
	private final AnnotationTypeValue[]	values;

	public AnnotationTypeItem(int type, AnnotationTypeValue... values) {
		this.type = type;
		this.values = values;
	}
	
	@Override
	public String toString() {
		return "AnnotationTypeItem [type=" + type + ", values=" + Arrays.toString(values) + "]";
	}

	public static class AnnotationTypeValue {
		private final byte	type;
		private final int 	ref1;
		private final int 	ref2;
		private final int 	typeIndex;
		private final LocalVarTypeDescriptor[] localDesc;
		private final TypePathDescriptor[] pathDesc;
		private final AnnotationValue[] values;

		public AnnotationTypeValue(final byte type, final int ref1, final int ref2, final LocalVarTypeDescriptor[] localDesc, final TypePathDescriptor[] pathDesc, final int typeIndex, final AnnotationValue[] values) {
			this.type = type;
			this.ref1 = ref1;
			this.ref2 = ref2;
			this.localDesc = localDesc;
			this.pathDesc = pathDesc;
			this.typeIndex = typeIndex;
			this.values = values;
		}

		public byte getType() {
			return type;
		}

		public int getRef1() {
			return ref1;
		}

		public int getRef2() {
			return ref2;
		}

		public int getTypeIndex() {
			return typeIndex;
		}

		public LocalVarTypeDescriptor[] getLocalDesc() {
			return localDesc;
		}

		public TypePathDescriptor[] getPathDesc() {
			return pathDesc;
		}

		public AnnotationValue[] getValues() {
			return values;
		}

		@Override
		public String toString() {
			return "AnnotationTypeValue [type=" + type + ", ref1=" + ref1 + ", ref2=" + ref2 + ", typeIndex="
					+ typeIndex + ", localDesc=" + Arrays.toString(localDesc) + ", pathDesc="
					+ Arrays.toString(pathDesc) + ", values=" + Arrays.toString(values) + "]";
		}
	}
	
	public static class LocalVarTypeDescriptor {
		private final int 	startPC;
		private final int 	length;
		private final int 	index;
		
		public LocalVarTypeDescriptor(final int startPC, final int length, final int index) {
			this.startPC = startPC;
			this.length = length;
			this.index = index;
		}

		public int getStartPC() {
			return startPC;
		}

		public int getLength() {
			return length;
		}

		public int getIndex() {
			return index;
		}

		@Override
		public String toString() {
			return "LocalVarTypeDescriptor [startPC=" + startPC + ", length=" + length + ", index=" + index + "]";
		}
	}
	
	public static class TypePathDescriptor {
		private final int	pathKind;
		private final int	argumentIndex;
		
		public TypePathDescriptor(final int pathKind, final int argumentIndex) {
			this.pathKind = pathKind;
			this.argumentIndex = argumentIndex;
		}

		public int getPathKind() {
			return pathKind;
		}

		public int getArgumentIndex() {
			return argumentIndex;
		}

		@Override
		public String toString() {
			return "TypePathDescriptor [pathKind=" + pathKind + ", argumentIndex=" + argumentIndex + "]";
		}
	}
}
