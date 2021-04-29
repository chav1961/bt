package chav1961.bt.opc.interfaces;

public interface OpcTreeFolderItem extends OpcTreeItem, Iterable<OpcTreeItem> {
	void addChild(OpcTreeItem child);
	int getChildCount();
	OpcTreeItem getChild(int index);
	OpcTreeItem setChild(int index, OpcTreeItem child);
	OpcTreeItem removeChild(int index);
}
