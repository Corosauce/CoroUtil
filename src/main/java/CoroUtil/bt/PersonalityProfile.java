package CoroUtil.bt;

import CoroUtil.ability.Ability;
import CoroUtil.bt.nodes.*;
import CoroUtil.bt.selector.Selector;
import CoroUtil.bt.selector.SelectorConcurrent;
import CoroUtil.entity.render.AnimationStateObject;
import CoroUtil.forge.CoroUtil;
import CoroUtil.packet.PacketHelper;
import CoroUtil.util.CoroUtilAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

	/*public float cacheBestDistMelee = 10;
	public float cacheBestDistRangeMelee = 10; //the acceptable range around bestDist

	public float cacheBestDistRanged = 10;
	public float cacheBestDistRangeRanged = 10; //the acceptable range around bestDist
*/

	public float cacheFurthestMeleeUsable = 0;

	@SideOnly(Side.CLIENT)
	public ConcurrentHashMap<String, AnimationStateObject> animationData;

	public PersonalityProfile(AIBTAgent parAgent) {
		agent = parAgent;
		abilities = new ConcurrentHashMap<String, Ability>();

		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
			animationData = new ConcurrentHashMap<String, AnimationStateObject>();

			Render renderObj = (Render) Minecraft.getMinecraft().getRenderManager().entityRenderMap.get(agent.ent.getClass());
			//TODO: readd 1.8.8
			/*if (renderObj instanceof RenderEntityCoroAI) {

				for (Map.Entry<String, ModelRendererBones> entry : ((RenderEntityCoroAI) renderObj).modelTechne.partsAllChildren.entrySet()) {
					AnimationStateObject obj = new AnimationStateObject(entry.getKey());
					obj.rotateAngleXDesired = entry.getValue().rotateAngleX;
					obj.rotateAngleYDesired = entry.getValue().rotateAngleY;
					obj.rotateAngleZDesired = entry.getValue().rotateAngleZ;
					animationData.put(entry.getKey(), obj);

					//we might need to feed it the techne rotations that we commented out in TechneModelEpochs import
				}
				//((RenderCoroAIEntity) renderObj).modelTechne.parts.get("top")
				animationData.put("top", new AnimationStateObject("top"));
			}*/
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

	public void updateCache() {

		updateListCache();
		updateAbilityInfoCache();
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

	public void updateAbilityInfoCache() {

		cacheFurthestMeleeUsable = 0;

		for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
			Ability ability = entry.getValue();
			if (ability.type == Ability.TYPE_MELEE) {
				if (ability.bestDist + ability.bestDistRange/2 > cacheFurthestMeleeUsable) {
					cacheFurthestMeleeUsable = ability.bestDist + ability.bestDistRange/2;
				}
			} else if (ability.type == Ability.TYPE_RANGED) {

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
					if (ability.canActivate()) {

						abilityStart(ability, null);

					}
					Ability ability2 = abilities.get("Idle");
					if (ability2 != null) {
						//switched to check active instead of not active...
						if (ability2.isActiveOrCoolingDown()) {
							ability2.setFinishedEntirely();
							syncAbility(ability2);
						}
					}
				}
			} else {
				if (agent.blackboard.getTarget() == null) {
					Ability ability = abilities.get("Idle");
					if (ability != null) {
						if (ability.canActivate()) {
							abilityStart(ability, null);
						}
					}
				}
			}
		}

		try {
			for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
				if (entry.getValue().isActiveOrCoolingDown()) {
					entry.getValue().tick();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	@SideOnly(Side.CLIENT)
	public void tickAbilitiesRender(Render parRender) {
		//System.out.println("abilities count: " + abilities.size());
		for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
			//System.out.println("render active? " + entry.getValue().isActive());
			if (entry.getValue().isActiveOrCoolingDown()) {
				entry.getValue().tickRender(parRender);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void tickAbilitiesRenderModel(ModelBase parModel) {
		for (Map.Entry<String, Ability> entry : abilities.entrySet()) {
			//System.out.println("render active? " + entry.getValue().isActive());
			if (entry.getValue().isActiveOrCoolingDown()) {
				entry.getValue().tickRenderModel(parModel);
			}
		}
	}

	public void init() {
		btSurviving = new SelectorConcurrent(null);
		btAttacking = new SelectorConcurrent(null);
		btIdling = new SelectorConcurrent(null);

	}

	public void initProfile(int profileType) {
		if (profileType == -1) {
			initDefaultProfile();
		} else {
			System.out.println("unsupported profileType for PersonalityProfile");
		}
	}

	public void initDefaultProfile() {
		btSurviving.add(new Flee(btSurviving, agent.entInt, agent.blackboard));
		//btIdling.add(new Wander(btIdling, agent.entInt, agent.blackboard, 8));

		//temp combat movement, becomes not so temp!
		btAttacking.add(new TrackTarget(null, agent.entInt, agent.blackboard));
		//btAttacking.add(new AttackMelee(btAttacking, agent.entInt, agent.blackboard, 3F, 5));

		//Ability system divided between melee and ranged abilities, no combos
		CombatLogic selCombat = new CombatLogic(null, agent.entInt);
		selCombat.add(new AttackMeleeBest(null, agent.entInt));
		selCombat.add(new AttackRangedBest(null, agent.entInt));

		btAttacking.add(selCombat);
	}

	public void syncAbilitiesFull(boolean rangeOverride) {
		System.out.println("full sync abilities - " + agent.ent);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("command", "CoroAI_Ent");
		nbt.setInteger("entityID", agent.ent.getEntityId());
		nbt.setTag("abilities", CoroUtilAbility.nbtSaveAbilities(abilities));
		FMLProxyPacket packet = PacketHelper.getNBTPacket(nbt, CoroUtil.eventChannelName);//PacketHelper.createPacketForNBTHandler("CoroAI_Ent", nbt);
		if (rangeOverride) {
			CoroUtil.eventChannel.sendToDimension(packet, agent.ent.world.provider.getDimension());
			//PacketDispatcher.sendPacketToAllInDimension(packet, agent.ent.world.provider.dimensionId);
		} else {
			CoroUtil.eventChannel.sendToAllAround(packet, new NetworkRegistry.TargetPoint(agent.ent.world.provider.getDimension(), agent.ent.posX, agent.ent.posY, agent.ent.posZ, abilitySyncRange));
			//PacketDispatcher.sendPacketToAllAround(agent.ent.posX, agent.ent.posY, agent.ent.posZ, abilitySyncRange, agent.ent.world.provider.dimensionId, packet);
		}
	}

	public void syncAbility(Ability ability) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("command", "CoroAI_Ent");
		nbt.setInteger("entityID", agent.ent.getEntityId());
		nbt.setTag("abilities", CoroUtilAbility.nbtSyncWriteAbility(ability, false));
		//Packet packet = PacketHelper.createPacketForNBTHandler("CoroAI_Ent", nbt);
		FMLProxyPacket packet = PacketHelper.getNBTPacket(nbt, CoroUtil.eventChannelName);
		CoroUtil.eventChannel.sendToAllAround(packet, new NetworkRegistry.TargetPoint(agent.ent.world.provider.getDimension(), agent.ent.posX, agent.ent.posY, agent.ent.posZ, abilitySyncRange));
		//PacketDispatcher.sendPacketToAllAround(agent.ent.posX, agent.ent.posY, agent.ent.posZ, abilitySyncRange, agent.ent.world.provider.dimensionId, packet);
	}

	public void nbtSyncRead(NBTTagCompound par1nbtTagCompound) {
		CoroUtilAbility.nbtLoadSkills(par1nbtTagCompound.getCompoundTag("abilities"), abilities, agent.ent, true);
	}

	public void nbtRead(NBTTagCompound par1nbtTagCompound) {
		CoroUtilAbility.nbtLoadSkills(par1nbtTagCompound.getCompoundTag("abilities"), abilities, agent.ent);
	}

    public void nbtWrite(NBTTagCompound par1nbtTagCompound) {
    	par1nbtTagCompound.setTag("abilities", CoroUtilAbility.nbtSaveAbilities(abilities));
	}

    public boolean shouldChaseTarget() {
    	return true;
    }

    public boolean shouldWander() {
    	return true;
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

		//use cache of longest melee ability range
		if (curRange > cacheFurthestMeleeUsable) return false;

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

	public void setFearless() {
		aggression = 1F;
	}

	public boolean hookHitBy(DamageSource par1DamageSource, float par2) {

		Entity ent = par1DamageSource.getTrueSource();
		if (!agent.ent.world.isRemote) {
			if (ent != null && agent.isEnemy(ent)) {
				if (agent.blackboard.getTarget() == null) {
					agent.blackboard.setTarget(ent);
				}

				for (Map.Entry<String, Ability> entry : agent.profile.abilities.entrySet()) {
					if (entry.getValue().isActive() && entry.getValue().canHitCancel(par1DamageSource)) {
						entry.getValue().setFinishedPerform();
					}
				}
				agent.profile.syncAbilitiesFull(false);
			}
		}

		//dont cancel
		return false;
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
