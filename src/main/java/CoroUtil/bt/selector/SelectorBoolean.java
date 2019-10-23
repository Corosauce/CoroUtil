package CoroUtil.bt.selector;

import org.apache.commons.lang3.mutable.MutableBoolean;

import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;

public class SelectorBoolean extends Selector {

	/*
	 * Ticks child index 0 if false, 1 if true
	 *  */
	
	//public boolean[] valRef;
	public MutableBoolean valRef;
	
	//tracking for debug
	public boolean lastVal;
	
	public SelectorBoolean(Behavior parParent, MutableBoolean parRef) {
		super(parParent);
		valRef = parRef;
	}
	
	@Override
	public EnumBehaviorState tick() {
		//== false required apparently
		if (valRef.get() == false) {
			dbg("valRef.get() is " + valRef.get() + ", exec: 0");
			setState(children.get(0).tick());
		} else {
			dbg("valRef.get() is " + valRef.get() + ", exec: 1");
			setState(children.get(1).tick());
		}
		if (valRef.get() != lastVal) {
			dbg("switch to " + valRef.get());
			lastVal = valRef.get();
		}
		if (shouldPrintDebug()) dbg(valRef.get());
		return this.state;
	}
}
