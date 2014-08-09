package CoroUtil.pathfinding;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IntHashMap;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import CoroUtil.ChunkCoordinatesSize;
import CoroUtil.DimensionChunkCache;
import CoroUtil.OldUtil;
import CoroUtil.componentAI.IAdvPF;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.util.CoroUtilBlock;

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
    		System.out.println("Initializing PFQueue");
	    	instance = this;
	    	queue = new LinkedList();
	    	pfDelays = new HashMap();
	        //Start the endless loop!
	        (new Thread(this, "Pathfinder Thread")).start();
	        
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
    			System.out.println("Serious PFQueue crash, reinitializing");
    			//ex.printStackTrace();
    			instance = null;
    		}
    		
    	}
    	
    	System.out.println("Old PFQueue thread end");
    	
    }
    
    public void manageQueue() {
    	//System.out.print("!!");
    	//c_CoroAIUtil.watchWorldObj(); - depreciated, uses entity world obj for multi dimension use
    	maxRequestAge = 2000;
    	
    	boolean give1NodePathIfFail = false;
    	
    	//l
    	
    	Random rand = new Random();
    	
    	//statsPerSecondLastReset = System.currentTimeMillis() / 10000;
    	
    	if (statsPerSecondLastReset < System.currentTimeMillis() / 10000) {
    		statsPerSecondLastReset = System.currentTimeMillis() / 10000;
    		statsPerSecondNode = statsPerSecondNodeSoFar;
    		statsPerSecondPath = statsPerSecondPathSoFar;
    		statsPerSecondPathSkipped = statsPerSecondPathSkippedSoFar;
    		statsPerSecondNodeMaxIter = statsPerSecondNodeMaxIterSoFar;
    		statsPerSecondNodeSoFar = 0;
    		statsPerSecondNodeMaxIterSoFar = 0;
    		statsPerSecondPathSoFar = 0;
    		statsPerSecondPathSkippedSoFar = 0;
    	}
    	
    	try {
	    	Iterator it = pfDelays.entrySet().iterator();
	        while (it.hasNext()) {
	            Map.Entry pairs = (Map.Entry)it.next();
	            //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	            long time = (Long)pairs.getValue();
	            if (time < System.currentTimeMillis() - 30000) {
	            	//System.out.println("ent " + pairs.getKey() + " removing, val: " + pairs.getValue());
	    			pfDelays.remove(pairs.getKey());
	    			
	    		}
	            //it.remove(); // avoids a ConcurrentModificationException
	        }
    	} catch (Exception ex) {
    		//ex.printStackTrace();
    	}
    	
    	
    	
		
    	
	    	boolean processed = false;
	    	while (!processed && queue.size() > 0) {
				if (queue.size() > 0) {
					//
					lastQueueSize = queue.size();
					//queue.clear();
					if (queue.size() > 50 && System.currentTimeMillis() % 2000 == 0) {
						System.out.println("PF Size: " + queue.size());
					}
					
					try {
						/*int wat[] = new int[3];
						wat[6] = 44;*/
						if (queue.get(0).timeCreated + this.maxRequestAge > System.currentTimeMillis()) {
							processed = true;
			    			this.path.clearPath();
			    	        this.pointMap.clearMap();
			    	        pathOptions = new PathPointEx[32];
			    	        foundEnd = false;
			    	        canClimb = false;
			    	        canUseLadder = true;
			    	        
			    	        statsPerSecondPathSoFar++;
			    	        
			    	        //System.out.println("PROCESS PF!");
			    	        
			    	        //catching any game exit errors
				    	        try {
				    	        
				    	        	dbg("PF TRY: " + queue.get(0).retryState + " | " + queue.get(0).sourceEntity);
				    	        	
					    	        //hmmmmmmmmmmmmmmm
					    	        if (queue.get(0).sourceEntity instanceof EntityCreature) {
					    	        	entH = (EntityCreature)queue.get(0).sourceEntity;
					    	        }
					    	        
				    	        	//Vanilla defaults
				    	        	dropSize = queue.get(0).safeDropHeight;
				    	        	canUseLadder = queue.get(0).canUseLadder;
				    	        	
					    	        
					    	        
					    	        if (queue.get(0).sourceEntity instanceof IAdvPF) {
					    	        	canUseLadder = ((IAdvPF)queue.get(0).sourceEntity).canClimbLadders();
					    	        	canClimb = ((IAdvPF)queue.get(0).sourceEntity).canClimbWalls();
					    	        	dropSize = ((IAdvPF)queue.get(0).sourceEntity).getDropSize();
					    	        }
					    			
					    	        //cleanup(queue.get(0).sourceEntity);
					    	        
					    	        
					    	        
					    	        //Pathfind
					    	        maxNodeIterations = queue.get(0).maxNodeIterations;
					    	        //Multi world dimension pathfinding fix 
					    	        worldMap = DimensionChunkCache.dimCacheLookup.get(queue.get(0).source.dimensionId);
					    	        
					    	        PathEntityEx pathEnt = null;
					    	        
					    	        if (queue.get(0).sourceEntity != null) {
					    	        	double dist = queue.get(0).sourceEntity.getDistance(queue.get(0).dest.posX, queue.get(0).dest.posY, queue.get(0).dest.posZ);
					    	        	if (dist > 256) {
					    	        		dbg("B PF: " + queue.get(0).retryState + " | " + dist + " | " + queue.get(0).sourceEntity);
					    	        	}
					    	        	if (queue.get(0).retryState > 0) {
					    	        		dbg("B PF: " + queue.get(0).retryState + " | " + dist + " | " + queue.get(0).sourceEntity);
					    	        		dbg("PF R: " + queue.get(0).retryState + "|" + queue.get(0).maxNodeIterations);
					    	        	} else {
					    	        		dbg("run path");
					    	        	}
					    	        	
					    	        	//PATHFIND!
					    	        	pathEnt = createPathTo(queue.get(0));
					    	        	//pathEnt = createEntityPathTo(queue.get(0).sourceEntity, queue.get(0).dest.posX, queue.get(0).dest.posY, queue.get(0).dest.posZ, queue.get(0).distMax);
					    	        	
					    	        } else {
					    	        	
					    	        	//PATHFIND!
					    	        	pathEnt = createPathTo(queue.get(0).source, queue.get(0).dest.posX, queue.get(0).dest.posY, queue.get(0).dest.posZ, queue.get(0).distMax, 0);
					    	        	
					    	        }
					    			//PathEntity pathEnt = createEntityPathTo(queue.get(0).sourceEntity, queue.get(0).x, queue.get(0).y, queue.get(0).z, queue.get(0).dist);
					    			//System.out.println(pathEnt.pathLength);
					    			//Callback code goes here
					    			
					    			if (pathEnt != null) {
					    				dbg("returned a path size: " + pathEnt.pathLength);
					    				try {
						    				if (queue.get(0).retryState > 0) {
						    	        		dbg("PF SUCCESS: " + queue.get(0).retryState + "|" + queue.get(0).maxNodeIterations);
						    	        	}
					    				} catch (Exception ex) {
					    					System.out.println("this error happens a lot, new bug?");
					    					ex.printStackTrace();
					    				}
					    				//System.out.println("set path");
					    			} else {
					    				//System.out.println("not path");
					    			}
					    			
					    			//Direct path setting code
					    			//if (queue.get(0).sourceEntity instanceof c_IEnhPF) {
					    				if (pathEnt != null) {
					    					setPath(pathEnt);
					    				} else {
					    					if (queue.get(0).sourceEntity != null && queue.get(0).retryState < 4) {
						    					//System.out.println("retryState: " + queue.get(0).retryState);
					    						
						    					/*PathPointEx points[] = new PathPointEx[1];
						    			        points[0] = new PathPointEx(queue.get(0).x, queue.get(0).y, queue.get(0).z);
						    			        setPath(new PathEntityEx(points));*/
						    			        
						    			        if (queue.get(0).sourceEntity instanceof c_IEnhPF) {
						    			        	((c_IEnhPF)queue.get(0).sourceEntity).faceCoord(queue.get(0).dest.posX, queue.get(0).dest.posY, queue.get(0).dest.posZ, 180F, 180F);
						    			        } else if (queue.get(0).sourceEntity instanceof ICoroAI) {
						    			        	((ICoroAI)queue.get(0).sourceEntity).getAIAgent().faceCoord(queue.get(0).dest.posX, queue.get(0).dest.posY, queue.get(0).dest.posZ, 180F, 180F);	
						    			        }
						    			        
						    			        EntityLiving center = (EntityLiving)queue.get(0).sourceEntity;
						    			        
						    			        
						    			        
						    			        float look = rand.nextInt(90)-45;
						    			        //int height = 10;
						    			        double dist = rand.nextInt(26)+(queue.get(0).retryState * 6);
						    			        int gatherX = (int)Math.floor(center.posX + ((double)(-Math.sin((center.rotationYaw+look) / 180.0F * 3.1415927F)/* * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)*/) * dist));
						    			        int gatherY = (int)center.posY;//Math.floor(center.posY-0.5 + (double)(-MathHelper.sin(center.rotationPitch / 180.0F * 3.1415927F) * dist) - 0D); //center.posY - 0D;
						    			        int gatherZ = (int)Math.floor(center.posZ + ((double)(Math.cos((center.rotationYaw+look) / 180.0F * 3.1415927F)/* * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)*/) * dist));
						    			        
						    			        Block block = getBlock(gatherX, gatherY, gatherZ);
						    			        int tries = 0;
						    			        if (!CoroUtilBlock.isAir(block)) {
						    			        	int offset = -5;
							    			        
							    			        while (tries < 30) {
							    			        	if (CoroUtilBlock.isAir(block)) {
							    			        		break;
							    			        	}
							    			        	gatherY += offset++;
							    			        	block = getBlock(gatherX, gatherY, gatherZ);
							    			        	tries++;
							    			        }
						    			        } else {
						    			        	int offset = 0;
						    			        	while (tries < 30) {
						    			        		if (!CoroUtilBlock.isAir(block)) break;
						    			        		gatherY -= offset++;
						    			        		block = getBlock(gatherX, gatherY, gatherZ);
							    			        	tries++;
						    			        	}
						    			        }
						    			        
						    			        
						    			        
						    			        if (tries < 30) {
						    			        	//retry path! found air
						    			        	dbg(tries + "|" + queue.get(0).retryState + " partial path try: " + queue.get(0).sourceEntity);
						    			        	
						    			        	queue.get(0).dest = new ChunkCoordinatesSize(gatherX, gatherY, gatherZ, queue.get(0).source.dimensionId, queue.get(0).source.width, queue.get(0).source.height);
						    			        	queue.get(0).retryState++;
						    			        	queue.add(queue.get(0)); //puts the job back in at the end.... but make sure code below doesnt remove all
						    			        	
						    			        	/*PFQueueItem job = new PFQueueItem(queue.get(0).entSourceRef, gatherX, gatherY, gatherZ, queue.get(0).dist, 0, queue.get(0).callback);
						    			        	job.dimensionID = queue.get(0).dimensionID;
						    			        	job.maxNodeIterations = 4000;
						    			        	job.retryState = queue.get(0).retryState + 1;*/
						    				    	//queue.add(job);
						    				    	
						    				    	//PFQueueItem job2 = queue.get(0).
						    				    	
						    			        } else {
						    			        	//System.out.println("topmost block pf");
						    			        	//retry path to topmost block
						    			        	/*PFQueueItem job = new PFQueueItem(queue.get(0).entSourceRef, gatherX, worldMap.getHeightValue(gatherX, gatherZ)+1, gatherZ, queue.get(0).dist, 0, queue.get(0).callback);
						    			        	job.maxNodeIterations = 1500;
						    			        	job.retryState = queue.get(0).retryState + 1;
						    				    	queue.add(job);*/
						    			        }
					    					} else {
					    						if (give1NodePathIfFail) {
						    						PathPointEx points[] = new PathPointEx[1];
					    					        points[0] = new PathPointEx(queue.get(0).dest.posX, queue.get(0).dest.posY, queue.get(0).dest.posZ);
					    					        
					    					        setPath(new PathEntityEx(points));
					    						}
				    					        
					    						
					    					}
					    				}
					    			//}
					    				
					    				
				    	        } catch (Exception ex) {
				    	        	ex.printStackTrace();
				    	        	//do nothing
				    	        }
				    	       
						} else {
							statsPerSecondPathSkippedSoFar++;
							//System.out.println("PF Job aborted, too old: " + (System.currentTimeMillis() - queue.get(0).timeCreated));
							if (give1NodePathIfFail) {
								PathPointEx points[] = new PathPointEx[1];
						        points[0] = new PathPointEx(queue.get(0).dest.posX, queue.get(0).dest.posY, queue.get(0).dest.posZ);
								
						        setPath(new PathEntityEx(points));
							}
					        
							/*if (queue.get(0).entSourceRef instanceof c_IEnhPF) {
								
						        ((c_IEnhPF)queue.get(0).entSourceRef).setPathExToEntity(new PathEntityEx(points));
							} else if (queue.get(0).entSourceRef instanceof ICoroAI) {
	    						((ICoroAI)queue.get(0).entSourceRef).setPathToEntity(convertToPathEntity(new PathEntityEx(points)));
							} else if (queue.get(0).entSourceRef instanceof EntityPlayer) {
	    						c_CoroAIUtil.playerPathfindCallback(new PathEntityEx(points));
	    					} else if (queue.get(0).entSourceRef instanceof EntityLiving) {
	    						((EntityLiving)queue.get(0).entSourceRef).getNavigator().setPath(convertToPathEntity(new PathEntityEx(points)), 0.23F);
	    					}*/
							
							
						}
					
					//Finally delete entry
					queue.remove(0);
					
					} catch (Exception ex) {
			    		//ex.printStackTrace();
			    		try {
			    			queue.clear();
			    		} catch (Exception ex2) {
			    			//ex2.printStackTrace();
			    			queue.clear();
			    		}
			    	}
				}
	    	}
    	   	
	    	
		
		if (processed || queue.size() == 0) {
			try {
				int sleep = 50-queue.size();
				if (processed) sleep = 3;
				if (sleep < 1) { sleep = 1; }
				//System.out.println("sleep size: " + sleep);
				
				Thread.sleep(sleep);/*if (queue.size() < 20) { Thread.sleep(100); } else { Thread.sleep(10); }*/ } catch (Exception ex) {}
		}
		
    	
    }
    
    public void setPath(PathEntityEx pathEnt) {
    	
    	//System.out.println("que: " + queue.size());
    	
    	lastSuccessPFTime = System.currentTimeMillis();
    	
    	if (queue.get(0).sourceEntity instanceof c_IEnhPF) {
			//((c_IEnhPF)queue.get(0).sourceEntity).setPathToEntity(pathEnt);
			
			((c_IEnhPF)queue.get(0).sourceEntity).setPathExToEntity(pathEnt);
		} else if (queue.get(0).sourceEntity instanceof ICoroAI) {
			//
			((ICoroAI)queue.get(0).sourceEntity).setPathResultToEntity(convertToPathEntity(pathEnt));
		} else if (queue.get(0).sourceEntity instanceof EntityPlayer) {
			OldUtil.playerPathfindCallback(pathEnt);
		} else if (queue.get(0).sourceEntity instanceof EntityCreeper) {
			if (queue.get(0).callback != null) {
				queue.get(0).callback.pfComplete(new PFCallbackItem(convertToPathEntity(pathEnt), (EntityLiving)queue.get(0).sourceEntity, 1F));
			} else {
				((EntityLiving)queue.get(0).sourceEntity).getNavigator().setPath(convertToPathEntity(pathEnt), 1F);
			}
			
		} else if (queue.get(0).sourceEntity instanceof EntityLiving) {
			//System.out.println("setting path on living ent: " + pathEnt.pathLength + " - " + (Float) c_CoroAIUtil.getPrivateValueBoth(EntityLiving.class, (EntityLiving)queue.get(0).sourceEntity, c_CoroAIUtil.refl_obf_Item_moveSpeed, c_CoroAIUtil.refl_mcp_Item_moveSpeed));
			//System.out.println("?!?!?!?");
			
			//TODO: issue here with recent changes breaking behavior tree callback, needs fix! check for callback before source entity!!!
			//mostly resolved in tryPath method, there are scenarios where it never gets the callback though... need external wait timeout maybe
			
			if (queue.get(0).callback != null) {
				queue.get(0).callback.pfComplete(new PFCallbackItem(convertToPathEntity(pathEnt), (EntityLiving)queue.get(0).sourceEntity, 1F));
			} else {
				((EntityLiving)queue.get(0).sourceEntity).getNavigator().setPath(convertToPathEntity(pathEnt), (Float) 1F);
			}
			
		} else {
			if (queue.get(0).callback != null) {
				queue.get(0).callback.pfComplete(new PFCallbackItem(convertToPathEntity(pathEnt), null, 1F));
			}
		}
    }
    
    public synchronized void cleanup(Entity ent) {
    	//pfDelays.remove(ent);
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
			//(par2 - (double)(par1Entity.width / 2.0F)), MathHelper.floor_double(par4), MathHelper.floor_double(par6 - (double)(par1Entity.width / 2.0F))
			//return tryPath(var1, MathHelper.floor_double(var2.posX-0.5F), (int)(var2.boundingBox.minY), (int)(var2.posZ-1.5F), var3, priority, parCallback);
			
			//ok, we're adding 0.5 here to try to fix an issue when target ent is standing on half slab, might fix stairs issues too?
			return tryPath(var1, (int)Math.floor(var2.posX), (int)Math.floor(var2.boundingBox.minY + 0.5), (int)Math.floor(var2.posZ), var3, priority, parCallback);
			
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
    		DimensionChunkCache.updateAllWorldCache();
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
	    		job.dimensionID = var1.worldObj.provider.dimensionId;
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
    	if (var1 != null && CoroUtilBlock.isAir(var1.worldObj.getBlock(x, y-1, z))) {
    		while (CoroUtilBlock.isAir(var1.worldObj.getBlock(x, --y, z)) && y > 0) { y--; }    				
    	}
    	
    	//SOURCE - might not work right - fix fence horror, find the near air block
    	/*int id = var1.worldObj.getBlockId(MathHelper.floor_double(var1.posX), MathHelper.floor_double(var1.boundingBox.minY), MathHelper.floor_double(var1.posZ)x, y-1, z);
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
    		DimensionChunkCache.updateAllWorldCache();
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
	    		job.dimensionID = var1.worldObj.provider.dimensionId;
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
    
    public PathEntity convertToPathEntity(PathEntityEx pathEx) {
    	
    	if (pathEx != null) {
	    	PathPoint points[] = new PathPoint[pathEx.pathLength];
	    	for (int i = 0; i < points.length; i++) {
	    		int y = pathEx.points[i].yCoord;
	    		
	    		
	    		points[i] = new PathPoint(pathEx.points[i].xCoord, y, pathEx.points[i].zCoord);
	    	}
	    	return new PathEntity(points);
    	}
    	return null;
    }
    
    /*public PathEntityEx convertToPathEntityEx(PathEntity pathEx) {
    	
    	if (pathEx != null) {
	    	PathPointEx points[] = new PathPointEx[pathEx.pathLength];
	    	for (int i = 0; i < points.length; i++) {
	    		int y = pathEx.points[i].yCoord;
	    		int id = worldMap.getBlockId(pathEx.points[i].xCoord, y, pathEx.points[i].zCoord);
	    		
	    		//just 0
	    		if (i == 0 && id != 0 && Block.blocksList[id] != null && Block.blocksList[id].blockMaterial == Material.water) {
	    			//y+=1;
	    		}
	    		
	    		if (i != 0 && id != 0 && Block.blocksList[id] != null && Block.blocksList[id].blockMaterial == Material.water) {
	    			//y-=1;
	    		}
	    		
	    		//check 1 lower
	    		id = worldMap.getBlockId(pathEx.points[i].xCoord, y-1, pathEx.points[i].zCoord);
	    		
	    		//just 0
	    		if (i == 0 && id != 0 && Block.blocksList[id] != null && Block.blocksList[id].blockMaterial == Material.water) {
	    			//y+=0;
	    		}
	    		
	    		if (i != 0 && id != 0 && Block.blocksList[id] != null && Block.blocksList[id].blockMaterial == Material.water) {
	    			//y-=1;
	    		}
	    		
	    		points[i] = new PathPointEx(pathEx.points[i].xCoord, y, pathEx.points[i].zCoord);
	    	}
	    	return new PathEntityEx(points);
    	}
    	return null;
    }*/
    
    public PathEntityEx createPathTo(PFJobData parJob) {
    	
    	//SOURCE FIX - fence, iron bars, glass panes
    	//difficult to fix, if too high, has to find CLOSEST air spot to back up to when pathing, this requires the source entity so only those types of paths will be supported
    	//solution might be hacky, but its seldom used in the runtime
    	//for some reason adding in the air check to each direction check broke it entirely, without the check this mostly works, they strafe to side sometimes as distance check isnt perfect
    	
    	//ZC zombies are resisting the fix, they backtrack but then give up on path following quickly, why? lets add in random for now
    	Block block = getBlock(parJob.source.posX, parJob.source.posY, parJob.source.posZ);
    	
    	if (isFenceLike(block)/*id == Block.fence.blockID || id == Block.fenceIron.blockID || id == Block.fenceGate.blockID*/) {
    		if (parJob.sourceEntity != null) {
    			double bestDist = 99999;
    			ChunkCoordinatesSize bestCoords = null;
    			
    			Random rand = new Random();
    			
    			double dist = parJob.sourceEntity.getDistance(parJob.source.posX+1.5D, parJob.source.posY, parJob.source.posZ+0.5D);
    			ChunkCoordinatesSize coords = new ChunkCoordinatesSize(parJob.source.posX+1, parJob.source.posY, parJob.source.posZ, parJob.source.dimensionId, parJob.source.width, parJob.source.height);
    			if (CoroUtilBlock.isAir(getBlock(coords.posX, coords.posY+1, coords.posZ)) && dist < bestDist && rand.nextInt(4) == 0) {
    				bestDist = dist;
    				bestCoords = coords;
    			}
    			
    			dist = parJob.sourceEntity.getDistance(parJob.source.posX+0.5D, parJob.source.posY, parJob.source.posZ+1.5D);
    			coords = new ChunkCoordinatesSize(parJob.source.posX, parJob.source.posY, parJob.source.posZ+1, parJob.source.dimensionId, parJob.source.width, parJob.source.height);
    			if (CoroUtilBlock.isAir(getBlock(coords.posX, coords.posY+1, coords.posZ)) && dist < bestDist && rand.nextInt(4) == 0) {
    				bestDist = dist;
    				bestCoords = coords;
    			}
    			
    			dist = parJob.sourceEntity.getDistance(parJob.source.posX-1.5D, parJob.source.posY, parJob.source.posZ+0.5D);
    			coords = new ChunkCoordinatesSize(parJob.source.posX-1, parJob.source.posY, parJob.source.posZ, parJob.source.dimensionId, parJob.source.width, parJob.source.height);
    			if (CoroUtilBlock.isAir(getBlock(coords.posX, coords.posY+1, coords.posZ)) && dist < bestDist && rand.nextInt(4) == 0) {
    				bestDist = dist;
    				bestCoords = coords;
    			}
    			
    			dist = parJob.sourceEntity.getDistance(parJob.source.posX+0.5D, parJob.source.posY, parJob.source.posZ-1.5D);
    			coords = new ChunkCoordinatesSize(parJob.source.posX, parJob.source.posY, parJob.source.posZ-1, parJob.source.dimensionId, parJob.source.width, parJob.source.height);
    			if (CoroUtilBlock.isAir(getBlock(coords.posX, coords.posY+1, coords.posZ)) && dist < bestDist && rand.nextInt(4) == 0) {
    				bestDist = dist;
    				bestCoords = coords;
    			}
    			
    			//int id2 = getBlockId(parJob.source.posX+1, parJob.source.posY, parJob.source.posZ);
    			
    			if (bestCoords != null) {
    				parJob.source = bestCoords;
    			}
    		} else {
    			parJob.source.posY++;
    		}
    	}
    	
    	PathPointEx startPoint = this.openPoint(MathHelper.floor_double(parJob.source.posX), MathHelper.floor_double(parJob.source.posY), MathHelper.floor_double(parJob.source.posZ));
        PathPointEx endPoint = this.openPoint(MathHelper.floor_double(parJob.dest.posX - (double)(parJob.source.width / 2.0F)), MathHelper.floor_double(parJob.dest.posY), MathHelper.floor_double(parJob.dest.posZ - (double)(parJob.source.width / 2.0F)));
        
        //should we really be flooring these? why not ciel without the + 1????
        PathPointEx size = new PathPointEx((int)Math.ceil(parJob.source.width), (int)Math.ceil(parJob.source.height), (int)Math.ceil(parJob.source.width));
        PathEntityEx var12 = this.addToPath(parJob, startPoint, endPoint, size, parJob.distMax);
        
        if (parJob != null && parJob.sourceEntity != null) {
        	//System.out.println("post pf entityID: " + parJob.sourceEntity.entityId + " - path length: " + var12.pathLength);
        }
        
        return var12;
    }

    public PathEntityEx createEntityPathTo(Entity var1, Entity var2, float var3) {
        return this.createEntityPathTo(var1, var2.posX, var2.boundingBox.minY, var2.posZ, var3);
    }

    @Deprecated
    public PathEntityEx createEntityPathTo(Entity var1, int var2, int var3, int var4, float var5) {
    	/*double x = MathHelper.floor_double(var1.boundingBox.minX);
    	double y = MathHelper.floor_double(var1.boundingBox.minY);
    	double z = MathHelper.floor_double(var1.boundingBox.minZ);
    	if (!var1.onGround) {
    		if (!var1.isInWater()) {
    			y++;
    			while (getBlockId((int)x, (int)--y, (int)z) == 0 && y > 0) { }    				
    			y--;
    			int id = getBlockId((int)x, (int)y, (int)z);
    			if (id > 0 && Block.blocksList[id] != null && (Block.blocksList[id].blockMaterial == Material.water || Block.blocksList[id].blockMaterial == Material.circuits)) {
    				y--;
    			}
    		} else {
    			while (getBlockId((int)x, (int)y, (int)z) != 0) { y++; }
    			y-=1;
    		}
    	}*/
    	
    	int y = 0;
    	
    	Block block = getBlock(MathHelper.floor_double(var1.boundingBox.minX), MathHelper.floor_double(var1.boundingBox.minY), MathHelper.floor_double(var1.boundingBox.minZ));
    	
    	if (block instanceof BlockSlab) {
    		y++;
    	}
    	
        return this.createEntityPathTo(var1, (double)((float)var2), (double)((float)var3), (double)((float)var4), var5, y/*(int)(y - MathHelper.floor_double(var1.boundingBox.minY))*/);
    }

    @Deprecated
    public PathEntityEx createEntityPathTo(Entity var1, double var2, double var4, double var6, float var8) {
    	return createEntityPathTo(var1, var2, var4, var6, var8, 0);
    }
    
    //Pathfinding without a required entity
    @Deprecated
    public PathEntityEx createPathTo(ChunkCoordinatesSize parCoordSize, double var2, double var4, double var6, float parMaxDistPF, int yOffset) {
    	PathPointEx startPoint = this.openPoint(MathHelper.floor_double(parCoordSize.posX), MathHelper.floor_double(parCoordSize.posY) + yOffset, MathHelper.floor_double(parCoordSize.posZ));
        PathPointEx endPoint = this.openPoint(MathHelper.floor_double(var2 - (double)(parCoordSize.width / 2.0F)), MathHelper.floor_double(var4), MathHelper.floor_double(var6 - (double)(parCoordSize.width / 2.0F)));
        PathPointEx size = new PathPointEx(MathHelper.floor_float(parCoordSize.width + 1.0F), MathHelper.floor_float(parCoordSize.height + 1.0F), MathHelper.floor_float(parCoordSize.width + 1.0F));
        PathEntityEx var12 = this.addToPath(null, startPoint, endPoint, size, parMaxDistPF);
        return var12;
    }
    
    @Deprecated
    public PathEntityEx createEntityPathTo(Entity var1, double var2, double var4, double var6, float var8, int yOffset) {
        PathPointEx var9 = this.openPoint(MathHelper.floor_double(var1.boundingBox.minX), MathHelper.floor_double(var1.boundingBox.minY) + yOffset, MathHelper.floor_double(var1.boundingBox.minZ));
        PathPointEx var10 = this.openPoint(MathHelper.floor_double(var2 - (double)(var1.width / 2.0F)), MathHelper.floor_double(var4), MathHelper.floor_double(var6 - (double)(var1.width / 2.0F)));
        PathPointEx var11 = new PathPointEx(MathHelper.floor_float(var1.width + 1.0F), MathHelper.floor_float(var1.height + 1.0F), MathHelper.floor_float(var1.width + 1.0F));
        PathEntityEx var12 = this.addToPath(null, var9, var10, var11, var8);

        
        
        if (var12 != null) {
            //var12.foundEnd = foundEnd;
        }

        try {
            //Thread.sleep(500L);
        } catch (Throwable ex) {
        }
        
        //simplify path is trimming out the node infront of the ladder, this is a temp fix, a proper fix might be an extra marker on a node that its a ladder node, and that it cant be 'simplified'
        //if (queue.get(0) == null || (!queue.get(0).ladderInPath && !queue.get(0).canClimb)) var12 = simplifyPath(var12, var11);
        //System.out.println("PF asasasSize: " + queue.size());
        return var12;
    }

    private PathEntityEx addToPath(PFJobData parJob, PathPointEx parStartPoint, PathPointEx endPoint, PathPointEx size, float parMaxDistPF) {
        parStartPoint.totalPathDistance = 0.0F;
        parStartPoint.distanceToNext = parStartPoint.distanceTo(endPoint);
        parStartPoint.distanceToTarget = parStartPoint.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(parStartPoint);
        PathPointEx curPoint = parStartPoint;

        int lookCount = 0;
        int sleepCount = 0;
        
        long maxNodeIts = maxNodeIterations;
        if (parJob != null) maxNodeIts = parJob.maxNodeIterations;
        
        boolean lastIterationProgressed = false;
        
        while(!this.path.isPathEmpty() && lookCount++ < maxNodeIts) {
        	statsPerSecondNodeSoFar++;
        	try {
        		if (sleepCount++ > 100) {
        			Thread.sleep(1);
        			//System.out.println("sssssss: " + queue.size());
        			sleepCount = 0;
        		}
        	} catch (Exception ex) {
        		//ex.printStackTrace();
        	}
        	
        	//Grab next point from the front of the line
            PathPointEx nextBestPoint = this.path.dequeue();
            
            //if reached end, success
            if(nextBestPoint.equals(endPoint)) {
            	postPF(parJob, endPoint, nextBestPoint, lookCount);
                return this.createEntityPath(parStartPoint, endPoint);
            }

            if(nextBestPoint.distanceTo(endPoint) < curPoint.distanceTo(endPoint)) {
                curPoint = nextBestPoint;
            }

            nextBestPoint.isFirst = true;
            
            int var8 = this.findPathOptions(parJob, nextBestPoint, size, endPoint, parMaxDistPF);

            for(int var9 = 0; var9 < var8; ++var9) {
                PathPointEx potentialPoint = this.pathOptions[var9];
                float potentialPointPathDist = nextBestPoint.totalPathDistance + nextBestPoint.distanceTo(potentialPoint);

                if(!potentialPoint.isAssigned() || potentialPointPathDist < potentialPoint.totalPathDistance) {
                    potentialPoint.previous = nextBestPoint;
                    potentialPoint.totalPathDistance = potentialPointPathDist;
                    potentialPoint.distanceToNext = potentialPoint.distanceTo(endPoint);

                    lastIterationProgressed = true;
                    
                    if(potentialPoint.isAssigned()) {
                        this.path.changeDistance(potentialPoint, potentialPoint.totalPathDistance + potentialPoint.distanceToNext);
                    } else {
                        potentialPoint.distanceToTarget = potentialPoint.totalPathDistance + potentialPoint.distanceToNext;
                        this.path.addPoint(potentialPoint);
                    }
                }
            }
            
            
            if (!lastIterationProgressed) {
            	//future hook for optional failed endpoint mapping
            	if (parJob != null) {
            		if (parJob.mapOutPathfind) {
            			boolean proxFail = false;
            			for (int j = 0; j < parJob.listConnectablePoints.size(); j++) {
                			if (Math.sqrt(parJob.listConnectablePoints.get(j).getDistanceSquared(nextBestPoint.xCoord, nextBestPoint.yCoord, nextBestPoint.zCoord)) < parJob.mapOutDistBetweenPoints) {
                				proxFail = true;
                				break;
                			}
                		}
            			
            			if (!proxFail) {
            				parJob.listConnectablePoints.add(new ChunkCoordinates(nextBestPoint.xCoord, nextBestPoint.yCoord, nextBestPoint.zCoord));
            			}
            		}
            	}
            }
            
            lastIterationProgressed = false;
        }
        
        if (lookCount >= maxNodeIts) {
        	statsPerSecondNodeMaxIterSoFar++;
        }

        postPF(parJob, endPoint, curPoint, lookCount);
        
        if(curPoint == parStartPoint) {
            return null;
        } else {
            return this.createEntityPath(parStartPoint, curPoint);
        }
    }
    
    public void postPF(PFJobData parJob, PathPointEx source, PathPointEx dest, int lookCount) {
    	//MinecraftServer.getServer().getLogAgent().logInfo(info);
    	//ClientTickHandler.displayMessage(info);
    	
    	
        if (Math.abs(source.xCoord - dest.xCoord) < 2 && Math.abs(source.yCoord - dest.yCoord) < 2 && Math.abs(source.zCoord - dest.zCoord) < 2) {
            foundEnd = true;
            if (parJob != null) parJob.foundEnd = foundEnd;
        }
        
        if (parJob != null) {
	        
	        //Dynamic delay depending on how much work previous path was
	        if (parJob.sourceEntity != null) pfDelays.put(parJob.sourceEntity, Math.min((long)(System.currentTimeMillis() + (lookCount * ((double)pfDelayScale / 10))), pfDelayMax));
	        
	        String info = String.valueOf(("FE: " + foundEnd + " - LC: " + lookCount));
	        
	        parJob.pfComplete();
    	}
        //System.out.println(info);
    }

    private int findPathOptions(PFJobData parJob, PathPointEx curPoint, PathPointEx size, PathPointEx endPoint, float var5) {
        int var6 = 0;
        byte var7 = 0;
        
        Entity sourceEntity = null;
        if (parJob != null) {
        	sourceEntity = parJob.sourceEntity;
        }

        if(this.getVerticalOffset(parJob, sourceEntity, curPoint.xCoord, curPoint.yCoord + 1, curPoint.zCoord, size) == 1) {
            var7 = 1;
        }

        PathPointEx var8 = this.getSafePoint(parJob, sourceEntity, curPoint.xCoord, curPoint.yCoord, curPoint.zCoord + 1, size, var7);
        PathPointEx var9 = this.getSafePoint(parJob, sourceEntity, curPoint.xCoord - 1, curPoint.yCoord, curPoint.zCoord, size, var7);
        PathPointEx var10 = this.getSafePoint(parJob, sourceEntity, curPoint.xCoord + 1, curPoint.yCoord, curPoint.zCoord, size, var7);
        PathPointEx var11 = this.getSafePoint(parJob, sourceEntity, curPoint.xCoord, curPoint.yCoord, curPoint.zCoord - 1, size, var7);
        
        if(var8 != null && !var8.isFirst && var8.distanceTo(endPoint) < var5) {
            this.pathOptions[var6++] = var8;
        }

        if(var9 != null && !var9.isFirst && var9.distanceTo(endPoint) < var5) {
            this.pathOptions[var6++] = var9;
        }

        if(var10 != null && !var10.isFirst && var10.distanceTo(endPoint) < var5) {
            this.pathOptions[var6++] = var10;
        }

        if(var11 != null && !var11.isFirst && var11.distanceTo(endPoint) < var5) {
            this.pathOptions[var6++] = var11;
        }
        
        if (parJob.useFlyPathfinding || parJob.useSwimPathfinding) {
        	PathPointEx var12 = this.getSafePoint(parJob, sourceEntity, curPoint.xCoord, curPoint.yCoord + 1, curPoint.zCoord, size, var7);
        	PathPointEx var13 = this.getSafePoint(parJob, sourceEntity, curPoint.xCoord, curPoint.yCoord - 1, curPoint.zCoord, size, var7);
        	if(var12 != null && !var12.isFirst && var12.distanceTo(endPoint) < var5) {
                this.pathOptions[var6++] = var12;
            }
        	if(var13 != null && !var13.isFirst && var13.distanceTo(endPoint) < var5) {
                this.pathOptions[var6++] = var13;
            }
        }

        if (parJob != null && parJob.climbHeight > 1) {
            PathPointEx vvar8 = this.getClimbPoint(parJob, curPoint.xCoord, curPoint.yCoord, curPoint.zCoord + 1, size, var7, curPoint.xCoord, curPoint.zCoord);
            PathPointEx vvar9 = this.getClimbPoint(parJob, curPoint.xCoord - 1, curPoint.yCoord, curPoint.zCoord, size, var7, curPoint.xCoord, curPoint.zCoord);
            PathPointEx vvar10 = this.getClimbPoint(parJob, curPoint.xCoord + 1, curPoint.yCoord, curPoint.zCoord, size, var7, curPoint.xCoord, curPoint.zCoord);
            PathPointEx vvar11 = this.getClimbPoint(parJob, curPoint.xCoord, curPoint.yCoord, curPoint.zCoord - 1, size, var7, curPoint.xCoord, curPoint.zCoord);

            if(vvar8 != null && !vvar8.isFirst && vvar8.distanceTo(endPoint) < var5) {
                this.pathOptions[var6++] = vvar8;
            }

            if(vvar9 != null && !vvar9.isFirst && vvar9.distanceTo(endPoint) < var5) {
                this.pathOptions[var6++] = vvar9;
            }

            if(vvar10 != null && !vvar10.isFirst && vvar10.distanceTo(endPoint) < var5) {
                this.pathOptions[var6++] = vvar10;
            }

            if(vvar11 != null && !vvar11.isFirst && vvar11.distanceTo(endPoint) < var5) {
                this.pathOptions[var6++] = vvar11;
            }
        }

        if (/*parJob != null && parJob.sourceEntity != null && */canUseLadder) {
        	if (!CoroUtilBlock.isAir(getBlock(curPoint.xCoord, curPoint.yCoord, curPoint.zCoord)) && getBlock(curPoint.xCoord, curPoint.yCoord, curPoint.zCoord).isLadder(null, curPoint.xCoord, curPoint.yCoord, curPoint.zCoord, null)) {
        		//if (queue.get(0) != null) queue.get(0).ladderInPath = true; //might conflict with non queue using requests
		        PathPointEx vvar8 = this.getLadderPoint(parJob, sourceEntity, curPoint.xCoord, curPoint.yCoord, curPoint.zCoord + 1, size, var7, curPoint.xCoord, curPoint.zCoord);
		        PathPointEx vvar9 = this.getLadderPoint(parJob, sourceEntity, curPoint.xCoord - 1, curPoint.yCoord, curPoint.zCoord, size, var7, curPoint.xCoord, curPoint.zCoord);
		        PathPointEx vvar10 = this.getLadderPoint(parJob, sourceEntity, curPoint.xCoord + 1, curPoint.yCoord, curPoint.zCoord, size, var7, curPoint.xCoord, curPoint.zCoord);
		        PathPointEx vvar11 = this.getLadderPoint(parJob, sourceEntity, curPoint.xCoord, curPoint.yCoord, curPoint.zCoord - 1, size, var7, curPoint.xCoord, curPoint.zCoord);
		
		        if(vvar8 != null && !vvar8.isFirst && vvar8.distanceTo(endPoint) < var5) {
		            this.pathOptions[var6++] = vvar8;
		        }
		
		        if(vvar9 != null && !vvar9.isFirst && vvar9.distanceTo(endPoint) < var5) {
		            this.pathOptions[var6++] = vvar9;
		        }
		
		        if(vvar10 != null && !vvar10.isFirst && vvar10.distanceTo(endPoint) < var5) {
		            this.pathOptions[var6++] = vvar10;
		        }
		
		        if(vvar11 != null && !vvar11.isFirst && vvar11.distanceTo(endPoint) < var5) {
		            this.pathOptions[var6++] = vvar11;
		        }
        	}
        }

        return var6;
    }

    private PathPointEx getLadderPoint(PFJobData parJob, Entity var1, int x, int y, int z, PathPointEx var5, int var6, int origX, int origZ) {
        PathPointEx var7 = null;

        if(this.getVerticalOffset(parJob, var1, x, y, z, var5) == 1) {
            var7 = this.openPoint(x, y, z);
        }

        if(var7 == null && var6 > 0 && this.getVerticalOffset(parJob, var1, x, y + var6, z, var5) == 1) {
            var7 = this.openPoint(x, y + var6, z);
            y += var6;
        }

        if(var7 == null) {
            int var8 = 0;
            int var9 = 0;
            int var10 = 0;

            //while(y > 0 && y < 128 && (worldMap.getBlockId(x, y + 1, z)) == Block.ladder.blockID && (var10 = this.getVerticalOffset(var1, origX, y + 1, origZ, var5)) == 1) {
            while(y > 0 && y < 256 && ((var9 = this.getVerticalOffset(parJob, var1, x, y + 1, z, var5)) == 0) && (!CoroUtilBlock.isAir(getBlock(origX, y + 1, origZ)) && getBlock(origX, y + 1, origZ).isLadder(null/*var1.worldObj*/, origX, y, origZ, null))) {
                var10 = this.getVerticalOffset(parJob, var1, origX, y + 1, origZ, var5);
                ++var8;
                /*if(var8 >= 3) {
                   return null;
                }*/
                ++y;

                if(y > 0 && y < 256) {
                    var7 = this.openPoint(x, y+1, z);
                }
            }

            if (var10 != 1) {
                return null;
            }

            if(var9 == -2) {
                return null;
            }
        }

        return var7;
    }

    private PathPointEx getClimbPoint(PFJobData parJob, int x, int y, int z, PathPointEx var5, int var6, int origX, int origZ) {
        PathPointEx var7 = null;

        if(this.getVerticalOffset(parJob, parJob.sourceEntity, x, y, z, var5) == 1) {
            var7 = this.openPoint(x, y, z);
        }

        if(var7 == null && var6 > 0 && this.getVerticalOffset(parJob, parJob.sourceEntity, x, y + var6, z, var5) == 1) {
            var7 = this.openPoint(x, y + var6, z);
            y += var6;
        }

        if(var7 == null) {
            int var8 = 0;
            int var9 = 0;
            int var10 = 0;

            //while(y > 0 && y < 128 && (worldMap.getBlockId(x, y + 1, z)) == Block.ladder.blockID && (var10 = this.getVerticalOffset(var1, origX, y + 1, origZ, var5)) == 1) {
            while(y > 0 && y < 256 && ((var9 = this.getVerticalOffset(parJob, parJob.sourceEntity, x, y, z, var5)) == 0) && (var10 = this.getVerticalOffset(parJob, parJob.sourceEntity, origX, y, origZ, var5)) == 1 && (var10 = this.getVerticalOffset(parJob, parJob.sourceEntity, origX, y+1, origZ, var5)) == 1) {
                //;
                ++var8;

                if(var8 >= parJob.climbHeight+1) {
                    return null;
                }

                ++y;

                if(y > 0 && y < 256) {
                    var7 = this.openPoint(x, y, z);
                }
            }

            if (var10 != 1) {
                return null;
            }

            if(var9 == -2) {
                return null;
            }
        }

        return var7;
    }

    private PathPointEx getSafePoint(PFJobData parJob, Entity var1, int var2, int var3, int var4, PathPointEx size, int var6) {
        PathPointEx var7 = null;

        if(this.getVerticalOffset(parJob, var1, var2, var3, var4, size) == 1) {
            var7 = this.openPoint(var2, var3, var4);
        }
        
        /*if (var7 != null && getBlockId(var7.xCoord, var7.yCoord, var7.zCoord) != 0 && Block.blocksList[getBlockId(var7.xCoord, var7.yCoord, var7.zCoord)].isLadder(null, var7.xCoord, var7.yCoord, var7.zCoord)) {
        	System.out.println("hue");
        	return var7;
        }*/
        
        /*if (var7 != null && getBlockId(var7.xCoord, var7.yCoord+var6, var7.zCoord) != 0 && Block.blocksList[getBlockId(var7.xCoord, var7.yCoord+var6, var7.zCoord)].isLadder(null, var7.xCoord, var7.yCoord+var6, var7.zCoord)) {
        	System.out.println("hue2");
        	return var7;
        }*/

        /*Block block = Block.blocksList[worldMap.getBlockId(var2, var3 + var6 - 1, var4)];
        if (var1 != null && block != null && block.isLadder(var1.worldObj, var2, var3 + var6 - 1, var4)) {
        	//System.out.println("what!");
        	//return this.openPoint(var2, var3 + var6 - 1, var4);
        }*/
        
        //if (this.getVerticalOffset(var1, var2, var3 + var6, var4, var5) == -10) {
        	//System.out.println("what!");
        	//return this.openPoint(var2, var3 + var6 - 1, var4);
        //}
        
        if(var7 == null && var6 > 0 && this.getVerticalOffset(parJob, var1, var2, var3 + var6, var4, size) == 1) {
            var7 = this.openPoint(var2, var3 + var6, var4);
            var3 += var6;
        }
        
        /*if (queue.get(0).retryState > 0) {
        	if (var1 instanceof EntityKoaFisher) {
        		//dbg("here we go!");
        	}
        }*/
        
        if (!parJob.useFlyPathfinding) {
            //if we have a safe point
	        if(var7 != null) {
	            int var8 = 0;
	            int var9 = 0;
	
	            //start downscanning for actual ground, while still above y0, 
	            while(var3 > 0 && (var9 = this.getVerticalOffset(parJob, var1, var2, var3 - 1, var4, size)) == 1) {
	                ++var8;
	
	                //if ladder
	                /*if (var9 == -10) {
	                	System.out.println("ladder marked!");
	                	return this.openPoint(var2, var3 - 1, var4);
	                }*/
	                
	                //why 30?!!??! is this debug that wasnt removed?! removed the 30, hope i dont break stuff....
	                if(var8 >= dropSize) {
	                	/*if (queue.get(0).retryState > 0) {
	                		dbg("dropsize abort " + var1);
	                		dbg("");
	                	}*/
	                    return null;
	                }
	
	                --var3;
	
	                if(var3 > 0) {
	                    var7 = this.openPoint(var2, var3, var4);
	                }
	            }
	
	            if(var9 == -2) {
	                return null;
	            }
	        }
        }

        return var7;
    }

    private final PathPointEx openPoint(int var1, int var2, int var3) {
        int var4 = PathPointEx.makeHash(var1, var2, var3);
        PathPointEx var5 = (PathPointEx)this.pointMap.lookup(var4);

        if(var5 == null) {
            var5 = new PathPointEx(var1, var2, var3);
            this.pointMap.addKey(var4, var5);
        }

        return var5;
    }
    
    private int getVerticalOffset(PFJobData parJob, Entity var1, int var2, int var3, int var4, PathPointEx size) {
        for(int var6 = var2; var6 < var2 + size.xCoord; ++var6) {
            for(int var7 = var3; var7 < var3 + size.yCoord; ++var7) {
                for(int var8 = var4; var8 < var4 + size.zCoord; ++var8) {
                    Block var9 = getBlock(var6, var7, var8);

                    if(!CoroUtilBlock.isAir(var9)) {
                        //if(var9 != Blocks.iron_door && var9 != Blocks.wooden_door) {
                            if (isFenceLike(var9)) {
                                return -2;
                            }
                            
                            /*if (var9 == Block.fenceIron.blockID) {
                            	return 0;
                            }*/
                            
                            /*if (var9 == Block.ladder.blockID) {
                                System.out.println("ladder!");
                            }*/
                            
                            Material var11 = var9.getMaterial();//Block.blocksList[var9].blockMaterial;
                            //Block block = Block.blocksList[var9];
                            int meta = getBlockMetadata(var2, var3, var4);
                            
                            
                            int noOverrideID = -66;
                            
                            if (var1 instanceof ICoroAI && var1 instanceof IAdvPF) {
                            	int override = ((IAdvPF)var1).overrideBlockPathOffset((ICoroAI)var1, var9, meta, var2, var3, var4);
                            	
                            	if (override != noOverrideID) {
                            		return override;
                            	}
                            }
                            
                            if (OldUtil.isNoPathBlock(var1, var9, meta)) {
                        		return 2;
                        	}
                            
                            /*if (var1 != null && block != null && block.isLadder(var1.worldObj, var2, var3, var4)) {
                            	return -10;
                            }*/
                            
                            
                            
                            /*if (mod_PathingActivated.redMoonActive && entH != null && entH instanceof EntityZombie && entH.team != 1 && (var9 == Block.dirt.blockID || Block.blocksList[var9].blockMaterial == Material.wood || Block.blocksList[var9].blockMaterial == Material.glass)) {
                            	return 1;
                            }*/
                            
                            if (var11 == Material.circuits || var11 == Material.snow || var11 == Material.plants) {
                            	return 1;
                            }
                            
                            if (isPressurePlate(var9)) {
                                return 1;
                            }

                            if(var11.isSolid()) {
                                return 0;
                            }
                            
                            if (isNotPathable(var9)) {
                            	return -2;
                            }
                            
                            if(var11 == Material.water) {
                            	if (parJob.useSwimPathfinding) {
                            		return 1;
                            	} else {
                            		return -1;
                            	}
                            }

                            if(var11 == Material.lava || var11 == Material.cactus) {
                                return -2;
                            }
                            
                            //this is to replace isDoor open, this is the new more generic way to check for movement blockage
                            if (var9.getBlocksMovement(worldMap, var6, var7, var8)) {
                            	return -2;
                            }
                            
                            
                        /*} else {
                            if(!((BlockDoor)Block.doorWood).isDoorOpen(worldMap, var6, var7, var8)) {
                                return -2;
                            }
                        }*/
                    }
                }
            }
        }

        return 1;
    }

    private PathEntityEx createEntityPath(PathPointEx var1, PathPointEx var2) {
        int var3 = 1;
        PathPointEx var4;

        for(var4 = var2; var4.previous != null; var4 = var4.previous) {
            ++var3;
        }

        PathPointEx[] var5 = new PathPointEx[var3];
        var4 = var2;
        --var3;

        for(var5[var3] = var2; var4.previous != null; var5[var3] = var4) {
            var4 = var4.previous;
            --var3;
        }

        return new PathEntityEx(var5);
    }
    
    public PathEntityEx simplifyPath(PFJobData parJob, PathEntityEx pathentity, PathPointEx pathpoint)
    {
        if(pathentity == null)
        {
            return pathentity;
        }
        LinkedList linkedlist = new LinkedList();
        PathPointEx pathpoint1 = null;
        PathPointEx pathpoint2 = null;
        PathPointEx pathpoint3 = null;
        //PathPointEx apathpoint[] = (PathPointEx)pathentity.points;
        PathPointEx apathpoint[] = pathentity.points;
        int j = apathpoint.length;
        
        int sleepCount = 0;
        
        for(int k = 0; k < j; k++)
        {
        	try {
        		if (sleepCount++ > 10) {
        			Thread.sleep(1);
        			sleepCount = 0;
        		}
        	} catch (Exception ex) {
        		//ex.printStackTrace();
        	}
            PathPointEx pathpoint4 = apathpoint[k];
            if(pathpoint1 == null)
            {
                pathpoint1 = pathpoint4;
                linkedlist.add(pathpoint4);
                continue;
            }
            if(pathpoint2 == null)
            {
                if(pathpoint1.yCoord != pathpoint4.yCoord)
                {
                    pathpoint1 = pathpoint4;
                    linkedlist.add(pathpoint4);
                } else
                {
                    pathpoint2 = pathpoint4;
                }
                continue;
            }
            if(pathpoint2.yCoord != pathpoint4.yCoord)
            {
                linkedlist.add(pathpoint2);
                linkedlist.add(pathpoint4);
                pathpoint1 = pathpoint4;
                pathpoint2 = null;
                continue;
            }
            int l = pathpoint4.xCoord - pathpoint1.xCoord;
            int i1 = pathpoint4.zCoord - pathpoint1.zCoord;
            if(Math.abs(l) < Math.abs(i1))
            {
                float f = 0.0F;
                float f2 = (float)l / (float)Math.abs(i1);
                byte byte0 = 1;
                if(i1 < 0)
                {
                    byte0 = -1;
                }
                for(int j1 = 1; j1 < Math.abs(i1); j1++)
                {
                    if(getVerticalOffset(parJob, null, pathpoint1.xCoord + (int)Math.floor(f), pathpoint1.yCoord, pathpoint1.zCoord + j1 * byte0, pathpoint) != 1 || getVerticalOffset(parJob, null, pathpoint1.xCoord + (int)Math.floor(f), pathpoint1.yCoord - 1, pathpoint1.zCoord + j1 * byte0, pathpoint) == 1 || getVerticalOffset(parJob, null, pathpoint1.xCoord + (int)Math.floor(f) + 1, pathpoint1.yCoord, pathpoint1.zCoord + j1 * byte0, pathpoint) != 1 || getVerticalOffset(parJob, null, pathpoint1.xCoord + (int)Math.floor(f) + 1, pathpoint1.yCoord - 1, pathpoint1.zCoord + j1 * byte0, pathpoint) == 1 || getVerticalOffset(parJob, null, (pathpoint1.xCoord + (int)Math.floor(f)) - 1, pathpoint1.yCoord, pathpoint1.zCoord + j1 * byte0, pathpoint) != 1 || getVerticalOffset(parJob, null, (pathpoint1.xCoord + (int)Math.floor(f)) - 1, pathpoint1.yCoord - 1, pathpoint1.zCoord + j1 * byte0, pathpoint) == 1)
                    {
                        pathpoint1 = pathpoint2;
                        linkedlist.add(pathpoint2);
                        pathpoint2 = pathpoint4;
                    } else
                    {
                        f += f2;
                    }
                }

            } else
            {
                float f1 = 0.0F;
                float f3 = (float)i1 / (float)Math.abs(l);
                byte byte1 = 1;
                if(l < 0)
                {
                    byte1 = -1;
                }
                for(int k1 = 1; k1 < Math.abs(l); k1++)
                {
                    if(getVerticalOffset(parJob, null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord, pathpoint1.zCoord + (int)Math.floor(f1), pathpoint) != 1 || getVerticalOffset(parJob, null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord - 1, pathpoint1.zCoord + (int)Math.floor(f1), pathpoint) == 1 || getVerticalOffset(parJob, null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord, pathpoint1.zCoord + (int)Math.floor(f1) + 1, pathpoint) != 1 || getVerticalOffset(parJob, null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord - 1, pathpoint1.zCoord + (int)Math.floor(f1) + 1, pathpoint) == 1 || getVerticalOffset(parJob, null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord, (pathpoint1.zCoord + (int)Math.floor(f1)) - 1, pathpoint) != 1 || getVerticalOffset(parJob, null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord - 1, (pathpoint1.zCoord + (int)Math.floor(f1)) - 1, pathpoint) == 1)
                    {
                        pathpoint1 = pathpoint2;
                        linkedlist.add(pathpoint2);
                        pathpoint2 = pathpoint4;
                    } else
                    {
                        f1 += f3;
                    }
                }

            }
            pathpoint3 = pathpoint4;
        }

        if(pathpoint3 != null)
        {
            linkedlist.add(pathpoint3);
        } else
        if(pathpoint2 != null)
        {
            linkedlist.add(pathpoint2);
        }
        int i = 0;
        PathPointEx apathpoint1[] = new PathPointEx[linkedlist.size()];
        for(Iterator iterator = linkedlist.iterator(); iterator.hasNext();)
        {
            PathPointEx pathpoint5 = (PathPointEx)iterator.next();
            apathpoint1[i++] = pathpoint5;
        }
        return new PathEntityEx(apathpoint1);
    }
    
    private Block getBlock(int x, int y, int z) {
    	//if (!worldMap.checkChunksExist(x, 0, z , x, 128, z)) return 10;
    	return worldMap.getBlock(x, y, z);
    }
    
    private int getBlockMetadata(int x, int y, int z) {
    	//if (!worldMap.checkChunksExist(x, 0, z , x, 128, z)) return 0;
    	return worldMap.getBlockMetadata(x, y, z);
    }
    
    /*public static void renderPFLines(EntityLiving entityliving, double d, double d1, double d2, 
			float f, float f1) {
    	
    	if (renderLine) {
        	entityliving.ignoreFrustrumCheck = true;
        } else {
        	entityliving.ignoreFrustrumCheck = false;
        }
    	
    	if (renderLine && entityliving instanceof c_IEnhPF) {
    		c_IEnhPF koa = ((c_IEnhPF)entityliving);
    		if (koa.getPath() != null && koa.getPath().points != null) {
	            if (koa.getPath().points.length > 1) {
	            	int ii = koa.getPath().pathIndex - 1;
	            	if (ii < 0) ii = 0;
	            	for (int i = ii; i < koa.getPath().points.length-1; i++) {
	            		PathPointEx ppx = koa.getPath().points[i];
	            		PathPointEx ppx2 = koa.getPath().points[i+1];
	
	        	        if(ppx == null || ppx2 == null)
	        	            return;
	        	
	        	        if (renderLine) {
	        	        	entityliving.ignoreFrustrumCheck = true;
	        	            renderLine(ppx, ppx2, d, d1, d2, f, f1);
	        	        } else {
	        	        	entityliving.ignoreFrustrumCheck = false;
	        	        }
	            	}
	            	
	            }
    		}
    	}
    	
    }

	public static void renderLine(PathPointEx ppx, PathPointEx ppx2, double d, double d1, double d2, float f, float f1) {
	    Tessellator tessellator = Tessellator.instance;
	    RenderManager rm = RenderManager.instance;
	    
	    float castProgress = 1.0F;
	
	    float f10 = 0F;//((entitypirate.prevRenderYawOffset + (entitypirate.renderYawOffset - entitypirate.prevRenderYawOffset) * f1) * 3.141593F) / 180F;
	    double d4 = MathHelper.sin(f10);
	    double d6 = MathHelper.cos(f10);
	
	    double pirateX = ppx.xCoord + 0.5;//(entitypirate.prevPosX + (entitypirate.posX - entitypirate.prevPosX) * f1) - d6 * 0.35D - d4 * 0.85D;
	    double pirateY = ppx.yCoord + 0.5;//(entitypirate.prevPosY + (entitypirate.posY - entitypirate.prevPosY) * f1) +yoffset;
	    double pirateZ = ppx.zCoord + 0.5;//((entitypirate.prevPosZ + (entitypirate.posZ - entitypirate.prevPosZ) * f1) - d4 * 0.35D) + d6 * 0.85D;
	    double entX = ppx2.xCoord + 0.5;//(entity.boundingBox.minX + (entity.boundingBox.maxX - entity.boundingBox.minX) / 2D);
	    double entY = ppx2.yCoord + 0.5;//(entity.boundingBox.minY + (entity.boundingBox.maxY - entity.boundingBox.minY) / 2D);
	    double entZ = ppx2.zCoord + 0.5;//(entity.boundingBox.minZ + (entity.boundingBox.maxZ - entity.boundingBox.minZ) / 2D);
	
	    double fishX = castProgress*(entX - pirateX);
	    double fishY = castProgress*(entY - pirateY);
	    double fishZ = castProgress*(entZ - pirateZ);
	    GL11.glDisable(3553 GL_TEXTURE_2D);
	    GL11.glDisable(2896 GL_LIGHTING);
	    tessellator.startDrawing(3);
	    int stringColor = 0x888888;
	    if (((EntityNode)entitypirate).render) {
	    	stringColor = 0x880000;
	    } else {
	    	stringColor = 0xEF4034;
		//}
	    tessellator.setColorOpaque_I(stringColor);
	    int steps = 16;
	
	    for (int i = 0; i < steps; ++i) {
	        float f4 = i/(float)steps;
	        tessellator.addVertex(
	            pirateX - rm.renderPosX + fishX * f4,//(f4 * f4 + f4) * 0.5D + 0.25D,
	            pirateY - rm.renderPosY + fishY * f4,//(f4 * f4 + f4) * 0.5D + 0.25D,
	            pirateZ - rm.renderPosZ + fishZ * f4);
	    }
	    
	    tessellator.draw();
	    GL11.glEnable(2896 GL_LIGHTING);
	    GL11.glEnable(3553 GL_TEXTURE_2D);
	}*/
    
    //CJBs render pylon code -> 2012-03-20_19.23.58.png in my pictures / his dropbox
    /*private void drawWaypoint(float x, float y, float z, byte r, byte g, byte b)
    {
    	//GL11.glEnable(GL11.GL_LIGHTING);
    	
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        //GL11.glDisable(GL11.GL_DEPTH_TEST);
        y += 2f;
        float f1 = 0f;
        float f2 = 1f;
        float f3 = 0.5f;
        
        GL11.glColor4ub(r, g, b, (byte)200);
        GL11.glBegin(GL11.GL_QUADS);
	        //FRONT
		        GL11.glVertex3f(x+f1,y+f1,z+f2);
		        GL11.glVertex3f(x+f2,y+f1,z+f2); 
		        GL11.glVertex3f(x+f3,y+f2,z+f3);
		        GL11.glVertex3f(x+f3,y+f2,z+f3);
	        
	        //BACK
		        GL11.glVertex3f(x+f1,y+f1,z+f1);
		        GL11.glVertex3f(x+f3,y+f2,z+f3); 
		        GL11.glVertex3f(x+f3,y+f2,z+f3);
		        GL11.glVertex3f(x+f2,y+f1,z+f1);

	        //RIGHT
		        GL11.glVertex3f(x+f2,y+f1,z+f1);
		        GL11.glVertex3f(x+f3,y+f2,z+f3); 
		        GL11.glVertex3f(x+f3,y+f2,z+f3);
		        GL11.glVertex3f(x+f2,y+f1,z+f2);
	        //LEFT
		        GL11.glVertex3f(x+f1,y+f1,z+f1);
		        GL11.glVertex3f(x+f1,y+f1,z+f2); 
		        GL11.glVertex3f(x+f3,y+f2,z+f3);
		        GL11.glVertex3f(x+f3,y+f2,z+f3);
        GL11.glEnd();
        
        y-= 1;
        
        GL11.glBegin(GL11.GL_QUADS);
        //FRONT
	        GL11.glVertex3f(x+f3,y+f1,z+f3);
	        GL11.glVertex3f(x+f3,y+f1,z+f3); 
	        GL11.glVertex3f(x+f2,y+f2,z+f2);
	        GL11.glVertex3f(x+f1,y+f2,z+f2);
        
        //BACK
	        GL11.glVertex3f(x+f3,y+f1,z+f3);
	        GL11.glVertex3f(x+f1,y+f2,z+f1); 
	        GL11.glVertex3f(x+f2,y+f2,z+f1);
	        GL11.glVertex3f(x+f3,y+f1,z+f3);

        //RIGHT
	        GL11.glVertex3f(x+f3,y+f1,z+f3);
	        GL11.glVertex3f(x+f2,y+f2,z+f1); 
	        GL11.glVertex3f(x+f2,y+f2,z+f2);
	        GL11.glVertex3f(x+f3,y+f1,z+f3);
        //LEFT
	        GL11.glVertex3f(x+f3,y+f1,z+f3);
	        GL11.glVertex3f(x+f3,y+f1,z+f3); 
	        GL11.glVertex3f(x+f1,y+f2,z+f2);
	        GL11.glVertex3f(x+f1,y+f2,z+f1);
	        GL11.glEnd();
	        
	        GL11.glColor4f(1, 1, 1, 1.0f);
	        GL11.glBegin(GL11.GL_LINES);

		        GL11.glVertex3f(x+f1,y+f2,z+f1); GL11.glVertex3f(x+f1,y+f2,z+f2); 
		        GL11.glVertex3f(x+f1,y+f2,z+f2); GL11.glVertex3f(x+f2,y+f2,z+f2);
		        GL11.glVertex3f(x+f2,y+f2,z+f2); GL11.glVertex3f(x+f2,y+f2,z+f1);
		        GL11.glVertex3f(x+f2,y+f2,z+f1); GL11.glVertex3f(x+f1,y+f2,z+f1);
	        
		        GL11.glVertex3f(x+f3,y+f1,z+f3); GL11.glVertex3f(x+f1,y+f2,z+f1);
		        GL11.glVertex3f(x+f3,y+f1,z+f3); GL11.glVertex3f(x+f1,y+f2,z+f2);
	        	GL11.glVertex3f(x+f3,y+f1,z+f3); GL11.glVertex3f(x+f2,y+f2,z+f2);
	        	GL11.glVertex3f(x+f3,y+f1,z+f3); GL11.glVertex3f(x+f2,y+f2,z+f1);
	        	
	        	y+=1;
	        	GL11.glVertex3f(x+f1,y+f1,z+f1); GL11.glVertex3f(x+f3,y+f2,z+f3);
		        GL11.glVertex3f(x+f1,y+f1,z+f2); GL11.glVertex3f(x+f3,y+f2,z+f3);
	        	GL11.glVertex3f(x+f2,y+f1,z+f2); GL11.glVertex3f(x+f3,y+f2,z+f3);
	        	GL11.glVertex3f(x+f2,y+f1,z+f1); GL11.glVertex3f(x+f3,y+f2,z+f3);
	        
	        GL11.glEnd();
        
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }*/
    
    public void dbg(Object obj) {
    	//if (debug) System.out.println(obj);
    }
    
    public static boolean isFenceLike(Block block) {
    	return block == Blocks.fence || block == Blocks.iron_bars || block == Blocks.fence_gate || block == Blocks.cobblestone_wall || block == Blocks.nether_brick_fence;
    }
    
    public static boolean isPressurePlate(Block block) {
    	return block == Blocks.light_weighted_pressure_plate || block == Blocks.heavy_weighted_pressure_plate || block == Blocks.stone_pressure_plate || block == Blocks.wooden_pressure_plate;
    }
    
    //in 1.6.4 PFQueue, BlockFlowing was considered a -2 return..... it was probably an attempt to stop mobs from pathing into flowing water that stops their pathing progress, lets remove it for now
    public static boolean isNotPathable(Block block) {
    	return block == Blocks.enchanting_table/* || block == Blocks.flowing_water*/;
    }
}
