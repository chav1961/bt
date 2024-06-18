package chav1961.bt.audio.interfaces;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import chav1961.purelib.basic.Utils;

public enum MidiNotes {
	C_1(-1,0,"C"), 
	Cx_1(-1,1,"C#"),
	D_1(-1,2,"D"), 
	Dx_1(-1,3,"D#"), 
	E_1(-1,4,"E"), 
	F_1(-1,5,"F"),
	Fx_1(-1,6,"F#"),
	G_1(-1,7,"G"),
	Gx_1(-1,8,"G#"),
	A_1(-1,9,"A"),
	Ax_1(-1,10,"A#"),
	B_1(-1,11,"B"),
	C0(0,12,"C"),
	Cx0(0,13,"C#"),
	D0(0,14,"D"),
	Dx0(0,15,"D#"),
	E0(0,16,"E"),
	F0(0,17,"F"),
	Fx0(0,18,"F#"),
	G0(0,19,"G"),
	Gx0(0,20,"G#"),
	A0(0,21,"A"),
	Ax0(0,22,"A#"),
	B0(0,23,"B"),
	C1(1,24,"C"),
	Cx1(1,25,"C#"),
	D1(1,26,"D"),
	Dx1(1,27,"D#"),
	E1(1,28,"E"),
	F1(1,29,"F"),
	Fx1(1,30,"F#"),
	G1(1,31,"G"),
	Gx1(1,32,"G#"),
	A1(1,33,"A"),
	Ax1(1,34,"A#"),
	B1(1,35,"B"),
	C2(2,36,"C"),
	Cx2(2,37,"C#"),
	D2(2,38,"D"),
	Dx2(2,39,"D#"),
	E2(2,40,"E"),
	F2(2,41,"F"),
	Fx2(2,42,"F#"),
	G2(2,43,"G"),
	Gx2(2,44,"G#"),
	A2(2,45,"A"),
	Ax2(2,46,"A#"),
	B2(2,47,"B"),
	C3(3,48,"C"),
	Cx3(3,49,"C#"),
	D3(3,50,"D"),
	Dx3(3,51,"D#"),
	E3(3,52,"E"),
	F3(3,53,"F"),
	Fx3(3,54,"F#"),
	G3(3,55,"G"),
	Gx3(3,56,"G#"),
	A3(3,57,"A"),
	Ax3(3,58,"A#"),
	B3(3,59,"B"),
	C4(4,60,"C"),
	Cx4(4,61,"C#"),
	D4(4,62,"D"),
	Dx4(4,63,"D#"),
	E4(4,64,"E"),
	F4(4,65,"F"),
	Fx4(4,66,"F#"),
	G4(4,67,"G"),
	Gx4(4,68,"G#"),
	A4(4,69,"A"),
	Ax4(4,70,"A#"),
	B4(4,71,"B"),
	C5(5,72,"C"),
	Cx5(5,73,"C#"),
	D5(5,74,"D"),
	Dx5(5,75,"D#"),
	E5(5,76,"E"),
	F5(5,77,"F"),
	Fx5(5,78,"F#"),
	G5(5,79,"G"),
	Gx5(5,80,"G#"),
	A5(5,81,"A"),
	Ax5(5,82,"A#"), 
	B5(5,83,"B"),
	C6(6,84,"C"),
	Cx6(6,85,"C#"),
	D6(6,86,"D"),
	Dx6(6,87,"D#"),
	E6(6,88,"E"),
	F6(6,89,"F"),
	Fx6(6,90,"F#"), 
	G6(6,91,"G"),
	Gx6(6,92,"G#"),
	A6(6,93,"A"),
	Ax6(6,94,"A#"),
	B6(6,95,"B"),
	C7(7,96,"C"),
	Cx7(7,97,"C#"),
	D7(7,98,"D"),
	Dx7(7,99,"D#"),
	E7(7,100,"E"),
	F7(7,101,"F"),
	Fx7(7,102,"F#"),
	G7(7,103,"G"),
	Gx7(7,104,"G#"),
	A7(7,105,"A"),
	Ax7(7,106,"A#"),
	B7(7,107,"B"),
	C8(8,108,"C"),
	Cx8(8,109,"C#"),
	D8(8,110,"D"),
	Dx8(8,111,"D#"),
	E8(8,112,"E"),
	F8(8,113,"F"),
	Fx8(8,114,"F#"),
	G8(8,115,"G"),
	Gx8(8,116,"G#"),
	A8(8,117,"A"),
	Ax8(8,118,"A#"),
	B8(8,119,"B"),
	C9(8,120,"C"),
	Cx9(9,121,"C#"),
	D9(9,122,"D"),
	Dx9(9,123,"D#"),
	E9(9,124,"E"),
	F9(9,125,"F"),
	Fx9(9,126,"F#"),
	G9(9,127,"G"),
	Gx9(9,128,"G#"),
	A9(9,129,"A"),
	Ax9(9,130,"A#"),
	B9(9,131,"B");
	
