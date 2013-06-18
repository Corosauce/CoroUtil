package CoroAI.bt.selector;

import java.util.Iterator;

import net.minecraft.entity.ai.EntityAITaskEntry;

import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;

/* Until I can find a specific design purpose for this class, this classes purpose can be fulfulled using SelectorSequence with proper conditions */
public class SelectorConcurrent extends Selector {

	public SelectorConcurrent(Behavior parParent) {
		super(parParent);
	}
	
}
