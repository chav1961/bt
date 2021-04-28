package chav1961.bt.mnemoed.entities;

import java.util.Map;

import chav1961.purelib.basic.subscribable.Subscribable;

public class PanelEntityContainer extends BasicEntityContainer {
	private static final BasicEntityComponentChecker	checker = new BasicEntityComponentChecker(
			new Class[] {VisibilityProp.class}, 
			new Class[] {VisibilityProp.class}, 
			true); 

	public PanelEntityContainer(final Map<String, Subscribable<?>> vars) {
		super(vars);
	}

	protected void checkProperties(final EntityProp[] props) throws IllegalArgumentException {
		checker.totalCheck(props);
	}
	
	protected boolean isPropertyClassAvailable(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassAvailable(clazz);
	}
	
	protected boolean isPropertyClassMandatory(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassMandatory(clazz);
	}
	
	protected boolean isExtraPropertiesSupported() {
		return checker.supportsExtraProperties();
	}
}
