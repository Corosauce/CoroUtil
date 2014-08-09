package modconfig;

public interface IConfigCategory {

	public String getConfigFileName();
	public String getCategory();
	public void hookUpdatedValues();
	
}
