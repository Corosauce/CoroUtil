package CoroUtil.pathfinding;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import CoroUtil.ChunkCoordinatesSize;
import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.util.CoroUtilBlock;
import CoroUtil.util.DimensionChunkCacheNew;

//import org.lwjgl.opengl.GL11;

public class PFQueue implements Runnable {

	public static PFQueue instance;
	public static LinkedList<PFJobData> queue;
	public static HashMap pfDelays = new HashMap();
	public static boolean renderLine = true;
	
	public static long maxRequestAge = 8000;
	public static long maxNodeIterations = 15000;
	
	
	
	public IBlockAccess worldMap;
	
	//Temp used stuff
    private PathEx path = new PathEx();
    private IntHashMap pointMap = new IntHashMap();
    private PathPointEx[] pathOptions = new PathPointEx[32];

    public boolean foundEnd;
    public boolean canClimb = false;
    public int maxClimbHeight = 30;
    public boolean canUseLadder = true;
    public long dropSize = 4; //adjusted per queue request
    
    //hmmmmmmm
    public EntityCreature entH = null;

    
    //public boolean firstUse = true;
    
    //public boolean tryAgain = false;
    
    public static int lastQueueSize = 0;
    public static long lastSuccessPFTime = 0;
    public static int lastChunkCacheCount = 0;
    public long statsPerSecondLastReset = 0;
    public int statsPerSecondPathSoFar = 0;
    public int statsPerSecondPathSkippedSoFar = 0;
    public int statsPerSecondNodeSoFar = 0;
    public int statsPerSecondNodeMaxIterSoFar = 0;
    public static int statsPerSecondPath = 0;
    public static int statsPerSecondPathSkipped = 0;
    public static int statsPerSecondNode = 0;
    public static int statsPerSecondNodeMaxIter = 0;
    
    public static int pfDelayScale = 5;
    public static int pfDelayMax = 500;
    
    public static boolean debug = true;
    
    /*private static class PFQueueItem {
    	public int x;
    	public int y;
    	public int z;
    	
    	public float dist;
    	
    	public int dimensionID;
    	public Entity entSourceRef;
    	public Entity entTargRef; //always null check - not used for now?
    	public ChunkCoordinatesSize coordSize;
    	public int priority;
    	public long timeCreated;
    	public int retryState;
    	public int maxNodeIterations;
    	
    	//Possible keepers
    	public boolean canClimb = false;
        public boolean canUseLadder = false;
        
        public IPFCallback callback = null;
        
        //runtime use only
        public boolean ladderInPath = false;
        
        public boolean give1NodePathIfFail = false;
        
        PFQueueItem(Entity ent, int xx, int yy, int zz, float var2, int pri, IPFCallback parCallback) {
        	this(ent, xx, yy, zz, var2, pri);
        	callback = parCallback;
        }
        
        PFQueueItem(Entity ent, int xx, int yy, int zz, float var2, int pri) {
        	x = xx;
        	y = yy;
        	z = zz;
        	dist = var2;
        	entSourceRef = ent;
        	priority = pri;
        	timeCreated = System.currentTimeMillis();
        	retryState = 0;
        	maxNodeIterations = 15000;
        }
    	
    }*/

    public PFQueue(IBlockAccess var1) {
    	this(var1, true);
    }
    
    public PFQueue(IBlockAccess var1, boolean singleton) {
    	if (instance == null && singleton) {

	    	instance = this;
	    	queue = new LinkedList();
	    	pfDelays = new HashMap();
			//System.out.println("Initializing PFQueue");
	        //Start the endless loop!
	        //(new Thread(this, "Pathfinder Thread")).start();
	        
    	} else {
    		//not duplicate, just using different instance to prevent thread crashes on internal temp lists 
    		//System.out.println("duplicate creation PFQueue!");
    	}
    	
    	//Something is passing the proper dim cache before directly using its own instance of PFQueue
    	if (var1 != null) worldMap = var1;
    	
    }

    public void run() {
        //}
    	while (this == instance) {
    		
    		try {
    			manageQueue();
    			//Thread.sleep(50);
    		} catch (Exception ex) {
    			if (ConfigCoroUtil.PFQueueDebug) System.out.println("Serious PFQueue crash, reinitializing");
    			//ex.printStackTrace();
    			instance = null;
    		}
    		
    	}
    	
    	System.out.println("Old PFQueue thread end");
    	
    }
    
    public void manageQueue() {
		try {
			Thread.sleep(50);
		} catch (Exception ex) {}
    }
    