	private static final Pattern	PARSER = Pattern.compile("([-]{0,1}\\d)([a-gA-G])([#b]{0,1})");
	private static final Map<String, String>	AVAILABLE_NOTES = new HashMap<>();
	private static final Map<String, String>	BEMOLLES = new HashMap<>();
	
	static {
		for(MidiNotes item : values()) {
			AVAILABLE_NOTES.put(item.getPresentation(), item.getPresentation());
		}
		AVAILABLE_NOTES.put("Db", "C#");
		AVAILABLE_NOTES.put("Eb", "D#");
		AVAILABLE_NOTES.put("Gb", "F#");
		AVAILABLE_NOTES.put("Ab", "G#");
		AVAILABLE_NOTES.put("Bb", "A#");
		AVAILABLE_NOTES.put("B#", "C");
		AVAILABLE_NOTES.put("E#", "F");
		AVAILABLE_NOTES.put("Cb", "B");
		AVAILABLE_NOTES.put("Fb", "E");
		BEMOLLES.put("A#", "Bb");
		BEMOLLES.put("C#", "Db");
		BEMOLLES.put("D#", "Eb");
		BEMOLLES.put("F#", "Gb");
		BEMOLLES.put("G#", "Ab");
	}
	
	private final int		octaveNumber;
	private final int		noteNumber;
	private final String	presentation;
	
	private MidiNotes(final int octaveNuber, final int noteNumber, final String presentation) {
		this.octaveNumber = octaveNuber;
		this.noteNumber = noteNumber;
		this.presentation = presentation;
	}
	
	public int getOctaveNumber() {
		return octaveNumber;
	}
	
	public int getNoteNumber() {
		return noteNumber;
	}
	
	public String getPresentation() {
		return presentation;
	}

	public String getPresentation(final boolean bemollePreferred) {
		if (bemollePreferred) {
			return presentation.length() == 2 && presentation.charAt(1) == '#' ? BEMOLLES.get(presentation) : presentation;
		}
		else {
			return presentation;
		}
	}
	
	public boolean hasPrev() {
		return getNoteNumber() > 0;
	}

	public MidiNotes prev() {
		return valueOf(getNoteNumber()-1);
	}

	public boolean hasNext() {
		return getNoteNumber() < values().length - 1;
	}
	
	public MidiNotes next() {
		return valueOf(getNoteNumber()+1);
	}
	
	public static MidiNotes valueOf(final int noteNumber) {
		for (MidiNotes item : values()) {
			if (item.getNoteNumber() == noteNumber) {
				return item;
			}
		}
		throw new IllegalArgumentException("Unsupported note number ["+noteNumber+"] for MIDI notes"); 
	}

	public static MidiNotes valueOfX(final String presentation) {
		if (Utils.checkEmptyOrNullString(presentation)) {
			throw new IllegalArgumentException("Note presentation can't be null or empty");
		}
		else {
			final Matcher	m = PARSER.matcher(presentation);
			
			if (m.find()) {
				final int		octaveNumber = Integer.valueOf(m.group(1).trim());
				final String	halfTone = Utils.checkEmptyOrNullString(m.group(3)) ? m.group(3) : "";
				final String	note = m.group(2).toUpperCase() + halfTone;
				
				if (AVAILABLE_NOTES.containsKey(note)) {
					final String	normalized = AVAILABLE_NOTES.get(note);
					
					for (MidiNotes item : values()) {
						if (item.getOctaveNumber() == octaveNumber && item.getPresentation().equalsIgnoreCase(normalized)) {
							return item;
						}
					}
					throw new IllegalArgumentException("Unsupported octave number ["+octaveNumber+"] and/or note presentation ["+note+"] for MIDI notes"); 
				}
				else {
					throw new IllegalArgumentException("Non-existent note presentation ["+note+"] for MIDI notes"); 
				}
			}
			else {
				throw new IllegalArgumentException("Error parse presentation string ["+presentation+"]. Must be "+PARSER.pattern()); 
			}
		}
	}	
	
	public static MidiNotes valueOf(final int octaveNumber, final String presentation) {
		if (Utils.checkEmptyOrNullString(presentation)) {
			throw new IllegalArgumentException("Note presentation can't be null or empty");
		}
		else {
			for (MidiNotes item : values()) {
				if (item.getOctaveNumber() == octaveNumber && item.getPresentation().equalsIgnoreCase(presentation)) {
					return item;
				}
			}
			throw new IllegalArgumentException("Unsupported octave number ["+octaveNumber+"] and/or note presentation ["+presentation+"] for MIDI notes"); 
		}
	}
}
