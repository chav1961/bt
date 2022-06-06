package chav1961.bt.mnemort.entities.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

@FunctionalInterface
public interface StringSubscribableChangedListener {
	public static enum ChangedEventType {
		CHANGED;
	}

	public static enum ChangedValueType {
		CURRENT; 
	}
	
	void process(ChangedEventType event, ChangedValueType valueType, String oldValue, String newValue) throws ContentException;
}