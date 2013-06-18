package CoroAI.componentAI;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;

public class DiplomacyHelper {

	public String[] noAttackEnts;
	public Class[] noAttackEntsCls;
	
	public static boolean shouldTargetEnt(EntityLiving source, Entity target, boolean isTame) {
		if (target instanceof ICoroAI) return false; //temp
		if (isTame) {
			if (target instanceof EntityMob && !(target instanceof EntityCreeper)) {
				return true;
			}
		}
		return false;
	}
	
}
