package CoroUtil.entity;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.Vec3;

public abstract class EntityThrowableUsefull extends Entity implements IProjectile
{
     int xTile = -1;
    private int yTile = -1;
    private int zTile = -1;
    private Block inTile = null;
    protected boolean inGround = false;
    public int throwableShake = 0;

    /**
     * Is the entity that throws this 'thing' (snowball, ender pearl, eye of ender or potion)
     */
    public LivingEntity thrower;
    public LivingEntity target;
    
    //adding in this feature failed horribly, retry next time when this class is recoded to base motions off of rotations and force isntead of its current opposite
    public boolean targetSeeking = false;
    public float targetSeekAngleLimit = 5F; //max angle adjustment per tick
    
    private String throwerName = null;
    public int ticksInGround;
    public int ticksInAir = 0;
    
    public int ticksMaxAlive = 120;

    public EntityThrowableUsefull(World par1World)
    {
        super(par1World);
        this.setSize(0.25F, 0.25F);
    }

    protected void entityInit() {}

    @OnlyIn(Dist.CLIENT)

    /**
     * Checks if the entity is in range to render by using the past in distance and comparing it to its average edge
     * length * 64 * renderDistanceWeight Args: distance
     */
    public boolean isInRangeToRenderDist(double par1)
    {
        double d1 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
        d1 *= 64.0D;
        return par1 < d1 * d1;
    }

    public EntityThrowableUsefull(World par1World, LivingEntity par2EntityLivingBase, LivingEntity parTarget, double parSpeed)
    {
    	super(par1World);
    	this.thrower = par2EntityLivingBase;
        this.setSize(0.25F, 0.25F);
        target = parTarget;
    	Vec3 vec = getTargetVector(target);
    	
    	
    	this.motionX = vec.xCoord * parSpeed;
    	this.motionY = vec.yCoord * parSpeed;
    	this.motionZ = vec.zCoord * parSpeed;
    	
    	this.setPosition(par2EntityLivingBase.posX, par2EntityLivingBase.posY + (double)par2EntityLivingBase.getEyeHeight(), par2EntityLivingBase.posZ);
    	this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, (float)parSpeed, 0.0F);
    	
