package chav1961.bt.security.interfaces;

import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;

@LocaleResourceLocation("i18n:xml:root://chav1961.bt.security.interfaces.PasswordWeakness/chav1961/bt.security/i18n/i18n.xml")
public enum PasswordWeakness {
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.allok",tooltip="chav1961.bt.security.interfaces.pwdweakness.allok.tt")
    ALL_OK,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.tooshort",tooltip="chav1961.bt.security.interfaces.pwdweakness.tooshort.tt")
    TOO_SHORT,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.numericcharsmissing",tooltip="chav1961.bt.security.interfaces.pwdweakness.numericcharsmissing.tt")
    NUMERIC_CHARS_MISSING,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.alphabeticcharsmissing",tooltip="chav1961.bt.security.interfaces.pwdweakness.alphabeticcharsmissing.tt")
    ALPHABETIC_CHARS_MISSING,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.samecase",tooltip="chav1961.bt.security.interfaces.pwdweakness.samecase.tt")
    ALL_ALPHABETIC_CHARS_IN_SAME_CASE,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.specialcharsmissing",tooltip="chav1961.bt.security.interfaces.pwdweakness.specialcharsmissing.tt")
    SPECIAL_CHARS_MISSING,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.sameasusername",tooltip="chav1961.bt.security.interfaces.pwdweakness.sameasusername.tt")
    SAME_AS_USER_NAME,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.startswithforbiddenchars",tooltip="chav1961.bt.security.interfaces.pwdweakness.startswithforbiddenchars.tt")
    STARTS_WITH_FORBIDDEN_CHARS,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.endswithforbiddenchars",tooltip="chav1961.bt.security.interfaces.pwdweakness.endswithforbiddenchars.tt")
    ENDS_WITH_FORBIDDEN_CHARS,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.containsforbiddenchars",tooltip="chav1961.bt.security.interfaces.pwdweakness.containsforbiddenchars.tt")
    CONTAIN_FORBIDDEN_CHARS,
	@LocaleResource(value="chav1961.bt.security.interfaces.pwdweakness.inblacklist",tooltip="chav1961.bt.security.interfaces.pwdweakness.inblacklist.tt")
    IN_BLACKLIST
}