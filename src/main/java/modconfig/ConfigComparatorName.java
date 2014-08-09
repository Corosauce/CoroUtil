package modconfig;

import java.util.Comparator;

public class ConfigComparatorName implements Comparator<ConfigEntryInfo> {

	@Override
	public int compare(ConfigEntryInfo arg0, ConfigEntryInfo arg1) {
		// TODO Auto-generated method stub
		return arg0.name.compareToIgnoreCase(arg1.name);
	}

}
