package CoroUtil.capability;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class WorldDataInstance {

    private World world;

    public WorldDataInstance() {

    }

    public World getWorld() {
        return world;
    }

    public WorldDataInstance setWorld(World world) {
        this.world = world;
        return this;
    }

    public void readNBT(NBTTagCompound nbt) {

    }
    
    public void writeNBT(NBTTagCompound nbt) {

    }

    public void tick() {

    }
}
