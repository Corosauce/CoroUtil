package CoroAI.componentAI.jobSystem;

import CoroAI.bt.Behavior;
import CoroAI.bt.EnumBehaviorState;
import CoroAI.bt.OrdersHandler;

//compatibility class between CoroAI v3 task and behavior tree system
public class JobBehavior extends JobBase {
	
	public Behavior trunk;
	public OrdersHandler ordersHandler;
	public EnumBehaviorState lastTrunkReturnState;
	
	public JobBehavior(JobManager jm) {
		super(jm);
	}
	
	@Override
	public boolean shouldContinue() {
		// TODO Auto-generated method stub
		return lastTrunkReturnState != EnumBehaviorState.RUNNING;
	}
	
	@Override
	public boolean shouldExecute() {
		return true;//ai.entityToAttack == null || ai.entityToAttack.isDead;
	}
	
	public void tick() {
		if (trunk != null) {
			lastTrunkReturnState = trunk.tick();
		}
	}
	
	public void onIdleTickAct() {
		
	}

}
