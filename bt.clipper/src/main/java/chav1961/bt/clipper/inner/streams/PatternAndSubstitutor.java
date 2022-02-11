package chav1961.bt.clipper.inner.streams;

import java.io.IOException;

import chav1961.purelib.basic.exceptions.SyntaxException;

interface PatternAndSubstitutor {
	@FunctionalInterface
	interface OutputWriter {
		void store(char[] content, int from, int to) throws IOException;
	}
	
	char[] getKeyword();
	int process(char[] data, int from, OutputWriter writer) throws SyntaxException;
}
