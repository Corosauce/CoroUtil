package CoroUtil.packet;

import net.minecraft.nbt.NBTTagCompound;

public interface INBTPacketHandler {

	public void nbtDataFromServer(NBTTagCompound nbt);
	public void nbtDataFromClient(String parUser, NBTTagCompound nbt);
	
}
