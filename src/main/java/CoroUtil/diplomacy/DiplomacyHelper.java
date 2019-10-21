package CoroUtil.diplomacy;

import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import CoroUtil.bt.IBTAgent;
import net.minecraft.entity.player.PlayerEntity;

public class DiplomacyHelper {

	public String[] noAttackEnts;
	public Class[] noAttackEntsCls;
	
	/*public static boolean shouldTargetEnt(ICoroAI source, Entity target) {
		try {
			if (target instanceof ICoroAI) {
				//use team instance system
				TeamInstance tiSource = source.getAIAgent().dipl_info;
				TeamInstance tiTarget = ((ICoroAI)target).getAIAgent().dipl_info;
				if (tiSource.isEnemy(tiTarget)) return true;
			} else if (target instanceof IBTAgent) {
				//use team instance system
				TeamInstance tiSource = source.getAIAgent().dipl_info;
				TeamInstance tiTarget = ((IBTAgent)target).getAIBTAgent().dipl_info;
				if (tiSource.isEnemy(tiTarget)) return true;
			} else {
				if ((target instanceof EntityMob || target instanceof EntitySlime) && !source.getAIAgent().isThreat(target)) return true;
			}
		} catch (Exception ex) {
			//this will happen when ent death reference happens, cleanup, issue line was 'TeamInstance tiTarget = ((ICoroAI)target).getAIAgent().dipl_info;'
		}
		return false;
	}*/
	
	public static boolean shouldTargetEnt(IBTAgent source, Entity target) {
		TeamInstance tiSource = source.getAIBTAgent().dipl_info;
		try {
			
			TeamInstance tiTarget = null;
			
			/*if (target instanceof ICoroAI) {
				tiTarget = ((ICoroAI)target).getAIAgent().dipl_info;
			} else */if (target instanceof IBTAgent) {
				tiTarget = ((IBTAgent)target).getAIBTAgent().dipl_info;
			} else if (target instanceof PlayerEntity) {
				if (!((PlayerEntity)target).capabilities.isCreativeMode) {
					tiTarget = TeamTypes.getMinecartType("player");//if (tiSource.listEnemies.contains("player")) return true;
				}
			} else {
				//custom rule to avoid vanilla threats that wont attack us if left alone
				if (!isThreat(target)) {
					if ((target instanceof MonsterEntity || target instanceof SlimeEntity)) {
						tiTarget = TeamTypes.getMinecartType("undead");
					} else if (target instanceof AnimalEntity) {
						tiTarget = TeamTypes.getMinecartType("animal");
					} else {
						tiTarget = TeamTypes.getMinecartType("neutral");
					}
				}
			}

			if (tiTarget == null) return false;
			if (tiSource.isEnemy(tiTarget)) return true;
			
		} catch (Exception ex) {
			//this will happen when ent death reference happens, cleanup, issue line was 'TeamInstance tiTarget = ((ICoroAI)target).getAIAgent().dipl_info;'
		}
		return false;
	}
	
	public static boolean isThreat(Entity ent) {
		if (ent instanceof CreeperEntity || ent instanceof EndermanEntity) {
			return true;
		}
		return false;
	}
	
	/*public static boolean shouldTameTargetEnt(EntityLivingBase source, Entity target, AITamable tamable) {
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
			if ((target instanceof EntityMob || target instanceof EntitySlime) && !tamable.job.entInt.getAIAgent().isThreat(target)) return true;
			if (source instanceof ICoroAI) return shouldTargetEnt((ICoroAI)source, target);
		} catch (Exception ex) {
			//this will happen when ent death reference happens, cleanup, issue line was 'TeamInstance tiTarget = ((ICoroAI)target).getAIAgent().dipl_info;'
		}
		return false;
	}*/
	
}

