package CoroUtil.difficulty.data;

import CoroUtil.difficulty.data.cmods.*;
import CoroUtil.difficulty.data.conditions.*;
import CoroUtil.forge.CULog;
import CoroUtil.forge.CoroUtil;
import CoroUtil.util.UtilClasspath;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Corosus on 2/1/2017.
 *
 */
public class DifficultyDataReader {

    //using Class.class because we dont need a return type
    private static final Gson GSONBuffInventory = (new GsonBuilder()).registerTypeAdapter(DifficultyData.class, new DeserializerAllJson()).create();
    //private static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer()).registerTypeAdapter(LootPool.class, new LootPool.Serializer()).registerTypeAdapter(LootTable.class, new LootTable.Serializer()).registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer()).registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer()).registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer()).create();

    private static DifficultyData data;

    public static String lootTablesFolder = "loot_tables";

    public static HashMap<String, Class> lookupJsonNameToCmodDeserializer = new HashMap<>();
    public static HashMap<String, Class> lookupJsonNameToConditionDeserializer = new HashMap<>();

    //modes used for validating
    private static boolean debugValidate = false;
    private static boolean debugFlattenCmodsAndConditions = false;

    public static boolean debugValidate() {
        return debugValidate;
    }

    public static void setDebugValidate(boolean debugValidate) {
        DifficultyDataReader.debugValidate = debugValidate;
    }

    public static boolean debugFlattenCmodsAndConditions() {
        return debugFlattenCmodsAndConditions;
    }

    public static void setDebugFlattenCmodsAndConditions(boolean debugFlattenCmodsAndConditions) {
        DifficultyDataReader.debugFlattenCmodsAndConditions = debugFlattenCmodsAndConditions;
    }

    public static void init() {
        data = new DifficultyData();

        lookupJsonNameToCmodDeserializer.clear();
        lookupJsonNameToCmodDeserializer.put("inventory", CmodInventory.class);
        lookupJsonNameToCmodDeserializer.put("mob_drops", CmodMobDrops.class);
        lookupJsonNameToCmodDeserializer.put("attribute_health", CmodAttributeHealth.class);
        lookupJsonNameToCmodDeserializer.put("attribute_speed", CmodAttributeSpeed.class);
        lookupJsonNameToCmodDeserializer.put("xp", CmodXP.class);
        lookupJsonNameToCmodDeserializer.put("ai_antiair", CmodAITaskBase.class);
        lookupJsonNameToCmodDeserializer.put("ai_mining", CmodAITaskBase.class);
        lookupJsonNameToCmodDeserializer.put("ai_counterattack", CmodAITaskBase.class);
        lookupJsonNameToCmodDeserializer.put("ai_lunge", CmodAITaskBase.class);
        lookupJsonNameToCmodDeserializer.put("ai_infernal", CmodAIInfernal.class);
        lookupJsonNameToCmodDeserializer.put("template", CmodTemplateReference.class);

        lookupJsonNameToConditionDeserializer.clear();
        lookupJsonNameToConditionDeserializer.put("context", ConditionContext.class);
        lookupJsonNameToConditionDeserializer.put("difficulty", ConditionDifficulty.class);
        lookupJsonNameToConditionDeserializer.put("invasion_number", ConditionInvasionNumber.class);
        lookupJsonNameToConditionDeserializer.put("random", ConditionRandom.class);
        lookupJsonNameToConditionDeserializer.put("filter_mobs", ConditionFilterMobs.class);
        lookupJsonNameToConditionDeserializer.put("template", ConditionTemplateReference.class);
    }

    public static DifficultyData getData() {
        return data;
    }

    public static void loadFiles() {
        data.reset();

        CoroUtil.dbg("start reading difficulty files");

        /*File folderLoot = new File("I:\\newdev\\git\\CoroUtil_1.10.2\\src\\main\\resources\\assets\\coroutil\\config\\" + lootTablesFolder + "\\");

        String fileContents;

        if (folderLoot.exists()) {
            if (folderLoot.isFile()) {
                try {
                    fileContents = Files.toString(folderLoot, Charsets.UTF_8);
                    LootTable lootTable = net.minecraftforge.common.ForgeHooks.loadLootTable(GSON_INSTANCE, test, fileContents, true);
                    System.out.println(lootTable);
                } catch (Exception ex) {

                }
            }
        }*/



        //temp
        /*File dataFolder = new File("I:\\newdev\\git\\CoroUtil_1.10.2\\src\\main\\resources\\assets\\coroutil\\config\\");

        if (!dataFolder.exists()) {
            dataFolder = new File("/mnt/e/git/CoroUtil_1.12.x/src/main/resources/assets/coroutil/config/");
        }*/

        try {
            /*List<File> listFiles = UtilClasspath.getClassesForPackage("assets.coroutil.config", ".json");
            System.out.println(listFiles);*/

            /*final Collection<String> list = UtilClasspath.getResources("");
            for(final String name : list){
                CULog.dbg(name);
            }*/


            /*List<String> files = IOUtils.readLines(DifficultyDataReader.class.getClassLoader()
                    .getResourceAsStream("."), Charsets.UTF_8);*/

            /**
             * TODO: use this code to copy the json files to a filesystem folder
             * - config folder or per world folder?
             * - then user can customize them more like actual configs
             */

            List<String> listFiles = new ArrayList<>();

            for (ModContainer mod : Loader.instance().getActiveModList()) {
                //System.out.println(mod.getModId());

                if (mod.getModId().equals(CoroUtil.modID)) {
                    //UtilClasspath.loadRecipes(mod);
                    CraftingHelper.findFiles(mod, "assets/" + mod.getModId() + "/config",
                            null, (root, file) ->
                            {
                                //System.out.println("2:" + root + ", " + file);
                                if (file.toString().endsWith("json")) {
                                    listFiles.add(file.toString());
                                }
                                return true;
                            }, true, true);
                }
            }

            for (String file : listFiles) {
                processFileFromJarPath(file);
            }

            //

        } catch (Exception e) {
            e.printStackTrace();
        }


        /*try {

            if (dataFolder.exists()) {
                processFolder(dataFolder);
            } else {
                System.out.println("doesnt exist!");
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }*/

        CoroUtil.dbg("done");
    }

