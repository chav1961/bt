package chav1961.bt.jj.shadows.java.lang;

import chav1961.bt.jj.starter.JJ;
import chav1961.bt.jj.starter.ClassDescriptor;

public class Object {
    private static void _registerNatives() {
    	
    }
	
	public Object() {
	}
	
	public final Class<?> _getClass() {
		return ((ClassDescriptor)JJ.asObject(JJ.load8(JJ.asLong(this)-JJ.QWORD_SIZE))).getClassInstance();
	}
	
	public int hashCode() {
		return (int)JJ.asLong(this);
	}
	
	protected Object clone() throws CloneNotSupportedException {
		return null;
	}
	
	public final void _notify() {
		
	}

	public final void _notifyAll() {
		
	}
	
	public final void _wait(long timeout) throws InterruptedException {
		
	}
}
