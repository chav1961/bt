package chav1961.bt.audio.interfaces;

public enum MidiInstrumentPatch {
	AcousticGrandPiano(1),
	BrightAcousticPiano(2),
	ElectricGrandPiano(3),
	HonkyTonkPiano(4),	
	ElectricPiano1(5,"Rhodes Piano"),	
	ElectricPiano2(6,"Chorused Piano"),
	Harpsichord(7),	
	Clavinet(8),	
	Celesta(9),	
	Glockenspiel(10),
	MusicBox(11),
	Vibraphone(12),
	Marimba(13),	
	Xylophone(14),
	TubularBells(15),
	Dulcimer(16,"Santur"),
	DrawbarOrgan(17,"Hammond"),
	PercussiveOrgan(18),
	RockOrgan(19),	
	ChurchOrgan(20),	
	ReedOrgan(21),	
	Accordion(22,"French"),
	Harmonica(23),	
	TangoAccordion(24,"Band neon"),
	AcousticGuitar1(25,"nylon"),
	AcousticGuitar2(26,"steel"),
	ElectricGuitar1(27,"jazz"),
	ElectricGuitar2(28,"clean"),
	ElectricGuitar3(29,"muted"),
	OverdrivenGuitar(30),	
	DistortionGuitar(31),	
	GuitarHarmonics(32),	
	AcousticBass(33),	
	ElectricBass1(34,"fingered"),
	ElectricBass2(35,"picked"),	
	FretlessBass(36),	
	SlapBass1(37),	
	SlapBass2(38),	
	SynthBass1(39),	
	SynthBass2(40),	
	Violin(41),	
	Viola(42),	
	Cello(43),	
	Contrabass(44),	
	TremoloStrings(45),	
	PizzicatoStrings(46),	
	OrchestralHarp(47),	
	Timpani(48),	
	StringEnsemble1(49,"strings"),	
	StringEnsemble2(50,"slow strings"),
	SynthStrings1(51),	
	SynthStrings2(52),	
	ChoirAahs(53),	
	VoiceOohs(54),	
	SynthVoice(55),	
	OrchestraHit(56),	
	Trumpet(57),	
	Trombone(58),	
	Tuba(59),	
	MutedTrumpet(60),	
	FrenchHorn(61),	
	BrassSection(62),
	SynthBrass1(63),
	SynthBrass2(64),
	SopranoSax(65),
	AltoSax(66),
	TenorSax(67),
	BaritoneSax(68),
	Oboe(69),
	EnglishHorn(70),
	Bassoon(71),
	Clarinet(72),
	Piccolo(73),
	Flute(74),
	Recorder(75),
	PanFlute(76),
	BlownBottle(77),
	Shakuhachi(78),
	Whistle(79),
	Ocarina(80),
	Lead1(81,"square wave"),
	Lead2(82,"sawtooth wave"),
	Lead3(83,"calliope"),
	Lead4(84,"chiffer"),
	Lead5(85,"charang"),
	Lead6(86,"voice solo"),
	Lead7(87,"fifths"),
	Lead8(88,"bass + lead"),
	Pad1(89,"new age Fantasia"),
	Pad2(90,"warm"),
	Pad3(91,"polysynth"),
	Pad4(92,"choir space voice"),
	Pad5(93,"bowed glass"),
	Pad6(94,"metallic pro"),
	Pad7(95,"halo"),
	Pad8(96,"sweep"),
	FX1(97,"rain"),
	FX2(98,"soundtrack"),
	FX3(99,"crystal"),
	FX4(100,"atmosphere"),
	FX5(101,"brightness"),
	FX6(102,"goblins"),
	FX7(103,"echoes, drops"),
	FX8(104,"sci-fi, star theme"),
	Sitar(105),
	Banjo(106),
	Shamisen(107),
	Koto(108),
	Kalimba(109),
	BagPipe(110),
	Fiddle(111),
	Shanai(112),
	TinkleBell(113),
	Agogo(114),
	SteelDrums(115),
	Woodblock(116),
	TaikoDrum(117),
	MelodicTom(118),
	SynthDrum(119),
	ReverseCymbal(120),
	GuitarFretNoise(121),
	BreathNoise(122),
	Seashore(123),
	BirdTweet(124),
	TelephoneRing(125),
	Helicopter(126),
	Applause(127),
	Gunshot(128);
	
	private final int 		instrument;
	private final String	comment;
	
	private MidiInstrumentPatch(final int instrument) {
		this.instrument = instrument;
		this.comment = "";
	}

	private MidiInstrumentPatch(final int instrument, final String comment) {
		this.instrument = instrument;
		this.comment = comment;
	}
	
	public int getInstrumentNumber() {
		return instrument;
	}
	
	public String getComment() {
		return comment;
	}
	
	public MidiInstrumentPatch valueOf(final int instrumentNumber) {
		for (MidiInstrumentPatch item : values()) {
			if (item.getInstrumentNumber() == instrumentNumber) {
				return item;
			}
		}
		throw new IllegalArgumentException("Unsupported instrument number ["+instrumentNumber+"] for MIDI instrument patch"); 
	}
}
