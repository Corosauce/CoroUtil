package CoroUtil.bt.actions;

import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.leaf.LeafAction;

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
		//dbg("Leaf Delay Tick id: " + id + " - " + countCur + "/" + countMax);
		if (countCur++ > countMax) {
			reset();
			return EnumBehaviorState.SUCCESS;
		} else {
			return EnumBehaviorState.RUNNING;
		}
	}
	
	@Override
	public void reset() {
		//dbg("Leaf Delay Reset id: " + id);
		countCur = 0;
		super.reset();
	}
	
}
