package chav1961.bt.mnemort.entities.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public interface EntityInterface<Cargo> {
	public interface WalkerCallback<Cargo> {
		ContinueMode walkDown(NodeEnterMode mode, EntityInterface<Cargo> node) throws ContentException;
	}
	
	ContinueMode walkDown(WalkerCallback<Cargo> callback) throws ContentException;
	Cargo getCargo();
}
