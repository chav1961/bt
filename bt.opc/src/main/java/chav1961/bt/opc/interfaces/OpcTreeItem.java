package chav1961.bt.opc.interfaces;

import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public interface OpcTreeItem<Cargo> extends Cloneable {
	public enum OpcTreeItemType {
		FOLDER,
		SOURCE,
		GROUP,
		ITEM,
		VALUE
	}
	
	@FunctionalInterface
	public interface WalkDownCallback<T> {
		ContinueMode process(NodeEnterMode mode, OpcTreeItem<?> node, T parameter) throws ContentException;
	}
	
	OpcTreeItemType getItemType();
	long getItemId();
	String getItemName();
	void setItemName(String name);
	String getItemDescription();
	void setItemDescription(String description);
	Cargo getCargo();
	void setCargo(Cargo cargo);

	<T> ContinueMode walkDown(WalkDownCallback<T> callback, T parameter) throws ContentException;
	OpcTreeItem<Cargo> clone() throws IllegalArgumentException;
}
