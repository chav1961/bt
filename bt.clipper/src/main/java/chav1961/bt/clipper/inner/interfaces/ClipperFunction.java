package chav1961.bt.clipper.inner.interfaces;

public interface ClipperFunction extends ClipperExecutableValue, Iterable<ClipperParameter>, ClipperIdentifiedValue {
	boolean isProcedure();
	int getMaxParameterCount();
	int getMinParameterCount();
	ClipperParameter[] getParameters();
	ClipperParameter getParameter(int parameterNo);
	ClipperParameter getReturnType();
}
