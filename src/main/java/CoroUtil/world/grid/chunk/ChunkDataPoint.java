package CoroUtil.world.grid.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.Chunk;

public class ChunkDataPoint
{
	
	//main issue: accurate tracking of entity count
	
	//solution 1:
	//- use death event, and chunk entered/leave events to maintain the counts
	//- chunk unload events? they probably shouldnt factor in... as the data shouldnt require any loaded state (other than for updating the count data)
	
	//if chunk isnt loaded, the data is read only
	
	//can entities enter unloaded chunks? or does the chunk have to unload to unload the entity?
	//if they can enter unloaded chunks, we must be able to update the count of an unloaded chunk without loading mc chunk
	
	public ChunkDataGrid grid;
	
	//Mandatory data
    public int xCoord;
    public int yCoord; //to satisfy a chunkcoords (?)
    public int zCoord;
    public final int hash;

    //Feature specific data, cached on first creation
    public int spawnableType = -1; //-1 = cant spawn, 0 = land, 1 = water?, 2 = ???
    
    //runtime instance data
    public long lastTickTime; //uses gettotalworldtime
    public int countEntitiesEnemy = -1;
    //activityValue per player UUID
    private HashMap<UUID, PlayerDataGrid> lookupPlayersToActivity = new HashMap<UUID, PlayerDataGrid>();
    public PlayerDataGrid playerDataForAll = new PlayerDataGrid();
    //public long playerActivityTotal = 0;
    
    public Class enemyClass = EntityMob.class;
    
    //dynamic difficulty code
    public List<Float> listDPSAveragesShortTerm = new ArrayList<Float>();
    public List<Float> listDPSAveragesLongTerm = new ArrayList<Float>();
    public long lastDPSRecalc = 0;
    public float averageDPS;

    public ChunkDataPoint(ChunkDataGrid parGrid, int i, int k)
    {
    	grid = parGrid;
        xCoord = i;
        yCoord = 0;
        zCoord = k;
        hash = makeHash(i, k);
        updateCache();
    }
    
