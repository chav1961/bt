package chav1961.bt.matrix.macros;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import javax.print.attribute.standard.NumberOfInterveningJobs;

import chav1961.bt.matrix.macros.InternalParser.Lexema;
import chav1961.bt.matrix.macros.MacroParser.OperatorType;
import chav1961.bt.matrix.macros.interfaces.VariableRepo;

class ParseStack implements VariableRepo {
	private static final AtomicInteger	UNIQUE = new AtomicInteger();
	private static final int			INITIAL_NAMES = 16;
	
	private NameItem[] 		names = new NameItem[INITIAL_NAMES];
	private int				numberOfNames = 0;
	private StackItem		current = new StackItem(null);

	public ParseStack(final Function<String, Object> parameters) {
		
	}

	public int createNameId(final char[] source, final int from,  final int len) {
    	int low = 0;
    	int high = numberOfNames - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			final int result = MacroUtils.compareTo(source, from, len, names[mid].name);
			
			if (result < 0) {
				low = mid + 1;
			}
			else if (result > 0) {
				high = mid - 1;
			}
			else {
				return names[mid].id;
			}
		}
		if (numberOfNames > names.length - 1) {
			names = Arrays.copyOf(names, 2 * names.length);
		}
		final int	unique = UNIQUE.incrementAndGet();
		
		names[numberOfNames++] = new NameItem(Arrays.copyOfRange(source, from, from + len), unique);
		Arrays.sort(names, 0, numberOfNames);
		return unique;
	}

	public int createForwardLabel() {
		return 0;
	}

	public int createBackwardLabel() {
		return 0;
	}
	
	public void push(OperatorType type, Object... cargo) {
	}
	
	public OperatorType getTopType() {
		return null;
	}

	public Object[] getTopCargo() {
		return null;
	}
	
	public void pop(OperatorType type) {
		
	}
	
	@Override
	public <T> void addVariable(int nameId, Class<T> type) {
		if (current.hasVariable(nameId)) {
			throw new IllegalArgumentException("Duplicate name id ["+nameId+"] to add");
		}
		else {
			current.addVariable(nameId, type);
		}
	}

	@Override
	public boolean hasVariable(final int nameId) {
		StackItem	item = current;
		
		while (item != null) {
			if (item.hasVariable(nameId)) {
				return true;
			}
			else {
				item = item.prev;
			}
		}
		return false;
	}

	@Override
	public <T> Class<T> getVariableType(final int nameId) {
		StackItem	item = current;
		
		while (item != null) {
			if (item.hasVariable(nameId)) {
				return item.getVariableType(nameId);
			}
			else {
				item = item.prev;
			}
		}
		throw new IllegalArgumentException("Name id ["+nameId+"] not found");
	}

	@Override
	public <T> T getVariable(final int nameId) {
		StackItem	item = current;
		
		while (item != null) {
			if (item.hasVariable(nameId)) {
				return item.getVariable(nameId);
			}
			else {
				item = item.prev;
			}
		}
		throw new IllegalArgumentException("Name id ["+nameId+"] not found");
	}

	@Override
	public <T> void setVariable(int nameId, T value) {
		StackItem	item = current;
		
		while (item != null) {
			if (item.hasVariable(nameId)) {
				item.setVariable(nameId, value);
				return;
			}
			else {
				item = item.prev;
			}
		}
		throw new IllegalArgumentException("Name id ["+nameId+"] not found");
	}

	@Override
	public int[] getNameIds() {
		final Set<Integer>	temp = new HashSet<>();
		StackItem	item = current;
		
		while (item != null) {
			for(int nameid : item.getNameIds()) {
				temp.add(nameid);
			}
			item = item.prev;
		}
		final int[]	result = new int[temp.size()];
		int			index = 0;
		
		for(Integer nameId : temp) {
			result[index++] = nameId.intValue();
		}
		return result;
	}
	
	private static class NameItem implements Comparable<NameItem> {
		final char[]	name;
		final int		id;
		
		public NameItem(final char[] name, final int id) {
			this.name = name;
			this.id = id;
		}

		@Override
		public int compareTo(final NameItem o) {
			return MacroUtils.compareTo(name, 0, name.length, o.name);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			result = prime * result + Arrays.hashCode(name);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			NameItem other = (NameItem) obj;
			if (id != other.id) return false;
			if (!Arrays.equals(name, other.name)) return false;
			return true;
		}

		@Override
		public String toString() {
			return "NameItem [name=" + Arrays.toString(name) + ", id=" + id + "]";
		}
	}
	
	private static class VarItem implements Comparable<VarItem> {
		final int		varId;
		final Class<?>	clazz;
		Object			value;
		
		VarItem(final int varId, final Class<?> clazz) {
			this.varId = varId;
			this.clazz = clazz;
		}

		@Override
		public int compareTo(final VarItem o) {
			return varId - o.varId;
		}

		@Override
		public String toString() {
			return "VarItem [varId=" + varId + ", clazz=" + clazz + ", value=" + value + "]";
		}
	}
	
	private static class StackItem implements VariableRepo {
		private final StackItem	prev;
		private VarItem[]		vars = new VarItem[INITIAL_NAMES];
		private int				numberOfVars = 0;

		public StackItem(final StackItem prev) {
			this.prev = prev;
		}

		@Override
		public <T> void addVariable(final int nameId, final Class<T> type) {
			if (hasVariable(nameId)) {
				throw new IllegalArgumentException("Duplicate name id ["+nameId+"]");
			}
			else {
				if (numberOfVars > vars.length - 1) {
					vars = Arrays.copyOf(vars, 2 * vars.length);
				}
				vars[numberOfVars++] = new VarItem(nameId, type);
				Arrays.sort(vars, 0, numberOfVars);
			}
		}

		@Override
		public boolean hasVariable(final int nameId) {
	    	int low = 0;
	    	int high = numberOfVars - 1;

			while (low <= high) {
				int mid = (low + high) >>> 1;
				final int result = vars[mid].varId - nameId;
				
				if (result < 0) {
					low = mid + 1;
				}
				else if (result > 0) {
					high = mid - 1;
				}
				else {
					return true;
				}
			}
			return false;
		}

		@Override
		public <T> Class<T> getVariableType(final int nameId) {
			return (Class<T>) getVar(nameId).clazz;
		}

		@Override
		public <T> T getVariable(final int nameId) {
			return (T) getVar(nameId).value;
		}

		@Override
		public <T> void setVariable(final int nameId, final T value) {
			getVar(nameId).value = value;
		}
		
		private VarItem getVar(final int nameId) {
	    	int low = 0;
	    	int high = numberOfVars - 1;

			while (low <= high) {
				int mid = (low + high) >>> 1;
				final int result = vars[mid].varId - nameId;
				
				if (result < 0) {
					low = mid + 1;
				}
				else if (result > 0) {
					high = mid - 1;
				}
				else {
					return vars[mid];
				}
			}
			throw new IllegalArgumentException("Name id ["+nameId+"] not found");
		}

		@Override
		public int[] getNameIds() {
			final int[]	result = new int[numberOfVars];
			
			for(int index = 0; index < result.length; index++) {
				result[index] = vars[index].varId;
			}
			return result;
		}
	}

}
