package CoroAI.bt.actions;

import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.leaf.LeafAction;

public class Wander extends LeafAction {

	public Wander(Behavior parParent) {
		super(parParent);
	}
	
	@Override
	public EnumBehaviorState tick() {
		return EnumBehaviorState.FAILURE;
	}
	
}
