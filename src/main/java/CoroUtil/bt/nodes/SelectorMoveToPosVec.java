package CoroUtil.bt.nodes;

import net.minecraft.entity.MobEntity;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.selector.Selector;

public class SelectorMoveToPosVec extends Selector {

	public IBTAgent entInt;
	public MobEntity ent;
	public BlackboardBase blackboard;
	
	public float closeDist;
	
	public SelectorMoveToPosVec(Behavior parParent, IBTAgent parEnt, BlackboardBase parBB, float parCloseDist) {
		super(parParent);
		blackboard = parBB;
		entInt = parEnt;
		ent = (MobEntity)parEnt;
		closeDist = parCloseDist;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		//temp!
		//closeDist = 1;
		
		if (blackboard.posMoveTo != null) {
			//System.out.println("close combat!");
			
			double distToPos = blackboard.agent.ent.getDistance(blackboard.posMoveTo.xCoord, blackboard.posMoveTo.yCoord, blackboard.posMoveTo.zCoord);
			
			if (distToPos < closeDist) {
				return children.get(1).tick();
			} else {
				entInt.getAIBTAgent().setMoveTo(blackboard.posMoveTo.xCoord, blackboard.posMoveTo.yCoord, blackboard.posMoveTo.zCoord);
				//clear any path that was being used
				entInt.getAIBTAgent().pathNav.clearPathEntity();
				return children.get(0).tick();
			}
		}
		
		return EnumBehaviorState.SUCCESS;
		
	}
	
}
