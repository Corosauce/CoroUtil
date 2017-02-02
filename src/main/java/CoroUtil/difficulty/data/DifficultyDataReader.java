package CoroUtil.difficulty.data;

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
 * TODO: we dont need the DataFileCMod entries, only the DataEntryBuffInventory entries
 * - this is due to support for multiple entries per file, but we just want to work with the templates within the files, not the files themselves
 */
public class DifficultyDataReader {

    private static final Gson GSONBuffInventory = (new GsonBuilder()).registerTypeAdapter(DataFileCMod.class, new DeserializerBuffInventory()).create();

    public static List<DataEntryBuffInventory> listTemplatesInventory = new ArrayList<>();

    public DifficultyDataReader() {

    }

    public static void loadFiles() {

        listTemplatesInventory.clear();

        System.out.println("start reading difficulty files");

        //TODO: make it crawl a folder recursively, detecting type of file based on the standard "format" tag, add to list of that type

        //temp
        File fileBuff = new File("I:\\newdev\\git\\CoroUtil_1.10.2\\src\\main\\resources\\assets\\coroutil\\config\\buff_inventory.json");

        try {
            DataFileCMod fileJson = GSONBuffInventory.fromJson(new BufferedReader(new FileReader(fileBuff)), DataFileCMod.class);
            //TODO: surely there is a more generic solution to populating these lists....
            if (fileJson.format.toLowerCase().equals("inventory")) {
                for (DataEntryBase entry : fileJson.templates) {
                    listTemplatesInventory.add((DataEntryBuffInventory) entry);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("done");
        /*try (BufferedReader bReader = new BufferedReader(
                new InputStreamReader(this.getClass().getResourceAsStream(
                        pathUpdaterResourcesURL + fileUpdateRules)))) {
            data = new Gson().fromJson(bReader, UpdateData.class);
        }*/
    }

}
