package chav1961.bt.mnemoed.entities;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.GeneralPath;
import java.net.URI;

import org.junit.Assert;
import org.junit.Test;


public class PropTest {
	@Test
	public void anglePropTest() {
		final PrimitiveConstantValueSource	angle1 = new PrimitiveConstantValueSource(100), angle2 = new PrimitiveConstantValueSource(200);
		final AngleProp						angleProp = new AngleProp(angle1);
		
		Assert.assertEquals(angle1, angleProp.getAngle());
		angleProp.setAngle(angle2);
		Assert.assertEquals(angle2, angleProp.getAngle());
		Assert.assertEquals(new AngleProp(angle2).toString(), angleProp.toString());
	}

	@Test
	public void colorAndBackgroundPropTest() {
		final ObjectConstantValueSource<Color>	color1 = new ObjectConstantValueSource<Color>(Color.BLACK), color2 = new ObjectConstantValueSource<Color>(Color.WHITE);
		final ColorProp							colorProp = new ColorProp(color1);
		
		Assert.assertEquals(color1, colorProp.getColor());
		colorProp.setColor(color2);
		Assert.assertEquals(color2, colorProp.getColor());
		Assert.assertEquals(new ColorProp(color2).toString(), colorProp.toString());

		final BackgroundProp					bkProp = new BackgroundProp(color1);
		
		Assert.assertEquals(color1, bkProp.getBackground());
		bkProp.setBackground(color2);
		Assert.assertEquals(color2, bkProp.getBackground());
		Assert.assertEquals(new BackgroundProp(color2).toString(), bkProp.toString());
	}

	@Test
	public void borderPropTest() {
		final GeneralPath		gp1 = new GeneralPath(), gp2 = new GeneralPath();
		
		gp1.moveTo(0,0);	gp1.lineTo(0, 10);	gp1.lineTo(10, 10);	gp1.lineTo(10, 0); gp1.closePath();
		gp2.moveTo(0,0);	gp2.lineTo(0, 20);	gp2.lineTo(20, 20);	gp2.lineTo(20, 0); gp2.closePath();
		
		final ObjectConstantValueSource<GeneralPath>	path1 = new ObjectConstantValueSource<GeneralPath>(gp1), path2 = new ObjectConstantValueSource<GeneralPath>(gp2);
		final BorderProp						borderProp = new BorderProp(path1);

		Assert.assertEquals(path1, borderProp.getBorder());
		borderProp.setBorder(path2);
		Assert.assertEquals(path2, borderProp.getBorder());
		Assert.assertEquals(new BorderProp(path2).toString(), borderProp.toString());
	}

	@Test
	public void fontPropTest() {
		final FontProp		fontProp = new FontProp(new ObjectConstantValueSource<String>(Font.MONOSPACED),
													new PrimitiveConstantValueSource(12),
													new PrimitiveConstantValueSource(Font.PLAIN)
													);
		
		Assert.assertEquals(new ObjectConstantValueSource<String>(Font.MONOSPACED), fontProp.getFamily());
		fontProp.setFamily(new ObjectConstantValueSource<String>(Font.DIALOG));
		Assert.assertEquals(new ObjectConstantValueSource<String>(Font.DIALOG), fontProp.getFamily());
		
		Assert.assertEquals(new PrimitiveConstantValueSource(12), fontProp.getSize());
		fontProp.setSize(new PrimitiveConstantValueSource(24));
		Assert.assertEquals(new PrimitiveConstantValueSource(24), fontProp.getSize());
		
		Assert.assertEquals(new PrimitiveConstantValueSource(Font.PLAIN), fontProp.getStyle());
		fontProp.setStyle(new PrimitiveConstantValueSource(Font.BOLD));
		Assert.assertEquals(new PrimitiveConstantValueSource(Font.BOLD), fontProp.getStyle());

		Assert.assertEquals(new FontProp(new ObjectConstantValueSource<String>(Font.DIALOG),
										new PrimitiveConstantValueSource(24),
										new PrimitiveConstantValueSource(Font.BOLD)
										).toString(), 
							fontProp.toString());
	}

