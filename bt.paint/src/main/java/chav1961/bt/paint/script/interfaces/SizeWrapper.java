package chav1961.bt.paint.script.interfaces;

import java.awt.Dimension;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.SizeWrapperImpl;

public interface SizeWrapper extends ContentWrapper<Dimension> {
	Dimension getSize();
	SizeWrapper setSize(String size) throws PaintScriptException;
	SizeWrapper setSize(Dimension size) throws PaintScriptException;

	static SizeWrapper of(final Dimension size) {
		return new SizeWrapperImpl(size);
	}

	static SizeWrapper of(final String size) throws PaintScriptException {
		return new SizeWrapperImpl(size);
	}
}