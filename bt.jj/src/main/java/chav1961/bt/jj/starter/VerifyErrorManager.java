package chav1961.bt.jj.starter;

public class VerifyErrorManager {
	static final String	NO_COMMENT = "";
	
	static final String	ERR_ILLEGAL_MAGIC = "Illegal MAGIC [0x%1$08x], [0x%2$08x] awaited";
	static final String	ERR_VERSION_TOO_NEW = "Class file version [%1$s] is too new to support, max supported is [%2$s]";
	static final String	ERR_NON_EXISTENT_REF_CP = "Constant pool entry refers to non-existent constant pool entry [%1$d]";
	static final String	ERR_INVALID_REF_CP = "Illegal constant pool entry reference index [%1$d] (%2$s awaited)"; 
	static final String	ERR_UNSUPPORTED_CONSTANT_POOL_ITEM_TYPE = "Unsupported constant pool item type [%1$d]";	
	static final String	ERR_INVALID_METHOD_HANDLE_KIND_CP = "Constant pool entry contains reference kind [%1$d] out of range 1..9"; 
	static final String	ERR_INVALID_NAME_CP = "Invalid entity name [%1$s]"; 
	static final String	ERR_INVALID_CLASS_OR_METHOD_SIGNATURE_CP = "Invalid class or method signature [%1$s]"; 
	static final String	ERR_EXTRA_ACCESS_FLAGS = "Extra access flags [%1$x] for entity, only [%2$x] are available"; 
	static final String	ERR_INCOMPATIBLE_ACCESS_FLAGS = "Incompatible access flags [%1$x] for entity"; 
	static final String	ERR_DUPLICATE_ATTRIBUTE = "Duplicate attribute [%1$s] in attribute list"; 
	static final String	ERR_INVALID_TAG_STACKMAPTABLE = "Invalid attribute tag [%1$x]";
	static final String	ERR_UNSUPPORTED_TAG_STACKMAPTABLE = "Unsupported attribute tag [%1$x]";
	static final String	ERR_NON_ZERO_OUTER_CLASS = "Non-null outer class ref for null inner name ref";
	static final String	ERR_INVALID_START_PC_EXCEPTIONS = "Start PC [%1$d] out of range 0..%2$d"; 
	static final String	ERR_INVALID_END_PC_EXCEPTIONS = "End PC [%1$d] out of range 0..%2$d";
	static final String	ERR_INVALID_HANDLER_PC_EXCEPTIONS = "Handler PC [%1$d] out of range 0..%2$d";
	static final String	ERR_END_PC_LESS_START_PC_EXCEPTIONS = "Start PC [%1$d] must be less end PC [%2$d]";

	private static final String	FMT_PATH = "Path: %1$s\n";
	private static final String	FMT_OFFSET = "Offset: %1$06x (%2$s)\n";
	
	private static final int	MAX_NESTING = 64;
	private static final byte	TYPE_INDEX = 0;
	private static final byte	TYPE_REF = 1;
	private static final byte	TYPE_NAME = 2;
	
	private final byte[]		types = new byte[MAX_NESTING]; 
	private final int[]			refs = new int[MAX_NESTING]; 
	private final String[]		names = new String[MAX_NESTING]; 
	private ConstantPool 		pool = null;
	private int					depth = -1;
	
	public void setConstantPool(final ConstantPool pool) {
		if (pool == null) {
			throw new NullPointerException("Constant pool to set can't be null");
		}
		else {
			this.pool = pool;
		}
	}
	
	public void pushIndices() {
		if (depth >= MAX_NESTING - 1) {
			throw new IllegalStateException("Path stack overflow");
		}
		else {
			types[++depth] = TYPE_INDEX;
		}
	}
	
	public void pushNames() {
		if (depth >= MAX_NESTING - 1) {
			throw new IllegalStateException("Path stack overflow");
		}
		else {
			types[++depth] = TYPE_REF;
		}
	}
	
	public void pushSection(final String section) {
		if (section == null || section.isEmpty()) {
			throw new IllegalArgumentException("Section name can't be null or empty");
		}
		else if (depth >= MAX_NESTING - 1) {
			throw new IllegalStateException("Path stack overflow");
		}
		else {
			types[++depth] = TYPE_NAME;
		}
	}
	
	public void setIndex(final int index) {
		refs[depth] = index;
	}
	
	public void setName(final int name) {
		refs[depth] = name;
	}
	
	public void setSection(final String section) {
		if (section == null || section.isEmpty()) {
			throw new IllegalArgumentException("Section name can't be null or empty");
		}
		else {
			names[depth] = section;		
		}
	}
	
	public void pop() {
		if (depth <= 0) {
			throw new IllegalStateException("Path stack exhausted");
		}
		else {
			depth--;
		}
	}

	public VerifyError buildError(final int offset, final String errorId, final String comment, final Object... parameters) {
		if (errorId == null || errorId.isEmpty()) {
			throw new IllegalArgumentException("Error id can't be null or empty");
		}
		else if (comment == null) {
			throw new IllegalArgumentException("Comment can't be null");
		}
		else {
			final StringBuilder	sb = new StringBuilder();
			
			sb.append(String.format(FMT_PATH, buildPath()));
			sb.append(String.format(FMT_OFFSET, offset, comment));
			sb.append(String.format(errorId, parameters));
			return new VerifyError(sb.toString());
		}
	}

	private String buildPath() {
		final StringBuilder	sb = new StringBuilder();
		
		for (int index = 0; index <= depth; index++) {
			sb.append('/');
			switch (types[index]) {
				case TYPE_INDEX	:
					sb.append('[').append(refs[index]).append(']');
					break;
				case TYPE_REF	:
					if (pool != null) {
						sb.append(pool.deepToString(refs[index]));
					}
					else {
						sb.append('#').append(refs[index]);
					}
					break;
				case TYPE_NAME	:
					sb.append(names[index]);
					break;
				default :
					sb.append('*');
			}
		}
		return sb.toString();
	}
}
