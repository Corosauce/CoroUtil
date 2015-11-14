package CoroUtil.util;

import net.minecraft.nbt.NBTTagCompound;

public interface ISerializableNBT {

	public void readFromNBT(NBTTagCompound nbt);
	
	public NBTTagCompound writeToNBT(NBTTagCompound nbt);
	
}
