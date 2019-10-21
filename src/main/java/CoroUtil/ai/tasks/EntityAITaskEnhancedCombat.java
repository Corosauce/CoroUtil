package CoroUtil.ai.tasks;

import java.util.Random;
import java.util.UUID;

import CoroUtil.difficulty.UtilEntityBuffs;
import net.minecraft.block.material.Material;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import CoroUtil.ai.ITaskInitializer;
import CoroUtil.entity.data.AttackData;
import CoroUtil.util.BlockCoord;
import CoroUtil.difficulty.DynamicDifficulty;

import CoroUtil.config.ConfigHWMonsters;

public class EntityAITaskEnhancedCombat extends Goal implements ITaskInitializer
{
    World worldObj;
    CreatureEntity entity;
    /** An amount of decrementing ticks that allows the entity to attack once the tick reaches 0. */
    int attackTick;
    /** The speed with which the mob will approach the target */
    double speedTowardsTarget;
    
    /** When true, the mob will continue chasing its target, even if it can't find a path to them right now. */
    boolean longMemory;
    /** The PathEntity of our entity. */
    Path entityPathEntity;
    Class classTarget;
    private int delayCounter;
    private double x;
    private double y;
    private double z;
    private static final String __OBFID = "CL_00001595";

    private int failedPathFindingPenalty;


    private long counterAttackLastHitTime = 0;
    
    private boolean wasInAir = false;
    private boolean leapAttacking = false;
    
    private static final UUID lungeSpeedUUID = UUID.fromString("A9766B59-9566-4402-BC1F-2EE2A276D836");
    private static final AttributeModifier lungeSpeedModifier = new AttributeModifier(lungeSpeedUUID, "lungeSpeed", ConfigHWMonsters.lungeSpeed, 1);
    
    /*private boolean useLunging = false;
    private boolean useLeapAttack = false;*/

