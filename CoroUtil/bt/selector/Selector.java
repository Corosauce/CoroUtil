package CoroUtil.bt.selector;

import java.util.ArrayList;
import java.util.List;

import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;

public class Selector extends Behavior {

	//public Behavior activeBehavior;
	public int activeBehaviorIndex = -1;
	
	public List<Behavior> children;
	
	public Selector(Behavior parParent) {
		super(parParent);
		children = new ArrayList<Behavior>();
	}
	
	protected void addImpl(int parPri, Behavior child) {
		
		//not actually used this way yet, depends on order of adding child nodes for now
		//might wanna add some proper priority using code here, hashmap or maintained list?
		
		children.add(child);
	}
	
	public void add(Behavior child) {
		addImpl(0, child);
	}
	
	public void setActiveBehavior(int index/*, Behavior bh*/) {
		//activeBehavior = bh;
		activeBehaviorIndex = index;
	}
	
	@Override
	public void reset() {
		super.reset();
		resetActiveBehavior();
		resetChildren();
		//dbg(" Reset - " + debug);
		
	}
	
	public void resetChildren() {
		for (int i = 0; i < children.size(); i++) {
			Behavior bh = children.get(i);
			if (bh.state == EnumBehaviorState.RUNNING) {
				bh.reset();
			}
		}
	}
	
	public void resetActiveBehavior() {
		//activeBehavior = null;
		activeBehaviorIndex = -1;
	}
	
}
