package chav1961.bt.jj.starter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Preloader {
	public Preloader() {
		
	}
	
	public void preload() throws IOException {
		Path jmodFile = Path.of("C:/Program Files/Java/jdk-19/jmods/java.base.jmod").toAbsolutePath().normalize();

		try (FileSystem fileSystem = FileSystems.newFileSystem(jmodFile)) {
		  Files.walk(fileSystem.getRootDirectories().iterator().next()).forEachOrdered(System.err::println);
		}
		    
		loadVMClasses("", new File(new File("./"), "target/classes"));
		
		for (String item : System.getProperty("java.class.path").split(File.pathSeparator)) {
			final File	path = new File(item);
			
			if (path.isDirectory()) {
				loadStartupClasses("", path);
			}
			else if (path.isFile() && path.getName().endsWith(".jar")) {
				try(final FileInputStream	fis = new FileInputStream(path);
					final ZipInputStream	zis = new ZipInputStream(fis)) {
					
					loadStartupClasses(zis);
				}
			}
		}
		dumpContent();
	}

	private void loadVMClasses(final String path, final File item) throws IOException {
		if (item.isFile() && item.getName().endsWith(".class")) {
			loadClass(path.substring(1)+"."+item.getName().substring(0, item.getName().lastIndexOf('.')), item);
		}
		else {
			final File[]	children = item.listFiles();
			
			if (children != null) {
				for(File child : children) {
					loadVMClasses(path+"."+child.getName(), child);
				}
			}
		}
	}
	
	private void loadStartupClasses(final String path, final File item) throws IOException {
		if (item.isFile() && isStartupClass(item.getName())) {
			loadClass(path.substring(1)+"."+item.getName().substring(0, item.getName().lastIndexOf('.')), item);
		}
		else {
			final File[]	children = item.listFiles();
			
			if (children != null) {
				for(File child : children) {
					loadStartupClasses(path+"."+child.getName(), child);
				}
			}
		}
	}

	private void loadStartupClasses(final ZipInputStream zis) throws IOException {
		ZipEntry	ze;
		
		while ((ze = zis.getNextEntry()) != null) {
			if (isStartupClass(ze.getName())) {
				loadClass(ze.getName(), zis);
			}
		}
	}
	
	private boolean isStartupClass(final String className) {
		return className.startsWith("java/lang/") && className.endsWith(".class");
	}

	private void loadClass(final String className, final InputStream item) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			final byte[]	buffer = new byte[8192];
			int				length;
			
			while ((length = item.read(buffer)) > 0) {
				baos.write(buffer, 0, length);
			}
			prepareClass(ClassDefinitionLoader.parse(new ByteArrayReader(baos.toByteArray())));
		}
	}	
	
	private void loadClass(final String className, final File item) throws IOException {
		prepareClass(ClassDefinitionLoader.parse(new ByteArrayReader(Files.readAllBytes(item.toPath()))));
	}

	private void prepareClass(final ClassDescriptor desc) throws IOException {
		for (MethodItem item : desc.getMethods()) {
			final byte[]	compiled = ZeroCodeGen.compile(item);
			
		}
	}	
	
	private void dumpContent() {
		
	}
	
}
