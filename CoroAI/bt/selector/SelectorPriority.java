package CoroAI.bt.selector;

import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;

public class SelectorPriority extends Selector {

	public SelectorPriority(Behavior parParent) {
		super(parParent);
	}
	
	public void add(int parPri, Behavior child) {
		super.addImpl(parPri, child);
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		EnumBehaviorState childState = null;
		
		/*if (this.state == EnumBehaviorState.RUNNING) {
			if (activeBehaviorIndex != -1) {
				setState(children.get(activeBehaviorIndex).tick());
			} else {
				resetActiveBehavior();
				return EnumBehaviorState.INVALID;
			}
		}*/
		
		boolean foundPriority = false;
		
		for (int i = 0; i < children.size(); i++) {
			Behavior bh = children.get(i);
			
			if (!foundPriority) {
				childState = bh.tick();
				if (childState == EnumBehaviorState.RUNNING) {
					foundPriority = true;
				}
			} else {
				if (bh.state == EnumBehaviorState.RUNNING) {
					bh.reset();
				}
			}
		}
		
		return childState;
	}
	
}
