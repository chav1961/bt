package chav1961.bt.audio.interfaces;

public enum MidiPercussionKeys {
	AcousticBassDrum(35),	
	BassDrum1(36),
	SideStick(37),
	AcousticSnare(38),
	HandClap(39),
	ElectricSnare(40),
	LowFloorTom(41),
	ClosedHiHat(42),
	HighFloorTom(43),
	PedalHiHat(44),
	LowTom(45),
	OpenHiHat(46),
	LowMidTom(47),
	HiMidTom(48),
	CrashCymbal(49),
	HighTom1(50),
	RideCymbal1(51),
	ChineseCymbal(52),
	RideBell(53),
	Tambourine(54),
	SplashCymbal(55),
	Cowbell(56),
	CrashCymbal2(57),	
	Vibraslap(58),
	RideCymbal2(59),
	HiBongo(60),
	LowBongo(61),
	MuteHiConga(62),
	OpenHiConga(63),
	LowConga(64),
	HighTimbale(65),
	LowTimbale(66),
	HighAgogo(67),
	LowAgogo(68),
	Cabasa(69),
	Maracas(70),
	ShortWhistle(71),
	LongWhistle(72),
	ShortGuiro(73),
	LongGuiro(74),
	Claves(75),
	HiWoodBlock(76),
	LowWoodBlock(77),
	MuteCuica(78),
	OpenCuica(79),
	MuteTriangle(80),
	OpenTriangle(81);
	
	private final int	note;
	
	private MidiPercussionKeys(final int note) {
		this.note = note;
	}
	
	public int getNote() {
		return note;
	}
	
	public static MidiPercussionKeys valueOf(final int note) {
		for (MidiPercussionKeys item : values()) {
			if (item.getNote() == note) {
				return item;
			}
		}
		throw new IllegalArgumentException("Unsupported note number ["+note+"] for percurssion keys"); 
	}
}
