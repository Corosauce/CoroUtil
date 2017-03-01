package CoroUtil.difficulty.data;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

            JsonArray arrTemplates = eleTemplates.getAsJsonArray();
            Iterator<JsonElement> itTemplates = arrTemplates.iterator();
            while (itTemplates.hasNext()) {
                JsonElement eleTemplate = itTemplates.next();
                JsonObject objTemplate = eleTemplate.getAsJsonObject();

                DataMobSpawnsTemplate template = new DataMobSpawnsTemplate();
                template.name = objTemplate.get("name").getAsString();

                //currently mob spawn template names arent referenced for anything, so no need to index by them or check for duplicate names
                JsonArray arr = objTemplate.get("conditions").getAsJsonArray();

                template.conditions.addAll(getArray(arr, context, DataCondition.class));

                arr = objTemplate.get("spawns").getAsJsonArray();
                Iterator<JsonElement> it = arr.iterator();
                while (it.hasNext()) {
                    JsonElement ele = it.next();
                    JsonObject obj2 = ele.getAsJsonObject();

                    DataActionMobSpawns spawnTemplate = new DataActionMobSpawns();

                    spawnTemplate.count = obj2.get("count").getAsInt();

                    JsonArray arr2 = obj2.get("entities").getAsJsonArray();
                    Iterator<JsonElement> it2 = arr2.iterator();
                    while (it2.hasNext()) {
                        spawnTemplate.entities.add(it2.next().getAsString());
                    }

                    if (obj2.has("cmods")) {
                        JsonArray arr3 = obj2.get("cmods").getAsJsonArray();
                        spawnTemplate.cmods.addAll(getArray(arr3, context, DataCmod.class));
                    }

                    /*String name = obj2.get("condition").getAsString();

                    if (DifficultyDataReader.lookupJsonNameToConditionDeserializer.containsKey(name)) {
                        DataCondition cmod = context.deserialize(obj2, DifficultyDataReader.lookupJsonNameToConditionDeserializer.get(name));

                        template.conditions.add(cmod);
                    }*/

                    template.spawns.add(spawnTemplate);
                }

                data.addMobSpawnTemplate(template);
            }

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

                    template.cmods.addAll(getArray(arrCmods, context, DataCmod.class));

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

                    template.conditions.addAll(getArray(arrCmods, context, DataCondition.class));

                    data.addConditionTemplate(template.name, template);
                } else {
                    CoroUtil.forge.CoroUtil.dbg("WARNING: duplicate condition template name! " + template.name);
                }
            }


        }

        return data;
    }

    public <T> List<T> getArray(JsonArray arr, JsonDeserializationContext context, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        Iterator<JsonElement> it = arr.iterator();
        while (it.hasNext()) {
            JsonElement ele = it.next();
            JsonObject obj = ele.getAsJsonObject();

            if (obj.has("condition")) {
                String name = obj.get("condition").getAsString();

                if (DifficultyDataReader.lookupJsonNameToConditionDeserializer.containsKey(name)) {
                    DataCondition cmod = context.deserialize(obj, DifficultyDataReader.lookupJsonNameToConditionDeserializer.get(name));

                    list.add(clazz.cast(cmod));
                }
            } else if (obj.has("cmod")) {
                String name = obj.get("cmod").getAsString();

                if (DifficultyDataReader.lookupJsonNameToCmodDeserializer.containsKey(name)) {
                    DataCmod cmod = context.deserialize(obj, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(name));

                    list.add(clazz.cast(cmod));
                }
            }
        }

        return list;
    }
}
