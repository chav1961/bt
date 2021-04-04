package chav1961.bt.model;

import java.io.Serializable;

public class ModelEntity<Attr extends Enum<?>> implements Serializable {
	private static final long serialVersionUID = 4604064843265633337L;

	public <T> ModelEntity<Attr> addAttribute(Attr key, T value) {
		return this;
	}
	
	public <T> T getAttribute(Attr key) {
		return null;
	}

	public <T> T removeAttribute(Attr key) {
		return null;
	}
	
	public boolean containsAttribute(Attr key) {
		return false;
	}
}
