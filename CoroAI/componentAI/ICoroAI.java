package CoroAI.componentAI;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;



public abstract interface ICoroAI 
{
	public AIAgent getAIAgent();
	
	public void setPathToEntity(PathEntity pathentity);
	//public PathEntity getPath();
	public boolean isBreaking();
	public boolean isEnemy(Entity ent);
	
	public int getCooldownMelee();
	public int getCooldownRanged();
	
	//Used for non inventory using AIs?
	public void attackMelee(Entity ent, float dist);
	public void attackRanged(Entity ent, float dist);
	
}