package CoroUtil.forge;

import java.util.ArrayList;
import java.util.List;

import CoroUtil.client.debug.DebugRenderer;
import CoroUtil.difficulty.DynamicDifficulty;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.api.distmarker.OnlyIn;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.test.SoundTest;
import CoroUtil.world.WorldDirectorManager;
public class EventHandlerFML {

	//public static Manager formationManager;
	
	@OnlyIn(Dist.CLIENT)
	public static SoundTest soundTest;
	
	public static long timeLast = 0;
	
	@SubscribeEvent
	public void tickWorld(WorldTickEvent event) {
		if (event.phase == Phase.START) {
			CoroUtil.initTry();
		}
	}

	@SubscribeEvent
	public void worldLoad(WorldEvent.Load event) {
		int dimID = event.getWorld().provider.getDimension();
		CULog.dbg("adding CoroUtil world listener for dimID: " + dimID + ", remote?: " + event.getWorld().isRemote);
		event.getWorld().addEventListener(new CoroAIWorldAccess());
	}
	
	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {
		
		if (event.phase == Phase.START) {
			//System.out.println("tick coroutil");
			//if (formationManager == null) formationManager = new Manager();
			
			//if (formationManager != null) formationManager.tickUpdate();
			
			//Quest system
	    	World worlds[] = DimensionManager.getWorlds();
			for (int i = 0; i < worlds.length; i++) {
				PlayerQuestManager.i().tick(worlds[i]);
			}
			
			WorldDirectorManager.instance().tick();
			
			boolean debugIMC = false;
	        if (debugIMC) {
		        try {
			    	List<IMCMessage> listMsgs = new ArrayList<IMCMessage>();
			    	listMsgs = FMLInterModComms.fetchRuntimeMessages("CoroAI");
			    	for (int i = 0; i < listMsgs.size(); i++) {
			    		
			    		System.out.println("CoroAI side: " + listMsgs.get(i).key + " - modID: " + listMsgs.get(i).getSender() + " - source: " + listMsgs.get(i).toString() + " - " + listMsgs.get(i).getNBTValue());
			    	}
		    	} catch (Exception ex) {
		    		ex.printStackTrace();
		    	}
	        }
			
			boolean testSendRequestIMC = false;
			if (testSendRequestIMC) {
				
				CompoundNBT nbt = new CompoundNBT();
				nbt.putInt("x", 1);
				nbt.putInt("y", 2);
				nbt.putInt("z", 3);
				nbt.putString("replymod", "CoroAI");
				
				FMLInterModComms.sendRuntimeMessage("weather2", "weather2", "weather.raining", nbt);
				
			}
			
			DynamicDifficulty.tickServer(event);
		}
		
	}
	
	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tickRender(RenderTickEvent event) {
		//test CoroUtil context render all quests
		if (FMLClientHandler.instance().getClient().world != null && FMLClientHandler.instance().getClient().player != null) {
			if (event.phase == Phase.END) {
				//PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(FMLClientHandler.instance().getClient().player);
				
				//quests.renderQuestOverlay();
				/*if (soundTest == null) soundTest = new SoundTest();
				
				if (timeLast != FMLClientHandler.instance().getClient().world.getGameTime()) {
					timeLast = FMLClientHandler.instance().getClient().world.getGameTime();
					if (soundTest.active) {
						soundTest.tick();
					}
				}*/
			}
		}
	}

	@OnlyIn(Dist.CLIENT)
	@SubscribeEvent
	public void tickClient(TickEvent.ClientTickEvent event) {
		DebugRenderer.tickClient();
	}
	
	@SubscribeEvent
	public void playerLoggedIn(PlayerLoggedInEvent event) {
		PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(event.player);
		quests.sync();
	}
}

