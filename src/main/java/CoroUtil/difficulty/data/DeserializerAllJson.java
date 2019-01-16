package CoroUtil.difficulty.data;

import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.difficulty.data.cmods.CmodTemplateReference;
import CoroUtil.difficulty.data.conditions.ConditionTemplateReference;
import CoroUtil.difficulty.data.spawns.DataActionMobSpawns;
import CoroUtil.difficulty.data.spawns.DataMobSpawnsTemplate;
import CoroUtil.forge.CULog;
import com.google.gson.*;
import net.minecraft.entity.EntityLiving;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
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

        if (format.toLowerCase().equals(ConfigCoroUtilAdvanced.mobSpawnsProfile)) {
            CULog.dbg("detected mob spawns to deserialize by detecting: " + ConfigCoroUtilAdvanced.mobSpawnsProfile);
            JsonElement eleTemplates = obj.get("templates");

            JsonArray arrTemplates = eleTemplates.getAsJsonArray();
            Iterator<JsonElement> itTemplates = arrTemplates.iterator();
            while (itTemplates.hasNext()) {
                JsonElement eleTemplate = itTemplates.next();
                JsonObject objTemplate = eleTemplate.getAsJsonObject();

                DataMobSpawnsTemplate template = new DataMobSpawnsTemplate();
                template.name = objTemplate.get("name").getAsString();

                if (objTemplate.has("wave_message")) {
                    template.wave_message = objTemplate.get("wave_message").getAsString();
                }

                JsonArray arr;

                //currently mob spawn template names arent referenced for anything, so no need to index by them or check for duplicate names
                if (objTemplate.has("conditions")) {
                    arr = objTemplate.get("conditions").getAsJsonArray();

                    template.conditions.addAll(getArray(arr, DataCondition.class));
                }

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
            CULog.dbg("detected cmods to deserialize");

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
            CULog.dbg("detected conditions to deserialize");

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
        if (json.has("count_max")) {
            spawnTemplate.count_max = json.get("count_max").getAsInt();
        }
        if (json.has("count_difficulty_multiplier")) {
            spawnTemplate.count_difficulty_multiplier = json.get("count_difficulty_multiplier").getAsDouble();
        }

        if (json.has("spawn_type")) {
            String spawnType = json.get("spawn_type").getAsString();
            if (spawnType.equals("ground")) {
                spawnTemplate.spawnType = EntityLiving.SpawnPlacementType.ON_GROUND;
            } else if (spawnType.equals("air")) {
                spawnTemplate.spawnType = EntityLiving.SpawnPlacementType.IN_AIR;
            } else if (spawnType.equals("water")) {
                spawnTemplate.spawnType = EntityLiving.SpawnPlacementType.IN_WATER;
            }
        }

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
        obj.addProperty("count_max", spawns.count_max);
        obj.addProperty("count_difficulty_multiplier", spawns.count_difficulty_multiplier);
        JsonArray arr1 = new JsonArray();
        for (String str : spawns.entities) {
            arr1.add(new JsonPrimitive(str));
        }
        obj.add("entities", arr1);
        /*JsonArray arr2 = new JsonArray();
        for (DataCmod cmod : spawns.cmods) {
            arr2.add((new JsonParser()).parse((new Gson()).toJson(cmod, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(cmod.cmod))).getAsJsonObject());
            //arr2.add(new JsonPrimitive(((new Gson()).toJson(cmod, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(cmod.cmod)))));
        }*/
        obj.add("cmods", serializeCmods(spawns.cmods));
        return obj;
        //arr1.add();
        //obj.add("entities");
    }

    public static JsonArray serializeCmods(List<DataCmod> cmods) {
        JsonArray arr2 = new JsonArray();
        for (DataCmod cmod : cmods) {
            arr2.add((new JsonParser()).parse((new Gson()).toJson(cmod, DifficultyDataReader.lookupJsonNameToCmodDeserializer.get(cmod.cmod))).getAsJsonObject());
        }
        return arr2;
    }

    public static List<DataCmod> deserializeCmods(String json) {
        JsonArray array = (new JsonParser()).parse(json).getAsJsonArray();
        return getArray(array, DataCmod.class);
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

    public static List<DataCondition> getConditionFlattened(DataCondition condition) {
        List<DataCondition> list = new ArrayList<>();
        list.add(condition);
        return getConditionsFlattened(list);
    }

    /**
     * Get conditions including ones nested in templates
     * - aiming to use overridable conditions
     * - top level condition overrides one in a template if are of the same type
     * - there may be scenarios where i want to layer and use both conditions?
     * - what about 2 templates referenced, each with a random?
     * @return
     */
    public static List<DataCondition> getConditionsFlattened(List<DataCondition> conditions) {
        //List<DataCondition> listTop = new ArrayList<>();
        HashMap<String, DataCondition> lookup = new HashMap<>();
        //conditions.contains()

        for (DataCondition condition : conditions) {
            if (condition instanceof ConditionTemplateReference) {

            } else {
                if (!lookup.containsKey(condition.condition)) {
                    lookup.put(condition.condition, condition);
                } else {
                    CoroUtil.forge.CoroUtil.dbg("duplicate key for condition at top level " + condition.condition);
                }
            }

        }

        for (DataCondition condition : conditions) {
            if (condition instanceof ConditionTemplateReference) {
                DataConditionTemplate template = DifficultyDataReader.getData().lookupConditionTemplates.get(((ConditionTemplateReference) condition).template);
                if (template != null) {
                    for (DataCondition condition2 : template.conditions) {
                        //prevent nested templating for now
                        if (!(condition2 instanceof ConditionTemplateReference)) {
                            if (!lookup.containsKey(condition2.condition)) {
                                lookup.put(condition2.condition, condition2);
                            } else {
                                CoroUtil.forge.CoroUtil.dbg("duplicate key for condition from template " + condition2.condition);
                            }
                        }
                    }
                } else {
                    CoroUtil.forge.CoroUtil.dbg("warning, could not find condition template named " + ((ConditionTemplateReference) condition).template);
                }
            }
        }

        List<DataCondition> list = new ArrayList<>();
        list.addAll(lookup.values());
        return list;
    }

    public static List<DataCmod> getCmodFlattened(DataCmod cmod) {
        List<DataCmod> list = new ArrayList<>();
        list.add(cmod);
        return getCmodsFlattened(list);
    }

    public static List<DataCmod> getCmodsFlattened(List<DataCmod> cmods) {
        //List<DataCondition> listTop = new ArrayList<>();
        HashMap<String, DataCmod> lookup = new HashMap<>();
        //conditions.contains()

        for (DataCmod cmod : cmods) {
            if (cmod instanceof CmodTemplateReference) {

            } else {
                if (!lookup.containsKey(cmod.cmod)) {
                    lookup.put(cmod.cmod, cmod);
                } else {
                    CoroUtil.forge.CoroUtil.dbg("duplicate key for cmod at top level " + cmod.cmod);
                }
            }

        }

        for (DataCmod cmod : cmods) {
            if (cmod instanceof CmodTemplateReference) {
                DataCmodTemplate template = DifficultyDataReader.getData().lookupCmodTemplates.get(((CmodTemplateReference) cmod).template);
                if (template != null) {
                    for (DataCmod cmod2 : template.cmods) {
                        //prevent nested templating for now
                        if (!(cmod2 instanceof CmodTemplateReference)) {
                            if (!lookup.containsKey(cmod2.cmod)) {
                                lookup.put(cmod2.cmod, cmod2);
                            } else {
                                CoroUtil.forge.CoroUtil.dbg("duplicate key for cmod from template " + cmod2.cmod);
                            }
                        }
                    }
                } else {
                    CoroUtil.forge.CoroUtil.dbg("warning, could not find cmod template named " + ((CmodTemplateReference) cmod).template);
                }
            }
        }

        List<DataCmod> list = new ArrayList<>();
        list.addAll(lookup.values());
        return list;
    }
}
