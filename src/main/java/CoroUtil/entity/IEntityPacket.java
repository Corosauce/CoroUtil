package CoroUtil.entity;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;
/*
 * Use PacketHelper for creating the packets
 * */
public interface IEntityPacket {

	public void handleNBTFromClient(CompoundNBT par1NBTTagCompound);
	public void handleNBTFromServer(CompoundNBT par1NBTTagCompound);
	
}
