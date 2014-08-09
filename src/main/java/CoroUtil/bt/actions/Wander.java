package CoroUtil.bt.actions;

import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.leaf.LeafAction;

public class Wander extends LeafAction {

	public Wander(Behavior parParent) {
		super(parParent);
	}
	
	@Override
	public EnumBehaviorState tick() {
		return EnumBehaviorState.FAILURE;
	}
	
}
