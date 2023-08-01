package chav1961.bt.nlp.interfaces.internal;

import java.util.Set;

public interface WordDescriptor {
	
	public static enum Part {
		POST("POST","ЧР","часть речи"),
		NOUN("NOUN","СУЩ","имя существительноe"),
		ADJF("ADJF","ПРИЛ","имя прилагательное (полное)"),
		ADJS("ADJS","КР_ПРИЛ","имя прилагательное (краткое)"),
		COMP("COMP","КОМП","компаратив"),
		VERB("VERB","ГЛ","глагол (личная форма)"),
		INFN("INFN","ИНФ","глагол (инфинитив)"),
		PRTF("PRTF","ПРИЧ","причастие (полное)"),
		PRTS("PRTS","КР_ПРИЧ","причастие (краткое)"),
		GRND("GRND","ДЕЕПР","деепричастие"),
		NUMR("NUMR","ЧИСЛ","числительное"),
		ADVB("ADVB","Н","наречие"),
		NPRO("NPRO","МС","местоимение-существительное"),
		PRED("PRED","ПРЕДК","предикатив"),
		PREP("PREP","ПР","предлог"),
		CONJ("CONJ","СОЮЗ","союз"),
		PRCL("PRCL","ЧАСТ","частица"),
		INTJ("INTJ","МЕЖД","междометие"),
		PUNCT("PUNCT","ПУНКТ","пунктуация");
	
		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Part(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Anim {
		ANIM("anim","од","одушевлённое"), 
		INAN("inan","неод","неодушевлённое");

		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Anim(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}

	public static enum Gndr {
		MASC("masc","мр","мужской род"),	
		FEMN("femn","жр","женский род"),
		NEUT("neut","ср","средний род"),
		MASF("ms-f","мж","общий род (м/ж)");

		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Gndr(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Numbr {
		SING("sing","ед","единственное число"),
		PLUR("plur","мн","множественное число");
		
		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Numbr(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}

	public static enum Case {
		NOMN("nomn","им","именительный падеж"),
		GENT("gent","рд","родительный падеж"),
		DATV("datv","дт","дательный падеж"),
		ACCS("accs","вн","винительный падеж"),
		ABLT("ablt","тв","творительный падеж"),
		LOCT("loct","пр","предложный падеж"),
		VOCT("voct","зв","звательный падеж"),
		GEN1("gen1","рд1","первый родительный падеж"),
		GEN2("gen2","рд2","второй родительный (частичный) падеж"),
		ACC2("acc2","вн2","второй винительный падеж"),
		LOC1("loc1","пр1","первый предложный падеж"),
		LOC2("loc2","пр2","второй предложный (местный) падеж");

		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Case(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}	
	
	public static enum Aspect {
		PERF("perf","сов","совершенный вид"),
		IMPERF("impf","несов","несовершенный вид");

		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Aspect(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Trans {
		TRANS("tran","перех","переходный"),
		INTRANS("intr","intr","непереходный");

		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Trans(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Person {
		FIRST("1per","1л","1 лицо"),
		SECOND("2per","2л","2 лицо"),
		THIRD("3per","3л","3 лицо");
		
		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Person(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Tense {
		PRESENT("pres","наст","настоящее время"),
		PAST("past","прош","прошедшее время"),
		FUTURE("futr","буд","будущее время");
		
		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Tense(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Mood {
		INDC("indc","изъяв","изъявительное наклонение"),
		IMPR("impr","повел","повелительное наклонение");

		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Mood(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Involve {
		INCL("incl","вкл","говорящий включён (идем, идемте)"),
		EXCL("excl","выкл","говорящий не включён в действие (иди, идите)");
		
		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Involve(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Voice {
		ACTIVE("actv","действ","действительный залог"),
		PASSIVE("pssv","страд","страдательный залог");
		
		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Voice(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}
	
	public static enum Markers {
		Sgtm("Sgtm","sg","singularia tantum"),
		Pltm("Pltm","pl","pluralia tantum"),
		Fixd("Fixd","0","неизменяемое"),
		Abbr("Abbr","аббр","аббревиатура"),
		Name("Name","имя","имя"),
		Surn("Surn","фам","фамилия"),
		Patr("Patr","отч","отчество"),
		Geox("Geox","гео","топоним"),
		Orgn("Orgn","орг","организация"),
		Trad("Trad","tm","торговая марка"),
		Subx("Subx","субст?","возможна субстантивация"),
		Supr("Supr","превосх","превосходная степень"),
		Qual("Qual","кач","качественное"),
		Apro("Apro","мест-п","местоименное"),
		Anum("Anum","числ-п","порядковое"),
		Poss("Poss","притяж","притяжательное"),
		V_ey("V-ey","*ею","форма на -ею"),
		V_oy("V-oy","*ою","форма на -ою"),
		Cmp2("Cmp2","сравн2","сравнительная степень на по-"),
		V_ej("V-ej","*ей","форма компаратива на -ей"),
		Impe("Impe","безл","безличный"),
		Impx("Impx","безл?","возможно безличное употребление"),
		Mult("Mult","мног","многократный"),
		Refl("Refl","возвр","возвратный"),
		Infr("Infr","разг","разговорное"),
		Slng("Slng","жарг","жаргонное"),
		Arch("Arch","арх","устаревшее"),
		Litr("Litr","лит","литературный вариант"),
		Erro("Erro","опеч","опечатка"),
		Dist("Dist","искаж","искажение"),
		Ques("Ques","вопр","вопросительное"),
		Dmns("Dmns","указ","указательное"),
		Prnt("Prnt","вводн","вводное слово"),
		V_be("V-be","*ье","форма на -ье"),
		V_en("V-en","*енен","форма на -енен"),
		V_ie("V-ie","*ие","форма на -и- (веселие, твердостию); отчество с -ие"),
		V_bi("V-bi","*ьи","форма на -ьи"),
		Fimp("Fimp","*несов","деепричастие от глагола несовершенного вида"),
		Prdx("Prdx","предк?","может выступать в роли предикатива"),
		Coun("Coun","счетн","счётная форма"),
		Coll("Coll","собир","собирательное числительное"),
		V_sh("V-sh","*ши","деепричастие на -ши"),
		Af_p("Af-p","*предл","форма после предлога"),
		Inmx("Inmx","не/одуш?","может использоваться как одуш. / неодуш."),
		Vpre("Vpre","в_предл","Вариант предлога ( со, подо, ...)"),
		Anph("Anph","анаф","Анафорическое (местоимение)"),
		Init("Init","иниц","Инициал"),
		Adjx("Adjx","прил?","может выступать в роли прилагательного"),
		Ms_f("Ms-f","ор","колебание по роду (м/ж/с): кофе, вольво"),
		Hypo("Hypo","гипот","гипотетическая форма слова (победю, асфальтовее)");
		
		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		
		private Markers(final String internalId, final String externalId, final String comment) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
	}

	public static enum Categories {
		Part("POST","ЧР","часть речи",Part.class),
		Anim("ANim","Од-неод","категория одушевлённости",Anim.class),
		Gndr("GNdr","хр","род / род не выражен",Gndr.class),
		Numbr("NMbr","Число","число",Numbr.class),
		Case("CAse","Падеж","категория падежа",Case.class),
		Aspect("ASpc","Вид","категория вида",Aspect.class),
		Trans("TRns","Перех","категория переходности",Trans.class),
		Person("PErs","Лицо","категория лица",Person.class),
		Tense("TEns","Время","категория времени",Tense.class),
		Mood("MOod","Накл","категория наклонения",Mood.class),
		Involve("INvl","Совм","категория совместности",Involve.class),
		Voice("VOic","Залог","категория залога",Voice.class);
		
		private final String	internalId;
		private final String	externalId;
		private final String	comment;
		private final Class<? extends Enum<?>> representation;
		
		private Categories(final String internalId, final String externalId, final String comment, final Class<? extends Enum<?>> representation) {
			this.internalId = internalId;
			this.externalId = externalId;
			this.comment = comment;
			this.representation = representation;
		}
		
		public String getInternalId() {
			return internalId;
		}

		public String getExternalId() {
			return externalId;
		}
		
		public String getComment() {
			return comment;
		}
		
		public Class<? extends Enum<?>> getRepresentation() {
			return representation;
		}
	}
	
	
	Part getPart();
	Anim getAnim();
	Gndr getGndr();
	Numbr getNumbr();
	Case getCase();
	Aspect getAspect();
	Trans getTrans();
	Person getPerson();
	Tense getTense();
	Mood getMood();
	Involve getInvolve();
	Voice getVoice();
	Set<Markers> getMarkers();
	char[] getCurrentForm();
	String getCurrentFormAsString();
	char[] getInitialForm();
	String getInitialFormAsString();
}
