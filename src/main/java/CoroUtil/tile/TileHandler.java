package CoroUtil.tile;

import java.util.HashMap;
import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.FMLCommonHandler;
import CoroUtil.packet.PacketHelper;

public class TileHandler {

	//recode if needed, since DataWatcher was phased out
	
	//handles watching the tile entity (server side only? nope!)
	
	//since tiles are getting datawatchers, and using ids for the variable tracked sucks, using helper methods or code changes do:
	//- getDW("reloadTime"); -- this go from hashmap string to datawatcher lookup
	//- setDW("reloadTime", this.reload--); -- same thing but then updates the object
	
	//should it be a component added to tile entity, or extended class?
	//lets try component, just make the object, and slap in a bunch of strings and the variable type they should be
	//the internals should set it up to datawatcher ids and their types required
	//keep in mind, datawatcher inits with real data sometimes, this will init with blank data of that required type
	
	//code it will need to rewire
	
	//transform Packet40EntityMetadata into a packet channel
	
	/* server sending data to client
	 * 
	 * DataWatcher datawatcher = this.myEntity.getDataWatcher();

            if (datawatcher.hasChanges())
            {
                this.sendPacketToAllAssociatedPlayers(new Packet40EntityMetadata(this.myEntity.entityId, datawatcher, false));
            }
            
            
            
            public void handleEntityMetadata(Packet40EntityMetadata par1Packet40EntityMetadata)
    {
        Entity entity = this.getEntityByID(par1Packet40EntityMetadata.entityId);

        if (entity != null && par1Packet40EntityMetadata.getMetadata() != null)
        {
            entity.getDataWatcher().updateWatchedObjectsFromList(par1Packet40EntityMetadata.getMetadata());
        }
    }
	 
	 
	 */

	/*public TileEntity tEnt;
	public TileDataWatcher tileDataWatcher;
	
	public HashMap<String, Integer> mapNameToID;
	public int indexIDStart = 0;
	public int indexIDMax = 31;
	
	public TileHandler(TileEntity parTEnt) {
		tEnt = parTEnt;
		tileDataWatcher = new TileDataWatcher();
		mapNameToID = new HashMap<String, Integer>();
	}
	
	public void tickUpdate() {
		if (tileDataWatcher.hasChanges()) {
			//System.out.println("server side detects changes! packets go go go!");
			FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendPacketToAllPlayersInDimension(PacketHelper.createPacketForTEntDWServer(tEnt), tEnt.getWorld().provider.getDimension());
		}
	}
	
	public void addObject(String name, Object defaultType) {
		if (indexIDStart <= indexIDMax) {
			if (!mapNameToID.containsKey(name)) {
				tileDataWatcher.addObject(indexIDStart, defaultType);
				mapNameToID.put(name, indexIDStart);
				indexIDStart++;
			} else {
				System.out.println("TileDataWatcher SERIOUS ERROR: Object with name " + name + " already exists");
			}
		} else {
			System.out.println("TileDataWatcher SERIOUS ERROR: Hit max number of entries (32)");
		}
	}
	
	public void updateObject(String name, Object newValue) {
		tileDataWatcher.updateObject(mapNameToID.get(name), newValue);
	}
	
	public Object getObject(String name) {
		return tileDataWatcher.getWatchedObject(mapNameToID.get(name)).getObject();
	}
	
	public void handleServerSentDataWatcherList(List parList) {
		//System.out.println(this + " client received DW data, updating");
		tileDataWatcher.updateWatchedObjectsFromList(parList);
	}*/
}
