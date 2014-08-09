package CoroUtil.bt;

public abstract class Behavior {
	
	public Behavior parent;
	public EnumBehaviorState state = EnumBehaviorState.SUCCESS;
	public String debug = "";
	public String dbgName = "";
	
	public Behavior(Behavior parParent) {
		parent = parParent;
		dbgName = this.toString();
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
	
	public boolean shouldPrintDebug() {
		return false;//debug.length() > 0;
	}
	
	public void dbg(Object obj) {
		if (AIBTAgent.DEBUGTREES) {
			System.out.println((debug.length() > 0 ? debug : dbgName) + " debug: " + obj);
		}
	}
	
}
