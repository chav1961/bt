package chav1961.bt.audio.interfaces;

public enum MidiChannelFamily {
	Piano(1,8),	
	ChromaticPercussion(9,16),	
	Organ(17,24),	
	Guitar(25,32),	
	Bass(33,40),
	Strings(41,48),	
	Ensemble(49,56),	
	Brass(57,64),	
	Reed(65,72),
	Pipe(73,80),
	SynthLead(81,88),
	SynthPad(89,96),
	SynthEffects(97,104),
	Ethnic(105,112),
	Percussive(113,120),
	SoundEffects(121,128);
	
	private final int	fromRange;
	private final int	toRange;
	
	private MidiChannelFamily(final int fromRange, final int toRange) {
		this.fromRange = fromRange;
		this.toRange = toRange;
	}
	
	public int getFromRange() {
		return fromRange;
	}
	
	public int getToRange() {
		return toRange;
	}
	
	public boolean inRange(final int midiChannel) {
		return fromRange <= midiChannel && midiChannel <= toRange;
	}
}
