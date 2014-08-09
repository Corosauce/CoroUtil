package CoroUtil;

import java.util.HashMap;

public class DataLatcher {
	
	public HashMap values;
	
	DataLatcher() {
		values = new HashMap();
		
		//Defaulting values so you dont need to nullcheck
		values.put(DataTypes.followTarg, null);
		values.put(DataTypes.noMoveTicks, 0);
		values.put(DataTypes.noSeeTicks, 0);
		values.put(DataTypes.shouldDespawn, true);
	}
}
