package CoroAI.bt.actions;

import java.util.Random;

import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.leaf.LeafAction;

public class RandomChance extends LeafAction {

	public RandomChance(Behavior parParent) {
		super(parParent);
	}
	
	@Override
	public EnumBehaviorState tick() {
		Random rand = new Random();
		Boolean bool = rand.nextBoolean();
		//bool = false;
		System.out.println("Leaf Rand Tick - " + bool);
		return bool ? EnumBehaviorState.SUCCESS : EnumBehaviorState.FAILURE;
	}
	
}
