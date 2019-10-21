package CoroUtil.world.grid.chunk;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;

public class PlayerDataGrid {

	//playerActivityTimeSpent might be difficult to track accurately.... we need to know the player was previously in the chunk to reliably accumulate the value
	//i guess just tick every active player every 20*10 ticks, and increment the value by 20*10 ticks, its not 100% accurate given they couldve switched chunks
	//but it should be accurate enough, use an interval value to rely on, set in here
	
	public static long playerTimeSpentUpdateInterval = 20*10;
	
	public long playerActivityInteraction = 0; //interaction count in chunks
	public long playerActivityLastUpdated = 0; //world time last tick happened (to track freshness of data)
	public long playerActivityTimeSpent = 0; //world time spent in chunk
	
	public PlayerDataGrid() {
		
	}
	
	public void nbtRead(CompoundNBT parNBT) {
		playerActivityInteraction = parNBT.getLong("playerActivityInteraction");
		playerActivityLastUpdated = parNBT.getLong("playerActivityLastUpdated");
		playerActivityTimeSpent = parNBT.getLong("playerActivityTimeSpent");
	}
	
	public CompoundNBT nbtWrite() {
		CompoundNBT nbt = new CompoundNBT();
		
		nbt.putLong("playerActivityInteraction", playerActivityInteraction);
		nbt.putLong("playerActivityLastUpdated", playerActivityLastUpdated);
		nbt.putLong("playerActivityTimeSpent", playerActivityTimeSpent);
		
		return nbt;
	}
	
}

