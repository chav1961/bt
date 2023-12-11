package chav1961.bt.jj.starter;

class InterfaceItem {
	public final int	interfaceRef;
	private final ConstantPoolItem[]	pool;

	public InterfaceItem(final int interfaceRef, final ConstantPoolItem[] pool) {
		this.interfaceRef = interfaceRef;
		this.pool = pool;
	}

	@Override
	public String toString() {
		return "InterfaceItem [interfaceRef=" + ClassDefinitionLoader.resolveDescriptor(pool, interfaceRef) + "]";
	}
}
