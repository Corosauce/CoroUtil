package CoroUtil.world.grid.chunk;

import net.minecraft.nbt.NBTTagCompound;

public class PlayerDataGrid {

	//playerActivityTimeSpent might be difficult to track accurately.... we need to know the player was previously in the chunk to reliably accumulate the value
	//i guess just tick every active player every 20*10 ticks, and increment the value by 20*10 ticks, its not 100% accurate given they couldve switched chunks
	//but it should be accurate enough, use an interval value to rely on, set in here
	
	public static long playerTimeSpentUpdateInterval = 20*10;
	
	public long playerActivityInteraction = 0; //interaction count in chunks
	public long playerActivityLastUpdated = 0; //world time last update happened (to track freshness of data)
	public long playerActivityTimeSpent = 0; //world time spent in chunk
	
	public PlayerDataGrid() {
		
	}
	
	public void nbtRead(NBTTagCompound parNBT) {
		playerActivityInteraction = parNBT.getLong("playerActivityInteraction");
		playerActivityLastUpdated = parNBT.getLong("playerActivityLastUpdated");
		playerActivityTimeSpent = parNBT.getLong("playerActivityTimeSpent");
	}
	
	public NBTTagCompound nbtWrite() {
		NBTTagCompound nbt = new NBTTagCompound();
		
		nbt.setLong("playerActivityInteraction", playerActivityInteraction);
		nbt.setLong("playerActivityLastUpdated", playerActivityLastUpdated);
		nbt.setLong("playerActivityTimeSpent", playerActivityTimeSpent);
		
		return nbt;
	}
	
}
