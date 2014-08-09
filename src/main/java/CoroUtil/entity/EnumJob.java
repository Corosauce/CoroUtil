package CoroUtil.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumJob {
	
	INVADER,
	
	SHAMAN, WEATHERGURU, CHIEF, FISHERMAN, HUNTER, GATHERER, COOK,
	UNEMPLOYED, FINDFOOD, TRADING, PROTECT, MUSIC, 
	
	PLAYER_SURVIVE,	PLAYER_HUNT, PLAYER_FOLLOW
	;
	
	private static final Map<Integer, EnumJob> lookup = new HashMap<Integer, EnumJob>();
    static { for(EnumJob e : EnumSet.allOf(EnumJob.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumJob get(int intValue) { return lookup.get(intValue); }
}