    //needed for generic instantiation
    public EntityAITaskEnhancedCombat() {
		this.classTarget = PlayerEntity.class;
		this.speedTowardsTarget = 1D;
        this.longMemory = false;
	}

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    @Override
    public boolean shouldExecute()
    {
        LivingEntity entitylivingbase = this.entity.getAttackTarget();

        if (entitylivingbase == null)
        {
            return false;
        }
        else if (!entitylivingbase.isAlive())
        {
            return false;
        }
        else if (this.classTarget != null && !this.classTarget.isAssignableFrom(entitylivingbase.getClass()))
        {
            return false;
        }
        else
        {
            if (-- this.delayCounter <= 0)
            {
            	//System.out.println(this.entity.getEntityId() + " pathing to: " + entitylivingbase);
            	if (entity.onGround || entity.isInWater() || entity.isInsideOfMaterial(Material.LAVA)) {
            		this.entityPathEntity = this.entity.getNavigator().getPathToEntityLiving(entitylivingbase);
            	}
            	this.delayCounter = 4 + this.entity.getRNG().nextInt(7);
                return this.entityPathEntity != null;
            }
            else
            {
                return true;
            }
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    @Override
    public boolean shouldContinueExecuting()
    {
        LivingEntity entitylivingbase = this.entity.getAttackTarget();
        return entitylivingbase == null ? false : (!entitylivingbase.isAlive() ? false : (!this.longMemory ? !this.entity.getNavigator().noPath() : this.entity.isWithinHomeDistanceFromPosition(new BlockPos(MathHelper.floor(entitylivingbase.posX), MathHelper.floor(entitylivingbase.posY), MathHelper.floor(entitylivingbase.posZ)))));
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    @Override
    public void startExecuting()
    {
        this.entity.getNavigator().setPath(this.entityPathEntity, this.speedTowardsTarget);
        this.delayCounter = 0;
    }

    /**
     * Resets the task
     */
    @Override
    public void resetTask()
    {
        this.entity.getNavigator().clearPathEntity();
    }

    /**
     * Updates the task
     */
    @Override
    public void tick()
    {
    	//add to config!
    	double lungeDist = ConfigHWMonsters.lungeDist;
        double speedTowardsTargetLunge = ConfigHWMonsters.speedTowardsTargetLunge;
        long counterAttackDetectThreshold = ConfigHWMonsters.counterAttackDetectThreshold;
        long counterAttackReuseDelay = ConfigHWMonsters.counterAttackReuseDelay;
        double counterAttackLeapSpeed = ConfigHWMonsters.counterAttackLeapSpeed;
        
        LivingEntity entitylivingbase = this.entity.getAttackTarget();

        //fix for stealth mods that null out target entity in weird spots even after shouldExecute and shouldContinueExecuting is called
        if (entitylivingbase == null) {
            resetTask();
            return;
        }

        this.entity.getLookController().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
        double d0 = this.entity.getDistanceSq(entitylivingbase.posX, entitylivingbase.getBoundingBox().minY, entitylivingbase.posZ);
        double d1 = (double)(/*Math.sqrt(*/this.entity.width * 2.0F * this.entity.width * 2.0F/*)*/ + entitylivingbase.width);
        --this.delayCounter;
        //TEST
        //this.delayCounter = 0;
        //this.attackTick = 0;

        if ((this.longMemory || this.entity.getEntitySenses().canSee(entitylivingbase)) && this.delayCounter <= 0 && 
        		(this.x == 0.0D && this.y == 0.0D && this.z == 0.0D || 
        		entitylivingbase.getDistanceSq(this.x, this.y, this.z) >= 1.0D || this.entity.getRNG().nextFloat() < 0.05F))
        {
            this.x = entitylivingbase.posX;
            this.y = entitylivingbase.getBoundingBox().minY;
            this.z = entitylivingbase.posZ;
            this.delayCounter = failedPathFindingPenalty + 4 + this.entity.getRNG().nextInt(7);

            if (this.entity.getNavigator().getPath() != null)
            {
                PathPoint finalPathPoint = this.entity.getNavigator().getPath().getFinalPathPoint();
                if (finalPathPoint != null && entitylivingbase.getDistanceSq(finalPathPoint.x, finalPathPoint.y, finalPathPoint.z) < 1)
                {
                    failedPathFindingPenalty = 0;
                }
                else
                {
                    failedPathFindingPenalty += 10;
                }
            }
            else
            {
                failedPathFindingPenalty += 10;
            }

            if (d0 > 32D * 32D)
            {
                this.delayCounter += 10;
            }
            else if (d0 > 16D * 16D)
            {
                this.delayCounter += 5;
            }
            
            boolean pathResult = false;
            
            if (canUseLunging()) {
	            double curSpeed = entity.getAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).get();
	            
	            if (d0 <= lungeDist * lungeDist && curSpeed < UtilEntityBuffs.speedCap) {
	            	if (this.entity.getAttributes().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(lungeSpeedUUID) == null) {
	            		this.entity.getAttributes().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(this.lungeSpeedModifier);
	            	}
	            } else {
	            	if (this.entity.getAttributes().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).getModifier(lungeSpeedUUID) != null) {
	            		this.entity.getAttributes().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(this.lungeSpeedModifier);
	            	}
	            }
            }
            
            if (entity.onGround || entity.isInWater() || entity.isInsideOfMaterial(Material.LAVA)) {
            	
	            //System.out.println(this.entity.getEntityId() + " pathing to: " + entitylivingbase);
	            pathResult = this.entity.getNavigator().tryMoveToEntityLiving(entitylivingbase, this.speedTowardsTarget);
	            
            }

            if (!pathResult)
            {
                this.delayCounter += 15;
            }
        }

        this.attackTick = Math.max(this.attackTick - 1, 0);
        
        //counter attack leap
        if (canUseLeapAttack()) {
	        if (this.entity.onGround || entity.isInWater() || entity.isInsideOfMaterial(Material.LAVA)) {
	        	leapAttacking = false;
	        	//if (wasInAir) {
	        		
	        		AttackData data = DynamicDifficulty.lookupEntToDamageLog.get(entity.getEntityId());
	        		if (data != null) {
	        			if (data.getLastLogTime() > this.counterAttackLastHitTime) {
		        			if (data.getLastLogTime() + counterAttackDetectThreshold < entity.world.getGameTime()) {
		        				this.counterAttackLastHitTime = data.getLastLogTime() + counterAttackReuseDelay;
		        				
		        				double vecX = entitylivingbase.posX - this.entity.posX;
		        		        double vecZ = entitylivingbase.posZ - this.entity.posZ;
		        		        float xzDist = MathHelper.sqrt(vecX * vecX + vecZ * vecZ);
		        		        double dynamicReduce = Math.min(counterAttackLeapSpeed, counterAttackLeapSpeed / (3D-Math.min(3, xzDist)));
		        		        this.entity.motionX += vecX / (double)xzDist * dynamicReduce;
		        		        this.entity.motionZ += vecZ / (double)xzDist * dynamicReduce;
		        		        this.entity.motionY = 0.4D;
		        		        
		        		        //extra vertical
		        		        if (this.entity.getBoundingBox().minY < entitylivingbase.getBoundingBox().minY) {
		        		        	double extraY = Math.min(5D, entitylivingbase.getBoundingBox().minY - this.entity.getBoundingBox().minY);
		        		        	this.entity.motionY += 0.1D * extraY;
		        		        }
		        		        
		                		wasInAir = false;
		                		leapAttacking = true;
		                		this.entity.getNavigator().clearPathEntity();
		                		//important, if you clear path to entity, be sure to tick or clear where hes supposed to be last moving to
		                		//if you dont, it could look like they flee
		                		this.entity.getMoveHelper().setMoveTo(entitylivingbase.posX, entitylivingbase.posY, entitylivingbase.posZ, 1D);

		                		//quick repath on land, should be ok performance wise
                                Path path = this.entity.getNavigator().getPathToEntityLiving(entitylivingbase);
                                if (path != null) {
                                    this.entity.getNavigator().setPath(path, 1D);
                                }

		                		delayCounter = 0;
		        			}
		        			
	        			}
	        		}
	        		
	        		
	        	//}
	        	
	        } else {
	        	wasInAir = true;
	        }
        }
        
        //actual attack code
        if (d0 <= d1 && this.attackTick <= 0)
        {
            this.attackTick = 20;

            if (this.entity.getHeldItemMainhand() != null)
            {
                this.entity.swingArm(Hand.MAIN_HAND);
            }
            
            this.entity.attackEntityAsMob(entitylivingbase);

            int lowestHealthAllowed = 2;

            //make sure theyre actually still alive, and can lose more health
            if (entitylivingbase.getHealth() > lowestHealthAllowed) {
                if (leapAttacking && ConfigHWMonsters.counterAttackLeapExtraDamageMultiplier > 0) {
                    double extraDamage = this.entity.getAttributes().getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE).get();
                    extraDamage *= ConfigHWMonsters.counterAttackLeapExtraDamageMultiplier;
                    if (this.worldObj.getDifficulty() == Difficulty.EASY) {
                        extraDamage = extraDamage / 2.0F + 1.0F;
                    } else if (this.worldObj.getDifficulty() == Difficulty.HARD) {
                        extraDamage = extraDamage * 3.0F / 2.0F;
                    }
                    //entitylivingbase.attackEntityFrom();
                    //entitylivingbase.damageEntity(DamageSource.magic, (float) extraArmorPiercingDamage);
                    if (ConfigHWMonsters.counterAttackLeapArmorPiercing) {
                        float newHealth = entitylivingbase.getHealth() - (float) extraDamage;
                        if (newHealth < lowestHealthAllowed) {
                            newHealth = lowestHealthAllowed;
                        }
                        entitylivingbase.setHealth(newHealth);
                    } else {
                        entitylivingbase.attackEntityFrom(DamageSource.causeMobDamage(entity), (float) extraDamage);
                    }
                    //System.out.println("hit!: " + extraArmorPiercingDamage);

                }
            }
            
        }
    }

