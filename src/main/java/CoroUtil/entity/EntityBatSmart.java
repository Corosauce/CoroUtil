package CoroUtil.entity;

import java.util.Calendar;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.entity.ai.goal.LookAtGoal;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.passive.AmbientEntity;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.SoundEvents;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTables;
import net.minecraft.world.storage.loot.LootTables;

public class EntityBatSmart extends CreatureEntity implements IFlyingAnimal
{
    private static final DataParameter<Byte> HANGING = EntityDataManager.<Byte>createKey(EntityBatSmart.class, DataSerializers.BYTE);
    /** Coordinates of where the bat spawned. */
    private BlockPos currentFlightTarget;

    public boolean useVanillaAI = false;

    public EntityBatSmart(World worldIn)
    {
        super(worldIn);
        this.setSize(0.5F, 0.9F);
        this.setIsBatHanging(true);
        //TEMP
        this.setIsBatHanging(false);
        this.moveHelper = new FlyingMovementController(this);
    }

    protected void entityInit()
    {
        super.entityInit();
        this.dataManager.register(HANGING, Byte.valueOf((byte)0));
    }

    @Override
    protected PathNavigator createNavigator(World worldIn)
    {
        FlyingPathNavigator pathnavigateflying = new FlyingPathNavigator(this, worldIn);
        pathnavigateflying.setCanOpenDoors(false);
        pathnavigateflying.setCanFloat(true);
        pathnavigateflying.setCanEnterDoors(true);
        return pathnavigateflying;
    }

