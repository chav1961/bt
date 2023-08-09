package chav1961.bt.nlp.dictionary;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import chav1961.bt.nlp.interfaces.internal.WordDescriptor;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Anim;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Aspect;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Case;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Gndr;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Involve;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Markers;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Mood;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Numbr;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Part;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Person;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Tense;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Trans;
import chav1961.bt.nlp.interfaces.internal.WordDescriptor.Voice;
import chav1961.purelib.basic.AndOrTree;
import chav1961.purelib.basic.CharUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.SyntaxTreeInterface;

//http://opencorpora.org/?page=export
public class DictionaryManager {
	private static final int		MAGIC = 0xFEDAADEF;
	private static final char[]		LETTERS = "абвгдеёжзийклмнопрстуфхцчшщьыъэюяАБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЬЫЪЭЮЯ".toCharArray();
	
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
	private final SyntaxTreeInterface<List<WordDesc>>	initialTree;
	private final SyntaxTreeInterface<List<WordDesc>>	tree;
	
	public DictionaryManager(final InputStream dictionary) throws IOException {
		if (dictionary == null) {
			throw new NullPointerException("Dictionary to load can't be null");
		}
		else {
			try{
				final SAXParserFactory 	factory = SAXParserFactory.newInstance();
				final SAXParser 		saxParser = factory.newSAXParser();
				final MyHandler 		handler = new MyHandler();
				
				this.initialTree = new AndOrTree<>(1,1);
				this.tree = new AndOrTree<>(1,1);
				
				saxParser.parse(dictionary, handler);
				
				this.linkTypes = handler.linkTypes.toArray(new String[handler.linkTypes.size()]);
				this.links = handler.links.toArray(new LinkDesc[handler.links.size()]);
				this.orderedGrammemas = grammemas.values().toArray(new Grammema[grammemas.size()]);
				
				Arrays.sort(links, (o1,o2)->(int)(o2.from == o1.from ? o2.to - o1.to : o2.from - o1.from));
				Arrays.sort(orderedGrammemas, (o1,o2)->o2.index - o1.index);
			} catch (ParserConfigurationException | SAXException exc) {
				throw new IOException(exc);
			}
		}
	}

	public DictionaryManager(final DataInputStream rawDump) throws IOException {
		if (rawDump == null) {
			throw new NullPointerException("RAW dump to load can't be null");
		}
		else if (rawDump.readInt() != MAGIC) {
			throw new IOException("Illegal input stream format - bad first magic");
		}
		else {
			this.initialTree = AndOrTree.rawDownload(rawDump);
			
			for(int index = 0, maxIndex = rawDump.readInt(); index < maxIndex; index++) {
				final WordDesc	desc = WordDesc.download(rawDump);

				if (initialTree.getCargo(desc.id) == null) {
					initialTree.setCargo(desc.id, new ArrayList<>());
				}
				initialTree.getCargo(desc.id).add(desc); 
			}
			
			this.tree = AndOrTree.rawDownload(rawDump);
			
			for(int index = 0, maxIndex = rawDump.readInt(); index < maxIndex; index++) {
				final WordDesc	desc = WordDesc.download(rawDump);

				if (tree.getCargo(desc.id) == null) {
					tree.setCargo(desc.id, new ArrayList<>());
				}
				tree.getCargo(desc.id).add(desc); 
			}
			
			this.linkTypes = new String[rawDump.readInt()];
			for(int index = 0; index < linkTypes.length; index++) {
				linkTypes[index] = rawDump.readUTF();
			}
			
			this.orderedGrammemas = new Grammema[rawDump.readInt()];
			for(int index = 0; index < orderedGrammemas.length; index++) {
				final Grammema	grammema = Grammema.download(rawDump); 
				
				orderedGrammemas[index] = grammema; 
				grammemas.put(grammema.name, grammema);
			}
			
			this.links = new LinkDesc[rawDump.readInt()];
			for(int index = 0; index < links.length; index++) {
				links[index] = LinkDesc.download(rawDump);
			}
			if (rawDump.readInt() != MAGIC) {
				throw new IOException("Illegal input stream format - bad last magic");
			}
		}
	}
	
