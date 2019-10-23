package CoroUtil.difficulty;

import CoroUtil.ai.ITaskInitializer;
import CoroUtil.difficulty.buffs.*;
import CoroUtil.difficulty.data.DataCmod;
import CoroUtil.difficulty.data.DeserializerAllJson;
import CoroUtil.difficulty.data.cmods.CmodInventory;
import CoroUtil.difficulty.data.cmods.CmodInventoryDifficultyScaled;
import CoroUtil.difficulty.data.cmods.CmodInventoryEntry;
import CoroUtil.forge.CULog;
import CoroUtil.forge.CoroUtil;
import CoroUtil.util.BlockCoord;
import com.google.gson.JsonArray;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
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

    //storing the player it spawned for, so we can make things like omniscience only work for specific player
    //- important since we want other players to be able to opt out and be mostly safe
    public static String dataEntityBuffed_PlayerSpawnedFor = "CoroAI_HW_PlayerSpawnedFor";

    public static String dataEntityBuffed_LastTimePathfindLongDist = "CoroAI_LastTimePathfindLongDist";

    //deprecating?
    public static String dataEntityBuffed_Tried = "CoroAI_HW_Buffed_AI_Tried";
    /**
     * THESE MUST MATCH JSON DATA NAMES
     */

    public static String dataEntityBuffed_AI_CounterLeap = "ai_counterattack";
    public static String dataEntityBuffed_AI_Lunge = "ai_lunge";
    public static String dataEntityBuffed_AI_Hoist = "ai_hoist";
    public static String dataEntityBuffed_AI_Digging = "ai_mining";
    public static String dataEntityBuffed_AI_ExplodeOnStuck = "ai_explodeonstuck";
    public static String dataEntityBuffed_AI_Omniscience = "ai_omniscience";
    public static String dataEntityBuffed_AI_AntiAir = "ai_antiair";
    public static String dataEntityBuffed_AI_Infernal = "ai_infernal";
    public static String dataEntityBuffed_AI_Attack_Melee = "ai_attack_melee";
    public static String dataEntityBuffed_Health = "attribute_health";
    public static String dataEntityBuffed_AttackDamage = "attribute_attackdamage";
    //public static String dataEntityBuffed_Damage = "CoroAI_HW_Buffed_Damage";
    public static String dataEntityBuffed_Inventory = "inventory";
    public static String dataEntityBuffed_InventoryDifficultyScaled = "inventory_difficulty_scaled";
    public static String dataEntityBuffed_Speed = "attribute_speed";
    public static String dataEntityBuffed_Speed_Flying = "attribute_speed_flying";
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
    public static String dataEntityInitialSpawn = "CoroAI_HW_InitialSpawn";

    public static double speedCap = 0.4D;

    /**
     * Currently overrides any pre-existing cmod data present
     *
     * @param ent
     * @param cmods
     * @param difficulty
     */
    public static void registerAndApplyCmods(CreatureEntity ent, List<DataCmod> cmods, float difficulty) {

        List<DataCmod> cmodsFlat = DeserializerAllJson.getCmodsFlattened(cmods);

        //now lets process inventory_difficulty_scaled into just inventory
        //tick: this was a good idea and saved me having to write a serializer for it, but i forgot i still need to serialize the full thing for world reloading
        //might be worth keeping for the entity nbt to keep it slimmer, technically saves disk space
        boolean flattenConditionalInventory = false;
        if (flattenConditionalInventory) {
            for (ListIterator<DataCmod> it = cmodsFlat.listIterator(); it.hasNext(); ) {
                DataCmod cmod = it.next();
                if (cmod instanceof CmodInventoryDifficultyScaled) {
                    CmodInventory cmodInventory = null;
                    for (CmodInventoryEntry entry : ((CmodInventoryDifficultyScaled) cmod).listInventories) {
                        if (difficulty >= entry.min && difficulty <= entry.max) {
                            cmodInventory = entry.inventory;
                            break;
                        }
                    }
                    //replace with the actual inventory determined to be used
                    if (cmodInventory != null) {
                        it.set(cmodInventory);
                    }
                }
            }
        }

        if (!ent.getPersistentData().contains(dataEntityBuffed_Data)) {
            ent.getPersistentData().put(dataEntityBuffed_Data, new CompoundNBT());
        }
        CompoundNBT data = ent.getPersistentData().getCompound(dataEntityBuffed_Data);

        //add the json that holds config data for cmods
        JsonArray array = DeserializerAllJson.serializeCmods(cmodsFlat);
        data.putString(dataEntityCmodJson, array.toString());

        //apply the cmods via actual appliers, which also marks entity with easy to check cmod names
        for (DataCmod cmod : cmodsFlat) {
            CULog.dbg("applyBuff: " + cmod.cmod);
            applyBuff(cmod.cmod, ent, difficulty);
        }

        for (DataCmod cmod : cmodsFlat) {
            CULog.dbg("applyBuffPost: " + cmod.cmod);
            applyBuffPost(cmod.cmod, ent, difficulty);
        }

        //ent.getPersistentData().put(dataEntityBuffed_Data, data);
    }

    public static List<DataCmod> getAllCmodData(CreatureEntity ent) {
        CompoundNBT data = ent.getPersistentData().getCompound(dataEntityBuffed_Data);
        String json = data.getString(dataEntityCmodJson);
        return DeserializerAllJson.deserializeCmods(json);
    }

    public static DataCmod getCmodData(CreatureEntity ent, String cmodName) {
        CompoundNBT data = ent.getPersistentData().getCompound(dataEntityBuffed_Data);
        String json = data.getString(dataEntityCmodJson);
        List<DataCmod> cmods = DeserializerAllJson.deserializeCmods(json);
        for (DataCmod cmod : cmods) {
            if (cmod.cmod.equals(cmodName)) {
                return cmod;
            }
        }
        CoroUtil.dbg("warning, could not find cmod data for nane " + cmodName);
        return null;
    }

    public static void registerBuff(BuffBase buff) {
        lookupBuffs.put(buff.getTagName(), buff);
    }

    public static boolean hasBuff(CreatureEntity ent, BuffBase buff) {
        return hasBuff(ent, buff.getTagName());
    }

    public static boolean hasBuff(CreatureEntity ent, String buff) {
        CompoundNBT data = ent.getPersistentData().getCompound(dataEntityBuffed_Data);
        return data.getBoolean(buff);
    }

    /**
     * All non reloading methods must use this to make sure difficulty cache is set
     *
     * @param buffName
     * @param ent
     * @param difficulty
     * @return
     */
    public static boolean applyBuff(String buffName, CreatureEntity ent, float difficulty) {
        if (lookupBuffs.containsKey(buffName)) {

            //mark entity is buffed
            ent.getPersistentData().putBoolean(dataEntityBuffed, true);

            //store difficulty for reloading buffs later
            if (!ent.getPersistentData().contains(dataEntityBuffed_Difficulty)) {
                ent.getPersistentData().putFloat(dataEntityBuffed_Difficulty, difficulty);
            }


            //System.out.println("applying buff: " + buffName);

            return lookupBuffs.get(buffName).applyBuff(ent, difficulty);
        } else {
            CULog.dbg("failed to find buff applying class by name: " + buffName);
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
    public static void applyBuffPost(String buffName, CreatureEntity ent, float difficulty) {
        if (lookupBuffs.containsKey(buffName)) {

            //System.out.println("applying buff: " + buffName);

            lookupBuffs.get(buffName).applyBuffPost(ent, difficulty);
        }
    }

    public static void applyBuffPostAll(CreatureEntity ent, float difficulty) {
        List<String> buffs = UtilEntityBuffs.getAllBuffNames();
        CompoundNBT data = ent.getPersistentData().getCompound(UtilEntityBuffs.dataEntityBuffed_Data);
        for (String buff : buffs) {
            if (data.getBoolean(buff)) {
                BuffBase buffObj = UtilEntityBuffs.getBuff(buff);
                if (buffObj != null) {
                    CULog.dbg("applyBuffPostAll buff: " + buff);
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
    public static void buff_RollDice(World world, CreatureEntity ent, PlayerEntity playerClosest) {

        if (true) return;

        //we already gave him a chance to get buffs, abort
        if (ent.getPersistentData().getBoolean(dataEntityBuffDiceRolled)) return;

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

        ent.getPersistentData().putBoolean(dataEntityBuffDiceRolled, true);

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

    public static void setEquipment(CreatureEntity ent, EquipmentSlotType slot, ItemStack stack) {
        //used to prevent skeletons from being changed, but lets support it
        /*if (slot == EntityEquipmentSlot.MAINHAND && ent instanceof EntitySkeleton) {
            return;
        }*/
        ent.setItemStackToSlot(slot, stack);
        ent.setDropChance(slot, 0);
    }

    public static boolean hasTask(CreatureEntity ent, Class taskToCheckFor, boolean isTargetTask) {
        GoalSelector tasks = ent.tasks;
        if (isTargetTask) {
            tasks = ent.targetSelector;
        }
        boolean foundTask = false;
        for (Object entry2 : tasks.taskEntries) {
            GoalSelector.EntityAITaskEntry entry = (GoalSelector.EntityAITaskEntry) entry2;
            if (taskToCheckFor.isAssignableFrom(entry.action.getClass())) {
                foundTask = true;
                break;
            }
        }
        return foundTask;
    }

    /** example sexier version of hasTask **/
    public static boolean hasAi(MobEntity entity, Class<? extends Goal> clazz) {
        return entity.tasks.taskEntries.stream().anyMatch(task -> clazz.isAssignableFrom(task.action.getClass()));
    }

    public static boolean addGoal(CreatureEntity ent, Class taskToInject, int priorityOfTask, boolean isTargetTask) {
        GoalSelector tasks = ent.tasks;
        if (isTargetTask) {
            tasks = ent.targetSelector;
        }
        try {
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

    public static void processLootTableOnEntity(CreatureEntity ent, LootTable loottable, LivingDeathEvent event) {
        LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerWorld)ent.world)).withLootedEntity(ent).withDamageSource(event.getSource());

        if (ent.recentlyHit > 0 && ent.attackingPlayer != null)
        {
            lootcontext$builder = lootcontext$builder.withPlayer(ent.attackingPlayer).withLuck(ent.attackingPlayer.getLuck());
        }

        for (ItemStack itemstack : loottable.generateLootForPools(ent.world.rand, lootcontext$builder.build()))
        {
            ent.entityDropItem(itemstack, 0.0F);
        }
    }

    public static EquipmentForDifficulty getEquipmentItemsFromData(CmodInventory data) {

        /**
         * TODO: a command to validate/test the json files that all the items exist
         * - though consider scenario where they have a bunch of json files for mods that might not be installed
         * - i might want to ship mod with lots of json files with mods that they might install
         * - how to handle? maybe report if a range of difficulty lacks items?
         */

        EquipmentForDifficulty equipment = new EquipmentForDifficulty();
        //TODO: handle this better?
        try {
            Item item = getItemOrNull(data.inv_hand_main);
            if (item != null) {
                equipment.setWeapon(new ItemStack(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Item item = getItemOrNull(data.inv_hand_off);
            if (item != null) {
                equipment.setWeaponOffhand(new ItemStack(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<ItemStack> listArmor = new ArrayList<>();

        try {
            Item item = getItemOrNull(data.inv_field_78135_a);
            if (item != null) {
                listArmor.add(new ItemStack(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Item item = getItemOrNull(data.inv_chest);
            if (item != null) {
                listArmor.add(new ItemStack(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Item item = getItemOrNull(data.inv_legs);
            if (item != null) {
                listArmor.add(new ItemStack(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            Item item = getItemOrNull(data.inv_feet);
            if (item != null) {
                listArmor.add(new ItemStack(item));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        equipment.setListArmor(listArmor);

        return equipment;

    }

    public static Item getItemOrNull(String name) {
        if (name == null || name.equals("")) {
            return null;
        }
        return Item.getByNameOrId(name);
    }

    public static void onDeath(LivingDeathEvent event) {

        if (event.getEntityLiving() instanceof CreatureEntity) {
            CreatureEntity ent = (CreatureEntity)event.getEntityLiving();
            if (/*ent.canDropLoot() && */ent.world.getGameRules().getBoolean("doMobLoot")) {

                if (ent.getPersistentData().getBoolean(UtilEntityBuffs.dataEntityBuffed)) {
                    float difficultySpawnedIn = 0;
                    if (ent.getPersistentData().contains(UtilEntityBuffs.dataEntityBuffed_Difficulty)) {
                        difficultySpawnedIn = ent.getPersistentData().getFloat(UtilEntityBuffs.dataEntityBuffed_Difficulty);
                    } else {
                        //safely get difficulty for area
                        if (ent.world.isBlockLoaded(ent.getPosition())) {
                            difficultySpawnedIn = DynamicDifficulty.getDifficultyAveragedForArea(ent);
                        }
                    }

                    List<String> buffs = UtilEntityBuffs.getAllBuffNames();
                    CompoundNBT data = ent.getPersistentData().getCompound(UtilEntityBuffs.dataEntityBuffed_Data);
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
