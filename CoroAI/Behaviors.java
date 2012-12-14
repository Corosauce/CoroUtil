package CoroAI;

import java.util.HashMap;

import net.minecraft.src.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

import CoroAI.entity.c_EnhAI;


public class Behaviors {
	
	public static Behaviors instance;
	
	public static HashMap entFields;
	
	public static HashMap<Entity, Boolean> aiEnhanced = new HashMap();
	
	public static float followTriggerDist = 32F;
	
	
	
	Behaviors() {
		if (instance == null) {
	    	instance = this;
	    	entFields = new HashMap();
		}
	}
	
	public static void check(Entity me) {
		if (instance == null) new Behaviors();
		if (!(entFields.containsKey(me.entityId))) {
			entFields.put(me.entityId, new DataLatcher());
		}
	}
	
	public static void AI(EntityCreature me) {
		
		//Pre-use check stuff for static class awesomeness
		
		check(me);
		
		//default is set here now
		int ticks = (Integer)getData(me, DataTypes.noMoveTicks);
		//System.out.println("NMT: " + ticks);
		if((me.isInWater() && notMoving(me, 0.05F)) || (!me.isInWater()) && notMoving(me, 0.10F)) {
			ticks++;
			if (me.isInWater()) {
				double var2 = me.prevPosX - me.posX;
		        double var4 = me.prevPosZ - me.posZ;
		        float var6 = (float)Math.sqrt(var2 * var2 + var4 * var4);
		        /*if (ticks > 0) *///System.out.println("NMT: " + var6 + " - " + ticks);
		        
			}
			
			

            if(ticks > 150) {
                if(me.worldObj.rand.nextInt(10) == 0) {
                    //System.out.println("idle trigger! - " + ticks);
                	
                    if (me instanceof c_IEnhPF) {
                    	((c_IEnhPF)me).noMoveTriggerCallback();
                    } else {
                    	me.setAttackTarget(null);
                        me.setPathToEntity(null);
                    }
                    ticks = 0;
                }
            }
        } else {
        	ticks = 0;
        }
		
		if (me instanceof EntityCreeper) {	
			//wheatFollow(me);
		}
		
		//updates
		setData(me, DataTypes.noMoveTicks, ticks);
		//((DataLatcher)entFields.get(me)).values.put(DataTypes.noMoveTicks, ticks);
	}
	
	public static void enhanceMonsterAIClose(EntityCreature koa, EntityCreature entHit) {
		entHit.setAttackTarget(koa);
	}
	
	public static void enhanceMonsterAI(EntityLiving ent) {
		
		c_EnhAI koa = null;
		if (ent instanceof c_EnhAI) {
			koa = (c_EnhAI)ent;
		} else {
			//psh!
			return;
		}
		
		int huntRange = 32;
		
		//float closestDist = 9999;
		//EntityCreature closestEnt = null;
		
		List list = ent.worldObj.getEntitiesWithinAABB(EntityCreature.class, ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
        for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
            if (entity1 instanceof EntityCreature) {
            	EntityCreature entC = ((EntityCreature)entity1);
            	
            	if (entC.getEntityToAttack() instanceof EntityPlayer/* && entC.getEntityToAttack() != ModLoader.getMinecraftInstance().thePlayer*/) {
            		entC.setTarget(null);
            	}
            	
            	//if(entC.getEntityToAttack() == null/* || entC.worldObj.rand.nextInt(5) == 0*/) {
            		if (koa.isEnemy(entity1)) {
	                	if (((EntityLiving) entity1).canEntityBeSeen(ent)) {
	                		//if (sanityCheck(entity1)) 
	                		/*if (entC.getNavigator().getPath() == null || entC.getNavigator().getPath().isFinished()) {
	                			PathPoint points[] = new PathPoint[1];
    					        points[0] = new PathPoint((int)ent.posX, (int)ent.posY, (int)ent.posZ);
	                			entC.getNavigator().setPath(new PathEntity(points), entC.getAIMoveSpeed());
	                		}*/
	                		
	                		float dist = entC.getDistanceToEntity(ent);
	                		
	                		if (dist <= 16F) {
	                			/*if (dist < closestDist) {
	                				closestDist = dist;
	                				closestEnt = entity1;
	                			}*/
	                			
	                			if (entC.getAttackTarget() == null || (entC.getAttackTarget() != null && dist < entC.getAttackTarget().getDistanceToEntity(ent))) {
	                				entC.setAttackTarget(ent);
		                			entC.setTarget(ent);
		                			
		                			//entC.getNavigator().setPath(entC.getNavigator().getPathToEntityLiving(ent), 0.3F);
		                			PFQueue.getPath(entC, ent, 16F);
		                			if (!aiEnhanced.containsKey(entC)) {
		                				entC.tasks.addTask(3, new EntityAIAttackOnCollide(entC, c_EnhAI.class, 0.23F/*entC.getAIMoveSpeed()*/, true));
		                				EntityAITasks targetTasks = (EntityAITasks)c_CoroAIUtil.getPrivateValueBoth(EntityLiving.class, entC, "bn", "targetTasks");
		                				if (targetTasks != null) {
		                					targetTasks.addTask(2, new EntityAINearestAttackableTarget(entC, c_EnhAI.class, 16.0F, 0, true));
		                				} else System.out.println("update targetTasks reflection");
		                				aiEnhanced.put(entC, true);
		                			}
	                			}
	                			
	                			
	                			
	                			//System.out.println("ENHANCE!" + entC + "targetting: " + ent);
	                			//entC.setTarget(ent);
	                			//entC.set
	                		}
	                		//PFQueue.getPath(entity1, ent, huntRange);
	                			//huntTarget(entity1);
	    	            		//found = true;
	    	            		//break;
	                		//}
	                		//this.hasAttacked = true;
	                		//getPathOrWalkableBlock(entity1, 16F);
	                	}
            		}
                /*} else {
                	
                }*/
            }
            
        }
        
        //if (closestEnt != null) {
        	
        //}
	}
	
