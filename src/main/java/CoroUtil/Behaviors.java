package CoroUtil;

import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.*;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.goal.NearestAttackableTargetGoal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import CoroUtil.pathfinding.c_IEnhPF;
import net.minecraft.item.Items;


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
	
	public static void AI(CreatureEntity me) {
		
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
                if(me.world.rand.nextInt(10) == 0) {
                    //System.out.println("idle trigger! - " + ticks);
                	
                    if (me instanceof c_IEnhPF) {
                    	((c_IEnhPF)me).noMoveTriggerCallback();
                    } else {
                    	me.setAttackTarget(null);
                        me.getNavigator().setPath(null, 0);
                    }
                    ticks = 0;
                }
            }
        } else {
        	ticks = 0;
        }
		
		if (me instanceof CreeperEntity) {
			//wheatFollow(me);
		}
		
		//updates
		setData(me, DataTypes.noMoveTicks, ticks);
		//((DataLatcher)entFields.get(me)).values.put(DataTypes.noMoveTicks, ticks);
	}
	
	public static void enhanceMonsterAIClose(CreatureEntity koa, CreatureEntity entHit) {
		entHit.setAttackTarget(koa);
	}
	
	public static void checkOrFixTargetTasks(CreatureEntity ent, Class targetClass) {
		if (!aiEnhanced.containsKey(ent)) {
			Goal newTask = new MeleeAttackGoal(ent, /*targetClass, */0.23D/*entC.getAIMoveSpeed()*/, true);
			Goal newTargetTask = new NearestAttackableTargetGoal(ent, targetClass, true);
			if (ent instanceof IRangedAttackMob && ent instanceof SkeletonEntity) newTask = new RangedBowAttackGoal((SkeletonEntity)ent, 0.25F, /*20, */60, 15.0F);
			
			ent.tasks.addTask(3, newTask);
			GoalSelector targetTasks = (GoalSelector)OldUtil.getPrivateValueSRGMCP(MobEntity.class, ent, "field_70715_bh", "targetTasks");
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
	
	public static void wheatFollow(CreatureEntity me) {
		boolean found = false;
		//followTriggerDist = 32F;
		//if (me.getEntityToAttack() == null) {
			List ents = me.world.getEntitiesWithinAABB(PlayerEntity.class, me.getEntityBoundingBox().grow((double)followTriggerDist, (double)followTriggerDist, (double)followTriggerDist));
	        for(int var3 = 0; var3 < ents.size(); ++var3) {
	           PlayerEntity var5 = (PlayerEntity)ents.get(var3);
	           if (me.getDistanceToEntity(var5) > 3F) {
		           if(var5.getActiveItemStack() != null && var5.getActiveItemStack().getItem() == Items.WHEAT) {
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
        		if (target instanceof PlayerEntity) {
        			PlayerEntity var5 = (PlayerEntity)target;
 	           		if(var5.getActiveItemStack() == null || (var5.getActiveItemStack() != null && var5.getActiveItemStack().getItem() == Items.WHEAT)) {
 	           			setTarget(me, null);
 	           		}
        		}
        		//
        		//if (entityToAttack instanceof EntityPlayer) { entityToAttack = null; }
        	//}
        }
	}
	
	public static void setTarget(CreatureEntity me, Entity targ) {
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
    		if (targ instanceof LivingEntity) {
    			me.setAttackTarget((LivingEntity)targ);
    		}
	}
	
	public static void follow(CreatureEntity me, Entity targ, float dist) {
		if (instance == null) new Behaviors();
		
		
	}
	
	//Generic functions
	public static boolean notMoving(LivingEntity var0, float var1) {
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