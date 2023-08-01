package chav1961.bt.nlp.dictionary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import chav1961.bt.nlp.interfaces.internal.WordDescriptor;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

//http://opencorpora.org/?page=export
public class DictionaryManager {
	private static final int		MAGIC = 0xFEDAADEF;
	
	private static final Enum<?>[]				INDICES = {
													null, 
													WordDescriptor.Categories.Part,
													WordDescriptor.Part.NOUN,
													WordDescriptor.Part.ADJF,
													WordDescriptor.Part.ADJS,
													WordDescriptor.Part.COMP,
													WordDescriptor.Part.VERB,
													WordDescriptor.Part.INFN,
													WordDescriptor.Part.PRTF,
													null,
													
													WordDescriptor.Part.PRTS,
													WordDescriptor.Part.GRND,
													WordDescriptor.Part.NUMR,
													WordDescriptor.Part.ADVB,
													WordDescriptor.Part.NPRO,
													WordDescriptor.Part.PRED,
													WordDescriptor.Part.PREP,
													WordDescriptor.Part.CONJ,
													WordDescriptor.Part.PRCL,
													WordDescriptor.Part.INTJ,
													
													null,
													WordDescriptor.Categories.Anim,
													WordDescriptor.Anim.ANIM,
													WordDescriptor.Anim.INAN,
													WordDescriptor.Categories.Gndr,
													WordDescriptor.Gndr.MASC,
													WordDescriptor.Gndr.FEMN,
													WordDescriptor.Gndr.NEUT,
													WordDescriptor.Gndr.MASF,
													WordDescriptor.Categories.Numbr,
													
													WordDescriptor.Numbr.SING,
													WordDescriptor.Numbr.PLUR,
													WordDescriptor.Markers.Sgtm,
													WordDescriptor.Markers.Pltm,
													null,
													null,
													WordDescriptor.Markers.Fixd,
													WordDescriptor.Categories.Case,
													WordDescriptor.Case.NOMN,
													WordDescriptor.Case.GENT,
													
													WordDescriptor.Case.DATV,
													WordDescriptor.Case.ACCS,
													WordDescriptor.Case.ABLT,
													WordDescriptor.Case.LOCT,
													WordDescriptor.Case.VOCT,
													WordDescriptor.Case.GEN1,
													WordDescriptor.Case.GEN2,
													WordDescriptor.Case.ACC2,
													WordDescriptor.Case.LOC1,
													WordDescriptor.Case.LOC2,
													
													WordDescriptor.Markers.Abbr,
													WordDescriptor.Markers.Name,
													WordDescriptor.Markers.Surn,
													WordDescriptor.Markers.Patr,
													WordDescriptor.Markers.Orgn,
													WordDescriptor.Markers.Trad,
													WordDescriptor.Markers.Subx,
													WordDescriptor.Markers.Supr,
													WordDescriptor.Markers.Qual,
													WordDescriptor.Markers.Apro,
													
													WordDescriptor.Markers.Anum,
													WordDescriptor.Markers.Poss,
													WordDescriptor.Markers.V_ey,
													WordDescriptor.Markers.V_oy,
													WordDescriptor.Markers.Cmp2,
													WordDescriptor.Markers.V_ej,
													WordDescriptor.Categories.Aspect,
													WordDescriptor.Aspect.PERF,
													WordDescriptor.Aspect.IMPERF,
													
													WordDescriptor.Categories.Trans,
													WordDescriptor.Trans.TRANS,
													WordDescriptor.Trans.INTRANS,
													WordDescriptor.Markers.Impe,
													WordDescriptor.Markers.Impx,
													WordDescriptor.Markers.Mult,
													WordDescriptor.Markers.Refl,
													WordDescriptor.Categories.Person,
													WordDescriptor.Person.FIRST,
													WordDescriptor.Person.SECOND,
													WordDescriptor.Person.THIRD,
													
													WordDescriptor.Categories.Tense,
													WordDescriptor.Tense.PRESENT,
													WordDescriptor.Tense.PAST,
													WordDescriptor.Tense.FUTURE,
													WordDescriptor.Categories.Involve,
													WordDescriptor.Involve.INCL,
													WordDescriptor.Involve.EXCL,
													
													WordDescriptor.Categories.Voice,
													WordDescriptor.Voice.ACTIVE,
													WordDescriptor.Voice.PASSIVE,
													WordDescriptor.Markers.Infr,
													WordDescriptor.Markers.Slng,
													WordDescriptor.Markers.Arch,
													WordDescriptor.Markers.Litr,
													WordDescriptor.Markers.Erro,
													WordDescriptor.Markers.Dist,
													WordDescriptor.Markers.Ques,
													
													WordDescriptor.Markers.Dmns,
													null,
													WordDescriptor.Markers.Prnt,
													WordDescriptor.Markers.V_be,
													WordDescriptor.Markers.V_en,
													WordDescriptor.Markers.V_ie,
													WordDescriptor.Markers.V_bi,
													WordDescriptor.Markers.Fimp,
													WordDescriptor.Markers.Prdx,
													WordDescriptor.Markers.Coun,
													
													WordDescriptor.Markers.Coll,
													WordDescriptor.Markers.V_sh,
													WordDescriptor.Markers.Af_p,
													WordDescriptor.Markers.Inmx,
													WordDescriptor.Markers.Vpre,
													WordDescriptor.Markers.Anph,
													WordDescriptor.Markers.Init,
													WordDescriptor.Markers.Adjx,
													WordDescriptor.Markers.Ms_f,
													
													WordDescriptor.Markers.Hypo,													
												};
	
