package CoroUtil.quest.quests;


import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import CoroUtil.quest.EnumQuestState;
import CoroUtil.util.CoroUtilItem;

public class ItemQuest extends ActiveQuest {
	
	public String neededItemID;
	public int neededItemCount;
	public boolean returnToQuestGiver;
	
	public int curItemCount;
	
	public ItemQuest() {
		questType = "getItem";
	}
	
	public void initCustomData(String itemID, int count, boolean parReturnToQuestGiver) {
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
		if (event.entityPlayer.equals(playerQuests.getPlayer()) && CoroUtilItem.getNameByItem(event.item.getEntityItem().getItem()).equals(neededItemID)) {
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
		neededItemID = parNBT.getString("neededItemID");
		neededItemCount = parNBT.getInteger("neededItemCount");
		returnToQuestGiver = parNBT.getBoolean("returnToQuestGiver");
	}
	
	@Override
	public void save(NBTTagCompound parNBT) {
		super.save(parNBT);
		parNBT.setString("neededItemID", neededItemID);
		parNBT.setInteger("neededItemCount", neededItemCount);
		parNBT.setBoolean("returnToQuestGiver", returnToQuestGiver);
	}
}
