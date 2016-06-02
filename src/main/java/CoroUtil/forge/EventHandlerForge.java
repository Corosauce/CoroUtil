package CoroUtil.forge;

import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.chunk.ChunkDataPoint;
import CoroUtil.world.player.DynamicDifficulty;

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
		//check server side because some mods invoke saving client side (bad standard)
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			if (((WorldServer)event.getWorld()).provider.getDimension() == 0) {
				CoroAI.writeOutData(false);
			}
		}
	}
	
	@SubscribeEvent
	public void worldLoad(Load event) {
		if (!event.getWorld().isRemote) {
			if (((WorldServer)event.getWorld()).provider.getDimension() == 0) {
				if (WorldDirectorManager.instance().getWorldDirector(CoroAI.modID, event.getWorld()) == null) {
					WorldDirectorManager.instance().registerWorldDirector(new WorldDirector(true), CoroAI.modID, event.getWorld());
				}
			}
		}
	}
	
	@SubscribeEvent
	public void breakBlockHarvest(HarvestDropsEvent event) {
		PlayerQuestManager.i().onEvent(event);
		DynamicDifficulty.handleHarvest(event);
	}
	
	@SubscribeEvent
	public void breakBlockPlayer(BreakEvent event) {
		PlayerQuestManager.i().onEvent(event);
	}
	
	@SubscribeEvent
	public void blockPlayerInteract(PlayerInteractEvent event) {
		if (!event.getWorld().isRemote) {
			try {
				
				//an event is fired where its air and has no chunk X or Z, cancel this
				if (event.action == Action.RIGHT_CLICK_AIR) return;
				
				if (ConfigCoroAI.trackPlayerData) {
					ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(event.getWorld()).getChunkData(event.getPos().getX() / 16, event.getPos().getZ() / 16);
					cdp.addToPlayerActivityInteract(event.getEntityPlayer().getGameProfile().getId(), 1);
				}
				
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	@SubscribeEvent
	public void entityHurt(LivingHurtEvent event) {
		DynamicDifficulty.logDamage(event);
	}
	
	@SubscribeEvent
	public void entityKilled(LivingDeathEvent event) {
		DynamicDifficulty.logDeath(event);
	}
	
	
}
