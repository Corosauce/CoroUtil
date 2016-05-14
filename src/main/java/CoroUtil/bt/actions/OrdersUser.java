package CoroUtil.bt.actions;

import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.OrdersHandler;
import CoroUtil.bt.leaf.LeafAction;

public class OrdersUser extends LeafAction {

	//its assumed that multiples of this class can be placed in a behavior tree
	//orders object should be reference from the main behavior job
	//orders have names bound to them, somehow implement a list of supported orders
	//so that when the branch is active and ticks this leaf once in a while, it tells the main orders reference its ready to support that list of orders
	//then if you had another one of these classes in another part of the behavior tree, and that part activates, the list of supported orders would change because
	//the active ai context has changed
	//so, how to mark properly, and this assumes if order comes in, active parent then goes right to orders or what?
	//also whats best return value when theres no orders? i guess thats when i mark that its awaiting these orders
	public OrdersHandler orders;
	public String ordersAcceptable = "";
	
	public OrdersUser(Behavior parParent, OrdersHandler parOrders, String parOrdersAcceptable) {
		super(parParent);
		ordersAcceptable = parOrdersAcceptable;
		orders = parOrders;
	}
	
	@Override
	public EnumBehaviorState tick() {
		
		if (orders != null) {
			/*if (ordersAcceptable.equals("build")) {
				System.out.println("!!!!!!!");
			}*/
			//mark that its accepting this type of orders
			//System.out.println("dbg: setting orders to " + ordersAcceptable);
			orders.ordersAcceptable = ordersAcceptable;
			
			if (orders.activeOrders != null) {
				if (ordersAcceptable.contains(orders.activeOrders.activeOrdersName)) {
					//set last active return state for some reason
					orders.activeOrders.activeOrdersStatusLast = orders.activeOrders.activeOrdersAI.tick();
					return orders.activeOrders.activeOrdersStatusLast;
				} else {
					dbg("ALERT: accepted orders object type not allowed for this branch, was orders not properly cancelled? nulling orders, need proper fix, returning FAILURE");
					return EnumBehaviorState.FAILURE;
				}
			} else {
				//i guess use this return state for now to mark empty orders state, here would be where you mark the acceptable orders type i guess
				return EnumBehaviorState.INVALID;
			}
		} else {
			return EnumBehaviorState.INVALID;
		}
	}
	
}
