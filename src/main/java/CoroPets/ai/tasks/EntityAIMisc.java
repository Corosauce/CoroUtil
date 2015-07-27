package CoroPets.ai.tasks;

import java.util.List;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import CoroUtil.OldUtil;

public class EntityAIMisc extends EntityAIBase
{
    private EntityLiving thePet;
    World theWorld;

    public EntityAIMisc(EntityLiving par1EntityTameable)
    {
        this.thePet = par1EntityTameable;
        this.theWorld = par1EntityTameable.worldObj;
        //this.setMutexBits(3);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
        //also prevent pet from targetting player, maybe move this to a misc AI ticker task
        if (this.thePet.getAttackTarget() instanceof EntityPlayer) {
        	this.thePet.setAttackTarget(null);
        }
        
        //TODO: find a way to work around mojangs lack of null checking that causes this code to crash out EntityAIAttackOnCollide
        if (this.thePet.getAttackTarget() != null && this.thePet.getAttackTarget().getEntityData().getBoolean(CoroPets.CoroPets.tameString)) {
        	this.thePet.setAttackTarget(null);
        	try {
        		List executingTasks = (List) OldUtil.getPrivateValueBoth(EntityAITasks.class, this.thePet.tasks, "field_75780_b", "executingTaskEntries");
            	if (executingTasks != null) {
            		executingTasks.clear();
            	}
			} catch (Exception e) {
				e.printStackTrace();
			}
        	
        }
        
        //heal
        if (theWorld.getTotalWorldTime() % 20 == 0) {
        	this.thePet.heal(1);
        }
        
        //silence!
        this.thePet.livingSoundTime = -1000;
        
        //System.out.println("health: " + this.thePet.getHealth());

        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
    	return false;
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting()
    {
    	
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
    	
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
    	
    }
}
