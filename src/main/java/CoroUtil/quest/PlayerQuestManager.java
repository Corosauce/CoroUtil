package CoroUtil.quest;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;
import CoroUtil.quest.quests.ActiveQuest;
import CoroUtil.util.CoroUtilEntity;

public class PlayerQuestManager {

	//public static PlayerQuestManager i;
	public HashMap<String, PlayerQuests> playerQuests;
	
	private static PlayerQuestManager serverManager;
	private static PlayerQuestManager clientManager;
	
	public static PlayerQuestManager i() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			if (serverManager == null) {
				serverManager = new PlayerQuestManager();
			}
			return serverManager;
		} else {
			if (clientManager == null) {
				clientManager = new PlayerQuestManager();
			}
			return clientManager;
		}
	}
	
	public PlayerQuestManager() {
		//worldRef = world;
		playerQuests = new HashMap();
		//i = this;
	}
	
	public void check(String username) {
		if (!playerQuests.containsKey(username)) {
			PlayerQuests quests = new PlayerQuests(this, username);
			playerQuests.put(username, quests);
			if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
				quests.diskLoadFromFile();
			}
		}
	}
	
	//needs internals to not use player object first
	public PlayerQuests getPlayerQuests(String username) {
		check(username);
		return playerQuests.get(username);
	}
	
	public PlayerQuests getPlayerQuests(EntityPlayer entP) {
		check(CoroUtilEntity.getName(entP));
		return playerQuests.get(CoroUtilEntity.getName(entP));
	}
	
	//this is being ticked per dimension, is this accurate?
	public void tick(World parWorld) {
		//tick style that auto creates quest object for every player and ticks it
		for (int i = 0; i < parWorld.playerEntities.size(); i++) {
			EntityPlayer entP = (EntityPlayer)parWorld.playerEntities.get(i);
			check(CoroUtilEntity.getName(entP));
			playerQuests.get(CoroUtilEntity.getName(entP)).tick(parWorld);
		}
	}
	
	/*public void giveQuest(int questID, String username, boolean giveToAllPlayers) {
		
		if (giveToAllPlayers) {
			for (int i = 0; i < worldRef.playerEntities.size(); i++) {
				EntityPlayer entP = (EntityPlayer)worldRef.playerEntities.get(i);
				check(entP);
				playerQuests.get(entP.username).giveQuest(questID);
			}
		} else {
			EntityPlayer entP = worldRef.getPlayerEntityByName(username);
			if (entP != null) {
				check(entP);
				playerQuests.get(entP.username).giveQuest(questID);
			}
		}
		
		saveAndSync();
	}*/
	
	public void clearQuests(World parWorld, boolean save, String username) {
		if (username == null || username.equals("")) {
			for (int i = 0; i < parWorld.playerEntities.size(); i++) {
				EntityPlayer entP = (EntityPlayer)parWorld.playerEntities.get(i);
				if (playerQuests.containsKey(CoroUtilEntity.getName(entP))) {
					playerQuests.get(CoroUtilEntity.getName(entP)).questsClearAll();
				}
			}
		} else {
			if (playerQuests.containsKey(username)) {
				playerQuests.get(username).questsClearAll();
			}
			
		}
		
		if (save) saveData(true, false);
	}
	
	//marks for all in current world
	public void markQuestCompleteForAll(World parWorld, ActiveQuest quest) {
		for (int i = 0; i < parWorld.playerEntities.size(); i++) {
			EntityPlayer entP = (EntityPlayer)parWorld.playerEntities.get(i);
			check(CoroUtilEntity.getName(entP));
			playerQuests.get(CoroUtilEntity.getName(entP)).questRemove(quest);
		}
	}
	
	public void onEvent(Event event) {
		for (Map.Entry<String, PlayerQuests> entry : playerQuests.entrySet()) {
			entry.getValue().onEvent(event);
		}
	}
	
	public void saveData(boolean andSync, boolean andUnload) {
		for (Map.Entry<String, PlayerQuests> entry : playerQuests.entrySet()) {
			if (andSync) {
				entry.getValue().saveAndSyncPlayer();
			} else {
				entry.getValue().diskSaveToFile();
			}
			
			if (andUnload) {
				entry.getValue().reset();
			}
		}/*
		for (int i = 0; i < parWorld.playerEntities.size(); i++) {
			EntityPlayer entP = (EntityPlayer)parWorld.playerEntities.get(i);
			check(CoroUtilEntity.getName(entP));
			playerQuests.get(CoroUtilEntity.getName(entP)).saveAndSyncImpl();
		}*/
	}
	
	//its assumed the data was saved already
	public void reset() {
		for (Map.Entry<String, PlayerQuests> entry : playerQuests.entrySet()) {
			entry.getValue().reset();
		}
		playerQuests.clear();
	}
	
	/*public void saveAndUnload(World parWorld) {
		saveAndSync(parWorld);
		clearQuests(parWorld, false, "");
	}*/
	
}
