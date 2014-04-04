package CoroUtil.bt.nodes;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
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
			
			boolean canSeeCoord = this.blackboard.agent.ent.worldObj.clip(this.blackboard.agent.ent.worldObj.getWorldVec3Pool().getVecFromPool(this.blackboard.agent.ent.posX, this.blackboard.agent.ent.posY + (double)this.blackboard.agent.ent.getEyeHeight(), this.blackboard.agent.ent.posZ), this.blackboard.agent.ent.worldObj.getWorldVec3Pool().getVecFromPool(blackboard.posMoveTo.xCoord, blackboard.posMoveTo.yCoord+1.5, blackboard.posMoveTo.zCoord)) == null;
			
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
		
		if (!safetyCheck()) {
			blackboard.shouldTrySurvival.setValue(true);
		} else {
			blackboard.shouldTrySurvival.setValue(false);
		}
		
		blackboard.shouldFollowOrders.setValue(blackboard.agent.ordersHandler.activeOrders != null && blackboard.agent.profile.shouldFollowOrders());
		
		
		boolean isSafe = false;
		
		blackboard.isPathSafe.setValue(isSafe);
		blackboard.isLongPath.setValue(distLevel == 2);
		blackboard.isClosePath.setValue(distLevel == 0);
		blackboard.isSafeOrClosePath.setValue(isSafe || distLevel == 0);
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
		
		//temp - force fight
		//blackboard.shouldFollowOrders.setValue(true);
		//blackboard.shouldTrySurvival.setValue(false);
		
		return super.tick();
	}
	
	public boolean safetyCheck() {
		return !blackboard.agent.profile.shouldTrySurvival();
	}

}
