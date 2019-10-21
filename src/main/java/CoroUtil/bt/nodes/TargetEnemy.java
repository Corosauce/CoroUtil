package CoroUtil.bt.nodes;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.util.math.AxisAlignedBB;
import CoroUtil.bt.AIBTAgent;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.selector.Selector;
import CoroUtil.util.BlockCoord;

public class TargetEnemy extends Selector {

	//0 = nothing to attack, 1 = attacking, 2 = sanity check says no
	//no longer forces a moveto
	
	public IBTAgent entInt;
	public MobEntity ent;
	
	public float rangeHunt = 16;
	public BlockCoord holdPos = null; //if not null, center scan and best target scan is based from this instead of entity, shouldnt cancel active target, and should have a range higher than enemy projectile ranges
	//public float rangeStray = 8;
	public int scanRate = -1;
	public int randRate = -1;
	
	public TargetEnemy(Behavior parParent, IBTAgent parEnt, float parRange, BlockCoord parHoldPos, int parScanRate, int parRandRate) {
		super(parParent);
		entInt = parEnt;
		ent = (MobEntity)parEnt;
		rangeHunt = parRange;
		holdPos = parHoldPos;
		scanRate = parScanRate;
		randRate = parRandRate;
		//rangeStray = parStray;
	}
	
	public boolean sanityCheck(Entity target) {
		/*if (ent.getHealth() < ent.getMaxHealth() / 4F * 2) {
			return false;
		}*/
		return true;
	}

	@Override
	public EnumBehaviorState tick() {
		
		//TEMP!
		//rangeHunt = 16;
		
		boolean xRay = false;
		
		LivingEntity protectEnt = ent;
		Random rand = new Random();
		
		AIBTAgent ai = entInt.getAIBTAgent();
		
		if ((scanRate == -1 || ent.world.getTotalWorldTime() % scanRate == 0) && (ai.blackboard.getTarget() == null || (randRate == -1 || rand.nextInt(randRate) == 0))) {
			boolean found = false;
			boolean sanityAborted = false;
			Entity clEnt = null;
			float closest = 9999F;
	    	List list = null;
	    	if (holdPos != null) {
	    		list = ent.world.getEntitiesWithinAABBExcludingEntity(ent, new AxisAlignedBB(holdPos.posX, holdPos.posY, holdPos.posZ, holdPos.posX, holdPos.posY, holdPos.posZ).grow(rangeHunt*2, rangeHunt/2, rangeHunt*2));
	    	} else {
	    		list = ent.world.getEntitiesWithinAABBExcludingEntity(ent, protectEnt.getEntityBoundingBox().grow(rangeHunt*2, rangeHunt/2, rangeHunt*2));
	    	}
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            if(ai.isEnemy(entity1))
	            {
	            	if (xRay || ((LivingEntity) entity1).canEntityBeSeen(protectEnt)) {
	            		if (sanityCheck(entity1)/* && entity1 instanceof EntityPlayer*/) {
	            			float dist = 0;// = protectEnt.getDistanceToEntity(entity1);
	            			/*if (holdPos != null) {
	            				dist = (float) entity1.getDistance(holdPos.posX, holdPos.posY, holdPos.posZ);
	            			} else {*/
	            				dist = protectEnt.getDistanceToEntity(entity1);
	            			//}
	            			//System.out.println("dist: " + dist);
	            			if (dist < closest && dist < rangeHunt) {
	            				closest = dist;
	            				clEnt = entity1;
	            			}
	            		} else {
	            			sanityAborted = true;
	            		}
	            	}
	            }
	        }
	        if (clEnt != null) {
	        	Entity curTarg = ai.blackboard.getTarget();
	        	if (clEnt != curTarg) {
	        		ai.blackboard.setTarget(clEnt);
	        		//ai.blackboard.trackTarget(true);
	        	} else {
	        		//ai.blackboard.trackTarget(false);
	        	}
	        	//ai.huntTarget(clEnt);
	        	//System.out.println("hunting");
	        	if (children.size() > 1) return children.get(1).tick();
	        } else {
	        	if (!sanityAborted) {
	        		//System.out.println("subjob");
	        		if (children.size() > 0) return children.get(0).tick();
	        	} else {
	        		//System.out.println("fleeing");
	        		if (children.size() > 2) return children.get(2).tick();
	        	}
	        }
		} else {
			if (ai.blackboard.getTarget() != null) {
				//ai.blackboard.trackTarget(false);
				if (children.size() > 1) return children.get(1).tick();
			}
		}
		
		if (ai.blackboard.getTarget() == null && sanityCheck(null)) {
			//System.out.println("subjob");
			if (children.size() > 0) return children.get(0).tick();
		}
		
		if (!sanityCheck(null)) {
			if (children.size() > 2) return children.get(2).tick();
		}
		
		return super.tick();
	}
	
}
