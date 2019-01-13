package CoroUtil.ai.tasks;

import java.util.List;

import CoroUtil.config.ConfigHWMonsters;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.forge.CoroUtil;
import CoroUtil.packet.PacketHelper;
import CoroUtil.difficulty.DynamicDifficulty;

public class EntityAITaskAntiAir extends EntityAIBase implements ITaskInitializer
{
    private EntityCreature entity = null;
    private EntityPlayer targetLastTracked = null;
    
    private int leapDelayCur = 0;
    //private int leapDelayRate = 40;
    
    private boolean autoAttackTest = true;
    private boolean tryingToGrab = false;
    private boolean grabLock = false;
    
    private String dataPlayerLastPullDownTick = "HW_M_lastPullDownTick";

	//needed for generic instantiation
    public EntityAITaskAntiAir()
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
	@Override
    public boolean shouldExecute()
    {
    	
    	if (!ConfigHWMonsters.antiAir) return false;
    	
    	if (entity.getAttackTarget() != null || autoAttackTest) {
    		targetLastTracked = getFlyingPlayerNear();
    		return targetLastTracked != null;
    		/*if (entity.worldObj.getTotalWorldTime() % 60 == 0) {
    			return true;
    		}*/
    		
    	}
    	
        return false;
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
	public boolean shouldContinueExecuting()
    {
    	
    	if (!ConfigHWMonsters.antiAir) return false;
    	
    	if (entity.getAttackTarget() != null || autoAttackTest) {
	    	targetLastTracked = getFlyingPlayerNear();
			return targetLastTracked != null || tryingToGrab || grabLock;
    	} else {
    		return false;
    	}
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
	public void startExecuting()
    {
    	//System.out.println("start!");
    }

    /**
     * Resets the task
     */
    @Override
	public void resetTask()
    {
    	for (Entity ent : entity.getRecursivePassengers()) {
    		if (ent instanceof EntityPlayer) {
    			//entity.dismountEntity(entityIn);
    			//since removePassenger is private i guess just remove all...
    			//oh wait
    			ent.dismountRidingEntity();
    			//entity.removePassengers();
    			//break;
    		}
    	}
    	/*if (entity.riddenByEntity instanceof EntityPlayer) {
			entity.riddenByEntity.mountEntity(null);
		}*/
    	//System.out.println("reset!");
    }

    /**
     * Updates the task
     */
    @Override
	public void updateTask()
    {
    	
    	entity.fallDistance = 0;
    	
    	if (entity.getAttackTarget() != null || autoAttackTest) {
	    	targetLastTracked = getFlyingPlayerNear();
	    	
	    	if (targetLastTracked != null) {
	    		
	    		double dist = entity.getDistanceToEntity(targetLastTracked);
	    		
	    		//System.out.println(targetLastTracked.motionY);
	    		
	    		
	    		long time = targetLastTracked.getEntityData().getLong(DynamicDifficulty.dataPlayerDetectInAirTime);
	    		boolean inAirLongEnough = time > ConfigHWMonsters.antiAirLeapRate;
	    		
	    		if (ConfigHWMonsters.antiAirType == 0) {
	    		
		    		if (entity.onGround || entity.isInWater() || entity.isInsideOfMaterial(Material.LAVA)) {
		    			
		    			
		    			
		    			
		    	    	if (leapDelayCur == 0 && inAirLongEnough) {
			    			
			    			double vecX = targetLastTracked.posX - entity.posX;
			    			double vecY = targetLastTracked.posY - entity.posY;
			    			double vecZ = targetLastTracked.posZ - entity.posZ;
			    			
			    			if (dist != 0) {
			    				vecX /= dist;
			    				vecY /= dist;
			    				vecZ /= dist;
			    				
			    				double speed = ConfigHWMonsters.antiAirLeapSpeed * dist;
			    				double xzAmp = 1.3D;
			    				
			    				entity.motionX = vecX * speed * xzAmp;
			    				entity.motionY = (vecY * speed) + 0.1D;
			    				entity.motionZ = vecZ * speed * xzAmp;
			    				
			    				//entity.onGround = false;
			    				
			    				leapDelayCur = ConfigHWMonsters.antiAirLeapRate;
			    				
			    				tryingToGrab = true;
			    			}
		    	    	}
		    		} else {
		    			
		    			if (tryingToGrab) {
			    			
			    			
			    			if (dist < 2 || grabLock) {
			    				if (targetLastTracked.getRidingEntity() == null) { 
			    					targetLastTracked.startRiding(entity, true);
				    				//targetLastTracked.mountEntity(entity);
				    				grabLock = true;
				    				if (autoAttackTest && targetLastTracked.capabilities.isFlying) {
				    					targetLastTracked.capabilities.isFlying = false;
				    				}
			    				}
			    				tryingToGrab = false;
			    			}
		    			}
		    			
		    		}
	    		} else if (ConfigHWMonsters.antiAirType == 1) {
	    			if (inAirLongEnough) {
	    				if (ConfigHWMonsters.antiAirApplyPotions) {
				    		targetLastTracked.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 100, 2));
				    		targetLastTracked.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
				    		targetLastTracked.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 100, 2));
	    				}
			    		
			    		if (targetLastTracked instanceof EntityPlayerMP) {
			    			EntityPlayerMP entMP = (EntityPlayerMP) targetLastTracked;
			    			long lastPullTime = targetLastTracked.getEntityData().getLong(dataPlayerLastPullDownTick);
			    			if (entMP.world.getTotalWorldTime() != lastPullTime) {
			    				targetLastTracked.getEntityData().setLong(dataPlayerLastPullDownTick, entMP.world.getTotalWorldTime());
			    				if (ConfigHWMonsters.antiAirUseRelativeMotion) {
			    					CoroUtil.eventChannel.sendTo(PacketHelper.getPacketForRelativeMotion(entMP, 0, ConfigHWMonsters.antiAirPullDownRate, 0), entMP);
			    				} else {
			    					//entMP.playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(targetLastTracked.getEntityId(), 0, ConfigHWMonsters.antiAirPullDownRate, 0));
			    				}
			    			}
			    		}
	    			}
	    		}
	    	}
    	}
    	
    	if (ConfigHWMonsters.antiAirType == 0) {
	    	if (entity.onGround || entity.isInWater() || entity.isInsideOfMaterial(Material.LAVA)) {
	    		if (leapDelayCur > 0) {
		    		leapDelayCur--;
	    		}
	    		grabLock = false;
	    		for (Entity ent : entity.getRecursivePassengers()) {
	        		if (ent instanceof EntityPlayer) {
	        			ent.dismountRidingEntity();
	        		}
	        	}
		    	/*if (entity.riddenByEntity instanceof EntityPlayer) {
					entity.riddenByEntity.mountEntity(null);
				}*/
	    	}
    	}
    }
    
    public EntityPlayer getFlyingPlayerNear() {
    	
    	int findRange = ConfigHWMonsters.antiAirTryDist;
    	AxisAlignedBB aabb = new AxisAlignedBB(entity.posX, entity.posY, entity.posZ, entity.posX, entity.posY, entity.posZ);
		aabb = aabb.grow(findRange, findRange, findRange);
		List list = entity.world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
		boolean found = false;
		double closest = 99999;
		EntityPlayer closestPlayer = null;
        for(int j = 0; j < list.size(); j++)
        {
        	EntityPlayer ent = (EntityPlayer)list.get(j);
        	
        	if (isPlayerFlying(ent)) {
        		if (ent.canEntityBeSeen(entity) && ent.getRidingEntity() == null) {
	        		double dist = ent.getDistanceToEntity(entity);
	        		if (dist < closest) {
	        			closest = dist;
	        			closestPlayer = ent;
	        		}
    			}
        	}
        }
    	
    	return closestPlayer;
    }
    
    public boolean isPlayerFlying(EntityPlayer player) {
    	return player.getEntityData().getLong(DynamicDifficulty.dataPlayerDetectInAirTime) > 0;
    }
}
