package CoroUtil.ai.tasks;

import java.util.List;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.AxisAlignedBB;
import CoroUtil.ai.ITaskInitializer;

public class TaskCallForHelp extends EntityAIBase implements ITaskInitializer
{
    private EntityCreature entity = null;
    //private EntityLivingBase targetLastTracked = null;

    //needed for generic instantiation
    public TaskCallForHelp()
    {
        //this.setMutexBits(3);
    }
    
    @Override
    public void setEntity(EntityCreature creature) {
    	this.entity = creature;
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute()
    {
    	//System.out.println("should call for help?");
    	//return true if not pathing, has target
    	if (entity.getAttackTarget() != null) {
    		if (entity.world.getTotalWorldTime() % 60 == 0) {
    			return true;
    		}
    		
    	}
    	
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
    	//System.out.println("start!");
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
    	//System.out.println("reset!");
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {

        //fix for stealth mods that null out target entity in weird spots even after shouldExecute and shouldContinueExecuting is called
        if (entity.getAttackTarget() == null) {
            resetTask();
            return;
        }

    	//System.out.println("calling for help!");
    	int callRange = 150;
    	AxisAlignedBB aabb = new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ);
		aabb = aabb.grow(callRange, callRange, callRange);
		List list = entity.world.getEntitiesWithinAABB(EntityZombie.class, aabb);
		boolean found = false;
        for(int j = 0; j < list.size(); j++)
        {
        	EntityCreature ent = (EntityCreature)list.get(j);
        	if (ent.getAttackTarget() == null && ent.canEntityBeSeen(entity)) {
        		found = true;
        		//System.out.println(ent + " answered call!");
        		ent.setAttackTarget(entity.getAttackTarget());
        	}
        }
        if (!found && list.size() < 50) {
        	if (entity.getAttackTarget() != null/* && entity.getAttackTarget().getDistanceToEntity(entity) > 16*/) {
        		/*EntityZombie zombie = new EntityZombie(entity.worldObj);
        		zombie.setPosition(entity.posX, entity.posY, entity.posZ);
        		entity.worldObj.spawnEntityInWorld(zombie);
        		System.out.println("spawned in new zombie!");*/
        	}
        	
        }
    }
}
