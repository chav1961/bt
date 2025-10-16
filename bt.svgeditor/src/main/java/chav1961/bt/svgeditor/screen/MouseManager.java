package chav1961.bt.svgeditor.screen;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.util.Locale;

import javax.swing.JComponent;

import chav1961.purelib.basic.SubstitutableProperties.PropertyGroupChangeEvent;
import chav1961.purelib.basic.SubstitutableProperties.PropertyGroupChangeListener;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class MouseManager implements MouseListener, MouseMotionListener, MouseWheelListener, 
							LocaleChangeListener, AutoCloseable, PropertyGroupChangeListener {

	protected MouseManager() {
	}

	protected void addListeners(final JComponent owner) {
		if (owner == null) {
			throw new NullPointerException("Owner can't be null"); 
		}
		else {
			owner.addMouseListener(this);
			owner.addMouseMotionListener(this);
			owner.addMouseWheelListener(this);
		}
	}

	protected void removeListeners(final JComponent owner) {
		if (owner == null) {
			throw new NullPointerException("Owner can't be null"); 
		}
		else {
			owner.removeMouseWheelListener(this);
			owner.removeMouseMotionListener(this);
			owner.removeMouseListener(this);
		}
	}
	
	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
	}

	@Override
	public void mouseDragged(final MouseEvent e) {
	}

	@Override
	public void mouseMoved(final MouseEvent e) {
	}

	@Override
	public void mouseClicked(final MouseEvent e) {
	}

	@Override
	public void mousePressed(final MouseEvent e) {
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
	}

	@Override
	public void mouseEntered(final MouseEvent e) {
	}

	@Override
	public void mouseExited(final MouseEvent e) {
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
	}

	@Override
	public void close() throws RuntimeException {
	}

	@Override
	public void propertyChange(final PropertyChangeEvent evt) {
	}

	@Override
	public void propertiesChange(final PropertyGroupChangeEvent event) {
	}
}
