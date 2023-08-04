package chav1961.bt.nlp.dictionary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

// http://opencorpora.org/?page=export
public class LoadDict {

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
		// TODO Auto-generated method stub
		final SAXParserFactory 	factory = SAXParserFactory.newInstance();
		final SAXParser 		saxParser = factory.newSAXParser();
		final MyHandler 		handler = new MyHandler();
		
		final long startTime = System.currentTimeMillis();
		final long startMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		
		saxParser.parse("c:/tmp/dict.opcorpora.xml", handler);
		
		final long endTime = System.currentTimeMillis();
		
		System.gc();
		final long endMem = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
		
		System.err.println("Total="+handler.count+", duration="+(endTime - startTime)+", memory="+(endMem - startMem));
		
		final long	time1 = System.currentTimeMillis();
		AndOrTree.rawUpload((AndOrTree<?>)handler.tree, new DataOutputStream(OutputStream.nullOutputStream()));
		System.err.println("Upload duration="+(System.currentTimeMillis() - time1));
		
	}

	public static class MyHandler extends DefaultHandler {
		int 			count = 0;
		
		private final List<String>					linkTypes = new ArrayList<>();
		private final Map<String, Grammema>			parents = new HashMap<>();
		private final SyntaxTreeInterface<WordDesc>	tree = new AndOrTree<>(1,1);
		private final List<LinkDesc>				links = new ArrayList<>();
		private final StringBuilder					sb = new StringBuilder();
		private int		grammemaCount = 0;
		private int		id;
		private int		initialId;
		private String 	parent = null;
		private String 	currentForm = null;
		private long	lobits;
		private long	hibits;
		private long	lobitsInitial;
		private long	hibitsInitial;
		private boolean collect = false;
		
		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			if (collect) {
				sb.append(ch, start, length);
			}
	    }
		
		@Override
		public void startElement(final String uri, final String lName, final String qName, final Attributes attr) throws SAXException {
			switch (qName) {
				case "grammeme"	:
					parent = attr.getValue("parent");
					break;
				case "name"	:
					sb.setLength(0);
					collect = true;
					break;
				case "lemma" 	:
					id = Integer.valueOf(attr.getValue("id"));
					break;
				case "l" 		:
					lobitsInitial = 0;
					hibitsInitial = 0;
					lobits = 0;
					hibits = 0;
					initialId = (int)tree.placeName((CharSequence)attr.getValue("t"), null);
					break;
				case "f" 		:
					lobits = 0;
					hibits = 0;
					currentForm = attr.getValue("t");
					break;
				case "g" 		:
					final int index = parents.get(attr.getValue("v")).index;
					
					if (index < 63) {
						lobits |= (1L << index);
					}
					else {
						hibits |= (1L << (index- 64));
					}
					break;
				case "type" 	:
					sb.setLength(0);
					collect = true;
					break;
				case "link" 	:
					links.add(new LinkDesc(Integer.valueOf(attr.getValue("from")), Integer.valueOf(attr.getValue("to")), Integer.valueOf(attr.getValue("type"))));
					break;
			}
			
			if (count++ % 10000 == 0) {
				System.err.print('.');
			}
			if (count % 1000000 == 0) {
				System.err.println();
			}
		}
		
		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			switch (qName) {
				case "grammeme"	:
					break;
				case "name"	:
					final String	name = sb.toString();
					
					parents.put(name, new Grammema(name, parent, grammemaCount++));
					collect = false;
					break;
				case "lemma" 	:
					break;
				case "l" 		:
					lobitsInitial = lobits;
					hibitsInitial = hibits;
					break;
				case "f" 		:
					final long		nameId = tree.seekName((CharSequence)currentForm);
					final WordDesc	word = new WordDesc(id, initialId, lobits | lobitsInitial, hibits | hibitsInitial);
					
					if (nameId < 1) {
						tree.placeName((CharSequence)currentForm, word);
					}
					else {
						final WordDesc	cargo = tree.getCargo(nameId);
						
						if (cargo != null) {
							cargo.next = word;
						}
						else {
							tree.setCargo(nameId, word);
						}
					}
					break;
				case "type" 	:
					linkTypes.add(sb.toString());
					collect = false;
					break;
				case "link" 	:
					break;
			}
		}		
	}

	private static class Grammema {
		final String	name;
		final String	parent;
		final int		index;
		
		public Grammema(String name, String parent, int index) {
			this.name = name;
			this.parent = parent;
			this.index = index;
		}

		@Override
		public String toString() {
			return "Grammema [name=" + name + ", parent=" + parent + ", index=" + index + "]";
		}
	}
	
	private static class WordDesc {
		final int	id;
		final int	initialForm;
		final long	loBits;
		final long	hiBits;
		WordDesc	next = null;
		
		public WordDesc(final int id, final int initialForm, final long loBits, final long hiBits) {
			this.id = id;
			this.initialForm = initialForm;
			this.loBits = loBits;
			this.hiBits = hiBits;
		}

		@Override
		public String toString() {
			return "WordDesc [id=" + id + ", initialForm=" + initialForm + ", loBits=" + loBits + ", hiBits=" + hiBits + "]";
		}
		
		public static void upload(final WordDesc desc, final DataOutputStream dos) throws IOException {
			
		}

		public static WordDesc download(final DataInputStream dis) throws IOException {
			return null;
		}
	}
	
	private static class LinkDesc {
		final int		from;
		final int 		to;
		final int		type;
		
		public LinkDesc(int from, int to, int type) {
			this.from = from;
			this.to = to;
			this.type = type;
		}

		@Override
		public String toString() {
			return "LinkDesc [from=" + from + ", to=" + to + ", type=" + type + "]";
		}
	}
}
