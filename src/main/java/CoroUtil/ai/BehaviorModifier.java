package CoroUtil.ai;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import CoroUtil.OldUtil;
import CoroUtil.forge.CoroAI;
import CoroUtil.pets.PetsManager;
import CoroUtil.util.Vec3;

public class BehaviorModifier {
	
	//pet mod design notes/ideas
	
	//need registry to mark and looking existing pets against
	//- used for targetting non pets
	//- used for fixing active fights between pets (skeletons line of fire incidents)
	//- used for invoking enemy target pets tasks 
	
	//entityid
	//public static HashMap<Integer, Boolean> aiEnhanced = new HashMap<Integer, Boolean>();
	
	public static void enhanceZombiesToDig(World parWorld, Vec3 parPos, Class[] taskToInject, int priorityOfTask, float chanceToEnhance) {
		int modifyRange = 100;
		
		AxisAlignedBB aabb = new AxisAlignedBB(parPos.xCoord, parPos.yCoord, parPos.zCoord, parPos.xCoord, parPos.yCoord, parPos.zCoord);
		aabb = aabb.expand(modifyRange, modifyRange, modifyRange);
		List list = parWorld.getEntitiesWithinAABB(EntityZombie.class, aabb);
		
		int enhanceCount = 0;
		int enhanceCountTry = 0;
		
        for(int j = 0; j < list.size(); j++)
        {
        	EntityCreature ent = (EntityCreature)list.get(j);
            
        	if (ent != null && !ent.isDead) {
        		//if (!aiEnhanced.containsKey(ent.getEntityId())) {
        		//log that we've tried to enhance with chance already, prevent further attempts to avoid stacking the odds per call on this method
        		if (!ent.getEntityData().getBoolean("CoroAI_HW_EnhanceChanceTried")) {
        			
        			enhanceCountTry++;
        			
        			ent.getEntityData().setBoolean("CoroAI_HW_EnhanceChanceTried", true);
        			
        			if (parWorld.rand.nextFloat() < chanceToEnhance) {
        			
	        			if (!ent.getEntityData().getBoolean("CoroAI_HW_Enhanced")) {
	            			for (Class clazz : taskToInject) {
	    		        		try {
	    		        			Constructor<?> cons = clazz.getConstructor();
	    		    				Object obj = cons.newInstance();
	    		    				if (obj instanceof ITaskInitializer) {
	    		    					ITaskInitializer task = (ITaskInitializer) obj;
	    		    					task.setEntity(ent);
	    		    					//System.out.println("adding task into zombie: " + taskToInject);
	    		    					ent.tasks.addTask(priorityOfTask, (EntityAIBase) task);
	    		    					//aiEnhanced.put(ent.getEntityId(), true);
	    		    					((PathNavigateGround)ent.getNavigator()).setBreakDoors(false);
	    		    					
	    		    					
	    		    					
	    		    				}
	    						} catch (Exception e) {
	    							e.printStackTrace();
	    						}
	            			}
	            			
	            			enhanceCount++;
	            			ent.getEntityData().setBoolean("CoroAI_HW_Enhanced", true);
	    					ent.getEntityData().setBoolean("CoroAI_HW_GravelDeath", true);
	            		}
        			}
        		} else {
        			//System.out.println("already tried to enhance on this entity");
        		}
        	}
        }
        
        System.out.println("enhanced " + enhanceCount + " of " + enhanceCountTry + " entities");
	}/*

	public static void test(World parWorld, Vec3 parPos, EntityPlayer player) {
		
		int modifyRange = 10;
		
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(parPos.xCoord, parPos.yCoord, parPos.zCoord, parPos.xCoord, parPos.yCoord, parPos.zCoord);
		aabb = aabb.expand(modifyRange, modifyRange, modifyRange);
		List list = parWorld.getEntitiesWithinAABB(EntityCreature.class, aabb);
        for(int j = 0; j < list.size(); j++)
        {
        	EntityCreature ent = (EntityCreature)list.get(j);
            
        	if (ent != null && !ent.isDead) {
        		if (!ent.getEntityData().getBoolean(CoroPets.tameString)) {
        			
        			//ffffffaaaaaiiiiiiillllllllll - but should work in 1.8 where EVERYTHING uses task system
        			if (ent instanceof EntitySpider) {
        				EntityCreature oldEnt = ent;
	        			if (ent instanceof EntityCaveSpider) {
	        				ent = new EntityCaveSpider(parWorld) {
	        					@Override
	        					protected boolean isAIEnabled() {
	        						return true;
	        					}};
	        			} else if (ent instanceof EntitySpider) {
	        				ent = new EntitySpider(parWorld) {
	        					@Override
	        					protected boolean isAIEnabled() {
	        						return true;
	        					}};
	        			}
	        			
	        			ent.setPosition(oldEnt.posX, oldEnt.posY, oldEnt.posZ);
	        			ent.setEntityId(oldEnt.getEntityId());
	        			//add other copy code here
	        			
	        			ent.worldObj.loadedEntityList.remove(oldEnt);
	        			ent.worldObj.loadedEntityList.add(ent);
	        			
	        			//oldEnt.setDead();
	        			//ent.worldObj.spawnEntityInWorld(ent);
        			}
        			
        			tameMob(ent, player.getGameProfile().getId(), true);
	        		
        		}
        	}
        }
	}
	
	public static void tameMob(EntityCreature ent, UUID uuid, boolean addEntry) {
		
		if (addEntry) {
			PetsManager.instance().addPet(uuid, ent);
			ent.getEntityData().setBoolean(CoroPets.tameString, true);
			ent.setCustomNameTag("Pet " + ent.getClass().getSimpleName());
			ent.func_110163_bv();
			CoroAI.dbg("tamed: " + ent);
		} else {
			CoroAI.dbg("retamed: " + ent);
		}
		

		removeTargetPlayer(ent);
		addFollowTask(ent, uuid);
		addTargetNonPetsTask(ent);
		ent.tasks.addTask(0, new EntityAIMisc(ent));
		//aiEnhanced.put(ent.getEntityId(), true);
		
		if (!ent.isChild()) {
			ent.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.25D);
		}
		
		ent.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(50);
		//FIRE IMMUNE NEED HERE
		OldUtil.setPrivateValueSRGMCP(Entity.class, ent, "field_70178_ae", "isImmuneToFire", true);
		//ent.set
		
		ent.addPotionEffect(new PotionEffect(Potion.regeneration.id, 10000, 1, false));
		ent.addPotionEffect(new PotionEffect(Potion.damageBoost.id, 10000, 1, false));
	}
	
	public static void removeTargetPlayer(EntityLiving parEnt) {
		try {
			for (int i = 0; i < parEnt.targetTasks.taskEntries.size(); i++) {
				EntityAITaskEntry entry = (EntityAITaskEntry) parEnt.targetTasks.taskEntries.get(i);
				if (entry.action instanceof EntityAINearestAttackableTarget) {
					Class clazz = (Class)OldUtil.getPrivateValueSRGMCP(EntityAINearestAttackableTarget.class, entry.action, "field_75307_b", "targetClass");
					if (EntityPlayer.class.isAssignableFrom(clazz) || EntityVillager.class.isAssignableFrom(clazz)) {
						System.out.println("removing target task for: " + clazz);
						parEnt.targetTasks.removeTask(entry.action);
						parEnt.setAttackTarget(null);
						i--;
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void addFollowTask(EntityCreature ent, UUID player) {
		EntityAIBase newTask = new EntityAIFollowOwner(ent, player, 1.0D, 5.0F, 2.0F);
		
		ent.tasks.addTask(4, newTask);
		
	}
	
	public static void addTargetNonPetsTask(EntityCreature ent) {
		EntityAIBase newTask = new EntityAIAttackHostilesOnCollide(ent, 1.5D, true);
		EntityAIBase newTargetTask = new EntityAINearestAttackableHostileTarget(ent, 0, true);
		//doesnt seem needed now, just make sure to add the attack task to melee users
		//if (ent instanceof IRangedAttackMob && ent instanceof EntitySkeleton) newTask = new EntityAIArrowAttack((IRangedAttackMob)ent, 0.25F, 20, 60, 15.0F);
		
		if (!(ent instanceof IRangedAttackMob)) {
			ent.tasks.addTask(3, newTask);
		}
		EntityAITasks targetTasks = ent.targetTasks;
		if (targetTasks != null) {
			targetTasks.addTask(2, newTargetTask);
		}
		
	}
	
	public static void reset() {
		aiEnhanced.clear();
	}*/
	
}
