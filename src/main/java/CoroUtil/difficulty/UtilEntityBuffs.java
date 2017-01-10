package CoroUtil.difficulty;

import CoroUtil.ai.BehaviorModifier;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.ai.tasks.TaskCallForHelp;
import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.CoroUtilCrossMod;
import CoroUtil.world.player.DynamicDifficulty;
import CoroUtil.ai.tasks.EntityAITaskAntiAir;
import CoroUtil.ai.tasks.EntityAITaskEnhancedCombat;
import CoroUtil.config.ConfigHWMonsters;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by Corosus on 1/8/2017.
 */
public class UtilEntityBuffs {



    public static String dataEntityBuffed_AI_CoroAI = "CoroAI_HW_Buffed_AI_CoroAI";
    public static String dataEntityBuffed_AI_Digging = "CoroAI_HW_Buffed_AI_Digging";

    //deprecating?
    public static String dataEntityBuffed_Tried = "CoroAI_HW_Buffed_AI_Tried";

    public static String dataEntityBuffed_AI_Infernal = "CoroAI_HW_Buffed_AI_Infernal";

    public static String dataEntityBuffed_Health = "CoroAI_HW_Buffed_Health";
    public static String dataEntityBuffed_Damage = "CoroAI_HW_Buffed_Damage";
    public static String dataEntityBuffed_Inventory = "CoroAI_HW_Buffed_Inventory";
    public static String dataEntityBuffed_Speed = "CoroAI_HW_Buffed_Speed";
    public static String dataEntityBuffed_XP = "CoroAI_HW_Buffed_XP";

    public static Class[] tasksToInject = new Class[] { EntityAITaskEnhancedCombat.class, EntityAITaskAntiAir.class };
    public static int[] taskPriorities = { 2, 3 };

    public static Class[] tasksToInjectInv = new Class[] { TaskDigTowardsTarget.class, TaskCallForHelp.class };
    public static int[] taskPrioritiesInv = {5, 5};

    public static double speedCap = 0.4D;

    public static HashMap<Integer, EquipmentForDifficulty> lookupDifficultyToEquipment = new HashMap<>();

    public static float inventoryStages = 5;

    static {
        EquipmentForDifficulty obj = new EquipmentForDifficulty();
        List<ItemStack> listItems = new ArrayList<ItemStack>();
        obj.setListArmor(listItems);
        lookupDifficultyToEquipment.put(0, obj);

        obj = new EquipmentForDifficulty();
        listItems = new ArrayList<ItemStack>();
        listItems.add(new ItemStack(Items.LEATHER_HELMET));
        listItems.add(new ItemStack(Items.LEATHER_CHESTPLATE));
        listItems.add(new ItemStack(Items.LEATHER_LEGGINGS));
        listItems.add(new ItemStack(Items.LEATHER_BOOTS));
        obj.setListArmor(listItems);
        obj.setWeapon(new ItemStack(Items.WOODEN_SWORD));
        lookupDifficultyToEquipment.put(1, obj);

        obj = new EquipmentForDifficulty();
        listItems = new ArrayList<ItemStack>();
        listItems.add(new ItemStack(Items.CHAINMAIL_HELMET));
        listItems.add(new ItemStack(Items.CHAINMAIL_CHESTPLATE));
        listItems.add(new ItemStack(Items.CHAINMAIL_LEGGINGS));
        listItems.add(new ItemStack(Items.CHAINMAIL_BOOTS));
        obj.setListArmor(listItems);
        obj.setWeapon(new ItemStack(Items.STONE_SWORD));
        lookupDifficultyToEquipment.put(2, obj);

        obj = new EquipmentForDifficulty();
        listItems = new ArrayList<ItemStack>();
        listItems.add(new ItemStack(Items.IRON_HELMET));
        listItems.add(new ItemStack(Items.IRON_CHESTPLATE));
        listItems.add(new ItemStack(Items.IRON_LEGGINGS));
        listItems.add(new ItemStack(Items.IRON_BOOTS));
        obj.setListArmor(listItems);
        obj.setWeapon(new ItemStack(Items.IRON_SWORD));
        lookupDifficultyToEquipment.put(3, obj);

        obj = new EquipmentForDifficulty();
        listItems = new ArrayList<ItemStack>();
        listItems.add(new ItemStack(Items.DIAMOND_HELMET));
        listItems.add(new ItemStack(Items.DIAMOND_CHESTPLATE));
        listItems.add(new ItemStack(Items.DIAMOND_LEGGINGS));
        listItems.add(new ItemStack(Items.DIAMOND_BOOTS));
        obj.setListArmor(listItems);
        obj.setWeapon(new ItemStack(Items.DIAMOND_SWORD));
        lookupDifficultyToEquipment.put(4, obj);
    }
    
