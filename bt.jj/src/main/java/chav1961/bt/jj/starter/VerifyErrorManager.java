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
	
	
	
	
//	static final String	ERR_NON_EXISTENT_REF_THIS_CLASS = "THIS CLASS item referes to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_THIS_CLASS = "Illegal constant pool entry [%1$d] for THIS CLASS item (CONSTANT_Class awaited)"; 
//	static final String	ERR_NON_EXISTENT_REF_SUPER_CLASS = "SUPER CLASS item referes to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_SUPER_CLASS = "Illegal constant pool entry [%1$d] for SUPER CLASS item (CONSTANT_Class awaited)"; 
//	static final String	ERR_NON_EXISTENT_REF_INTERFACE = "INTERFACE item at index [%1$d] referes to non-existent constant pool entry [%2$d]";
//	static final String	ERR_INVALID_REF_INTERFACE = "Illegal constant pool entry [%1$d] for INTERFACE item at index [%2$d] (CONSTANT_Class awaited)"; 
//	static final String	ERR_NON_EXISTENT_REF_FIELD = "FIELD item at index [%1$d] refers to non-existent constant pool entry [%2$d]";
//	static final String	ERR_INVALID_REF_FIELD = "Illegal constant pool entry [%1$d] for FIELD item at index [%2$d] (CONSTANT_Utf8 awaited)"; 
//	static final String	ERR_INVALID_NAME_FIELD = "Illegal constant pool entry [%1$d] for FIELD item at index [%2$d] (invalid field name '%3$s')"; 
//	static final String	ERR_INVALID_SIGNATURE_FIELD = "Illegal constant pool entry [%1$d] for FIELD item at index [%2$d] (invalid field signature '%3$s')"; 
//	static final String	ERR_NON_EXISTENT_REF_METHOD = "METHOD item at index [%1$d] refers to non-existent constant pool entry [%2$d]";
//	static final String	ERR_INVALID_REF_METHOD = "Illegal constant pool entry [%1$d] for METHOD item at index [%2$d] (CONSTANT_Utf8 awaited)"; 
//	static final String	ERR_INVALID_NAME_METHOD = "Illegal constant pool entry [%1$d] for METHOD item at index [%2$d] (invalid method name '%3$s')"; 
//	static final String	ERR_INVALID_SIGNATURE_METHOD = "Illegal constant pool entry [%1$d] for METHOD item at index [%2$d] (invalid method signature '%3$s')"; 
//	static final String	ERR_NON_EXISTENT_REF_ATTRIBUTE = "ATTRIBUTE [%1$d/%2$d] refers to non-existent constant pool entry [%3$d]";
//	static final String	ERR_INVALID_REF_ATTRIBUTE = "Illegal constant pool entry [%1$d] for ATTRIBUTE item [%2$d/%3$d] (CONSTANT_Utf8 awaited)"; 
////	static final String	ERR_INVALID_NAME_CPE = "Constant pool entry for [%1$s] at index [%2$d] refers to constant pool entry [%3$d], contains invalid entity name [%4$s]"; 
//	static final String	ERR_INVALID_METHOD_HANDLE_KIND_CPE = "Constant pool entry for METHOD HANDLE at index [%1$d] contains reference kind [%2$d] out of range 1..9"; 
//	static final String	ERR_INVALID_METHOD_SIGNATURE_CPE = "Constant pool entry for [%1$s] at index [%2$d] refers to constant pool entry [%3$d] contains invalid method signature [%4$s]"; 
//	static final String	ERR_EXTRA_ACCESS_FLAGS = "Entity [%1$s] contains extra access flags [%2$x]"; 
//	static final String	ERR_DUPLICATE_ATTRIBUTE = "Entity [%1$s] with name [%2$s] contains duplicate attribute [%3$s]"; 
//	static final String	ERR_NOT_APPLICABLE_ATTRIBUTE = "Entity [%1$s] with name [%2$s] contains unapplicable attribute [%3$s]"; 
//	static final String	ERR_INVALIF_REF_FIELD_ATTR = "Entity [%1$s] with name [%2$s] and signature [%3$s] referenced to invalid constant pool entry [%4$d] ([%5$s] awaited)"; 
//	static final String	ERR_INVALID_CLASS_SIGNATURE_ENTITY = "Entity [%1$s] with name [%2$s] refers to constant pool entry [%3$d], contains invalid class signature [%4$s]"; 
//	static final String	ERR_NON_EXISTENT_REF_CONSTVALUE = "Constant pool entry for CONSTVALUE ATTRIBUTE refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_CONSTVALUE = "Constant pool entry for CONSTANTVALUE ATTRIBUTE refers to invalid constant pool entry [%1$d] (CONSTANT_Integer, CONSTANT_Long, CONSTANT_Float, CONSTANT_Double or CONSTANT_String awaited)";
//	static final String	ERR_NON_EXISTENT_REF_SIGNATURE = "Constant pool entry for SIGNATURE ATTRIBUTE refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_SIGNATURE = "Constant pool entry for SIGNATURE ATTRIBUTE refers to invalid constant pool entry [%1$d] (CONSTANT_UTF8 awaited)";
//	static final String	ERR_NON_EXISTENT_REF_SOURCEFILE = "SOURCEFILE ATTRIBUTE refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_SOURCEFILE = "Constant pool entry for SOURCEFILE ATTRIBUTE refers to invalid constant pool entry [%1$d] (CONSTANT_UTF8 awaited)";
//	static final String	ERR_NON_EXISTENT_REF_ANNOTATION_VALUE = "Constant pool entry for [%1$s] ATTRIBUTE at index [%2$d]/[%3$d] refers to non-existent constant pool entry [%4$d]";
//	static final String	ERR_INVALID_REF_ANNOTATION_VALUE = "Constant pool entry for [%1$s] ATTRIBUTE at index [%2$d]/[%3$d] refers to invalid constant pool entry [%4$d] (%5$s awaited)";
//	static final String	ERR_INVALID_REF_ANNOTATION_BOOL_VALUE = "Constant pool entry for [%1$s] ATTRIBUTE at index [%2$d]/[%3$d] refers to invalid constant pool entry [%4$d] (integer value [%5$d] is neither false (0) nor true (1))";
//	static final String	ERR_INVALID_REF_ANNOTATION_CLASS_SIGNATURE = "Constant pool entry for [%1$s] ATTRIBUTE at index [%2$d]/[%3$d] refers to invalid constant pool entry [%4$d] (invalid class signature '%5$s')";
//	static final String	ERR_INVALID_REF_ANNOTATION_NAME = "Constant pool entry for [%1$s] ATTRIBUTE at index [%2$d]/[%3$d] refers to invalid constant pool entry [%4$d] (invalid name '%5$s')";
//	static final String	ERR_INVALID_REF_ANNOTATION_TAG = "Constant pool entry for [%1$s] ATTRIBUTE at index [%2$d]/[%3$d] has invalid tag value '%4$c'";
//	static final String	ERR_INVALID_REF_ANNOTATION_TARGET_TYPE = "Constant pool entry for [%1$s] ATTRIBUTE at index [%2$d]/[%3$d] has invalid target type [%4$x]";
//	static final String	ERR_NON_EXISTENT_REF_METHOD_PARAMETERS = "Constant pool entry for METHOD_PARAMETERS refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_METHOD_PARAMETERS = "Constant pool entry for METHOD_PARAMETERS refers to invalid constant pool entry [%1$d] (CONSTANT_UTF8 awaited)";
//	static final String	ERR_INVALID_REF_METHOD_PARAMETERS_NAME = "Constant pool entry for METHOD_PARAMETERS refers to invalid constant pool entry [%1$d] (invalid name '%2$s')";
//	static final String	ERR_EXTRA_ACCESS_FLAGS_METHOD_PARAMETERS = "Constant pool entry for METHOD_PARAMETERS contains extra acccess flags [%1$x]";
//	static final String	ERR_NON_EXISTENT_REF_INNER_CLASSES = "INNER_CLASSES ATTRIBUTE refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_INNER_CLASSES = "INNER_CLASSES ATTRIBUTE refers to invalid constant pool entry [%1$d] (%2$s awaited)";
//	static final String	ERR_INVALID_NAME_REF_INNER_CLASSES = "INNER_CLASSES ATTRIBUTE refers to invalid constant pool entry [%1$d] (invalid name '%2$s')";
//	static final String	ERR_EXTRA_ACCESS_FLAGS_INNER_CLASSES = "Constant pool entry for INNER_CLASSES contains extra acccess flags [%1$x]";
//	static final String	ERR_NON_EXISTENT_REF_ENCLOSING_METHOD = "ENCLOSING_METHOD ATTRIBUTE refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_ENCLOSING_METHOD = "ENCLOSING_METHOD ATTRIBUTE refers to invalid constant pool entry [%1$d] (%2$s awaited)";
//	static final String	ERR_INVALID_START_PC_EXCEPTIONS = "CODE ATTRIBUTE has invalid exception table item at index [%1$d]: start_PC [%2$d] outside code array [%3$d]";
//	static final String	ERR_INVALID_END_PC_EXCEPTIONS = "CODE ATTRIBUTE has invalid exception table item at index [%1$d]: end_PC [%2$d] outside code array [%3$d]";
//	static final String	ERR_END_PC_LESS_START_PC_EXCEPTIONS = "CODE ATTRIBUTE has invalid exception table item at index [%1$d]: end_PC [%2$d] is less than start_PC [%3$d]";
//	static final String	ERR_INVALID_HANDLER_PC_EXCEPTIONS = "CODE ATTRIBUTE has invalid exception table item at index [%1$d]: handler_PC [%2$d] outside code array [%3$d]";
//	static final String	ERR_NON_EXISTENT_CATCH_EXCEPTIONS = "CODE ATTRIBUTE has invalid exception table item at index [%1$d]: catch refers to non-existent constant pool entry [%2$d]";
//	static final String	ERR_INVALID_CATCH_EXCEPTIONS = "CODE ATTRIBUTE has invalid exception table item at index [%1$d]: catch refers to invalid constant pool entry [%2$d] (CONSTANT_Class awaited)";
//	static final String	ERR_INVALID_TAG_STACKMAPTABLE = "STACKMAPTABLE ATTRIBUTE has invalid tag [%2$x] at index [%1$d]";
//	static final String	ERR_UNSUPPORTED_TAG_STACKMAPTABLE = "STACKMAPTABLE ATTRIBUTE has unsupported tag [%2$x] at index [%1$d]";
//	static final String	ERR_NON_EXISTENT_REF_EXCEPTIONS = "EXCEPTIONS ATTRIBUTE refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_EXCEPTIONS = "EXCEPTIONS ATTRIBUTE refers to invalid constant pool entry [%1$d] (CONSTANT_Class awaited)";
//	static final String	ERR_NON_EXISTENT_REF_LOCAL_VARIABLE = "LOCAL_VARIABLE ATTRIBUTE refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_LOCAL_VARIABLE = "LOCAL_VARIABLE ATTRIBUTE refers to invalid constant pool entry [%1$d] (%2$s awaited)";
//	static final String	ERR_NON_EXISTENT_REF_LOCAL_VARIABLE_TYPE = "LOCAL_VARIABLE_TYPE ATTRIBUTE refers to non-existent constant pool entry [%1$d]";
//	static final String	ERR_INVALID_REF_LOCAL_VARIABLE_TYPE = "LOCAL_VARIABLE_TYPE ATTRIBUTE refers to invalid constant pool entry [%1$d] (CONSTANT_Utf8 awaited)";
//	static final String	ERR_INVALID_SIGNATURE_LOCAL_VARIABLE_TYPE = "LOCAL_VARIABLE_TYPE ATTRIBUTE refers to invalid constant pool entry [%1$d] (invalid class signature '%2$s')";
//	static final String	ERR_NON_EXISTENT_REF_BOOTSTRAP_METHODS = "BOOTSTRAP_METHODS ATTRIBUTE at index [%1$d] refers to non-existent constant pool entry [%2$d]";
//	static final String	ERR_INVALID_REF_BOOTSTRAP_METHODS = "BOOTSTRAP_METHODS ATTRIBUTE at index [%1$d] refers to invalid constant pool entry [%2$d] (CONSTANT_MethofHandle awaited)";
//	static final String	ERR_NON_EXISTENT_ARG_REF_BOOTSTRAP_METHODS = "BOOTSTRAP_METHODS ATTRIBUTE at index [%1$d] - argument [%2$d] refers to non-existent constant pool entry [%3$d]";

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
