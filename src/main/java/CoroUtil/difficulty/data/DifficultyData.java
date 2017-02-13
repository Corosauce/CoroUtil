package CoroUtil.difficulty.data;

import CoroUtil.difficulty.data.cmodinventory.DataEntryInventoryTemplate;
import CoroUtil.difficulty.data.cmodmobdrops.DataEntryMobDropsTemplate;
import CoroUtil.forge.CoroUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.world.storage.loot.LootTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Corosus on 2/1/2017.
 *
 */
public class DifficultyData {

    public List<DataEntryInventoryTemplate> listTemplatesInventory = new ArrayList<>();
    public List<DataEntryMobDropsTemplate> listTemplatesMobDrops = new ArrayList<>();

    public HashMap<String, LootTable> lookupLootTables = new HashMap<>();

    public DifficultyData() {

    }

}
