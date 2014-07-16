package CoroUtil.bt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import CoroUtil.ability.Ability;
import CoroUtil.bt.nodes.AttackMeleeBest;
import CoroUtil.bt.nodes.AttackRangedBest;
import CoroUtil.bt.nodes.CombatLogic;
import CoroUtil.bt.nodes.Flee;
import CoroUtil.bt.nodes.TrackTarget;
import CoroUtil.bt.nodes.Wander;
import CoroUtil.bt.selector.Selector;
import CoroUtil.bt.selector.SelectorConcurrent;
import CoroUtil.entity.render.AnimationStateObject;
import CoroUtil.entity.render.TechneModelCoroAI;
import CoroUtil.forge.CoroAI;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilAbility;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PersonalityProfile {

	//control priorities:
	//loyalty to duty
	//self discipline
	//true nature
	
	//final result from:
	//self discipline * loyalty to duty (0-1) * true nature factor if triggered (0-1)
	//true nature might come in when the situation becomes desparate, if they continue to fight to protect, or flee for their own survival, to 
	
	public AIBTAgent agent;
	
	public Selector btSurviving;
	public Selector btAttacking;
	public Selector btIdling;
	
	//most of these values will range from 0 to 1
	
	//0 = avoids, 1 = attacks
	public float aggression = 0.5F;
	public float intelligence = 0.5F;
	
	public float loyalty_job = 0.8F;
	public float loyalty_personal = 0.8F; //not sure when this comes in, need personal desires
	
	//Combat related stuff
	//public boolean hasMeleeWeapon = true;
	//public boolean hasRangedWeapon = false;
	
	//For default weapons, this should eventually tie into the itemstack somehow...
	//public float attackDistMelee = 3;
	//public float attackRateMelee = 10;
	//public float attackDistRanged = 20;
	//public float attackRateRanged = 40;
	
	//runtimes moved here
	//public float attackCooldownMelee = 0;
	//public float attackCooldownRanged = 0;

    public ConcurrentHashMap<String, Ability> abilities = new ConcurrentHashMap<String, Ability>();
	//public List<String> listAssignedAbilities = new ArrayList<String>();
	/*public List<Ability> listAbilities;*/
	public List<Ability> listAbilitiesMelee = new ArrayList<Ability>();
	public List<Ability> listAbilitiesRanged = new ArrayList<Ability>();
	
	public float abilitySyncRange = 64F;
	
	@SideOnly(Side.CLIENT)
	public ConcurrentHashMap<String, AnimationStateObject> animationData;
	
	public PersonalityProfile(AIBTAgent parAgent) {
		agent = parAgent;
		abilities = new ConcurrentHashMap<String, Ability>();
		
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			animationData = new ConcurrentHashMap<String, AnimationStateObject>();
		}
	}
	
	//DOES THIS METHOD WORK FOR RELOADING? i bet it doesnt
	public void addAbilityRemap(Ability parAbility, String addAsName, int type) {
		System.out.println("warning: addAbilityRemap fails to remap for entity nbt reloads, needs fix or removal");
		abilities.put(addAsName, parAbility);
		parAbility.type = type;
		
		//maybe redundant, maybe only redundant on nbt reload due to updateListCache()
		if (type == Ability.TYPE_MELEE) {
			listAbilitiesMelee.add(parAbility);
		} else if (type == Ability.TYPE_RANGED) {
			listAbilitiesRanged.add(parAbility);
		}
	}
	
	public void addAbility(Ability parAbility, int type) {
		abilities.put(parAbility.name, parAbility);
		parAbility.type = type;
		
		//maybe redundant, maybe only redundant on nbt reload due to updateListCache()
		if (type == Ability.TYPE_MELEE) {
			listAbilitiesMelee.add(parAbility);
		} else if (type == Ability.TYPE_RANGED) {
			listAbilitiesRanged.add(parAbility);
		}
	}
	
	public void addAbilityMelee(Ability parAbility) {
		addAbilityMelee(parAbility.name, parAbility);
	}
	
	public void addAbilityMelee(String name, Ability parAbility) {
		if (abilities.contains(name)) {
			System.out.println("AI warning, adding ability " + name + " overtop preexisting one");
		}
		abilities.put(name, parAbility);
		parAbility.type = Ability.TYPE_MELEE;
		
		listAbilitiesMelee.add(parAbility);
	}
	
	public void addAbilityRanged(Ability parAbility) {
		addAbilityRanged(parAbility.name, parAbility);
	}
	
	public void addAbilityRanged(String name, Ability parAbility) {
		abilities.put(name, parAbility);
		parAbility.type = Ability.TYPE_RANGED;
		
		listAbilitiesRanged.add(parAbility);
	}
	
	public void updateListCache() {
		listAbilitiesMelee.clear();
		listAbilitiesRanged.clear();
		
		for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
			if (entry.getValue().type == Ability.TYPE_MELEE) {
				listAbilitiesMelee.add(entry.getValue());
			} else if (entry.getValue().type == Ability.TYPE_RANGED) {
				listAbilitiesRanged.add(entry.getValue());
			}
		}
	}
	
	public void tickAbilities() {
		
		//Persistant animation stuff - needs better consideration
		//Make these movement methods event based not time loop based, reduces packet load of updating their states when not needed (in theory)
		if (FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
			if (agent.blackboard.isMoving.getValue()) {
				Ability ability = abilities.get("Walk");
				if (ability != null) {
					if (!ability.isActive()) {
						
						abilityStart(ability, null);
						
					}
					Ability ability2 = abilities.get("Idle");
					if (ability2 != null) {
						if (!ability2.isActive()) {
							ability2.setFinishedEntirely();
							syncAbility(ability2);
						}
					}
				}
			} else {
				if (agent.blackboard.getTarget() == null) {
					Ability ability = abilities.get("Idle");
					if (ability != null) {
						if (!ability.isActive()) {
							abilityStart(ability, null);
						}
					}
				}
			}
		}
		
		for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
			if (entry.getValue().isActive()) {
				entry.getValue().tick();
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void tickAbilitiesRender(TechneModelCoroAI model) {
		for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
			if (entry.getValue().isActive()) {
				entry.getValue().tickRender(model);
			}
		}
	}
	
	public void init() {
		btSurviving = new SelectorConcurrent(null);
		btAttacking = new SelectorConcurrent(null);
		btIdling = new SelectorConcurrent(null);
		
	}
	
	public void initDefaultProfile() {
		btSurviving.add(new Flee(btSurviving, agent.entInt, agent.blackboard));
		btIdling.add(new Wander(btIdling, agent.entInt, agent.blackboard, 8));
		
		//temp combat movement
		btAttacking.add(new TrackTarget(null, agent.entInt, agent.blackboard));
		//btAttacking.add(new AttackMelee(btAttacking, agent.entInt, agent.blackboard, 3F, 5));
		
		CombatLogic selCombat = new CombatLogic(null, agent.entInt);
		selCombat.add(new AttackMeleeBest(null, agent.entInt));
		selCombat.add(new AttackRangedBest(null, agent.entInt));
		
		btAttacking.add(selCombat);
	}
	
	public void initProfile(int profileType) {
		initDefaultProfile();
	}
	
	public void syncAbilitiesFull(boolean rangeOverride) {
		System.out.println("full sync abilities - " + agent.ent);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("command", "syncAbilities");
		nbt.setInteger("entityID", agent.ent.getEntityId());
		nbt.setTag("abilities", CoroUtilAbility.nbtSaveAbilities(abilities));
		FMLProxyPacket packet = PacketHelper.getNBTPacket(nbt, CoroAI.eventChannelName);//PacketHelper.createPacketForNBTHandler("CoroAI_Ent", nbt);
		if (rangeOverride) {
			CoroAI.eventChannel.sendToDimension(packet, agent.ent.worldObj.provider.dimensionId);
			//PacketDispatcher.sendPacketToAllInDimension(packet, agent.ent.worldObj.provider.dimensionId);
		} else {
			CoroAI.eventChannel.sendToAllAround(packet, new NetworkRegistry.TargetPoint(agent.ent.worldObj.provider.dimensionId, agent.ent.posX, agent.ent.posY, agent.ent.posZ, abilitySyncRange));
			//PacketDispatcher.sendPacketToAllAround(agent.ent.posX, agent.ent.posY, agent.ent.posZ, abilitySyncRange, agent.ent.worldObj.provider.dimensionId, packet);
		}
	}
	
	public void syncAbility(Ability ability) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("command", "syncAbilities");
		nbt.setInteger("entityID", agent.ent.getEntityId());
		nbt.setTag("abilities", CoroUtilAbility.nbtSyncWriteAbility(ability, false));
		//Packet packet = PacketHelper.createPacketForNBTHandler("CoroAI_Ent", nbt);
		FMLProxyPacket packet = PacketHelper.getNBTPacket(nbt, CoroAI.eventChannelName);
		CoroAI.eventChannel.sendToAllAround(packet, new NetworkRegistry.TargetPoint(agent.ent.worldObj.provider.dimensionId, agent.ent.posX, agent.ent.posY, agent.ent.posZ, abilitySyncRange));
		//PacketDispatcher.sendPacketToAllAround(agent.ent.posX, agent.ent.posY, agent.ent.posZ, abilitySyncRange, agent.ent.worldObj.provider.dimensionId, packet);
	}
	
	public void nbtSyncRead(NBTTagCompound par1nbtTagCompound) {
		//override with mod specific profile that uses own skill mapping
	}
	
	public void nbtRead(NBTTagCompound par1nbtTagCompound) {
		//override with mod specific profile that uses own skill mapping
	}
	
    public void nbtWrite(NBTTagCompound par1nbtTagCompound) {
    	par1nbtTagCompound.setTag("abilities", CoroUtilAbility.nbtSaveAbilities(abilities));
	}
	
	public boolean shouldFollowOrders() {
		float healthRatio = agent.ent.getHealth() / agent.ent.getMaxHealth();
		if (healthRatio < (1F - ((loyalty_job + aggression) / 2F))) {
			return false;
		}
		return true;
	}
	
	public boolean shouldTrySurvival() {
		float healthRatio = agent.ent.getHealth() / agent.ent.getMaxHealth();
		if (healthRatio < (1F - aggression)) {
			return true;
		}
		return false;
	}
	
	public void updateAttackInfo() {
		//hasMeleeWeapon = true;
		//hasRangedWeapon = false;
	}
	
	public boolean canMelee() {
		//temp skill logic
		boolean hasMeleeSkill = listAbilitiesMelee.size() > 0;
		if (/*hasMeleeWeapon || */hasMeleeSkill) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canRanged() {
		//temp skill logic
		boolean hasRangedSkill = listAbilitiesRanged.size() > 0;
		if (/*hasRangedWeapon || */hasRangedSkill) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean shouldMelee(float curRange) {
		if (!canMelee()) return false;
		
		//if theres a skill within acceptable range given, return true...... return skill??
		//for now...
		//IDEA: just return true if any melee skill, then let the 'invoke best melee attack' behavior choose the actual skill used...
		return true;
	}
	
	public boolean shouldRanged(float curRange) {
		if (!canRanged()) return false;
		
		return true;
	}
	
	public void abilityStart(Ability ability, Entity parTarget) {
		//System.out.println("server: start ability: " + ability.name + " - " + agent.ent.getEntityName());
		ability.reset(); //incase skill wasnt reset properly, interrupted maybe?
		ability.setTarget(parTarget);
		ability.setActive();
		syncAbility(ability);
	}
	
	/*public void attackMeleeTrigger(Entity parTarget) {
		attackCooldownMelee = attackRateMelee;
		attackMeleePerform(parTarget);
	}
	
	public void attackMeleePerform(Entity parTarget) {
		parTarget.attackEntityFrom(new EntityDamageSource("mob", agent.ent), 5);
		agent.ent.swingItem();
	}
	
	public void attackRangedTrigger(Entity parTarget) {
		attackCooldownRanged = attackRateRanged;
		attackRangedPerform(parTarget);
	}
	
	public void attackRangedPerform(Entity parTarget) {
		parTarget.attackEntityFrom(new EntityDamageSource("mob", agent.ent), 5);
		agent.ent.swingItem();
	}*/
}
