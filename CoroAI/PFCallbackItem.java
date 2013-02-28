package CoroAI;

import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;

public class PFCallbackItem {
	
	public PathEntity pe;
	public EntityLiving ent;
	public float speed;
	public boolean foundEnd = false;
	
	public PFCallbackItem(PathEntity parPE, EntityLiving parEnt, float parSpeed) {
		pe = parPE;
		ent = parEnt;
		speed = parSpeed;
	}

	public PFCallbackItem(PathEntity parPE, EntityLiving parEnt, float parSpeed, boolean parFound) {
		this(parPE, parEnt, parSpeed);
		foundEnd = parFound;
	}
	
}
