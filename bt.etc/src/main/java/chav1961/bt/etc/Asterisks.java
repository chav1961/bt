package chav1961.bt.etc;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class Asterisks extends JComponent {
	private static final long  serialVersionUID = 4066983457124156696L; // <1> 
	private static final String		SOFT_TITLE = "Methyl \u2B24 Cyanide \u2B24 Software \u2B24 ";
	private static final String		TEST_TITLE = "Test title for testing";
	private static final double		ASTERISK_CYRCLE_RADIUS = 0.4;
	private static final double		LETTER_SIZE = 0.075;
	private static final double		WINDOW_SIZE = 1.5;
	private static final float		LINE_WIDTH = 0.0025f;
	private static final Ball[]		BALLS = {new Ball(-0.09, -0.075, 0.05, Color.GRAY),
			 								 new Ball(-0.025, 0.085, 0.05, Color.GRAY),
											 new Ball(-0.075, 0.025, 0.1, Color.BLACK),
											 new Ball(-0.15, 0.085, 0.05, Color.GRAY),
											 new Ball(0, 0, 0.1, Color.BLACK),
											 new Ball(0.075, -0.025, 0.1, Color.BLUE),
											};
	
	@Override
	public void paintComponent(final Graphics g) {	// <2>
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();
		final AffineTransform	at = pickCoordinates(g2d);
		final int				stringSize = SOFT_TITLE.length();
		final int				testSize = TEST_TITLE.length();

		g2d.setTransform(at);
		fillBackground(g2d);
		
		final AffineTransform	displ = new AffineTransform(at);

		displ.translate(-WINDOW_SIZE/6, WINDOW_SIZE/6);
		g2d.setTransform(displ);
		
		for (int index = 0; index < stringSize; index++) {
			paintRotatedLetter(g2d, ASTERISK_CYRCLE_RADIUS, 180 - index * 360 / stringSize, LETTER_SIZE, SOFT_TITLE.charAt(index));
		}
		for (int index = 0; index < BALLS.length; index++) {
			paintBall(g2d, BALLS[index].x, BALLS[index].y, BALLS[index].r, BALLS[index].color);
		}
		g2d.setTransform(at);
		
		for (int index = 0; index < testSize; index++) {
			paintPlainLetter(g2d, -WINDOW_SIZE/2 + WINDOW_SIZE * index / (testSize + 2), -WINDOW_SIZE/6, LETTER_SIZE, TEST_TITLE.charAt(index));
		}
		
		g2d.setTransform(oldAt);
	}

	private AffineTransform pickCoordinates(final Graphics2D g2d) {
		final Dimension		screenSize = this.getSize();
		final AffineTransform	result = new AffineTransform();

		result.scale(screenSize.getWidth()/WINDOW_SIZE, -screenSize.getHeight()/WINDOW_SIZE);
		result.translate(WINDOW_SIZE/2, -WINDOW_SIZE/2);
		return result;
	}
	
	
	private void fillBackground(final Graphics2D g2d) {		// <4>
		final RadialGradientPaint rgp = new RadialGradientPaint(0.0f, 0.0f
										, (float)(0.75f*WINDOW_SIZE)
										, new float[]{0.0f, 1.0f}
										, new Color[]{Color.YELLOW, Color.BLACK});
		
		final Rectangle2D.Double	r2d = new Rectangle2D.Double(-WINDOW_SIZE/2, -WINDOW_SIZE/2, WINDOW_SIZE, WINDOW_SIZE);
		final Paint					oldPaint = g2d.getPaint();
		
		g2d.setPaint(rgp);
		g2d.fill(r2d);
		g2d.setPaint(oldPaint);
	}
	
	private void paintRotatedLetter(final Graphics2D g2d, final double radius, final double angle, final double scale, final char symbol) {
		final Color				oldColor = g2d.getColor();
		final Font				oldFont = g2d.getFont();
		final AffineTransform	oldAt = g2d.getTransform();
		final AffineTransform	at = new AffineTransform(oldAt);
		
		at.translate(radius*Math.cos(Math.PI*angle/180), radius*Math.sin(Math.PI*angle/180));
		at.rotate(Math.PI*(angle + 90)/180);
		at.scale(-scale,scale);

		g2d.setTransform(at);
		
		g2d.setFont(new Font("Courier", Font.BOLD, 1));
		g2d.setColor(Color.BLUE);
		g2d.drawString(new String(new char[]{symbol}),0,0);
		
		g2d.setColor(oldColor);
		g2d.setFont(oldFont);
		g2d.setTransform(oldAt);
	}

	private void paintPlainLetter(final Graphics2D g2d, final double x, final double y, final double scale, final char symbol) {
		final Color				oldColor = g2d.getColor();
		final Font				oldFont = g2d.getFont();
		final AffineTransform	oldAt = g2d.getTransform();
		final AffineTransform	at = new AffineTransform(oldAt);
		
		at.translate(x, y);
		at.scale(scale,-scale);

		g2d.setTransform(at);
		
		g2d.setFont(new Font("Courier", Font.BOLD, 1));
		g2d.setColor(Color.BLUE);
		g2d.drawString(new String(new char[]{symbol}),0,0);
		
		g2d.setColor(oldColor);
		g2d.setFont(oldFont);
		g2d.setTransform(oldAt);
	}
	
	
	private void paintBall(final Graphics2D g2d, final double x, final double y, final double r, final Color color) {
		// TODO Auto-generated method stub
		final Color				oldColor = g2d.getColor();
		final Ellipse2D.Double	r2d = new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r);
		final Stroke			oldStroke = g2d.getStroke();
		
		g2d.setColor(color);
		g2d.fill(r2d);
		g2d.setColor(Color.WHITE);
		g2d.setStroke(new BasicStroke(LINE_WIDTH));
		g2d.draw(r2d);
		g2d.setColor(oldColor);
	}

	
	public static void main(String[] args) {
		final JFrame	frame = new JFrame("Test ");
		
		frame.setPreferredSize(new Dimension(800,800));
		frame.getContentPane().add(new Asterisks(),BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
	private static class Ball {
		private final double	x;
		private final double	y;
		private final double	r;
		private final Color		color;
		
		public Ball(double x, double y, double r, Color color) {
			this.x = x;
			this.y = y;
			this.r = r;
			this.color = color;
		}

		@Override
		public String toString() {
			return "Ball [x=" + x + ", y=" + y + ", r=" + r + ", color=" + color + "]";
		}
	}
}
