package CoroAI.forge;

import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import CoroAI.componentAI.ICoroAI;

public class EventHandler {

	@ForgeSubscribe
    public void deathEvent(LivingDeathEvent event) {
		//if (event.entityLiving instanceof ICoroAI) ((ICoroAI)event.entityLiving).getAIAgent().cleanup();
	}
}
