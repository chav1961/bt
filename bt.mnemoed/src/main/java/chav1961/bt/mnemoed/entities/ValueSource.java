package chav1961.bt.mnemoed.entities;

import chav1961.bt.mnemoed.interfaces.JsonSerialzable;

public abstract class ValueSource implements JsonSerialzable {
	private final ValueSourceType	type;
	
	ValueSource(final ValueSourceType type) {
		this.type = type;
	}
	
	public ValueSourceType getSourceType() {
		return type;
	}
}
