package chav1961.bt.clipper.inner.interfaces;

public interface StackFrame extends Cloneable {
	public static enum Location {
		LOCAL,
		PRIVATE,
		PARAMETER,
		NONLOCAL,
		UNKNOWN
	}
	
	StackFrame getParent();
	int getFileAssociated();
	int getLineAssociated();
	ClipperFunction getFunctionAssociated();
	StackFrame clone() throws CloneNotSupportedException;
	
	ClipperParameter[] getLocalDeclarations();
	ClipperParameter[] getPrivateDeclarations();
	ClipperParameter[] getParametersDeclarations();
	ClipperParameter getLocalEntity(int number);
	ClipperParameter getEntity(long id);
	Location getEntityLocation(long id);
	ClipperValue getValueAssociated(ClipperParameter parameter);
}
