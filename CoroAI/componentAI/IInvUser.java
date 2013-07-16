package CoroAI.componentAI;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.PathEntity;



public abstract interface IInvUser 
{
	public void postInitFakePlayer();
	
	public String getLocalizedName();
}