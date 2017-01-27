package CoroUtil.difficulty;

import CoroUtil.util.BlockCoord;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.ISimulationTickable;
import CoroUtil.world.location.TickableLocationBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Created by Corosus on 1/13/2017.
 */
public class BuffedLocation extends TickableLocationBase {

    //inverts role, actually lowers difficulty in area
    private boolean isDebuff;

    public int buffDistRadius = 32;
    public float difficulty = 1F;

    private boolean tickDecay = false;
    private int age = 0;
    private int maxAge = 20*60*10;

    public BuffedLocation() {
        super();
    }

    public BuffedLocation(int buffDistRadius, float difficulty) {
        super();
        this.buffDistRadius = buffDistRadius;
        this.difficulty = difficulty;
    }

    public boolean isDebuff() {
        return isDebuff;
    }

    public void setDebuff(boolean debuff) {
        isDebuff = debuff;
    }

    public boolean isTickDecay() {
        return tickDecay;
    }

    public void setDecays(boolean tickDecay) {
        this.tickDecay = tickDecay;
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

        if (tickDecay) {
            age++;

            if (age >= maxAge) {
                remove();
                return;
            }
        }

        if (world.getTotalWorldTime() % 40 == 0) {
            //System.out.println("tick! " + origin);
        }
    }

    public void remove() {
        WorldDirector wd = WorldDirectorManager.instance().getCoroUtilWorldDirector(this.getWorld());
        if (wd != null) {
            wd.removeTickingLocation(this);
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
