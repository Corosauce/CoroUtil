package CoroUtil.bt.orders;

import net.minecraft.entity.EntityLiving;
import net.minecraft.nbt.NBTTagCompound;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.OrdersData;
import CoroUtil.bt.actions.Delay;
import CoroUtil.bt.nodes.TargetEnemy;
import CoroUtil.bt.selector.Selector;
import CoroUtil.bt.selector.SelectorMoveToCoords;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.CoroUtilNBT;

public class OrdersGuardPosition extends OrdersData {

	public IBTAgent entInt;
	public EntityLiving ent;
	public final BlockCoord coordsGuard; //shouldnt need to do reference magic...
	public float guardRadius = 8;
	
	public OrdersGuardPosition(IBTAgent parEnt, BlockCoord parCoords, float parRadius) {
		super();
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
		guardRadius = parRadius;
		coordsGuard = parCoords;
		activeOrdersName = "guard_position";
	}
	
	public static OrdersGuardPosition newFromNBT(IBTAgent parEnt, NBTTagCompound nbt) {
		try {
			OrdersGuardPosition orders = new OrdersGuardPosition(parEnt, CoroUtilNBT.readCoords("coordsGuard", nbt), nbt.getFloat("guardRadius"));
			return orders;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound parentCompound) {
		parentCompound.setFloat("guardRadius", guardRadius);
		if (coordsGuard != null) CoroUtilNBT.writeCoords("coordsGuard", coordsGuard, parentCompound);
		return super.writeToNBT(parentCompound);
	}
	
	@Override
	public void initBehaviors() {
		
		/*
		 *                         doNothing
		 * Hunt, if targ in range<
		 *                         moveToGuard
		 */
		
		
		this.activeOrdersAI = new TargetEnemy(null, entInt, guardRadius, coordsGuard, -1, 20);//new SelectorMoveToCoords(null, this.ent, new BlockCoord[] { coordsGuard }, (int)guardRadius, false, false);
		Selector move = new SelectorMoveToCoords(null, entInt, new BlockCoord[] { coordsGuard }, (int)guardRadius, false, false);
		activeOrdersAI.add(move);
		move.add(new Delay(activeOrdersAI, 1, 0)); //this should never fire because the coords are never null, so no worry of its success return value
		move.add(new Delay(activeOrdersAI, 1, 0));
	}

}
