package chav1961.bt.svgeditor.interfaces;

import java.awt.event.MouseEvent;
import java.util.EventListener;

public interface StateChangedListener extends EventListener {
	void scaleChanged(double oldScale, double newScale);
	void locationChanged(final MouseEvent event);
	void contentChanged();
}
