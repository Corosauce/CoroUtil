package CoroUtil.quest.quests;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import CoroUtil.quest.EnumQuestState;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilNBT;

public class BreakBlockQuest extends ActiveQuest {
	
	//quest supports breaking a block at a coordinate AND/OR breaking a specific block (x number of times), determines rules based on null or not null values
	
	//note, count is for amount of blocks needed to break, not count in inventory
	
	//configurations
	public BlockCoord blockCoords;
	public String blockType;
	public int blockCountNeeded = -1;
	
	//progression
	public int blockCountCurrent = 0;

	public BreakBlockQuest() {
		questType = "breakBlock";
	}
	
	public void initCustomData(BlockCoord parCoords, Block parBlock) {
		super.initCustomData();

		blockCoords = parCoords;
		blockType = Block.REGISTRY.getNameForObject(parBlock).toString();
		
	}

	@Override
	public void tick() {
		super.tick();
		
		if (curState == EnumQuestState.ASSIGNED) {
			
			if (blockCountNeeded != -1) {
				if (blockCountCurrent >= blockCountNeeded) {
					if (returnToQuestGiver) {
						setState(EnumQuestState.CONCLUDING);
					} else {
						eventComplete();
					}
				}
			}
		} else if (curState == EnumQuestState.CONCLUDING) {
			//logic that determines they have talked to the quest giver to complete the quest, should this be here or in the koa?
		}
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof BreakEvent) {
			handleEvent((BreakEvent)event);
		}
	}
	
	public void handleEvent(BreakEvent event) {
		//System.out.println("EVENT!: " + event.getPlayer() + " - " + event.x + " - " + event.y + " - " + event.z);
		if (event.getPlayer() == null || !CoroUtilEntity.getName(event.getPlayer()).equals(playerQuests.playerName)) {
			return;
		}
		if (getBlock() != null) {
			if (getBlock() != event.state.getBlock()) {
				return;
			}
		}
		if (blockCoords != null) {
			if (blockCoords.posX != event.pos.getX() || blockCoords.posY != event.pos.getY() || blockCoords.posZ != event.pos.getZ()) {
				return;
			}
		}
		if (blockCountNeeded != -1) {
			blockCountCurrent++;
		} else {
			eventComplete();
		}
		saveAndSync();
	}
	
	public Block getBlock() {
		return (Block)Block.REGISTRY.getObject(new ResourceLocation(blockType));
	}
	
	public void load(NBTTagCompound parNBT) {
		super.load(parNBT);
		blockCountNeeded = parNBT.getInteger("blockCountNeeded");
		blockCountCurrent = parNBT.getInteger("blockCountCurrent");
		blockCoords = CoroUtilNBT.readCoords("blockCoords", parNBT);
		blockType = parNBT.getString("blockType");
	}
	
	public void save(NBTTagCompound parNBT) {
		super.save(parNBT);
		parNBT.setInteger("blockCountNeeded", blockCountNeeded);
		parNBT.setInteger("blockCountCurrent", blockCountCurrent);
		CoroUtilNBT.writeCoords("blockCoords", blockCoords, parNBT);
		parNBT.setString("blockType", blockType);
	}
}
