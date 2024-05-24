package chav1961.bt.installbuilder;


import java.awt.SplashScreen;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

public class Executor {
	public static final String	PURELIB_NAME = "purelib.jar";
	public static final String	INSTALLER_NAME = "installer.jar";
	public static final String	SETTINGS_RESOURCE_NAME = "settings.props"; 
	public static final String	LOCALIZING_STRINGS_RESOURCE_NAME = "i18n.xml"; 

	public Executor(final Properties settings) {
		
	}
	
	public static void main(final String[] args) throws Throwable {
		final SplashScreen 	splash = SplashScreen.getSplashScreen();

		try(final URLClassLoader	loader = new URLClassLoader(new URL[] {extractJar(INSTALLER_NAME), extractJar(PURELIB_NAME)}, Thread.currentThread().getContextClassLoader())) {
			final Class<?>			cl = loader.loadClass("chav1961.bt.installer.Application");
			
			cl.getMethod("processMain", SplashScreen.class, String[].class).invoke(null, splash, args);
		}
	}
	
	private static URL extractJar(final String jarName) throws IOException {
		final File	dir = new File(System.getProperty("java.io.tmpdir"));
		final File	library = new File(dir, jarName);	

		library.deleteOnExit();
		try(final InputStream	is = Executor.class.getResourceAsStream("/"+jarName);
			final OutputStream	os = new FileOutputStream(library)) {
			final byte[]	buffer = new byte[8192];
			int	len;
			
			while ((len = is.read(buffer)) > 0) {
				os.write(buffer, 0, len);
			}
			os.flush();
		}
		return library.toURI().toURL();
	}
}
