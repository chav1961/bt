package chav1961.bt.mnemoed.entities;

import java.util.Map;

import chav1961.purelib.basic.subscribable.Subscribable;

public class TextEntityComponent extends BasicEntityComponent {
	private static final BasicEntityComponentChecker	checker = new BasicEntityComponentChecker(
									new Class[] {FontProp.class, TextProp.class, BackgroundProp.class}, 
									new Class[] {TextProp.class}, 
									true); 

	protected TextEntityComponent(final Map<String, Subscribable<?>> vars) {
		super(vars);
	}
	
	public TextProp getText() {
		return getProp(TextProp.class);
	}
	
	public void setText(final TextProp prop) {
		setProp(TextProp.class,prop);
	}
	
	public FontProp getFont() {
		return getProp(FontProp.class);
	}

	public void setFont(final FontProp prop) {
		setProp(FontProp.class,prop);
	}

	public BackgroundProp getBackground() {
		return getProp(BackgroundProp.class);
	}
	
	public void setBackground(final BackgroundProp prop) {
		setProp(BackgroundProp.class,prop);
	}
	
	@Override
	protected boolean isPropertyClassAvailable(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassAvailable(clazz) || super.isPropertyClassAvailable(clazz);
	}

	@Override
	protected boolean isPropertyClassMandatory(Class<? extends EntityProp> clazz) {
		return checker.isPropClassMandatory(clazz) || super.isPropertyClassMandatory(clazz);
	}

	@Override
	protected boolean isExtraPropertiesSupported() {
		return checker.supportsExtraProperties() && super.isExtraPropertiesSupported();
	}
}
