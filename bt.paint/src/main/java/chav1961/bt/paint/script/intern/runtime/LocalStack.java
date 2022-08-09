package chav1961.bt.paint.script.intern.runtime;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import chav1961.bt.paint.control.Predefines;
import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.intern.interfaces.ExecuteScriptCallback;
import chav1961.bt.paint.script.intern.parsers.ScriptParserUtil.EntityDescriptor;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

public class LocalStack {
	private final Predefines			predef;
	private final ExecuteScriptCallback	callback;
	private final List<VarKeeper[]>		stack = new ArrayList<>();
	
	public LocalStack(final SyntaxTreeInterface<EntityDescriptor> vars, final Predefines predef, final ExecuteScriptCallback callback) throws PaintScriptException, InterruptedException {
		if (vars == null) {
			throw new NullPointerException("Vars list can't be null");
		}
		else if (predef == null) {
			throw new NullPointerException("Predefines can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Script callback can't be null");
		}
		else {
			this.predef = predef;
			this.callback = callback;
			pushLevel(vars);
			initialize(stack.get(0), vars);
		}
	}
	
	public void push(final SyntaxTreeInterface<EntityDescriptor> vars) throws PaintScriptException, InterruptedException {
		if (vars == null) {
			throw new NullPointerException("Vars list can't be null");
		}
		else {
			pushLevel(vars);
			initialize(stack.get(0), vars);
		}
	}

	public void pop() {
		if (stack.isEmpty()) {
			throw new IllegalStateException("Stack exhausted"); 
		}
		else {
			stack.remove(0);
		}
	}
	
	public Object getVar(final long varId) throws PaintScriptException {
		final VarKeeper[]	vars = stack.get(0);
        int 				low = 0, high = vars.length - 1, mid;
        long				midVal;

        while (low <= high) {
            mid = (low + high) >>> 1;
            midVal = vars[mid].desc.id;

            if (midVal < varId)
                low = mid + 1;
            else if (midVal > varId)
                high = mid - 1;
            else
                return vars[mid].currentValue;
        }
        throw new PaintScriptException("Var id ["+varId+"] not found in the current stack");
	}

	private void pushLevel(final SyntaxTreeInterface<EntityDescriptor> vars) {
		final List<VarKeeper>	list = new ArrayList<>();
		
		vars.walk((content,len,id,cargo)->{
			if (cargo instanceof EntityDescriptor) {
				list.add(new VarKeeper(cargo));
			}
			return true;
		});
		stack.add(0, list.toArray(new VarKeeper[list.size()]));
	}

	private void initialize(final VarKeeper[] varKeepers, final SyntaxTreeInterface<EntityDescriptor> vars) throws PaintScriptException, InterruptedException {
		for (VarKeeper item : varKeepers) {
			if (item.desc.initials != null) {
				Array.set(item.currentValue, 0, ScriptExecutorUtil.calc(item.desc.initials, vars, this, predef, 0, callback));
			}
		}
	}

	
	private static class VarKeeper {
		private final EntityDescriptor	desc;
		private Object					currentValue;
		
		private VarKeeper(final EntityDescriptor desc) {
			this.desc = desc;
			this.currentValue = Array.newInstance(desc.dataType.get(0).getLeftValueClassAssociated(), 1);
		}
	}
}
