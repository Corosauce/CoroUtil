package CoroUtil.music;

import java.util.ArrayList;
import java.util.List;

public class Melody {

	public Sequencer sequencer;
	public int ticksPlaying = 0;
	
	//while playing, iterate entire list per tick and play notes that 'match up' to right timing, further thought on how thats done is needed, probably respect time signature standards?
	public List<NoteEntry> listNotes = new ArrayList<NoteEntry>();
	public int octive = 4;
	
	//entry info
	public int bar = 0;
	public int channel = 5;
	public int instrument = 100;
	public int velocity = 100;
	
	public float length = NoteHelper.NOTE_FULL;
	
	public Melody(Sequencer parSeq) {
		sequencer = parSeq;
	}
	
	public void setMidiData(int parChannel, int parInstrument) {
		channel = parChannel;
		instrument = parInstrument;
		MusicPlayer.mc[channel].programChange(0, MusicPlayer.instr[instrument].getPatch().getProgram());
	}
	
	public void addNote(int parNote, float parStart, float parLength) {
		NoteEntry note = new NoteEntry(parNote, parStart, parLength);
		listNotes.add(note);
	}
	
	public void tick() {
		//System.out.println("melody " + this + " ticksPlaying: " + ticksPlaying);
		for (int i = 0; i < listNotes.size(); i++) {
			NoteEntry note = listNotes.get(i);
			
			
			
			if (ticksPlaying == getNoteStart(note)) {
				System.out.println("Start note! " + note.note);
				MusicPlayer.mc[channel].noteOn((octive * NoteHelper.NOTES_IN_OCTIVE) + note.note, velocity);
			}
			
			if (ticksPlaying == getNoteEnd(note)) {
				System.out.println("End note! " + note.note);
				MusicPlayer.mc[channel].noteOff((octive * NoteHelper.NOTES_IN_OCTIVE) + note.note);
			}
		}
		
		if (ticksPlaying == length * NoteHelper.CONV_BEAT_TO_FASTEST_NOTE * NoteHelper.CONV_BEAT_TO_TICK) {
			reset();
		} else {
			ticksPlaying++;
		}
		
		
	}
	
	//needs to be called upon melody completion
	public void reset() {
		ticksPlaying = 0;
		
		//test
		/*if (octive > 0) {
			octive--;
		}*/
	}
	
	public void stop() {
		reset();
	}
	
	public int getNoteStart(NoteEntry parNote) {
		return (int) (parNote.start * NoteHelper.CONV_BEAT_TO_FASTEST_NOTE * NoteHelper.CONV_BEAT_TO_TICK);
	}
	
	public int getNoteEnd(NoteEntry parNote) {
		return (int) (parNote.start * NoteHelper.CONV_BEAT_TO_FASTEST_NOTE * NoteHelper.CONV_BEAT_TO_TICK) + (int) (parNote.length * NoteHelper.CONV_BEAT_TO_FASTEST_NOTE * NoteHelper.CONV_BEAT_TO_TICK);
	}
	
}
