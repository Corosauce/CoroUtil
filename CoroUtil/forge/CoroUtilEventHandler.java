package CoroUtil.forge;

import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import CoroUtil.quest.PlayerQuestManager;

public class CoroUtilEventHandler {

	@ForgeSubscribe
	public void deathEvent(LivingDeathEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
	
	@ForgeSubscribe
	public void pickupEvent(EntityItemPickupEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
	
	@ForgeSubscribe
	public void worldSave(Save event) {
		
		//this is called for every dimension
		
		if (((WorldServer)event.world).provider.dimensionId == 0) {
			CoroAI.writeOutData(false);
		}
	}

}
