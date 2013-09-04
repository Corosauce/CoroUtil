package CoroAI.bt.selector;

import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;

public class SelectorBoolean extends Selector {

	public boolean[] valRef;
	
	//tracking for debug
	public boolean lastVal;
	
	public SelectorBoolean(Behavior parParent, boolean[] parRef) {
		super(parParent);
		valRef = parRef;
	}
	
	@Override
	public EnumBehaviorState tick() {
		//== false required apparently
		if (valRef[0] == false) {
			//dbg("valRef[0] is " + valRef[0] + ", exec: 0");
			setState(children.get(0).tick());
		} else {
			//dbg("valRef[0] is " + valRef[0] + ", exec: 1");
			setState(children.get(1).tick());
		}
		if (valRef[0] != lastVal) {
			//dbg("switch to " + valRef[0]);
			lastVal = valRef[0];
		}
		return this.state;
	}
}