    // MAIN INTERFACE FUNCTIONS START \\
    public static boolean getPath(Entity var1, Entity var2, float var3) {
    	return getPath(var1, var2, var3, 0);
    }
    
    public static boolean getPath(Entity var1, Entity var2, float var3, int priority) {
    	return getPath(var1, var2, var3, priority, null);
    }
    
    public static boolean getPath(Entity var1, Entity var2, float var3, int priority, IPFCallback parCallback) {
		if(var1 != null && var2 != null) {
			//(par2 - (double)(par1Entity.width / 2.0F)), MathHelper.floor(par4), MathHelper.floor(par6 - (double)(par1Entity.width / 2.0F))
			//return tryPath(var1, MathHelper.floor(var2.posX-0.5F), (int)(var2.boundingBox.minY), (int)(var2.posZ-1.5F), var3, priority, parCallback);
			
			//ok, we're adding 0.5 here to try to fix an issue when target ent is standing on half slab, might fix stairs issues too?
			return tryPath(var1, (int)Math.floor(var2.posX), (int)Math.floor(var2.getEntityBoundingBox().minY + 0.5), (int)Math.floor(var2.posZ), var3, priority, parCallback);
			
		} else {
			return false;
		}
    }
    
    public static boolean getPath(Entity var1, int x, int y, int z, float var2) {
    	return getPath(var1, x, y, z, var2, 0);
    }
	
    public static boolean getPath(Entity var1, int x, int y, int z, float var2, int priority) {
    	return getPath(var1, x, y, z, var2, priority, null);
    }
    
	public static boolean getPath(Entity var1, int x, int y, int z, float var2, int priority, IPFCallback parCallback) {
        return tryPath(var1, x, y, z, var2, priority, parCallback);
    }
	
	public static boolean getPath(ChunkCoordinatesSize coordSize, int x, int y, int z, float var2, int priority, IPFCallback parCallback) {
        return tryPath(null, x, y, z, var2, priority, parCallback, coordSize);
    }
	// MAIN INTERFACE FUNCTIONS END //

	public static long lastCacheUpdate = 0;
	
	public static boolean tryPath(Entity var1, int x, int y, int z, float var2, int priority, IPFCallback parCallback) {
		return tryPath(var1, x, y, z, var2, priority, parCallback, null);
	}
	
	public static boolean tryPath(PFJobData parJob) {
		
		parJob.initData();
		
		if (instance == null) {
    		new PFQueue(null);
    	}
		
		if (lastCacheUpdate < System.currentTimeMillis()) {
    		lastCacheUpdate = System.currentTimeMillis() + 10000;
    		DimensionChunkCacheNew.updateAllWorldCache();
    	}
		
		int delay = 3000 + (queue.size() * 20);
    	boolean tryPath = true;
    	
    	//System.out.println(pfDelays.size());
    	
    	if (parJob.sourceEntity != null) {
    		Entity var1 = parJob.sourceEntity;
	    	if (pfDelays.containsKey(var1)) {
	    		Object obj = pfDelays.get(var1);
	    		long time = 0;
	    		if (obj != null) {
	    			time = (Long)obj;
	    		}
	    		//System.out.println(time);
	    		if (time < System.currentTimeMillis()) {
	    			pfDelays.put(var1, System.currentTimeMillis() + delay);
	    		} else {
	    			tryPath = false;
	    		}
	    		//int time = (int)Integer.pfDelays.get(var1).;
	    	} else {
	    		//System.out.println("new unique pfdelay entry, count: " + pfDelays.size());
	    		pfDelays.put(var1, System.currentTimeMillis() + delay);
	    	}
	    	
	    	//new anti queue overload, checks if entity has existing job in queue, cancels new request if does
	    	//possible side solution, if existing job and isnt close to front of queue, bail on existing, add new one to queue
	    	try {
	    		//this might be a stupid idea, perhaps make a second maintained list for job lookups via entity
	    		//or just use this as a method to see what ai is overtrying, to fix with path every 10 ticks thing
	    		/*for (int i = queue.size()-1; i >= 0; i--) {
	    			PFJobData job = queue.get(i);
	    			if (var1 == job.sourceEntity) {
	    				queue.remove(job);
	    				//System.out.println("preventing redundant pathing attempt, queue size was " + queue.size());
	    				//tryPath = false;
	    				break;
	    			}
	    		}*/
	    	} catch (Exception ex) {
	    		//ex.printStackTrace();
	    	}
    	}
    	
    	if (tryPath || parJob.priority == -1) {
    		
	    	/*PFQueueItem job = new PFQueueItem(var1, x, y, z, var2, priority, parCallback);
	    	job.coordSize = parCoordSize;
	    	if (var1 != null) { 
	    		job.dimensionID = var1.world.provider.dimensionId;
	    	} else {
	    		job.dimensionID = parCoordSize.dimensionId;
	    	}*/
	    	
	    	try {
		    	if (parJob.priority == 0) { queue.addLast(parJob); }
		    	else if (parJob.priority == -1) { queue.addFirst(parJob); }
		    	else {
		    		//Basic fake
		    		//queue.addFirst(job);
		    		
		    		//Real
		    		int pos = 0;
		    		while (queue.size() > 0 && parJob.priority < queue.get(pos++).priority) { } queue.add(pos, parJob);
		    	}
	    	} catch (Exception ex) { if (false/*mod_EntMover.masterDebug*/) System.out.println("pfqueue job aborted: " + ex); }
	    	
	    	//instance.manageQueue();
	    	//if (var1 != null) {
	    		//System.out.println("ent " + var1.entityId + " pathing, maxdist: " + var2);
	    	//}
	    	
	    	// IMPORTANT!!!!! //
	    	// implement a per entity time delay, a hashmap of entity instance->int perhaps? clean it with isDead checks? its threaded!
	    	
	    	///////////////////////////////////////////////
	    	//Need config for:
	    	// - Temp path setting for AI ents
	    	// - direct path setting after path calculated
	    	// - A proper callback for these instead maybe, so implementer can choose what to do with path?
	    	///////////////////////////////////////////////
	    	
	    	return true;
    	} else {
    		return false;
    	}
	}
	
