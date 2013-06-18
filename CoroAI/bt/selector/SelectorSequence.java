package CoroAI.bt.selector;

import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;

public class SelectorSequence extends Selector {

	public SelectorSequence(Behavior parParent) {
		super(parParent);
	}
	
	@Override
	public EnumBehaviorState tick() {
		//this.activeBehaviorIndex = -1;
		
		if (this.state == EnumBehaviorState.RUNNING) {
			System.out.println("SEQ RUNNING");
			if (activeBehaviorIndex != -1) {
				setState(children.get(activeBehaviorIndex).tick());
			} else {
				resetActiveBehavior();
				return EnumBehaviorState.INVALID;
			}
		}
		
		if (this.state == EnumBehaviorState.SUCCESS) {
			//EnumBehaviorState childState = EnumBehaviorState.READY;
			while (this.state == EnumBehaviorState.SUCCESS) {
				activeBehaviorIndex++;
				/*if (activeBehaviorIndex == -1) {
					activeBehaviorIndex = 0;
				} else */if (activeBehaviorIndex >= children.size()) {
					resetActiveBehavior();
					return EnumBehaviorState.SUCCESS;
				}
				
				this.state = children.get(activeBehaviorIndex).tick();
			}
		} else if (this.state == EnumBehaviorState.FAILURE) {
			reset();
		}
		
		return this.state;
	}
	
}
