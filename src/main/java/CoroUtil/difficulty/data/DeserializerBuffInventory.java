package CoroUtil.difficulty.data;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Corosus on 2/1/2017.
 */
public class DeserializerBuffInventory implements JsonDeserializer<DataFileCMod> {

    @Override
    public DataFileCMod deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        DataFileCMod file = new DataFileCMod();
        List<DataEntryBase> listEntries = new ArrayList<>();

        JsonElement eleFormat = obj.get("format");
        file.format = eleFormat.getAsString();
        if (file.format.toLowerCase().equals("inventory")) {
            JsonElement eleTemplates = obj.get("templates");
            JsonArray arrTemplates = eleTemplates.getAsJsonArray();
            Iterator<JsonElement> it = arrTemplates.iterator();
            while (it.hasNext()) {
                JsonElement eleInv = it.next();
                DataEntryBuffInventory entry = context.deserialize(eleInv, DataEntryBuffInventory.class);
                listEntries.add(entry);
            }
        }

        file.templates = listEntries;
        return file;
    }
}