    public static void buffGeneric(World world, EntityCreature ent, EntityPlayer playerClosest) {
        if (ent instanceof EntityZombie) {
						/*if (ConfigHWMonsters.antiAir) {
							BehaviorModifier.addTaskIfMissing(ent, TaskAntiAir.class, tasksToInject, taskPriorities[0]);
						}*/

            //note, there are 2 instances of attack on collide, we are targetting the first one that is for player
            //TODO: 1.10.2 verify going from EntityAIAttackOnCollide to EntityAIZombieAttack doesnt break things
            BehaviorModifier.replaceTaskIfMissing(ent, EntityAIZombieAttack.class, tasksToInject, taskPriorities);
        }

        if (!ent.getEntityData().getBoolean(dataEntityBuffed_AI_CoroAI)) {
            ent.getEntityData().setBoolean(dataEntityBuffed_AI_CoroAI, true);
            //BehaviorModifier.addTaskIfMissing(ent, TaskDigTowardsTarget.class, tasksToInject, taskPriorities[0]);

            float difficulty = DynamicDifficulty.getDifficultyScaleAverage(world, playerClosest, new BlockCoord(ent));

            /**
             * The mathematical behavior is as follows:
             * Operation 0: Increment X by Amount,
             * Operation 1: Increment Y by X * Amount,
             * Operation 2: Y = Y * (1 + Amount) (equivalent to Increment Y by Y * Amount).
             * The game first sets X = Base, then executes all Operation 0 modifiers, then sets Y = X,
             * then executes all Operation 1 modifiers, and finally executes all Operation 2 modifiers.
             */

            float maxHealthClean = Math.round(ent.getMaxHealth() * 1000F) / 1000F;
            //System.out.println("health max before: " + maxHealthClean);

            double healthBoostMultiply = (double)(/*1F + */difficulty * ConfigHWMonsters.scaleHealth);
            ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("health multiplier boost", healthBoostMultiply, 2));

            //chance to ignore knockback based on difficulty
            ent.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(difficulty * ConfigHWMonsters.scaleKnockbackResistance);

            String debug = "";

            double curSpeed = ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            //avoid retardedly fast speeds
            if (curSpeed < speedCap) {
                double speedBoost = (Math.min(ConfigHWMonsters.scaleSpeedCap, difficulty * ConfigHWMonsters.scaleSpeed));
                debug += "speed % " + speedBoost;
                ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier("speed multiplier boost", speedBoost, 2));
            }

            debug += ", new speed: " + ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
            //System.out.println("mobs final speed: " + ent.getEntityAttribute(SharedMonsterAttributes.movementSpeed).getAttributeValue());
            //System.out.println("difficulty: " + difficulty);
            //System.out.println("hb %: " + healthBoostMultiply);
            maxHealthClean = Math.round(ent.getMaxHealth() * 1000F) / 1000F;
            //System.out.println("health max: " + maxHealthClean);

            debug += ", health boost: " + healthBoostMultiply;

            ent.setHealth(ent.getMaxHealth());

            debug += ", new health: " + maxHealthClean;

