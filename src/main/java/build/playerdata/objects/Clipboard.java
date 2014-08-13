package build.playerdata.objects;

import net.minecraft.nbt.NBTTagCompound;
import build.playerdata.IPlayerData;
import build.world.Build;

public class Clipboard implements IPlayerData {

	public Build clipboardData;
	
	public Clipboard() {
		clipboardData = new Build(0, 0, 0, "blank", true);
		clipboardData.newFormat = true;
	}
	
	@Override
	public void nbtLoad(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void nbtSave(NBTTagCompound nbt) {
		// TODO Auto-generated method stub
		
	}

}
