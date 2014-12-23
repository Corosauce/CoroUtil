package CoroUtil.music;

import java.util.ArrayList;
import java.util.List;

public class Sequencer implements Runnable {

	//working out classical terms to programmable terms:
	//1/4 is quarter note
	//1/1 is full note, fills 1 bar
	//4 bars per line
	//tempo = bpm
	//bpm and notes have little relation, its just how fast the melody is played / iterated
	//but lets approximate expected rate of play for 120 bpm...
	//we decided a quarter note is a 'beat', so 4 per bar per line, 4*4 = 16 beats per line, 
	//so, for 120 bpm, (60/bpm) * line(16) = 8, 8 seconds per line
	//sounds about right
	//further: a quarter note is a beat, so it would play for 0.5 seconds
	
	//conversion if quarter note was fastest note:
	//trigger note if ticksplaying == note.start * 4
	
	//complication, we must spread out ticksPlaying, in order to account for fastest playable note, sixteenth note (1/16)
	//so every 4th ticksPlaying = 1 beat passed
	
	//in order to convert stored note 1/4 into the real ticks length, we do 1/4 * 4 * 4
	//this converts 1/4 to a 'beat' and then stretches it out more to account for fastest note of 1/16th, a factor of 4 from quarter note
	
	public boolean running = false;
	public int tempo = 120;
	
	public int ticksPlaying = 0;
	
	//possibly unneeded, or just for non note usage
	public int beatsPlaying = 0;
	
	public List<Melody> listMelodies = new ArrayList<Melody>();
	
	@Override
	public void run() {
		while (running) {
			try {
				tickIterate();
				//this is for bpm, not beats per second, so not 1000, but 60000
				float delayMilli = 60000/(tempo*NoteHelper.CONV_BEAT_TO_FASTEST_NOTE);
				int delayNano = (int)((delayMilli - ((int)delayMilli)) * 1000000);
				Thread.sleep((int)delayMilli, delayNano);
			} catch (Throwable throwable) {
                throwable.printStackTrace();
            }
		}
	}
	
	//should be time synced to its bpm give or take process time for each tick
	public void tickIterate() {
		
		//System.out.println("seq ticksPlaying: " + ticksPlaying);
		
		//for now, tick all melodies added
		for (int i = 0; i < listMelodies.size(); i++) {
			Melody mel = listMelodies.get(i);
			mel.tick();
		}
		
		ticksPlaying++;
		
		//output every second, or every beat? yeah every beat
		if (ticksPlaying % (NoteHelper.CONV_BEAT_TO_FASTEST_NOTE) == 0) {
			beatsPlaying++;
			//System.out.println("er: " + ticksPlaying);
		}
	}
	
	public synchronized void start() {
		running = true;
	}
	
	public synchronized void stop() {
		running = false;
		for (int i = 0; i < listMelodies.size(); i++) {
			Melody mel = listMelodies.get(i);
			mel.stop();
		}
	}
	
}
