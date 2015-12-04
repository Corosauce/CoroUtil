package CoroUtil.bt;

import net.minecraft.nbt.NBTTagCompound;

public class OrdersHandler {
	
	public IBTAgent ent;
	
	public OrdersData activeOrders;
	public String ordersAcceptable = "<NULL>";
	
	public OrdersHandler(IBTAgent parEnt) {
		ent = parEnt;
	}
	
	public void setOrders(OrdersData parNewOrders) {
		activeOrders = parNewOrders;
	}
	
	/*public void readFromNBT(NBTTagCompound parNBT, TownObject team) {
		String activeOrdersName = parNBT.getString("activeOrdersName");
		if (!activeOrdersName.equals("")) {

			OrdersData orders = null;
			NBTTagCompound activeOrdersNBT = parNBT.getCompoundTag("activeOrdersNBT");
			
			//needs to be moved to some sort of nbt serialization mapping
			//orders name -> class, new instance with basic constructor, call its read with nbt data
			if (activeOrdersName.equals("gather")) {
				orders = OrdersGatherRes.newOrdersGatherResFromNBT(activeOrdersNBT, team);//(resNode, new BlockCoord[] { new BlockCoord(resNode.posOrigin) }, new BlockCoord[] { new BlockCoord(team.spawn) }, team);
			} else if (activeOrdersName.equals("guard_position")) {
				orders = OrdersGuardPosition.newFromNBT(ent, activeOrdersNBT, team);
			}
			
			if (orders != null) {
				ent.getAIBTAgent().ordersHandler.setOrders(orders);
				orders.ent = ent;
				orders.initBehaviors();
			}
			
		}
	}*/
	
	public NBTTagCompound writeToNBT(NBTTagCompound parentCompound) {
		if (activeOrders != null) {
			parentCompound.setString("activeOrdersName", activeOrders.activeOrdersName);
			
			NBTTagCompound activeOrdersNBT = new NBTTagCompound();
			activeOrdersNBT = activeOrders.writeToNBT(activeOrdersNBT);
			parentCompound.setTag("activeOrdersNBT", activeOrdersNBT);
		}
		return parentCompound;
	}
}