    //TODO: replace with temp copy from jar to filesystem and load from filesystem for user editability?
    public static void processFileFromJarPath(String path) {
        try {

            String pathRoot = path.substring(path.indexOf("assets/coroutil/"));

            if (path.contains(lootTablesFolder)) {
                CULog.dbg("processing, detected as loot table: " + path.substring(path.lastIndexOf("/")+1).toString());

                String temp = pathRoot.replace("assets/" + CoroUtil.modID + "/", "");
                String fileContents = UtilClasspath.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, temp));
                CULog.dbg("file contents size: " + fileContents.length());

                String fileName = path.substring(path.lastIndexOf("/")+1).replace(".json", "");
                ResourceLocation resName = new ResourceLocation(CoroUtil.modID + ":loot_tables." + fileName);
                LootTable lootTable = net.minecraftforge.common.ForgeHooks.loadLootTable(LootTableManager.GSON_INSTANCE, resName, fileContents, true, null);
                data.lookupLootTables.put(fileName, lootTable);
            } else {
                CULog.dbg("processing, detected as DifficultyData: " + path.substring(path.lastIndexOf("/")+1).toString());
                String temp = pathRoot.replace("assets/" + CoroUtil.modID + "/", "");
                String fileContents = UtilClasspath.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, temp));
                CULog.dbg("file contents size: " + fileContents.length());

                GSONBuffInventory.fromJson(fileContents, DifficultyData.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void processFileFromFilesystem(File file) {
        try {

            //CoroUtil.dbg("processing: " + file.toString());
            String lastPath = file.getParent().toLowerCase();
            if (lastPath.endsWith(lootTablesFolder)) {
                CULog.dbg("processing, detected as loot table: " + file.toString());
                String fileContents = Files.toString(file, Charsets.UTF_8);
                String fileName = file.getName().replace(".json", "");
                ResourceLocation resName = new ResourceLocation(CoroUtil.modID + ":loot_tables." + fileName);
                LootTable lootTable = net.minecraftforge.common.ForgeHooks.loadLootTable(LootTableManager.GSON_INSTANCE, resName, fileContents, true, null);
                data.lookupLootTables.put(fileName, lootTable);
            } else {
                CULog.dbg("processing, detected as DifficultyData: " + file.toString());
                GSONBuffInventory.fromJson(new BufferedReader(new FileReader(file)), DifficultyData.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    /*public static void processFileFromString(String path, String contents) {
        try {
            if (path.contains(lootTablesFolder)) {
                CULog.dbg("processing, detected as loot table: " + path.toString());
                //ResourceLocation resLoc = new ResourceLocation();
                String temp = path.replace("assets/" + CoroUtil.modID, "").replace("/", ".");
                String fileContents = UtilClasspath.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, temp));
                //String fileContents = Files.toString(path, Charsets.UTF_8);
                String fileName = path.substring(path.lastIndexOf("/")+1).replace(".json", "");
                ResourceLocation resName = new ResourceLocation(CoroUtil.modID + ":loot_tables." + fileName);
                LootTable lootTable = net.minecraftforge.common.ForgeHooks.loadLootTable(LootTableManager.GSON_INSTANCE, resName, fileContents, true, null);
                data.lookupLootTables.put(fileName, lootTable);
            } else {
                CULog.dbg("processing, detected as DifficultyData: " + file.toString());
                GSONBuffInventory.fromJson(new BufferedReader(new FileReader(file)), DifficultyData.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

    public static void processFolder(File path) {
        for (File child : path.listFiles()) {
            if (child.isFile()) {
                try {
                    if (child.toString().endsWith(".json")) {
                        processFileFromFilesystem(child);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                processFolder(child);
            }
        }
    }

}
