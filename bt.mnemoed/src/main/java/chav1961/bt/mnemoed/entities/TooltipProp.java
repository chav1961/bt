package chav1961.bt.mnemoed.entities;

import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;

public class TooltipProp extends EntityProp {
	private ObjectValueSource	tooltip;
	
	public TooltipProp(final ObjectValueSource tooltip) throws NullPointerException {
		if (tooltip == null) {
			throw new NullPointerException("Tooltip to set can't be null");
		}
		else {
			this.tooltip = tooltip;
		}
	}

	public ObjectValueSource getTooltip() {
		return tooltip;
	}

	public void setTooltip(final ObjectValueSource tooltip) throws NullPointerException {
		if (tooltip == null) {
			throw new NullPointerException("Tooltip to set can't be null");
		}
		else {
			this.tooltip = tooltip;
		}
	}

	@Override
	public void upload(JsonStaxPrinter printer) throws PrintingException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void download(JsonStaxParser parser) throws SyntaxException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return "TooltipProp [tooltip=" + tooltip + "]";
	}
}
