package chav1961.bt.mnemoed.entities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.print.attribute.standard.NumberUp;

import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.subscribable.Subscribable;


public abstract class BasicEntityComponent {
	private static final BasicEntityComponentChecker	checker = new BasicEntityComponentChecker(
					new Class[] {TooltipProp.class, ColorProp.class, LocationProp.class, VisibilityProp.class}, 
					new Class[] {LocationProp.class},
					false);
	
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
		setProp(TooltipProp.class,prop);
	}
	
	public ColorProp getColor() {
		return getProp(ColorProp.class);
	}
	
	public void setColor(final ColorProp prop) {
		setProp(ColorProp.class,prop);
	}

	public LocationProp getLocation() {
		return getProp(LocationProp.class);
	}
	
	public void setLocation(final LocationProp prop) {
		setProp(LocationProp.class,prop);
	}
	
	public VisibilityProp getVisibility() {
		return getProp(VisibilityProp.class);
	}
	
	public void setVisibility(final VisibilityProp prop) {
		setProp(VisibilityProp.class,prop);
	}
	
	public <T extends EntityProp> boolean hasExplicitProp(final Class<T> awaited) {
		if (awaited == null) {
			throw new NullPointerException("Awaited class can't be null");
		}
		else {
			return props.containsKey(awaited); 
		}
	}
	
	/**
	 * <p>Get property value for the given property class</p>
	 * @param <T> Property type
	 * @param awaited property class to get value for
	 * @return property value or null if not found
	 * @throws NullPointerException awaited class is null
	 * @throws IllegalArgumentException awaited property class is not supported or not found anywhere in the entities hierarchy
	 */
	protected <T extends EntityProp> T getProp(final Class<T> awaited) throws NullPointerException, IllegalArgumentException {
		if (props == null) {
			throw new NullPointerException("Awaited call can't be null");
		}
		else if (!isPropertyClassAvailable(awaited)) {
			if (isExtraPropertiesSupported()) {
				if (hasExplicitProp(awaited)) {
					return (T) props.get(awaited);
				}
			}
			if (getParent() != null) {
				return getParent().getProp(awaited);
			}
			else {
				throw new IllegalArgumentException("Awaited property class ["+awaited.getCanonicalName()+"] is not supported or not found anywhere in the entites hierarchy");
			}
		}
		else {
			if (!hasExplicitProp(awaited)) {
				if (getParent() != null) {
					return getParent().getProp(awaited);
				}
				else {
					return null;
				}
			}
			else {
				return (T) props.get(awaited);
			}
		}
	}
	
	/**
	 * <p>Set property value for the given property class</p>
	 * @param <T> Property type
	 * @param awaited property class to store
	 * @param prop property value to store
	 * @return previous property value (can be null)
	 * @throws IllegalArgumentException property class is not supported for the given entity
	 */
	protected <T extends EntityProp> T setProp(final Class<T> awaited, final T prop) throws IllegalArgumentException {
		if (isPropertyClassAvailable(awaited) || isExtraPropertiesSupported()) {
			if (prop == null) {
				return (T) props.remove(awaited);
			}
			else {
				return (T) props.put(awaited,prop);
			}
		}
		else {
			throw new IllegalArgumentException("Property class ["+awaited.getCanonicalName()+"] to set is not supported for the given entity");
		}
	}
	
	/**
	 * <p>Check the property class is available for the current entity</p>
	 * @param clazz class to check
	 * @return true if the class is available
	 */
	protected boolean isPropertyClassAvailable(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassAvailable(clazz);
	}
	
	/**
	 * <p>Check the property class is mandatory for the current entity</p>
	 * @param clazz class to check
	 * @return true if the class is mandatory
	 */
	protected boolean isPropertyClassMandatory(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassMandatory(clazz);
	}
	
	/**
	 * <p>Weather extra properties are available for the given entity</p>
	 * @return
	 */
	protected boolean isExtraPropertiesSupported() {
		return checker.supportsExtraProperties();
	}

	protected void checkProperties() throws IllegalArgumentException {
		final Map<Class<? extends EntityProp>,EntityProp>	effectiveProps = new HashMap<>();
		final Set<Class<? extends EntityProp>>				effectiveAvailables = new HashSet<>();
		final Set<Class<? extends EntityProp>>				effectiveMandatories = new HashSet<>();
		final StringBuilder	sb = new StringBuilder();
		
		
		collectProperties(effectiveProps);
		collectAvailables(effectiveAvailables);
		collectMandatories(effectiveMandatories);
		
		for (Entry<Class<? extends EntityProp>, EntityProp> item : effectiveProps.entrySet()) {
			if (!effectiveAvailables.contains(item.getKey())) {
				if (!isExtraPropertiesSupported()) {
					sb.append("Unsupported property ["+item.getKey().getCanonicalName()+"] was detected\n");
				}
			}
		}
		for (Class<? extends EntityProp> item : effectiveMandatories) {
			if (!effectiveProps.containsKey(item) || effectiveProps.get(item) == null) {
				sb.append("Mandatory property ["+item.getCanonicalName()+"] is missing\n");
			}
		}
		if (!sb.isEmpty()) {
			throw new IllegalArgumentException("Check erros for ["+this.getClass().getCanonicalName()+"] was detected:\n"+sb);
		}
	}
	
	protected void collectAvailables(final Set<Class<? extends EntityProp>> result) {
		result.addAll(Arrays.asList(checker.available));
	}

	protected void collectMandatories(final Set<Class<? extends EntityProp>> result) {
		result.addAll(Arrays.asList(checker.mandatory));
	}

	void collectProperties(final Map<Class<? extends EntityProp>,EntityProp> result) {
		if (getParent() != null) {
			getParent().collectProperties(result);
		}
		result.putAll(props);
	}
	
	
	protected static class BasicEntityComponentChecker {
		protected final Class<? extends EntityProp>[] available;
		protected final Class<? extends EntityProp>[] mandatory;
		protected final boolean		allowExtraProperties;
		
		protected BasicEntityComponentChecker(final Class<? extends EntityProp>[] available, final Class<? extends EntityProp>[] mandatory, final boolean allowExtraProperties) {
			this.available = available;
			this.mandatory = mandatory;
			this.allowExtraProperties = allowExtraProperties;
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
		
		@Override
		public String toString() {
			return "BasicEntityComponentChecker [available=" + Arrays.toString(available) + ", mandatory=" + Arrays.toString(mandatory) + ", allowExtraProperties=" + allowExtraProperties + "]";
		}
	}
	
}
