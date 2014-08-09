package CoroUtil.diplomacy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeamInstance {

	//public int id;
	/* blank is neutral, rest is per instance rules */
	public String type = "";
	public int subTeam = -1;
	//public EnumDiploType type = EnumDiploType.HOSTILES;
	
	//attack
	public List<String> listEnemies = new ArrayList<String>();
	
	//should not engage, maybe even avoid
	public List<String> listThreats = new ArrayList<String>();
	
	//allies, would enable use of future generic systems for shared threat assistance, notifying targets to other allies in the area
	public List<String> listAllies = new ArrayList<String>();
	
	public TeamInstance(String parType, String[] enemies, String[] threats, String[] allies) {
		this(parType);
		listEnemies = new ArrayList<String>(Arrays.asList(enemies));
		listThreats = new ArrayList<String>(Arrays.asList(threats));
		listAllies = new ArrayList<String>(Arrays.asList(allies));
	}
	
	public TeamInstance(String parType) {
		type = parType;
	}
	
	public boolean isEnemy(TeamInstance parTeam) {
		boolean isEnemy = listEnemies.contains(parTeam.type);
		if (!isEnemy && subTeam != -1 && parTeam.subTeam != -1 && subTeam != parTeam.subTeam) isEnemy = true;
		return isEnemy;
	}
}

