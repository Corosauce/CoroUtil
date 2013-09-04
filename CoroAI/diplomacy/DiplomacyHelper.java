package CoroAI.diplomacy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import CoroAI.componentAI.AITamable;
import CoroAI.componentAI.ICoroAI;
import CoroAI.entity.c_EnhAI;

public class DiplomacyHelper {

	public String[] noAttackEnts;
	public Class[] noAttackEntsCls;
	
	public static boolean shouldTargetEnt(ICoroAI source, Entity target) {
		try {
			if (target instanceof ICoroAI) {
				//use team instance system
				TeamInstance tiSource = source.getAIAgent().dipl_info;
				TeamInstance tiTarget = ((ICoroAI)target).getAIAgent().dipl_info;
				if (tiSource.isEnemy(tiTarget)) return true;
			} else {
				if (target instanceof c_EnhAI) return true;
				if ((target instanceof EntityMob || target instanceof EntitySlime) && !source.getAIAgent().isThreat(target)) return true;
			}
		} catch (Exception ex) {
			//this will happen when ent death reference happens, cleanup, issue line was 'TeamInstance tiTarget = ((ICoroAI)target).getAIAgent().dipl_info;'
		}
		return false;
	}
	
	public static boolean shouldTameTargetEnt(EntityLivingBase source, Entity target, AITamable tamable) {
		//If new ai
		try {
			if (target instanceof ICoroAI) {
				if (tamable.ownerCachedInstance != null) {
					//If the target is an enemy of our owner, attack it
					if (((ICoroAI)target).getAIAgent().jobMan.getPrimaryJob().isEnemy(tamable.ownerCachedInstance)) {
						return true;
					}
				}
			}
			if (target instanceof c_EnhAI) return true;
			if ((target instanceof EntityMob || target instanceof EntitySlime) && !tamable.job.entInt.getAIAgent().isThreat(target)) return true;
			if (source instanceof ICoroAI) return shouldTargetEnt((ICoroAI)source, target);
		} catch (Exception ex) {
			//this will happen when ent death reference happens, cleanup, issue line was 'TeamInstance tiTarget = ((ICoroAI)target).getAIAgent().dipl_info;'
		}
		return false;
	}
	
}
