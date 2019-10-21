package CoroUtil.bt;

import net.minecraft.nbt.CompoundNBT;
import CoroUtil.bt.selector.Selector;

public class OrdersData {

	public IBTAgent ent;
	public String activeOrdersName = "";
	public Selector activeOrdersAI;
	public EnumBehaviorState activeOrdersStatusLast = EnumBehaviorState.INVALID;
	private float importance = 1F;
	
	public OrdersData() {
		
	}
	
	public void initBehaviors() {
		
	}
	
	public void setImportance(float importance) {
		this.importance = importance;
	}
	
	public float getImportance() {
		return importance;
	}
	
	/*public void readFromNBT(NBTTagCompound parNBT) {
		
	}*/
	
	public CompoundNBT writeToNBT(CompoundNBT parentCompound) {
		return parentCompound;
	}
}
