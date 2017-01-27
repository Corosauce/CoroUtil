package CoroUtil.difficulty;

import CoroUtil.ai.ITaskInitializer;
import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.difficulty.buffs.*;
import CoroUtil.forge.CoroUtil;
import CoroUtil.util.BlockCoord;
import CoroUtil.ai.tasks.EntityAITaskAntiAir;
import CoroUtil.ai.tasks.EntityAITaskEnhancedCombat;
import CoroUtil.config.ConfigHWMonsters;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.ISimulationTickable;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Created by Corosus on 1/8/2017.
 */
public class UtilEntityBuffs {

    public static HashMap<String, BuffBase> lookupBuffs = new HashMap<>();

    //used to flag if entity is buffed at all, helps with checking if difficulty was cached, and other efficiencies
    public static String dataEntityBuffed = "CoroAI_HW_Buffed";
    //used for flagging we tried to buff, doesnt mean hes actually buffed or not, just that we gave him a chance to be buffed
    public static String dataEntityBuffDiceRolled = "CoroAI_HW_BuffDiceRolled";
    //cached difficulty for when he was buffed, for reloading from disk
    public static String dataEntityBuffed_Difficulty = "CoroAI_HW_Difficulty";

    //deprecating?
    public static String dataEntityBuffed_Tried = "CoroAI_HW_Buffed_AI_Tried";

    //consider moving these buff name fields to their own class for easy reference

    public static String dataEntityBuffed_AI_LungeAndCounterLeap = "CoroAI_HW_Buffed_AI_LungeAndCounterLeap";
    public static String dataEntityBuffed_AI_Digging = "CoroAI_HW_Buffed_AI_Digging";
    public static String dataEntityBuffed_AI_AntiAir = "CoroAI_HW_Buffed_AI_AntiAir";
    public static String dataEntityBuffed_AI_Infernal = "CoroAI_HW_Buffed_AI_Infernal";
    public static String dataEntityBuffed_Health = "CoroAI_HW_Buffed_Health";
    public static String dataEntityBuffed_Damage = "CoroAI_HW_Buffed_Damage";
    public static String dataEntityBuffed_Inventory = "CoroAI_HW_Buffed_Inventory";
    public static String dataEntityBuffed_Speed = "CoroAI_HW_Buffed_Speed";
    public static String dataEntityBuffed_XP = "CoroAI_HW_Buffed_XP";

    /**
     * Flags from invasion mod for its own dice rolling
     */

    public static String dataEntityEnhanced = "CoroAI_HW_Inv_Enhanced";
    public static String dataEntityEnhanceTried = "CoroAI_HW_Inv_EnhanceTried";
    public static String dataEntityWaveSpawned = "CoroAI_HW_Inv_WaveSpawned";

    //use for buffs that say they can apply but failed to apply
    //do we need it?
    public static String dataFlagFailed = "_Failed";

    //public static String dataFlagApplied = "_Applied";

    //public static Class[] tasksToInject = new Class[] { EntityAITaskEnhancedCombat.class, EntityAITaskAntiAir.class };
    //public static int[] taskPriorities = { 2, 3 };

    //public static Class[] tasksToInjectInv = new Class[] { TaskDigTowardsTarget.class, TaskCallForHelp.class };
    //public static int[] taskPrioritiesInv = {5, 5};

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

