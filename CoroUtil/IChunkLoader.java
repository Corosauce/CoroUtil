package CoroUtil;

import net.minecraftforge.common.ForgeChunkManager.Ticket;

public interface IChunkLoader {
	
	public void setChunkTicket(Ticket parTicket);
	
	public void forceChunkLoading(int chunkX, int chunkZ);

}