    @Override
    protected void initEntityAI()
    {
        //this.aiSit = new EntityAISit(this);
        //this.tasks.addTask(0, new EntityAIPanic(this, 1.25D));
        this.tasks.addTask(0, new SwimGoal(this));
        this.tasks.addTask(1, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        //this.tasks.addTask(2, this.aiSit);
        //this.tasks.addTask(2, new EntityAIFollowOwnerFlying(this, 1.0D, 5.0F, 1.0F));
        this.tasks.addTask(2, new WaterAvoidingRandomFlyingGoal(this, 1.0D));
        //this.tasks.addTask(3, new EntityAILandOnOwnersShoulder(this));
        //this.tasks.addTask(3, new EntityAIFollow(this, 1.0D, 3.0F, 7.0F));
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume()
    {
        return 0.1F;
    }

    /**
     * Gets the pitch of living sounds in living entities.
     */
    protected float getSoundPitch()
    {
        return super.getSoundPitch() * 0.95F;
    }

    @Nullable
    public SoundEvent getAmbientSound()
    {
        return this.getIsBatHanging() && this.rand.nextInt(4) != 0 ? null : SoundEvents.ENTITY_BAT_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource p_184601_1_)
    {
        return SoundEvents.ENTITY_BAT_HURT;
    }

    protected SoundEvent getDeathSound()
    {
        return SoundEvents.ENTITY_BAT_DEATH;
    }

    /**
     * Returns true if this entity should push and be pushed by other entities when colliding.
     */
    public boolean canBePushed()
    {
        return false;
    }

    protected void collideWithEntity(Entity entityIn)
    {
    }

    protected void collideWithNearbyEntities()
    {
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(6.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.FLYING_SPEED);
        this.getEntityAttribute(SharedMonsterAttributes.FLYING_SPEED).setBaseValue(0.4000000059604645D);
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.20000000298023224D);
    }

    public boolean getIsBatHanging()
    {
        return (((Byte)this.dataManager.get(HANGING)).byteValue() & 1) != 0;
    }

    public void setIsBatHanging(boolean isHanging)
    {
        byte b0 = ((Byte)this.dataManager.get(HANGING)).byteValue();

        if (isHanging)
        {
            this.dataManager.set(HANGING, Byte.valueOf((byte)(b0 | 1)));
        }
        else
        {
            this.dataManager.set(HANGING, Byte.valueOf((byte)(b0 & -2)));
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        super.onUpdate();

        if (this.getIsBatHanging())
        {
            this.motionX = 0.0D;
            this.motionY = 0.0D;
            this.motionZ = 0.0D;
            this.posY = (double)MathHelper.floor(this.posY) + 1.0D - (double)this.height;
        }
        else
        {
            this.motionY *= 0.6000000238418579D;
        }
    }

    protected void updateAITasks()
    {
        super.updateAITasks();
        if (useVanillaAI) {
            BlockPos blockpos = new BlockPos(this);
            BlockPos blockpos1 = blockpos.up();

            if (this.getIsBatHanging()) {
                if (this.world.getBlockState(blockpos1).isNormalCube()) {
                    if (this.rand.nextInt(200) == 0) {
                        this.rotationYawHead = (float) this.rand.nextInt(360);
                    }

                    if (this.world.getNearestPlayerNotCreative(this, 4.0D) != null) {
                        this.setIsBatHanging(false);
                        this.world.playEvent((PlayerEntity) null, 1025, blockpos, 0);
                    }
                } else {
                    this.setIsBatHanging(false);
                    this.world.playEvent((PlayerEntity) null, 1025, blockpos, 0);
                }
            } else {
                if (this.currentFlightTarget != null && (!this.world.isAirBlock(this.currentFlightTarget) || this.currentFlightTarget.getY() < 1)) {
                    this.currentFlightTarget = null;
                }

                if (this.currentFlightTarget == null || this.rand.nextInt(30) == 0 || this.currentFlightTarget.distanceSq((double) ((int) this.posX), (double) ((int) this.posY), (double) ((int) this.posZ)) < 4.0D) {
                    this.currentFlightTarget = new BlockPos((int) this.posX + this.rand.nextInt(7) - this.rand.nextInt(7), (int) this.posY + this.rand.nextInt(6) - 2, (int) this.posZ + this.rand.nextInt(7) - this.rand.nextInt(7));
                }

                double d0 = (double) this.currentFlightTarget.getX() + 0.5D - this.posX;
                double d1 = (double) this.currentFlightTarget.getY() + 0.1D - this.posY;
                double d2 = (double) this.currentFlightTarget.getZ() + 0.5D - this.posZ;
                this.motionX += (Math.signum(d0) * 0.5D - this.motionX) * 0.10000000149011612D;
                this.motionY += (Math.signum(d1) * 0.699999988079071D - this.motionY) * 0.10000000149011612D;
                this.motionZ += (Math.signum(d2) * 0.5D - this.motionZ) * 0.10000000149011612D;
                float f = (float) (MathHelper.atan2(this.motionZ, this.motionX) * (180D / Math.PI)) - 90.0F;
                float f1 = MathHelper.wrapDegrees(f - this.rotationYaw);
                this.moveForward = 0.5F;
                this.rotationYaw += f1;

                if (this.rand.nextInt(100) == 0 && this.world.getBlockState(blockpos1).isNormalCube()) {
                    this.setIsBatHanging(true);
                }
            }
        }
    }

    /**
     * returns if this entity triggers Block.onEntityWalking on the blocks they walk on. used for spiders and wolves to
     * prevent them from trampling crops
     */
    protected boolean canTriggerWalking()
    {
        return false;
    }

    public void fall(float distance, float damageMultiplier)
    {
    }

    protected void updateFallState(double y, boolean onGroundIn, BlockState state, BlockPos pos)
    {
    }

    /**
     * Return whether this entity should NOT trigger a pressure plate or a tripwire.
     */
    public boolean doesEntityNotTriggerPressurePlate()
    {
        return true;
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean attackEntityFrom(DamageSource source, float amount)
    {
        if (this.isEntityInvulnerable(source))
        {
            return false;
        }
        else
        {
            if (!this.world.isRemote && this.getIsBatHanging())
            {
                this.setIsBatHanging(false);
            }

            return super.attackEntityFrom(source, amount);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(CompoundNBT compound)
    {
        super.readEntityFromNBT(compound);
        this.dataManager.set(HANGING, Byte.valueOf(compound.getByte("BatFlags")));
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(CompoundNBT compound)
    {
        super.writeEntityToNBT(compound);
        compound.setByte("BatFlags", ((Byte)this.dataManager.get(HANGING)).byteValue());
    }

    /**
     * Checks if the entity's current position is a valid location to spawn this entity.
     */
    public boolean getCanSpawnHere()
    {
        BlockPos blockpos = new BlockPos(this.posX, this.getEntityBoundingBox().minY, this.posZ);

        if (blockpos.getY() >= this.world.getSeaLevel())
        {
            return false;
        }
        else
        {
            int i = this.world.getLightFromNeighbors(blockpos);
            int j = 4;

            if (this.isDateAroundHalloween(this.world.getCurrentDate()))
            {
                j = 7;
            }
            else if (this.rand.nextBoolean())
            {
                return false;
            }

            return i > this.rand.nextInt(j) ? false : super.getCanSpawnHere();
        }
    }

    private boolean isDateAroundHalloween(Calendar p_175569_1_)
    {
        return p_175569_1_.get(2) + 1 == 10 && p_175569_1_.get(5) >= 20 || p_175569_1_.get(2) + 1 == 11 && p_175569_1_.get(5) <= 3;
    }

    public float getEyeHeight()
    {
        return this.height / 2.0F;
    }

    @Nullable
    protected ResourceLocation getLootTable()
    {
        return LootTables.ENTITIES_BAT;
    }
}