package chav1961.bt.matrix.macros.runtime;

import java.util.ArrayList;
import java.util.List;

import chav1961.bt.matrix.macros.runtime.interfaces.ProgramStack;
import chav1961.bt.matrix.macros.runtime.interfaces.Value;
import chav1961.bt.matrix.macros.runtime.interfaces.Value.ValueType;

class ProgramStackImpl implements ProgramStack {
	private BlockDescriptor	current = new BlockDescriptor(null);
	private StackDescriptor	stack = null;
	
	public ProgramStackImpl() {
		
	}

	@Override
	public void pushBlock() {
		current = new BlockDescriptor(current);
	}

	@Override
	public int getBlockDepth() {
		return current.getDepth();
	}
	
	@Override
	public void declare(final int name, final ValueType type) {
		current.addVar(name, type);
	}

	@Override
	public boolean hasVar(int name) {
		BlockDescriptor	desc = current;
		
		while (desc != null) {
			if (desc.hasVariableHere(name)) {
				return true;
			}
			else {
				desc = desc.parent;
			}
		}
		return false; 
	}
	
	@Override
	public ValueType getVarType(int name) {
		return current.getVariableType(name);
	}
	
	@Override
	public Value getVarValue(final int name) {
		return current.getVariable(name);
	}

	@Override
	public void setVarValue(final int name, final Value value) {
		current.setVariable(name, value);
	}

	@Override
	public void popBlock() {
		if (current.parent == null) {
			throw new IllegalStateException("Block stack exhausted");
		}
		else {
			current = current.parent;
		}
	}

	@Override
	public void pushStackValue(final Value value) {
		final StackDescriptor	sd = new StackDescriptor(stack);
		
		sd.value = value;
		stack = sd;
	}

	@Override
	public int getStackDepth() {
		StackDescriptor	temp = stack;
		int	result = 0;
		
		while (temp != null) {
			temp = temp.parent;
			result++;
		}
		return result;
	}
	
	@Override
	public Value getStackValue() {
		if (stack == null) {
			throw new IllegalStateException("Stack is empty");
		}
		else {
			return stack.value;
		}
	}

	@Override
	public void setStackValue(final Value value) {
		if (stack == null) {
			throw new IllegalStateException("Stack is empty");
		}
		else {
			stack.value = value;
		}
	}

	@Override
	public Value popStackValue() {
		if (stack == null) {
			throw new IllegalStateException("Stack exhausted");
		}
		else {
			final Value	result = stack.value;
			
			stack = stack.parent;
			return result;
		}
	}
	
	private static class VarDescriptor {
		private final int		name;
		private final ValueType	type;
		private Value			value;
		
		private VarDescriptor(int name, ValueType type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String toString() {
			return "VarDescriptor [name=" + name + ", type=" + type + ", value=" + value + "]";
		}
	}
	
	private static class BlockDescriptor {
		private final BlockDescriptor	parent;
		private final List<VarDescriptor> desc = new ArrayList<>();
		
		private BlockDescriptor(BlockDescriptor parent) {
			this.parent = parent;
		}
		
		private void addVar(final int name, final ValueType type) throws IllegalArgumentException {
			if (hasVariableHere(name)) {
					throw new IllegalArgumentException("Duplicate name index ["+name+"] at this level");
			}
			else {
				desc.add(new VarDescriptor(name, type));
			}
		}
		
		private boolean hasVariableHere(final int name) {
			for (VarDescriptor item : desc) {
				if (item.name == name) {
					return true;
				}
			}
			return false; 
		}
		
		private ValueType getVariableType(final int name) throws IllegalArgumentException {
			BlockDescriptor	current = this;
			
			while (current != null) {
				if (current.hasVariableHere(name)) {
					for (VarDescriptor item : current.desc) {
						if (item.name == name) {
							return item.type;
						}
					}
				}
				else {
					current = current.parent;
				}
			}
			throw new IllegalArgumentException("Variable name index ["+name+"] not found anywhere"); 
		}

		private Value getVariable(final int name) throws IllegalArgumentException {
			BlockDescriptor	current = this;
			
			while (current != null) {
				if (current.hasVariableHere(name)) {
					for (VarDescriptor item : current.desc) {
						if (item.name == name) {
							return item.value;
						}
					}
				}
				else {
					current = current.parent;
				}
			}
			throw new IllegalArgumentException("Variable name index ["+name+"] not found anywhere"); 
		}

		private void setVariable(final int name, final Value value) throws IllegalArgumentException {
			BlockDescriptor	current = this;
			
			while (current != null) {
				if (current.hasVariableHere(name)) {
					for (VarDescriptor item : current.desc) {
						if (item.name == name) {
							if (value != null && value.getType() != item.type) {
								throw new IllegalArgumentException("Uncompatible assignment: variable name index ["+name+"] has type ["+item.type+"], but value to assign has type ["+value.getType()+"]"); 
							}
							else {
								item.value = value;
								return;
							}
						}
					}
				}
				else {
					current = current.parent;
				}
			}
			throw new IllegalArgumentException("Variable name index ["+name+"] not found anywhere"); 
		}

		private int getDepth() {
			BlockDescriptor	current = this;
			int	depth = 0;
			
			while (current != null) {
				current = current.parent;
				depth++;
			}
			return depth;
		}
		
		@Override
		public String toString() {
			return "BlockDescriptor [desc=" + desc + "]";
		}
	}
	
	private static class StackDescriptor {
		final StackDescriptor	parent;
		Value					value;
		
		private StackDescriptor(StackDescriptor parent) {
			this.parent = parent;
		}
	}
}
