package chav1961.bt.mnemoed.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import chav1961.purelib.basic.subscribable.Subscribable;

class SubscribableEntity {
	private Map<String,Subscribable<?>>	items = new HashMap<>();
	
	public SubscribableEntity() {
		
	}
	
	public Set<String> getItemNames() {
		return items.keySet(); 
	}
	
	public Subscribable<?> getItem(final String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (!items.containsKey(name)) {
			throw new IllegalArgumentException("Name ["+name+"] is missing in th entity"); 
		}
		else {
			return items.get(name);
		}
	}

	public Subscribable<?> setItem(final String name, final Subscribable<?> value) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name can't be null or empty"); 
		}
		else if (value == null) {
			throw new NullPointerException("Value to set can't be null or empty"); 
		}
		else {
			return items.put(name, value);
		}
	}
}
