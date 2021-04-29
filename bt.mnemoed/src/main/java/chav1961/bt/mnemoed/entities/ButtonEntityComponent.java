package chav1961.bt.mnemoed.entities;

import java.util.Map;

import chav1961.purelib.basic.subscribable.Subscribable;

public class ButtonEntityComponent extends BasicEntityComponent {
	private static final BasicEntityComponentChecker	checker = new BasicEntityComponentChecker(
			new Class[] {FontProp.class, TextProp.class, BorderProp.class, BackgroundProp.class, URIProp.class}, 
			new Class[] {BorderProp.class, BackgroundProp.class, URIProp.class}, 
			true); 

	protected ButtonEntityComponent(Map<String, Subscribable<?>> vars) {
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

	public void getFont(final FontProp prop) {
		setProp(FontProp.class,prop);
	}

	public BackgroundProp getBackground() {
		return getProp(BackgroundProp.class);
	}
	
	public void setBackground(final BackgroundProp prop) {
		setProp(BackgroundProp.class,prop);
	}

	public URIProp getUri() {
		return getProp(URIProp.class);
	}
	
	public void setUri(final URIProp prop) {
		setProp(URIProp.class,prop);
	}

	public BorderProp getBorder() {
		return getProp(BorderProp.class);
	}
	
	public void setBorder(final BorderProp prop) {
		setProp(BorderProp.class,prop);
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
