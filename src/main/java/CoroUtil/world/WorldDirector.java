package CoroUtil.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import CoroUtil.config.ConfigCoroUtilAdvanced;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.event.WorldEvent;
import CoroUtil.pathfinding.PathPointEx;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.CoroUtilFile;
import CoroUtil.world.grid.chunk.ChunkDataPoint;
import CoroUtil.world.grid.chunk.PlayerDataGrid;
import CoroUtil.world.location.ISimulationTickable;

public class WorldDirector implements Runnable {

	//For now only server side
	
	public int dimID = -1;
	public String modID = "modID";
	public String type = "default"; //for multiple world directors per mod per dimension (just in case), not actually supported atm, id need another hashmap layer...
	//private World world;
	
	public int cachedTopBlockHome = -1;
	private NBTTagCompound extraData = new NBTTagCompound(); //this worlds extra data, excluding the read in non nbt stuff
	
	//Not serialized for now
	public List<WorldEvent> worldEvents = new ArrayList<WorldEvent>();
	
	//TODO: consider locationless ticking simulations
	public ConcurrentHashMap<Integer, ISimulationTickable> lookupTickingManagedLocations;
	
	public ConcurrentLinkedQueue<ISimulationTickable> listTickingLocations;
	
	//server side only thread
	public boolean useThreading = false;
	public Thread threadedDirector = null;
	private boolean threadRunning = false;
	/**
	 * TODO: implement advanced scheduling system that lets delays happen on a per simulation level
	 * - possible way to do is a cyclical linked list where each entry is an interval of '1', and entries(list of entries in entry) are spaced out based on their desired delays
	 * -- size of list is determined by entry with largest delay
	 * 
	 * for now we use a 1 second delay for all simulations and internally they do additional delay on top of that
	 */
	
	public int threadSleepRate = 50 * 20;
	public boolean threadServerSideOnly = true;
	
	/**
	 * For sharing and maintaining an update queue limiter between simulations that do the same thing
	 * eg: 2 trees that need lots of updates, between both of them they can only do a max of x amount of block updates
	 * 
	 * for future consider an elegant way to spread out these updates between all that need updates, factoring in a few weighted priorities like proximity to player
	 */
	public HashMap<String, Integer> lookupNameToUpdatesPerTickCur = new HashMap<String, Integer>();
	
	//in future think of a more proper registration way that wont make things conflict and redundantly add per instance so much
	public HashMap<String, Integer> lookupNameToUpdatesPerTickLimit = new HashMap<String, Integer>();
	
	//reflection made
	public WorldDirector(boolean runThread) {
		this();
		this.useThreading = runThread;
	}
	
	public WorldDirector() {
		lookupTickingManagedLocations = new ConcurrentHashMap<>();
		listTickingLocations = new ConcurrentLinkedQueue<>();
	}
	
	public void initAndStartThread() {
		if (!threadRunning) {
			threadRunning = true;
			threadedDirector = new Thread(this, "World Simulation Thread");
			threadedDirector.start();
		} else {
			System.out.println("tried to start thread when already running for CoroUtil world director");
		}
	}
	
	public void stopThread() {
		threadRunning = false;
	}
	
	//required for reading in, etc
	public void initData(String parModID, World parWorld) {
		dimID = parWorld.provider.getDimension();
		modID = parModID;
		
		if (useThreading) {
			if (!parWorld.isRemote || !threadServerSideOnly) {
				initAndStartThread();
			}
		}
	}
	
	/*public void init(String parModID, int parDimID, String parType) {
		init(parModID, parDimID);
		type = parType;
	}*/
	
	public World getWorld() {
		//if (world == null) {
			World world = DimensionManager.getWorld(dimID);
		//}
		return world;
	}
	
	public NBTTagCompound getExtraData() {
		return extraData;
	}
	
	public void reset() {
		extraData = new NBTTagCompound();
		cachedTopBlockHome = -1;
		worldEvents.clear();
	}
	
	public void addEvent(WorldEvent event) {
		worldEvents.add(event);
		event.init();
	}
	
	public void addTickingLocation(ISimulationTickable location) {
		addTickingLocation(location, true);
	}
	
	public void addTickingLocation(ISimulationTickable location, boolean init) {
		//if (lookupDungeonEntrances == null) lookupDungeonEntrances = new HashMap<Integer, DungeonEntrance>();
		if (location.getOrigin() != null) {
			Integer hash = PathPointEx.makeHash(location.getOrigin().posX, location.getOrigin().posY, location.getOrigin().posZ);
			if (!lookupTickingManagedLocations.containsKey(hash)) {
				lookupTickingManagedLocations.put(hash, location);
				//relocated to a ticking first time init so it can be after readnbt
				//if (init) location.init();
			} else {
				System.out.println("warning: location already exists at these coords: " + location.getOrigin());
			}
		}
		listTickingLocations.add(location);
	}
	
