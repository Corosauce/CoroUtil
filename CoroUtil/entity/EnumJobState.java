package CoroUtil.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumJobState {
	
	IDLE, W1, W2, W3, W4, W5; 
	
	private static final Map<Integer, EnumJobState> lookup = new HashMap<Integer, EnumJobState>();
	static { for(EnumJobState e : EnumSet.allOf(EnumJobState.class)) { lookup.put(e.ordinal(), e); } }
	public static EnumJobState get(int intValue) { return lookup.get(intValue); }
}
