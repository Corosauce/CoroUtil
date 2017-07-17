package CoroUtil.pathfinding;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import CoroUtil.ChunkCoordinatesSize;
import CoroUtil.util.BlockCoord;

public class PFJobData {

	//Mandatory fields
	public ChunkCoordinatesSize source;
	public Entity sourceEntity;
	public BlockCoord dest;
	public float distMax;

	//Configurable settings
	public int priority = 1; //higher the lower priority
	public int climbHeight = 1; //upwards only
	public int safeDropHeight = 4;
	public boolean canUseLadder = true;
	public boolean canSwimOnWater = true; //requires addition to PFQueue
	public IPFCallback callback = null;
	public boolean performShortPathRetries = false;
	public boolean omniDirectionalPathRequest = false;
	public long maxNodeIterations = 15000;
	public boolean useFlyPathfinding = false;
	public boolean useSwimPathfinding = false; //not just top of water, full water pathing, like as if flying

	//Extra data usage
	public boolean mapOutPathfind = false;
	public float mapOutDistBetweenPoints = 8;
	public List<BlockCoord> listConnectablePoints = new ArrayList<BlockCoord>();

	public boolean fixFloatingStartPosition = false;
	public boolean fixFloatingEndPosition = false;

	//Runtime data
	public int retryState = 0;
	public long timeCreated;
	public boolean ladderInPath = false;
	public boolean foundEnd = false;

	public PFJobData(ChunkCoordinatesSize coordSize, int x, int y, int z, float var2) {
		source = coordSize;
		dest = new BlockCoord(x, y, z);
		distMax = var2;
		timeCreated = System.currentTimeMillis();
	}

	public PFJobData(Entity parEnt, int x, int y, int z, float var2) {
		source = new ChunkCoordinatesSize(MathHelper.floor(parEnt.posX), MathHelper.floor(parEnt.posY), MathHelper.floor(parEnt.posZ), parEnt.world.provider.getDimension(), parEnt.width, parEnt.height);
		sourceEntity = parEnt;
		dest = new BlockCoord(x, y, z);
		distMax = var2;
		timeCreated = System.currentTimeMillis();
	}

	//set this up to get called from within PFQueue unless design needs change
	public void initData() {
		if (omniDirectionalPathRequest) {
			//Modify the drop down height to equal climb height so path can work both ways
			safeDropHeight = climbHeight;

			//Disable ladder pathing since downwards ladder pathing does not work yet
			canUseLadder = false;
		}
	}

	//set this up to get called after pathfind, unless just calling IPFCallback is enough
	public void pfComplete() {
		//System.out.println("listConnectablePoints size: " + listConnectablePoints.size());
	}

	public void addMapOutPoint(BlockCoord parCoords) {

	}

}
