package chav1961.bt.matrix.utils;

import java.io.IOException;
import java.io.InputStream;

import org.yaml.snakeyaml.Yaml;

import chav1961.purelib.basic.Utils;

public class ProgramItem {
	public String 	programName;
	public String	programBody;
	
	public static ProgramItem[] load(final String source) throws IOException {
		if (Utils.checkEmptyOrNullString(source)) {
			throw new IllegalArgumentException("Source string can't be null or empty");
		}
		else {
			try(final InputStream	is = ProgramItem.class.getResourceAsStream(source)) {
				if (is == null) {
					throw new IllegalArgumentException("Resource ["+source+"] not found");
				}
				else {
					return new Yaml().loadAs(is, ProgramItem[].class);
				}
			}
		}
	}
}
