package CoroAI.bt;

import CoroAI.bt.actions.Delay;
import CoroAI.bt.actions.RandomChance;
import CoroAI.bt.selector.SelectorConcurrent;
import CoroAI.bt.selector.SelectorPriority;
import CoroAI.bt.selector.SelectorSequence;

public class Test {

	SelectorPriority priSelector;
	
	public Test() {
		
		priSelector = new SelectorPriority(null);
		SelectorSequence pri0 = new SelectorSequence(priSelector);
		pri0.debug = "pri0";
		pri0.add(new RandomChance(pri0));
		pri0.add(new Delay(pri0, 6, 0));
		SelectorSequence pri1 = new SelectorSequence(priSelector);
		pri1.debug = "pri1";
		pri1.add(new Delay(pri1, 3, 1));
		pri1.add(new Delay(pri1, 3, 2));
		priSelector.add(0, pri0);
		priSelector.add(1, pri1);
	}
	
	public void tick() {
		System.out.println("-------------");
		priSelector.tick();
	}
	
}
