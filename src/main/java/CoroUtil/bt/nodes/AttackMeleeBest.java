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

public class AttackMeleeBest extends Selector implements IAbilityUsageCallback {

	//Convert default melee into a default melee ability, for all the benefits of ability system for all attacks
	
	//This and AttackRangedBest will use default melee and skill melee based on situation  
	//- will update blackboard.isUsingMelee/Ranged
	//- no combos, just strait up uses a skill, so
	//- this COULD use ISkillCallback
	//- default attack cooldown from profile i guess
	//- custom case handle default attack
	//- for skills, decide on what one to use and...
	//- since skills are instances in a list, make this class invoke it and watch like a newly adapted ActionUseSkill would
	
	//default is insta performed and starts cooldown
	//skills are watched over for callback
	//dont forget, skills tick like: chargeup, perform, cooldown, marked ready
	
	//design flaw, default attack requires this to be ticked, skills were added so they only need the 1 call from CombatLogic
	
	public IBTAgent entInt;
	public EntityLiving ent;
	public BlackboardBase blackboard;
	public PersonalityProfile profile;
	
	//public float attackCooldown = 0; //for only default attack
	public Ability activeAbility = null;
	public boolean activeIsDefault = false; //this might have logic issues as its semi runtime, might not as its used in events
	
	//public int activeComboIndex = -1;
	
	private MutableBoolean isReady = new MutableBoolean(true);
	
	public AttackMeleeBest(Behavior parParent, IBTAgent parEnt) {
		super(parParent);
		blackboard = parEnt.getAIBTAgent().blackboard;
		profile = parEnt.getAIBTAgent().profile;
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
	}

	@Override
	public EnumBehaviorState tick() {
		
		boolean isReadyBool = /*(activeIsDefault && profile.attackCooldownMelee <= 0) || */activeAbility == null;
		
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
				if (profile.listAbilitiesMelee.size() > 0) {
					int tryCount = 0;
					int maxTries = profile.listAbilitiesMelee.size();
					
					Ability ability = null;
					while (true) {
						
						ability = profile.listAbilitiesMelee.get(rand.nextInt(profile.listAbilitiesMelee.size()));
						
						
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
				
				/*if (!usedAbility) {
					if (dist < profile.attackDistMelee) {
						profile.attackMeleeTrigger(target);
					}
				}*/
			}
		}
		
		isReady.setValue(isReadyBool);
		
		return super.tick();
	}
	
	public MutableBoolean getBooleanRef() {
		return isReady;
	}
	
	public boolean comboActive() {
		//added to fix issue of it not updating due to tick possible ordering lock out
		boolean isReadyBool = /*(activeIsDefault && profile.attackCooldownMelee <= 0) || */activeAbility == null;
		isReady.setValue(isReadyBool);
		return !isReady.getValue();
	}
	
	public void abilityStart(Ability ability, Entity parTarget) {
		/*if (ability == null) {
			activeIsDefault = true;
			attackCooldown = profile.attackRateMelee;
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
		//System.out.println("combo finished" + " - " + ent);
		//activeComboIndex = -1;
		if (activeAbility != null) profile.syncAbility(activeAbility);
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
