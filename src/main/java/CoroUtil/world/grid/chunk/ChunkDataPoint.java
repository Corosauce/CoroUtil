package CoroUtil.world.grid.chunk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import CoroUtil.difficulty.DamageSourceEntry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.nbt.CompoundNBT;
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
	//if they can enter unloaded chunks, we must be able to tick the count of an unloaded chunk without loading mc chunk
	
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
    
    public Class enemyClass = MonsterEntity.class;
    
    //dynamic difficulty code
    public List<Float> listDPSAveragesShortTerm = new ArrayList<Float>();
    public List<Float> listDPSAveragesLongTerm = new ArrayList<Float>();
    public long lastDPSRecalc = 0;
    public float averageDPS;

    boolean useSpawnableType = false;

    public DamageSourceEntry highestDamage;

    public ChunkDataPoint(ChunkDataGrid parGrid, int i, int k)
    {
    	grid = parGrid;
        xCoord = i;
        yCoord = 0;
        zCoord = k;
        highestDamage = new DamageSourceEntry();
        hash = makeHash(i, k);
        updateCache();
    }
    
    public void initFirstTime() {
    	try {
	    	updateCache();
	    	
	    	//if (grid.world.checkChunksExist(xCoord * 16, 0, zCoord * 16, xCoord * 16, 0, zCoord * 16)) {
			if (useSpawnableType) {
				if (grid.world.isBlockLoaded(new BlockPos(xCoord * 16, 0, zCoord * 16), true)) {
					Chunk chunk = grid.world.getChunkFromChunkCoords(xCoord, zCoord);

					int countWater = 0;
					int countLand = 0;

					//this should get threaded?
					//just do a basic scan for majority of chunk being land (even land consider too?)
					for (int x = 0; x < 16; x++) {
						for (int z = 0; z < 16; z++) {
							int heightVal = Math.max(0, chunk.getHeightValue(x, z) - 1);

							if (heightVal >= 0) {
								BlockState state = chunk.getBlockState(new BlockPos(x, heightVal, z));
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
    
    public void read(CompoundNBT nbt) {
    	lastTickTime = nbt.getLong("lastTickTime");
    	spawnableType = nbt.getInt("spawnableType");
    	countEntitiesEnemy = nbt.getInt("countEntitiesEnemy");
    	CompoundNBT nbtListPlayers = nbt.getCompound("listPlayers");
    	Iterator it = nbtListPlayers.keySet().iterator();
    	while (it.hasNext()) {
    		String entryName = (String) it.next();
    		CompoundNBT entry = nbtListPlayers.getCompound(entryName);
    		UUID uuid = UUID.fromString(entry.getString("UUID"));
    		PlayerDataGrid playerData = new PlayerDataGrid();
    		playerData.nbtRead(entry.getCompound("playerData"));
    		//long activityVal = entry.getLong("activityVal");
    		
    		lookupPlayersToActivity.put(uuid, playerData);
    	}
    	
    	playerDataForAll.nbtRead(nbt.getCompound("playerDataForAll"));
    	
    	lastDPSRecalc = nbt.getLong("lastDPSRecalc");
    	averageDPS = nbt.getFloat("averageDPS");
    	
    	
    	CompoundNBT nbtDPSs = nbt.getCompound("listDPSAveragesLongTerm");
    	it = nbtDPSs.keySet().iterator();
    	while (it.hasNext()) {
    		String entryName = (String) it.next();
    		
    		listDPSAveragesLongTerm.add(nbtDPSs.getFloat(entryName));
    	}

    	if (nbt.contains("highestDamage")) {
			CompoundNBT highestDamageNBT = nbt.getCompound("highestDamage");
			highestDamage = new DamageSourceEntry();
			highestDamage.source_entity_true = highestDamageNBT.getString("source_entity_true");
			highestDamage.source_entity_immediate = highestDamageNBT.getString("source_entity_immediate");
			highestDamage.target_entity = highestDamageNBT.getString("target_entity");
			highestDamage.source_type = highestDamageNBT.getString("source_type");
			highestDamage.highestDamage = highestDamageNBT.getFloat("highestDamage");
            highestDamage.damageTimeAveraged = highestDamageNBT.getFloat("damageTimeAveraged");
			highestDamage.lastLogTime = highestDamageNBT.getLong("lastLogTime");
            highestDamage.timeDiffSeconds = highestDamageNBT.getFloat("timeDiffSeconds");
			highestDamage.source_pos = new BlockPos(highestDamageNBT.getInt("x"), highestDamageNBT.getInt("y"), highestDamageNBT.getInt("z"));
		}

    	
    	int test = 0;
    	/*xCoord = nbt.getInt("xCoord");
    	yCoord = nbt.getInt("yCoord");  -- read in from init
    	zCoord = nbt.getInt("zCoord");*/
    }
    
    public CompoundNBT write() {
    	CompoundNBT nbt = new CompoundNBT();
    	
    	nbt.putLong("lastTickTime", lastTickTime);
    	nbt.putInt("spawnableType", spawnableType);
    	nbt.putInt("countEntitiesEnemy", countEntitiesEnemy);
    	
    	CompoundNBT nbtListPlayers = new CompoundNBT();
    	int count = 0;
    	for (Map.Entry<UUID, PlayerDataGrid> entry : lookupPlayersToActivity.entrySet()) {
    		CompoundNBT nbtEntry = new CompoundNBT();
    		nbtEntry.putString("UUID", entry.getKey().toString());
    		nbtEntry.put("playerData", entry.get().nbtWrite());
    		//nbtEntry.putLong("activityVal", entry.get());
    		
    		//a taglist might be more efficient on space....
    		nbtListPlayers.put("entry_" + count++, nbtEntry);
    	}
    	nbt.put("listPlayers", nbtListPlayers);
    	
    	nbt.put("playerDataForAll", playerDataForAll.nbtWrite());
    	
    	nbt.putInt("xCoord", xCoord);
    	nbt.putInt("yCoord", yCoord);
    	nbt.putInt("zCoord", zCoord);
    	
    	//System.out.println("countEntitiesEnemy: " + countEntitiesEnemy + " - " + xCoord + ", " + zCoord);
    	
    	nbt.putLong("lastDPSRecalc", lastDPSRecalc);
    	nbt.putFloat("averageDPS", averageDPS);
    	CompoundNBT nbtDPSs = new CompoundNBT();
    	for (int i = 0; i < listDPSAveragesLongTerm.size(); i++) {
    		nbtDPSs.putFloat("entry_" + i, listDPSAveragesLongTerm.get(i));
    	}
    	nbt.put("listDPSAveragesLongTerm", nbtDPSs);

    	CompoundNBT highestDamageNBT = new CompoundNBT();
		highestDamageNBT.putString("source_entity_true", highestDamage.source_entity_true);
		highestDamageNBT.putString("source_entity_immediate", highestDamage.source_entity_immediate);
		highestDamageNBT.putString("target_entity", highestDamage.target_entity);
		highestDamageNBT.putString("source_type", highestDamage.source_type);
		highestDamageNBT.putFloat("highestDamage", highestDamage.highestDamage);
        highestDamageNBT.putFloat("damageTimeAveraged", highestDamage.damageTimeAveraged);
        highestDamageNBT.putFloat("timeDiffSeconds", highestDamage.timeDiffSeconds);
		highestDamageNBT.putLong("lastLogTime", highestDamage.lastLogTime);
		highestDamageNBT.putInt("x", highestDamage.source_pos.getX());
		highestDamageNBT.putInt("y", highestDamage.source_pos.getY());
		highestDamageNBT.putInt("z", highestDamage.source_pos.getZ());

    	nbt.put("highestDamage", highestDamageNBT);
    	
    	return nbt;
    }
    
    public void tickUpdate() {
    	long curTickTime = grid.world.getGameTime();
    	
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
    	playerDataForAll.playerActivityLastUpdated = grid.world.getGameTime();
    	if (uuid != null) {
    		getPlayerData(uuid).playerActivityTimeSpent += parVal;
    		getPlayerData(uuid).playerActivityLastUpdated = grid.world.getGameTime();
    		//System.out.println("setting player activity time value for chunk " + xCoord + " - " + zCoord + " to " + lookupPlayersToActivity.get(uuid).playerActivityTimeSpent);
    	}
    }
    
    public void addToPlayerActivityInteract(UUID uuid, long parVal) {
    	playerDataForAll.playerActivityInteraction += parVal;
    	playerDataForAll.playerActivityLastUpdated = grid.world.getGameTime();
    	if (uuid != null) {
    		getPlayerData(uuid).playerActivityInteraction += parVal;
    		getPlayerData(uuid).playerActivityLastUpdated = grid.world.getGameTime();
    		//System.out.println("setting player activity interaction value for chunk " + xCoord + " - " + zCoord + " to " + lookupPlayersToActivity.get(uuid).playerActivityInteraction);
    	}
    	
    }
}
