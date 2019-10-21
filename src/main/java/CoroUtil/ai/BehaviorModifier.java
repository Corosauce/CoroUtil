package CoroUtil.ai;

import java.lang.reflect.Constructor;
import java.util.List;

import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.monster.ZombieEntity;
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
		aabb = aabb.grow(modifyRange, modifyRange, modifyRange);
		List list = parWorld.getEntitiesWithinAABB(ZombieEntity.class, aabb);
		
		int enhanceCount = 0;
		int enhanceCountTry = 0;
		
        for(int j = 0; j < list.size(); j++)
        {
        	CreatureEntity ent = (CreatureEntity)list.get(j);
            
        	if (ent != null && !ent.isDead) {
        		//if (!aiEnhanced.containsKey(ent.getEntityId())) {
        		//log that we've tried to enhance with chance already, prevent further attempts to avoid stacking the odds per call on this method
        		if (!ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityBuffed_Tried)) {
        			
        			enhanceCountTry++;

        			ent.getEntityData().putBoolean(UtilEntityBuffs.dataEntityBuffed_Tried, true);
        			
        			//if (parWorld.rand.nextFloat() < chanceToEnhance) {
        			
	        			if (!ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityBuffed_AI_CounterLeap)) {
	            			for (Class clazz : taskToInject) {
	    		        		addGoal(ent, clazz, priorityOfTask, false);
	            			}
	            			
	            			enhanceCount++;
	            		}
        			//}
        		} else {
        			//System.out.println("already tried to enhance on this entity");
        		}
        	}
        }
        
        //System.out.println("enhanced " + enhanceCount + " of " + enhanceCountTry + " entities");
	}
	
	public static boolean addTaskIfMissing(CreatureEntity ent, Class taskToCheckFor, Class[] taskToInject, int priorityOfTask, boolean isTargetTask) {
		boolean foundTask = false;

		GoalSelector tasks = ent.tasks;
		if (isTargetTask) {
			tasks = ent.targetTasks;
		}

		for (Object entry2 : tasks.taskEntries) {
			Goal entry = (Goal) entry2;
			if (taskToCheckFor.isAssignableFrom(entry.action.getClass())) {
				foundTask = true;
				break;
			}
		}
		
		if (!foundTask) {
			//System.out.println("HW-M: Detected entity was recreated and missing tasks, readding tasks and changes");
			for (Class clazz : taskToInject) {
				addGoal(ent, clazz, priorityOfTask, isTargetTask);
			}
		} else {
			//temp output to make sure detection works
			//System.out.println("already has task!");
		}
		
		return !foundTask;
		
	}

	public static boolean replaceTaskIfMissing(CreatureEntity ent, Class taskToReplace, Class tasksToReplaceWith, int priorityOfTask, boolean isTargetTask) {
		Goal foundTask = null;

		GoalSelector tasks = ent.tasks;
		if (isTargetTask) {
			tasks = ent.targetTasks;
		}

		for (Object entry2 : tasks.taskEntries) {
			Goal entry = (Goal) entry2;
			if (taskToReplace.isAssignableFrom(entry.action.getClass())) {
				foundTask = entry;
				break;
			}
		}

		if (foundTask != null) {
			tasks.taskEntries.remove(foundTask);

			addGoal(ent, tasksToReplaceWith, priorityOfTask, isTargetTask);
		}

		return foundTask != null;

	}
	
	public static boolean replaceTaskIfMissing(CreatureEntity ent, Class taskToReplace, Class[] tasksToReplaceWith, int[] priorityOfTask, boolean isTargetTask) {
		Goal foundTask = null;

		GoalSelector tasks = ent.tasks;
		if (isTargetTask) {
			tasks = ent.targetTasks;
		}

		for (Object entry2 : tasks.taskEntries) {
			Goal entry = (Goal) entry2;
			if (taskToReplace.isAssignableFrom(entry.action.getClass())) {
				foundTask = entry;
				break;
			}
		}
		
		if (foundTask != null) {
			tasks.taskEntries.remove(foundTask);
			
			for (int i = 0; i < tasksToReplaceWith.length; i++) {
				addGoal(ent, tasksToReplaceWith[i], priorityOfTask[i], isTargetTask);
			}
		}
		
		return foundTask != null;
		
	}
	
	public static boolean addGoal(CreatureEntity ent, Class taskToInject, int priorityOfTask, boolean isTargetTask) {
		try {

			GoalSelector tasks = ent.tasks;
			if (isTargetTask) {
				tasks = ent.targetTasks;
			}

			Constructor<?> cons = taskToInject.getConstructor();
			Object obj = cons.newInstance();
			if (obj instanceof ITaskInitializer) {
				ITaskInitializer task = (ITaskInitializer) obj;
				task.setEntity(ent);
				//System.out.println("adding task into zombie: " + taskToInject);
				tasks.addGoal(priorityOfTask, (Goal) task);
				//aiEnhanced.put(ent.getEntityId(), true);
				
				
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
}

