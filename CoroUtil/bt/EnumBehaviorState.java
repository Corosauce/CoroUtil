package CoroUtil.bt;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumBehaviorState 
{
	INVALID, SUCCESS, FAILURE, RUNNING, SUSPENDED;
	
	private static final Map<Integer, EnumBehaviorState> lookup = new HashMap<Integer, EnumBehaviorState>();
    static { for(EnumBehaviorState e : EnumSet.allOf(EnumBehaviorState.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumBehaviorState get(int intValue) { return lookup.get(intValue); }
}
