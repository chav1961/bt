package chav1961.bt.mnemort.entities.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;

@FunctionalInterface
public interface FloatSubscribableChangedListener {
	public static enum ChangedEventType {
		CHANGED, LOW_WARNING, HIGH_WARNING, LOW_ERROR, HIGH_ERROR, SPEED_WARNING, SPEED_ERROR;
	}

	public static enum ChangedValueType {
		MINIMUM, MAXIMUM, CURRENT, 
		LOW_WARNING_UP, LOW_WARNING_DOWN, HIGH_WARNING_UP, HIGH_WARNING_DOWN, 
		LOW_ERROR_UP, LOW_ERROR_DOWN, HIGH_ERROR_UP, HIGH_ERROR_DOWN,
		SPEED_WARNING_UP, SPEED_WARNING_DOWN, SPEED_ERROR_UP, SPEED_ERROR_DOWN;
	}
	
	void process(ChangedEventType event, ChangedValueType valueType, float oldValue, float newValue) throws ContentException;
}