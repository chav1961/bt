package chav1961.bt.nlp.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class NLPUtils {
	public static void prepareDictionaryManagerContent(final InputStream xmlModel, final DataOutputStream rawDump) throws IOException {
		if (xmlModel == null) {
			throw new NullPointerException("XML model stream can't be null");
		}
		else if (rawDump == null) {
			throw new NullPointerException("RAW dump stream can't be null");
		}
		else {
			
		}
	}
}
