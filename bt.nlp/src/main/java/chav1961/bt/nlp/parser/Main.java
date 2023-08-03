package chav1961.bt.nlp.parser;

import java.util.Arrays;
import java.util.HashSet;

public class Main {
	public static boolean wordInGrammar(String g, String w) {
		Earley ep = new Earley(g.split(";"));
		
		return ep.solve(w);
	}

	public static void main(String [] args) {
		/* Test our implementation of the Early parser. */
		// wordInGrammar("S->aA|Aa|aAAA|AAAABBBABABABA;A->aA|bA|~;B->bbbA|ABA|A", "aaa");
		// wordInGrammar("S->S+M|M;M->M*T|T;T->a|b|c|d", "a++b*c");
		System.err.println("\nAll palindromic binary strings of length 5:");
		
		for (String w : new StringGenerator(new HashSet<>(Arrays.asList('0', '1')), 5)) {
			if (wordInGrammar("S->0:S:0|1:S:1|0|1|~", w)) {
				System.err.println(w);
			}
		}
	}
}