	public void removeTickingLocation(ISimulationTickable location) {
		if (location.getOrigin() != null) {
			Integer hash = PathPointEx.makeHash(location.getOrigin().posX, location.getOrigin().posY, location.getOrigin().posZ);
			if (lookupTickingManagedLocations.containsKey(hash)) {
				lookupTickingManagedLocations.remove(hash);
				location.cleanup();
			} else {
				System.out.println("Error, couldnt find location for removal");
			}
		}
		listTickingLocations.remove(location);
	}
	
	public void setSharedSimulationUpdateRateLimit(String name, int limit) {
		lookupNameToUpdatesPerTickLimit.put(name, limit);
	}
	
	public int getSharedSimulationUpdateRateLimit(String name) {
		return lookupNameToUpdatesPerTickLimit.get(name);
	}
	
	public int getSharedSimulationUpdateRateCurrent(String name) {
		if (!lookupNameToUpdatesPerTickCur.containsKey(name)) {
			lookupNameToUpdatesPerTickCur.put(name, 0);
		}
		return lookupNameToUpdatesPerTickCur.get(name);
	}
	
	public void setSharedSimulationUpdateRateCurrent(String name, int cur) {
		lookupNameToUpdatesPerTickCur.put(name, cur);
	}
	
	public ISimulationTickable getTickingSimulationByLocation(BlockCoord parCoords) {
		Integer hash = PathPointEx.makeHash(parCoords.posX, parCoords.posY, parCoords.posZ);
		return lookupTickingManagedLocations.get(hash);
	}
	
	public void tick() {
		for (int i = 0; i < worldEvents.size(); i++) {
			WorldEvent event = worldEvents.get(i);
			if (event.isComplete()) {
				event.cleanup();
				worldEvents.remove(i--);
			}
		}
		
		//reset usage limits for this tick
		/*Iterator<String, Integer> it = lookupNameToUpdatesPerTickCur.entrySet().iterator();
		while (it.hasNext()) {
			
		}*/
		lookupNameToUpdatesPerTickCur.clear();
		
		//efficient enough? or should i use a list...
		/*Iterator<ISimulationTickable> it = lookupTickingManagedLocations.values().iterator();
		while (it.hasNext()) {
			ISimulationTickable ml = it.next();
			ml.tickUpdate();
		}*/
		for (ISimulationTickable entry : listTickingLocations) {
			entry.tickUpdate();
		}
		
		World world = getWorld();
		
		//update occupance chunk data for each player
		if (ConfigCoroUtilAdvanced.trackPlayerData) {
			if (world.getTotalWorldTime() % PlayerDataGrid.playerTimeSpentUpdateInterval == 0) {
				for (int i = 0; i < world.playerEntities.size(); i++) {
					EntityPlayer entP = world.playerEntities.get(i);
					ChunkDataPoint cdp = WorldDirectorManager.instance().getChunkDataGrid(world).getChunkData(MathHelper.floor(entP.posX) / 16, MathHelper.floor(entP.posZ) / 16);
					cdp.addToPlayerActivityTime(entP.getGameProfile().getId(), PlayerDataGrid.playerTimeSpentUpdateInterval);
				}
			}
		}
	}
	
	public boolean isCoordAndNearAreaNaturalBlocks(World parWorld, int x, int y, int z, int range) {
		if (isNaturalSurfaceBlock(parWorld.getBlockState(new BlockPos(x, y, z)).getBlock()) && 
				isNaturalSurfaceBlock(parWorld.getBlockState(new BlockPos(x+range, y, z)).getBlock()) && 
				isNaturalSurfaceBlock(parWorld.getBlockState(new BlockPos(x-range, y, z)).getBlock()) &&
				isNaturalSurfaceBlock(parWorld.getBlockState(new BlockPos(x, y, z+range)).getBlock()) &&
				isNaturalSurfaceBlock(parWorld.getBlockState(new BlockPos(x, y, z-range)).getBlock())) {
			return true;
		}
		return false;
	}
	
	public boolean isNaturalSurfaceBlock(Block id) {
		if (id == Blocks.SNOW || id == Blocks.GRASS || id == Blocks.DIRT || id == Blocks.SAND || id == Blocks.STONE || id == Blocks.GRAVEL || id == Blocks.TALLGRASS) {
			return true;
		}
		if (isLogOrLeafBlock(id)) return true;
		return false;
	}
	
	public boolean isLogOrLeafBlock(Block id) {
		Block block = id;
		if (block == null) return false;
		if (block.getMaterial(block.getDefaultState()) == Material.LEAVES) return true;
		if (block.getMaterial(block.getDefaultState()) == Material.PLANTS) return true;
		if (block.getMaterial(block.getDefaultState()) == Material.WOOD) return true;
		return false;
	}
	
	public int getTopGroundBlock(World world, int x, int startY, int z) {
		
		int curY = startY;
		int safetyCount = 0;
		while (curY > 0 && safetyCount++ < 300) {
			Block id = world.getBlockState(new BlockPos(x, curY, z)).getBlock();
			
			if (isNaturalSurfaceBlock(id)) {
				return curY;
			}
			
			curY--;
		}
		return 1;
	}
	
	public void tryReadFromFile() {
		readFromFile();
	}
	
