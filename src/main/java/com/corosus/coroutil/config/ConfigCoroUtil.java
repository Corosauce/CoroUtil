package com.corosus.coroutil.config;

import com.corosus.modconfig.ConfigParams;
import com.corosus.modconfig.IConfigCategory;

import java.io.File;

public class ConfigCoroUtil implements IConfigCategory {

	@ConfigParams(comment = "logging", min = 0, max = 0)
	public static boolean useLoggingLog = true;
	public static boolean useLoggingDebug = false;
	public static boolean useLoggingError = true;

	@Override
	public String getName() {
		return "General";
	}

	@Override
	public String getRegistryName() {
		return "coroutil_general";
	}

	@Override
	public String getConfigFileName() {
		return "CoroUtil" + File.separator + getName();
	}

	@Override
	public String getCategory() {
		return getName();
	}

	@Override
	public void hookUpdatedValues() {

	}


}
