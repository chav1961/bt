package chav1961.bt.preproc.runtime.interfaces;

public interface CharAppendable extends Appendable {
	@Override
	CharAppendable append(char c);
	@Override
	CharAppendable append(CharSequence csq, int start, int end);
	@Override
	CharAppendable append(CharSequence csq);
	
	CharAppendable append(char[] content);
}
