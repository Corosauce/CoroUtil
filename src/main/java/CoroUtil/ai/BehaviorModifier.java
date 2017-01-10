package CoroUtil.ai;

import java.lang.reflect.Constructor;
import java.util.List;

import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks.EntityAITaskEntry;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import CoroUtil.util.Vec3;

public class BehaviorModifier {
	
	//pet mod design notes/ideas
	
	//need registry to mark and looking existing pets against
	//- used for targetting non pets
	//- used for fixing active fights between pets (skeletons line of fire incidents)
	//- used for invoking enemy target pets tasks 
	
	//entityid
	//public static HashMap<Integer, Boolean> aiEnhanced = new HashMap<Integer, Boolean>();
	
	public static void enhanceZombies(World parWorld, Vec3 parPos, Class[] taskToInject, int priorityOfTask, int modifyRange/*, float chanceToEnhance*/) {
		
		
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
        		if (!ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityBuffed_Tried)) {
        			
        			enhanceCountTry++;

        			ent.getEntityData().setBoolean(UtilEntityBuffs.dataEntityBuffed_Tried, true);
        			
        			//if (parWorld.rand.nextFloat() < chanceToEnhance) {
        			
	        			if (!ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityBuffed_AI_CoroAI)) {
	            			for (Class clazz : taskToInject) {
	    		        		addTask(ent, clazz, priorityOfTask);
	            			}
	            			
	            			enhanceCount++;
	            			performExtraChanges(ent);
	            		}
        			//}
        		} else {
        			//System.out.println("already tried to enhance on this entity");
        		}
        	}
        }
        
        //System.out.println("enhanced " + enhanceCount + " of " + enhanceCountTry + " entities");
	}
	
	public static boolean addTaskIfMissing(EntityCreature ent, Class taskToCheckFor, Class[] taskToInject, int priorityOfTask) {
		boolean foundTask = false;
		for (Object entry2 : ent.tasks.taskEntries) {
			EntityAITaskEntry entry = (EntityAITaskEntry) entry2;
			if (taskToCheckFor.isAssignableFrom(entry.action.getClass())) {
				foundTask = true;
				break;
			}
		}
		
		if (!foundTask) {
			//System.out.println("HW-M: Detected entity was recreated and missing tasks, readding tasks and changes");
			for (Class clazz : taskToInject) {
				addTask(ent, clazz, priorityOfTask);
			}
			performExtraChanges(ent);
		} else {
			//temp output to make sure detection works
			//System.out.println("already has task!");
		}
		
		return !foundTask;
		
	}
	
	public static boolean replaceTaskIfMissing(EntityCreature ent, Class taskToReplace, Class[] tasksToReplaceWith, int[] priorityOfTask) {
		EntityAITaskEntry foundTask = null;
		for (Object entry2 : ent.tasks.taskEntries) {
			EntityAITaskEntry entry = (EntityAITaskEntry) entry2;
			if (taskToReplace.isAssignableFrom(entry.action.getClass())) {
				foundTask = entry;
				break;
			}
		}
		
		if (foundTask != null) {
			ent.tasks.taskEntries.remove(foundTask);
			
			for (int i = 0; i < tasksToReplaceWith.length; i++) {
				addTask(ent, tasksToReplaceWith[i], priorityOfTask[i]);
			}
		}
		
		return foundTask != null;
		
	}
	
	public static boolean addTask(EntityCreature ent, Class taskToInject, int priorityOfTask) {
		try {
			Constructor<?> cons = taskToInject.getConstructor();
			Object obj = cons.newInstance();
			if (obj instanceof ITaskInitializer) {
				ITaskInitializer task = (ITaskInitializer) obj;
				task.setEntity(ent);
				//System.out.println("adding task into zombie: " + taskToInject);
				ent.tasks.addTask(priorityOfTask, (EntityAIBase) task);
				//aiEnhanced.put(ent.getEntityId(), true);
				
				
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static void performExtraChanges(EntityCreature ent) {
		/*ent.getNavigator().setBreakDoors(false);
		//((PathNavigateGround)ent.getNavigator()).setBreakDoors(false);
		ent.getEntityData().setBoolean(dataEntityBuffed_AI_CoroAI, true);
		ent.getEntityData().setBoolean("CoroAI_HW_GravelDeath", true);
		if (ent.getEquipmentInSlot(0) == null) {
			EventHandlerForge.setEquipment(ent, 0, new ItemStack(Items.iron_pickaxe));
		} else {
			if (ent.getEquipmentInSlot(0).getItem() == Items.wooden_sword) {
				EventHandlerForge.setEquipment(ent, 0, new ItemStack(Items.wooden_pickaxe));
			} else if (ent.getEquipmentInSlot(0).getItem() == Items.stone_sword) {
				EventHandlerForge.setEquipment(ent, 0, new ItemStack(Items.stone_pickaxe));
			} else if (ent.getEquipmentInSlot(0).getItem() == Items.iron_sword) {
				EventHandlerForge.setEquipment(ent, 0, new ItemStack(Items.iron_pickaxe));
			} else if (ent.getEquipmentInSlot(0).getItem() == Items.diamond_sword) {
				EventHandlerForge.setEquipment(ent, 0, new ItemStack(Items.diamond_pickaxe));
			}
		}*/
	}
	/*

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
