package CoroUtil.world.location;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;
import CoroUtil.util.BlockCoord;
import net.minecraft.world.World;

public interface ISimulationTickable {

	public void init();
	public void initPost();
	public void tickUpdate();
	public void tickUpdateThreaded();
	public void readFromNBT(CompoundNBT parData);
	public CompoundNBT writeToNBT(CompoundNBT parData);
	public void cleanup();
	public void setWorldID(int ID);
	public World getWorld();
	public BlockCoord getOrigin();
	public void setOrigin(BlockCoord coord);
	public boolean isThreaded();
	public String getSharedSimulationName();
}
