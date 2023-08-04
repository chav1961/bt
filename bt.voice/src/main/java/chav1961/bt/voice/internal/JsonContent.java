package chav1961.bt.voice.internal;

import chav1961.purelib.basic.Utils;

public class JsonContent {
	public String partial;
	public String text;

	public String getContent() {
		if (Utils.checkEmptyOrNullString(text)) {
			if (Utils.checkEmptyOrNullString(partial)) {
				return "";
			}
			else {
				return partial;
			}
		}
		else {
			return text;
		}
	}
	
	@Override
	public String toString() {
		return "JsonContent [partial=" + partial + ", text=" + text + "]";
	}
}
