package CoroUtil.quest.quests;


import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import CoroUtil.quest.EnumQuestState;
import CoroUtil.util.CoroUtilItem;

public class ItemQuest extends ActiveQuest {
	
	public String neededItemID; //uses ItemRegistry, for blocks it might be best to get its ItemBlock counterpart
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
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EntityItemPickupEvent) {
			pickupEvent((EntityItemPickupEvent)event);
		}
	}
	
	public void pickupEvent(EntityItemPickupEvent event) {
		if (event.getEntityPlayer().equals(playerQuests.getPlayer()) && CoroUtilItem.getNameByItem(event.item.getEntityItem().getItem()).equals(neededItemID)) {
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
	}
	
	@Override
	public void save(NBTTagCompound parNBT) {
		super.save(parNBT);
		parNBT.setString("neededItemID", neededItemID);
		parNBT.setInteger("neededItemCount", neededItemCount);
	}
	
	@Override
	public String getTitle() {
		return "Collect Item";
	}
	
	@Override
	public List<String> getInstructions(List<String> parList) {
		if (parList == null) parList = new ArrayList<String>();
		String itemName = neededItemID;
		Item item = CoroUtilItem.getItemByName(neededItemID);
		if (item != null) {
			ItemStack is = new ItemStack(item);
			itemName = is.getDisplayName();
		}
		String str = "Collect a total of " + neededItemCount + " " + itemName;
		parList.add(str);
		return parList;
	}
	
	@Override
	public List<String> getInfoProgress(List<String> parList) {
		if (parList == null) parList = new ArrayList<String>();
		String str = curItemCount + " of " + neededItemCount + " collected";
		parList.add(str);
		return parList;
	}
}
