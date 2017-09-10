package CoroUtil.forge;

import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.test.SoundTest;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.player.DynamicDifficulty;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.event.FMLInterModComms;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
public class EventHandlerFML {

	public static World lastWorld = null;
	//public static Manager formationManager;

	@SideOnly(Side.CLIENT)
	public static SoundTest soundTest;

	public static long timeLast = 0;

	@SubscribeEvent
	public void tickWorld(WorldTickEvent event) {
		if (event.phase == Phase.START) {
			CoroUtil.initTry();
		}
	}

	@SubscribeEvent
	public void tickServer(ServerTickEvent event) {

		if (event.phase == Phase.START) {
			//System.out.println("tick coroutil");
			//if (formationManager == null) formationManager = new Manager();

			//might not account for dynamic dimension addition during runtime
	    	if (lastWorld != DimensionManager.getWorld(0)) {
	    		lastWorld = DimensionManager.getWorld(0);

	    		World worlds[] = DimensionManager.getWorlds();
	    		for (int i = 0; i < worlds.length; i++) {
	    			worlds[i].addEventListener(new CoroAIWorldAccess());
	    		}
	    	}

			//if (formationManager != null) formationManager.tickUpdate();

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
		if (FMLClientHandler.instance().getClient().world != null && FMLClientHandler.instance().getClient().player != null) {
			if (event.phase == Phase.END) {
				PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(FMLClientHandler.instance().getClient().player);

				//quests.renderQuestOverlay();
				if (soundTest == null) soundTest = new SoundTest();

				if (timeLast != FMLClientHandler.instance().getClient().world.getTotalWorldTime()) {
					timeLast = FMLClientHandler.instance().getClient().world.getTotalWorldTime();
					if (soundTest.active) {
						soundTest.tick();
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void playerLoggedIn(PlayerLoggedInEvent event) {
		PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(event.player);
		quests.sync();
	}
}
