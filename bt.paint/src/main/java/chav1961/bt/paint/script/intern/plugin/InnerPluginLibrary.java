package chav1961.bt.paint.script.intern.plugin;

import chav1961.bt.paint.script.interfaces.PluginLibrary;

public class InnerPluginLibrary implements PluginLibrary {
	private final Library	library = new Library();
	
	public InnerPluginLibrary() {
	}
	
	@Override
	public boolean canServe(final String name) {
		return "bt.paint".equals(name);
	}

	@Override
	public Object getContent() {
		return library;
	}

	@Override
	public void close() throws RuntimeException {
	}
	
	public static class Library {
		
	}
}
