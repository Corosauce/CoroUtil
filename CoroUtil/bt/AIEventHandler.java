package CoroUtil.bt;

import java.util.Map;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import CoroUtil.ability.Ability;

public class AIEventHandler {

	public AIBTAgent agent;
	
	//Handles both incoming events from external influences, and events from internal triggers (maybe move internal triggers to a profile thing?) 
	
	public AIEventHandler(AIBTAgent parAgent) {
		agent = parAgent;
	}
	
	public void cleanup() {
		agent = null;
	}
	
	//External Events
	
	public boolean interact(EntityPlayer par1EntityPlayer) {
		return false;
	}
	
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
    {
		
		Entity ent = par1DamageSource.getEntity();
		if (ent != null && agent.isEnemy(ent)) {
			if (agent.blackboard.getTarget() == null) {
				agent.blackboard.setTarget(ent);
			}

			for (Map.Entry<String, Ability> entry : agent.profile.abilities.entrySet()) {
				if (entry.getValue().isActive()) {
					entry.getValue().setFinishedPerform();
				}
			}
			agent.profile.syncAbilitiesFull(false);
		}
		
		//dont cancel
		return false;
    }
	
	//Internal Events
	
	public void hookSetTargetPre(Entity target) {
		
	}
	
}
