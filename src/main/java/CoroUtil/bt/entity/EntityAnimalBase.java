package CoroUtil.bt.entity;

import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import CoroUtil.ability.Ability;
import CoroUtil.ability.IAbilityUser;
import CoroUtil.bt.AIBTAgent;
import CoroUtil.bt.IBTAgent;
import CoroUtil.entity.IEntityPacket;

public class EntityAnimalBase extends EntityAnimal implements IBTAgent, IAbilityUser, IEntityPacket {

	public AIBTAgent agent;
	
	public EntityAnimalBase(World par1World) {
		super(par1World);
		
		initAIProfile();
    	agent.initBTTemplate();
	}

	@Override
	protected boolean isAIEnabled() {
		return true;
	}
	
	@Override
	public EntityAgeable createChild(EntityAgeable entityageable) {
		// TODO Auto-generated method stub
		return null;
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
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1EntityLivingData) {
        initRPGStats();
        return super.onSpawnWithEgg(getAIBTAgent().onSpawnEvent(par1EntityLivingData));
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
        
		//for now lets init this each time its created instead of initial spawn with nbt
		//getAIBTAgent().profile.addAbilityMelee(AbilityMapping.newAbility("AttackMelee").init(this));
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
        
        agent.setSpeedNormalBase(0.65F);
        agent.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(20);
    }
    
    public void updateAITasks() {
    	//AIBTAgent.DEBUGTREES = true;
    	//System.out.println("PUG AI TICK START");
        agent.tickAI();
        AIBTAgent.DEBUGTREES = false;
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
		
		String command = par1nbtTagCompound.getString("command");
		
		if (command.equals("tamed")) {
			String s = "heart";

	        for (int i = 0; i < 7; ++i)
	        {
	            double d0 = this.rand.nextGaussian() * 0.02D;
	            double d1 = this.rand.nextGaussian() * 0.02D;
	            double d2 = this.rand.nextGaussian() * 0.02D;
	            this.worldObj.spawnParticle(s, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
	        }
		}
		
	}
}
