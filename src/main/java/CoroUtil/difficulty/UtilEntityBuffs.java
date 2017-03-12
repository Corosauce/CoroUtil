package CoroUtil.difficulty;

import CoroUtil.ai.ITaskInitializer;
import CoroUtil.ai.tasks.TaskDigTowardsTarget;
import CoroUtil.difficulty.buffs.*;
import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DeserializerAllJson;
import CoroUtil.difficulty.data.cmodinventory.DataEntryInventoryTemplate;
import CoroUtil.difficulty.data.DifficultyDataReader;
import CoroUtil.difficulty.data.cmodmobdrops.DataEntryMobDropsTemplate;
import CoroUtil.forge.CoroUtil;
import CoroUtil.util.BlockCoord;
import CoroUtil.ai.tasks.EntityAITaskAntiAir;
import CoroUtil.ai.tasks.EntityAITaskEnhancedCombat;
import com.google.gson.JsonArray;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

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

    /*public static String dataEntityBuffed_AI_LungeAndCounterLeap = "CoroAI_HW_Buffed_AI_LungeAndCounterLeap";
    public static String dataEntityBuffed_AI_Digging = "CoroAI_HW_Buffed_AI_Digging";
    public static String dataEntityBuffed_AI_AntiAir = "CoroAI_HW_Buffed_AI_AntiAir";
    public static String dataEntityBuffed_AI_Infernal = "CoroAI_HW_Buffed_AI_Infernal";
    public static String dataEntityBuffed_Health = "CoroAI_HW_Buffed_Health";
    public static String dataEntityBuffed_Damage = "CoroAI_HW_Buffed_Damage";
    public static String dataEntityBuffed_Inventory = "CoroAI_HW_Buffed_Inventory";
    public static String dataEntityBuffed_Speed = "CoroAI_HW_Buffed_Speed";
    public static String dataEntityBuffed_XP = "CoroAI_HW_Buffed_XP";
    public static String dataEntityBuffed_MobDrops = "CoroAI_HW_Buffed_MobDrops";*/

    public static String dataEntityBuffed_AI_LungeAndCounterLeap = "ai_counterattack";
    public static String dataEntityBuffed_AI_Digging = "ai_mining";
    public static String dataEntityBuffed_AI_AntiAir = "ai_antiair";
    public static String dataEntityBuffed_AI_Infernal = "ai_infernal";
    public static String dataEntityBuffed_Health = "attribute_health";
    //public static String dataEntityBuffed_Damage = "CoroAI_HW_Buffed_Damage";
    public static String dataEntityBuffed_Inventory = "inventory";
    public static String dataEntityBuffed_Speed = "attribute_speed";
    public static String dataEntityBuffed_XP = "xp";
    public static String dataEntityBuffed_MobDrops = "mob_drops";

    public static String dataEntityBuffed_Data = "CoroAI_HW_Buffed_Data";
    public static String dataEntityCmodJson = "cmodjson";

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

        registerBuff(new BuffHealth());
        //registerBuff(new BuffSpeed());
        registerBuff(new BuffXP());
        registerBuff(new BuffMobDrops());
        registerBuff(new BuffInventory());
        registerBuff(new BuffAI_Infernal());
        registerBuff(new BuffAI_TaskMining(dataEntityBuffed_AI_Digging, TaskDigTowardsTarget.class, 5));
        registerBuff(new BuffAI_TaskBase(dataEntityBuffed_AI_AntiAir, EntityAITaskAntiAir.class, 3));
        registerBuff(new BuffAI_TaskBase(dataEntityBuffed_AI_LungeAndCounterLeap, EntityAITaskEnhancedCombat.class, 2, EntityAIZombieAttack.class));
    }

    /**
     * Currently overrides any pre-existing cmod data present
     *
     * @param ent
     * @param cmods
     * @param difficulty
     */
    public static void registerAndApplyCmods(EntityCreature ent, List<DataCmod> cmods, float difficulty) {

        List<DataCmod> cmodsFlat = DeserializerAllJson.getCmodsFlattened(cmods);

        if (!ent.getEntityData().hasKey(dataEntityBuffed_Data)) {
            ent.getEntityData().setTag(dataEntityBuffed_Data, new NBTTagCompound());
        }
        NBTTagCompound data = ent.getEntityData().getCompoundTag(dataEntityBuffed_Data);

        //add the json that holds config data for cmods
        JsonArray array = DeserializerAllJson.serializeCmods(cmodsFlat);
        data.setString(dataEntityCmodJson, array.toString());

        //apply the cmods via actual appliers, which also marks entity with easy to check cmod names
        for (DataCmod cmod : cmodsFlat) {
            applyBuff(cmod.cmod, ent, difficulty);
        }

        for (DataCmod cmod : cmodsFlat) {
            applyBuffPost(cmod.cmod, ent, difficulty);
        }

        //ent.getEntityData().setTag(dataEntityBuffed_Data, data);
    }

    public static List<DataCmod> getAllCmodData(EntityCreature ent) {
        NBTTagCompound data = ent.getEntityData().getCompoundTag(dataEntityBuffed_Data);
        String json = data.getString(dataEntityCmodJson);
        return DeserializerAllJson.deserializeCmods(json);
    }

    public static DataCmod getCmodData(EntityCreature ent, String cmodName) {
        NBTTagCompound data = ent.getEntityData().getCompoundTag(dataEntityBuffed_Data);
        String json = data.getString(dataEntityCmodJson);
        List<DataCmod> cmods = DeserializerAllJson.deserializeCmods(json);
        for (DataCmod cmod : cmods) {
            if (cmod.cmod.equals(cmodName)) {
                return cmod;
            }
        }
        return null;
    }

    public static void registerBuff(BuffBase buff) {
        lookupBuffs.put(buff.getTagName(), buff);
    }

    public static boolean hasBuff(EntityCreature ent, BuffBase buff) {
        return hasBuff(ent, buff.getTagName());
    }

    public static boolean hasBuff(EntityCreature ent, String buff) {
        NBTTagCompound data = ent.getEntityData().getCompoundTag(dataEntityBuffed_Data);
        return data.getBoolean(buff);
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
        NBTTagCompound data = ent.getEntityData().getCompoundTag(UtilEntityBuffs.dataEntityBuffed_Data);
        for (String buff : buffs) {
            if (data.getBoolean(buff)) {
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

        if (true) return;

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
            //applyBuff(UtilEntityBuffs.dataEntityBuffed_AI_Digging, ent, difficulty);
            //applyBuff(UtilEntityBuffs.dataEntityBuffed_AI_AntiAir, ent, difficulty);
            //applyBuff(dataEntityBuffed_AI_Infernal, ent, difficulty);
            applyBuff(dataEntityBuffed_Inventory, ent, difficulty);
            applyBuff(dataEntityBuffed_MobDrops, ent, difficulty);
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

    public static LootTable getRandomLootForDifficulty(EntityCreature ent, float difficulty) {
        List<DataEntryMobDropsTemplate> listForDifficulty = new ArrayList<>();

        /*for (DataEntryMobDropsTemplate entry : DifficultyDataReader.getData().listTemplatesMobDrops) {
            if (difficulty >= entry.level_min && difficulty <= entry.level_max) {
                listForDifficulty.add(entry);
            }
        }*/

        //TODO: do weighted random stuff here, for now just choose one at pure random
        if (listForDifficulty.size() > 0) {
            Random rand = new Random();
            int choice = rand.nextInt(listForDifficulty.size());

            DataEntryMobDropsTemplate entry = listForDifficulty.get(choice);

            LootTable loot = DifficultyDataReader.getData().lookupLootTables.get(entry.loot_table);

            if (loot != null) {
                return loot;
            } else {
                loot = ent.worldObj.getLootTableManager().getLootTableFromLocation(new ResourceLocation(entry.loot_table));
                if (loot != null) {
                    return loot;
                } else {
                    CoroUtil.dbg("couldnt find loot table: " + entry.loot_table);
                }

            }

        } else {
            CoroUtil.dbg("couldnt find loot to drop within difficulty range");
        }

        return null;
    }

    public static void processLootTableOnEntity(EntityCreature ent, LootTable loottable, LivingDeathEvent event) {
        LootContext.Builder lootcontext$builder = (new LootContext.Builder((WorldServer)ent.worldObj)).withLootedEntity(ent).withDamageSource(event.getSource());

        if (ent.recentlyHit > 0 && ent.attackingPlayer != null)
        {
            lootcontext$builder = lootcontext$builder.withPlayer(ent.attackingPlayer).withLuck(ent.attackingPlayer.getLuck());
        }

        for (ItemStack itemstack : loottable.generateLootForPools(ent.worldObj.rand, lootcontext$builder.build()))
        {
            ent.entityDropItem(itemstack, 0.0F);
        }
    }

    public static EquipmentForDifficulty getRandomEquipmentForDifficulty(float difficulty) {

        /**
         * TODO: a command to validate/test the json files that all the items exist
         * - though consider scnenario where they have a bunch of json files for mods that might not be installed
         * - i might want to ship mod with lots of json files with mods that they might install
         * - how to handle? maybe report if a range of difficulty lacks items?
         */




        //TODO: consider replacing EquipmentForDifficulty with DataEntryInventoryTemplate

        List<DataEntryInventoryTemplate> listEquipmentForDifficulty = new ArrayList<>();


        /*for (DataEntryInventoryTemplate entry : DifficultyDataReader.getData().listTemplatesInventory) {
            if (difficulty >= entry.level_min && difficulty <= entry.level_max) {
                listEquipmentForDifficulty.add(entry);
            }
        }*/

        //TODO: do weighted random stuff here, for now just choose one at pure random
        if (listEquipmentForDifficulty.size() > 0) {
            Random rand = new Random();
            int choice = rand.nextInt(listEquipmentForDifficulty.size());

            DataEntryInventoryTemplate entry = listEquipmentForDifficulty.get(choice);

            EquipmentForDifficulty equipment = new EquipmentForDifficulty();
            //TODO: handle this better?
            try {
                Item item = Item.getByNameOrId(entry.inv_hand_main);
                if (item != null) {
                    equipment.setWeapon(new ItemStack(item));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                Item item = Item.getByNameOrId(entry.inv_hand_off);
                if (item != null) {
                    equipment.setWeaponOffhand(new ItemStack(item));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            List<ItemStack> listArmor = new ArrayList<>();

            try {
                Item item = Item.getByNameOrId(entry.inv_head);
                if (item != null) {
                    listArmor.add(new ItemStack(item));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                Item item = Item.getByNameOrId(entry.inv_chest);
                if (item != null) {
                    listArmor.add(new ItemStack(item));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                Item item = Item.getByNameOrId(entry.inv_legs);
                if (item != null) {
                    listArmor.add(new ItemStack(item));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            try {
                Item item = Item.getByNameOrId(entry.inv_feet);
                if (item != null) {
                    listArmor.add(new ItemStack(item));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            equipment.setListArmor(listArmor);

            return equipment;
        } else {
            return null;
        }

    }

    public static void onDeath(LivingDeathEvent event) {

        if (event.getEntityLiving() instanceof EntityCreature) {
            EntityCreature ent = (EntityCreature)event.getEntityLiving();
            if (/*ent.canDropLoot() && */ent.worldObj.getGameRules().getBoolean("doMobLoot")) {

                if (ent.getEntityData().getBoolean(UtilEntityBuffs.dataEntityBuffed)) {
                    float difficultySpawnedIn = 0;
                    if (ent.getEntityData().hasKey(UtilEntityBuffs.dataEntityBuffed_Difficulty)) {
                        difficultySpawnedIn = ent.getEntityData().getFloat(UtilEntityBuffs.dataEntityBuffed_Difficulty);
                    } else {
                        //safely get difficulty for area
                        if (ent.worldObj.isBlockLoaded(ent.getPosition())) {
                            difficultySpawnedIn = DynamicDifficulty.getDifficultyAveragedForArea(ent);
                        }
                    }

                    List<String> buffs = UtilEntityBuffs.getAllBuffNames();
                    NBTTagCompound data = ent.getEntityData().getCompoundTag(UtilEntityBuffs.dataEntityBuffed_Data);
                    for (String buff : buffs) {
                        if (data.getBoolean(buff)) {
                            BuffBase buffObj = UtilEntityBuffs.getBuff(buff);
                            if (buffObj != null) {
                                //System.out.println("reloading buff: " + buff);
                                buffObj.applyBuffOnDeath(ent, difficultySpawnedIn, event);
                            } else {
                                CoroUtil.dbg("warning: unable to find buff by name of " + buff);
                            }
                        }
                    }
                }
            }
        }

    }
}
