package CoroUtil.playerdata;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompoundNBT;

/**
 * Old design, just use entity.getEntityData() nbt
 * 
 * @author Corosus
 *
 */

@Deprecated
public interface IPlayerData {

	public void init(String parUsername);
	
	public void nbtLoad(CompoundNBT nbt);
	public CompoundNBT nbtSave();
	
	public void nbtSyncFromServer(CompoundNBT nbt);
	public void nbtCommandFromClient(CompoundNBT nbt);
	
	public void tick();
	
}

