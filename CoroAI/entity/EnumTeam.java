package CoroAI.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumTeam 
{
	PLAYER, KOA, HOSTILES, COMRADE;
	
	private static final Map<Integer, EnumTeam> lookup = new HashMap<Integer, EnumTeam>();
    static { for(EnumTeam e : EnumSet.allOf(EnumTeam.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumTeam get(int intValue) { return lookup.get(intValue); }
}
