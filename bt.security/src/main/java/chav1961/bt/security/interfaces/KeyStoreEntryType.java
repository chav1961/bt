package chav1961.bt.security.interfaces;

import java.util.regex.Pattern;

public enum KeyStoreEntryType {
	RSA_KEY(".*"),
	DES_KEY(".*"),
	CERTIFICATE(".*"),
	UNKNOWN("");
	
	private final Pattern	fileMaskSupported;
	
	private KeyStoreEntryType(final String fileMaskSupported) {
		this.fileMaskSupported = Pattern.compile(fileMaskSupported);
	}
	
	public Pattern getFileMaskSupported() {
		return fileMaskSupported;
	}
}
