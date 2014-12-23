package CoroUtil.music;

public class NoteEntry {

	public int note = 0; //base note, 0-12+ (over 12 for melodies that stretch past 1 octave range), base octave isnt factored in here
	public float start = 0; //make this also use note values, for typical 4/4 (quadruple time, 4 beats per bar, 4 bars, a beat is a quarter note) time signature, to start on 3rd beat, value would be 3/4
	public float length = 1/4; //default a quarter note https://en.wikipedia.org/wiki/Note_value
	
	public NoteEntry(int parNote, float parStart, float parLength) {
		note = parNote;
		start = parStart;
		length = parLength;
	}
	
}
