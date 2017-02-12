package CoroUtil.difficulty.data;

import CoroUtil.difficulty.data.cmodinventory.DataEntryInventoryTemplate;
import CoroUtil.difficulty.data.cmodmobdrops.DataEntryMobDropsTemplate;
import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Corosus on 2/1/2017.
 */
public class DeserializerCModJson implements JsonDeserializer<Class> {

    @Override
    public Class deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        JsonElement eleFormat = obj.get("format");
        String format = eleFormat.getAsString();

        if (format.toLowerCase().equals("inventory")) {
            JsonElement eleTemplates = obj.get("templates");

            JsonArray arrTemplates = eleTemplates.getAsJsonArray();
            Iterator<JsonElement> it = arrTemplates.iterator();
            while (it.hasNext()) {
                JsonElement eleInv = it.next();
                DataEntryInventoryTemplate entry = context.deserialize(eleInv, DataEntryInventoryTemplate.class);
                DifficultyDataReader.listTemplatesInventory.add(entry);
            }

        } else if (format.toLowerCase().equals("mob_drops")) {
            JsonElement eleTemplates = obj.get("templates");

            JsonArray arrTemplates = eleTemplates.getAsJsonArray();
            Iterator<JsonElement> it = arrTemplates.iterator();
            while (it.hasNext()) {
                JsonElement eleInv = it.next();
                DataEntryMobDropsTemplate entry = context.deserialize(eleInv, DataEntryMobDropsTemplate.class);
                DifficultyDataReader.listTemplatesMobDrops.add(entry);
            }
        }

        return null;
    }
}
