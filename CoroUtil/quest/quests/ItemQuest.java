package CoroUtil.quest.quests;


import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import CoroUtil.quest.EnumQuestState;

public class ItemQuest extends ActiveQuest {
	
	public int neededItemID;
	public int neededItemCount;
	public boolean returnToQuestGiver;
	
	public int curItemCount;
	
	public ItemQuest() {
		questType = "getItem";
	}
	
	public void initCustomData(int itemID, int count, boolean parReturnToQuestGiver) {
		super.initCustomData();
		
		neededItemID = itemID;
		neededItemCount = count;
		returnToQuestGiver = parReturnToQuestGiver;
	}

	@Override
	public void tick() {
		super.tick();
		
		if (curState == EnumQuestState.ASSIGNED) {
			//check inv for item
			if (curItemCount >= neededItemCount) {
				if (returnToQuestGiver) {
					setState(EnumQuestState.CONCLUDING);
				} else {
					setState(EnumQuestState.COMPLETE);
				}
			}
		} else if (curState == EnumQuestState.CONCLUDING) {
			
		}
	}
	
	public void pickupEvent(EntityItemPickupEvent event) {
		if (event.entityPlayer.equals(playerQuests.getPlayer()) && event.item.getEntityItem().getItem().itemID == neededItemID) {
			curItemCount++;
			saveAndSync();
			System.out.println("quest item inc");
		}
	}
	
	@Override
	public void eventComplete() {
		super.eventComplete();
	}
	
	@Override
	public void load(NBTTagCompound parNBT) {
		super.load(parNBT);
		neededItemID = parNBT.getInteger("neededItemID");
		neededItemCount = parNBT.getInteger("neededItemCount");
		returnToQuestGiver = parNBT.getBoolean("returnToQuestGiver");
	}
	
	@Override
	public void save(NBTTagCompound parNBT) {
		super.save(parNBT);
		parNBT.setInteger("neededItemID", neededItemID);
		parNBT.setInteger("neededItemCount", neededItemCount);
		parNBT.setBoolean("returnToQuestGiver", returnToQuestGiver);
	}
}
