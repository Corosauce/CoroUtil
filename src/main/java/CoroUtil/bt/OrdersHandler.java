package CoroUtil.bt;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;

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
	
	/*public void read(NBTTagCompound parNBT, TownObject team) {
		String activeOrdersName = parNBT.getString("activeOrdersName");
		if (!activeOrdersName.equals("")) {

			OrdersData orders = null;
			NBTTagCompound activeOrdersNBT = parNBT.getCompound("activeOrdersNBT");
			
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
	
	public CompoundNBT write(CompoundNBT parentCompound) {
		if (activeOrders != null) {
			parentCompound.putString("activeOrdersName", activeOrders.activeOrdersName);
			
			CompoundNBT activeOrdersNBT = new CompoundNBT();
			activeOrdersNBT = activeOrders.write(activeOrdersNBT);
			parentCompound.put("activeOrdersNBT", activeOrdersNBT);
		}
		return parentCompound;
	}
}
