package CoroUtil.packet;

import net.minecraft.nbt.NBTTagCompound;

public class NBTDataManager {

	//implement a registry to have better sharing, for once these are used for more than just 1 feature/gui
	
	public static NBTTagCompound nbtDataClient;
	public static NBTTagCompound nbtDataServer;
	
	public static void nbtDataFromServer(NBTTagCompound parNBT) {
		
	}
	
	public static void nbtDataFromClient(String parUser, NBTTagCompound parNBT) {
		
	}
	
}
