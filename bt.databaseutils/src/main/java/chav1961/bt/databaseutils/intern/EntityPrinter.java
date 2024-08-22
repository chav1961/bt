package chav1961.bt.databaseutils.intern;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.PrintingException;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.char2char.CodePrintStreamWrapper;

public class EntityPrinter {
	private static final String		IMPORTS = "";
	
	private final ContentNodeMetadata	table;
	private final ContentNodeMetadata[]	keys;
	private final String				root;
	
	public EntityPrinter(final ContentNodeMetadata table, final ContentNodeMetadata[] keys, final String root) {
		if (table == null) {
			throw new NullPointerException("Table can't be null");
		}
		else if (keys == null || keys.length == 0) {
			throw new IllegalArgumentException("Keys list can't be null or empty array");
		}
		else if (Utils.checkEmptyOrNullString(root)) {
			throw new IllegalArgumentException("Root path can't be null or empty");
		}
		else {
			this.table = table;
			this.keys = keys;
			this.root = root;
		}
	}
	
	public void print(final CodePrintStreamWrapper pw) throws IOException, PrintingException {
		final Set<Class<?>>	classes = new HashSet<>();
		
		pw.println("package "+root.replace('/', '.')+";");
		pw.println();
		for(ContentNodeMetadata column : table) {
			classes.add(column.getType());
		}
		pw.println(IMPORTS);
		for (Class<?> item : classes) {
			if (!item.isPrimitive() && !item.isArray() && !"java.lang".equals(item.getPackage().getName())) {
				pw.println("import "+item.getCanonicalName()+";");
			}
		}
		pw.println();
		pw.println("@Entity");
		pw.println("@Table(name=\""+table.getName().toUpperCase()+"\")");
		pw.println("public class "+table.getName()+" {");
		pw.enter();
		if (keys.length > 1) {
			pw.println("@EmbeddedId");
			pw.println("private PrimaryKey pk = new PrimaryKey();");
			pw.println();
		}
		else {
			pw.println("@Id");
			pw.println("@GeneratedValue(strategy = GenerationType.UUID)");
			pw.println("@Column(name = \""+keys[0].getName().toUpperCase()+"\")");
			pw.println("private "+keys[0].getType().getSimpleName()+" "+keys[0].getName()+";");
			pw.println();
		}
		for (ContentNodeMetadata column : table) {
			if (!URIUtils.parseQuery(column.getApplicationPath()).containsKey("pkSeq")) {
				pw.println("@Column(name = \""+column.getName().toUpperCase()+"\")");
				pw.println("private "+keys[0].getType().getSimpleName()+" "+keys[0].getName()+";");
				pw.println();
			}
		}
		for (ContentNodeMetadata column : table) {
			if (!URIUtils.parseQuery(column.getApplicationPath()).containsKey("pkSeq")) {
				final String	name = column.getName();
				final String	capsName = Character.toUpperCase(name.charAt(0))+name.substring(1);
				
				pw.println("public "+column.getType().getSimpleName()+" get"+capsName+"() {");
				pw.enter().println("return "+name+";").leave();
				pw.println("}");
				pw.println();
				pw.println("public void set"+capsName+"(final "+column.getType().getSimpleName()+" value) {");
				pw.enter().println("this."+name+" = value;").leave();
				pw.println("}");
				pw.println();
			}
		}
		if (keys.length > 1) {
			pw.println();
			pw.println("@Embeddable");
			pw.println("public static class PrimaryKey implements Serializable {");
			pw.println("private static final long serialVersionUID = 1;");
			for(ContentNodeMetadata item : keys) {
				pw.println("@Column(name = \""+item.getName().toUpperCase()+"\")");
				pw.println("p "+item.getType().getSimpleName()+" "+item.getName()+";");
			}
			pw.println("}");
		}
		pw.leave();
		pw.println("}");
	}
}
