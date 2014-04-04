package CoroUtil.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumResource {
	
	WOOD, STONE, COAL;
	
	private static final Map<Integer, EnumJob> lookup = new HashMap<Integer, EnumJob>();
    static { for(EnumJob e : EnumSet.allOf(EnumJob.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumJob get(int intValue) { return lookup.get(intValue); }
}
