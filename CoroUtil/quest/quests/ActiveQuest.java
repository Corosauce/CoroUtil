package CoroUtil.quest.quests;


import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.quest.EnumQuestState;
import CoroUtil.quest.PlayerQuests;

public class ActiveQuest {

	//public int questID;
	public PlayerQuests playerQuests;
	
	public int dimIDCreatedIn = 0;
	
	public String questType = "baseQuestObject"; //child classes must override this in their constructor
	public EnumQuestState curState = EnumQuestState.ASSIGNED;
	
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
	}
	
	public void save(NBTTagCompound parNBT) {
		parNBT.setString("classNamePath", this.getClass().getCanonicalName());
		parNBT.setInteger("curState", curState.ordinal());
		parNBT.setInteger("dimIDCreatedIn", dimIDCreatedIn);
	}
	
	public void setState(EnumQuestState state) {
		curState = state;
		saveAndSync();
	}
	
	public boolean isPlayerInOriginalDimension() {
		return dimIDCreatedIn == playerQuests.getWorld().provider.dimensionId;
	}
	
	public boolean isComplete() {
		return curState == EnumQuestState.COMPLETE;
	}
	
	public void eventComplete() {
		setState(EnumQuestState.COMPLETE);
	}
}
