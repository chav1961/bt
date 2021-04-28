package chav1961.bt.mnemoed.entities;

import java.util.Map;

import chav1961.purelib.basic.subscribable.Subscribable;

public class HyperlinkComponent extends TextEntityComponent {
	private static final BasicEntityComponentChecker	checker = new BasicEntityComponentChecker(
			new Class[] {URIProp.class}, 
			new Class[] {URIProp.class}, 
			false); 

	public HyperlinkComponent(Map<String, Subscribable<?>> vars) {
		super(vars);
	}

	public URIProp getUri() {
		return getProp(URIProp.class);
	}
	
	public void setUri(final URIProp prop) {
		setProp(prop);
	}

	@Override
	protected void checkProperties(final EntityProp[] props) {
		super.checkProperties(props);
		checker.totalCheck(props);
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
