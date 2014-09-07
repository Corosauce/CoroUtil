package CoroUtil.bt.nodes;

import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;

import org.apache.commons.lang3.mutable.MutableBoolean;

import CoroUtil.ability.Ability;
import CoroUtil.ability.IAbilityUsageCallback;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.PersonalityProfile;
import CoroUtil.bt.selector.Selector;

public class AttackRangedBest extends Selector implements IAbilityUsageCallback {

	public IBTAgent entInt;
	public EntityLiving ent;
	public BlackboardBase blackboard;
	public PersonalityProfile profile;
	
	//public float attackCooldown = 0; //for only default attack
	public Ability activeAbility = null;
	public boolean activeIsDefault = false;
	
	//public int activeComboIndex = -1;
	
	private MutableBoolean isReady = new MutableBoolean(true);
	
	public AttackRangedBest(Behavior parParent, IBTAgent parEnt) {
		super(parParent);
		blackboard = parEnt.getAIBTAgent().blackboard;
		profile = parEnt.getAIBTAgent().profile;
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
	}

	@Override
	public EnumBehaviorState tick() {
		
		boolean isReadyBool = /*(activeIsDefault && profile.attackCooldownRanged <= 0) || */activeAbility == null;
		
		boolean randomChoice = true;
		int oddsToUseCombo = 30;
		
		Random rand = new Random();
		
		if (isReadyBool) {
			Entity target = blackboard.getTarget();
			if (target != null && ent.canEntityBeSeen(target)) {
				double dist = ent.getDistanceToEntity(target);
				
				//need indexing! for now:
				//- choose random ability, check dist
				//- if cant use because dist, try default dist and default
				
				//boolean usedAbility = false;
				
				//tries for a count of ability list size or until it finds an ability that isnt active
				if (profile.listAbilitiesRanged.size() > 0) {
					int tryCount = 0;
					int maxTries = profile.listAbilitiesRanged.size();
					
					Ability ability = null;
					while (true) {
						
						ability = profile.listAbilitiesRanged.get(rand.nextInt(profile.listAbilitiesRanged.size()));
						
						
						if (ability.canActivate() && dist <= ability.bestDist + ability.bestDistRange/2 && dist >= ability.bestDist - ability.bestDistRange/2) {
							
						} else {
							ability = null;
						}
						
						if (ability != null || tryCount++ >= maxTries) {
							break;
						}
					}
					
					if (ability != null) {
						abilityStart(ability, target);
					}
					
					
					/*if (dist <= ability.bestDist + ability.bestDistRange/2 && dist >= ability.bestDist - ability.bestDistRange/2) {
						//System.out.println("use ability: " + ability + " - " + ent);
						abilityStart(ability, target);
						//usedAbility = true;
					} else {
						//System.out.println("out of range - " + ent);
					}*/
				}
				
				/*if (profile.listAbilitiesRanged.size() > 0) {
					Ability ability = profile.listAbilitiesRanged.get(rand.nextInt(profile.listAbilitiesRanged.size()));
					if (dist <= ability.bestDist + ability.bestDistRange/2 && dist >= ability.bestDist - ability.bestDistRange/2) {
						abilityStart(ability, target);
						//usedAbility = true;
					}
				}*/
				
				/*if (!usedAbility) {
					if (dist < profile.attackDistRanged) {
						profile.attackRangedTrigger(target);
					}
				}*/
			}
		}
		
		isReady.setValue(isReadyBool);
		blackboard.isUsingRanged.setValue(!isReadyBool);
		
		return super.tick();
	}
	
	public MutableBoolean getBooleanRef() {
		return isReady;
	}
	
	public boolean comboActive() {
		return !isReady.getValue();
	}
	
	public void abilityStart(Ability ability, Entity parTarget) {
		/*if (ability == null) {
			activeIsDefault = true;
			attackCooldown = profile.attackRateRanged;
		} else {*/
		activeIsDefault = false;
		//feed it target?
		activeAbility = ability;
		ability.setCallback(this);
		profile.abilityStart(ability, parTarget);
		//}
		//System.out.println("combo start! " + selectorIndex);
		
		//activeComboIndex = selectorIndex;
		//if (children.get(selectorIndex) instanceof SelectorSequenceControl) ((SelectorSequenceControl)children.get(selectorIndex)).startSequence();
	}
	
	public void abilityFinish() {
		//System.out.println("callback finished");
		//activeComboIndex = -1;
		activeIsDefault = false;
		activeAbility = null;
	}

	@Override
	public void abilityFinished(Ability parAbility) {
		//System.out.println("callback received");
		//incase post cooldown callback from old ability calls while different ability is being used
		if (parAbility == activeAbility) {
			abilityFinish();
		}
	}
	
}
