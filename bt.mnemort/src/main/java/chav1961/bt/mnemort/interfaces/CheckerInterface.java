package chav1961.bt.mnemort.interfaces;

import java.net.URI;

import chav1961.purelib.basic.subscribable.Subscribable;

public interface CheckerInterface<Listener> {
	public enum CheckSeverity {
		INFO, WARNING, ERROR, CRITICAL
	}
	
	public enum CheckType {
	}
	
	URI getCheckerURI();
	CheckType getCheckType();
	Subscribable<Listener> getSubscribable();
	CheckerInterface<Listener> getParent();
	CheckSeverity processCheck();
}
