package CoroUtil.music;

import java.util.ArrayList;
import java.util.List;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;


public class MusicPlayer {
	
	public static Synthesizer synth;
	public static MidiChannel[] mc;
	public static Instrument[] instr;
	
	public static List<Sequencer> listSequencers = new ArrayList<Sequencer>();
	
	public static void initMidi() {
		try {
			synth = MidiSystem.getSynthesizer();
			synth.open();
			mc = synth.getChannels();
			instr = synth.getDefaultSoundbank().getInstruments();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void startSequence(Sequencer parSequence) {
		listSequencers.add(parSequence);
		parSequence.start();
		(new Thread(parSequence, "SequencerThread")).start();
	}
	
	public static void stopAllSequences() {
		for (int i = 0; i < listSequencers.size(); i++) {
			listSequencers.get(i).stop();
		}
		
		listSequencers.clear();
		
		//stop all notes in everything
		for (int i = 0; i < mc.length; i++) {
			mc[i].allNotesOff();
		}
	}
	

}
