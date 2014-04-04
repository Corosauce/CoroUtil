package CoroUtil.bt.nodes;

import net.minecraft.entity.EntityLiving;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.selector.Selector;

public class SelectorMoveToPathBest extends Selector {

	public IBTAgent entInt;
	public EntityLiving ent;
	public BlackboardBase blackboard;
	
	public int pathfindRangeFar = 128;
	
	//repathing
	public int repathDelay = 20*20;
	public int repathDelayFailAdd = 60;
	public long lastPathTime = 0;
	
	//threaded timeout tracking
	public int repathWaitTime = 80;
	public long lastRequestTime = 0;
	public boolean lastAttemptFailed = false;
	//public int closeDist; //cant have a close dist, it will reroute to insta path or vec movement node
	
	public SelectorMoveToPathBest(Behavior parParent, IBTAgent parEnt, BlackboardBase parBB) {
		super(parParent);
		blackboard = parBB;
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		//repathfinding over active one has issues, causes a pause in their navigation each time, track down and fix, disabled for now
		
		//TEMP!
		repathWaitTime = 20*5;
		pathfindRangeFar = 128;
		repathDelay = 20*20;
		
		if (blackboard.isPathReceived.getValue()) {
			blackboard.resetReceived();
			//System.out.println("setting final path from threaded pf - " + ent.entityId);
			
			//WE NEED TO MEND PATHS HERE! outdated origin coord on threaded path vs instant temp short path that had them moving
			
			entInt.getAIBTAgent().pathNav.setPath(blackboard.pathMoveToPathFar, blackboard.agent.moveSpeed);
			if (blackboard.pathMoveToPathFar == null || blackboard.pathMoveToPathFar.isFinished()) {
				lastAttemptFailed = true;
				lastPathTime = ent.worldObj.getTotalWorldTime() + repathDelayFailAdd; //add on penalty
			} else {
				lastAttemptFailed = false;
			}
		}
		
		if (blackboard.posMoveTo != null) {
			
			//if (ent.getNavigator().noPath()/*blackboard.pathMoveToPathFar == null || blackboard.pathMoveToPathFar.isFinished()*/) {
			//test for stalling movement
			if ((entInt.getAIBTAgent().pathNav.noPath()/* && !lastAttemptFailed*/)/* || lastPathTime + repathDelay < ent.worldObj.getTotalWorldTime()*/) {
				if (ent.onGround || ent.isInWater()) {
					if (!blackboard.isWaitingForPath.getValue()) {
						//System.out.println("request out - " + ent.entityId);
						
						//TEMP CANCELLING THREAD
						blackboard.requestPathFar(blackboard.posMoveTo, pathfindRangeFar);
						lastRequestTime = ent.worldObj.getTotalWorldTime();
					} else {
						//System.out.println((lastRequestTime + repathWaitTime) - ent.worldObj.getTotalWorldTime());
						if (lastRequestTime + repathWaitTime < ent.worldObj.getTotalWorldTime()) {
							//System.out.println("threaded path request timed out, retrying");
							blackboard.isWaitingForPath.setValue(false); //reset attempt
							blackboard.resetReceived();
						}
					}
					lastPathTime = ent.worldObj.getTotalWorldTime();
				}
				//tick child while waiting (temp insta pathing) - causing issues
				//System.out.println("insta path while wait");
				//temp moveto vec way
				return children.get(0).tick();
			} else {
				
				
				
				
			}
			entInt.getAIBTAgent().pathNav.setCanSwim(true);
			entInt.getAIBTAgent().pathNav.onUpdateNavigation();
		}
		
		return EnumBehaviorState.SUCCESS;
		
	}
	
}
