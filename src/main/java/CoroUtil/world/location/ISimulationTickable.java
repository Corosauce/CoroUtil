package CoroUtil.world.location;

import net.minecraft.nbt.NBTTagCompound;

public interface ISimulationTickable {

	public void init();
	public void initPost();
	public void tickUpdate();
	public void tickUpdateThreaded();
	public void readFromNBT(NBTTagCompound parData);
	public NBTTagCompound writeToNBT(NBTTagCompound parData);
	public void cleanup();
	public ChunkCoordinates getOrigin();
	public boolean isThreaded();
	public String getSharedSimulationName();
}
