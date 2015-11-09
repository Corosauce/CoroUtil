package CoroUtil.world.location;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

public interface ISimulationTickable {

	public void tickUpdate();
	public void readFromNBT(NBTTagCompound parData);
	public void writeToNBT(NBTTagCompound parData);
	public void cleanup();
	public ChunkCoordinates getOrigin();
}
