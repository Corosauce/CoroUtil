package CoroUtil.difficulty.data;

import CoroUtil.difficulty.data.cmodinventory.DataEntryInventoryTemplate;
import CoroUtil.difficulty.data.cmodmobdrops.DataEntryMobDropsTemplate;
import CoroUtil.forge.CoroUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    private static final Gson GSONBuffInventory = (new GsonBuilder()).registerTypeAdapter(Class.class, new DeserializerCModJson()).create();

    public static List<DataEntryInventoryTemplate> listTemplatesInventory = new ArrayList<>();
    public static List<DataEntryMobDropsTemplate> listTemplatesMobDrops = new ArrayList<>();

    public DifficultyDataReader() {

    }

    public static void loadFiles() {

        listTemplatesInventory.clear();

        CoroUtil.dbg("start reading difficulty files");

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
            GSONBuffInventory.fromJson(new BufferedReader(new FileReader(file)), Class.class);
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
