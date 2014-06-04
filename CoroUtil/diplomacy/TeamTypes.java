package CoroUtil.diplomacy;

import java.util.HashMap;

public class TeamTypes {

	//how do we handle non ICoroAI?
	//try binding them to a team instance, ie vanilla undead to undead TeamInstance, 
	
	//public static List<TeamInstance> typesList = new ArrayList<TeamInstance>();
	public static HashMap<String, TeamInstance> typesLookup = new HashMap<String, TeamInstance>();
	
	
	
	public static void initTypes() {
		typesLookup.clear();
		//typesList.clear();
		
		//Vanilla entity entries, used with instanceof checks in DiplomacyHelper
		addType("player", new String[] {}, new String[] {}, new String[] {}); //stuff that extends EntityPlayer
		addType("animal", new String[] {}, new String[] {}, new String[] {}); //stuff that extends EntityAnimal
		addType("undead", new String[] {"comrade", "koa", "player"}, new String[] {}, new String[] {}); //stuff that extends EntityMob and extra rules like slimes (and maybe also implements IMob?)/
		
		addType("pet", new String[] {"undead"}, new String[] {}, new String[] {"animal"});
		
		//Custom AI category entries
		addType("neutral", new String[] {}, new String[] {}, new String[] {});
		
		//Tropicraft
		addType("koa", new String[] {"ashen", "hostile", "undead" }, new String[] {}, new String[] {});
		addType("ashen", new String[] {"koa", "player", "comrade" }, new String[] {}, new String[] {});
		
		//ZC
		addType("comrade", new String[] {"ashen", "hostile", "undead"}, new String[] {}, new String[] {});
		
		//Epoch
		addType("hostile", new String[] {"koa", "player", "comrade", "town_npc" }, new String[] {}, new String[] {}); //overridden and added to from epoch
		addType("town_npc", new String[] {"ashen", "hostile", "undead"}, new String[] {}, new String[] {});
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
