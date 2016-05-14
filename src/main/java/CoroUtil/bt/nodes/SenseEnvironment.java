package CoroUtil.bt.nodes;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.leaf.LeafAction;

public class SenseEnvironment extends LeafAction {

	public BlackboardBase blackboard;
	
	//public int distMedMax = 12;
	//public int distCloseMax = 4;
	
	public SenseEnvironment(Behavior parParent, BlackboardBase parBlackboard) {
		super(parParent);
		blackboard = parBlackboard;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		blackboard.manageCallbackQueue();
		
		Entity target = blackboard.getTarget();
		if (target != null) {
			if (target.isDead || (target instanceof EntityLivingBase && ((EntityLivingBase)target).deathTime > 0)) {
				blackboard.setTarget(null);
			}
		}
		
		//null active target if we need to survive
		/*if (!safetyCheck()) {
			blackboard.setTarget(null);
		}*/
		
		int distLevel = 0; //0 = close, 1 = med, 2 = far
		
		if (blackboard.posMoveTo != null) {
			double distToPos = blackboard.agent.ent.getDistance(blackboard.posMoveTo.xCoord, blackboard.posMoveTo.yCoord, blackboard.posMoveTo.zCoord);
			
			boolean canSeeCoord = this.blackboard.agent.ent.worldObj.rayTraceBlocks(new Vec3(this.blackboard.agent.ent.posX, this.blackboard.agent.ent.posY + (double)this.blackboard.agent.ent.getEyeHeight(), this.blackboard.agent.ent.posZ), new Vec3(blackboard.posMoveTo.xCoord, blackboard.posMoveTo.yCoord+1.5, blackboard.posMoveTo.zCoord)) == null;
			
			if (!canSeeCoord || (target != null && !this.blackboard.agent.ent.canEntityBeSeen(target)) || distToPos > blackboard.distMed.getValue()) {
				distLevel = 2;
			} else {
				if (distToPos < blackboard.distClose.getValue()) {
					distLevel = 0;
				} else {
					distLevel = 1;
				}
			}
		}
		
		blackboard.shouldChaseTarget.setValue(blackboard.agent.profile.shouldChaseTarget());
		blackboard.shouldWander.setValue(blackboard.agent.profile.shouldWander());
		
		if (!safetyCheck()) {
			blackboard.shouldTrySurvival.setValue(true);
			
			//track closest threat, maybe move to survival leaf in template
			float huntRange = 32F;
			if (blackboard.agent.ent.worldObj.getTotalWorldTime() % 10 == 0) {
				boolean found = false;
				//boolean sanityAborted = false;
				Entity clEnt = null;
				float closest = 9999F;
		    	List list = blackboard.agent.ent.worldObj.getEntitiesWithinAABBExcludingEntity(blackboard.agent.ent, blackboard.agent.ent.getEntityBoundingBox().expand(huntRange, huntRange/2, huntRange));
		        for(int j = 0; j < list.size(); j++)
		        {
		            Entity entity1 = (Entity)list.get(j);
		            if(blackboard.agent.isEnemy(entity1))
		            {
		            	if (false || ((EntityLivingBase) entity1).canEntityBeSeen(blackboard.agent.ent)) {
		            		//if (sanityCheck()/* && entity1 instanceof EntityPlayer*/) {
		            			float dist = blackboard.agent.ent.getDistanceToEntity(entity1);
		            			//System.out.println("dist: " + dist);
		            			if (dist < closest) {
		            				closest = dist;
		            				clEnt = entity1;
		            			}
		            		//} else {
		            			//sanityAborted = true;
		            		//}
		            	}
		            }
		        }
		        if (clEnt != null) {
		        	blackboard.lastFleeTarget = clEnt;
		        }
			}
			
		} else {
			blackboard.shouldTrySurvival.setValue(false);
		}
		
		blackboard.shouldFollowOrders.setValue(blackboard.agent.ordersHandler.activeOrders != null && blackboard.agent.profile.shouldFollowOrders());
		
		
		boolean isSafe = false;
		
		blackboard.isPathSafe.setValue(isSafe);
		
		blackboard.isLongPath.setValue(distLevel == 2);
		blackboard.isClosePath.setValue(distLevel == 0);
		blackboard.isSafeOrClosePath.setValue(isSafe || distLevel == 0);
		
		//flying overrides
		if (blackboard.canFlyPath.getValue() || blackboard.canSwimPath.getValue()) {
			if (blackboard.posMoveTo != null) {
				if (canPosBeSeen(blackboard.posMoveTo)) {
					blackboard.isLongPath.setValue(false);
					blackboard.isSafeOrClosePath.setValue(true);
					blackboard.isClosePath.setValue(true);
					blackboard.isPathSafe.setValue(true);
				} else {
					blackboard.isLongPath.setValue(true);
					
				}
			}
		}
		
		blackboard.moveCondition.setValue(distLevel);
		blackboard.isFighting.setValue(/*blackboard.shouldFollowOrders.getValue() && */!blackboard.shouldTrySurvival.getValue() && blackboard.getTarget() != null);
		
		if (blackboard.isFighting.getValue()) {
			//System.out.println("4234");
		}
		
		double horizSpeed = Math.sqrt(blackboard.agent.ent.motionX * blackboard.agent.ent.motionX + blackboard.agent.ent.motionZ * blackboard.agent.ent.motionZ);
		
		
		
		if (horizSpeed > 0.01) {
			//System.out.println(horizSpeed);
			blackboard.isMoving.setValue(true);
		} else {
			blackboard.isMoving.setValue(false);
		}
		
		//there is a potential bug here of an old job in PFQueue.queue still coming back after this timeout has occured, potentially interfering with movement routines...
		if (blackboard.isWaitingForPath.booleanValue()) {
			if (blackboard.lastTimeRequestedPFThreaded + blackboard.PFThreadedTimeout > System.currentTimeMillis()) {
				blackboard.resetReceived();
			}
		}
		
		//temp - force fight
		//blackboard.shouldFollowOrders.setValue(true);
		//blackboard.shouldTrySurvival.setValue(false);
		
		return super.tick();
	}
	
