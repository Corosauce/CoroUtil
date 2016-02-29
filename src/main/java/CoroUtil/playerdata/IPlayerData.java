package CoroUtil.playerdata;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Old design, just use entity.getEntityData() nbt
 * 
 * @author Corosus
 *
 */

@Deprecated
public interface IPlayerData {

	public void init(String parUsername);
	
	public void nbtLoad(NBTTagCompound nbt);
	public NBTTagCompound nbtSave();
	
	public void nbtSyncFromServer(NBTTagCompound nbt);
	public void nbtCommandFromClient(NBTTagCompound nbt);
	
	public void tick();
	
}
