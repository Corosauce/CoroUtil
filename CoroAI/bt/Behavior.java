package CoroAI.bt;

public abstract class Behavior {
	
	Behavior parent;
	public EnumBehaviorState state = EnumBehaviorState.SUCCESS;
	public String debug = "";
	
	public Behavior(Behavior parParent) {
		parent = parParent;
	}
	
	public void setState(EnumBehaviorState parState) {
		state = parState;
	}
	
	public EnumBehaviorState tick() {
		return EnumBehaviorState.SUCCESS;
	}
	
	//Called when a tree has to abort due to higher priority event
	public void reset() {
		setState(EnumBehaviorState.SUCCESS);
	}
	
}
