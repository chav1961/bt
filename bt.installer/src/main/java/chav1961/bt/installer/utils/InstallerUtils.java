package chav1961.bt.installer.utils;

import java.net.URL;

public class InstallerUtils {
	public static String class2JarEntryName(final Class<?> clazz) {
		if (clazz == null) {
			throw new NullPointerException("Class to get entry name can't be null or empty");
		}
		else {
			return clazz.getName().replace('.', '/')+".class";
		}
	}
	
	public static URL class2URL(final Class<?> clazz) {
		if (clazz == null) {
			throw new NullPointerException("Class to get entry name can't be null or empty");
		}
		else {
			return clazz.getResource(clazz.getSimpleName()+".class");
		}
	}
}
