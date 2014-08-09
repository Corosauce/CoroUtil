package CoroUtil.componentAI.jobSystem;

import net.minecraft.nbt.NBTTagCompound;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.OrdersHandler;
import CoroUtil.entity.EnumJobState;

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
		return lastTrunkReturnState != EnumBehaviorState.RUNNING;
	}
	
	@Override
	public boolean shouldExecute() {
		return true;//ai.entityToAttack == null || ai.entityToAttack.isDead;
	}
	
	@Override
	public void tick() {
		if (trunk != null) {
			lastTrunkReturnState = trunk.tick();
			
			//help tell AIAgent if it should enable idle activity or not
			if (lastTrunkReturnState == EnumBehaviorState.RUNNING) {
				state = EnumJobState.W1;
			} else {
				this.state = EnumJobState.IDLE;
			}
		}
	}
	
	@Override
	public void onIdleTickAct() {
		//having them idle wander from old job code doesnt work well atm, BT forces them back to their moveto coords as soon as they are beyond its threshold range
		//super.onIdleTickAct();
	}

	@Override
	public boolean avoid(boolean actOnTrue) {
		return super.avoid(actOnTrue);
	}
}