	private final String[]						linkTypes;
	private final LinkDesc[]					links;
	private final Map<String, Grammema>			grammemas = new HashMap<>();
	private final Grammema[]					orderedGrammemas;
	private final SyntaxTreeInterface<WordDesc>	tree = new AndOrTree<>(1,1);
	
	public DictionaryManager(final InputStream dictionary) throws IOException {
		if (dictionary == null) {
			throw new NullPointerException("Dictionary to load can't be null");
		}
		else {
			try{
				final SAXParserFactory 	factory = SAXParserFactory.newInstance();
				final SAXParser 		saxParser = factory.newSAXParser();
				final MyHandler 		handler = new MyHandler();
				
				saxParser.parse(dictionary, handler);
				
				this.linkTypes = handler.linkTypes.toArray(new String[handler.linkTypes.size()]);
				this.links = handler.links.toArray(new LinkDesc[handler.links.size()]);
				this.orderedGrammemas = grammemas.values().toArray(new Grammema[grammemas.size()]);
				
				Arrays.sort(links, (o1,o2)->o2.from == o1.from ? o2.to - o1.to : o2.from - o1.from);
				Arrays.sort(orderedGrammemas, (o1,o2)->o2.index - o1.index);
			} catch (ParserConfigurationException | SAXException exc) {
				throw new IOException(exc);
			}
		}
	}

	public int nextWord(final char[] content, final int from, final Consumer<WordDescriptor> callback) throws SyntaxException {
		return 0;
	}
	
	public void allWords(final Consumer<WordDescriptor> callback) {
		
	}
	
	public static DictionaryManager download(final DataInputStream is) {
		return null;
	}
	
	public static DictionaryManager upload(final DataOutputStream is) {
		return null;
	}
	
	private class MyHandler extends DefaultHandler {
		private final List<String>					linkTypes = new ArrayList<>();
		private final List<LinkDesc>				links = new ArrayList<>();
		private final StringBuilder					sb = new StringBuilder();
		private int		grammemaCount = 0;
		private int		id;
		private String 	parent = null;
		private String 	initialForm = null;
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
					initialForm = attr.getValue("t");
					break;
				case "f" 		:
					lobits = 0;
					hibits = 0;
					currentForm = attr.getValue("t");
					break;
				case "g" 		:
					final int index = grammemas.get(attr.getValue("v")).index;
					
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
		}
		
		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			switch (qName) {
				case "grammeme"	:
					break;
				case "name"	:
					final String	name = sb.toString();
					
					grammemas.put(name, new Grammema(name, parent, grammemaCount++));
					collect = false;
					break;
				case "lemma" 	:
					break;
				case "l" 		:
					lobitsInitial = lobits;
					hibitsInitial = hibits;
					break;
				case "f" 		:
					final long	nameId = tree.seekName((CharSequence)currentForm);
					
					if (nameId < 1) {
						tree.placeName((CharSequence)currentForm, new WordDesc(id, initialForm, lobits | lobitsInitial, hibits | hibitsInitial));
					}
					else {
						tree.getCargo(nameId).next = new WordDesc(id, initialForm, lobits | lobitsInitial, hibits | hibitsInitial);
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
		final int		id;
		final String	initialForm;
		final long		loBits;
		final long		hiBits;
		WordDesc		next = null;
		
		public WordDesc(final int id, final String initialForm, final long loBits, final long hiBits) {
			this.id = id;
			this.initialForm = initialForm;
			this.loBits = loBits;
			this.hiBits = hiBits;
		}

		@Override
		public String toString() {
			return "WordDesc [id=" + id + ", initialForm=" + initialForm + ", loBits=" + loBits + ", hiBits=" + hiBits + "]";
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