	public int nextWord(final char[] content, final int from, final Consumer<WordDescriptor> callback) throws SyntaxException {
		if (content == null || content.length == 0) {
			throw new IllegalArgumentException("Content can't be null or empty array");
		}
		else if (from < 0 || from >= content.length) {
			throw new IllegalArgumentException("From position ["+from+"] out of range 0.."+(content.length-1));
		}
		else if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			final int[]	ranges = new int[2];
			final int	after = CharUtils.parseNameExtended(content, CharUtils.skipBlank(content, from, false), ranges, LETTERS);
			final long	id = tree.seekName(content, ranges[0], ranges[1] - ranges[0] + 1);
			
			if (id >= 0) {
				for (WordDesc desc : tree.getCargo(id)) {
					callback.accept(new WordDescriptorImpl(desc));
				}
			}
			return after;
		}
	}
	
	public void allWords(final Consumer<WordDescriptor> callback) {
		if (callback == null) {
			throw new NullPointerException("Callback can't be null"); 
		}
		else {
			tree.walk((name, length, id, c)->{
				for(WordDesc item : c) {
					callback.accept(new WordDescriptorImpl(item));
				}
				return true;
			});
		}
	}

	public void upload(final DataOutputStream dos) throws IOException {
		if (dos == null) {
			throw new NullPointerException("Output stream can't be null");
		}
		else {
			final int[]			count = new int[1];
			final IOException[]	exc = new IOException[] {null};
			
			dos.writeInt(MAGIC);
			
			AndOrTree.rawUpload((AndOrTree<?>)initialTree, dos);
			count[0] = 0;
			initialTree.walk((seq, len, id, cargo)->{
				count[0]+= cargo.size();
				return true;
			});
			dos.writeInt(count[0]);
			initialTree.walk((seq, len, id, cargo)->{
				for (WordDesc item : cargo) {
					try{
						WordDesc.upload(item, dos);
					} catch (IOException e) {
						exc[0] = e;
						return false;
					}
				}
				return true;
			});
			if (exc[0] != null) {
				throw exc[0];
			}

			AndOrTree.rawUpload((AndOrTree<?>)tree, dos);
			count[0] = 0;
			tree.walk((seq, len, id, cargo)->{
				count[0]+= cargo.size();
				return true;
			});
			dos.writeInt(count[0]);
			tree.walk((seq, len, id, cargo)->{
				for (WordDesc item : cargo) {
					try{
						WordDesc.upload(item, dos);
					} catch (IOException e) {
						exc[0] = e;
						return false;
					}
				}
				return true;
			});
			if (exc[0] != null) {
				throw exc[0];
			}
			
			dos.writeInt(linkTypes.length);
			for(int index = 0; index < linkTypes.length; index++) {
				dos.writeUTF(linkTypes[index]);
			}

			dos.writeInt(orderedGrammemas.length);
			for(int index = 0; index < orderedGrammemas.length; index++) {
				Grammema.upload(orderedGrammemas[index], dos);
			}

			dos.writeInt(links.length);
			for(int index = 0; index < links.length; index++) {
				LinkDesc.upload(links[index], dos);
			}
			
			dos.writeInt(MAGIC);
			dos.flush();
		}
	}

	private static long readValue(final DataInputStream dis, final int lengthIndex) throws IOException {
		switch (lengthIndex) {
			case 0 : return dis.readByte();
			case 1 : return dis.readShort();
			case 2 : return dis.readInt();
			case 3 : return dis.readLong();
			default :
				throw new UnsupportedOperationException("Length index ["+(lengthIndex)+"] is not supported yet");
		}
	}
	
	private static void writeValue(final int lengthIndex, final long value, final DataOutputStream dos) throws IOException {
		switch (lengthIndex) {
			case 0 : dos.writeByte((int)value);		break;
			case 1 : dos.writeShort((int)value);	break;
			case 2 : dos.writeInt((int)value);		break;
			case 3 : dos.writeLong(value);			break;
			default :
				throw new UnsupportedOperationException("Length index ["+(lengthIndex)+"] is not supported yet");
		}
	}
	
	private class MyHandler extends DefaultHandler {
		private final List<String>					linkTypes = new ArrayList<>();
		private final List<LinkDesc>				links = new ArrayList<>();
		private final StringBuilder					sb = new StringBuilder();
		private int		grammemaCount = 0;
		private long	id;
		private long 	initialForm;
		private String 	parent = null;
		private long 	currentForm;
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
					final String	lString = attr.getValue("t"); 
					
					lobitsInitial = 0;
					hibitsInitial = 0;
					initialForm = initialTree.seekName((CharSequence)lString);
					
					if (initialForm < 0) {
						initialForm = initialTree.placeName((CharSequence)lString, null);
					}
					break;
				case "f" 		:
					final String	fString = attr.getValue("t");
					
					lobits = 0;
					hibits = 0;
					currentForm = tree.seekName((CharSequence)fString);
					
					if (currentForm < 0) {
						currentForm = tree.placeName((CharSequence)fString, null);
					}
					break;
				case "g" 		:
					final int index = grammemas.get(attr.getValue("v")).index;
					
					if (index < 63) {
						lobits |= (1L << index);
						lobitsInitial |= (1L << index);
					}
					else {
						hibits |= (1L << (index - 64));
						hibitsInitial |= (1L << (index - 64));
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
					List<WordDesc>	listInitial = initialTree.getCargo(initialForm);
					
					if (listInitial == null) {
						initialTree.setCargo(initialForm, listInitial = new ArrayList<>());
					}
					listInitial.add(new WordDesc(initialForm, initialForm, lobitsInitial, hibitsInitial));
					break;
				case "f" 		:
					List<WordDesc>	list = tree.getCargo(currentForm); 
					
					if (list == null) {
						tree.setCargo(currentForm, list = new ArrayList<>());
					}
					list.add(new WordDesc(currentForm, initialForm, lobits | lobitsInitial, hibits | hibitsInitial));
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
		
		public Grammema(final String name, final String parent, final int index) {
			this.name = name;
			this.parent = parent;
			this.index = index;
		}

		@Override
		public String toString() {
			return "Grammema [name=" + name + ", parent=" + parent + ", index=" + index + "]";
		}

		public static void upload(final Grammema desc, final DataOutputStream dos) throws IOException {
			if (desc == null) {
				throw new NullPointerException("Grammema can't be null");
			}
			else if (dos == null) {
				throw new NullPointerException("Output stream can't be null");
			}
			else {
				dos.writeUTF(desc.name);
				dos.writeUTF(desc.parent);
				dos.writeInt(desc.index);
			}
		}

		public static Grammema download(final DataInputStream dis) throws IOException {
			if (dis == null) {
				throw new NullPointerException("Input stream can't be null");
			}
			else {
				return new Grammema(dis.readUTF(), dis.readUTF(), dis.readInt());
			}
		}
	}
	
	private static class WordDesc {
		final long		id;
		final long		initialForm;
		final long		loBits;
		final long		hiBits;
		
		public WordDesc(final long id, final long initialForm, final long loBits, final long hiBits) {
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
			if (desc == null) {
				throw new NullPointerException("Word descriptor can't be null");
			}
			else if (dos == null) {
				throw new NullPointerException("Output stream can't be null");
			}
			else {
				final int	idSize = Utils.getSignificantSize(desc.id);
				final int	initialFormSize = Utils.getSignificantSize(desc.initialForm);
				final int	loBitsSize = Utils.getSignificantSize(desc.loBits);
				final int	hiBitsSize = Utils.getSignificantSize(desc.hiBits);
				
				dos.writeByte((idSize << 6) | (initialFormSize << 4) | (loBitsSize << 2) | (hiBitsSize << 0));
				writeValue(idSize, desc.id, dos);
				writeValue(initialFormSize, desc.initialForm, dos);
				writeValue(loBitsSize, desc.loBits, dos);
				writeValue(hiBitsSize, desc.hiBits, dos);
			}
		}
		
		public static WordDesc download(final DataInputStream dis) throws IOException {
			if (dis == null) {
				throw new NullPointerException("Input stream can't be null");
			}
			else {
				final int	length = dis.readByte();
				
				return new WordDesc(
						readValue(dis, (length >> 6) & 0x03), 
						readValue(dis, (length >> 4) & 0x03), 
						readValue(dis, (length >> 2) & 0x03), 
						readValue(dis, (length >> 0) & 0x03)
				);
			}
		}

	}
	
	private static class LinkDesc {
		final long		from;
		final long 		to;
		final int		type;
		
		public LinkDesc(final long from, final long to, final int type) {
			this.from = from;
			this.to = to;
			this.type = type;
		}

		@Override
		public String toString() {
			return "LinkDesc [from=" + from + ", to=" + to + ", type=" + type + "]";
		}
		
		public static void upload(final LinkDesc desc, final DataOutputStream dos) throws IOException {
			if (desc == null) {
				throw new NullPointerException("Link descriptor can't be null");
			}
			else if (dos == null) {
				throw new NullPointerException("Output stream can't be null");
			}
			else {
				final int	fromSize = Utils.getSignificantSize(desc.from); 
				final int	toSize = Utils.getSignificantSize(desc.to); 
				final int	typeSize = Utils.getSignificantSize(desc.type);
				
				dos.writeByte((fromSize << 4) | (toSize << 2) | (typeSize << 0));
				writeValue(fromSize, desc.from, dos);
				writeValue(toSize, desc.to, dos);
				writeValue(typeSize, desc.type, dos);
			}
		}

		public static LinkDesc download(final DataInputStream dis) throws IOException {
			if (dis == null) {
				throw new NullPointerException("Input stream can't be null");
			}
			else {
				final int	length = dis.readByte();
				
				return new LinkDesc(
						readValue(dis, (length >> 4) & 0x03),
						readValue(dis, (length >> 2) & 0x03),
						(int) readValue(dis, (length >> 0) & 0x03)
				);
			}
		}
	}
	
	private class WordDescriptorImpl implements WordDescriptor {
		private final WordDesc	nested;
		
		private WordDescriptorImpl(final WordDesc nested) {
			this.nested = nested;
		}
		
		@Override
		public Voice getVoice() {
			return getValue(Voice.class);
		}
		
		@Override
		public Trans getTrans() {
			return getValue(Trans.class);
		}
		
		@Override
		public Tense getTense() {
			return getValue(Tense.class);
		}
		
		@Override
		public Person getPerson() {
			return getValue(Person.class);
		}
		
		@Override
		public Part getPart() {
			return getValue(Part.class);
		}
		
		@Override
		public Numbr getNumbr() {
			return getValue(Numbr.class);
		}
		
		@Override
		public Mood getMood() {
			return getValue(Mood.class);
		}
		
		@Override
		public Involve getInvolve() {
			return getValue(Involve.class);
		}
		
		@Override
		public Case getCase() {
			return getValue(Case.class);
		}
		
		@Override
		public Aspect getAspect() {
			return getValue(Aspect.class);
		}
		
		@Override
		public Anim getAnim() {
			return getValue(Anim.class);
		}
		
		@Override
		public Gndr getGndr() {
			return getValue(Gndr.class);
		}
		
		@Override
		public String getInitialFormAsString() {
			return new String(getInitialForm());
		}
		
		@Override
		public char[] getInitialForm() {
			final char[]	result = new char[initialTree.getNameLength(nested.initialForm)];
			
			initialTree.getName(nested.initialForm, result, 0);
			return result;
		}

		@Override
		public String getCurrentFormAsString() {
			return new String(getCurrentForm());
		}
		
		@Override
		public char[] getCurrentForm() {
			final char[]	result = new char[tree.getNameLength(nested.id)];
			
			tree.getName(nested.id, result, 0);
			return result;
		}

		@Override
		public Set<Markers> getMarkers() {
			final Set<Markers>	result = new HashSet<>();
			
			for(int index = 0; index < INDICES.length; index++) {
				if (((index <= 63 ? nested.loBits : nested.hiBits) & (1L << (index <= 63 ? index : index - 64))) != 0) {
					if (INDICES[index] instanceof Markers) {
						result.add((Markers)INDICES[index]);
					}
				}
			}
			return result;
		}

		@Override
		public List<WordDescriptor> getInitialFormDesc() {
			if (nested.id != nested.initialForm) {
				return toWordDesc(initialTree.getCargo(nested.initialForm));
			}
			else {
				return new ArrayList<>();
			}
		}
		
		private <T extends Enum<?>> T getValue(final Class<T> clazz) {
			for(int index = 0; index < INDICES.length; index++) {
				if (((index <= 63 ? nested.loBits : nested.hiBits) & (1L << (index <= 63 ? index : index - 64))) != 0) {
					if (clazz.isAssignableFrom(INDICES[index].getClass())) {
						return clazz.cast(INDICES[index]);
					}
				}
			}
			return null;
		}

		private List<WordDescriptor> toWordDesc(final List<WordDesc> cargo) {
			final List<WordDescriptor> result = new ArrayList<>();
			
			for (WordDesc item : cargo) {
				result.add(new WordDescriptorImpl(item));
			}
			return result;
		}
	}
}
