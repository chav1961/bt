package chav1961.bt.jj.starter;

class InterfaceItem {
	public final int	interfaceRef;
	final int			offset;
	private final ConstantPool	pool;

	public InterfaceItem(final int offset, final int interfaceRef, final ConstantPool pool) {
		this.offset = offset;
		this.interfaceRef = interfaceRef;
		this.pool = pool;
	}

	@Override
	public String toString() {
		return "InterfaceItem [interfaceRef=" +  pool.deepToString(interfaceRef) + "]";
	}
}
