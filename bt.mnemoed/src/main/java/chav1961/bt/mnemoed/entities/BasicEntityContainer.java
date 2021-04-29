package chav1961.bt.mnemoed.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import chav1961.bt.mnemoed.entities.BasicEntityComponent.BasicEntityComponentChecker;
import chav1961.purelib.basic.subscribable.Subscribable;

public abstract class BasicEntityContainer extends BasicEntityComponent implements Iterable<BasicEntityComponent>{
	private static final BasicEntityComponentChecker	checker = new BasicEntityComponentChecker(
			new Class[] {BackgroundProp.class, BorderProp.class}, 
			new Class[] {BackgroundProp.class},
			true);
	
	private final List<BasicEntityComponent>	entities = new ArrayList<>();

	BasicEntityContainer(final Map<String, Subscribable<?>> vars) {
		super(vars);
	}

	public BackgroundProp getBackground() {
		return getProp(BackgroundProp.class);
	}
	
	public void setBackground(final BackgroundProp prop) {
		setProp(BackgroundProp.class,prop);
	}
	
	public BorderProp getBorder() {
		return getProp(BorderProp.class);
	}
	
	public void setBorder(final BorderProp prop) {
		setProp(BorderProp.class,prop);
	}
	
	protected boolean isPropertyClassAvailable(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassAvailable(clazz) || isExtraPropertiesSupported() && super.isPropertyClassAvailable(clazz);
	}
	
	protected boolean isPropertyClassMandatory(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassMandatory(clazz) || isExtraPropertiesSupported() && super.isPropertyClassMandatory(clazz);
	}
	
	protected boolean isExtraPropertiesSupported() {
		return checker.supportsExtraProperties();
	}

	protected void collectAvailables(final Set<Class<? extends EntityProp>> result) {
		super.collectAvailables(result);
		result.addAll(Arrays.asList(checker.available));
	}

	protected void collectMandatories(final Set<Class<? extends EntityProp>> result) {
		super.collectMandatories(result);
		result.addAll(Arrays.asList(checker.mandatory));
	}
	
	protected void add(final BasicEntityComponent component) throws NullPointerException, IllegalArgumentException {
		if (component == null) {
			throw new NullPointerException("Component to add can't be null"); 
		}
		else if (component.getParent() != null) {
			throw new IllegalArgumentException("Component to add already was added into another container"); 
		}
		else {
			entities.add(component);
			component.setParent(this);
		}
	}
	
	@Override
	public Iterator<BasicEntityComponent> iterator() {
		return new Iterator<BasicEntityComponent>() {
			int		index = entities.size()-1;
			boolean	wasRemoved = false;
			
		    public boolean hasNext() {
		    	return index >= 0 && index < entities.size();
		    }

		    public BasicEntityComponent next() {
		    	if (hasNext()) {
		    		wasRemoved = false;
		    		return entities.get(index--);
		    	}
		    	else {
		    		return null;
		    	}
		    }

		    public void remove() {
		    	final int	lastIndex = index + 1;

		    	if (wasRemoved) {
		    		throw new IllegalStateException("Attempt to remove item was removed earlier"); 
		    	}
		    	else if (lastIndex >= 0 && lastIndex < entities.size()) {
		        	final BasicEntityComponent	removed = entities.remove(lastIndex);
		        	
		        	removed.setParent(null);
		        	wasRemoved = true;		        	
		        }
		    }
		};
	}
}