	public static void setData(Entity ent, DataTypes dtEnum, Object obj) {
		//System.out.println("set: " + ent.entityId);
		//DataLatcher dl = (DataLatcher)entFields.get(ent.entityId);
		//System.out.println("set: " + ent.entityId + "|" + dl);
		((DataLatcher)entFields.get(ent.entityId)).values.put(dtEnum, obj);
	}
	
	public static Object getData(Entity ent, DataTypes dtEnum) {
		
		//DataLatcher dl = (DataLatcher)entFields.get(ent.entityId);
		//System.out.println("get: " + ent.entityId + "|" + dl);
		return ((DataLatcher)entFields.get(ent.entityId)).values.get(dtEnum);
	}
	
	public static void wheatFollow(EntityCreature me) {
		boolean found = false;
		//followTriggerDist = 32F;
		//if (me.getEntityToAttack() == null) {
			List ents = me.worldObj.getEntitiesWithinAABB(EntityPlayer.class, me.boundingBox.addCoord((double)followTriggerDist, (double)followTriggerDist, (double)followTriggerDist));
	        for(int var3 = 0; var3 < ents.size(); ++var3) {
	           EntityPlayer var5 = (EntityPlayer)ents.get(var3);
	           if (me.getDistanceToEntity(var5) > 3F) {
		           if(var5.getCurrentEquippedItem() != null && var5.getCurrentEquippedItem().itemID == Item.wheat.shiftedIndex) {
		        	  found = true;
		              setTarget(me, var5);
		              
		              //creeper no despawn set
		              setData(me, DataTypes.shouldDespawn, false);
		           }
	           }
	        }
		//}
        
        if (!found) {
        	//if (entFields.containsKey(me)) {
        		Entity target = (Entity)getData(me, DataTypes.followTarg);
        		if (target instanceof EntityPlayer) {
        			EntityPlayer var5 = (EntityPlayer)target;
 	           		if(var5.getCurrentEquippedItem() == null || (var5.getCurrentEquippedItem() != null && var5.getCurrentEquippedItem().itemID != Item.wheat.shiftedIndex)) {
 	           			setTarget(me, null);
 	           		}
        		}
        		//
        		//if (entityToAttack instanceof EntityPlayer) { entityToAttack = null; }
        	//}
        }
	}
	
	public static void setTarget(EntityCreature me, Entity targ) {
		if (instance == null) new Behaviors();
		
		//System.out.println("setting target: " + targ);
		
		//if (entFields.containsKey(me)) {
    		//DataLatcher dl = (DataLatcher)entFields.get(me); 
    		//System.out.println(time);
    		if (((Entity)getData(me, DataTypes.followTarg)) != targ) {
    			//entFields.put(me, targ);
    			setData(me, DataTypes.followTarg, targ);
    		} else {
    			
    		}
    		//int time = (int)Integer.pfDelays.get(var1).;
    	//} else {
    		
    		//entFields.put(me, targ);
    	//}
    		if (targ instanceof EntityLiving) {
    			me.setAttackTarget((EntityLiving)targ);
    		}
	}
	
	public static void follow(EntityCreature me, Entity targ, float dist) {
		if (instance == null) new Behaviors();
		
		
	}
	
	//Generic functions
	public static boolean notMoving(EntityLiving var0, float var1) {
        double var2 = var0.prevPosX - var0.posX;
        double var4 = var0.prevPosZ - var0.posZ;
        float var6 = (float)Math.sqrt(var2 * var2 + var4 * var4);
        //System.out.println(var6);
        return var6 < var1;
    }

    public float getXZDistanceToEntity(Entity me, Entity var1) {
        float var2 = (float)(me.posX - me.posX);
        float var3 = (float)(me.posZ - me.posZ);
        return (float)Math.sqrt((double)(var2 * var2 + var3 * var3));
    }
	
}