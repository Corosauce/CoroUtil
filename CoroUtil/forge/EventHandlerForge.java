package CoroUtil.forge;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import CoroUtil.quest.PlayerQuestManager;

public class EventHandlerForge {

	@SubscribeEvent
	public void deathEvent(LivingDeathEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
	
	@SubscribeEvent
	public void pickupEvent(EntityItemPickupEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
	
	@SubscribeEvent
	public void worldSave(Save event) {
		
		//this is called for every dimension
		
		if (((WorldServer)event.world).provider.dimensionId == 0) {
			CoroAI.writeOutData(false);
		}
	}
}
