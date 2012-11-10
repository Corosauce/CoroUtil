package CoroAI;

import net.minecraft.src.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

//import org.lwjgl.opengl.GL11;

public class PFQueue implements Runnable {

	public static PFQueue instance;
	public static LinkedList<PFQueueItem> queue;
	public static HashMap pfDelays;
	public static boolean renderLine = true;
	
	public static long maxRequestAge = 1000;
	public static long maxNodeIterations;
	
	
	
	public static World worldMap;
	
	//Temp used stuff
    private PathEx path = new PathEx();
    private IntHashMap pointMap = new IntHashMap();
    private PathPointEx[] pathOptions = new PathPointEx[32];

    public boolean foundEnd;
    public boolean canClimb = false;
    public static boolean canUseLadder = false;
    public static long dropSize = 4; //adjusted per queue request
    
    //hmmmmmmm
    public EntityCreature entH = null;

    
    //public boolean firstUse = true;
    
    //public boolean tryAgain = false;
    
    
    
    private static class PFQueueItem {
    	public int x;
    	public int y;
    	public int z;
    	
    	public float dist;
    	
    	public Entity entSourceRef;
    	public Entity entTargRef; //always null check - not used for now?
    	public int priority;
    	public long timeCreated;
    	public int retryState;
    	public int maxNodeIterations;
    	
    	//Possible keepers
    	public boolean canClimb = false;
        public boolean canUseLadder = false;
        
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
    	
    }

    public PFQueue(World var1) {
    	if (instance == null) {
	    	instance = this;
	    	queue = new LinkedList();
	    	pfDelays = new HashMap();
	        worldMap = var1;
	        
	        //Start the endless loop!
	        (new Thread(this, "Pathfinder Thread")).start();
	        
    	} else {
    		System.out.println("duplicate creation PFQueue!");
    	}
    }

    public void run() {
        //}
    	while (true) {
    		
    		manageQueue();
    		
    	}
    	
    	
    }
    
