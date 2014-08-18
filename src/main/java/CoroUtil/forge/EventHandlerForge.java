package CoroUtil.forge;

import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

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
	
	@SubscribeEvent
	public void worldLoad(Load event) {
		if (!event.world.isRemote) {
			if (((WorldServer)event.world).provider.dimensionId == 0) {
				WorldDirectorManager.instance().registerWorldDirector(new WorldDirector(), CoroAI.modID, event.world);
			}
		}
	}
	
	@SubscribeEvent
	public void breakBlockHarvest(HarvestDropsEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
	
	@SubscribeEvent
	public void breakBlockPlayer(BreakEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
}
