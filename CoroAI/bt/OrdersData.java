package CoroAI.bt;

import CoroAI.bt.selector.Selector;
import CoroAI.componentAI.ICoroAI;

public class OrdersData {

	public ICoroAI ent;
	public String activeOrdersName = "";
	public Selector activeOrdersAI;
	public EnumBehaviorState activeOrdersStatusLast = EnumBehaviorState.INVALID;
	
	public OrdersData() {
		
	}
	
	public void initBehaviors() {
		
	}
}
