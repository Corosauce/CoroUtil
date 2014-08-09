package CoroUtil.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumActState 
{
	IDLE, WALKING, FOLLOWING, FIGHTING, PLAYING, DANCING, SLEEPING;
	
	private static final Map<Integer, EnumActState> lookup = new HashMap<Integer, EnumActState>();
    static { for(EnumActState e : EnumSet.allOf(EnumActState.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumActState get(int intValue) { return lookup.get(intValue); }
}
