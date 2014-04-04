package CoroUtil.bt.nodes;

import java.util.Random;

import org.apache.commons.io.filefilter.AgeFileFilter;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
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
	
	public Vec3 lastWanderPos = Vec3.createVectorHelper(0, 0, 0);
	
	public Wander(Behavior parParent, IBTAgent parEnt, BlackboardBase parBB, float parRange) {
		super(parParent);
		blackboard = parBB;
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
		wanderRange = parRange;
	}

	@Override
	public EnumBehaviorState tick() {
		
		wanderRange = 8;
		
		Random rand = new Random();
		
		//if (true) return EnumBehaviorState.SUCCESS;
		
		if (lastWanderPos != null) {
			//we need to clear path to let it keep updating for some reason
			if (ent.getDistance(lastWanderPos.xCoord, lastWanderPos.yCoord, lastWanderPos.zCoord) < 2) {
				////entInt.getAIBTAgent().blackboard.setMoveTo(lastWanderPos);
				//entInt.getAIBTAgent().pathNav.clearPathEntity();
			}
		}
		
		if (rand.nextInt(20*10) != 0 || (!entInt.getAIBTAgent().pathNav.noPath() && !entInt.getAIBTAgent().pathNav.getPath().isFinished())) return EnumBehaviorState.SUCCESS;
		
        boolean flag = false;
        float i = -1;
        float j = -1;
        float k = -1;
        float f = -99999F;
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

        if (flag)
        {
        	lastWanderPos = Vec3.createVectorHelper(i, j, k);
        	entInt.getAIBTAgent().blackboard.setMoveTo(lastWanderPos);
        	//System.out.println("wander - " + i + " - " + j + " - " + k);
        	
        } else {
        	lastWanderPos = null;
        }
	    
		
		return super.tick();
	}
	
}
