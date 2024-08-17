package chav1961.bt.databaseutils;

import chav1961.purelib.basic.ArgParser;

public class Application {
	public static final String	ARG_SOURCE = "source";
	public static final String	ARG_VERSION = "version";
	public static final String	ARG_PACK = "pack";
	public static final String	ARG_TARGET_DIR = "target";

	public static void main(final String[] args) {
		// TODO Auto-generated method stub

	}

	private static class Args extends ArgParser {
		private static final ArgParser.AbstractArg[]	KEYS = {
			new FileArg(ARG_SOURCE, false, false, "JSON model file source location. In missing, model content will be read from System.in"),
			new BooleanArg(ARG_PACK, false, false, "Pack content generated and send it to System.out. Mutually exclusive with target"),
			new FileArg(ARG_TARGET_DIR, false, false, "Target directory to store generated content to. Mutually exclusive with pack"),
			new StringArg(ARG_VERSION, false, false, "Make content for version typed"),
		};
		
		private Args() {
			super(KEYS);
		}
	}
}
