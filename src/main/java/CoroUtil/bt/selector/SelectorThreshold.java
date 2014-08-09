package CoroUtil.bt.selector;

import org.apache.commons.lang3.mutable.MutableInt;

import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;

public class SelectorThreshold extends Selector {

	//After doing some research of clean coding methods vs efficiencies, I've decided the array trick to passing integer reference is best choice
	//getters and setters will be required on both sides, owell
	//source: https://today.java.net/pub/a/today/2005/03/24/autoboxing.html#performance_issue
	public MutableInt valRef;
	public int threshold;
	
	public SelectorThreshold(Behavior parParent, MutableInt parRef, int parThreshold) {
		super(parParent);
		valRef = parRef;
		threshold = parThreshold;
	}
	
	public SelectorThreshold(Behavior parParent, MutableInt parRef, int parThreshold, Behavior ifLessThan, Behavior ifMoreThanOrEqual) {
		this(parParent, parRef, parThreshold);
		add(ifLessThan);
		add(ifMoreThanOrEqual);
	}
	
	public int getVal() {
		return valRef.getValue();
	}
	
	public void setVal(int inc) {
		valRef.setValue(inc);
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		//threshold code change from sequence
		if (activeBehaviorIndex == -1) {
			setState(EnumBehaviorState.RUNNING);
			if (valRef.getValue() < threshold) {
				activeBehaviorIndex = 0;
			} else {
				activeBehaviorIndex = 1;
			}
		}
		
		if (this.state == EnumBehaviorState.RUNNING) {
			//dbg("SEQ RUNNING");
			if (activeBehaviorIndex != -1) {
				setState(children.get(activeBehaviorIndex).tick());
			} else {
				resetActiveBehavior();
				return EnumBehaviorState.INVALID;
			}
		}
		
		if (this.state == EnumBehaviorState.SUCCESS) {
			//EnumBehaviorState childState = EnumBehaviorState.READY;
			
			resetActiveBehavior();
			return EnumBehaviorState.SUCCESS;
			
			/*while (this.state == EnumBehaviorState.SUCCESS) {
				activeBehaviorIndex++;
				if (activeBehaviorIndex == -1) {
					activeBehaviorIndex = 0;
				} else if (activeBehaviorIndex >= children.size()) {
					resetActiveBehavior();
					return EnumBehaviorState.SUCCESS;
				}
				
				this.state = children.get(activeBehaviorIndex).tick();
			}*/
		} else if (this.state == EnumBehaviorState.FAILURE || this.state == EnumBehaviorState.INVALID) {
			reset();
		}
		
		return this.state;/*
		
		if (valRef[0] < threshold) {
			//dbg("valRef[0] is " + valRef[0] + ", exec: 0");
			setState(children.get(0).tick());
		} else {
			//dbg("valRef[0] is " + valRef[0] + ", exec: 1");
			setState(children.get(1).tick());
		}
		return this.state;*/
	}
}
