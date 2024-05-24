package chav1961.bt.installbuilder.tools;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import chav1961.bt.installbuilder.Executor;
import chav1961.bt.installbuilder.utils.InstallbuilderUtils;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.Utils;

public class Repository {
	private static final String	IMAGES_DIR = "/images/";
	
	private URL					pureLibLocation = null;
	private URL					installerLocation = null;
	private Settings			settings = null;
	private Parameters			parameters = null;
	private String				strings = null;
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

	public void addPureLibLocation(final URL location) {
		if (location == null) {
			throw new NullPointerException("PureLib location to add can't be null");
		}
		else {
			this.pureLibLocation = location;
		}
	}

	public void addInstallerLocation(final URL location) {
		if (location == null) {
			throw new NullPointerException("Installer location to add can't be null");
		}
		else {
			this.installerLocation = location;
		}
	}
	
	public void addLocalizingStrings(final String strings) {
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
			dump(jos, InstallbuilderUtils.class2JarEntryName(Executor.class), InstallbuilderUtils.class2URL(Executor.class));
			if (settings != null) {
				dump(jos, Executor.SETTINGS_RESOURCE_NAME, settings.getInputStream());
			}
			if (strings != null) {
				dump(jos, Executor.LOCALIZING_STRINGS_RESOURCE_NAME, new ByteArrayInputStream(strings.getBytes(PureLibSettings.DEFAULT_CONTENT_ENCODING)));
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
			try(final InputStream	is = pureLibLocation.openStream()) {
				dump(jos, Executor.PURELIB_NAME, is);
			}
			try(final InputStream	is = installerLocation.openStream()) {
				dump(jos, Executor.INSTALLER_NAME, is);
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
