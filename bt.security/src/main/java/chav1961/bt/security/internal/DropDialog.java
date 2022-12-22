package chav1961.bt.security.internal;

import java.util.Arrays;

import chav1961.bt.security.interfaces.KeyStoreEntryType;

public class DropDialog implements AutoCloseable {
	public String				filePath;
	public String				aliasName;
	public KeyStoreEntryType	type;
	public char[]				password;
	public char[]				passwordRetyped;
	
	@Override
	public void close() throws RuntimeException {
		if (password != null) {
			Arrays.fill(password, ' ');
		}
		if (passwordRetyped != null) {
			Arrays.fill(passwordRetyped, ' ');
		}
	}
}
