package chav1961.bt.clipper.interfaces;

public interface ClipperFunction extends ClipperExecutableValue, Iterable<ClipperParameter>, ClipperIdentifiedValue {
	boolean isProcedure();
	int getMaxParameterCount();
	int getMinParameterCount();
	ClipperParameter getParameter(int parameterNo);
	ClipperParameter getReturnType();
}
