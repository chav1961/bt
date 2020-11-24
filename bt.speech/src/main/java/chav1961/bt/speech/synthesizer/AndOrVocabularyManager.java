package chav1961.bt.speech.synthesizer;

import java.io.IOException;

import javax.speech.EngineStateException;
import javax.speech.SpeechLocale;
import javax.speech.VocabularyManager;
import javax.speech.Word;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class AndOrVocabularyManager implements VocabularyManager {
	private static final Word[]		EMPTY_WORDS = new Word[0];	
	private static final String[]	EMPTY_STRINGS = new String[0];	
	
	private final SyntaxTreeInterface<Word>	tree = new AndOrTree<>();
	
	public AndOrVocabularyManager() {
	}
	
	@Override
	public void addWord(final Word word) throws EngineStateException, SecurityException {
		if (word == null) {
			throw new NullPointerException("Word to add can't be null"); 
		}
		else {
			tree.placeName(word.getText(), word);
		}
	}

	@Override
	public void addWords(final Word[] words) throws EngineStateException, SecurityException {
		if (words == null || Utils.checkArrayContent4Nulls(words) >= 0) {
			throw new NullPointerException("Words to add can't be null array and must not contains nulls inside"); 
		}
		else {
			for (Word item : words) {
				addWord(item);
			}
		}
	}

	@Override
	public String[] getPronounciations(String text, SpeechLocale locale) throws EngineStateException {
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("Text to get words for can't be null or empty");
		}
		else if (locale == null) {
			throw new NullPointerException("Locale can't be null");
		}
		else if (locale != SpeechLocale.RUSSIAN) {
			return EMPTY_STRINGS;
		}
		else {
			final Word[]	words = getWords(text,locale);
			
			if (words.length == 0) {
				return EMPTY_STRINGS;
			}
			else {
				int	totalCount = 0, displ = 0;
				
				for (Word item : words) {
					totalCount += item.getPronunciations().length;
				}
				final String[]	result = new String[totalCount];
				
				for (Word item : words) {
					final String[]	value = item.getPronunciations();
					
					System.arraycopy(value, 0, result, displ, value.length);
					displ += value.length;
				}
				return result;
			}
		}
	}

	@Override
	public Word[] getWords(final String text, final SpeechLocale locale) throws EngineStateException {
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("Text to get words for can't be null or empty");
		}
		else if (locale == null) {
			throw new NullPointerException("Locale can't be null");
		}
		else if (locale != SpeechLocale.RUSSIAN) {
			return EMPTY_WORDS;
		}
		else {
			final long	id = tree.seekName(text);
			
			if (id >= 0) {
				return new Word[] {tree.getCargo(id)};
			}
			else {
				return EMPTY_WORDS;
			}
		}
	}

	@Override
	public void removeWord(final Word word) throws EngineStateException, IllegalArgumentException, SecurityException {
		if (word == null) {
			throw new NullPointerException("Word to remove can't be null"); 
		}
		else {
			final long	id = tree.seekName(word.getText());
			
			if (id >= 0) {
				tree.removeName(id);
			}
		}
	}

	@Override
	public void removeWords(final Word[] words) throws EngineStateException, IllegalArgumentException, SecurityException {
		if (words == null || Utils.checkArrayContent4Nulls(words) >= 0) {
			throw new NullPointerException("Words to remove can't be null array and must not contains nulls inside"); 
		}
		else {
			for (Word item : words) {
				removeWord(item);
			}
		}
	}

	public void download(final JsonStaxParser parser) throws IOException {
		
	}
	
	public void upload(final JsonStaxPrinter printer) throws IOException {
		
	}
}
