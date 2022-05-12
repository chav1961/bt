package chav1961.bt.clipper.inner.interfaces;

public interface ClipperSyntaxEntity {
	public enum SyntaxEntityType {
		UNKNOWN,
		BUILTIN;
	}
	
	SyntaxEntityType getSyntaxEntityType();
	String getSyntaxEntityName();
	ClipperValue getValueAssociated();
}