        addBuff(new BuffHealth());
        //addBuff(new BuffSpeed());
        addBuff(new BuffXP());
        addBuff(new BuffInventory());
        addBuff(new BuffAI_Infernal());
        addBuff(new BuffAI_TaskMining(dataEntityBuffed_AI_Digging, TaskDigTowardsTarget.class, 5));
        addBuff(new BuffAI_TaskBase(dataEntityBuffed_AI_AntiAir, EntityAITaskAntiAir.class, 3));
        addBuff(new BuffAI_TaskBase(dataEntityBuffed_AI_LungeAndCounterLeap, EntityAITaskEnhancedCombat.class, 2, EntityAIZombieAttack.class));
    }

    public static void addBuff(BuffBase buff) {
        lookupBuffs.put(buff.getTagName(), buff);
    }

    public static boolean hasBuff(EntityCreature ent, BuffBase buff) {
        return hasBuff(ent, buff.getTagName());
    }

    public static boolean hasBuff(EntityCreature ent, String buff) {
        return ent.getEntityData().getBoolean(buff);
    }

    /**
     * Non batch based buff applying, checks if buff was not already applied, then calls applyBuff and applyBuffPost
     *
     * @param buffName
     * @param ent
     * @param difficulty
     * @return
     */
    public static boolean applyBuffSingularTry(String buffName, EntityCreature ent, float difficulty) {
        if (!hasBuff(ent, buffName)) {
            BuffBase buff = getBuff(buffName);
            if (buff != null) {
                if (buff.canApplyBuff(ent, difficulty)) {
                    if (!applyBuff(buffName, ent, difficulty)) {
                        return false;
                    } else {
                        applyBuffPost(buffName, ent, difficulty);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * All non reloading methods must use this to make sure difficulty cache is set
     *
     * @param buffName
     * @param ent
     * @param difficulty
     * @return
     */
    public static boolean applyBuff(String buffName, EntityCreature ent, float difficulty) {
        if (lookupBuffs.containsKey(buffName)) {

            //mark entity is buffed
            ent.getEntityData().setBoolean(dataEntityBuffed, true);

            //store difficulty for reloading buffs later
            if (!ent.getEntityData().hasKey(dataEntityBuffed_Difficulty)) {
                ent.getEntityData().setFloat(dataEntityBuffed_Difficulty, difficulty);
            }


            //System.out.println("applying buff: " + buffName);

            return lookupBuffs.get(buffName).applyBuff(ent, difficulty);
        } else {
            return false;
        }
    }

    /**
     *
     * @param buffName
     * @param ent
     * @param difficulty
     * @return
     */
    public static void applyBuffPost(String buffName, EntityCreature ent, float difficulty) {
        if (lookupBuffs.containsKey(buffName)) {

            //System.out.println("applying buff: " + buffName);

            lookupBuffs.get(buffName).applyBuffPost(ent, difficulty);
        }
    }

    public static void applyBuffPostAll(EntityCreature ent, float difficulty) {
        List<String> buffs = UtilEntityBuffs.getAllBuffNames();
        for (String buff : buffs) {
            if (ent.getEntityData().getBoolean(buff)) {
                BuffBase buffObj = UtilEntityBuffs.getBuff(buff);
                if (buffObj != null) {
                    //System.out.println("applyBuffPostAll buff: " + buff);
                    buffObj.applyBuffPost(ent, difficulty);
                } else {
                    CoroUtil.dbg("warning: unable to find buff by name of " + buff);
                }
            }
        }
    }

    public static List<String> getAllBuffNames() {
        return new ArrayList<>(lookupBuffs.keySet());
    }

    public static BuffBase getBuff(String buff) {
        return lookupBuffs.get(buff);
    }

    /**
     * Randomly decide how many aspects to buff, and by how much based on difficulty and config
     *
     * @param world
     * @param ent
     * @param playerClosest
     */
    public static void buff_RollDice(World world, EntityCreature ent, EntityPlayer playerClosest) {

        //we already gave him a chance to get buffs, abort
        if (ent.getEntityData().getBoolean(dataEntityBuffDiceRolled)) return;

        /**
         * number of buffs to add depends on difficulty
         * try to add random buff, if already added, reroll to try another
         */

        //temp
        int amountOfBuffs = 2;
        int remainingBuffs = amountOfBuffs;
        Random rand = new Random();

        float difficulty = DynamicDifficulty.getDifficultyScaleAverage(world, playerClosest, new BlockCoord(ent));

        //NEW!

        //TODO: filter buffs by minimum required difficulty level for better tiered progression
        List<String> listBuffs = UtilEntityBuffs.getAllBuffNames();
        Collections.shuffle(listBuffs);

        ent.getEntityData().setBoolean(dataEntityBuffDiceRolled, true);

        boolean testSpecific = true;

        //TEMP
        if (testSpecific) {
            applyBuff(UtilEntityBuffs.dataEntityBuffed_AI_Digging, ent, difficulty);
            //applyBuff(UtilEntityBuffs.dataEntityBuffed_AI_AntiAir, ent, difficulty);
            //applyBuff(dataEntityBuffed_AI_Infernal, ent, difficulty);
        } else {
            for (String buff : listBuffs) {
                if (remainingBuffs > 0) {
                    if (!hasBuff(ent, buff)) {
                        if (getBuff(buff).canApplyBuff(ent, difficulty)) {
                            //use main method that also marks entity buffed and caches difficulty
                            if (applyBuff(buff, ent, difficulty)) {
                                remainingBuffs--;
                            }
                        }
                    }
                } else {
                    break;
                }
            }
        }

        //TODO: if there was a buff before this method body, this might be calling the post call redundantly
        UtilEntityBuffs.applyBuffPostAll(ent, difficulty);
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