    	//move it out of source a bit
    	this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
    }
    
    public EntityThrowableUsefull(World par1World, LivingEntity par2EntityLivingBase)
    {
    	this(par1World, par2EntityLivingBase, 1);
    }
    
    public EntityThrowableUsefull(World par1World, LivingEntity par2EntityLivingBase, double parSpeed)
    {
        super(par1World);
        this.thrower = par2EntityLivingBase;
        this.setSize(0.25F, 0.25F);
        this.setLocationAndAngles(par2EntityLivingBase.posX, par2EntityLivingBase.posY + (double)par2EntityLivingBase.getEyeHeight(), par2EntityLivingBase.posZ, par2EntityLivingBase.rotationYaw, par2EntityLivingBase.rotationPitch);
        this.posX -= (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.posY -= 0.10000000149011612D;
        this.posZ -= (double)(MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * 0.16F);
        this.setPosition(this.posX, this.posY, this.posZ);
        //this.yOffset = 0.0F;
        float f = 0.4F;
        this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * f);
        this.motionY = (double)(-MathHelper.sin((this.rotationPitch + this.func_70183_g()) / 180.0F * (float)Math.PI) * f);
        
        
        
        this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, (float)parSpeed, 0.0F);
    }
    
    @Override
    public double getYOffset() {
    	return super.getYOffset();
    }

    public EntityThrowableUsefull(World par1World, double par2, double par4, double par6)
    {
        super(par1World);
        this.setSize(0.25F, 0.25F);
        this.setPosition(par2, par4, par6);
        //this.yOffset = 0.0F;
    }
    
    public Vec3 getTargetVector(LivingEntity target) {
    	double vecX = target.posX - thrower.posX;
    	double vecY = target.posY - thrower.posY;
    	double vecZ = target.posZ - thrower.posZ;
    	double dist = Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
    	Vec3 vec3 = new Vec3(vecX / dist, vecY / dist, vecZ / dist);
    	return vec3;
    }

    protected float func_70183_g()
    {
        return 0.0F;
    }

    /**
     * Similar to setArrowHeading, it's point the throwable entity to a x, y, z direction.
     */
    public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8)
    {
        float f2 = MathHelper.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
        par1 /= (double)f2;
        par3 /= (double)f2;
        par5 /= (double)f2;
        par1 += this.rand.nextGaussian() * 0.007499999832361937D * (double)par8;
        par3 += this.rand.nextGaussian() * 0.007499999832361937D * (double)par8;
        par5 += this.rand.nextGaussian() * 0.007499999832361937D * (double)par8;
        par1 *= (double)par7;
        par3 *= (double)par7;
        par5 *= (double)par7;
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;
        float f3 = MathHelper.sqrt(par1 * par1 + par5 * par5);
        this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(par1, par5) * 180.0D / Math.PI);
        this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(par3, (double)f3) * 180.0D / Math.PI);
        this.ticksInGround = 0;
    }

    @OnlyIn(Dist.CLIENT)

    /**
     * Sets the velocity to the args. Args: x, y, z
     */
    public void setVelocity(double par1, double par3, double par5)
    {
        this.motionX = par1;
        this.motionY = par3;
        this.motionZ = par5;

        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F)
        {
            float f = MathHelper.sqrt(par1 * par1 + par5 * par5);
            this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(par1, par5) * 180.0D / Math.PI);
            this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(par3, (double)f) * 180.0D / Math.PI);
        }
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void onUpdate()
    {
        this.lastTickPosX = this.posX;
        this.lastTickPosY = this.posY;
        this.lastTickPosZ = this.posZ;
        super.onUpdate();

        if (this.throwableShake > 0)
        {
            --this.throwableShake;
        }

        if (this.isCollidedHorizontally) {
        	this.setDead();
        }
        
        if (this.inGround)
        {
            Block i = this.world.getBlockState(new BlockPos(this.xTile, this.yTile, this.zTile)).getBlock();

            if (i == this.inTile)
            {
                ++this.ticksInGround;

                if (this.ticksInGround == 1200)
                {
                    this.setDead();
                }

                return;
            }

            this.inGround = false;
            this.motionX *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionY *= (double)(this.rand.nextFloat() * 0.2F);
            this.motionZ *= (double)(this.rand.nextFloat() * 0.2F);
            this.ticksInGround = 0;
            this.ticksInAir = 0;
        }
        else
        {
            ++this.ticksInAir;
        }
        
        if (ticksExisted >= ticksMaxAlive) {
        	setDead();
        }

        Vec3 vec3 = new Vec3(this.posX, this.posY, this.posZ);
        Vec3 vec31 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        RayTraceResult movingobjectposition = this.world.rayTraceBlocks(vec3.toMCVec(), vec31.toMCVec());
        vec3 = new Vec3(this.posX, this.posY, this.posZ);
        vec31 = new Vec3(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

        if (movingobjectposition != null)
        {
            vec31 = new Vec3(movingobjectposition.hitVec.x, movingobjectposition.hitVec.y, movingobjectposition.hitVec.z);
        }

        if (!this.world.isRemote)
        {
        	RayTraceResult temp = tickEntityCollision(vec3, vec31);
        	if (temp != null) movingobjectposition = temp;
        }

        if (movingobjectposition != null)
        {
            this.onImpact(movingobjectposition);
        }

        /*this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;*/
        
        this.move(MoverType.SELF, motionX, motionY, motionZ);
        
        float f1 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)(Math.atan2(this.motionX, this.motionZ) * 180.0D / Math.PI);

        for (this.rotationPitch = (float)(Math.atan2(this.motionY, (double)f1) * 180.0D / Math.PI); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F)
        {
            ;
        }

        while (this.rotationPitch - this.prevRotationPitch >= 180.0F)
        {
            this.prevRotationPitch += 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw < -180.0F)
        {
            this.prevRotationYaw -= 360.0F;
        }

        while (this.rotationYaw - this.prevRotationYaw >= 180.0F)
        {
            this.prevRotationYaw += 360.0F;
        }

        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

        /*if (targetSeeking && target != null) {
        	adjustSeekMotion();
        }*/
        
        float f2 = 1F;//0.99F;
        float f3 = this.getGravityVelocity();

        if (this.isInWater())
        {
            for (int k = 0; k < 4; ++k)
            {
                float f4 = 0.25F;
                this.world.spawnParticle(ParticleTypes.WATER_BUBBLE, this.posX - this.motionX * (double)f4, this.posY - this.motionY * (double)f4, this.posZ - this.motionZ * (double)f4, this.motionX, this.motionY, this.motionZ);
            }

            f2 = 0.8F;
        }

        this.motionX *= (double)f2;
        this.motionY *= (double)f2;
        this.motionZ *= (double)f2;
        this.motionY -= (double)f3;
        this.setPosition(this.posX, this.posY, this.posZ);
    }
    
    public void adjustSeekMotion() {
    	//since code is motion based not angle based, we must use the recently updated rotationYaw, adjust it, then apply a new motionX and Z based on previous sqrt speed of prev motionX Z
    	double speedOld = Math.sqrt(motionX * motionX + motionZ * motionZ);
    	
    	double vecX = target.posX - this.posX;
    	double vecZ = target.posZ - this.posZ;
    	
    	float aimAngle = (float)(Math.atan2(vecZ, vecX) * 180.0D / Math.PI) - 90.0F;
    	
    	//rotationYaw = this.updateRotation(rotationYaw, aimAngle, targetSeekAngleLimit);
    	
    	//this.motionX = (double)(-MathHelper.sin(this.rotationYaw / 180.0F * (float)Math.PI) * /*MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * */speedOld);
        //this.motionZ = (double)(MathHelper.cos(this.rotationYaw / 180.0F * (float)Math.PI) * /*MathHelper.cos(this.rotationPitch / 180.0F * (float)Math.PI) * */speedOld);
        
    	float maxSpeed = 0.01F;
    	float adjRate = 0.03F;
    	
        if (this.posY < target.posY) {
        	if (motionY < 0) this.motionY += 0.01F;
        } else {
        	if (motionY > 0) this.motionY -= 0.01F;
        }
        
        if (Math.abs(motionX) > maxSpeed) {
        	motionX *= 0.85F;
        }
        
        if (Math.abs(motionZ) > maxSpeed) {
        	motionZ *= 0.85F;
        }
        
        //lazy way
        if (this.posX < target.posX) {
        	this.motionX += adjRate;
        } else {
        	this.motionX -= adjRate;
        }
        
        if (this.posZ < target.posZ) {
        	this.motionZ += adjRate;
        } else {
        	this.motionZ -= adjRate;
        }
    }
    
    public void faceEntity(Entity p_70625_1_, float p_70625_2_, float p_70625_3_)
    {
        double d0 = p_70625_1_.posX - this.posX;
        double d2 = p_70625_1_.posZ - this.posZ;
        double d1;

        if (p_70625_1_ instanceof LivingEntity)
        {
            LivingEntity entitylivingbase = (LivingEntity)p_70625_1_;
            d1 = entitylivingbase.posY + (double)entitylivingbase.getEyeHeight() - (this.posY + (double)this.getEyeHeight());
        }
        else
        {
            d1 = (p_70625_1_.getEntityBoundingBox().minY + p_70625_1_.getEntityBoundingBox().maxY) / 2.0D - (this.posY + (double)this.getEyeHeight());
        }

        double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
        float f2 = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
        float f3 = (float)(-(Math.atan2(d1, d3) * 180.0D / Math.PI));
        this.rotationPitch = this.updateRotation(this.rotationPitch, f3, p_70625_3_);
        this.rotationYaw = this.updateRotation(this.rotationYaw, f2, p_70625_2_);
    }

    /**
     * Arguments: current rotation, intended rotation, max increment.
     */
    private float updateRotation(float p_70663_1_, float p_70663_2_, float p_70663_3_)
    {
        float f3 = MathHelper.wrapDegrees(p_70663_2_ - p_70663_1_);

        if (f3 > p_70663_3_)
        {
            f3 = p_70663_3_;
        }

        if (f3 < -p_70663_3_)
        {
            f3 = -p_70663_3_;
        }

        return p_70663_1_ + f3;
    }
    
    public RayTraceResult tickEntityCollision(Vec3 vec3, Vec3 vec31) {
    	Entity entity = null;
        List list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D, 1.0D, 1.0D));
        double d0 = 0.0D;
        LivingEntity entityliving = this.getThrower();

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);

            if (entity1.canBeCollidedWith() && (entity1 != entityliving || this.ticksInAir >= 5))
            {
                float f = 0.3F;
                AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().grow((double)f, (double)f, (double)f);
                RayTraceResult movingobjectposition1 = axisalignedbb.calculateIntercept(vec3.toMCVec(), vec31.toMCVec());

                if (movingobjectposition1 != null)
                {
                    double d1 = vec3.toMCVec().distanceTo(movingobjectposition1.hitVec);

                    if (d1 < d0 || d0 == 0.0D)
                    {
                        entity = entity1;
                        d0 = d1;
                    }
                }
            }
        }

        if (entity != null)
        {
            return new RayTraceResult(entity);
        }
        return null;
    }

    /**
     * Gets the amount of gravity to apply to the thrown entity with each tick.
     */
    protected float getGravityVelocity()
    {
        return 0.03F;
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(RayTraceResult movingobjectposition) {
    	setDead();
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(CompoundNBT par1NBTTagCompound)
    {
        par1NBTTagCompound.setShort("xTile", (short)this.xTile);
        par1NBTTagCompound.setShort("yTile", (short)this.yTile);
        par1NBTTagCompound.setShort("zTile", (short)this.zTile);
        par1NBTTagCompound.setByte("inTile", (byte)Block.getIdFromBlock(this.inTile));
        par1NBTTagCompound.setByte("shake", (byte)this.throwableShake);
        par1NBTTagCompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));

        if ((this.throwerName == null || this.throwerName.length() == 0) && this.thrower != null && this.thrower instanceof PlayerEntity)
        {
            this.throwerName = CoroUtilEntity.getName(thrower);
        }

        par1NBTTagCompound.setString("ownerName", this.throwerName == null ? "" : this.throwerName);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(CompoundNBT par1NBTTagCompound)
    {
        this.xTile = par1NBTTagCompound.getShort("xTile");
        this.yTile = par1NBTTagCompound.getShort("yTile");
        this.zTile = par1NBTTagCompound.getShort("zTile");
        this.inTile = Block.getBlockById(par1NBTTagCompound.getByte("inTile") & 255);
        this.throwableShake = par1NBTTagCompound.getByte("shake") & 255;
        this.inGround = par1NBTTagCompound.getByte("inGround") == 1;
        this.throwerName = par1NBTTagCompound.getString("ownerName");

        if (this.throwerName != null && this.throwerName.length() == 0)
        {
            this.throwerName = null;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public float getShadowSize()
    {
        return 0.0F;
    }

    public LivingEntity getThrower()
    {
        if (this.thrower == null && this.throwerName != null && this.throwerName.length() > 0)
        {
            this.thrower = this.world.getPlayerEntityByName(this.throwerName);
        }

        return this.thrower;
    }
}
