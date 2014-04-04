package CoroUtil.componentAI;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;



public abstract interface ICoroAI 
{
	public AIAgent getAIAgent();
	
	public void setPathResultToEntity(PathEntity pathentity); //named this way to prevent mcp obfuscating it (matched a method signature)
	//public PathEntity getPath();
	public boolean isBreaking();
	public boolean isEnemy(Entity ent);
	
	public int getCooldownMelee();
	public int getCooldownRanged();
	
	//Used for non inventory using AIs?
	public void attackMelee(Entity ent, float dist);
	public void attackRanged(Entity ent, float dist);
	
	/* Required to cleanup memory usage, set your agent reference to null */
	public void cleanup();
	
}