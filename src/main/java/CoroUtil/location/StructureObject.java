package CoroUtil.location;

import java.util.ArrayList;
import java.util.List;

import build.ICustomGen;
import build.world.BuildJob;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.util.ChunkCoordinatesBlock;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.CoroUtilNBT;

public class StructureObject implements ICustomGen {

	//taken from rpgmod, removing block grid usage, keeping in pattern building part
	//if going to reimplement it, add a new class on top of this that can manage health of a building
	
	//should be able to implement the orders taking interface

	public long ID = 0; //incremented from a saved value in team
	public ManagedLocation location;
	public String name = ""; //this is automatically set when created via BuildingMapping, including when tile entity recreates it from nbt
	public int dimID; //used when making a new team, DONT USE OTHERWISE
	public int locationID = -1;
	//public TileEntityRTSBuilding tEnt = null;
	
	public boolean isBuilt = false;
	
	public ChunkCoordinates pos; // the "center" of the building - getBuildingCornerCoord() is used to get the corner min XYZ, to help deal with even and odd sized buildings
	
	public boolean callbackNeedsFirstTimeInit = false; //originally for first time max health calc
	
	public NBTTagCompound initNBTTileEntity = new NBTTagCompound();
	
	/*public BuildingBase(TeamObject parTeam, ChunkCoordinates parPos, ChunkCoordinates parSize) {
		team = parTeam;
		pos = parPos;
		size = parSize;
	}*/
	
	public StructureObject() {
		pos = new ChunkCoordinates();
	}
	
	public void dbg(Object obj) {
		System.out.println("RTSDBG " + name + ": " + obj);
	}
	
	public void init() {
		if (!isBuilt) {
			buildStructureComplete(true);
			isBuilt = true;
		}
	}
	
	public void tickUpdate() {
		
	}
	
	public void writeToNBT(NBTTagCompound var1)
    {
        var1.setLong("ID", ID);
		var1.setString("name", name);
        var1.setInteger("teamID", locationID);
        var1.setBoolean("isBuilt", isBuilt);
        CoroUtilNBT.writeCoords("pos", pos, var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
    	if (var1.hasKey("ID")) ID = var1.getLong("ID");
    	if (var1.hasKey("name")) name = var1.getString("name"); //maybe redundant, since to create this object you must feed it the name ahead of time and it gets set
        if (var1.hasKey("teamID")) locationID = var1.getInteger("teamID");
        isBuilt = var1.getBoolean("isBuilt");
        pos = CoroUtilNBT.readCoords("pos", var1);
        
    }
	
	public List<ChunkCoordinatesBlock> getStructureGenerationComplete(boolean firstTimeScan) {
		
		//get a list from each piece, or pass the list onto each piece?
		//second idea is more efficient
		
		List<ChunkCoordinatesBlock> dataTotal = new ArrayList<ChunkCoordinatesBlock>();
		List<Integer> redundancyData = new ArrayList<Integer>();
		
		//we dont get redundancy data back, does it still work? a list isnt reinitialized so....
		
		dataTotal = getStructureGenerationPattern(dataTotal, redundancyData, firstTimeScan);
		dataTotal = getStructureGenerationSchematic(dataTotal, redundancyData, firstTimeScan);
		
		return dataTotal;
	}
	
	public List<ChunkCoordinatesBlock> getStructureGenerationPattern(List<ChunkCoordinatesBlock> parData, List<Integer> redundancyData, boolean firstTimeScan) {
		return parData;
	}
	
	public List<ChunkCoordinatesBlock> getStructureGenerationSchematic(List<ChunkCoordinatesBlock> parData, List<Integer> redundancyData, boolean firstTimeScan) {
		return parData;
	}
	
	//meant for overriding by StructureDamagable
	public void updateStructureState(List<ChunkCoordinatesBlock> parStructure, boolean firstTimeGen) {
		
	}
    
    public void buildStructureComplete(boolean firstTimeGen) {
    	
    	updateInitNBTForTileEntities();
    	
    	List<ChunkCoordinatesBlock> data = getStructureGenerationPattern(new ArrayList<ChunkCoordinatesBlock>(), new ArrayList<Integer>(), firstTimeGen);
		buildPattern(data);
		
		//no need to wait, get health now
		if (!buildSchematic()) {
			updateStructureState(data, firstTimeGen);
			//healthCur = getStructureHealth(data, firstTimeGen);
			buildingCompleted(firstTimeGen);
		} else {
			callbackNeedsFirstTimeInit = firstTimeGen;
		}
		
		//this will get rerouted to some method that switches depending on if a schematic is printed afterwards or not
		
    }

	@Override
	public void genPassPre(World world, BuildJob parBuildJob, int parPass) {
		if (parPass == -1) {
			System.out.println("building gen complete, calculating cur and max health");
			List<ChunkCoordinatesBlock> data = getStructureGenerationComplete(false);
			updateStructureState(data, callbackNeedsFirstTimeInit);
			//healthCur = getStructureHealth(data, callbackNeedsFirstTimeInit);
			buildingCompleted(callbackNeedsFirstTimeInit);
		}
	}
	
	//called when building has been completed, repairs currently also call this
	public void buildingCompleted(boolean firstTimeGen) {
		
	}
	
	public void updateInitNBTForTileEntities() {
		initNBTTileEntity.setInteger("teamID", this.locationID);
		initNBTTileEntity.setLong("structureID", this.ID);
		
		
		
		//more stuff to add
		
		//unique to building:
		//move points (path point to windows, idle spots for waiting while resources grow etc)
		
	}
    
    public boolean buildSchematic() {
    	return false;
    }
    
    //should get a full override (dont call super) for StructureDamagable so it can set the health data per block placement basis
	public void buildPattern(List<ChunkCoordinatesBlock> parStructure) {
		World world = DimensionManager.getWorld(location.dimID);
		if (world != null) {
			for (int i = 0; i < parStructure.size(); i++) {
				ChunkCoordinatesBlock coords = parStructure.get(i);
				
				world.setBlock(coords.posX, coords.posY, coords.posZ, coords.block, coords.meta, 3);
			}
		}
	}
    
    //meant to be overridden
    public ChunkCoordinates getBuildingCornerCoord() {
    	return null;
    }
	
	public boolean isSafeToGenerateOver(Block id) {
		return CoroUtilBlock.isAir(id) || id == Blocks.tallgrass || id == Blocks.snow;
	}
	
	public boolean isRequiredGroundBlock(Block id) {
		//is ground level block
		if (id == Blocks.water || id == Blocks.flowing_water || id == Blocks.grass || id == Blocks.dirt || id == Blocks.sand || id == Blocks.stone || id == Blocks.gravel/* || id == Block.tallGrass.blockID*/) {
			return true;
		}
		//if (id == BlockRegistry.dirtPath.blockID) return true;
		//if (isLogOrLeafBlock(id)) return true;
		return false;
	}
	
	public boolean isLogOrLeafBlock(Block block) {
		if (block == null) return false;
		if (block.getMaterial() == Material.leaves) return true;
		if (block.getMaterial() == Material.plants) return true;
		if (block.getMaterial() == Material.wood) return true;
		return false;
	}
	
	public int getTopGroundBlock(World world, int x, int startY, int z) {
		
		int curY = startY;
		int safetyCount = 0;
		while (curY > 0 && safetyCount++ < 300) {
			Block id = world.getBlock(x, curY, z);
			
			if (isRequiredGroundBlock(id)) {
				return curY;
			}
			
			curY--;
		}
		return 1;
	}

	@Override
	public NBTTagCompound getInitNBTTileEntity() {
		return initNBTTileEntity;
	}
	
}
