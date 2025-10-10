package chav1961.bt.svgeditor.parser;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;

import chav1961.bt.svgeditor.primitives.PrimitiveWrapper;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PreparationException;

public class PrimitiveWrapperTransferable implements Transferable, Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final DataFlavor		FLAVOR;
	private static final DataFlavor[]	FLAVOR_LIST;
	
	static {
		try {
			FLAVOR = new DataFlavor(DataFlavor.javaSerializedObjectMimeType+"; class="+PrimitiveWrapperTransferable.class.getName());
			FLAVOR_LIST = new DataFlavor[]{FLAVOR};
		} catch (ClassNotFoundException e) {
			throw new PreparationException(e);
		}
	}

	public final PrimitiveWrapper[] content;
	
	public PrimitiveWrapperTransferable(final PrimitiveWrapper... content) {
		if (content == null || content.length == 0 || Utils.checkArrayContent4Nulls(content) >= 0) {
			throw new IllegalArgumentException("Content to transfer is null, empty or cintains nulls inside");
		}
		else {
			this.content = content;
		}
	}
	
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return FLAVOR_LIST;
	}

	@Override
	public boolean isDataFlavorSupported(final DataFlavor flavor) {
		return Objects.equals(FLAVOR, flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
		if (!isDataFlavorSupported(flavor)) {
			throw new UnsupportedFlavorException(flavor); 
		}
		else {
			return this;
		}
	}
}
