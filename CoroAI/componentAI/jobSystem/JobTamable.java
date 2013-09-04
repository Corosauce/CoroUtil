package CoroAI.componentAI.jobSystem;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import CoroAI.componentAI.AITamable;


public class JobTamable extends JobBase {
	
	public ItemStack tameItem = null;
	public int tameTimeCur = 0;
	public int tameTimeGain = 12000;
	public int tameTimeMax = 24000 * 3;
	
	public JobTamable(JobManager jm, ItemStack is) {
		super(jm);
		tameItem = is;
	}
	
	public void tick() {
		if (tameTimeCur > 0) {
			//if (!ai.ent.isPotionActive(Potion.confusion.id)) ai.ent.addPotionEffect(new PotionEffect(Potion.confusion.id, 5, 0)); //moved to generic taming
			tameTimeCur--;
		}
		
		AITamable tamable = entInt.getAIAgent().jobMan.getPrimaryJob().tamable;
		
		if (tamable.isTame() && tameTimeCur == 0) {
			tamable.tameClear();
		}
	}
	
	@Override
	public boolean hookInteract(EntityPlayer par1EntityPlayer) {
		if (par1EntityPlayer.getCurrentEquippedItem() != null) {
			if (!ent.worldObj.isRemote) {
				if (par1EntityPlayer.getCurrentEquippedItem().isItemEqual(tameItem)) {
					AITamable tamable = entInt.getAIAgent().jobMan.getPrimaryJob().tamable;
					if (!tamable.isTame()) tamable.tameBy(par1EntityPlayer.username);
					incTameTime();
					if (ai.useInv) {
						ai.entInv.inventory.setInventorySlotContents(0, par1EntityPlayer.getCurrentEquippedItem().copy());
			    		par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = null;
			    		ai.entInv.sync();
			    		ai.entInv.setCurrentSlot(0);
			    		ai.entInv.rightClickItem();
			    		ai.entInv.inventory.setInventorySlotContents(0, null);
					} else {
						par1EntityPlayer.inventory.mainInventory[par1EntityPlayer.inventory.currentItem] = par1EntityPlayer.getCurrentEquippedItem().copy();
					}
		    		
		    		
				}
			}
    		return true;
    	}
		return super.hookInteract(par1EntityPlayer);
	}
	
	public void incTameTime() {
		tameTimeCur += tameTimeGain;
		if (tameTimeCur > tameTimeMax) tameTimeCur = tameTimeMax;
	}

}
