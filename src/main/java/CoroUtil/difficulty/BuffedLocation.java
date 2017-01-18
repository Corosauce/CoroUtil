package CoroUtil.difficulty;

import CoroUtil.util.BlockCoord;
import CoroUtil.world.location.ISimulationTickable;
import CoroUtil.world.location.TickableLocationBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Created by Corosus on 1/13/2017.
 */
public class BuffedLocation extends TickableLocationBase {

    public int buffDistRadius = 32;
    public float difficulty = 1F;

    public BuffedLocation() {
        super();
    }

    public BuffedLocation(int buffDistRadius, float difficulty) {
        super();
        this.buffDistRadius = buffDistRadius;
        this.difficulty = difficulty;
    }

    @Override
    public void init() {

    }

    @Override
    public void initPost() {

    }

    @Override
    public void tickUpdate() {
        World world = getWorld();

        if (world.getTotalWorldTime() % 40 == 0) {
            //System.out.println("tick! " + origin);
        }
    }

    @Override
    public void tickUpdateThreaded() {

    }

    @Override
    public void readFromNBT(NBTTagCompound parData) {
        super.readFromNBT(parData);
        difficulty = parData.getFloat("difficulty");
        buffDistRadius = parData.getInteger("radius");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound parData) {
        parData = super.writeToNBT(parData);
        parData.setInteger("radius", buffDistRadius);
        parData.setFloat("difficulty", difficulty);
        return parData;
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

    @Override
    public boolean isThreaded() {
        return false;
    }
}