    public void initFirstTime() {
    	try {
	    	updateCache();
	    	
	    	//if (grid.world.checkChunksExist(xCoord * 16, 0, zCoord * 16, xCoord * 16, 0, zCoord * 16)) {
	    	if (grid.world.isBlockLoaded(new BlockPos(xCoord * 16, 0, zCoord * 16), true)) {
		    	Chunk chunk = grid.world.getChunkFromChunkCoords(xCoord, zCoord);
		    	
		    	int countWater = 0;
		    	int countLand = 0;
		    	
		    	//this should get threaded?
		    	//just do a basic scan for majority of chunk being land (even land consider too?)
		    	for (int x = 0; x < 16; x++) {
		    		for (int z = 0; z < 16; z++) {
		    			int heightVal = Math.max(0, chunk.getHeightValue(x, z)-1);
		    			
		    			if (heightVal >= 0) {
		    				IBlockState state = chunk.getBlockState(new BlockPos(x, heightVal, z));
			    			Block id = state.getBlock();
			    			
			    			if (id.getMaterial(state).isLiquid()) {
		    					countWater++;
		    				} else {
		    					countLand++;
		    				}
		    			}
		    		}
		    	}
	
				//System.out.println("ChunkDataPoint, countWater: " + countWater + ", countLand: " + countLand);
		    	
				if (countLand > countWater) {
					spawnableType = 0;
					//System.out.println("set to land");
				} else {
					spawnableType = 1;
					//System.out.println("set to water");
				}
	    	} else {
	    		//System.out.println("chunk doesnt exist");
	    	}
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

    public void updateCache()
    {
    	//if (grid.world.checkChunksExist(xCoord * 16, 0, zCoord * 16, xCoord * 16, 0, zCoord * 16)) {
    	if (grid.world.isBlockLoaded(new BlockPos(xCoord * 16, 0, zCoord * 16), true)) {
    		Chunk chunk = grid.world.getChunkFromChunkCoords(xCoord, zCoord);
    		List<Entity> listEntities = getEntitiesFromLoadedChunk(enemyClass, chunk);
    		countEntitiesEnemy = listEntities.size();
    		//System.out.println("Enemies count: " + countEntitiesEnemy);
    	} else {
    		//System.out.println("chunk doesnt exist");
    	}
    	
    }
    
    public boolean isRemovable() {
    	//I dont think we want to ever remove chunk based grid points
    	return false;
    }

    public static int makeHash(int i, int k)
    {
    	int j = 0;
        return j & 0xff | (i & 0x7fff) << 8 | (k & 0x7fff) << 24 | (i >= 0 ? 0 : 0x80000000) | (k >= 0 ? 0 : 0x8000);
    }

    public float distanceTo(ChunkDataPoint pathpoint)
    {
        float f = pathpoint.xCoord - xCoord;
        float f1 = pathpoint.yCoord - yCoord;
        float f2 = pathpoint.zCoord - zCoord;
        return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof ChunkDataPoint)
        {
            ChunkDataPoint pathpoint = (ChunkDataPoint)obj;
            return hash == pathpoint.hash && xCoord == pathpoint.xCoord && yCoord == pathpoint.yCoord && zCoord == pathpoint.zCoord;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return hash;
    }

    public String toString()
    {
        return (new StringBuilder()).append(xCoord).append(", ").append(yCoord).append(", ").append(zCoord).toString();
    }
    
    public void readFromNBT(NBTTagCompound nbt) {
    	lastTickTime = nbt.getLong("lastTickTime");
    	spawnableType = nbt.getInteger("spawnableType");
    	countEntitiesEnemy = nbt.getInteger("countEntitiesEnemy");
    	NBTTagCompound nbtListPlayers = nbt.getCompoundTag("listPlayers");
    	Iterator it = nbtListPlayers.getKeySet().iterator();
    	while (it.hasNext()) {
    		String entryName = (String) it.next();
    		NBTTagCompound entry = nbtListPlayers.getCompoundTag(entryName);
    		UUID uuid = UUID.fromString(entry.getString("UUID"));
    		PlayerDataGrid playerData = new PlayerDataGrid();
    		playerData.nbtRead(entry.getCompoundTag("playerData"));
    		//long activityVal = entry.getLong("activityVal");
    		
    		lookupPlayersToActivity.put(uuid, playerData);
    	}
    	
    	playerDataForAll.nbtRead(nbt.getCompoundTag("playerDataForAll"));
    	
    	lastDPSRecalc = nbt.getLong("lastDPSRecalc");
    	averageDPS = nbt.getFloat("averageDPS");
    	
    	
    	NBTTagCompound nbtDPSs = nbt.getCompoundTag("listDPSAveragesLongTerm");
    	it = nbtDPSs.getKeySet().iterator();
    	while (it.hasNext()) {
    		String entryName = (String) it.next();
    		
    		listDPSAveragesLongTerm.add(nbtDPSs.getFloat(entryName));
    	}
    	
    	int test = 0;
    	/*xCoord = nbt.getInteger("xCoord");
    	yCoord = nbt.getInteger("yCoord");  -- read in from init
    	zCoord = nbt.getInteger("zCoord");*/
    }
    
    public NBTTagCompound writeToNBT() {
    	NBTTagCompound nbt = new NBTTagCompound();
    	
    	nbt.setLong("lastTickTime", lastTickTime);
    	nbt.setInteger("spawnableType", spawnableType);
    	nbt.setInteger("countEntitiesEnemy", countEntitiesEnemy);
    	
    	NBTTagCompound nbtListPlayers = new NBTTagCompound();
    	int count = 0;
    	for (Map.Entry<UUID, PlayerDataGrid> entry : lookupPlayersToActivity.entrySet()) {
    		NBTTagCompound nbtEntry = new NBTTagCompound();
    		nbtEntry.setString("UUID", entry.getKey().toString());
    		nbtEntry.setTag("playerData", entry.getValue().nbtWrite());
    		//nbtEntry.setLong("activityVal", entry.getValue());
    		
    		//a taglist might be more efficient on space....
    		nbtListPlayers.setTag("entry_" + count++, nbtEntry);
    	}
    	nbt.setTag("listPlayers", nbtListPlayers);
    	
    	nbt.setTag("playerDataForAll", playerDataForAll.nbtWrite());
    	
    	nbt.setInteger("xCoord", xCoord);
    	nbt.setInteger("yCoord", yCoord);
    	nbt.setInteger("zCoord", zCoord);
    	
    	//System.out.println("countEntitiesEnemy: " + countEntitiesEnemy + " - " + xCoord + ", " + zCoord);
    	
    	nbt.setLong("lastDPSRecalc", lastDPSRecalc);
    	nbt.setFloat("averageDPS", averageDPS);
    	NBTTagCompound nbtDPSs = new NBTTagCompound();
    	for (int i = 0; i < listDPSAveragesLongTerm.size(); i++) {
    		nbtDPSs.setFloat("entry_" + i, listDPSAveragesLongTerm.get(i));
    	}
    	nbt.setTag("listDPSAveragesLongTerm", nbtDPSs);
    	
    	return nbt;
    }
    
    public void tickUpdate() {
    	long curTickTime = grid.world.getTotalWorldTime();
    	
    	//code that scales based on ticktime diff goes here
    	
    	lastTickTime = curTickTime;
    }
    
    public void cleanup() {
    	grid = null;
    }
    
    //TODO: fix for 1.8
    public List<Entity> getEntitiesFromLoadedChunk(Class filterClass, Chunk chunk) {
    	
    	List list = new ArrayList<Entity>();
    	/*for (int k = 0; k < chunk.getEntityLists().length; ++k)
        {
            ClassInheritanceMultiMap<Entity> list1 = chunk.getEntityLists()[k];

            for (int l = 0; l < list1.size(); ++l)
            {
                Entity entity = (Entity)list1.get(l);

                if (filterClass.isAssignableFrom(entity.getClass()))
                {
                    list.add(entity);
                }
            }
        }*/
    	return list;
    }
    
    public PlayerDataGrid getPlayerData(UUID uuid) {
    	PlayerDataGrid playerData = lookupPlayersToActivity.get(uuid);
    	if (playerData == null) {
    		playerData = new PlayerDataGrid();
    		lookupPlayersToActivity.put(uuid, playerData);
    	}
    	return playerData;
    }
    
    public void addToPlayerActivityTime(UUID uuid, long parVal) {
    	playerDataForAll.playerActivityTimeSpent += parVal;
    	playerDataForAll.playerActivityLastUpdated = grid.world.getTotalWorldTime();
    	if (uuid != null) {
    		getPlayerData(uuid).playerActivityTimeSpent += parVal;
    		getPlayerData(uuid).playerActivityLastUpdated = grid.world.getTotalWorldTime();
    		//System.out.println("setting player activity time value for chunk " + xCoord + " - " + zCoord + " to " + lookupPlayersToActivity.get(uuid).playerActivityTimeSpent);
    	}
    }
    
    public void addToPlayerActivityInteract(UUID uuid, long parVal) {
    	playerDataForAll.playerActivityInteraction += parVal;
    	playerDataForAll.playerActivityLastUpdated = grid.world.getTotalWorldTime();
    	if (uuid != null) {
    		getPlayerData(uuid).playerActivityInteraction += parVal;
    		getPlayerData(uuid).playerActivityLastUpdated = grid.world.getTotalWorldTime();
    		//System.out.println("setting player activity interaction value for chunk " + xCoord + " - " + zCoord + " to " + lookupPlayersToActivity.get(uuid).playerActivityInteraction);
    	}
    	
    }
}
