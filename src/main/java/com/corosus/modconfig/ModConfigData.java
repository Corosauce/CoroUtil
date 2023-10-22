package com.corosus.modconfig;

import com.corosus.coroconfig.CoroConfigTracker;
import com.corosus.coroconfig.CoroModConfig;
import com.corosus.coroconfig.CoroModContainerConfig;
import com.corosus.coroutil.util.CULog;
import com.corosus.coroutil.util.OldUtil;
import com.corosus.coroconfig.CoroConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;

import java.lang.reflect.Field;
import java.util.*;

public class ModConfigData {
	public String configID;
	public Class configClass;
	public IConfigCategory configInstance;
	
	public HashMap<String, String> valsString = new HashMap<>();
	public HashMap<String, Integer> valsInteger = new HashMap<>();
	public HashMap<String, Double> valsDouble = new HashMap<>();
	public HashMap<String, Boolean> valsBoolean = new HashMap<>();

	public HashMap<String, CoroConfigSpec.ConfigValue<String>> valsStringConfig = new HashMap<>();
	public HashMap<String, CoroConfigSpec.ConfigValue<Integer>> valsIntegerConfig = new HashMap<>();
	public HashMap<String, CoroConfigSpec.ConfigValue<Double>> valsDoubleConfig = new HashMap<>();
	public HashMap<String, CoroConfigSpec.ConfigValue<Boolean>> valsBooleanConfig = new HashMap<>();

	//Client data
	public List<ConfigEntryInfo> configData = new ArrayList<>();
    public String saveFilePath;

	public CoroModContainerConfig container;

