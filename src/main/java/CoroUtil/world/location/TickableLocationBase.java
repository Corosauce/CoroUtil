package CoroUtil.world.location;

import CoroUtil.util.BlockCoord;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

/**
 * Base class for ticking locations (threaded ticking optional)
 * Implements the basic requirements for the system eg origin and classname for serialization
 *
 * Created by Corosus on 1/13/2017.
 */
    public abstract class TickableLocationBase implements ISimulationTickable {

    public World world;
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
    public void readFromNBT(NBTTagCompound parData) {
        origin = new BlockCoord(parData.getInteger("posX"), parData.getInteger("posY"), parData.getInteger("posZ"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound parData) {
        parData.setString("classname", this.getClass().getCanonicalName());
        parData.setInteger("posX", origin.getX());
        parData.setInteger("posY", origin.getY());
        parData.setInteger("posZ", origin.getZ());
        return parData;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void setWorld(World world) {
        this.world = world;
    }

    @Override
    public World getWorld() {
        return world;
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
