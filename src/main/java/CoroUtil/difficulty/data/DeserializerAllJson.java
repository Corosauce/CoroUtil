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

                template.conditions.addAll(getArray(arr, DataCondition.class));

                arr = objTemplate.get("spawns").getAsJsonArray();
                Iterator<JsonElement> it = arr.iterator();
                while (it.hasNext()) {
                    JsonElement ele = it.next();
                    JsonObject obj2 = ele.getAsJsonObject();

                    DataActionMobSpawns spawnTemplate = deserializeSpawns(obj2);

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

                    template.cmods.addAll(getArray(arrCmods, DataCmod.class));

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

                    template.conditions.addAll(getArray(arrCmods, DataCondition.class));

                    data.addConditionTemplate(template.name, template);
                } else {
                    CoroUtil.forge.CoroUtil.dbg("WARNING: duplicate condition template name! " + template.name);
                }
            }


        }

        return data;
    }

    public static DataActionMobSpawns deserializeSpawns(JsonObject json) {
        DataActionMobSpawns spawnTemplate = new DataActionMobSpawns();

        spawnTemplate.count = json.get("count").getAsInt();

        JsonArray arr2 = json.get("entities").getAsJsonArray();
        Iterator<JsonElement> it2 = arr2.iterator();
        while (it2.hasNext()) {
            spawnTemplate.entities.add(it2.next().getAsString());
        }

        if (json.has("cmods")) {
            JsonArray arr3 = json.get("cmods").getAsJsonArray();
            spawnTemplate.cmods.addAll(getArray(arr3, DataCmod.class));
        }
        return spawnTemplate;
    }

    public static JsonObject serializeSpawns(DataActionMobSpawns spawns) {
        JsonObject obj = new JsonObject();
        obj.addProperty("count", spawns.count);
        JsonArray arr1 = new JsonArray();
        for (String str : spawns.entities) {
            arr1.add(new JsonPrimitive(str));
        }
        obj.add("entities", arr1);
        JsonArray arr2 = new JsonArray();
        for (DataCmod cmod : spawns.cmods) {
            arr2.add((new JsonParser()).parse((new Gson()).toJson(cmod, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(cmod.cmod))).getAsJsonObject());
            //arr2.add(new JsonPrimitive(((new Gson()).toJson(cmod, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(cmod.cmod)))));
        }
        obj.add("cmods", arr2);
        return obj;
        //arr1.add();
        //obj.add("entities");
    }

    public static <T> List<T> getArray(JsonArray arr, /*JsonDeserializationContext context, */Class<T> clazz) {
        List<T> list = new ArrayList<>();
        Iterator<JsonElement> it = arr.iterator();
        while (it.hasNext()) {
            JsonElement ele = it.next();
            JsonObject obj = ele.getAsJsonObject();

            if (obj.has("condition")) {
                String name = obj.get("condition").getAsString();

                if (DifficultyDataReader.lookupJsonNameToConditionDeserializer.containsKey(name)) {
                    //DataCondition cmod = context.deserialize(obj, DifficultyDataReader.lookupJsonNameToConditionDeserializer.get(name));
                    DataCondition cmod = (DataCondition)(new Gson()).fromJson(obj, DifficultyDataReader.lookupJsonNameToConditionDeserializer.get(name));

                    list.add(clazz.cast(cmod));
                }
            } else if (obj.has("cmod")) {
                String name = obj.get("cmod").getAsString();

                if (DifficultyDataReader.lookupJsonNameToCmodDeserializer.containsKey(name)) {
                    //DataCmod cmod = context.deserialize(obj, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(name));
                    DataCmod cmod = (DataCmod)(new Gson()).fromJson(obj, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(name));

                    list.add(clazz.cast(cmod));
                }
            }
        }

        return list;
    }
}
