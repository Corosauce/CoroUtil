package CoroUtil.playerdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Old design, just use entity.getEntityData() nbt
 * 
 * @author Corosus
 *
 */

@Deprecated
public class PlayerDataObject {

	public String username;
	public HashMap<String, IPlayerData> playerData = new HashMap<String, IPlayerData>();
	public HashMap<IPlayerData, String> playerDataReverseLookup = new HashMap<IPlayerData, String>();
	public ArrayList tickList = new ArrayList();
	
	public PlayerDataObject(String parUsername) {
		username = parUsername;
	}
	
	public void addObject(String name, IPlayerData ipd) {
		playerData.put(name, ipd);
		playerDataReverseLookup.put(ipd, name);
	}
	
	public IPlayerData get(String objectName) {
		return playerData.get(objectName);
	}
	
	public void nbtLoadAll(NBTTagCompound parNBT) {
		
		Iterator it = playerData.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			
			String entryModule = (String)pairs.getKey();
			IPlayerData pdo = (IPlayerData)pairs.getValue();

			pdo.init(username);
			
			NBTTagCompound nbt = parNBT.getCompoundTag(entryModule);
			if (nbt != null) {
				pdo.nbtLoad(nbt);
			} else {
				System.out.println("fresh!");
			}
		}
	}
	
	public void nbtSyncAll(NBTTagCompound parNBT) {
		NBTTagCompound data = new NBTTagCompound();
		
		Iterator it = playerData.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			
			String entryModule = (String)pairs.getKey();
			IPlayerData pdo = (IPlayerData)pairs.getValue();
			
			NBTTagCompound nbt = data.getCompoundTag(entryModule);
			if (nbt != null) {
				pdo.nbtSyncFromServer(nbt);
			}
		}
	}
	
}
