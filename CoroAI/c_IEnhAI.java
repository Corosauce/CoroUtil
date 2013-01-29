package CoroAI;


public abstract interface c_IEnhAI
{
	public float getMoveSpeed();
	//public int health = 0;
	public boolean canUseLadders();
	
	//public boolean isPathableBlock(c_IEnhAI ent, int id, int meta, int x, int y, int z);
	//public void pfComplete(PathEntityEx pathEx);
	
	public int overrideBlockPathOffset(c_IEnhAI ent, int id, int meta, int x, int y, int z);
	
}