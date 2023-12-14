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

public class JJ {
	public static final int	BYTE_SIZE = 1;
	public static final int	WORD_SIZE = 2;
	public static final int	DWORD_SIZE = 4;
	public static final int	QWORD_SIZE = 8;
	
	public static final int	MMAP_PROT_EXEC = 1;
	public static final int	MMAP_PROT_READ = 1;
	public static final int	MMAP_PROT_WRITE = 1;

	public static final int	MMAP_SHARED = 1;
	public static final int	MMAP_SHARED_VALIDATE = 1;
	public static final int	MMAP_PRIVATE = 1;
	public static final int	MMAP_ANON = 1;
	public static final int	MMAP_MORESERVE = 1;
	
	static long		fixedEntries;
	static long		mapAddress;
	static long		mapSize;
	static int		argCount;
	static long 	argArrayRef;

	@JJFIxedEntry(0)
	public static native long open(final char[] file, final int from, final int length, final int flags);
	@JJFIxedEntry(1)
	public static native int read(final long fd, final byte[] content, final int from, final int length);
	@JJFIxedEntry(2)
	public static native void close(final long fd);
	@JJFIxedEntry(3)
	public static native long filelength(final char[] file, final int from, final int length);
	@JJFIxedEntry(4)
	public static native long opendir(final char[] dir, final int from, final int length, int flags);
	@JJFIxedEntry(5)
	public static native int readdir(final long dd, final char[] buffer, final int from, final int length);
	@JJFIxedEntry(6)
	public static native void closedir(final long dd);
	@JJFIxedEntry(7)
	public static native long mmap(final long fd, final long size, final int flags);
	@JJFIxedEntry(8)
	public static native void munmap(final long addr, final long size);
	@JJFIxedEntry(9)
	public static native Object asObject(final long ref);
	@JJFIxedEntry(10)
	public static native long asLong(final Object ref);
	@JJFIxedEntry(11)
	public static native int load1(final long ref);
	@JJFIxedEntry(12)
	public static native int load2(final long ref);
	@JJFIxedEntry(13)
	public static native int load4(final long ref);
	@JJFIxedEntry(14)
	public static native long load8(final long ref);
	@JJFIxedEntry(15)
	public static native void store1(final long ref, final int val);
	@JJFIxedEntry(16)
	public static native void store2(final long ref, final int val);
	@JJFIxedEntry(17)
	public static native void store4(final long ref, final int val);
	@JJFIxedEntry(18)
	public static native void store8(final long ref, final long val);
	
	
	public static int startup(final long mapAddress, final long mapSize, final long fixedEntries, final int argCount, final long argArrayRef) {
		JJ.mapAddress = mapAddress;
		JJ.mapSize = mapSize;
		JJ.argCount = argCount;
		JJ.argArrayRef = argArrayRef;

		final char[][]	parameters = new char[argCount][];
		
		for(int index = 0; index < parameters.length; index++) {
			final long 		startAddr = load8(argArrayRef + QWORD_SIZE * index);
			long			currentAddr = startAddr;
			
			while (load1(currentAddr) != 0) {
				currentAddr++;
			}
			final char[]	parm = new char[(int)(currentAddr - startAddr)];
			
			for(int pos = 0; pos < parm.length; pos++) {
				parm[index] = (char)load1(startAddr + index); 
			}
			parameters[index] = parm;
		}

		
		
		return 0;
	}
	
	public static void shutdown() {
	}
	
	public static void main(final String[] args) throws IOException {
		  Path jmodFile = Path.of("C:/Program Files/Java/jdk-19/jmods/java.base.jmod").toAbsolutePath().normalize();

		    try (FileSystem fileSystem = FileSystems.newFileSystem(jmodFile)) {
		      Files.walk(fileSystem.getRootDirectories().iterator().next()).forEachOrdered(System.err::println);
		    }
		    
		loadVMClasses("", new File(new File("./"), "target/classes"));
		
		System.err.println("Home="+System.getProperty("java.home"));
		
		
		
		
		
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
	
	private static void loadVMClasses(final String path, final File item) throws IOException {
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
	
	private static void loadStartupClasses(final String path, final File item) throws IOException {
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

	private static void loadStartupClasses(final ZipInputStream zis) throws IOException {
		ZipEntry	ze;
		
		while ((ze = zis.getNextEntry()) != null) {
			if (isStartupClass(ze.getName())) {
				loadClass(ze.getName(), zis);
			}
		}
	}
	
	private static boolean isStartupClass(final String className) {
		return className.startsWith("java/lang/") && className.endsWith(".class");
	}

	private static void loadClass(final String className, final InputStream item) throws IOException {
		try(final ByteArrayOutputStream	baos = new ByteArrayOutputStream()) {
			final byte[]	buffer = new byte[8192];
			int				length;
			
			while ((length = item.read(buffer)) > 0) {
				baos.write(buffer, 0, length);
			}
			prepareClass(ClassDefinitionLoader.parse(new ByteArrayReader(baos.toByteArray())));
			System.err.println("Load JAR "+className);
		}
	}	
	
	private static void loadClass(final String className, final File item) throws IOException {
		prepareClass(ClassDefinitionLoader.parse(new ByteArrayReader(Files.readAllBytes(item.toPath()))));
		System.err.println("Load "+className);
	}

	private static void prepareClass(final ClassDescriptor desc) throws IOException {
	}	
	
	private static void dumpContent() {
		
	}
}