	private void readFromFile() {
		try {
			
			String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroUtil" + File.separator + "World" + File.separator;
			
			String fullPath = saveFolder + "WorldData_" + modID + "_" + dimID + "_" + type + ".dat";
			
			if ((new File(fullPath)).exists()) {
				readFromNBT(CompressedStreamTools.readCompressed(new FileInputStream(fullPath)));
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void writeToFile(boolean unloadInstances) {

    	if (unloadInstances) {
    		stopThread();
    	}
    	
		//means nothing to save, and that nbt read from disk hasnt been called yet, so we definately cant let it touch the file
		//if (extraData == null) return;
    	try {
    		
    		NBTTagCompound nbt = new NBTTagCompound();
    		
    		boolean bool = false;
    		if (extraData != null) bool = extraData.getBoolean("generatedTown");
    		//System.out.println("writing nbt, generatedTown: " + bool);
    		
    		//update runtime data to nbt
    		writeToNBT(nbt);
    		//if (extraData == null) extraData = new NBTTagCompound();
    		
    		String saveFolder = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroUtil" + File.separator + "World" + File.separator;
    		
    		//System.out.println("saveFolder: " + saveFolder);
    		
    		//Write out to file
    		if (!(new File(saveFolder).exists())) (new File(saveFolder)).mkdirs();
    		FileOutputStream fos = new FileOutputStream(saveFolder + "WorldData_" + modID + "_" + dimID + "_" + type + ".dat");
	    	CompressedStreamTools.writeCompressed(nbt, fos);
	    	fos.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void readFromNBT(NBTTagCompound parData) {
		extraData = parData.getCompoundTag("extraData");
		
		//these are mandatory fields set during registration, and would lose their values if read in here
		/*modID = parData.getString("modID");
		type = parData.getString("type");
		dimID = parData.getInteger("dimID");*/
		
		NBTTagCompound tickingLocations = parData.getCompoundTag("tickingLocations");
		
		Iterator it = tickingLocations.getKeySet().iterator();
		
		
		while (it.hasNext()) {
			String keyName = (String)it.next();
			NBTTagCompound nbt = tickingLocations.getCompoundTag(keyName);
			
			String classname = nbt.getString("classname");
			
			ClassLoader classLoader = WorldDirector.class.getClassLoader();

			Class aClass = null;
			
		    try {
		        aClass = classLoader.loadClass(classname);
		        //System.out.println("aClass.getName() = " + aClass.getName());
		    } catch (ClassNotFoundException e) {
		        e.printStackTrace();
		    }

			ISimulationTickable locationObj = null;
		    if (aClass != null) {
		    	try {
		    		locationObj = (ISimulationTickable)aClass.getConstructor(new Class[] {}).newInstance();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
		    }
		    if (locationObj != null) {
		    	locationObj.init();
				locationObj.readFromNBT(nbt);
				locationObj.initPost();
				addTickingLocation(locationObj);
				
				//System.out.println("reading in ticking location: " + nbt.toString() + " - " + entrance.getOrigin().posX + " - " + entrance.spawn.posZ);
		    }
		}
	}
	
	public void writeToNBT(NBTTagCompound parData) {
		NBTTagCompound nbtSet = new NBTTagCompound();
		
		int index = 0;
		/*for (Map.Entry<Integer, ISimulationTickable> entry : lookupTickingManagedLocations.entrySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			entry.getValue().writeToNBT(nbt);
			nbtSet.setTag("" + index++, nbt);
		}*/
		for (ISimulationTickable entry : listTickingLocations) {
			NBTTagCompound nbt = new NBTTagCompound();
			entry.writeToNBT(nbt);
			nbtSet.setTag("" + index++, nbt);
		}
		parData.setTag("tickingLocations", nbtSet);
		
		parData.setString("classname", this.getClass().getCanonicalName());
		
		//these are mandatory fields set during registration
		//parData.setString("modID", modID);
		//parData.setString("type", type);
		//parData.setInteger("dimID", dimID);
		
		parData.setTag("extraData", extraData);
	}

	@Override
	public void run() {
		while (threadRunning) {
			try {
				//since the main purpose of this system will basically have all simulations all run with a thread too, no need to split up lookup to threaded and non threaded versions
				/*Iterator<ISimulationTickable> it = lookupTickingManagedLocations.values().iterator();
				while (it.hasNext()) {
					ISimulationTickable ml = it.next();
					if (ml.isThreaded()) {
						ml.tickUpdateThreaded();
					}
				}*/
				for (ISimulationTickable entry : listTickingLocations) {
					if (entry.isThreaded()) {
						entry.tickUpdateThreaded();
					}
				}
				Thread.sleep(threadSleepRate);
			} catch (Exception e) {
				e.printStackTrace();
				stopThread();
			}
			
		}
		
	}
	
	//grid methods
	
	//bypassing death and chunkunload hooks to go right to managed location data through entity AI agent
	/*public void markEntityDied(EntityLivingBase ent) {
		
	}
	
	public void markEntityRemoved(EntityEpochBase ent) {
		
	}*/
	
	/*public void markEntitySpawnegetOrigin()tTime(EntityEpochBase ent) {
		
	}
	
	public void areaScanCompleteCallback(AreaScanner areaScanner) {
		
	}*/
}
