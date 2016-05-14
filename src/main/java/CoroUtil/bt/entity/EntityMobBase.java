package CoroUtil.bt.entity;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import CoroUtil.ability.Ability;
import CoroUtil.ability.IAbilityUser;
import CoroUtil.bt.AIBTAgent;
import CoroUtil.bt.IBTAgent;
import CoroUtil.entity.IEntityPacket;

public class EntityMobBase extends EntityMob implements IBTAgent, IAbilityUser, IEntityPacket {

	public AIBTAgent agent;
	
	public EntityMobBase(World par1World) {
		super(par1World);
		
		initAIProfile();
    	agent.initBTTemplate();
    	initExtraAI();
	}

	@Override
	public AIBTAgent getAIBTAgent() {
		return agent;
	}

	@Override
	public EntityLivingBase getEntityLiving() {
		return this;
	}

	@Override
	public void cleanup() {
		agent = null;
	}

	@Override
	public Ability activateAbility(String ability, Object... objects) {
		
		Ability abilityObj = (Ability)agent.profile.abilities.get(ability);
		if (abilityObj != null) {
			abilityObj.setActive();
		}
		return abilityObj;
	}
	
	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty,
			IEntityLivingData livingdata) {
		initRPGStats();
		return super.onInitialSpawn(difficulty, getAIBTAgent().onSpawnEvent(livingdata));
	}

	@Override
	public ConcurrentHashMap getAbilities() {
		return agent.profile.abilities;
	}

	public void checkAgent() {
        if (agent == null) agent = new AIBTAgent(this);
    }
	
	public void initRPGStats() {
        //getAIBTAgent().profile.addAbility(SkillMapping.newAbility("Idle").init(this), Ability.TYPE_MISC);
        //getAIBTAgent().profile.addAbility(SkillMapping.newAbility("Walk").init(this), Ability.TYPE_MISC);
        
        //this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(20);
    }
    
	/* For initializing different profiles if needed */
    public void initAIProfile() {
        agent.profile.init();
        agent.profile.initProfile(-1);
    }
    
    /* For adding onto the existing AI template and profile stuff */
    public void initExtraAI() {
    	
    }
    
    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        
        agent.setSpeedNormalBase(0.50F);
        agent.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);
    }
    
    public void updateAITasks() {
        agent.tickAI();
    }

    @Override
    protected void entityInit()
    {
        super.entityInit();
        checkAgent();
        agent.entityInit();
    }

    @Override
    public boolean interact(EntityPlayer par1EntityPlayer) {
        checkAgent();
        return agent.eventHandler.interact(par1EntityPlayer);
    }

    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
    {
    	checkAgent();
        if (!worldObj.isRemote && (agent == null || agent.ent == null)) return false;
     
        boolean result = agent.eventHandler.attackEntityFrom(par1DamageSource, par2);
        
        //only reset hit cooldown if from entity
        /*if (par1DamageSource.getEntity() != null) {
        	hurtResistantTime = 0;
        }*/
        
        //true is cancel, forge rules
        if (result) {
        	return true;
        } else {
        	return super.attackEntityFrom(par1DamageSource, par2);
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1nbtTagCompound) {
        super.readEntityFromNBT(par1nbtTagCompound);
        checkAgent();
        agent.nbtRead(par1nbtTagCompound);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1nbtTagCompound) {
        super.writeEntityToNBT(par1nbtTagCompound);
        agent.nbtWrite(par1nbtTagCompound);
    }
    
    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        agent.tickLiving();
    }
    
    @Override
	public void handleNBTFromClient(NBTTagCompound par1nbtTagCompound) {
		//will probably never happen, AI has no control on a client
	}

	@Override
	public void handleNBTFromServer(NBTTagCompound par1nbtTagCompound) {
		agent.nbtDataFromServer(par1nbtTagCompound);
	}
}
