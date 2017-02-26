package CoroUtil.difficulty.data;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.Iterator;

/**
 * Created by Corosus on 2/1/2017.
 */
public class DeserializerAllJson implements JsonDeserializer<DifficultyData> {

    @Override
    public DifficultyData deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        JsonElement eleFormat = obj.get("format");
        String format = eleFormat.getAsString();

        //since this class is run on multiple files, we cant just make a new instance each time, consider a refactor? loot tables arent handled in this class either
        DifficultyData data = DifficultyDataReader.getData();//new DifficultyData();

        if (format.toLowerCase().equals("mob_spawns")) {
            JsonElement eleTemplates = obj.get("templates");


        } else if (format.toLowerCase().equals("cmods")) {
            JsonElement eleTemplates = obj.get("templates");

            JsonArray arrTemplates = eleTemplates.getAsJsonArray();
            Iterator<JsonElement> itTemplates = arrTemplates.iterator();
            while (itTemplates.hasNext()) {
                JsonElement eleTemplate = itTemplates.next();
                JsonObject objTemplate = eleTemplate.getAsJsonObject();

                DataCmodTemplate template = new DataCmodTemplate();
                template.name = objTemplate.get("name").getAsString();

                if (!data.lookupCmodTemplates.containsKey(template.name)) {
                    JsonArray arrCmods = objTemplate.get("cmods").getAsJsonArray();
                    Iterator<JsonElement> itCmods = arrCmods.iterator();
                    while (itCmods.hasNext()) {
                        JsonElement eleCmod = itCmods.next();
                        JsonObject objCmod = eleCmod.getAsJsonObject();

                        String cmodName = objCmod.get("cmod").getAsString();

                        if (DifficultyDataReader.lookupJsonNameToCmodDeserializer.containsKey(cmodName)) {
                            DataCmod cmod = context.deserialize(objCmod, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(cmodName));

                            template.cmods.add(cmod);
                        }
                    }
                    data.addCmodTemplate(template.name, template);
                } else {
                    CoroUtil.forge.CoroUtil.dbg("WARNING: duplicate cmod template name! " + template.name);
                }
            }
        } else if (format.toLowerCase().equals("conditions")) {
            JsonElement eleTemplates = obj.get("templates");

            JsonArray arrTemplates = eleTemplates.getAsJsonArray();
            Iterator<JsonElement> itTemplates = arrTemplates.iterator();
            while (itTemplates.hasNext()) {
                JsonElement eleTemplate = itTemplates.next();
                JsonObject objTemplate = eleTemplate.getAsJsonObject();

                DataConditionTemplate template = new DataConditionTemplate();
                template.name = objTemplate.get("name").getAsString();

                if (!data.lookupConditionTemplates.containsKey(template.name)) {
                    JsonArray arrCmods = objTemplate.get("conditions").getAsJsonArray();
                    Iterator<JsonElement> itCmods = arrCmods.iterator();
                    while (itCmods.hasNext()) {
                        JsonElement eleCmod = itCmods.next();
                        JsonObject objCmod = eleCmod.getAsJsonObject();

                        String cmodName = objCmod.get("condition").getAsString();

                        if (DifficultyDataReader.lookupJsonNameToConditionDeserializer.containsKey(cmodName)) {
                            DataCondition cmod = context.deserialize(objCmod, DifficultyDataReader.lookupJsonNameToConditionDeserializer.get(cmodName));

                            template.conditions.add(cmod);
                        }
                    }
                    data.addConditionTemplate(template.name, template);
                } else {
                    CoroUtil.forge.CoroUtil.dbg("WARNING: duplicate condition template name! " + template.name);
                }
            }


        }

        return data;
    }
}
