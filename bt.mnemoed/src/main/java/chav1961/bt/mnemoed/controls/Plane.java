package chav1961.bt.mnemoed.controls;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import chav1961.bt.mnemoed.entities.BackgroundProp;
import chav1961.bt.mnemoed.entities.BasicEntityComponent;
import chav1961.bt.mnemoed.entities.BasicEntityContainer;
import chav1961.bt.mnemoed.entities.ColorProp;
import chav1961.bt.mnemoed.entities.EntityAlignment;
import chav1961.bt.mnemoed.entities.FontProp;
import chav1961.bt.mnemoed.entities.HyperlinkComponent;
import chav1961.bt.mnemoed.entities.LabelComponent;
import chav1961.bt.mnemoed.entities.LocationProp;
import chav1961.bt.mnemoed.entities.ObjectConstantValueSource;
import chav1961.bt.mnemoed.entities.PrimitiveConstantValueSource;
import chav1961.bt.mnemoed.entities.TextProp;
import chav1961.bt.mnemoed.entities.TooltipProp;
import chav1961.bt.mnemoed.entities.VisibilityProp;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.streams.JsonStaxParser;
import chav1961.purelib.streams.JsonStaxPrinter;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;

public class Plane extends JBackgroundComponent {
	private static final long serialVersionUID = -831480745460062449L;

	@FunctionalInterface
	private interface DrawingInterface {
		void draw(Graphics2D g2d, BasicEntityComponent item);
	}
	
	private static final Map<Class<?>,DrawingInterface>	MAPPER = new HashMap<>(); 
	private static final Map<TextAttribute, Integer> 	UNDERLINE_FONT = new HashMap<TextAttribute, Integer>();
	
	
	static {
		MAPPER.put(LabelComponent.class, Plane::drawLabel);
		MAPPER.put(HyperlinkComponent.class, Plane::drawHyperlink);

		UNDERLINE_FONT.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
	}
	

	public Plane(final Localizer localizer) {
		super(localizer);
		setLayout(null);
	}

	protected BasicEntityComponent getRootComponent() {
		final HyperlinkComponent	result = new HyperlinkComponent(new HashMap<>());
		
		result.setColor(new ColorProp(new ObjectConstantValueSource(Color.BLUE)));
		result.setVisibility(new VisibilityProp(new PrimitiveConstantValueSource(true)));
		result.setLocation(new LocationProp(new PrimitiveConstantValueSource(20), new PrimitiveConstantValueSource(20)));
		result.setTooltip(new TooltipProp(new ObjectConstantValueSource("tooltip")));
		result.setBackground(new BackgroundProp(new ObjectConstantValueSource(Color.RED)));
		result.setText(new TextProp(new ObjectConstantValueSource("tooltip"), new ObjectConstantValueSource(EntityAlignment.WEST)));
		result.setFont(new FontProp(new ObjectConstantValueSource("Monospace"), new PrimitiveConstantValueSource(12), new PrimitiveConstantValueSource(Font.BOLD)));
		return result;
	}
	
	@Override
	public void paintComponent(final Graphics g) {
		super.paintComponent(g);
		paintComponents((Graphics2D)g, getRootComponent());
	}
	
	private void paintComponents(final Graphics2D g2d, final BasicEntityComponent component) {
		if (((PrimitiveConstantValueSource)component.getVisibility().getVisibility()).getBooleanValue()) {
			MAPPER.get(component.getClass()).draw(g2d, component);
			
			if (component instanceof BasicEntityContainer) {
				for (BasicEntityComponent item : ((BasicEntityContainer)component)) {
					paintComponents(g2d,item);
				}
			}
		}
	}
	
	public void serialize(final JsonStaxPrinter printer) throws PrintingException {
		
	}

	public void deserialize(final JsonStaxParser printer) throws SyntaxException {
		
	}

	private static void drawLabel(final Graphics2D g2d, final BasicEntityComponent item) {
		final LabelComponent	lc = (LabelComponent)item;
		final Color				oldColor = g2d.getColor();
		final Font				oldFont = g2d.getFont();
		final int				fontSize = ((PrimitiveConstantValueSource)lc.getFont().getSize()).getIntValue(); 
		final Font				newFont = new Font(
									((ObjectConstantValueSource<String>)lc.getFont().getFamily()).getObjectValue(),
									((PrimitiveConstantValueSource)lc.getFont().getStyle()).getIntValue(),
									fontSize
								);
		final String			text = ((ObjectConstantValueSource<String>)lc.getText().getText()).getObjectValue();
		final int				x = ((PrimitiveConstantValueSource)lc.getLocation().getxLocation()).getIntValue(); 
		final int				y = ((PrimitiveConstantValueSource)lc.getLocation().getyLocation()).getIntValue(); 
		
		g2d.setFont(newFont);

		if (lc.getBackground().getBackground() != null) {
			final FontMetrics	fm = g2d.getFontMetrics(newFont);
			final int			width = fm.stringWidth(text), height = fm.getHeight();
			
			g2d.setColor(((ObjectConstantValueSource<Color>)lc.getBackground().getBackground()).getObjectValue());
			g2d.fillRect(x, y, width, height);
		}
		
		g2d.setColor(((ObjectConstantValueSource<Color>)lc.getColor().getColor()).getObjectValue());
		g2d.drawString(text,x,y+fontSize);
		
		g2d.setFont(oldFont);
		g2d.setColor(oldColor);
	}

	private static void drawHyperlink(final Graphics2D g2d, final BasicEntityComponent item) {
		final HyperlinkComponent	lc = (HyperlinkComponent)item;
		final Color				oldColor = g2d.getColor();
		final Font				oldFont = g2d.getFont();
		final int				fontSize = ((PrimitiveConstantValueSource)lc.getFont().getSize()).getIntValue(); 
		final Font				newFont = new Font(
									((ObjectConstantValueSource<String>)lc.getFont().getFamily()).getObjectValue(),
									((PrimitiveConstantValueSource)lc.getFont().getStyle()).getIntValue(),
									fontSize
								).deriveFont(UNDERLINE_FONT);
		final String			text = ((ObjectConstantValueSource<String>)lc.getText().getText()).getObjectValue();
		final int				x = ((PrimitiveConstantValueSource)lc.getLocation().getxLocation()).getIntValue(); 
		final int				y = ((PrimitiveConstantValueSource)lc.getLocation().getyLocation()).getIntValue(); 
		
		g2d.setFont(newFont);

		if (lc.getBackground().getBackground() != null) {
			final FontMetrics	fm = g2d.getFontMetrics(newFont);
			final int			width = fm.stringWidth(text), height = fm.getHeight();
			
			g2d.setColor(((ObjectConstantValueSource<Color>)lc.getBackground().getBackground()).getObjectValue());
			g2d.fillRect(x, y, width, height);
		}
		
		g2d.setColor(((ObjectConstantValueSource<Color>)lc.getColor().getColor()).getObjectValue());
		g2d.drawString(text,x,y+fontSize);
		
		g2d.setFont(oldFont);
		g2d.setColor(oldColor);
	}
}
