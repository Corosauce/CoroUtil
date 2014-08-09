package CoroUtil.entity;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
/*
 * Use PacketHelper for creating the packets
 * */
public interface IEntityPacket {

	public void handleNBTFromClient(NBTTagCompound par1NBTTagCompound);
	public void handleNBTFromServer(NBTTagCompound par1NBTTagCompound);
	
}
