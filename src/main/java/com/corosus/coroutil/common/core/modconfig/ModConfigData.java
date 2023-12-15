package com.corosus.coroutil.common.core.modconfig;

import com.corosus.coroutil.common.core.util.OldUtil;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ModConfigData {
	public String configID;
	public Class configClass;
	public IConfigCategory configInstance;
	
	public HashMap<String, String> valsString = new HashMap<>();
	public HashMap<String, Integer> valsInteger = new HashMap<>();
	public HashMap<String, Double> valsDouble = new HashMap<>();
	public HashMap<String, Boolean> valsBoolean = new HashMap<>();

	//Client data
	public List<ConfigEntryInfo> configData = new ArrayList<>();
    public String saveFilePath;

	public ModConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
		configID = parStr;
		configClass = parClass;
		configInstance = parConfig;
		saveFilePath = savePath;
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
			Object obj = ConfigMod.instance().getField(configID, fieldName);
			if (obj instanceof String) {
				valsString.put(fieldName, (String)obj);
				setFieldBasedOnType(fieldName, getConfigString(fieldName));
			} else if (obj instanceof Integer) {
				valsInteger.put(fieldName, (Integer)obj);
				setFieldBasedOnType(fieldName, getConfigInteger(fieldName));
			} else if (obj instanceof Double) {
				valsDouble.put(fieldName, (Double)obj);
				setFieldBasedOnType(fieldName, getConfigDouble(fieldName));
			} else if (obj instanceof Boolean) {
				valsBoolean.put(fieldName, (Boolean)obj);
				setFieldBasedOnType(fieldName, getConfigBoolean(fieldName));
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
	    	Object obj = ConfigMod.instance().getField(configID, fieldName);
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
    
    public abstract void writeConfigFile(boolean resetConfig);

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
			Object obj = ConfigMod.instance().getField(configID, fieldName);
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
			setConfig(fieldName, obj);
		} catch (Exception ex) { ex.printStackTrace(); }
	}/*

	public abstract void initConfigString(String name, String comment, String value);

	public abstract void initConfigInteger(String name, String comment, int value, int min, int max);

	public abstract void initConfigDouble(String name, String comment, double value, double min, double max);

	public abstract void initConfigBoolean(String name, String comment, boolean value);*/

	public abstract String getConfigString(String fieldName);

	public abstract Integer getConfigInteger(String fieldName);

	public abstract Double getConfigDouble(String fieldName);

	public abstract Boolean getConfigBoolean(String fieldName);

	public abstract <T> void setConfig(String fieldName, T value);

}