            //System.out.println(debug);
        }
    }

    /**
     * Randomly decide how many aspects to buff, and by how much based on difficulty and config
     *
     * @param world
     * @param ent
     * @param playerClosest
     */
    public static void buff_RollDice(World world, EntityCreature ent, EntityPlayer playerClosest) {

        /**
         * number of buffs to add depends on difficulty
         * try to add random buff, if already added, reroll to try another
         */

        //temp
        int amountOfBuffs = 2;
        int remainingBuffs = amountOfBuffs;
        Random rand = new Random();

        float difficulty = DynamicDifficulty.getDifficultyScaleAverage(world, playerClosest, new BlockCoord(ent));

        //until i go more object oriented, a buff can return false to say it didnt properly apply, and still set the tag to prevent retrying
        //- this is so it wont keep retrying that buff but wont decrement remainingBuffs

        while (remainingBuffs > 0) {
            int randVal = rand.nextInt(6);
            if (randVal == 0) {
                if (!ent.getEntityData().getBoolean(dataEntityBuffed_Health)) {
                    if (buffHealth(world, ent, playerClosest, difficulty)) {
                        remainingBuffs--;
                    }
                }
            } else if (randVal == 1) {
                if (!ent.getEntityData().getBoolean(dataEntityBuffed_Damage)) {
                    if (buffDamage(world, ent, playerClosest, difficulty)) {
                        remainingBuffs--;
                    }
                }
            } else if (randVal == 2) {
                if (!ent.getEntityData().getBoolean(dataEntityBuffed_Inventory)) {
                    if (buffInventory(world, ent, playerClosest, difficulty)) {
                        remainingBuffs--;
                    }
                }
            } else if (randVal == 3) {
                if (!ent.getEntityData().getBoolean(dataEntityBuffed_Speed)) {
                    if (buffSpeed(world, ent, playerClosest, difficulty)) {
                        remainingBuffs--;
                    }
                }
            } else if (randVal == 4) {
                if (!ent.getEntityData().getBoolean(dataEntityBuffed_AI_CoroAI)) {
                    if (buffAI_CoroAI_Combat(world, ent, playerClosest, difficulty)) {
                        remainingBuffs--;
                    }
                }
            } else if (randVal == 5) {
                if (!ent.getEntityData().getBoolean(dataEntityBuffed_AI_Infernal)) {
                    if (buffAI_Infernal(world, ent, playerClosest, difficulty)) {
                        remainingBuffs--;
                    }
                }
            }

            //endless loop protection - check if it has all buffs
            //keep in mind AI buff can fail and use only the tried tag
            if (ent.getEntityData().getBoolean(dataEntityBuffed_Health) &&
                    ent.getEntityData().getBoolean(dataEntityBuffed_Damage) &&
                    ent.getEntityData().getBoolean(dataEntityBuffed_Inventory) &&
                    ent.getEntityData().getBoolean(dataEntityBuffed_Speed) &&
                    ent.getEntityData().getBoolean(dataEntityBuffed_AI_CoroAI) &&
                    ent.getEntityData().getBoolean(dataEntityBuffed_AI_Infernal)) {
                break;
            }
        }


    }

    public static boolean buffHealth(World world, EntityCreature ent, EntityPlayer playerClosest, float difficulty) {

        double healthBoostMultiply = (/*1F + */difficulty * ConfigHWMonsters.scaleHealth);
        ent.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(new AttributeModifier("health multiplier boost", healthBoostMultiply, 2));

        //group with health buff for now...
        ent.getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(difficulty * ConfigHWMonsters.scaleKnockbackResistance);

        ent.getEntityData().setBoolean(dataEntityBuffed_Health, true);

        return true;
    }

    public static boolean buffDamage(World world, EntityCreature ent, EntityPlayer playerClosest, float difficulty) {


        ent.getEntityData().setBoolean(dataEntityBuffed_Damage, true);

        //TODO: decide if we will use this

        return false;
    }

    /**
     * Inventory using bipeds only!
     *
     * @param world
     * @param ent
     * @param playerClosest
     */
    public static boolean buffInventory(World world, EntityCreature ent, EntityPlayer playerClosest, float difficulty) {

        ent.getEntityData().setBoolean(dataEntityBuffed_Inventory, true);

        int inventoryStage = getInventoryStageBuff(difficulty);

        EquipmentForDifficulty equipment = lookupDifficultyToEquipment.get(inventoryStage);
        if (equipment != null) {
            //allow for original weapon to remain if there was one and we are trying to remove it
            if (equipment.getWeapon() != null) setEquipment(ent, EntityEquipmentSlot.MAINHAND, equipment.getWeapon());
            //ent.setCurrentItemOrArmor(0, equipment.getWeapon());
            for (int i = 0; i < 4; i++) {
                //TODO: verify 1.10.2 update didnt mess with this, maybe rewrite a bit for new sane slot based system
                if (equipment.getListArmor().size() >= i+1) {
                    setEquipment(ent, equipment.getSlotForSlotID(i)/*i+1*/, equipment.getListArmor().get(i));
                    //ent.setCurrentItemOrArmor(i+1, equipment.getListArmor().get(i));
                } else {
                    setEquipment(ent, equipment.getSlotForSlotID(i)/*i+1*/, null);
                    //ent.setCurrentItemOrArmor(i+1, null);

                }
            }

        } else {
            System.out.println("error, couldnt find equipment for difficulty value: " + inventoryStage);
        }

        return true;
    }

    public static boolean buffSpeed(World world, EntityCreature ent, EntityPlayer playerClosest, float difficulty) {

        double curSpeed = ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue();
        //avoid retardedly fast speeds
        if (curSpeed < speedCap) {
            double speedBoost = (Math.min(ConfigHWMonsters.scaleSpeedCap, difficulty * ConfigHWMonsters.scaleSpeed));
            //debug += "speed % " + speedBoost;
            ent.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier("speed multiplier boost", speedBoost, 2));
        }

        ent.getEntityData().setBoolean(dataEntityBuffed_Speed, true);
        return true;
    }

    public static boolean buffXP(World world, EntityCreature ent, EntityPlayer playerClosest, float difficulty) {

        ent.getEntityData().setBoolean(dataEntityBuffed_XP, true);

        try {
            int xp = ObfuscationReflectionHelper.getPrivateValue(EntityLiving.class, ent, "field_70728_aV", "experienceValue");
            xp += difficulty * 10F;
            ObfuscationReflectionHelper.setPrivateValue(EntityLiving.class, ent, xp, "field_70728_aV", "experienceValue");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean buffAI_CoroAI_Digging(World world, EntityCreature ent, EntityPlayer playerClosest, float difficulty) {

        ent.getEntityData().setBoolean(dataEntityBuffed_AI_Digging, true);


        //do dig task only, not call for help, move that elsewhere, generic ai combat buff maybe
        if (!hasTask(ent, tasksToInjectInv[0])) {
            addTask(ent, tasksToInjectInv[0], taskPrioritiesInv[0]);
        }

        /*for (Class clazz : tasksToInjectInv) {
            addTask(ent, clazz, taskPrioritiesInv[0]);
        }*/

        return true;
    }

    public static boolean buffAI_CoroAI_Combat(World world, EntityCreature ent, EntityPlayer playerClosest, float difficulty) {

        //TODO: make it so the tasks themselves dont have rand chance, have it be on if they are added so this method called means they 100% are either used or not used

        ent.getEntityData().setBoolean(dataEntityBuffed_AI_CoroAI, true);

        if (ent instanceof EntityZombie) {
						/*if (ConfigHWMonsters.antiAir) {
							BehaviorModifier.addTaskIfMissing(ent, TaskAntiAir.class, tasksToInject, taskPriorities[0]);
						}*/

            //note, there are 2 instances of attack on collide, we are targetting the first one that is for player
            //TODO: 1.10.2 verify going from EntityAIAttackOnCollide to EntityAIZombieAttack doesnt break things
            BehaviorModifier.replaceTaskIfMissing(ent, EntityAIZombieAttack.class, tasksToInject, taskPriorities);
        } else {
            return false;
        }


        return true;
    }

    public static boolean buffAI_Infernal(World world, EntityCreature ent, EntityPlayer playerClosest, float difficulty) {

        ent.getEntityData().setBoolean(dataEntityBuffed_AI_Infernal, true);

        if (!CoroUtilCrossMod.hasInfernalMobs()) return false;

        //how many modifiers do we add? based on difficulty i guess, x2!
        return CoroUtilCrossMod.infernalMobs_AddRandomModifiers(ent, (int)(difficulty * 2D));
    }

    /**
     * Migrated methods from invasion mod
     */

    public static int getInventoryStageBuff(float difficultyScale) {
        //clamp difficulty between 0 and 1 until we expand on this equipment more
        float scaleCap = MathHelper.clamp_float(difficultyScale, 0F, 1F);
        float scaleDivide = 1F / inventoryStages;
        int inventoryStage = 0;
        for (int i = 0; i < inventoryStages; i++) {
            if (scaleCap <= scaleDivide * (i+1)) {
                inventoryStage = i;
                break;
            }
        }
        return inventoryStage;
    }

    public static void setEquipment(EntityCreature ent, EntityEquipmentSlot slot/*int slot*/, ItemStack stack) {
        if (slot == EntityEquipmentSlot.MAINHAND/*slot == 0*/ && ent instanceof EntitySkeleton) {
            return;
        }
		/*ent.setCurrentItemOrArmor(slot, stack);
		ent.setEquipmentDropChance(slot, 0);*/
        ent.setItemStackToSlot(slot, stack);
        ent.setDropChance(slot, 0);

    }

    public static boolean hasTask(EntityCreature ent, Class taskToCheckFor) {
        boolean foundTask = false;
        for (Object entry2 : ent.tasks.taskEntries) {
            EntityAITasks.EntityAITaskEntry entry = (EntityAITasks.EntityAITaskEntry) entry2;
            if (taskToCheckFor.isAssignableFrom(entry.action.getClass())) {
                foundTask = true;
                break;
            }
        }
        return foundTask;
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
    
}