    public void manageQueue() {
    	//System.out.print("!!");
    	//c_CoroAIUtil.watchWorldObj(); - depreciated, uses entity world obj for multi dimension use
    	
    	if (queue.size() > 30) {
			System.out.println("PF Size: " + queue.size());
		}
		
    	boolean processed = false;
    	while (!processed && queue.size() > 0) {
			if (queue.size() > 0) {
				if (queue.get(0).timeCreated + this.maxRequestAge > System.currentTimeMillis()) {
					processed = true;
	    			this.path.clearPath();
	    	        this.pointMap.clearMap();
	    	        foundEnd = false;
	    	        
	    	        //catching any game exit errors
		    	        try {
		    	        
			    	        //hmmmmmmmmmmmmmmm
			    	        if (queue.get(0).entSourceRef instanceof EntityCreature) {
			    	        	entH = (EntityCreature)queue.get(0).entSourceRef;
			    	        }
			    	        
			    	        //Fishermen hook for preventing dropdown pathing
			    	        if (queue.get(0).entSourceRef instanceof c_IEnhAI && ((c_IEnhAI)queue.get(0).entSourceRef).canUseLadders()) {
			    	        	//if (((EntityKoaMember)queue.get(0).entSourceRef).occupation == EnumKoaOccupation.mFISHERMAN || ((EntityKoaMember)queue.get(0).entSourceRef).occupation == EnumKoaOccupation.fFISHERMAN) {
			    	        		dropSize = 2;
			    	        		canUseLadder = true;
			    	        	//}
			    	        } else {
			    	        	//Vanilla defaults
			    	        	dropSize = 4;
			    	        	canUseLadder = true;
			    	        }
			    			
			    	        
			    	        
			    	        //Pathfind
			    	        maxNodeIterations = queue.get(0).maxNodeIterations;
			    	        //Multi world dimension pathfinding fix 
			    	        worldMap = queue.get(0).entSourceRef.worldObj;
			    			PathEntityEx pathEnt = createEntityPathTo(queue.get(0).entSourceRef, queue.get(0).x, queue.get(0).y, queue.get(0).z, queue.get(0).dist);
			    			//PathEntity pathEnt = createEntityPathTo(queue.get(0).entSourceRef, queue.get(0).x, queue.get(0).y, queue.get(0).z, queue.get(0).dist);
			    			//System.out.println(pathEnt.pathLength);
			    			//Callback code goes here
			    			
			    			//Direct path setting code
			    			//if (queue.get(0).entSourceRef instanceof c_IEnhPF) {
			    				if (pathEnt != null) {
			    					//my system..... still used?
			    					if (queue.get(0).entSourceRef instanceof c_IEnhPF) {
			    						//((c_IEnhPF)queue.get(0).entSourceRef).setPathToEntity(pathEnt);
			    						
			    						((c_IEnhPF)queue.get(0).entSourceRef).setPathExToEntity(pathEnt);
			    						
			    					} else if (queue.get(0).entSourceRef instanceof EntityPlayer) {
			    						c_CoroAIUtil.playerPathfindCallback(pathEnt);
			    					} else if (queue.get(0).entSourceRef instanceof EntityLiving) {
			    						((EntityLiving)queue.get(0).entSourceRef).getNavigator().setPath(convertToPathEntity(pathEnt), 0.23F);
			    					}
			    					
			    					
			    					
			    				} else {
			    					if (queue.get(0).retryState < 4) {
				    					//System.out.println("retryState: " + queue.get(0).retryState);
				    					PathPointEx points[] = new PathPointEx[1];
				    			        points[0] = new PathPointEx(queue.get(0).x, queue.get(0).y, queue.get(0).z);
				    			        ((c_IEnhPF)queue.get(0).entSourceRef).setPathExToEntity(new PathEntityEx(points));
				    			        ((c_IEnhPF)queue.get(0).entSourceRef).faceCoord(queue.get(0).x, queue.get(0).y, queue.get(0).z, 180F, 180F);
				    			        
				    			        EntityLiving center = (EntityLiving)queue.get(0).entSourceRef;
				    			        
				    			        
				    			        
				    			        float look = worldMap.rand.nextInt(90)-45;
				    			        //int height = 10;
				    			        double dist = worldMap.rand.nextInt(26)+(queue.get(0).retryState * 6);
				    			        int gatherX = (int)(center.posX + ((double)(-Math.sin((center.rotationYaw+look) / 180.0F * 3.1415927F) * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)) * dist));
				    			        int gatherY = (int)(center.posY-0.5 + (double)(-MathHelper.sin(center.rotationPitch / 180.0F * 3.1415927F) * dist) - 0D); //center.posY - 0D;
				    			        int gatherZ = (int)(center.posZ + ((double)(Math.cos((center.rotationYaw+look) / 180.0F * 3.1415927F) * Math.cos(center.rotationPitch / 180.0F * 3.1415927F)) * dist));
				    			        
				    			        int id = getBlockId(gatherX, gatherY, gatherZ);
				    			        
				    			        int offset = -5;
				    			        
				    			        while (offset < 5) {
				    			        	if (id == 0) {
				    			        		break;
				    			        	}
				    			        	
				    			        	id = getBlockId(gatherX, gatherY+offset++, gatherZ);
				    			        }
				    			        
				    			        if (offset < 5) {
				    			        	//retry path! found air
				    			        	PFQueueItem job = new PFQueueItem(queue.get(0).entSourceRef, gatherX, gatherY, gatherZ, queue.get(0).dist, 0);
				    			        	job.maxNodeIterations = 1000;
				    			        	job.retryState = queue.get(0).retryState + 1;
				    				    	queue.add(job);
				    				    	
				    			        } else {
				    			        	//System.out.println("topmost block pf");
				    			        	//retry path to topmost block
				    			        	PFQueueItem job = new PFQueueItem(queue.get(0).entSourceRef, gatherX, worldMap.getHeightValue(gatherX, gatherZ)+1, gatherZ, queue.get(0).dist, 0);
				    			        	job.maxNodeIterations = 1500;
				    			        	job.retryState = queue.get(0).retryState + 1;
				    				    	queue.add(job);
				    			        }
			    					} else {
			    						if (queue.get(0).entSourceRef instanceof c_IEnhPF) {
			    							PathPointEx points[] = new PathPointEx[1];
			    					        points[0] = new PathPointEx(queue.get(0).x, queue.get(0).y, queue.get(0).z);
			    					        ((c_IEnhPF)queue.get(0).entSourceRef).setPathExToEntity(new PathEntityEx(points));
			    					        //((EntityLiving)queue.get(0).entSourceRef).getNavigator().setPath(convertToPathEntity(new PathEntityEx(points)), 0.23F);
			    						} else {
			    							/*PathPoint points[] = new PathPoint[1];
			    					        points[0] = new PathPoint(queue.get(0).x, queue.get(0).y, queue.get(0).z);
			    					        ((EntityTropicraftPlayerProxy)queue.get(0).entSourceRef).setPathToEntity(new PathEntity(points));*/
			    						}
			    					}
			    				}
			    			//}
			    				
			    				
		    	        } catch (Exception ex) {
		    	        	//do nothing
		    	        }
		    	       
				} else {
					if (queue.get(0).entSourceRef instanceof c_IEnhPF) {
						PathPointEx points[] = new PathPointEx[1];
				        points[0] = new PathPointEx(queue.get(0).x, queue.get(0).y, queue.get(0).z);
				        ((c_IEnhPF)queue.get(0).entSourceRef).setPathExToEntity(new PathEntityEx(points));
					} else {
						/*PathPoint points[] = new PathPoint[1];
				        points[0] = new PathPoint(queue.get(0).x, queue.get(0).y, queue.get(0).z);
				        ((EntityTropicraftPlayerProxy)queue.get(0).entSourceRef).setPathToEntity(new PathEntity(points));*/
					}
				}
				
				//Finally delete entry
				queue.remove();
			}
    	}
		
