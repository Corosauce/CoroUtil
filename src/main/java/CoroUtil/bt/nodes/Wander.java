package CoroUtil.bt.nodes;

import java.util.Random;

import net.minecraft.entity.EntityLiving;
import net.minecraft.util.Vec3;
import CoroUtil.OldUtil;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.selector.Selector;

public class Wander extends Selector {

	//0 = nothing to attack, 1 = attacking, 2 = sanity check says no
	
	public IBTAgent entInt;
	public EntityLiving ent;
	public BlackboardBase blackboard;
	
	public float wanderRange = 16;
	
	public Vec3 lastWanderPos = new Vec3(0, 0, 0);
	
	public Wander(Behavior parParent, IBTAgent parEnt, BlackboardBase parBB, float parRange) {
		super(parParent);
		blackboard = parBB;
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
		wanderRange = parRange;
	}

	@Override
	public EnumBehaviorState tick() {
		
		if (!blackboard.shouldWander.getValue()) return EnumBehaviorState.SUCCESS;
		
		wanderRange = 4;
		
		Random rand = new Random();
		
		//if (true) return EnumBehaviorState.SUCCESS;
		
		if (lastWanderPos != null) {
			//we need to clear path to let it keep updating for some reason
			if (ent.getDistance(lastWanderPos.xCoord, lastWanderPos.yCoord, lastWanderPos.zCoord) < 2) {
				////entInt.getAIBTAgent().blackboard.setMoveTo(lastWanderPos);
				//entInt.getAIBTAgent().pathNav.clearPathEntity();
			}
		}
		
		//
		
		
		
        boolean flag = false;
        float i = -1;
        float j = -1;
        float k = -1;
        float f = -99999F;
        
        if (entInt.getAIBTAgent().tamable.occupyCoord == null || OldUtil.getDistanceXZ(ent, entInt.getAIBTAgent().tamable.occupyCoord) < entInt.getAIBTAgent().tamable.followDistMax) {
        	
        	if (entInt.getAIBTAgent().tamable.shouldStayStill() || (!entInt.getAIBTAgent().pathNav.noPath() && !entInt.getAIBTAgent().pathNav.getPath().isFinished())) return EnumBehaviorState.SUCCESS;
        	
        	if (rand.nextInt(100) == 0) {
	        	for (int l = 0; l < 10; l++)
	            {
	            	float i1 = (float) (ent.posX + (rand.nextFloat()*wanderRange - (wanderRange/2)));
	            	float j1 = (float) ent.posY;//MathHelper.floor_double((ent.posY + (double)rand.nextInt((int)wanderRange/2)) - wanderRange/4);
	            	float k1 = (float) (ent.posZ + (rand.nextFloat()*wanderRange - (wanderRange/2)));
	                float f1 = 1F;//getBlockPathWeight(i1, j1, k1);
	                if (f1 > f)
	                {
	                    f = f1;
	                    i = i1;
	                    j = j1;
	                    k = k1;
	                    flag = true;
	                    break;
	                }
	            }
        	}
		} else {
			if (!entInt.getAIBTAgent().tamable.shouldStayStill()) {
				if (rand.nextInt(5) == 0) {
					//System.out.println("waiting for path still? " + entInt.getAIBTAgent().blackboard.isWaitingForPath.booleanValue());
					if (!entInt.getAIBTAgent().blackboard.isWaitingForPath.booleanValue()/* && (entInt.getAIBTAgent().pathNav.noPath() || entInt.getAIBTAgent().pathNav.getPath().isFinished())*/) {
						flag = true;
						i = (float) entInt.getAIBTAgent().tamable.getPlayerCached().posX;
						j = (float) entInt.getAIBTAgent().tamable.getPlayerCached().posY;
						k = (float) entInt.getAIBTAgent().tamable.getPlayerCached().posZ;
						entInt.getAIBTAgent().tamable.occupyCoord = new ChunkCoordinates((int)i, (int)j, (int)k);
						//i = occupyCoord.
						//job.ai.walkTo(job.ent, occupyCoord, job.ai.maxPFRange, 600);
						//int randsize = 8;
			    		//job.ai.walkTo(ent, ai.homeX+rand.nextInt(randsize) - (randsize/2), ai.homeY+1, ai.homeZ+rand.nextInt(randsize) - (randsize/2),ai.maxPFRange, 600);
					}
				}
			}
		}
        
        if (flag)
        {
        	lastWanderPos = new Vec3(i, j, k);
        	entInt.getAIBTAgent().blackboard.setMoveTo(lastWanderPos);
        	//System.out.println("wander - " + i + " - " + j + " - " + k);
        	
        } else {
        	lastWanderPos = null;
        }
	    
		
		return super.tick();
	}
	
}
