package CoroAI.bt;

import CoroAI.componentAI.ICoroAI;

public class OrdersHandler {
	
	public ICoroAI ent;
	
	public OrdersData activeOrders;
	/*public String activeOrdersName = "";
	public Selector activeOrdersAI;
	public EnumBehaviorState activeOrdersStatusLast = EnumBehaviorState.INVALID;*/
	
	public String ordersAcceptable = "<NULL>";
	
	public OrdersHandler(ICoroAI parEnt) {
		ent = parEnt;
	}
	
	public void setOrders(OrdersData parNewOrders) {
		activeOrders = parNewOrders;
	}
}
