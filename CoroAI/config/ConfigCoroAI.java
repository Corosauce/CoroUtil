package CoroAI.config;

import modconfig.IConfigCategory;

public class ConfigCoroAI implements IConfigCategory {

	public static boolean chunkCacheOverworldOnly = false;
	
	@Override
	public String getConfigFileName() {
		return "CoroAI";
	}

	@Override
	public String getCategory() {
		return "CoroAI";
	}

	@Override
	public void hookUpdatedValues() {
		// TODO Auto-generated method stub
		
	}

}
