package CoroUtil.music;

public class NoteHelper {

	public static int CONV_BEAT_TO_FASTEST_NOTE = 4; //factor of 4 shifting beat (1/4) to fastest possible note (1/16)
	public static int CONV_BEAT_TO_TICK = 4; //since 1/4 note is a beat, we must convert again for ticks
	
	public static float NOTE_FULL = 1F;
	public static float NOTE_HALF = 1F/2F;
	public static float NOTE_QUARTER = 1F/4F;
	public static float NOTE_EIGTH = 1F/8F;
	public static float NOTE_SIXTEENTH = 1F/16F;
	
	public static int NOTES_IN_OCTIVE = 12;
	
}
