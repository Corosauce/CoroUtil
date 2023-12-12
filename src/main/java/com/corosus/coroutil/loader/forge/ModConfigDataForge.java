package com.corosus.coroutil.loader.forge;

import com.corosus.coroutil.common.core.modconfig.*;
import com.corosus.coroutil.common.core.util.CULog;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ModConfigDataForge extends ModConfigData {

    public HashMap<String, ForgeConfigSpec.ConfigValue<String>> valsStringConfig = new HashMap<>();
    public HashMap<String, ForgeConfigSpec.ConfigValue<Integer>> valsIntegerConfig = new HashMap<>();
    public HashMap<String, ForgeConfigSpec.ConfigValue<Double>> valsDoubleConfig = new HashMap<>();
    public HashMap<String, ForgeConfigSpec.ConfigValue<Boolean>> valsBooleanConfig = new HashMap<>();

    public ModConfigDataForge(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
        super(savePath, parStr, parClass, parConfig);
    }

    /*@Override
    public void initConfigString(String name, String comment, String value) {
        valsStringConfig.put(name, builder.comment(comment).define(name, (String)obj));
    }

    @Override
    public void initConfigInteger(String name, String comment, int value, int min, int max) {

    }

    @Override
    public void initConfigDouble(String name, String comment, double value, double min, double max) {

    }

    @Override
    public void initConfigBoolean(String name, String comment, boolean value) {

    }*/

    @Override
    public String getConfigString(String fieldName) {
        return valsStringConfig.get(fieldName).get();
    }

    @Override
    public Integer getConfigInteger(String fieldName) {
        return valsIntegerConfig.get(fieldName).get();
    }

    @Override
    public Double getConfigDouble(String fieldName) {
        return valsDoubleConfig.get(fieldName).get();
    }

    @Override
    public Boolean getConfigBoolean(String fieldName) {
        return valsBooleanConfig.get(fieldName).get();
    }

    @Override
    public <T> void setConfig(String fieldName, T obj) {
        if (obj instanceof String) {
            valsStringConfig.get(fieldName).set((String)obj);
            valsStringConfig.get(fieldName).save();
        } else if (obj instanceof Integer) {
            valsIntegerConfig.get(fieldName).set((Integer)obj);
            valsIntegerConfig.get(fieldName).save();
        } else if (obj instanceof Double) {
            valsDoubleConfig.get(fieldName).set((Double)obj);
            valsDoubleConfig.get(fieldName).save();
        } else if (obj instanceof Boolean) {
            valsBooleanConfig.get(fieldName).set((Boolean)obj);
            valsBooleanConfig.get(fieldName).save();
        } else {
            //dbg("unhandled datatype, update initField");
        }
    }

    @Override
    public void writeConfigFile(boolean resetConfig) {

        //TODO: see if we need support for resetting config
        //if (resetConfig) if (saveFilePath.exists()) saveFilePath.delete();
        //preInitConfig = new Configuration(saveFilePath);
        //preInitConfig.load();
        ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
        BUILDER.comment("General mod settings").push("general");

        Field[] fields = configClass.getDeclaredFields();

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            String name = field.getName();

            addToConfig(BUILDER, field, name);
        }

        CULog.dbg("writeConfigFile invoked for " + this.configID + ", resetConfig: " + resetConfig);
        BUILDER.pop();
        ForgeConfigSpec CONFIG = BUILDER.build();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG, saveFilePath + ".toml");
    }

    /**
     * Perform the actual adding of values to the config file
     * @param name Name of the variable
     * @param field Field in the file the variable is
     */
    private void addToConfig(ForgeConfigSpec.Builder builder, Field field, String name) {

        // Comment from the annotation on the value of the actual variable that 'name' is retrieved from
        //space intentional here to workaround forge hating blank comments
        String comment = "-";
        double min = Double.MIN_VALUE;
        double max = Double.MAX_VALUE;

        ConfigComment anno_comment = field.getAnnotation(ConfigComment.class);
        if (anno_comment != null) {
            comment = anno_comment.value()[0];
        }

        ConfigParams anno_params = field.getAnnotation(ConfigParams.class);
        if (anno_params != null) {
            comment = anno_params.comment();
            min = anno_params.min();
            max = anno_params.max();
        }

        //System.out.println("registering config field: " + name);

        Object obj = ConfigMod.instance().getField(configID, name);
        if (obj instanceof String) {
            valsStringConfig.put(name, builder.comment(comment).define(name, (String)obj));
        } else if (obj instanceof Integer) {
            valsIntegerConfig.put(name, builder.comment(comment).defineInRange(name, (Integer)obj, (int)min, (int)max));
        } else if (obj instanceof Double) {
            valsDoubleConfig.put(name, builder.comment(comment).defineInRange(name, (Double)obj, min, max));
        } else if (obj instanceof Boolean) {
            valsBooleanConfig.put(name, builder.comment(comment).define(name, (Boolean)obj));
        } else {
            //dbg("unhandled datatype, update initField");
        }
        setFieldBasedOnType(name, obj);
    }
}
