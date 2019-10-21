package CoroUtil.bt.nodes;

import net.minecraft.entity.MobEntity;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.selector.Selector;
import net.minecraft.entity.MobEntity;

public class TrackTarget extends Selector {
	
	public IBTAgent entInt;
	public MobEntity ent;
	public BlackboardBase blackboard;
	
	public TrackTarget(Behavior parParent, IBTAgent parEnt, BlackboardBase parBB) {
		super(parParent);
		blackboard = parBB;
		entInt = parEnt;
		ent = (MobEntity)parEnt;
	}

	@Override
	public EnumBehaviorState tick() {
		
		if (blackboard.isFighting.get() && blackboard.shouldChaseTarget.get()) {
			blackboard.trackTarget(false);
		}
		
		return super.tick();
	}
	
}

