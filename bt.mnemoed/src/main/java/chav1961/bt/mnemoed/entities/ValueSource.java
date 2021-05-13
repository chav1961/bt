package chav1961.bt.mnemoed.entities;

import chav1961.bt.mnemoed.interfaces.JsonSerializable;

public abstract class ValueSource implements JsonSerializable {
	private final ValueSourceType	type;
	
	ValueSource(final ValueSourceType type) {
		this.type = type;
	}
	
	public ValueSourceType getSourceType() {
		return type;
	}
}
