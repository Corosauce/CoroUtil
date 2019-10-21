package CoroUtil.pathfinding;

import net.minecraft.entity.MobEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.Path;

public class PFCallbackItem {
	
	public Path pe;
	public MobEntity ent;
	public float speed;
	public boolean foundEnd = false;
	
	public PFCallbackItem(Path parPE, MobEntity parEnt, float parSpeed) {
		pe = parPE;
		ent = parEnt;
		speed = parSpeed;
	}

	public PFCallbackItem(Path parPE, MobEntity parEnt, float parSpeed, boolean parFound) {
		this(parPE, parEnt, parSpeed);
		foundEnd = parFound;
	}
	
}
