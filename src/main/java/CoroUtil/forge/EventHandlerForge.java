package CoroUtil.forge;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.Tuple;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.RenderWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.grid.chunk.ChunkDataPoint;
import CoroUtil.world.player.DynamicDifficulty;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandlerForge {
	
	public static HashMap<Integer, ChunkDataEntry> lookupChunkUpdateCount = new HashMap<Integer, ChunkDataEntry>();
	
	static class ChunkDataEntry {
		public int x = 0;
		public int z = 0;
		public int count = 0;
		public int hash = 0;
		
		public ChunkDataEntry(int x, int z) {
			this.x = x;
			this.z = z;
			this.hash = PathPoint.makeHash(x, 0, z);
		}
		
		@Override
		public int hashCode() {
			return hash;
		}
	}

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
			if (((WorldServer)event.world).provider.dimensionId == 0) {
				CoroAI.writeOutData(false);
			}
		}
	}
	
	@SubscribeEvent
	public void worldLoad(Load event) {
		if (!event.world.isRemote) {
			if (((WorldServer)event.world).provider.dimensionId == 0) {
				if (WorldDirectorManager.instance().getWorldDirector(CoroAI.modID, event.world) == null) {
					WorldDirectorManager.instance().registerWorldDirector(new WorldDirector(true), CoroAI.modID, event.world);
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
		if (!event.world.isRemote) {
			try {
				
				//an event is fired where its air and has no chunk X or Z, cancel this
				if (event.action == Action.RIGHT_CLICK_AIR) return;
				
				if (ConfigCoroAI.trackPlayerData) {
					ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(event.world).getChunkData(event.x / 16, event.z / 16);
					cdp.addToPlayerActivityInteract(event.entityPlayer.getGameProfile().getId(), 1);
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
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void renderWorldPre(RenderWorldEvent.Pre event) {
		if (ConfigCoroAI.debugChunkRenderUpdates || ConfigCoroAI.debugChunkRenderUpdatesPoll) {
			ChunkCache cache = event.chunkCache;
			int chunkX = ObfuscationReflectionHelper.getPrivateValue(ChunkCache.class, cache, "field_72818_a", "chunkX");
			int chunkZ = ObfuscationReflectionHelper.getPrivateValue(ChunkCache.class, cache, "field_72816_b", "chunkZ");
			
			long time = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
			
			if (ConfigCoroAI.debugChunkRenderUpdates) {
				System.out.println(time + " - render update for chunk: " + chunkX + ", " + chunkZ + " - pos: " + (chunkX * 16 + 8) + ", " + (chunkZ * 16 + 8));
			}
			
			if (ConfigCoroAI.debugChunkRenderUpdatesPoll) {
				int hash = PathPoint.makeHash(chunkZ, 0, chunkX);
				if (!lookupChunkUpdateCount.containsKey(hash)) {
					lookupChunkUpdateCount.put(hash, new ChunkDataEntry(chunkX, chunkZ));
				}
				
				ChunkDataEntry entry = lookupChunkUpdateCount.get(hash);
				entry.count++;
				
			}
		}
		
		
	}
	
}
