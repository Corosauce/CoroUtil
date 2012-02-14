package net.CoroAI;

import java.util.HashMap;

import net.minecraft.src.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;

public class Behaviors {
	
	public static Behaviors instance;
	
	public static HashMap entFields;
	
	public static float followTriggerDist = 32F;
	
	Behaviors() {
		if (instance == null) {
	    	instance = this;
	    	entFields = new HashMap();
		}
	}
	
	public static void check(Entity me) {
		if (instance == null) new Behaviors();
		if (!(entFields.containsKey(me))) {
			entFields.put(me, new DataLatcher());
		}
	}
	
	public static void AI(EntityCreature me) {
		
		//Pre-use check stuff for static class awesomeness
		
		check(me);
		
		//default is set here now
		int ticks = 0;
		if(notMoving(me, 0.15F)) {
			ticks = (Integer)getData(me, DataTypes.noMoveTicks) + 1;
			

            if(ticks > 50) {
                if(me.worldObj.rand.nextInt(10) == 0) {
                    //System.out.println("idle trigger! - " + ticks);
                	me.setEntityToAttack(null);
                    me.setPathToEntity(null);
                    if (me instanceof EntityTropicraftPlayerProxy) {
                    	((EntityTropicraftPlayerProxy)me).setPathExToEntity(null);
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
	
	public static void setData(Entity ent, DataTypes dtEnum, Object obj) {
		((DataLatcher)entFields.get(ent)).values.put(dtEnum, obj);
	}
	
	public static Object getData(Entity ent, DataTypes dtEnum) {
		return ((DataLatcher)entFields.get(ent)).values.get(dtEnum);
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
		me.setEntityToAttack(targ);
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