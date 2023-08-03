package chav1961.bt.nlp.parser;

//-- Description -- 
//Assume 'S' is always the start symbol 
//You may assume that your method will be tested in the following setting: 
//- grammar will contain between 1 and 100 strings, 
//- each string will represent one production (possibly with multiple right 
//hand sides, separated by | (see examples)), 
//- word can be empty or up to 10000 terminal symbols (characters) long. 		
//-- References -- 
//[1] Jay Earley. An Efficient Context-Free Parsing Algorithm. Communications of the ACM, 1970. 
//[2] John Aycock and Nigel Horspool. Practical Earley Parsing. Computer Journal, 2002. 
//

import java.util.*;

public class Earley {
	private final ArrayList<LinkedList<State>>	stateSets = new ArrayList<>();
	private final Grammar 						g;
	
	public Earley(final String... grammar) {
		g = new Grammar(grammar);
	}
	
	// helper methods 
	private boolean isNonterminal(final String c) {
		return Character.isUpperCase(c.charAt(0)) || c.equals("@");
	}
	
	// If production (P, j) is in S_i and x_{i+1}= current terminal, then add P with rhsIdx+1 to S_{i+1}
	private void scanner(final State state, final int crrIdx) {
		State newState = new State(state.prod, state.rhsIdx+1, state.prevSet);
		if (!stateSets.get(crrIdx+1).contains(newState)) {
			stateSets.get(crrIdx+1).add(newState);
		}
	}
	
	// We use the predictor from [2]; it nicely handles $\varepsilon$-productions 
	// For [A -> alpha o E beta, i], it adds [E -> o gamma, j] for all productions in S_i
	// with E on the left-hand side 
	private void predictor(final State state, final int crrIdx, final Set<String> nullableVars) {
		final String BB = state.prod.getProdRight()[state.rhsIdx];
		
		for (Production p : g) {
			if (p.getProdLeft().equals(BB)) {
				State newState = new State(p, 0, crrIdx);
				if (!stateSets.get(crrIdx).contains(newState)) {
					stateSets.get(crrIdx).add(newState);
				}
			}
		}
		// Need this to handle $\varepsilon$-productions [2] 
		if (nullableVars.contains(BB)) { // B is nullable, i.e., B =>* eps 
			State newState = new State(state.prod, state.rhsIdx+1, state.prevSet);
			if (!stateSets.get(crrIdx).contains(newState)) {
				stateSets.get(crrIdx).add(newState);
			}
		}
	}
	
	private void completer(final State state, final int crrIdx) {
		final int 				j = state.prevSet;
		final LinkedList<State>	stateSet = stateSets.get(j);
		
		for (int i = 0; i < stateSet.size(); ++i) {
			final State s = stateSet.get(i);
			
			if (s.rhsIdx < s.prod.getProdRight().length && s.prod.getProdRight()[s.rhsIdx].equals(state.prod.getProdLeft())) {
				final State newState = new State(s.prod, s.rhsIdx + 1, s.prevSet);
				
				if (!stateSets.get(crrIdx).contains(newState)) {
					stateSets.get(crrIdx).add(newState);
				}
			}
		}
	}
	
	private void initialize(final int n) {
		// add the initial state set S_0
		final Production newProd = new Production("@", "S");
		final LinkedList<State> initState = new LinkedList<State>();
		
		initState.add(new State(newProd, 0, 0));
		stateSets.add(initState);
		for (int i = 0; i < n; ++i) {
			stateSets.add(new LinkedList<State>());
		}
	}
	
	private void process(final int i, final String a, final Set<String> nullableVars) {
		final LinkedList<State> stateSet = stateSets.get(i);
		
		int crrSize = 0;
		do {
			crrSize = stateSet.size();
			// iterate over the states from the current state set 
			for (int j = 0; j < crrSize; ++j) {
				State state = stateSet.get(j);
				// apply either scanner, predictor, or completer 
				if (state.rhsIdx == state.prod.getProdRight().length) {
					completer(state, i); 
				} else {
					if (isNonterminal(state.prod.getProdRight()[state.rhsIdx])) {
						predictor(state, i, nullableVars);
					} else if (state.prod.getProdRight()[state.rhsIdx].equals(a)) {
						scanner(state, i);
					} // else Nothing left to do 
				}
			}
		} while (crrSize != stateSet.size());
	}
	
