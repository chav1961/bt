package chav1961.bt.installer.executor;

import java.awt.SplashScreen;
import java.io.IOException;
import java.util.Properties;

public class Executor {
	public static final String	SETTINGS_RESOURCE_NAME = "settings.props"; 
	public static final String	LOCALIZING_STRINGS_RESOURCE_NAME = "i18n.props"; 

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final SplashScreen 	splash = SplashScreen.getSplashScreen();
		final Properties	settings = new Properties();

		try{
			settings.load(Executor.class.getResourceAsStream(SETTINGS_RESOURCE_NAME));

			System.err.println("Settings: "+settings);
			Thread.sleep(5000);
			
			if (splash != null) {
				splash.close();
			}
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (splash != null && splash.isVisible()) {
				splash.close();
			}
		}

	}

}
