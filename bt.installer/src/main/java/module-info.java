module chav1961.bt.installer {
	requires transitive chav1961.purelib;
	requires java.desktop;

	uses chav1961.purelib.i18n.interfaces.Localizer;
	exports chav1961.bt.installer to chav1961.bt.installbuilder;
}
