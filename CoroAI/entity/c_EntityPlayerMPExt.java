package CoroAI.entity;

import java.lang.reflect.Field;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import CoroAI.c_CoroAIUtil;
import CoroAI.componentAI.ICoroAI;
import CoroAI.componentAI.IInvUser;

public class c_EntityPlayerMPExt extends EntityPlayerMP
{

	public Field itemInUse;
	public ItemStack itemInUse_val;
	public Field itemInUseCount;
	public int itemInUseCount_val;
	public Field foodLevel;
	
    public c_EntityPlayerMPExt(MinecraftServer par1MinecraftServer, World par2World, String par3Str, ItemInWorldManager par4ItemInWorldManager)
    {
    	
        super(par1MinecraftServer, par2World, par3Str, par4ItemInWorldManager);
        try {
        	itemInUse = c_CoroAIUtil.s_getItemInUse();
        	if (itemInUse != null) itemInUse_val = (ItemStack)itemInUse.get(this);
        	
	        itemInUseCount = c_CoroAIUtil.s_getItemInUseCount();
	        if (itemInUseCount != null) itemInUseCount_val = (Integer)itemInUseCount.get(this);
	        
	        foodLevel = c_CoroAIUtil.s_getFoodLevel();
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
    
    //Overrides added to fix missing getters/setters
    public int getItemInUseCount()
    
    {
    	try {
	    	itemInUseCount_val = (Integer)itemInUseCount.get(this);
	        return itemInUseCount_val;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		return 0;
    	}
    }
    
    public ItemStack getItemInUse()
    {
    	try {
	    	itemInUse_val = (ItemStack)itemInUse.get(this);
	        return itemInUse_val;
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		return null;
    	}
    }
    
    public void setFoodLevel(int level) {
    	try {
    		foodLevel.set(this.foodStats, level);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);

        
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        
    }
    
    @Override
    public String getTranslatedEntityName() {
    	ICoroAI ai = c_CoroAIUtil.playerToCompAILookup.get(username);
    	if (ai instanceof IInvUser) return ((IInvUser) ai).getLocalizedName();
    	return "something horrible";
    }
    
    
}