	public boolean safetyCheck() {
		return !blackboard.agent.profile.shouldTrySurvival();
	}
	
	public boolean canPosBeSeen(Vec3 parPos)
    {
		boolean entCheck = this.blackboard.agent.ent.worldObj.rayTraceBlocks(new Vec3(this.blackboard.agent.ent.posX, this.blackboard.agent.ent.posY, this.blackboard.agent.ent.posZ), new Vec3(parPos.xCoord, parPos.yCoord, parPos.zCoord)) == null;
		//boolean topCheck = this.blackboard.agent.ent.worldObj.clip(new Vec3(this.blackboard.agent.ent.posX, this.blackboard.agent.ent.boundingBox.maxY, this.blackboard.agent.ent.posZ), new Vec3(parPos.xCoord, parPos.yCoord, parPos.zCoord)) == null;
		//boolean bottomCheck = this.blackboard.agent.ent.worldObj.clip(new Vec3(this.blackboard.agent.ent.posX, this.blackboard.agent.ent.boundingBox.minY + 0.3, this.blackboard.agent.ent.posZ), new Vec3(parPos.xCoord, parPos.yCoord, parPos.zCoord)) == null;
        return entCheck;
    }
	
	public boolean canEntityBeSeen(Entity par1Entity)
    {
        return this.blackboard.agent.ent.worldObj.rayTraceBlocks(new Vec3(this.blackboard.agent.ent.posX, this.blackboard.agent.ent.posY, this.blackboard.agent.ent.posZ), new Vec3(par1Entity.posX, par1Entity.posY + (double)par1Entity.getEyeHeight(), par1Entity.posZ)) == null;
    }

}
