package chav1961.bt.opc.utils;

import java.net.URI;

import chav1961.bt.opc.interfaces.OpcTreeItem;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.enumerations.ContinueMode;
import chav1961.purelib.enumerations.NodeEnterMode;

public class OpcUtils {
	@FunctionalInterface
	public interface OpcItemRefreshCallback<T> {
		ContinueMode getValue(NodeEnterMode mode, OpcTreeItem<?> node, T parameter) throws ContentException;
	}
	
	public static <T> OpcTreeItem<T> buildOpcTreeStructure(final URI... servers) {
		return null;
	}
	
	public static <T> void refreshOpcTreeContent(final OpcTreeItem<T> root, final OpcItemRefreshCallback<T> callback, final URI... servers) {
		
	}

	public static <T> void simulateOpcTreeContent(final OpcTreeItem<T> root, final OpcItemRefreshCallback<T> callback) {
		
	}
}
