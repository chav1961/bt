module chav1961.bt.installerbuilder {
	requires transitive chav1961.purelib;
	requires java.desktop;
	requires chav1961.bt.installer;
	
	uses chav1961.purelib.i18n.interfaces.Localizer;
	exports chav1961.bt.installbuilder;
}
