package CoroUtil.difficulty.data;

import CoroUtil.difficulty.data.cmodinventory.DataEntryInventoryTemplate;
import CoroUtil.difficulty.data.cmodmobdrops.DataEntryMobDropsTemplate;
import CoroUtil.forge.CoroUtil;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Corosus on 2/1/2017.
 *
 */
public class DifficultyDataReader {

    //using Class.class because we dont need a return type
    private static final Gson GSONBuffInventory = (new GsonBuilder()).registerTypeAdapter(DifficultyData.class, new DeserializerCModJson()).create();
    //private static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer()).registerTypeAdapter(LootPool.class, new LootPool.Serializer()).registerTypeAdapter(LootTable.class, new LootTable.Serializer()).registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer()).registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer()).registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer()).create();

    private static DifficultyData data;

    public static String lootTablesFolder = "loot_tables";

    public DifficultyDataReader() {
        data = new DifficultyData();
    }

    public static DifficultyData getData() {
        return data;
    }

    public void loadFiles() {

        data.listTemplatesInventory.clear();
        data.listTemplatesMobDrops.clear();

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
        File dataFolder = new File("I:\\newdev\\git\\CoroUtil_1.10.2\\src\\main\\resources\\assets\\coroutil\\config\\");

        try {

            processFolder(dataFolder);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        CoroUtil.dbg("done");
    }

    public static void processFile(File file) {
        try {

            CoroUtil.dbg("processing: " + file.toString());
            String lastPath = file.getParent().toLowerCase();
            if (lastPath.endsWith(lootTablesFolder)) {
                String fileContents = Files.toString(file, Charsets.UTF_8);
                String fileName = file.getName().replace(".json", "");
                ResourceLocation resName = new ResourceLocation(CoroUtil.modID + ":loot_tables." + fileName);
                LootTable lootTable = net.minecraftforge.common.ForgeHooks.loadLootTable(LootTableManager.GSON_INSTANCE, resName, fileContents, true);
                data.lookupLootTables.put(fileName, lootTable);
            } else {
                GSONBuffInventory.fromJson(new BufferedReader(new FileReader(file)), DifficultyData.class);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void processFolder(File path) {
        for (File child : path.listFiles()) {
            if (child.isFile()) {
                try {
                    if (child.toString().endsWith(".json")) {
                        processFile(child);
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
