package CoroUtil.packet;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;

public class NBTDataManager {

	//implement a registry to have better sharing, for once these are used for more than just 1 feature/gui
	
	public static CompoundNBT nbtDataClient;
	public static CompoundNBT nbtDataServer;
	
	public static void nbtDataFromServer(CompoundNBT parNBT) {
		
	}
	
	public static void nbtDataFromClient(String parUser, CompoundNBT parNBT) {
		
	}
	
}

