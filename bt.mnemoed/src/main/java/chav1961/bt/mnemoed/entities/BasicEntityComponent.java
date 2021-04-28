package chav1961.bt.mnemoed.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.print.attribute.standard.NumberUp;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.subscribable.Subscribable;

public abstract class BasicEntityComponent {
	private static final BasicEntityComponentChecker	checker = new BasicEntityComponentChecker(
					new Class[] {TooltipProp.class, ColorProp.class, LocationProp.class, VisibilityProp.class}, 
					new Class[] {ColorProp.class, LocationProp.class, VisibilityProp.class},
					true);
	
	protected final Map<Class<? extends EntityProp>,EntityProp>	props = new HashMap<>();
	protected final Map<String,Subscribable<?>> vars;
	private BasicEntityContainer				parent = null;
	
	BasicEntityComponent(final Map<String,Subscribable<?>> vars) {
		this.vars = vars;
	}

	public BasicEntityContainer getParent() {
		return parent;
	}
	
	protected void setParent(final BasicEntityContainer parent) {
		this.parent = parent;
	}
	
	public TooltipProp getTooptip() {
		return getProp(TooltipProp.class);
	}
	
	public void setTooltip(final TooltipProp prop) {
		setProp(prop);
	}
	
	public ColorProp getColor() {
		return getProp(ColorProp.class);
	}
	
	public void setColor(final ColorProp prop) {
		setProp(prop);
	}

	public LocationProp getLocation() {
		return getProp(LocationProp.class);
	}
	
	public void setLocation(final LocationProp prop) {
		setProp(prop);
	}
	
	public VisibilityProp getVisibility() {
		return getProp(VisibilityProp.class);
	}
	
	public void setVisibility(final VisibilityProp prop) {
		setProp(prop);
	}
	
	protected <T extends EntityProp> T getProp(final Class<T> awaited) throws NullPointerException, IllegalArgumentException {
		if (props == null) {
			throw new NullPointerException("Awaited call can't be null");
		}
		else if (isPropertyClassAvailable(awaited) || isExtraPropertiesSupported()) {
			return (T) props.get(awaited);
		}
		else {
			throw new IllegalArgumentException("Awaited property class ["+awaited.getCanonicalName()+"] is not supported");
		}
	}
	
	protected <T extends EntityProp> T setProp(final T prop) throws NullPointerException, IllegalArgumentException {
		if (props == null) {
			throw new NullPointerException("Property to store can't be null");
		}
		else if (isPropertyClassAvailable(prop.getClass()) || isExtraPropertiesSupported()) {
			return (T) props.put((Class<? extends EntityProp>)prop.getClass(),prop);
		}
		else {
			throw new IllegalArgumentException("Property class ["+prop.getClass().getCanonicalName()+"] to set is not supported");
		}
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
	
	protected static class BasicEntityComponentChecker {
		private final Class<? extends EntityProp>[] available;
		private final Class<? extends EntityProp>[] mandatory;
		private final boolean						allowExtraProperties;
		
		protected BasicEntityComponentChecker(final Class<? extends EntityProp>[] available, final Class<? extends EntityProp>[] mandatory, final boolean allowExtraProperties) {
			this.available = available;
			this.mandatory = mandatory;
			this.allowExtraProperties = allowExtraProperties;
		}
		
		protected void totalCheck(final EntityProp... props) throws IllegalArgumentException {
			if (props == null || Utils.checkArrayContent4Nulls(props) >= 0) {
				throw new IllegalArgumentException("Null properties array or nulls inside props list"); 
			}
			else {
				final StringBuilder	message = new StringBuilder();

				try {availablesCheck(props);
				} catch (IllegalArgumentException exc) {
					message.append(exc.getLocalizedMessage());
				}
				try {mandatoryCheck(props);
				} catch (IllegalArgumentException exc) {
					message.append(exc.getLocalizedMessage());
				}
				if (message.length() > 0) {
					throw new IllegalArgumentException("Errors in the properties list:\n"+message);
				}
			}
		}

		protected boolean isPropClassAvailable(final Class<? extends EntityProp> clazz) {
			for (Class<? extends EntityProp> item : available) {
				if (item.isAssignableFrom(clazz)) {
					return true;
				}
			}
			return false;
		}

		protected boolean isPropClassMandatory(final Class<? extends EntityProp> clazz) {
			for (Class<? extends EntityProp> item : available) {
				if (item.isAssignableFrom(clazz)) {
					return true;
				}
			}
			return false;
		}
		
		protected boolean supportsExtraProperties() {
			return allowExtraProperties;
		}
		
		private void availablesCheck(final EntityProp... props) throws IllegalArgumentException {
			final StringBuilder	message = new StringBuilder();
			
loop1:		for (EntityProp current : props) {
				final Class<? extends EntityProp>	clazz = (Class<? extends EntityProp>) current.getClass();
				
				for (Class<? extends EntityProp> item : available) {
					if (item.isAssignableFrom(clazz)) {
						continue loop1;
					}
				}
				if (!allowExtraProperties) {
					message.append("Class [").append(clazz.getCanonicalName()).append("] is not available\n");
				}
			}
			if (message.length() > 0) {
				throw new IllegalArgumentException(message.toString());
			}
		}

		private void mandatoryCheck(final EntityProp... props) throws IllegalArgumentException {
			final StringBuilder	message = new StringBuilder();
			
loop2:		for (Class<? extends EntityProp> item : mandatory) {
				for (EntityProp current : props) {
					if (item.isAssignableFrom(current.getClass())) {
						continue loop2;
					}
				}
				message.append("Mandatory class [").append(item.getCanonicalName()).append("] is missing\n");
			}
			if (message.length() > 0) {
				throw new IllegalArgumentException(message.toString());
			}
		}
		
		@Override
		public String toString() {
			return "BasicEntityComponentChecker [available=" + Arrays.toString(available) + ", mandatory=" + Arrays.toString(mandatory) + ", allowExtraProperties=" + allowExtraProperties + "]";
		}
	}
	
}