    public boolean canUseLeapAttack() {
        return entity.getEntityData().getCompound(UtilEntityBuffs.dataEntityBuffed_Data).getBoolean(UtilEntityBuffs.dataEntityBuffed_AI_CounterLeap);
    }

    public boolean canUseLunging() {
        return entity.getEntityData().getCompound(UtilEntityBuffs.dataEntityBuffed_Data).getBoolean(UtilEntityBuffs.dataEntityBuffed_AI_Lunge);
    }

	@Override
	public void setEntity(CreatureEntity creature) {
		this.entity = creature;
		this.worldObj = this.entity.world;/*
		
		EntityPlayer player = DynamicDifficulty.getBestPlayerForArea(worldObj, new BlockCoord(entity));
		
		if (player != null) {
			float difficulty = DynamicDifficulty.getDifficultyScaleAverage(worldObj, player, new BlockCoord(entity));
			
			Random rand = new Random();
			if (rand.nextFloat() < difficulty * ConfigHWMonsters.scaleLeapAttackUseChance) {
				useLeapAttack = true;
			} else {
				
			}
			
			if (rand.nextFloat() < difficulty * ConfigHWMonsters.scaleLungeUseChance) {
				useLunging = true;
			} else {
				
			}
			
			//System.out.println("leap? " + useLeapAttack + " lunge? " + useLunging);
		}*/
	}
}