	@Test
	public void locationAndScalePropTest() {
		final LocationProp	locationProp = new LocationProp(new PrimitiveConstantValueSource(10), new PrimitiveConstantValueSource(20));
		
		Assert.assertEquals(new PrimitiveConstantValueSource(10), locationProp.getxLocation());
		locationProp.setxLocation(new PrimitiveConstantValueSource(15));
		Assert.assertEquals(new PrimitiveConstantValueSource(15), locationProp.getxLocation());

		Assert.assertEquals(new PrimitiveConstantValueSource(20), locationProp.getyLocation());
		locationProp.setyLocation(new PrimitiveConstantValueSource(25));
		Assert.assertEquals(new PrimitiveConstantValueSource(25), locationProp.getyLocation());

		Assert.assertEquals(new LocationProp(new PrimitiveConstantValueSource(15), new PrimitiveConstantValueSource(25)).toString(), locationProp.toString());

		final ScaleProp		scaleProp = new ScaleProp(new PrimitiveConstantValueSource(10), new PrimitiveConstantValueSource(20));
		
		Assert.assertEquals(new PrimitiveConstantValueSource(10), scaleProp.getXScale());
		scaleProp.setXScale(new PrimitiveConstantValueSource(15));
		Assert.assertEquals(new PrimitiveConstantValueSource(15), scaleProp.getXScale());

		Assert.assertEquals(new PrimitiveConstantValueSource(20), scaleProp.getYScale());
		scaleProp.setYScale(new PrimitiveConstantValueSource(25));
		Assert.assertEquals(new PrimitiveConstantValueSource(25), scaleProp.getYScale());

		Assert.assertEquals(new ScaleProp(new PrimitiveConstantValueSource(15), new PrimitiveConstantValueSource(25)).toString(), scaleProp.toString());
	}

	@Test
	public void textAndTooltipPropTest() {
		final TextProp	textProp = new TextProp(new ObjectConstantValueSource<String>("test"), new ObjectConstantValueSource<EntityAlignment>(EntityAlignment.CENTER));
		
		Assert.assertEquals(new ObjectConstantValueSource<String>("test"), textProp.getText());
		textProp.setText(new ObjectConstantValueSource<String>("replaced"));
		Assert.assertEquals(new ObjectConstantValueSource<String>("replaced"), textProp.getText());
		
		Assert.assertEquals(new ObjectConstantValueSource<EntityAlignment>(EntityAlignment.CENTER), textProp.getAlign());
		textProp.setAlign(new ObjectConstantValueSource<EntityAlignment>(EntityAlignment.NORTH));
		Assert.assertEquals(new ObjectConstantValueSource<EntityAlignment>(EntityAlignment.NORTH), textProp.getAlign());
		
		Assert.assertEquals(new TextProp(new ObjectConstantValueSource<String>("replaced"), new ObjectConstantValueSource<EntityAlignment>(EntityAlignment.NORTH)).toString(), textProp.toString());

		final TooltipProp	ttProp = new TooltipProp(new ObjectConstantValueSource<String>("test"));

		Assert.assertEquals(new ObjectConstantValueSource<String>("test"), ttProp.getTooltip());
		ttProp.setTooltip(new ObjectConstantValueSource<String>("replaced"));
		Assert.assertEquals(new ObjectConstantValueSource<String>("replaced"), ttProp.getTooltip());

		Assert.assertEquals(new TooltipProp(new ObjectConstantValueSource<String>("replaced")).toString(), ttProp.toString());
	}

	@Test
	public void uriPropTest() {
		final URIProp	uriProp = new URIProp(new ObjectConstantValueSource<URI>(URI.create("file:./f1")));

		Assert.assertEquals(new ObjectConstantValueSource<URI>(URI.create("file:./f1")), uriProp.getUri());
		uriProp.setUri(new ObjectConstantValueSource<URI>(URI.create("file:./f2")));
		Assert.assertEquals(new ObjectConstantValueSource<URI>(URI.create("file:./f2")), uriProp.getUri());

		Assert.assertEquals(new URIProp(new ObjectConstantValueSource<URI>(URI.create("file:./f2"))).toString(), uriProp.toString());
	}

	@Test
	public void visibilityPropTest() {
		final VisibilityProp	visibilityProp = new VisibilityProp(new PrimitiveConstantValueSource(true));

		Assert.assertEquals(new PrimitiveConstantValueSource(true), visibilityProp.getVisibility());
		visibilityProp.setVisibility(new PrimitiveConstantValueSource(false));
		Assert.assertEquals(new PrimitiveConstantValueSource(false), visibilityProp.getVisibility());

		Assert.assertEquals(new VisibilityProp(new PrimitiveConstantValueSource(false)).toString(), visibilityProp.toString());
	}
}
