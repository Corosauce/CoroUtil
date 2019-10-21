package CoroUtil.packet;

import net.minecraft.nbt.CompoundNBT;

public interface INBTPacketHandler {

	public void nbtDataFromServer(CompoundNBT nbt);
	public void nbtDataFromClient(String parUser, CompoundNBT nbt);
	
}

