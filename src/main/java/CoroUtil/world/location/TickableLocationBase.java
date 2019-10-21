package CoroUtil.world.location;

import CoroUtil.util.BlockCoord;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Base class for ticking locations (threaded ticking optional)
 * Implements the basic requirements for the system eg origin and classname for serialization
 *
 * Created by Corosus on 1/13/2017.
 */
    public abstract class TickableLocationBase implements ISimulationTickable {

    //public World world;
    public int dimensionID;
    public BlockCoord origin;
    public String classname;

    /**
     * Required for serialization
     */
    public TickableLocationBase() {

    }

    @Override
    public void init() {

    }

    @Override
    public void initPost() {

    }

    @Override
    public void tickUpdate() {

    }

    @Override
    public void tickUpdateThreaded() {

    }

    @Override
    public void read(CompoundNBT parData) {
        origin = new BlockCoord(parData.getInteger("posX"), parData.getInt("posY"), parData.getInt("posZ"));
        dimensionID = parData.getInteger("dimensionID");
    }

    @Override
    public CompoundNBT write(CompoundNBT parData) {
        parData.putString("classname", this.getClass().getCanonicalName());
        parData.putInt("posX", origin.getX());
        parData.putInt("posY", origin.getY());
        parData.putInt("posZ", origin.getZ());
        parData.putInt("dimensionID", dimensionID);
        return parData;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void setWorldID(int ID) {
        this.dimensionID = ID;
    }

    @Override
    public World getWorld() {
        return DimensionManager.getWorld(dimensionID);
    }

    @Override
    public void setOrigin(BlockCoord coord) {
        this.origin = coord;
    }

    @Override
    public BlockCoord getOrigin() {
        return origin;
    }

    @Override
    public boolean isThreaded() {
        return false;
    }

    @Override
    public String getSharedSimulationName() {
        return null;
    }
}

