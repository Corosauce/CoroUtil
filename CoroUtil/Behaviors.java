package CoroUtil;

import java.util.HashMap;
import java.util.List;

import CoroUtil.pathfinding.c_IEnhPF;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;


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
		if (!(entFields.containsKey(me.getEntityId()))) {
			entFields.put(me.getEntityId(), new DataLatcher());
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
	
	public static void checkOrFixTargetTasks(EntityCreature ent, Class targetClass) {
		if (!aiEnhanced.containsKey(ent)) {
			EntityAIBase newTask = new EntityAIAttackOnCollide(ent, targetClass, 0.23D/*entC.getAIMoveSpeed()*/, true);
			EntityAIBase newTargetTask = new EntityAINearestAttackableTarget(ent, targetClass, 0, true);
			if (ent instanceof IRangedAttackMob && ent instanceof EntitySkeleton) newTask = new EntityAIArrowAttack((IRangedAttackMob)ent, 0.25F, 20, 60, 15.0F);
			
			ent.tasks.addTask(3, newTask);
			EntityAITasks targetTasks = (EntityAITasks)OldUtil.getPrivateValueSRGMCP(EntityLiving.class, ent, "field_70715_bh", "targetTasks");
			if (targetTasks != null) {
				targetTasks.addTask(2, newTargetTask);
				//System.out.println("Adding targetting!");
			} else {
				System.out.println("update targetTasks reflection");
			}
			aiEnhanced.put(ent, true);
		}
	}
	
	public static void setData(Entity ent, DataTypes dtEnum, Object obj) {
		//System.out.println("set: " + ent.entityId);
		//DataLatcher dl = (DataLatcher)entFields.get(ent.entityId);
		//System.out.println("set: " + ent.entityId + "|" + dl);
		((DataLatcher)entFields.get(ent.getEntityId())).values.put(dtEnum, obj);
	}
	
	public static Object getData(Entity ent, DataTypes dtEnum) {
		
		//DataLatcher dl = (DataLatcher)entFields.get(ent.entityId);
		//System.out.println("get: " + ent.entityId + "|" + dl);
		return ((DataLatcher)entFields.get(ent.getEntityId())).values.get(dtEnum);
	}
	
	public static void wheatFollow(EntityCreature me) {
		boolean found = false;
		//followTriggerDist = 32F;
		//if (me.getEntityToAttack() == null) {
			List ents = me.worldObj.getEntitiesWithinAABB(EntityPlayer.class, me.boundingBox.addCoord((double)followTriggerDist, (double)followTriggerDist, (double)followTriggerDist));
	        for(int var3 = 0; var3 < ents.size(); ++var3) {
	           EntityPlayer var5 = (EntityPlayer)ents.get(var3);
	           if (me.getDistanceToEntity(var5) > 3F) {
		           if(var5.getCurrentEquippedItem() != null && var5.getCurrentEquippedItem().getItem() == Items.wheat) {
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
 	           		if(var5.getCurrentEquippedItem() == null || (var5.getCurrentEquippedItem() != null && var5.getCurrentEquippedItem().getItem() == Items.wheat)) {
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
    		if (targ instanceof EntityLivingBase) {
    			me.setAttackTarget((EntityLivingBase)targ);
    		}
	}
	
	public static void follow(EntityCreature me, Entity targ, float dist) {
		if (instance == null) new Behaviors();
		
		
	}
	
	//Generic functions
	public static boolean notMoving(EntityLivingBase var0, float var1) {
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