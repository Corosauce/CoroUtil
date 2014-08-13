package build;

import net.minecraft.nbt.NBTTagCompound;
import build.world.Build;

public interface SchematicData {
	abstract void readFromNBT(NBTTagCompound par1NBTTagCompound, Build build);
	abstract void writeToNBT(NBTTagCompound par1NBTTagCompound, Build build);
}