    public static boolean tryPath(Entity var1, int x, int y, int z, float var2, int priority, IPFCallback parCallback, ChunkCoordinatesSize parCoordSize) {
    	
    	//DESTINATION - Adjust if in air, assumes they are gravity bound
    	if (var1 != null && CoroUtilBlock.isAir(var1.world.getBlockState(new BlockPos(x, y-1, z)).getBlock())) {
    		while (CoroUtilBlock.isAir(var1.world.getBlockState(new BlockPos(x, --y, z)).getBlock()) && y > 0) { y--; }    				
    	}
    	
    	//SOURCE - might not work right - fix fence horror, find the near air block
    	/*int id = var1.world.getBlockId(MathHelper.floor(var1.posX), MathHelper.floor(var1.boundingBox.minY), MathHelper.floor(var1.posZ)x, y-1, z);
    	if (id != 0 && Block.blocksList[id] instanceof BlockFence) {
    		System.out.println("fence fix test");
    		Random rand = new Random();
    		
    		//derp attempt
    		var1.posX += rand.nextInt(2)-1;
    		var1.posZ += rand.nextInt(2)-1;
    	}*/
    	
    	//Main instance check and initialization 
    	if (instance == null) {
    		if (var1 == null) return false;
    		new PFQueue(null);
    	}
    	
    	if (lastCacheUpdate < System.currentTimeMillis()) {
    		lastCacheUpdate = System.currentTimeMillis() + 10000;
    		DimensionChunkCacheNew.updateAllWorldCache();
    	}
    	
    	int delay = 3000 + (queue.size() * 20);
    	boolean tryPath = true;
    	
    	//System.out.println(pfDelays.size());
    	
    	
    	if (pfDelays.containsKey(var1)) {
    		Object obj = pfDelays.get(var1);
    		long time = 0;
    		if (obj != null) {
    			time = (Long)obj;
    		}
    		//System.out.println(time);
    		if (time < System.currentTimeMillis()) {
    			pfDelays.put(var1, System.currentTimeMillis() + delay);
    		} else {
    			tryPath = false;
    		}
    		//int time = (int)Integer.pfDelays.get(var1).;
    	} else {
    		//System.out.println("new unique pfdelay entry, count: " + pfDelays.size());
    		pfDelays.put(var1, System.currentTimeMillis() + delay);
    	}
    	
    	//new anti queue overload, checks if entity has existing job in queue, cancels new request if does
    	//possible side solution, if existing job and isnt close to front of queue, bail on existing, add new one to queue
    	try {
    		//this might be a stupid idea, perhaps make a second maintained list for job lookups via entity
    		//or just use this as a method to see what ai is overtrying, to fix with path every 10 ticks thing
    		/*for (int i = queue.size()-1; i >= 0; i--) {
    			PFJobData job = queue.get(i);
    			if (var1 == job.sourceEntity) {
    				queue.remove(job);
    				PFQueue.instance.dbg("preventing redundant pathing attempt, queue size was " + queue.size() + " - source ent was: " + job.sourceEntity);
    				//tryPath = false;
    				break;
    			}
    		}*/
    	} catch (Exception ex) {
    		//ex.printStackTrace();
    	}
    	
    	//temp
    	//var2 = 16;
    	
    	if (tryPath || priority == -1) {
    	
    		if (priority == -1) {
    			int hwta = 0;
    		}
    		
    		PFJobData job = null;
    		
    		if (var1 != null) {
    			job = new PFJobData(var1, x, y, z, var2);
    			job.callback = parCallback;
    			job.canUseLadder = true;
    		} else if (parCoordSize != null) {
    			job = new PFJobData(parCoordSize, x, y, z, var2);
    			job.callback = parCallback;
    			job.canUseLadder = true;
    		} else {
    			System.out.println("invalid use of PFQueue");
    		}
    		
	    	/*PFQueueItem job = new PFQueueItem(var1, x, y, z, var2, priority, parCallback);
	    	job.coordSize = parCoordSize;
	    	if (var1 != null) {
	    		job.dimensionID = var1.world.provider.dimensionId;
	    	} else {
	    		job.dimensionID = parCoordSize.dimensionId;
	    	}*/
	    	try {
		    	if (priority == 0) { queue.addLast(job); }
		    	else if (priority == -1) { queue.addFirst(job); }
		    	else {
		    		//Basic fake
		    		//queue.addFirst(job);
		    		
		    		//Real
		    		int pos = 0;
		    		while (queue.size() > 0 && priority < queue.get(pos++).priority) { } 
		    		
		    		queue.add(pos, job);
		    	}
	    	} catch (Exception ex) { if (false/*mod_EntMover.masterDebug*/) System.out.println("pfqueue job aborted: " + ex); }
	    	
	    	//instance.manageQueue();
	    	if (var1 != null) {
	    		//System.out.println("ent " + var1.entityId + " pathing, maxdist: " + var2);
	    	}
	    	
	    	// IMPORTANT!!!!! //
	    	// implement a per entity time delay, a hashmap of entity instance->int perhaps? clean it with isDead checks? its threaded!
	    	
	    	///////////////////////////////////////////////
	    	//Need config for:
	    	// - Temp path setting for AI ents
	    	// - direct path setting after path calculated
	    	// - A proper callback for these instead maybe, so implementer can choose what to do with path?
	    	///////////////////////////////////////////////
	    	
	    	//If an AI entity is the one asking for a path, set a temp path
	    	if (var1 instanceof c_IEnhPF) {
	    		c_IEnhPF entC = (c_IEnhPF)var1;
	    		//Give entity temp path thats above their head they cant hit
		    	//PathPointEx points[] = new PathPointEx[1];
		        //points[0] = new PathPointEx((int)(var1.posX-0.5), (int)(var1.posY + 4D), (int)(var1.posZ-0.5));
		        //entC.setPathToEntity(new PathEntityEx(points));
	    	} else if (var1 instanceof EntityLiving) {
	    		//EntityLiving entL = (EntityLiving)var1;
	    		//PathPoint points[] = new PathPoint[1];
		        //points[0] = new PathPoint((int)(var1.posX-0.5), (int)(var1.posY + 4D), (int)(var1.posZ-0.5));
	    		//entL.getNavigator().setPath(new PathEntity(points), 0.23F);
	    		try {
	    			//EntityAITasks tasks = (EntityAITasks)ModLoader.getPrivateValue(EntityLiving.class, entL, "tasks");
	    			//ArrayList tasksToDo = (ArrayList)ModLoader.getPrivateValue(EntityAITasks.class, tasks, "tasksToDo");
	    			//ModLoader.setPrivateValue(EntityAIAttackOnCollide.class, tasksToDo.get(2), "field_48269_i", 999);	    			
	    		} catch (Exception ex) {}
	    		
	    		
	    	}
	    	return true;
    	} else {
    		return false;
    	}
    }
    
    public void dbg(Object obj) {
    	if (debug) System.out.println(obj);
    }
    
    public static boolean isFenceLike(Block block) {
    	return block instanceof BlockFence || block == Blocks.COBBLESTONE_WALL;
    }
    
    public static boolean isPressurePlate(Block block) {
    	return block == Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE || block == Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE || block == Blocks.STONE_PRESSURE_PLATE || block == Blocks.WOODEN_PRESSURE_PLATE;
    }
    
    //in 1.6.4 PFQueue, BlockFlowing was considered a -2 return..... it was probably an attempt to stop mobs from pathing into flowing water that stops their pathing progress, lets remove it for now
    public static boolean isNotPathable(Block block) {
    	return block == Blocks.ENCHANTING_TABLE/* || block == Blocks.flowing_water*/;
    }
}
