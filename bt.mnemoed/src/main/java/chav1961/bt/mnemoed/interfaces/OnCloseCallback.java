package chav1961.bt.mnemoed.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

@FunctionalInterface
public interface OnCloseCallback {
	public void onClose() throws ContentException;
}
