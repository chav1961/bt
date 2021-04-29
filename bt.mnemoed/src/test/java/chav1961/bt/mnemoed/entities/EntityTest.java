package chav1961.bt.mnemoed.entities;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import chav1961.purelib.basic.subscribable.Subscribable;

public class EntityTest {
	@Test
	public void basicEntityComponentTest() {
		final BasicEntityComponent	entity = new PseudoEntityComponent(new HashMap<>());
		final BasicEntityContainer	parent = new PseudoEntityContainer(new HashMap<>());
		
		Assert.assertNull(entity.getParent());
		entity.setParent(parent);
		Assert.assertEquals(parent,entity.getParent());

		// checker test 
		
		Assert.assertTrue(entity.isPropertyClassAvailable(LocationProp.class));
		Assert.assertFalse(entity.isPropertyClassAvailable(TextProp.class));
		
		try{entity.isPropertyClassAvailable(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}

		Assert.assertTrue(entity.isPropertyClassMandatory(LocationProp.class));
		Assert.assertFalse(entity.isPropertyClassMandatory(TextProp.class));
		
		try{entity.isPropertyClassMandatory(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		
		Assert.assertFalse(entity.isExtraPropertiesSupported());
		
		// Properties test
		
		final ColorProp	color = new ColorProp(new ObjectConstantValueSource<Color>(Color.BLACK));
		
		Assert.assertNull(entity.getColor());
		entity.setColor(color);
		Assert.assertEquals(color,entity.getColor());
		
		final TooltipProp	tooltip = new TooltipProp(new ObjectConstantValueSource<String>("test"));
		
		Assert.assertNull(entity.getTooptip());
		entity.setTooltip(tooltip);
		Assert.assertEquals(tooltip,entity.getTooptip());

		final LocationProp	location = new LocationProp(new PrimitiveConstantValueSource(10), new PrimitiveConstantValueSource(20));
		
		Assert.assertNull(entity.getLocation());
		entity.setLocation(location);
		Assert.assertEquals(location,entity.getLocation());
		
		final VisibilityProp	visibility = new VisibilityProp(new PrimitiveConstantValueSource(true));
		
		Assert.assertNull(entity.getVisibility());
		entity.setVisibility(visibility);
		Assert.assertEquals(visibility,entity.getVisibility());
	}

	@Test
	public void basicEntityContainerTest() {
		final BasicEntityComponent	child = new PseudoEntityComponent(new HashMap<>());
		final BasicEntityContainer	entity = new PseudoEntityContainer(new HashMap<>());
	
		// content test
		
		int	count = 0;
		for (BasicEntityComponent item : entity) {
			count++;
		}
		Assert.assertEquals(0, count);
		
		Assert.assertNull(child.getParent());
		entity.add(child);
		Assert.assertEquals(entity, child.getParent());

		try{entity.add(null);
			Assert.fail("Mandatory exception was not detected (null 1-st argument)");
		} catch (NullPointerException exc) {
		}
		try{entity.add(child);
			Assert.fail("Mandatory exception was not detected (attempt to add companent already was added elsewhere)");
		} catch (IllegalArgumentException exc) {
		}
		
		count = 0;
		for (BasicEntityComponent item : entity) {
			count++;
		}
		Assert.assertEquals(1, count);
		
		final Iterator<BasicEntityComponent>	iterator = entity.iterator();
		
		while (iterator.hasNext()) {
			iterator.next();
			iterator.remove();
			
			try{iterator.remove();
				Assert.fail("Mandatory exception was not detected (duplicate removing)");
			} catch (IllegalStateException exc) {
			}
		}

		count = 0;
		for (BasicEntityComponent item : entity) {
			count++;
		}
		Assert.assertEquals(0, count);
		
		// Properties test
		
		final BackgroundProp	bkProp = new BackgroundProp(new ObjectConstantValueSource<Color>(Color.BLACK));
		
		Assert.assertNull(entity.getBackground());
		entity.setBackground(bkProp);
		Assert.assertEquals(bkProp,entity.getBackground());
		
		final GeneralPath		gp = new GeneralPath();		
		final BorderProp		borderProp = new BorderProp(new ObjectConstantValueSource<GeneralPath>(gp));
		
		Assert.assertNull(entity.getBorder());
		entity.setBorder(borderProp);
		Assert.assertEquals(borderProp,entity.getBorder());
		
		final TextProp			textProp = new TextProp(new ObjectConstantValueSource<String>("text"), new ObjectConstantValueSource<EntityAlignment>(EntityAlignment.CENTER));

		Assert.assertFalse(entity.isPropertyClassAvailable(TextProp.class));
		Assert.assertFalse(entity.isPropertyClassMandatory(TextProp.class));
		
		try{entity.getProp(TextProp.class);
			Assert.fail("Mandatory exception was not detected (awaited property is missing anuwhere)");
		} catch (IllegalArgumentException exc) {
		}
		entity.setProp(TextProp.class,textProp);
		Assert.assertEquals(textProp,entity.getProp(TextProp.class));
		
		// Properties inheritance test
		final ColorProp			colorProp = new ColorProp(new ObjectConstantValueSource<Color>(Color.BLACK));

		Assert.assertNull(entity.getColor());
		entity.setColor(colorProp);
		Assert.assertEquals(colorProp,entity.getColor());
		
		Assert.assertTrue(entity.isPropertyClassAvailable(ColorProp.class));
		Assert.assertTrue(entity.isPropertyClassMandatory(ColorProp.class));
	}

	@Test
	public void textComponentTest() {
	}

	@Test
	public void buttonComponentTest() {
	}

	@Test
	public void interactiveComponentTest() {
	}
}


class PseudoEntityComponent extends BasicEntityComponent {
	PseudoEntityComponent(Map<String, Subscribable<?>> vars) {
		super(vars);
	}
}

class PseudoEntityContainer extends BasicEntityContainer {

	PseudoEntityContainer(Map<String, Subscribable<?>> vars) {
		super(vars);
	}
}
