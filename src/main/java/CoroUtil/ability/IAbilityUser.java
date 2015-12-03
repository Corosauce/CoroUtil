package CoroUtil.ability;

import java.util.concurrent.ConcurrentHashMap;

public interface IAbilityUser {

	public Ability activateAbility(String ability, Object... objects);
	public ConcurrentHashMap getAbilities();
	//public List<String> getAssignedAbilities();
	
}
