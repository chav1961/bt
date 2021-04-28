package chav1961.bt.mnemoed.entities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import chav1961.bt.mnemoed.entities.BasicEntityComponent.BasicEntityComponentChecker;
import chav1961.purelib.basic.subscribable.Subscribable;

public abstract class BasicEntityContainer extends BasicEntityComponent implements Iterable<BasicEntityComponent>{
	private static final BasicEntityComponentChecker	checker = new BasicEntityComponentChecker(
			new Class[] {BackgroundProp.class, BorderProp.class}, 
			new Class[] {BackgroundProp.class, BorderProp.class},
			true);
	
	private final List<BasicEntityComponent>	entities = new ArrayList<>();

	BasicEntityContainer(final Map<String, Subscribable<?>> vars) {
		super(vars);
	}

	public BackgroundProp getBackground() {
		return getProp(BackgroundProp.class);
	}
	
	public void setBackground(final BackgroundProp prop) {
		setProp(prop);
	}
	
	public BorderProp getBorder() {
		return getProp(BorderProp.class);
	}
	
	public void setBorder(final BorderProp prop) {
		setProp(prop);
	}
	
	protected void checkProperties(final EntityProp[] props) throws IllegalArgumentException {
		checker.totalCheck(props);
		super.checkProperties(props);
	}
	
	protected boolean isPropertyClassAvailable(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassAvailable(clazz) || super.isPropertyClassAvailable(clazz);
	}
	
	protected boolean isPropertyClassMandatory(final Class<? extends EntityProp> clazz) {
		return checker.isPropClassMandatory(clazz) || super.isPropertyClassMandatory(clazz);
	}
	
	protected boolean isExtraPropertiesSupported() {
		return checker.supportsExtraProperties() && checker.supportsExtraProperties();
	}
	
	protected void add(final BasicEntityComponent component) throws NullPointerException {
		if (component == null) {
			throw new NullPointerException("Component to add can't be null"); 
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
			
		    public boolean hasNext() {
		    	return index >= 0 && index < entities.size();
		    }

		    public BasicEntityComponent next() {
		    	if (hasNext()) {
		    		return entities.get(index--);
		    	}
		    	else {
		    		return null;
		    	}
		    }

		    public void remove() {
		        if (hasNext()) {
		        	final BasicEntityComponent	removed = entities.remove(index+1);
		        	
		        	removed.setParent(null);
		        }
		    }
		};
	}
}
