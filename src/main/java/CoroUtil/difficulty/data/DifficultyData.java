package CoroUtil.difficulty.data;

import net.minecraft.world.storage.loot.LootTable;

import java.util.HashMap;

/**
 * Created by Corosus on 2/1/2017.
 *
 */
public class DifficultyData {

    public HashMap<String, DataCmodTemplate> lookupCmodTemplates = new HashMap<>();
    public HashMap<String, DataConditionTemplate> lookupConditionTemplates = new HashMap<>();

    public HashMap<String, LootTable> lookupLootTables = new HashMap<>();

    public DifficultyData() {

    }

    public void reset() {
        lookupLootTables.clear();
        lookupCmodTemplates.clear();
        lookupConditionTemplates.clear();
    }

    public void addCmodTemplate(String name, DataCmodTemplate template) {
        lookupCmodTemplates.put(name, template);
    }

    public void addConditionTemplate(String name, DataConditionTemplate template) {
        lookupConditionTemplates.put(name, template);
    }

    public void addLootTable(String name, LootTable template) {
        lookupLootTables.put(name, template);
    }
}
