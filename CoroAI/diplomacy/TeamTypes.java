package CoroAI.diplomacy;

import java.util.HashMap;

public class TeamTypes {

	//how do we handle non ICoroAI?
	//try binding them to a team instance, ie vanilla undead to undead TeamInstance, 
	
	//public static List<TeamInstance> typesList = new ArrayList<TeamInstance>();
	public static HashMap<String, TeamInstance> typesLookup = new HashMap<String, TeamInstance>();
	
	public static void initTypes() {
		typesLookup.clear();
		//typesList.clear();
		addType("neutral", new String[] {}, new String[] {}, new String[] {});
		addType("player", new String[] {}, new String[] {}, new String[] {}); //even needed? depends on how non ICoroAI things are handled, and where?
		addType("koa", new String[] {"ashen", "hostile", "undead" }, new String[] {}, new String[] {});
		addType("ashen", new String[] {"koa", "player", "comrade" }, new String[] {}, new String[] {});
		addType("hostile", new String[] {"koa", "player", "comrade" }, new String[] {}, new String[] {});
		addType("animal", new String[] {}, new String[] {}, new String[] {});
		addType("comrade", new String[] {"ashen", "hostile", "undead"}, new String[] {}, new String[] {});
		addType("undead", new String[] {"comrade", "koa", "player"}, new String[] {}, new String[] {});
	}
	
	public static void addType(String parType, String[] enemies, String[] threats, String[] allies) {
		typesLookup.put(parType, new TeamInstance(parType, enemies, threats, allies));
	}
	
	public static TeamInstance getType(String parType) {
		TeamInstance ti = TeamTypes.typesLookup.get(parType);
		if (ti == null) System.out.println("CoroAI WARNING: null team instance for type " + parType);
		return ti;
	}
	
}
