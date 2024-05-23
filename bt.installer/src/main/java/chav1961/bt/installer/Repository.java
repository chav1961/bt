package chav1961.bt.installer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import chav1961.bt.installer.executor.Executor;
import chav1961.bt.installer.tools.ImagesCollection;
import chav1961.bt.installer.tools.LocalizingStrings;
import chav1961.bt.installer.tools.Parameters;
import chav1961.bt.installer.tools.Settings;
import chav1961.bt.installer.tools.SplashScreenKeeper;
import chav1961.bt.installer.utils.InstallerUtils;
import chav1961.purelib.basic.Utils;

public class Repository {
	private static final String	IMAGES_DIR = "/images/";
	
	private Settings			settings = null;
	private Parameters			parameters = null;
	private LocalizingStrings	strings = null;
	private ImagesCollection	imagesCollection = null;
	private SplashScreenKeeper	splash = null;
	private String				comment = null;
	
	public Repository() {
		
	}

	public void addSettings(final Settings settings) {
		if (settings == null) {
			throw new NullPointerException("Settings to add can't be null");
		}
		else {
			this.settings = settings;
		}
	}
	
	public void addParameters(final Parameters parameters) {
		if (parameters == null) {
			throw new NullPointerException("Parameters to add can't be null");
		}
		else {
			this.parameters = parameters;
		}
	}

	public void addLocalizingStrings(final LocalizingStrings strings) {
		if (strings == null) {
			throw new NullPointerException("Localizing string to add can't be null");
		}
		else {
			this.strings = strings;
		}
	}
	
	public void addImagesCollection(final ImagesCollection collection) {
		if (collection == null) {
			throw new NullPointerException("Images colection to add can't be null");
		}
		else {
			this.imagesCollection = collection;
		}
	}
	
	public void addSplashScreen(final SplashScreenKeeper keeper) {
		if (keeper == null) {
			throw new NullPointerException("Splasl screen to add can't be null");
		}
		else {
			this.splash = keeper;
		}
	}

	public Parameters getParameters() {
		return parameters;
	}
	
	public Manifest getManifest() {
		final Manifest	m = new Manifest();
		
		m.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
		m.getMainAttributes().put(Attributes.Name.MAIN_CLASS, Executor.class.getCanonicalName());
		if (splash != null) {
			m.getMainAttributes().put(new Attributes.Name("SplashScreen-Image"), splash.getSplashScreenName());
		}
		
		return m;
	}
	
	public void setComment(final String comment) {
		if (Utils.checkEmptyOrNullString(comment)) {
			throw new IllegalArgumentException("Comment string to set can't be null or empty");
		}
		else {
			this.comment = comment;
		}
	}
	
	public void upload(final JarOutputStream jos) throws IOException {
		if (jos == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else {
			if (comment != null) {
				jos.setComment(comment);
			}
			dump(jos, InstallerUtils.class2JarEntryName(Executor.class), InstallerUtils.class2URL(Executor.class));
			if (settings != null) {
				dump(jos, Executor.class.getPackageName().replace('.','/')+'/'+Executor.SETTINGS_RESOURCE_NAME, settings.getInputStream());
			}
			if (strings != null) {
				dump(jos, Executor.class.getPackageName().replace('.','/')+'/'+Executor.LOCALIZING_STRINGS_RESOURCE_NAME, strings.getInputStream());
			}
			if (splash != null) {
				try(final InputStream	is = splash.getInputStream()) {
					dump(jos, splash.getSplashScreenName(), is);
				}
			}
			if (imagesCollection != null) {
				for(String item : imagesCollection.names()) {
					dump(jos, IMAGES_DIR+item, imagesCollection.getInputStream(item));
				}
			}
		}
	}

	private void dump(final JarOutputStream jos, final String entryName, final URL content) throws IOException {
		try(final InputStream	is = content.openStream()) {
			dump(jos, entryName, is);
		}
	}
	
	private void dump(final JarOutputStream jos, final String entryName, final InputStream content) throws IOException {
		final JarEntry	je = new JarEntry(entryName);
		
		je.setMethod(ZipEntry.DEFLATED);
		jos.putNextEntry(je);
		Utils.copyStream(content, jos);
		jos.closeEntry();
	}
}
