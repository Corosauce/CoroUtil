package CoroUtil.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;

import CoroUtil.music.Melody;
import CoroUtil.music.MusicPlayer;
import CoroUtil.music.NoteHelper;
import CoroUtil.music.Sequencer;

public class SoundTest {
	
	//all these vars are deprecated \\
	public int ticksPlaying = 0;
	public int curBar = 0;
	public int curPosInBar = 0;
	public int tickRate = 0; //bpm???
	public boolean active = false;
	//all these vars are deprecated //
	
	public Synthesizer synth;
	public MidiChannel[] mc;
	public Instrument[] instr;
	
	public List<Melody> listMelodies = new ArrayList<Melody>();

	public void start() {
		try {
			//active = true;
			ticksPlaying = 0;
			curBar = 0;
			curPosInBar = 0;
			//tickRate = 20;
			/*ShortMessage myMsg = new ShortMessage();
		    // Play the note Middle C (60) moderately loud
		    // (velocity = 93)on channel 4 (zero-based).
		    myMsg.setMessage(ShortMessage.NOTE_ON, 4, 60, 93); 
		    Synthesizer synth = MidiSystem.getSynthesizer();
		    synth.open();
		    
		    Instrument[] instr = synth.getDefaultSoundbank().getInstruments();
		    synth.loadInstrument(instr[53]);
		    
		    Receiver synthRcvr = synth.getReceiver();
		    
		    synthRcvr.send(myMsg, -1); // -1 means no time stamp*/
			
			synth = MidiSystem.getSynthesizer();
			synth.open();
			
			mc = synth.getChannels();
			instr = synth.getDefaultSoundbank().getInstruments();
			
			for (int i = 0; i < instr.length; i++) {
				System.out.println(i + ": " + instr[i]);
			}
			//synth.loadInstrument(instr[16]);
			
			//mc[5].programChange(instr[16].getPatch().getProgram());
			
			/*mc[5].noteOn(60, 600);
			
			mc[5].noteOff(60);*/
			
			
			MusicPlayer.initMidi();
			
			Sequencer seq = new Sequencer();
			seq.tempo = 120;
			
			Melody m1 = new Melody(seq);
			m1.setMidiData(5, 48);
			m1.length = NoteHelper.NOTE_HALF;
			m1.octive = 6;
			m1.addNote(0, 0, NoteHelper.NOTE_SIXTEENTH);
			m1.addNote(2, 1F/16F, NoteHelper.NOTE_SIXTEENTH);
			m1.addNote(4, 2F/16F, NoteHelper.NOTE_SIXTEENTH);
			m1.addNote(5, 3F/16F, NoteHelper.NOTE_SIXTEENTH);
			m1.addNote(5, 6F/16F, NoteHelper.NOTE_SIXTEENTH);
			m1.addNote(5, 8F/16F, NoteHelper.NOTE_SIXTEENTH);
			m1.addNote(5, 10F/16F, NoteHelper.NOTE_SIXTEENTH);
			seq.listMelodies.add(m1);
			
			m1 = new Melody(seq);
			m1.setMidiData(5, 48);
			m1.length = NoteHelper.NOTE_SIXTEENTH;
			m1.octive = 4;
			m1.addNote(0, 0, NoteHelper.NOTE_SIXTEENTH);
			m1.addNote(4, 0, NoteHelper.NOTE_SIXTEENTH);
			m1.addNote(7, 0, NoteHelper.NOTE_SIXTEENTH);
			seq.listMelodies.add(m1);
			
			MusicPlayer.startSequence(seq);
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void tick() {
		ticksPlaying++;
		
		tickRate = 40;
		
		curBar = ticksPlaying / tickRate;
		curPosInBar = ticksPlaying % tickRate;
		
		Random rand = new Random();
		
		List<Integer> listNotes = new ArrayList<Integer>();
		listNotes.add(0);
		listNotes.add(2);
		listNotes.add(4);
		listNotes.add(5);
		//listNotes.add(7);
		//listNotes.add(9);
		
		int s1 = 5;
		int s2 = 8;
		
		List<Integer> listTimings = new ArrayList<Integer>();
		listTimings.add(s1);
		listTimings.add(s1*2);
		listTimings.add(s1*3);
		listTimings.add(s1*3 + s2);
		listTimings.add(s1*3 + s2*2);
		listTimings.add(s1*3 + s2*3);
		//listTimings.add(32);
		
		try {
			int chan = 5;
			//mc[5].programChange(instr[100].getPatch().getProgram());
			//mc[5].programChange(instr[128].getPatch().getProgram());
			mc[chan].programChange(0, instr[100].getPatch().getProgram());
			//not full range, not sure why
			//int pitch = 8192 + (int)(Math.sin((Math.PI*2D) / (double)tickRate * (double)curPosInBar) / (Math.PI*2D) * 8192);
			//System.out.println(pitch);
			//mc[5].setPitchBend(pitch);
			
			
			if (listTimings.contains(curPosInBar)) {
				mc[chan].allNotesOff();
				
				int octive = 4;
				
				/*mc[5].noteOn((octive * 13) + 0 + (curPosInBar * 13), 100);
				mc[5].noteOn((octive * 13) + 4 + (curPosInBar * 13), 100);
				mc[5].noteOn((octive * 13) + 7 + (curPosInBar * 13), 100);*/
				
				//mc[5].noteOn((octive * 13) + (rand.nextInt(20)), 100);
				mc[chan].noteOn((octive * 13) + (listNotes.get(curBar % listNotes.size())), 100);
				//mc[chan].noteOn((octive * 13) + (2+listNotes.get(curBar % listNotes.size())), 100);
				//mc[chan].noteOn((octive * 13) + (4+listNotes.get(curBar % listNotes.size())), 100);
			}
			
			//channel index 9 is for drums
			chan = 9;
			
			mc[chan].programChange(instr[0].getPatch().getProgram());
			
			if (curPosInBar < 40) {
				if (curPosInBar % 5 == 0) {
					mc[chan].noteOn(36, 600);
				}
				
				if (curPosInBar % 10 == 0) {
					mc[chan].noteOn(31, 600);
				}
			}
			
			if (listTimings.contains(curPosInBar / 2)) {
				//mc[chan].allNotesOff();
				
				int octive = 2;
				//mc[9].noteOn((octive * 13) + (listNotes.get(curBar % listNotes.size())), 200);
				
				//mc[chan].noteOn((octive * 13) + (curPosInBar), 200);
				//mc[chan].noteOn(31, 200);
				
			}
			
			/*if (curPosInBar == 10) {
				mc[5].noteOff(60);
			}*/
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void stop() {
		active = false;
		MusicPlayer.stopAllSequences();
		
		try {
			for (int i = 0; i < mc.length; i++) {
				mc[i].allNotesOff();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
