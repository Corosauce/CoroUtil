package CoroUtil.forge;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.config.ConfigCoroAI;
import CoroUtil.forge.EventHandlerForge.ChunkDataEntry;
import CoroUtil.formation.Manager;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.test.SoundTest;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.player.DynamicDifficulty;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.Phase;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ServerTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EventHandlerFML {

	public static World lastWorld = null;
	public static Manager formationManager;
	
	@SideOnly(Side.CLIENT)
	public static SoundTest soundTest;
	
	public static long timeLast = 0;
	
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
				
				NBTTagCompound nbt = new NBTTagCompound();
				nbt.setInteger("x", 1);
				nbt.setInteger("y", 2);
				nbt.setInteger("z", 3);
				nbt.setString("replymod", "CoroAI");
				
				FMLInterModComms.sendRuntimeMessage("weather2", "weather2", "weather.raining", nbt);
				
			}
			
			DynamicDifficulty.tickServer(event);
		}
		
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tickRender(RenderTickEvent event) {
		//test CoroUtil context render all quests
		if (FMLClientHandler.instance().getClient().theWorld != null && FMLClientHandler.instance().getClient().thePlayer != null) {
			if (event.phase == Phase.END) {
				PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(FMLClientHandler.instance().getClient().thePlayer);
				
				//quests.renderQuestOverlay();
				if (soundTest == null) soundTest = new SoundTest();
				
				if (timeLast != FMLClientHandler.instance().getClient().theWorld.getTotalWorldTime()) {
					timeLast = FMLClientHandler.instance().getClient().theWorld.getTotalWorldTime();
					if (soundTest.active) {
						soundTest.tick();
					}
				}
			}
		}
	}
	

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void tickClient(ClientTickEvent event) {
		if (ConfigCoroAI.debugChunkRenderUpdatesPoll) {
			if (Minecraft.getMinecraft().theWorld == null || event.phase == Phase.START) return;
			long time = Minecraft.getMinecraft().theWorld.getTotalWorldTime();
			if (time % ConfigCoroAI.debugChunkRenderPollRate == 0) {
				System.out.println("poll results: ");
				for (Map.Entry<Integer, ChunkDataEntry> entrySet : EventHandlerForge.lookupChunkUpdateCount.entrySet()) {
					System.out.println(time + " - render data chunkpos: " + entrySet.getValue().x + ", " + entrySet.getValue().z + " - pos: " + (entrySet.getValue().x * 16 + 8) + ", " + (entrySet.getValue().z * 16 + 8) + " = " + entrySet.getValue().count);
				}
				EventHandlerForge.lookupChunkUpdateCount.clear();
			}
		}
	}
	
	@SubscribeEvent
	public void playerLoggedIn(PlayerLoggedInEvent event) {
		PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(event.player);
		quests.sync();
	}
}
