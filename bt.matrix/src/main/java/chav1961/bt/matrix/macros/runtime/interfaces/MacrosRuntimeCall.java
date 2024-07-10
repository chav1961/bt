package chav1961.bt.matrix.macros.runtime.interfaces;

import chav1961.purelib.basic.exceptions.CalculationException;
import chav1961.purelib.basic.exceptions.ContentException;

@FunctionalInterface
public interface MacrosRuntimeCall {
	Value process(MacrosRuntime rt, Value... parameters) throws ContentException, CalculationException;
}
