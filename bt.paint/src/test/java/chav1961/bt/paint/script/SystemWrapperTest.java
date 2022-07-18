package chav1961.bt.paint.script;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import chav1961.bt.paint.interfaces.PaintScriptException;
import chav1961.bt.paint.script.interfaces.ImageWrapper;
import chav1961.bt.paint.script.interfaces.PropertiesWrapper;
import chav1961.bt.paint.script.interfaces.SystemWrapper;
import chav1961.purelib.basic.SubstitutableProperties;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.fsys.interfaces.FileSystemInterface;

public class SystemWrapperTest {
	private static final File	wd = new File(System.getProperty("java.io.tmpdir"));
	private static final File	folder = new File(wd, "test.tmp");

	@BeforeClass
	public static void prepare() throws FileNotFoundException, IOException {
		try{Utils.deleteDir(folder);
		} catch (IllegalArgumentException exc) {
		}
		folder.mkdirs();
		final File	innerDir = new File(folder,"dir"); 
		
		innerDir.mkdir();
		new FileOutputStream(new File(folder,"file")).close();
		new FileOutputStream(new File(innerDir,"file1")).close();
		new FileOutputStream(new File(innerDir,"file2")).close();
	}

	@Test
	public void propertiesTest() throws IOException, PaintScriptException {		
		try(final FileSystemInterface	fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"))) {
			final SystemWrapper			sw = new SystemWrapperImpl(fsi, folder.toURI());
			
			try{sw.get("unknown");
				Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
			} catch (PaintScriptException exc) {
			}
			try{sw.get(null); 
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.get("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			Assert.assertEquals("123", sw.get("unknown","123"));
			try{sw.get(null, "123");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.get("", "123");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			} 
			try{sw.get("unknown", (String)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			
			Assert.assertNotNull(sw.get("java.io.tmpdir"));
			
			Assert.assertFalse(sw.contains("unknown"));
			Assert.assertTrue(sw.contains("java.io.tmpdir"));
			try{sw.contains(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.contains("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}	

	@Test
	public void keysAndConversionTest() throws IOException, PaintScriptException {		
		try(final FileSystemInterface	fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"))) {
			final SystemWrapper			sw = new SystemWrapperImpl(new SubstitutableProperties(Utils.mkProps("key1","100","key2","200")), fsi, folder.toURI());

			Assert.assertArrayEquals(new String[] {"key1","key2"}, sw.getPropKeys());

			try{sw.get("unknown", int.class);
				Assert.fail("Mandatory exception was not detected (unknown 1-st argument)");
			} catch (PaintScriptException exc) {
			}
			try{sw.get(null, int.class);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.get("", int.class);
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.get("key1", (Class<?>)null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{sw.get("key1", SubstitutableProperties.class);
				Assert.fail("Mandatory exception was not detected (unsupported 2-nd argument)");
			} catch (PaintScriptException exc) {
			}

			Assert.assertEquals(Integer.valueOf(100), sw.get("unknown", int.class, 100));
			try{sw.get(null, int.class, 100);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.get("", int.class, 100);
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.get("key1", (Class<String>)null, "");
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (NullPointerException exc) {
			}
			try{sw.get("key1", SubstitutableProperties.class, null);
				Assert.fail("Mandatory exception was not detected (unsupported 2-nd argument)");
			} catch (PaintScriptException exc) {
			}
		}
	}
	
	@Test
	public void fileManipulationTest() throws IOException, PaintScriptException {		
		try(final FileSystemInterface	fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"))) {
			final SystemWrapper			sw = new SystemWrapperImpl(fsi, folder.toURI());

			Assert.assertFalse(sw.exists("unknown"));
			Assert.assertTrue(sw.exists("file"));
			Assert.assertTrue(sw.isFile("file"));
			Assert.assertTrue(sw.isDirectory("dir"));
			Assert.assertArrayEquals(new String[] {"file1","file2"}, sw.list("dir"));
			Assert.assertTrue(sw.mkdir("dir1"));
			Assert.assertTrue(sw.ren("dir1","dir2"));
			Assert.assertTrue(sw.exists("dir2"));
			Assert.assertTrue(sw.rm("dir2"));
			
			try{sw.exists(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.exists("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{sw.isFile(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.isFile("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{sw.isDirectory(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.isDirectory("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}

			try{sw.list(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.list("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{sw.mkdir(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.mkdir("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{sw.ren(null,"123");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.ren("","123");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.ren("123",null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.ren("123","");
				Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}

			try{sw.rm(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.rm("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}

	@Test
	public void imageManipulationTest() throws IOException, PaintScriptException {		
		try(final FileSystemInterface	fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"))) {
			final SystemWrapper			sw = new SystemWrapperImpl(fsi, folder.toURI());
			
			final ImageWrapper			iw = new ImageWrapperImpl();
			
			sw.storeImage(iw, iw.getName());
			final ImageWrapper			iwNew = sw.loadImage(iw.getName());
			
			Assert.assertEquals(((BufferedImage)iw.getImage()).getWidth(), ((BufferedImage)iw.getImage()).getWidth());
			Assert.assertEquals(((BufferedImage)iw.getImage()).getHeight(), ((BufferedImage)iw.getImage()).getHeight());
			Assert.assertEquals(((BufferedImage)iw.getImage()).getType(), ((BufferedImage)iw.getImage()).getType());
			Assert.assertEquals(iw.getName(), iwNew.getName());
			Assert.assertEquals(iw.getFormat(), iwNew.getFormat());

			try{sw.loadImage(null);
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.loadImage("");
				Assert.fail("Mandatory exception was not detected (empty 1-st argument)");
			} catch (IllegalArgumentException exc) {
			}
			
			try{sw.storeImage(null, "test");
				Assert.fail("Mandatory exception was not detected (null 1-st argument)");
			} catch (NullPointerException exc) {
			}
			try{sw.storeImage(iw, null);
				Assert.fail("Mandatory exception was not detected (null 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
			try{sw.storeImage(iw, "");
				Assert.fail("Mandatory exception was not detected (empty 2-nd argument)");
			} catch (IllegalArgumentException exc) {
			}
		}
	}

	@Test
	public void propertiesManipulationTest() throws IOException, PaintScriptException {		
		try(final FileSystemInterface	fsi = FileSystemInterface.Factory.newInstance(URI.create("fsys:file:/"))) {
			final SystemWrapper			sw = new SystemWrapperImpl(fsi, folder.toURI());
			final PropertiesWrapper		pw = PropertiesWrapper.of(Utils.mkProps("key1","value1","key2","value2"));
			
			sw.storeProps(pw, "test.properties");
			final PropertiesWrapper		pwNew = sw.loadProps("test.properties");
			
			Assert.assertEquals(pw.getProperties(), pwNew.getProperties());
		}
	}
	
	@AfterClass
	public static void unprepare() {
		try{Utils.deleteDir(folder);
		} catch (IllegalArgumentException exc) {
		}
	}
}
