package CoroUtil.bt;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;

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
		
		return agent.profile.hookHitBy(par1DamageSource, par2);
		
		//dont cancel
		//return false;
    }
	
	//Internal Events
	
	public void hookSetTargetPre(Entity target) {
		
	}
	
}
