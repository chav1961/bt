package chav1961.bt.mnemoed.entities;

import java.io.IOException;

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
	public void upload(JsonStaxPrinter printer) throws PrintingException, IOException {
		if (printer == null) {
			throw new NullPointerException("Stax printer can't be null");
		}
		else {
			printer.startObject().name(getArgType(tooltip.getClass()).name());
			tooltip.upload(printer);
			printer.endObject();
		}
	}

	@Override
	public void download(JsonStaxParser parser) throws SyntaxException, IOException {
		if (parser == null) {
			throw new NullPointerException("Stax parser can't be null");
		}
		else {
			setTooltip(parseObjectValueSource(parser));
		}
	}

	@Override
	public String toString() {
		return "TooltipProp [tooltip=" + tooltip + "]";
	}
}
