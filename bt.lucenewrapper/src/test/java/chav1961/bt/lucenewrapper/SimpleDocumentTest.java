package chav1961.bt.lucenewrapper;

import org.junit.Assert;
import org.junit.Test;

public class SimpleDocumentTest {
	@Test
	public void basicTest() {
		final SimpleDocument2Save	sds = new SimpleDocument2Save(), sds1 = new SimpleDocument2Save(); 

		Assert.assertEquals(sds.hashCode(), sds1.hashCode());
		Assert.assertEquals(sds.toString(), sds1.toString());
		Assert.assertTrue(sds.equals(sds1));
		
		Assert.assertEquals("", sds.getTitle());
		Assert.assertEquals("", sds.getAuthor());
		Assert.assertEquals("", sds.getAnnotation());
		Assert.assertEquals("", sds.getText());
		Assert.assertEquals(0, sds.getTags().size());
		Assert.assertEquals(0, sds.getKeywords().size());
		
		sds.setTitle(null);
		Assert.assertEquals("", sds.getTitle());
		sds.setTitle("test");
		Assert.assertEquals("test", sds.getTitle());

		sds.setAuthor(null);
		Assert.assertEquals("", sds.getAuthor());
		sds.setAuthor("test");
		Assert.assertEquals("test", sds.getAuthor());

		sds.setAnnotation(null);
		Assert.assertEquals("", sds.getAnnotation());
		sds.setAnnotation("test");
		Assert.assertEquals("test", sds.getAnnotation());

		sds.setText(null);
		Assert.assertEquals("", sds.getText());
		sds.setText("test");
		Assert.assertEquals("test", sds.getText());
	}
}