		if (processed || queue.size() == 0) {
			try {
				int sleep = 50-queue.size();
				if (sleep < 1) { sleep = 1; }
				Thread.sleep(sleep);/*if (queue.size() < 20) { Thread.sleep(100); } else { Thread.sleep(10); }*/ } catch (Exception ex) {}
		}
    }
    
    // MAIN INTERFACE FUNCTIONS START \\
    public static boolean getPath(Entity var1, Entity var2, float var3) {
    	return getPath(var1, var2, var3, 0);
    }
    
    public static boolean getPath(Entity var1, Entity var2, float var3, int priority) {
		if(var1 != null && var2 != null) {
			//(par2 - (double)(par1Entity.width / 2.0F)), MathHelper.floor_double(par4), MathHelper.floor_double(par6 - (double)(par1Entity.width / 2.0F))
			return tryPath(var1, MathHelper.floor_double(var2.posX-0.5F), (int)(var2.boundingBox.minY), (int)(var2.posZ-1.5F), var3, priority);
			
		} else {
			return false;
		}
    }
    
    public static boolean getPath(Entity var1, int x, int y, int z, float var2) {
    	return getPath(var1, x, y, z, var2, 0);
    }
	
	public static boolean getPath(Entity var1, int x, int y, int z, float var2, int priority) {
        return tryPath(var1, x, y, z, var2, priority);
    }
	// MAIN INTERFACE FUNCTIONS END //

    public static boolean tryPath(Entity var1, int x, int y, int z, float var2, int priority) {
    	
    	//y += 10;
    	
    	if (!var1.onGround) {
    		if (!var1.isInWater()) {
    			while (var1.worldObj.getBlockId(x, --y, z) == 0 && y > 0) { }    				
    			y--;
    			int id = var1.worldObj.getBlockId(x, y, z);
    			if (id > 0 && Block.blocksList[id] != null && (Block.blocksList[id].blockMaterial == Material.water || Block.blocksList[id].blockMaterial == Material.circuits)) {
    				y--;
    			}
    			//System.out.println("Y-1 ID: " + var1.worldObj.getBlockId(x, y-1, z));
	    		/*if (var1 instanceof EntityTropicraftPlayerProxy) {
	    			((EntityTropicraftPlayerProxy)var1).setPathExToEntity(null);
	    		} else if (var1 instanceof EntityCreature) {
	    			((EntityCreature)var1).setPathToEntity(null);
	    		}
	    		return false;*/
    		} else {
    			while (var1.worldObj.getBlockId(x, y, z) != 0) { y++; }
    			y-=1;
    			//System.out.println("water Y-1 ID: " + var1.worldObj.getBlockId(x, y-1, z));
    		}
    	}
    	
    	//Main instance check and initialization 
    	if (instance == null) {
    		new PFQueue(var1.worldObj);
    	}
    	
    	
    	int delay = 3000;
    	boolean tryPath = true;
    	
    	//System.out.println(pfDelays.size());
    	
    	if (pfDelays.containsKey(var1)) {
    		long time = (Long)pfDelays.get(var1); 
    		//System.out.println(time);
    		if (time < System.currentTimeMillis()) {
    			pfDelays.put(var1, System.currentTimeMillis() + delay);
    		} else {
    			tryPath = false;
    		}
    		//int time = (int)Integer.pfDelays.get(var1).;
    	} else {
    		pfDelays.put(var1, System.currentTimeMillis() + delay);
    	}
    	
    	if (tryPath || priority == -1) {
    	
	    	PFQueueItem job = new PFQueueItem(var1, x, y, z, var2, priority);
	    	
	    	try {
		    	if (priority == 0) { queue.add(job); }
		    	else if (priority == -1) { queue.addFirst(job); }
		    	else {
		    		//Basic fake
		    		//queue.addFirst(job);
		    		
		    		//Real
		    		int pos = 0;
		    		while (queue.size() > 0 && priority < queue.get(pos++).priority) { } queue.add(pos, job);
		    	}
	    	} catch (Exception ex) { if (false/*mod_EntMover.masterDebug*/) System.out.println("pfqueue job aborted: " + ex); }
	    	
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
    
    public static PathEntity convertToPathEntity(PathEntityEx pathEx) {
    	
    	if (pathEx != null) {
	    	PathPoint points[] = new PathPoint[pathEx.pathLength];
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

    public PathEntityEx createEntityPathTo(Entity var1, Entity var2, float var3) {
        return this.createEntityPathTo(var1, var2.posX, var2.boundingBox.minY, var2.posZ, var3);
    }

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
    	
    	if (getBlockId(MathHelper.floor_double(var1.boundingBox.minX), MathHelper.floor_double(var1.boundingBox.minY-1), MathHelper.floor_double(var1.boundingBox.minZ)) == 0) {
    		y--;
    	}
    	
    	int id = getBlockId(MathHelper.floor_double(var1.boundingBox.minX), MathHelper.floor_double(var1.boundingBox.minY), MathHelper.floor_double(var1.boundingBox.minZ));
    	
    	if (id == Block.stoneSingleSlab.blockID || id == Block.woodSingleSlab.blockID) {
    		y++;
    	}
    	
        return this.createEntityPathTo(var1, (double)((float)var2 + 0.5F), (double)((float)var3 + 0.5F), (double)((float)var4 + 0.5F), var5, y/*(int)(y - MathHelper.floor_double(var1.boundingBox.minY))*/);
    }

    public PathEntityEx createEntityPathTo(Entity var1, double var2, double var4, double var6, float var8) {
    	return createEntityPathTo(var1, var2, var4, var6, var8, 0);
    }
    
    public PathEntityEx createEntityPathTo(Entity var1, double var2, double var4, double var6, float var8, int yOffset) {
        PathPointEx var9 = this.openPoint(MathHelper.floor_double(var1.boundingBox.minX), MathHelper.floor_double(var1.boundingBox.minY) + yOffset, MathHelper.floor_double(var1.boundingBox.minZ));
        PathPointEx var10 = this.openPoint(MathHelper.floor_double(var2 - (double)(var1.width / 2.0F)), MathHelper.floor_double(var4), MathHelper.floor_double(var6 - (double)(var1.width / 2.0F)));
        PathPointEx var11 = new PathPointEx(MathHelper.floor_float(var1.width + 1.0F), MathHelper.floor_float(var1.height + 1.0F), MathHelper.floor_float(var1.width + 1.0F));
        PathEntityEx var12 = this.addToPath(var1, var9, var10, var11, var8);

        if (var12 != null) {
            //var12.foundEnd = foundEnd;
        }

        try {
            //Thread.sleep(500L);
        } catch (Throwable ex) {
        }
        
        var12 = simplifyPath(var12, var11);
        //System.out.println("PF asasasSize: " + queue.size());
        return var12;
    }

    private PathEntityEx addToPath(Entity var1, PathPointEx var2, PathPointEx var3, PathPointEx var4, float var5) {
        var2.totalPathDistance = 0.0F;
        var2.distanceToNext = var2.distanceTo(var3);
        var2.distanceToTarget = var2.distanceToNext;
        this.path.clearPath();
        this.path.addPoint(var2);
        PathPointEx var6 = var2;

        int lookCount = 0;
        int sleepCount = 0;
        
        while(!this.path.isPathEmpty() && lookCount++ < maxNodeIterations) {
        	try {
        		if (sleepCount++ > 100) {
        			Thread.sleep(1);
        			//System.out.println("sssssss: " + queue.size());
        			sleepCount = 0;
        		}
        	} catch (Exception ex) {
        		ex.printStackTrace();
        	}
            PathPointEx var7 = this.path.dequeue();

            if(var7.equals(var3)) {
                return this.createEntityPath(var2, var3);
            }

            if(var7.distanceTo(var3) < var6.distanceTo(var3)) {
                var6 = var7;
            }

            var7.isFirst = true;
            int var8 = this.findPathOptions(var1, var7, var4, var3, var5);

            for(int var9 = 0; var9 < var8; ++var9) {
                PathPointEx var10 = this.pathOptions[var9];
                float var11 = var7.totalPathDistance + var7.distanceTo(var10);

                if(!var10.isAssigned() || var11 < var10.totalPathDistance) {
                    var10.previous = var7;
                    var10.totalPathDistance = var11;
                    var10.distanceToNext = var10.distanceTo(var3);

                    if(var10.isAssigned()) {
                        this.path.changeDistance(var10, var10.totalPathDistance + var10.distanceToNext);
                    } else {
                        var10.distanceToTarget = var10.totalPathDistance + var10.distanceToNext;
                        this.path.addPoint(var10);
                    }
                }
            }
        }

        if (Math.abs(var3.xCoord - var6.xCoord) < 2 && Math.abs(var3.yCoord - var6.yCoord) < 2 && Math.abs(var3.zCoord - var6.zCoord) < 2) {
            foundEnd = true;
        }
        
        //System.out.println("FE: " + foundEnd + " - LC: " + lookCount);

        if(var6 == var2) {
            return null;
        } else {
            return this.createEntityPath(var2, var6);
        }
    }

    private int findPathOptions(Entity var1, PathPointEx var2, PathPointEx var3, PathPointEx var4, float var5) {
        int var6 = 0;
        byte var7 = 0;

        if(this.getVerticalOffset(var1, var2.xCoord, var2.yCoord + 1, var2.zCoord, var3) == 1) {
            var7 = 1;
        }

        PathPointEx var8 = this.getSafePoint(var1, var2.xCoord, var2.yCoord, var2.zCoord + 1, var3, var7);
        PathPointEx var9 = this.getSafePoint(var1, var2.xCoord - 1, var2.yCoord, var2.zCoord, var3, var7);
        PathPointEx var10 = this.getSafePoint(var1, var2.xCoord + 1, var2.yCoord, var2.zCoord, var3, var7);
        PathPointEx var11 = this.getSafePoint(var1, var2.xCoord, var2.yCoord, var2.zCoord - 1, var3, var7);

        if(var8 != null && !var8.isFirst && var8.distanceTo(var4) < var5) {
            this.pathOptions[var6++] = var8;
        }

        if(var9 != null && !var9.isFirst && var9.distanceTo(var4) < var5) {
            this.pathOptions[var6++] = var9;
        }

        if(var10 != null && !var10.isFirst && var10.distanceTo(var4) < var5) {
            this.pathOptions[var6++] = var10;
        }

        if(var11 != null && !var11.isFirst && var11.distanceTo(var4) < var5) {
            this.pathOptions[var6++] = var11;
        }

        //TEMP!!!!! REFLECTIONIZE!!!
        if (canClimb/* || t_ent instanceof EntityZombieCBuilder*/) {
            PathPointEx vvar8 = this.getClimbPoint(var1, var2.xCoord, var2.yCoord, var2.zCoord + 1, var3, var7, var2.xCoord, var2.zCoord);
            PathPointEx vvar9 = this.getClimbPoint(var1, var2.xCoord - 1, var2.yCoord, var2.zCoord, var3, var7, var2.xCoord, var2.zCoord);
            PathPointEx vvar10 = this.getClimbPoint(var1, var2.xCoord + 1, var2.yCoord, var2.zCoord, var3, var7, var2.xCoord, var2.zCoord);
            PathPointEx vvar11 = this.getClimbPoint(var1, var2.xCoord, var2.yCoord, var2.zCoord - 1, var3, var7, var2.xCoord, var2.zCoord);

            if(vvar8 != null && !vvar8.isFirst && vvar8.distanceTo(var4) < var5) {
                this.pathOptions[var6++] = vvar8;
            }

            if(vvar9 != null && !vvar9.isFirst && vvar9.distanceTo(var4) < var5) {
                this.pathOptions[var6++] = vvar9;
            }

            if(vvar10 != null && !vvar10.isFirst && vvar10.distanceTo(var4) < var5) {
                this.pathOptions[var6++] = vvar10;
            }

            if(vvar11 != null && !vvar11.isFirst && vvar11.distanceTo(var4) < var5) {
                this.pathOptions[var6++] = vvar11;
            }
        }

        if (canUseLadder) {
        	if (getBlockId(var2.xCoord, var2.yCoord, var2.zCoord) == Block.ladder.blockID) {
		        PathPointEx vvar8 = this.getLadderPoint(var1, var2.xCoord, var2.yCoord, var2.zCoord + 1, var3, var7, var2.xCoord, var2.zCoord);
		        PathPointEx vvar9 = this.getLadderPoint(var1, var2.xCoord - 1, var2.yCoord, var2.zCoord, var3, var7, var2.xCoord, var2.zCoord);
		        PathPointEx vvar10 = this.getLadderPoint(var1, var2.xCoord + 1, var2.yCoord, var2.zCoord, var3, var7, var2.xCoord, var2.zCoord);
		        PathPointEx vvar11 = this.getLadderPoint(var1, var2.xCoord, var2.yCoord, var2.zCoord - 1, var3, var7, var2.xCoord, var2.zCoord);
		
		        if(vvar8 != null && !vvar8.isFirst && vvar8.distanceTo(var4) < var5) {
		            this.pathOptions[var6++] = vvar8;
		        }
		
		        if(vvar9 != null && !vvar9.isFirst && vvar9.distanceTo(var4) < var5) {
		            this.pathOptions[var6++] = vvar9;
		        }
		
		        if(vvar10 != null && !vvar10.isFirst && vvar10.distanceTo(var4) < var5) {
		            this.pathOptions[var6++] = vvar10;
		        }
		
		        if(vvar11 != null && !vvar11.isFirst && vvar11.distanceTo(var4) < var5) {
		            this.pathOptions[var6++] = vvar11;
		        }
        	}
        }

        return var6;
    }

    private PathPointEx getLadderPoint(Entity var1, int x, int y, int z, PathPointEx var5, int var6, int origX, int origZ) {
        PathPointEx var7 = null;

        if(this.getVerticalOffset(var1, x, y, z, var5) == 1) {
            var7 = this.openPoint(x, y, z);
        }

        if(var7 == null && var6 > 0 && this.getVerticalOffset(var1, x, y + var6, z, var5) == 1) {
            var7 = this.openPoint(x, y + var6, z);
            y += var6;
        }

        if(var7 == null) {
            int var8 = 0;
            int var9 = 0;
            int var10 = 0;

            //while(y > 0 && y < 128 && (worldMap.getBlockId(x, y + 1, z)) == Block.ladder.blockID && (var10 = this.getVerticalOffset(var1, origX, y + 1, origZ, var5)) == 1) {
            while(y > 0 && y < 128 && ((var9 = this.getVerticalOffset(var1, x, y + 1, z, var5)) == 0) && (getBlockId(origX, y + 1, origZ)) == Block.ladder.blockID) {
                var10 = this.getVerticalOffset(var1, origX, y + 1, origZ, var5);
                ++var8;
                /*if(var8 >= 3) {
                   return null;
                }*/
                ++y;

                if(y > 0 && y < 128) {
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

    private PathPointEx getClimbPoint(Entity var1, int x, int y, int z, PathPointEx var5, int var6, int origX, int origZ) {
        PathPointEx var7 = null;

        if(this.getVerticalOffset(var1, x, y, z, var5) == 1) {
            var7 = this.openPoint(x, y, z);
        }

        if(var7 == null && var6 > 0 && this.getVerticalOffset(var1, x, y + var6, z, var5) == 1) {
            var7 = this.openPoint(x, y + var6, z);
            y += var6;
        }

        if(var7 == null) {
            int var8 = 0;
            int var9 = 0;
            int var10 = 0;

            //while(y > 0 && y < 128 && (worldMap.getBlockId(x, y + 1, z)) == Block.ladder.blockID && (var10 = this.getVerticalOffset(var1, origX, y + 1, origZ, var5)) == 1) {
            while(y > 0 && y < 128 && ((var9 = this.getVerticalOffset(var1, x, y, z, var5)) == 0) && (var10 = this.getVerticalOffset(var1, origX, y, origZ, var5)) == 1) {
                //;
                ++var8;

                if(var8 >= 30) {
                    return null;
                }

                ++y;

                if(y > 0 && y < 128) {
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

    private PathPointEx getSafePoint(Entity var1, int var2, int var3, int var4, PathPointEx var5, int var6) {
        PathPointEx var7 = null;

        if(this.getVerticalOffset(var1, var2, var3, var4, var5) == 1) {
            var7 = this.openPoint(var2, var3, var4);
        }

        if(var7 == null && var6 > 0 && this.getVerticalOffset(var1, var2, var3 + var6, var4, var5) == 1) {
            var7 = this.openPoint(var2, var3 + var6, var4);
            var3 += var6;
        }

        if(var7 != null) {
            int var8 = 0;
            int var9 = 0;

            while(var3 > 0 && (var9 = this.getVerticalOffset(var1, var2, var3 - 1, var4, var5)) == 1) {
                ++var8;

                if(var8 >= dropSize) {
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
    
    private int getVerticalOffset(Entity var1, int var2, int var3, int var4, PathPointEx var5) {
        for(int var6 = var2; var6 < var2 + var5.xCoord; ++var6) {
            for(int var7 = var3; var7 < var3 + var5.yCoord; ++var7) {
                for(int var8 = var4; var8 < var4 + var5.zCoord; ++var8) {
                    int var9 = getBlockId(var6, var7, var8);

                    if(var9 > 0) {
                        if(var9 != Block.doorSteel.blockID && var9 != Block.doorWood.blockID) {
                            if (var9 == Block.fence.blockID || var9 == Block.netherFence.blockID) {
                                return -2;
                            }
                            
                            Material var11 = Block.blocksList[var9].blockMaterial;
                            Block block = Block.blocksList[var9];
                            int meta = worldMap.getBlockMetadata(var2, var3, var4);
                            
                            if (c_CoroAIUtil.isNoPathBlock(var1, var9, meta)) {
                            	return -2;
                            }
                            
                            /*if (var9 == Block.ladder.blockID) {
                                return -1;
                            }*/
                            
                            /*if (mod_PathingActivated.redMoonActive && entH != null && entH instanceof EntityZombie && entH.team != 1 && (var9 == Block.dirt.blockID || Block.blocksList[var9].blockMaterial == Material.wood || Block.blocksList[var9].blockMaterial == Material.glass)) {
                            	return 1;
                            }*/

                            if (block instanceof BlockStep) {
                            	return -2;
                            }
                            
                            if (var11 == Material.circuits) {
                            	return 1;
                            }
                            
                            if (var9 == Block.pressurePlatePlanks.blockID || var9 == Block.pressurePlateStone.blockID) {
                                return 1;
                            }

                            

                            if(var11.isSolid()) {
                                return 0;
                            }

                            if (block != null && block instanceof BlockFlowing) {
                            	return -2;
                            }
                            
                            if(var11 == Material.water) {
                                return -1;
                            }

                            if(var11 == Material.lava || var11 == Material.fire || var11 == Material.cactus) {
                                return -2;
                            }
                            
                            
                        } else {
                            if(!((BlockDoor)Block.doorWood).isDoorOpen(var1.worldObj, var6, var7, var8)) {
                                return -2;
                            }
                        }
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
    
    public PathEntityEx simplifyPath(PathEntityEx pathentity, PathPointEx pathpoint)
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
        		ex.printStackTrace();
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
                    if(getVerticalOffset(null, pathpoint1.xCoord + (int)f, pathpoint1.yCoord, pathpoint1.zCoord + j1 * byte0, pathpoint) != 1 || getVerticalOffset(null, pathpoint1.xCoord + (int)f, pathpoint1.yCoord - 1, pathpoint1.zCoord + j1 * byte0, pathpoint) == 1 || getVerticalOffset(null, pathpoint1.xCoord + (int)f + 1, pathpoint1.yCoord, pathpoint1.zCoord + j1 * byte0, pathpoint) != 1 || getVerticalOffset(null, pathpoint1.xCoord + (int)f + 1, pathpoint1.yCoord - 1, pathpoint1.zCoord + j1 * byte0, pathpoint) == 1 || getVerticalOffset(null, (pathpoint1.xCoord + (int)f) - 1, pathpoint1.yCoord, pathpoint1.zCoord + j1 * byte0, pathpoint) != 1 || getVerticalOffset(null, (pathpoint1.xCoord + (int)f) - 1, pathpoint1.yCoord - 1, pathpoint1.zCoord + j1 * byte0, pathpoint) == 1)
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
                    if(getVerticalOffset(null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord, pathpoint1.zCoord + (int)f1, pathpoint) != 1 || getVerticalOffset(null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord - 1, pathpoint1.zCoord + (int)f1, pathpoint) == 1 || getVerticalOffset(null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord, pathpoint1.zCoord + (int)f1 + 1, pathpoint) != 1 || getVerticalOffset(null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord - 1, pathpoint1.zCoord + (int)f1 + 1, pathpoint) == 1 || getVerticalOffset(null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord, (pathpoint1.zCoord + (int)f1) - 1, pathpoint) != 1 || getVerticalOffset(null, pathpoint1.xCoord + k1 * byte1, pathpoint1.yCoord - 1, (pathpoint1.zCoord + (int)f1) - 1, pathpoint) == 1)
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
    
    private int getBlockId(int x, int y, int z) {
    	if (!worldMap.checkChunksExist(x, 0, z , x, 128, z)) return 10;
    	return worldMap.getBlockId(x, y, z);
    }
    
    private int getBlockMetadata(int x, int y, int z) {
    	if (!worldMap.checkChunksExist(x, 0, z , x, 128, z)) return 0;
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
}
