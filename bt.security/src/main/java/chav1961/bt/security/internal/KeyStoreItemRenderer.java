package chav1961.bt.security.internal;

import java.awt.Component;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.ListCellRenderer;
import javax.swing.border.LineBorder;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;

import chav1961.bt.security.keystore.KeyStoreItem;
import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.LocaleResource;
import chav1961.purelib.i18n.interfaces.LocaleResourceLocation;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.swing.interfaces.SwingItemRenderer;

public class KeyStoreItemRenderer<R> implements SwingItemRenderer<KeyStoreItem, R> {
	private static final Set<Class<?>>	SUPPORTED_RENDERERDS = Set.of(TreeCellRenderer.class);
	
	@Override
	public boolean canServe(final Class<KeyStoreItem> class2Render, final Class<R> rendererType, final Object... options) {
		if (class2Render == null) {
			throw new NullPointerException("Class to render descriptor can't be null"); 
		}
		else if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (class2Render.isArray()) {
			return canServe((Class<KeyStoreItem>) class2Render.getComponentType(), rendererType, options);
		}
		else {
			return Enum.class.isAssignableFrom(class2Render) && SUPPORTED_RENDERERDS.contains(rendererType); 
		}
	}

	@Override
	public R getRenderer(final Class<R> rendererType, final Object... options) {
		if (rendererType == null) {
			throw new NullPointerException("Renderer type can't be null"); 
		}
		else if (TreeCellRenderer.class.isAssignableFrom(rendererType)) {
			return (R) new DefaultTreeCellRenderer() {
				private static final long serialVersionUID = 0L;
				
				@Override
				public Component getTreeCellRendererComponent(final JTree tree, final Object value, final boolean sel, final boolean expanded, final boolean leaf, final int row, boolean hasFocus) {
					if (value == null) {
						return new JLabel("unselected");
					}
					else {
						final JLabel	label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
						
						return label;
					}
				}
			};
		}
		else {
			throw new UnsupportedOperationException("Required cell renderer ["+rendererType+"] is not supported yet");
		}
	}

}