	public boolean solve(final String word) {
		// compute nullable symbols 
		final Set<String> nullableVars = g.getNullable();
		
		// run the earley recognizer 
		initialize(word.length());
		for (int i = 0; i < word.length(); ++i) {
			int crrSet = i+1; // set index 
			Character a = word.charAt(i);
			// Initialize the set S_{i+1} by applying the three operations to S_i until S_i stops changing 
			process(i, new String(new char[] {a.charValue()}), nullableVars);
		}
		process(word.length(), "/", nullableVars); // character '/' is never in T 
		
		State finalState = new State(new Production("@", "S"), 1, 0);
		LinkedList<State> lastStateSet = stateSets.get(word.length());
		// print();
		for (State s : lastStateSet) {
			if (s.equals(finalState)) {
				return true;
			}
		}
		return false;
	}

	//Single production 
	//Assume nonterminal is always a single character from {A,B,...,Z}, and terminal is always a string over {a,b,...,z} 
	//prodHead -> prodRhs 
	static class Production {
		private final String 	prodLeft; 
		private final String[]	prodRight; 
		
		// Constructor 
		public Production(final String prodLeft, final String... prodRight) {
			this.prodLeft = prodLeft;
			this.prodRight = prodRight;
		}

		public String getProdLeft() {
			return prodLeft;
		}
		
		public String[] getProdRight() {
			return prodRight;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((prodLeft == null) ? 0 : prodLeft.hashCode());
			result = prime * result + Arrays.hashCode(prodRight);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Production other = (Production) obj;
			if (prodLeft == null) {
				if (other.prodLeft != null) return false;
			} else if (!prodLeft.equals(other.prodLeft)) return false;
			if (!Arrays.equals(prodRight, other.prodRight)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "Production [prodLeft=" + prodLeft + ", prodRight=" + Arrays.toString(prodRight) + "]";
		}
	}

	//Let G = (V, T, P, S) be a context-free grammar 
	static class Grammar implements Iterable<Production> {
		private final List<Production> productions = new ArrayList<Production>();
		private final Set<String> nullables;
		
		// Constructor 
		public Grammar(final String... grammar) {
			for (String s : grammar) {
				final String[] parts = s.split("->");
				
				for (String rhs : parts[1].trim().split("\\|")) {
					addProduction(parts[0].trim(), rhs.trim().split("\\Q:\\E"));
				}
			}
			this.nullables = computeNullable();
		}
		
		public Grammar(final List<Production> productions) {
			this.productions.addAll(productions); 
			this.nullables = computeNullable();
		}

		public Set<String> getNullable() {
			return nullables;
		}
		
		@Override
		public Iterator<Production> iterator() {
			return productions.iterator();
		}
		
		@Override
		public String toString() {
			return "Grammar [productions=" + productions + "]";
		}

		private boolean addProduction(final Production prod) {
			return !productions.contains(prod) && productions.add(prod);
		}
		
		private boolean addProduction(final String prodHead, final String... prodRhs) {
			return addProduction(new Production(prodHead, prodRhs));
		}

		// Compute the set of nullable nonterminals: { A in V | A =>* eps } 
		// I decided to go with a simple fixed-point algorithm 
		private Set<String> computeNullable() {
			final Set<String> nullSet = new TreeSet<>();
			// find the ``base'' symbols --- all A in V such that A -> eps is in P 
			for (Production p : productions) {
				if (p.getProdRight()[0].equals("~")) { 
					nullSet.add(p.getProdLeft()); 
				}
			}
			if (nullSet.size() != 0) {
				boolean isNullable = true; 
				int crrSize = nullSet.size();
				do {
					crrSize = nullSet.size();
					for (Production p : productions) {
						isNullable = true;
						for (String c : p.getProdRight()) {
							if (Character.isLowerCase(c.charAt(0)) && Character.isLetter(c.charAt(0)) || !nullSet.contains(c)) {
								isNullable = false; 
								break;
							}
						}
						if (isNullable) { 
							nullSet.add(p.getProdLeft()); 
						}
					}
				} while (crrSize != nullSet.size());
			}
			return nullSet;
		}
	}

	//State 
	static class State {
		public Production prod;
		public int rhsIdx;
		public int prevSet;
		
		// Constructor 
		public State(final Production prod, final int rhsIdx, final int prevSet) {
			this.prod = prod; // production 
			this.rhsIdx = rhsIdx; // position of the dot on the right-hand-side of the production 
			this.prevSet = prevSet;
		}
		
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			} else if (!(o instanceof State)) {
				return false;
			} else {
				State s = (State)o;
				return rhsIdx == s.rhsIdx && prevSet == s.prevSet && prod.equals(s.prod);
			}
		}

		@Override
		public String toString() {
			return "State [prod=" + prod + ", rhsIdx=" + rhsIdx + ", prevSet=" + prevSet + "]";
		}
	}
}
