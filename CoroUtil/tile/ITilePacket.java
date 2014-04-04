package CoroUtil.tile;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;

/* Handles advanced packet systems for tile entities, tile entity is responsible for initing/ticking the tilehandler */
/* PKT - SRV->CL: onDataPacket - Forge packet method, can send NBT, uses getDescriptionPacket override to create the packet
 * NBT - CL->SRV: handleClientSentNBT - CoroAI method (needs sanitizing)
 * TDW - SRV->CL: handleServerSentDataWatcherList - CoroAI method
 * TDW - CL->SRV: handleClientSentDataWatcherList - CoroAI method (needs sanitizing) - if implemented proper, will propegate the updated values back to all clients in dimension including the client that set it (a good thing given sanitizing)
 * 
 * 
 * 
 * All methods inner function should be optional, some tiles will only use the TileDataWatcher, some will only use NBT, some both, depends on the needs 
 * 
 * Use PacketHelper for creating the packets
 * */
public interface ITilePacket {

	public TileHandler getTileHandler();
	public void handleClientSentNBT(String parUsername, NBTTagCompound par1NBTTagCompound);
    public void handleServerSentDataWatcherList(List parList);
    public void handleClientSentDataWatcherList(String parUsername, List parList);
	
}
