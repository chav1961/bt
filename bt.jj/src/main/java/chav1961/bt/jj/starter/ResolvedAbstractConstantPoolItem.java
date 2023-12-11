package chav1961.bt.jj.starter;

class ResolvedAbstractConstantPoolItem extends AbstractConstantPoolItem {
	public AbstractConstantPoolItem	source;

	ResolvedAbstractConstantPoolItem(final int itemType, final AbstractConstantPoolItem source) {
		super(itemType);
		this.source = source;
	}
}
