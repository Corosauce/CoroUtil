package CoroUtil.forge;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.formation.Manager;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.world.WorldDirectorManager;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;

public class EventHandlerFML {

	public static World lastWorld = null;
	public static Manager formationManager;
	
	@SubscribeEvent
	public void tickWorld(WorldTickEvent event) {
		if (event.phase == Phase.START) {
			CoroAI.initTry();
		}
	}
	
	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		
		if (event.phase == Phase.START) {
			//System.out.println("tick coroutil");
			if (formationManager == null) formationManager = new Manager();
			
			//might not account for dynamic dimension addition during runtime
	    	if (lastWorld != DimensionManager.getWorld(0)) {
	    		lastWorld = DimensionManager.getWorld(0);
	    		
	    		World worlds[] = DimensionManager.getWorlds();
	    		for (int i = 0; i < worlds.length; i++) {
	    			worlds[i].addWorldAccess(new CoroAIWorldAccess());
	    		}
	    	}
			
			if (formationManager != null) formationManager.tickUpdate();
			
			//Quest system
	    	World worlds[] = DimensionManager.getWorlds();
			for (int i = 0; i < worlds.length; i++) {
				PlayerQuestManager.i().tick(worlds[i]);
			}
			
			WorldDirectorManager.instance().onTick();
		}
		
	}
	
	@SubscribeEvent
	public void tickRender(RenderTickEvent event) {
		//test CoroUtil context render all quests
		if (FMLClientHandler.instance().getClient().thePlayer != null) {
			if (event.phase == Phase.END) {
				PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(FMLClientHandler.instance().getClient().thePlayer);
				
				//quests.renderQuestOverlay();
			}
		}
	}
	
	@SubscribeEvent
	public void playerLoggedIn(PlayerLoggedInEvent event) {
		PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(event.player);
		quests.sync();
	}
}
