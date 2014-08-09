package CoroUtil.quest;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumQuestState {
	
	UNASSIGNED, ASSIGNED, MISSION1, MISSION2, MISSION3, CONCLUDING, COMPLETE;
	
	private static final Map<Integer, EnumQuestState> lookup = new HashMap<Integer, EnumQuestState>();
    static { for(EnumQuestState e : EnumSet.allOf(EnumQuestState.class)) { lookup.put(e.ordinal(), e); } }
    public static EnumQuestState get(int intValue) { return lookup.get(intValue); }
}
