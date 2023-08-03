package chav1961.bt.nlp.parser;

import java.util.Set;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class StringGenerator implements Iterable<String>, Iterator<String> {
	private Character[] alphabet;
	private int stringLength;
	private int[] currentString;
	private boolean isLast;

	public StringGenerator(final Set<Character> alphabet, int stringLength) {
		if (alphabet == null || alphabet.isEmpty()) {
			throw new IllegalArgumentException("Alphabet is a nonempty set of characters");
		} else if (stringLength < 1) {
			throw new IllegalArgumentException("String length must be positive");
		}

		this.alphabet = alphabet.toArray(new Character[0]);
		this.stringLength = stringLength;
		this.isLast = false;

		this.currentString = new int[stringLength];
	}

	public Iterator<String> iterator() {
		return this;
	}

	public String next() {
		if (!hasNext()) {
			throw new NoSuchElementException("Iterator exhausted");
		}

		StringBuilder sb = new StringBuilder(stringLength);
		for (int idx : this.currentString) {
			sb.append(this.alphabet[idx]);
		}

		prepareNext();

		return sb.toString();
	}

	public boolean hasNext() {
		return !this.isLast;
	}

	private void prepareNext() {
		final int n = alphabet.length;
		int index = stringLength - 1;
		while (index >= 0 && currentString[index] == n-1) {
			index--;
		}
		if (index < 0) {
			this.isLast = true;
			return;
		}
		this.currentString[index] += 1;
		for (int j = stringLength-1; j > index; j--) {
			this.currentString[j] = 0;
		}
	}
}