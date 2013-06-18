package CoroAI.bt.actions;

import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.leaf.LeafAction;

public class Delay extends LeafAction {

	int id = 0;
	int countCur = 0;
	int countMax;
	
	public Delay(Behavior parParent, int max, int parID) {
		super(parParent);
		countMax = max;
		id = parID;
	}
	
	@Override
	public EnumBehaviorState tick() {
		System.out.println("Leaf Delay Tick id: " + id + " - " + countCur + "/" + countMax);
		if (countCur++ > countMax) {
			reset();
			return EnumBehaviorState.SUCCESS;
		} else {
			return EnumBehaviorState.RUNNING;
		}
	}
	
	@Override
	public void reset() {
		System.out.println("Leaf Delay Reset id: " + id);
		countCur = 0;
		super.reset();
	}
	
}