	public ModConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
		configID = parStr;
		configClass = parClass;
		configInstance = parConfig;
		saveFilePath = savePath;
		container = new CoroModContainerConfig(parStr);
	}
	
	public void updateHashMaps() {
    	Field[] fields = configClass.getDeclaredFields();
    	
    	for (int i = 0; i < fields.length; i++) {
    		Field field = fields[i];
    		String name = field.getName();
    		processField(name);
    	}
    }

	public void updateConfigFieldValues() {
		Field[] fields = configClass.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String name = field.getName();
			processFieldFromForgeConfig(name);
		}
	}

	private void processFieldFromForgeConfig(String fieldName) {
		try {
			Object obj = ConfigMod.getField(configID, fieldName);
			if (obj instanceof String) {
				valsString.put(fieldName, (String)obj);
				String val = valsStringConfig.get(fieldName).get();
				setFieldBasedOnType(fieldName, val);
			} else if (obj instanceof Integer) {
				valsInteger.put(fieldName, (Integer)obj);
				int what = valsIntegerConfig.get(fieldName).get();
				setFieldBasedOnType(fieldName, what);
			} else if (obj instanceof Double) {
				valsDouble.put(fieldName, (Double)obj);
				Double val = valsDoubleConfig.get(fieldName).get();
				setFieldBasedOnType(fieldName, val);
			} else if (obj instanceof Boolean) {
				valsBoolean.put(fieldName, (Boolean)obj);
				Boolean val = valsBooleanConfig.get(fieldName).get();
				setFieldBasedOnType(fieldName, val);
			} else {
				//dbg("unhandled datatype, update initField");
			}
		} catch (Exception ex) { ex.printStackTrace(); }
	}
	
	public void initData() {
    	valsString.clear();
    	valsInteger.clear();
    	valsDouble.clear();
    	valsBoolean.clear();
    	
    	updateHashMaps();
    }
	
	public boolean updateField(String name, Object obj) {
    	if (setFieldBasedOnType(name, obj)) {
        	//writeHashMapsToFile();
    		writeConfigFile(true);
        	return true;
    	}
    	return false;
    }
    
    public boolean setFieldBasedOnType(String name, Object obj) {
    	try {
    		if (valsString.containsKey(name)) {
    			OldUtil.setPrivateValue(configClass, configInstance, name, (String)obj);
    			inputField(name, (String)obj);
    		} else if (valsInteger.containsKey(name)) {
    			OldUtil.setPrivateValue(configClass, configInstance, name, Integer.valueOf(obj.toString()));
    			inputField(name, Integer.valueOf(obj.toString()));
    		} else if (valsDouble.containsKey(name)) {
    			OldUtil.setPrivateValue(configClass, configInstance, name, Double.valueOf(obj.toString()));
    			inputField(name, Double.valueOf(obj.toString()));
    		} else if (valsBoolean.containsKey(name)) {
    			OldUtil.setPrivateValue(configClass, configInstance, name, Boolean.valueOf(obj.toString()));
    			inputField(name, Boolean.valueOf(obj.toString()));
    		} else {
    			return false;
    		}
    		
    		return true;
    	}
    	catch (Exception ex) {
    		ex.printStackTrace();
    	}
    	return false;
    }
    
    /*public void writeHashMapsToFile() {
    	Iterator it = valsString.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry pairs = (Map.Entry)it.next();
	        String name = (String)pairs.getKey();
	        Object val = pairs.getValue();
	    }
    }*/
    
    private void processField(String fieldName) {
    	try {
	    	Object obj = ConfigMod.getField(configID, fieldName);
	    	if (obj instanceof String) {
	    		valsString.put(fieldName, (String)obj);
	    	} else if (obj instanceof Integer) {
	    		valsInteger.put(fieldName, (Integer)obj);
	    	} else if (obj instanceof Double) {
	    		valsDouble.put(fieldName, (Double)obj);
	    	} else if (obj instanceof Boolean) {
	    		valsBoolean.put(fieldName, (Boolean)obj);
	    	} else {
	    		//dbg("unhandled datatype, update initField");
	    	}
    	} catch (Exception ex) { ex.printStackTrace(); }
    }
    
    private void inputField(String fieldName, Object obj) {
    	if (obj instanceof String) {
    		valsString.put(fieldName, (String)obj);
    	} else if (obj instanceof Integer) {
    		valsInteger.put(fieldName, (Integer)obj);
    	} else if (obj instanceof Double) {
    		valsDouble.put(fieldName, (Double)obj);
    	} else if (obj instanceof Boolean) {
    		valsBoolean.put(fieldName, (Boolean)obj);
    	} else {
    		
    	}
    }
    
    public void writeConfigFile(boolean resetConfig) {

		//TODO: see if we need support for resetting config
        //if (resetConfig) if (saveFilePath.exists()) saveFilePath.delete();
    	//preInitConfig = new Configuration(saveFilePath);
    	//preInitConfig.load();
		CoroConfigSpec.Builder BUILDER = new CoroConfigSpec.Builder();
		BUILDER.comment("General mod settings").push("general");
    	
    	Field[] fields = configClass.getDeclaredFields();
    	
    	for (int i = 0; i < fields.length; i++) {
    		Field field = fields[i];
    		String name = field.getName();

    		addToConfig(BUILDER, field, name);
    	}

		CULog.dbg("writeConfigFile invoked for " + this.configID + ", resetConfig: " + resetConfig);
		BUILDER.pop();
		CoroConfigSpec CONFIG = BUILDER.build();
		//ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG, saveFilePath + ".toml");
		CoroModConfig config = new CoroModConfig(CoroModConfig.Type.COMMON, CONFIG, container, saveFilePath + ".toml");
		container.addConfig(config);
		CoroConfigTracker.INSTANCE.openConfig(config, FMLPaths.CONFIGDIR.get());

		//reloadSpecificConfig();
		updateConfigFieldValues();
		configInstance.hookUpdatedValues();
    }

	public void updateConfigFileWithRuntimeValues() {
		Field[] fields = configClass.getDeclaredFields();

		for (int i = 0; i < fields.length; i++) {
			Field field = fields[i];
			String name = field.getName();
			saveField(name);
		}
	}

	//updates values in lists, updates forges config field, saves field
	private void saveField(String fieldName) {
		try {
			Object obj = ConfigMod.getField(configID, fieldName);
			if (obj instanceof String) {
				valsString.put(fieldName, (String)obj);
				valsStringConfig.get(fieldName).set((String)obj);
				valsStringConfig.get(fieldName).save();
			} else if (obj instanceof Integer) {
				valsInteger.put(fieldName, (Integer)obj);
				valsIntegerConfig.get(fieldName).set((Integer)obj);
				valsIntegerConfig.get(fieldName).save();
			} else if (obj instanceof Double) {
				valsDouble.put(fieldName, (Double)obj);
				valsDoubleConfig.get(fieldName).set((Double)obj);
				valsDoubleConfig.get(fieldName).save();
			} else if (obj instanceof Boolean) {
				valsBoolean.put(fieldName, (Boolean)obj);
				valsBooleanConfig.get(fieldName).set((Boolean)obj);
				valsBooleanConfig.get(fieldName).save();
			} else {
				//dbg("unhandled datatype, update initField");
			}
		} catch (Exception ex) { ex.printStackTrace(); }
	}
    
    /**
     * Perform the actual adding of values to the config file
     * @param name Name of the variable
     * @param field Field in the file the variable is
     */
    private void addToConfig(CoroConfigSpec.Builder builder, Field field, String name) {

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

        Object obj = ConfigMod.getField(configID, name);
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
