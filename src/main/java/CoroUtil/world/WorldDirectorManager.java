package CoroUtil.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import CoroUtil.forge.CoroUtil;
import CoroUtil.world.grid.block.BlockDataGrid;
import CoroUtil.world.grid.chunk.ChunkDataGrid;

public class WorldDirectorManager {

	//public PhysicsWorldManager physMan = new PhysicsWorldManager();
	private List<BlockDataGrid> listGridsBlocks = new ArrayList<>();
	private List<ChunkDataGrid> listGridsChunks = new ArrayList<>();
	private HashMap<Integer, BlockDataGrid> lookupGridsBlockData = new HashMap<>();
	private HashMap<Integer, ChunkDataGrid> lookupGridsChunkData = new HashMap<>();
	
	//modID<dimID, WorldDirector>
	private HashMap<String, HashMap<Integer, WorldDirector>> lookupWorldDirectors = new HashMap<String, HashMap<Integer, WorldDirector>>();
	private List<WorldDirector> listWorldDirectors = new ArrayList<WorldDirector>();
	
	private static WorldDirectorManager instanceServer;
	private static WorldDirectorManager instanceClient; //not setup yet
	
	public static WorldDirectorManager instance() {
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			if (instanceServer == null) {
				instanceServer = new WorldDirectorManager();
				//sided singleton with reset methods
			}
			return instanceServer;
		} else {
			return null; //WIP
		}
		
	}
	
	public WorldDirectorManager() {
		//data = new NBTTagCompound();
		reset();
	}
	
	public WorldDirector getCoroUtilWorldDirector(World world) {
		WorldDirector wd = getWorldDirector(CoroUtil.modID, world);
		if (wd == null) {
			wd = new WorldDirector(true);
			WorldDirectorManager.instance().registerWorldDirector(wd, CoroUtil.modID, world);
		}
		return wd;
	}
	
	public WorldDirector getWorldDirector(String parModID, World world) {
		int dimID = world.provider.getDimension();
		if (lookupWorldDirectors.containsKey(parModID)) {
			HashMap<Integer, WorldDirector> lookup = lookupWorldDirectors.get(parModID);
			if (lookup.containsKey(dimID)) {
				return lookup.get(dimID);
			}
			
		}/* else {
			WorldDirector worldDirector = null;//
			if (world.provider.getDimensionName().equals(EPOCHDIM_OVERWORLD_NAME)) {
				worldDirector = new WorldDirectorEpochOverworld(dimID);
			} else if (world.provider.getDimensionName().equals(EPOCHDIM_DUNGEON_NAME)) {
				worldDirector = new WorldDirectorEpochDungeon(dimID);
			} else if (dimID == 0) {
				worldDirector = new WorldDirectorVanillaOverworld(dimID);
			} else {
				worldDirector = new WorldDirector(dimID);
			}
			//new WorldDirector(dimID);
			addWorldDirector(worldDirector, world);
			return worldDirector;
		}*/
		
		return null;
	}
	
	/* registers and attempts to read in any data for it */
	public void registerWorldDirector(WorldDirector worldDirector, String parModID, World world) {
		int dimID = world.provider.getDimension();
		HashMap<Integer, WorldDirector> lookup = lookupWorldDirectors.get(parModID);
		if (lookup == null) {
			lookup = new HashMap<Integer, WorldDirector>();
			lookupWorldDirectors.put(parModID, lookup);
		}
		lookup.put(dimID, worldDirector);
		listWorldDirectors.add(worldDirector);
		
		worldDirector.initData(parModID, world);
		worldDirector.tryReadFromFile();
	}
	
	public void reset() {
		Iterator<WorldDirector> it = listWorldDirectors.iterator();
		while (it.hasNext()) {
			it.next().reset();
		}
		//needs better cleanup
		/*if (blockDataGrid != null) {
			blockDataGrid.grid.clear();
			blockDataGrid = null;
		}*/
		for (int i = 0; i < listGridsBlocks.size(); i++) {
			listGridsBlocks.get(i).grid.clear();
		}
		listGridsBlocks.clear();
		for (int i = 0; i < listGridsChunks.size(); i++) {
			listGridsChunks.get(i).grid.clear();
		}
		listGridsChunks.clear();
		lookupGridsBlockData.clear();
		lookupGridsChunkData.clear();
		//physMan.reset();
		
	}
	
	public void addGridBlockData(int parDimID, BlockDataGrid parGrid) {
		listGridsBlocks.add(parGrid);
		lookupGridsBlockData.put(parDimID, parGrid);
	}
	
	public BlockDataGrid getBlockDataGrid(World world) {
		if (lookupGridsBlockData.containsKey(world.provider.getDimension())) {
			return lookupGridsBlockData.get(world.provider.getDimension());
		} else {
			BlockDataGrid grid = new BlockDataGrid(world);
			grid.readFromFile();
			addGridBlockData(world.provider.getDimension(), grid);
			return grid;
		}
		/*if (blockDataGrid == null && DimensionManager.getWorld(RPGMod.epochDimID) != null) {
			blockDataGrid = new BlockDataGrid(DimensionManager.getWorld(RPGMod.epochDimID));
			blockDataGrid.readFromFile();
		}
		return blockDataGrid;*/
	}
	
	public void addGridChunkData(int parDimID, ChunkDataGrid parGrid) {
		listGridsChunks.add(parGrid);
		lookupGridsChunkData.put(parDimID, parGrid);
	}
	
	public ChunkDataGrid getChunkDataGrid(World world) {
		if (lookupGridsChunkData.containsKey(world.provider.getDimension())) {
			return lookupGridsChunkData.get(world.provider.getDimension());
		} else {
			ChunkDataGrid grid = new ChunkDataGrid(world);
			grid.readFromFile();
			addGridChunkData(world.provider.getDimension(), grid);
			return grid;
		}
	}
	
	public void onTick() {
		
		Iterator<WorldDirector> it = listWorldDirectors.iterator();
		while (it.hasNext()) {
			WorldDirector wd = it.next();
			//check if world is loaded
			if (DimensionManager.getWorld(wd.dimID) != null) {
				wd.tick();
			}
		}
		
		/*WorldServer[] worlds = DimensionManager.getWorlds();
    	for (int i = 0; i < worlds.length; i++) {
    		WorldServer world = worlds[i];
    		onTickWorld(world);
    	}*/
    	
    	//physMan.tickServer();
	}
	
	public void resetDynamicDimData() {
		
	}
	
	public void initDynamicDimData(World world) {
		initDynamicDimData(world.provider.getDimension());
	}
	
	
	public void initDynamicDimData(int dimID) {
		
	}
	
	public void writeToFile(boolean unloadInstances) {
    	try {
    		//World Directors
    		Iterator<WorldDirector> it = listWorldDirectors.iterator();
    		while (it.hasNext()) {
    			it.next().writeToFile(unloadInstances);
    		}
    		
    		//BlockDataGrids
    		for (int i = 0; i < listGridsBlocks.size(); i++) {
				listGridsBlocks.get(i).writeToFile(unloadInstances);
			}
    		
    		for (int i = 0; i < listGridsChunks.size(); i++) {
    			listGridsChunks.get(i).writeToFile(unloadInstances);
			}
	    	
	    	if (unloadInstances) {
	    		listGridsBlocks.clear();
	    		lookupWorldDirectors.clear();
	    		
	    		listGridsBlocks.clear();
	    		lookupGridsBlockData.clear();
	    		
	    		listGridsChunks.clear();
	    		lookupGridsChunkData.clear();
	    	}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
