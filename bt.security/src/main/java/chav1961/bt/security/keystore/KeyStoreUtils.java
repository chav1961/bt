package chav1961.bt.security.keystore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import chav1961.bt.security.interfaces.KeyStoreEntryType;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.growablearrays.GrowableByteArray;

public class KeyStoreUtils {
	private static final Options[]	DUMMY_OPTIONS = new Options[0];
	
	public static enum Options {
		USE_HEX_ENCODING,
		USE_BASE64_ENCODING,
		USE_PRIVATE_KEY,
		USE_PUBLIC_KEY
	}
	
	public static KeyStoreEntryType detectKeyStoreEntryTypeByFileName(final String fileName) {
		if (Utils.checkEmptyOrNullString(fileName)) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else {
			for (KeyStoreEntryType item : KeyStoreEntryType.values()) {
				if (item.getFileMaskSupported().matcher(fileName).matches()) {
					return item;
				}
			}
			return null;
		}
	}

	public static Options[] detectKeyStoreEntryOptionsByFileName(final String fileName) {
		if (Utils.checkEmptyOrNullString(fileName)) {
			throw new IllegalArgumentException("File name can't be null or empty"); 
		}
		else {
			return DUMMY_OPTIONS;
		}
	}
	
	public static KeyStoreEntry loadKeyStoreEntry(final byte[] content, final KeyStoreEntryType type, final Options... options) throws IOException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("COntent can't be null or empty array"); 
		}
		else if (type == null) {
			throw new NullPointerException("Key store entry type can't be null"); 
		}
		else if (options == null || Utils.checkArrayContent4Nulls(options) > 0) {
			throw new IllegalArgumentException("Options are null or contains nulls inside"); 
		}
		else {
			switch (type) {
				case CERTIFICATE	:
					break;
				case DES_KEY		:
					break;
				case RSA_KEY		:
					break;
				default:
					throw new UnsupportedOperationException("Entry type ["+type+"] is not supported yet"); 
			}
			return null;
		}
	}
	
	public static KeyStoreEntry loadKeyStoreEntry(final InputStream is, final KeyStoreEntryType type, final Options... options) throws IOException {
		if (is == null) {
			throw new NullPointerException("Input stream can't be null"); 
		}
		else if (type == null) {
			throw new NullPointerException("Key store entry type can't be null"); 
		}
		else if (options == null || Utils.checkArrayContent4Nulls(options) > 0) {
			throw new IllegalArgumentException("Options are null or contains nulls inside"); 
		}
		else {
			return loadKeyStoreEntry(new GrowableByteArray(false).append(is).extract(), type, options);
		}
	}

	public static KeyStoreEntry loadKeyStoreEntry(final File f, final KeyStoreEntryType type, final Options... options) throws IOException {
		if (f == null) {
			throw new IllegalArgumentException("File to load key store entry can't be null"); 
		}
		else if (!f.exists() || !f.isFile() || !f.canRead()) {
			throw new IllegalArgumentException("File ["+f.getAbsolutePath()+"] is not exists, is not a file or doesn't have read access for you"); 
		}
		else if (type == null) {
			throw new NullPointerException("Key store entry type can't be null"); 
		}
		else if (options == null || Utils.checkArrayContent4Nulls(options) > 0) {
			throw new IllegalArgumentException("Options are null or contains nulls inside"); 
		}
		else {
			final Set<Options>	totalSet = new HashSet<>(Arrays.asList(detectKeyStoreEntryOptionsByFileName(f.getName()))); 
					
			totalSet.addAll(Arrays.asList(options));
			try(final InputStream	is = new FileInputStream(f)) {
				return loadKeyStoreEntry(new GrowableByteArray(false).append(is).extract(), type, totalSet.toArray(new Options[totalSet.size()]));
			}
		}
	}
	
	public static byte[] unloadKeyStoreEntry(final KeyStoreEntry entry, final Options... options) throws IOException {
		if (entry == null) {
			throw new NullPointerException("Key store entry can't be null"); 
		}
		else if (options == null || Utils.checkArrayContent4Nulls(options) > 0) {
			throw new IllegalArgumentException("Options are null or contains nulls inside"); 
		}
		else {
			switch (entry.getEntryType()) {
				case CERTIFICATE	:
					break;
				case DES_KEY		:
					break;
				case RSA_KEY		:
					break;
				default:
					throw new UnsupportedOperationException("Entry type ["+entry.getEntryType()+"] is not supported yet"); 
			}
			return null;
		}
	}

	public static void unloadKeyStoreEntry(final KeyStoreEntry entry, final OutputStream os, final Options... options) throws IOException {
		if (entry == null) {
			throw new NullPointerException("Key store entry can't be null"); 
		}
		else if (os == null) {
			throw new NullPointerException("Output stream can't be null"); 
		}
		else if (options == null || Utils.checkArrayContent4Nulls(options) > 0) {
			throw new IllegalArgumentException("Options are null or contains nulls inside"); 
		}
		else {
			os.write(unloadKeyStoreEntry(entry, options));
		}
	}

	public static void unloadKeyStoreEntry(final KeyStoreEntry entry, final File f, final Options... options) throws IOException {
		if (entry == null) {
			throw new NullPointerException("Key store entry can't be null"); 
		}
		else if (f == null) {
			throw new NullPointerException("File to store entry can't be null"); 
		}
		else if (f.isDirectory() || f.isFile() && !f.canWrite()) {
			throw new IllegalArgumentException("File to store entry is a directory or doesn't have write access for you"); 
		}
		else if (options == null || Utils.checkArrayContent4Nulls(options) > 0) {
			throw new IllegalArgumentException("Options are null or contains nulls inside"); 
		}
		else {
			final Set<Options>	totalSet = new HashSet<>(Arrays.asList(detectKeyStoreEntryOptionsByFileName(f.getName()))); 
			
			totalSet.addAll(Arrays.asList(options));
			final Options[]		total = totalSet.toArray(new Options[totalSet.size()]);
			
			try(final OutputStream	os = new FileOutputStream(f)) {
				unloadKeyStoreEntry(entry, os, total);
			}
		}
	}
}
