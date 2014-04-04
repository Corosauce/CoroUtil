package CoroUtil.entity;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumInfo 
{
	HOME_COORD, FOOD_COORD, FOOD_ENTITY, DIPL_WARN;
	
	private static final Map<Integer, EnumInfo> lookup = new HashMap<Integer, EnumInfo>();
    static { for(EnumInfo e : EnumSet.allOf(EnumInfo.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumInfo get(int intValue) { return lookup.get(intValue); }
}
