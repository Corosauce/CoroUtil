package CoroUtil.forge;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.formation.Manager;
import CoroUtil.quest.PlayerQuestManager;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
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
		}
		
	}
}
