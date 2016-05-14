package CoroUtil.world.location;

import net.minecraft.nbt.NBTTagCompound;
import CoroUtil.util.BlockCoord;

public interface ISimulationTickable {

	public void init();
	public void initPost();
	public void tickUpdate();
	public void tickUpdateThreaded();
	public void readFromNBT(NBTTagCompound parData);
	public NBTTagCompound writeToNBT(NBTTagCompound parData);
	public void cleanup();
	public BlockCoord getOrigin();
	public boolean isThreaded();
	public String getSharedSimulationName();
}
