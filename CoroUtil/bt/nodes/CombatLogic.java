package CoroUtil.bt.nodes;

import java.util.List;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.Vec3;
import CoroUtil.bt.AIBTAgent;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.PersonalityProfile;
import CoroUtil.bt.selector.Selector;

public class CombatLogic extends Selector {

	public IBTAgent entInt;
	public EntityLiving ent;
	public BlackboardBase blackboard;
	public PersonalityProfile profile;
	
	public CombatLogic(Behavior parParent, IBTAgent parEnt) {
		super(parParent);
		blackboard = parEnt.getAIBTAgent().blackboard;
		profile = parEnt.getAIBTAgent().profile;
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
	}

	@Override
	public EnumBehaviorState tick() {
		
		/*if (profile.attackCooldownMelee > 0) profile.attackCooldownMelee--;
		if (profile.attackCooldownRanged > 0) profile.attackCooldownRanged--;*/

		blackboard.isUsingMelee.setValue(((AttackMeleeBest)children.get(0)).comboActive()/* || profile.attackCooldownMelee > 0*/);
		blackboard.isUsingRanged.setValue(((AttackRangedBest)children.get(1)).comboActive()/* || profile.attackCooldownRanged > 0*/);
		
		Entity target = blackboard.getTarget();
		if (target != null) {
			float dist = ent.getDistanceToEntity(target);

			//System.out.println(blackboard.isUsingMelee.getValue() + " - " + ent);
			
			if (!blackboard.isUsingMelee.getValue() && !blackboard.isUsingRanged.getValue()) {
				if (profile.shouldMelee(dist)) {
					
					//child 0 tick
					return children.get(0).tick();
					
				} else {
					if (profile.shouldRanged(dist)) {
						//child 1 tick
						return children.get(1).tick();
					}
				}
			}
			
			
			
		}
		
		return super.tick();
	}
	
}
