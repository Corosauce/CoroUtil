package CoroAI.entity;

import net.minecraft.src.*;

public class JobTrade extends JobBase {
	
	public float tradeDistTrigger = 3F;
	
	public int tradeLastItemOffer;
	
	public JobTrade(JobManager jm) {
		super(jm);
	}
	
	public void tick() {
		jobTrading();
	}

	protected void jobTrading() {
		
		//consider this function broken after the refactoring!!!!!!!!!!!!!!!!!! FIX ME EVENTUALLY!
		
		EntityPlayer entP = ent.worldObj.getClosestPlayerToEntity(ent, 16F);
		int pX = (int)(entP.posX-0.5F);
		int pY = (int)entP.posY;
		int pZ = (int)(entP.posZ-0.5F);
		
		//Stop the trade
		if (ent.getDistanceToEntity(entP) > tradeDistTrigger/* || !mod_tropicraft.getIsEngaged(ent.entityId)*/) {
			/*if (mod_tropicraft.getIsEngaged(ent.entityId)) {
				mod_tropicraft.setEngaged(false, -1);
			}
			ent.conversationHandler.hasSaidFirstSentence = false;*/
			tradeTimeout = 600;
			ent.swapJob(jm.priJob);
		}
		
		if (state == EnumJobState.IDLE) {
			ent.walkTo(ent, pX, pY, pZ, ent.maxPFRange, 600);
			setJobState(EnumJobState.W1);
		} else if (state == EnumJobState.W1) {
			if (ent.getDistanceToEntity(entP)/*ent.getDistance(pX, pY, pZ)*/ <= 2F) {
				ent.setPathExToEntity(null);
				ent.faceEntity(entP, 30F, 30F);
				//ent.faceCoord((int)(homeX-0.5F), (int)homeY, (int)(homeZ-0.5F), 180, 180);
			
				//talk to player
				//System.out.println("talk trigger");
				
				//get trade item
				int itemSlot = getRandomTradeItemSlot();
				ent.setCurrentSlot(itemSlot);
				
				setJobState(EnumJobState.W2);
			} else if (walkingTimeout <= 0 || !ent.hasPath()) {
				//ent.setPathExToEntity(null);
				//walkTo(ent, pX, pY, pZ, maxPFRange, 600);
			}
		//Active trading
		} else if (state == EnumJobState.W2) {
			
			//bug fix
			if (ent.inventory.currentItem == 0) {
				ent.setCurrentSlot(getRandomTradeItemSlot());
			}
			
			if (entP.getCurrentEquippedItem() != null && tradeLastItemOffer != entP.getCurrentEquippedItem().itemID) {
				Item tryItem = entP.getCurrentEquippedItem().getItem();
				tradeLastItemOffer = entP.getCurrentEquippedItem().itemID;
				/*if (ent instanceof EntityKoaMemberNew && ((EntityKoaMemberNew)ent).trade_PlayerOfferings.contains(tryItem)) {
					
					ModLoader.getMinecraftInstance().thePlayer.addChatMessage(" <" + '\247' + "a" + ent.name + '\247' + "f" + "> " + ent.conversationHandler.getContentPhrase());
					
				} else {
					ModLoader.getMinecraftInstance().thePlayer.addChatMessage(" <" + '\247' + "a" + ent.name + '\247' + "f" + "> " + ent.conversationHandler.getUpsetPhrase());
				}*/
			}
			
			
			ent.faceEntity(entP, 10F, 10F);
			 
			//dont forget, when transaction occurs, duplicate the item, dont remove from koa (have him switch to main item though)
			
		}
		
		
	}
	
	protected int getRandomTradeItemSlot() {
		ent.activeTradeItemSlot = ent.slot_Trade - ent.rand.nextInt(3);
		
		return ent.activeTradeItemSlot;
	}
	
	public void koaTrade(EntityPlayer ep) {
		ItemStack itemPlayer = ep.getCurrentEquippedItem();
		ItemStack itemKoa = ent.getCurrentEquippedItem();
		
		if (itemKoa == null || itemPlayer == null) return;
		
		//Remove items into buffer, then swap
		
		//Do player item remove
		if (itemPlayer.isStackable() && itemPlayer.stackSize > 0) itemPlayer.stackSize--;
		if (itemPlayer.stackSize <= 0 || !itemPlayer.isStackable()) {
			ep.inventory.mainInventory[ep.inventory.currentItem] = null;
		}
		
		//Koa item remove, wait.... no!
		//itemPlayer.stackSize--;
		/*if (itemPlayer.stackSize == 0) {
			ep.inventory.mainInventory[inventory.currentItem] = null;
		}*/
		ep.inventory.addItemStackToInventory(itemKoa);
		ent.inventory.addItemStackToInventory(itemPlayer);
		
		/*if (mod_tropicraft.getIsEngaged(ent.entityId)) {
			mod_tropicraft.setEngaged(false, -1);
		}
		ent.conversationHandler.hasSaidFirstSentence = false;*/
		//ModLoader.getMinecraftInstance().thePlayer.addChatMessage("Woo");
		ent.swapJob(ent.job.priJob);
		
		//10 minutes
		tradeTimeout = 12000;
		
	}
	
}
