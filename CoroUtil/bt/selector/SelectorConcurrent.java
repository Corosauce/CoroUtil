package CoroUtil.bt.selector;

import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;

/* This class will run all the children on the same tick, or should it bail on the chain if one fails? need to give a return value, SUCCESS be default */
public class SelectorConcurrent extends Selector {

	public SelectorConcurrent(Behavior parParent) {
		super(parParent);
	}
	
	@Override
	public EnumBehaviorState tick() {
		if (activeBehaviorIndex != -1) {
			activeBehaviorIndex = 0;
		}
		
		//chain breaks if one returns failure
		for (int i = 0; i < children.size(); i++) {
			EnumBehaviorState returnState = children.get(i).tick();
			if (returnState == EnumBehaviorState.FAILURE) return returnState; //bail
		}
		
		return EnumBehaviorState.SUCCESS;
		
	}
	
}
