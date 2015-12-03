package CoroUtil.quest.quests;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import CoroUtil.quest.EnumQuestState;
import CoroUtil.quest.PlayerQuests;

public class ActiveQuest {

	//public int questID;
	public PlayerQuests playerQuests;
	
	public int dimIDCreatedIn = 0;
	
	public String questType = "baseQuestObject"; //child classes must override this in their constructor
	public EnumQuestState curState = EnumQuestState.ASSIGNED;
	public boolean returnToQuestGiver;
	
	public String modOwner = "";
	
	//public boolean givesPage = false;
	
	public ActiveQuest() {
		
	}
	
	//should only ever be fired once in the entire existance of quest (active or serialized)
	public void initFirstTime(int parDimensionID) {
		dimIDCreatedIn = parDimensionID;
	}
	
	public void initCreateObject(PlayerQuests parPlQuests) {
		playerQuests = parPlQuests;
		//questID = parID;
	}
	
	public void initCreateLoad() {
		
	}
	
	public void initCustomData() {
		
	}
	
	public void reset() {
		
	}
	
	public void tick() {
		
	}
	
	public void saveAndSync() {
		playerQuests.saveAndSyncAllPlayers();
	}
	
	public void load(NBTTagCompound parNBT) {
		curState = EnumQuestState.get(parNBT.getInteger("curState"));
		dimIDCreatedIn = parNBT.getInteger("dimIDCreatedIn");
		modOwner = parNBT.getString("modOwner");
		returnToQuestGiver = parNBT.getBoolean("returnToQuestGiver");
	}
	
	public void save(NBTTagCompound parNBT) {
		parNBT.setString("classNamePath", this.getClass().getCanonicalName());
		parNBT.setInteger("curState", curState.ordinal());
		parNBT.setInteger("dimIDCreatedIn", dimIDCreatedIn);
		parNBT.setString("modOwner", modOwner);
		parNBT.setBoolean("returnToQuestGiver", returnToQuestGiver);
	}
	
	public void setState(EnumQuestState state) {
		curState = state;
		saveAndSync();
	}
	
	public boolean isPlayerInOriginalDimension() {
		return dimIDCreatedIn == playerQuests.getWorld().provider.getDimensionId();
	}
	
	public boolean isComplete() {
		return curState == EnumQuestState.COMPLETE;
	}
	
	public void eventComplete() {
		setState(EnumQuestState.COMPLETE);
	}
	
	public void onEvent(Event event) {
		
	}
	
	@Override
	public String toString() {
		return questType;
	}
	
	public String getTitle() {
		return toString();
	}
	
	public List<String> getInstructions(List<String> parList) {
		if (parList == null) parList = new ArrayList<String>();
		parList.add("<missing instructions>");
		return parList;
	}
	
	public List<String> getInfoProgress(List<String> parList) {
		if (parList == null) parList = new ArrayList<String>();
		parList.add("<missing progress info>");
		return parList;
	}
}
