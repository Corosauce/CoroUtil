package CoroUtil.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumDiploType 
{
	PLAYER, KOA, HOSTILES, COMRADE;
	
	private static final Map<Integer, EnumDiploType> lookup = new HashMap<Integer, EnumDiploType>();
    static { for(EnumDiploType e : EnumSet.allOf(EnumDiploType.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumDiploType get(int intValue) { return lookup.get(intValue); }
}
