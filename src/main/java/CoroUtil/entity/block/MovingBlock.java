package CoroUtil.entity.block;

import io.netty.buffer.ByteBuf;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.Vec3;

public class MovingBlock extends Entity implements IEntityAdditionalSpawnData
{

	//Generic moving block
	
	//Needs:
	//- DW for rotationYaw and Pitch
	//- DW for state to use for various visual changes on client
	//-- ideas: spawned/freefalling, exploding/dying, unactive, fading out
	
	
	public boolean noCollision = false;
	public int age = 0;
	public int blockID = 0;
	public int blockMeta = 0;
	public float gravity = 0.04F;
	public float speedSlowing = 0.99F;
	public int blockifyDelay = 30;
	
	public float rotationYawB = 0;
	public float rotationPitchB = 0;
	public float rotationRoll = 0;
	public float prevRotationRoll = 0;
	public float rotationYawVel = 0;
	public float rotationPitchVel = 0;
	
	public int state = 0; //generic state var
	public BlockCoord coordsLastAir;
	public boolean blockToEntCollision = true;
	
	public boolean firstTick = true;
	
	public float scale = 1F;
	
	//targeting
	/*public EntityLivingBase target;
	public float targetTillDist = -1;
	public double targPrevPosX;
	public double targPrevPosY;
	public double targPrevPosZ;*/
	
	//synced data for render
	/*public int blockNum;
	public int blockRow;
	public boolean createParticles = false;*/
	
    public MovingBlock(World var1)
    {
        super(var1);
        setSize(scale, scale);
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    public MovingBlock(World var1, int parBlockID, int parMeta)
    {
        super(var1);
        blockID = parBlockID;
        blockMeta = parMeta;
        setSize(scale, scale);
        this.setPosition(this.posX, this.posY, this.posZ);
    }
    
    //TESTING COLLISION STUFF
    @Override
    public AxisAlignedBB getCollisionBox(Entity par1Entity) {
    	return super.getCollisionBox(par1Entity);
    }
    
    @Override
    public float getCollisionBorderSize() {
    	return super.getCollisionBorderSize();
    }
    
    @Override
    public void applyEntityCollision(Entity par1Entity) {
    	super.applyEntityCollision(par1Entity);
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(blockID);
        data.writeInt(blockMeta);
        data.writeFloat(gravity);
        data.writeInt(blockifyDelay);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
    	blockID = data.readInt();
    	blockMeta = data.readInt();
    	gravity = data.readFloat();
    	blockifyDelay = data.readInt();
    }

    @Override
    public boolean isInRangeToRenderDist(double var1)
    {
        return true;
    }
    
    @Override
    public boolean isInRangeToRender3d(double p_145770_1_, double p_145770_3_,
    		double p_145770_5_) {
    	return true;
    }

    @Override
    public boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public void entityInit() {
    	//TODO: 1.10 datawatcher to new system
    	/*this.dataWatcher.addObject(2, Float.valueOf(rotationYawB));
    	this.dataWatcher.addObject(3, Float.valueOf(rotationPitchB));
    	this.dataWatcher.addObject(4, Float.valueOf(rotationRoll));
    	this.dataWatcher.addObject(5, Integer.valueOf(state));
    	this.dataWatcher.addObject(6, Float.valueOf(scale));*/
    }

    @Override
    public boolean canBePushed()
    {
        return !this.isDead;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return !this.isDead && !this.noCollision;
    }

    @Override
    public void onUpdate()
    {
        //super.onUpdate();
        boolean superTick = true;
        if (superTick) {
        	super.onUpdate();
        } else {
        	this.prevDistanceWalkedModified = this.distanceWalkedModified;
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.prevRotationPitch = this.rotationPitch = this.rotationPitchB;
            this.prevRotationYaw = this.rotationYaw = this.rotationYawB;
            this.prevRotationRoll = this.rotationRoll;
        }
    	
    	++this.age;
    	
    	//datawatchers
    	//TODO: 1.10 datawatcher to new system
    	/*if (worldObj.isRemote) {
    		rotationYaw = rotationYawB = dataWatcher.getWatchableObjectFloat(2);
    		rotationPitch = rotationPitchB = dataWatcher.getWatchableObjectFloat(3);
    		rotationRoll = dataWatcher.getWatchableObjectFloat(4);
    		state = dataWatcher.getWatchableObjectInt(5);
    		scale = dataWatcher.getWatchableObjectFloat(6);
    	} else {
    		dataWatcher.updateObject(2, rotationYawB);
    		dataWatcher.updateObject(3, rotationPitchB);
    		dataWatcher.updateObject(4, rotationRoll);
    		dataWatcher.updateObject(5, state);
    		dataWatcher.updateObject(6, scale);
    		
    		if (firstTick) {
    			firstTick = false;
    			System.out.println("commenting out PacketDispatcher.sendPacketToAllInDimension call in MovingBlock");
    			//PacketDispatcher.sendPacketToAllInDimension(new Packet34EntityTeleport(getEntityId(), this.myEntitySize.multiplyBy32AndRound(posX), this.myEntitySize.multiplyBy32AndRound(posY), this.myEntitySize.multiplyBy32AndRound(posZ), (byte)0, (byte)0), worldObj.provider.dimensionId);
    		}
    	}*/
    	
    	//Main movement
        this.motionX *= (double)speedSlowing;
        this.motionY *= (double)speedSlowing;
        this.motionZ *= (double)speedSlowing;
        this.motionY -= (double)gravity;
        
        this.posX += this.motionX;
        this.posY += this.motionY;
        this.posZ += this.motionZ;
        
        this.setPosition(this.posX, this.posY, this.posZ);
        
        this.rotationPitchB += this.rotationPitchVel;
        this.rotationYawB += this.rotationYawVel;
        
        if (!world.isRemote) {
        	
        	if (posY < 0) {
        		this.setDead();
        		return;
        	}
        	
        	int curX = MathHelper.floor(posX);
	    	int curY = MathHelper.floor(posY);
	    	int curZ = MathHelper.floor(posZ);
	    	Block idCurPos = world.getBlockState(new BlockPos(curX, curY, curZ)).getBlock();
        	
	        if (blockifyDelay != -1 && age > blockifyDelay) {
		    	
	        	//should always raytrace ahead if motion > 1
	        	
		    	int aheadEndX = MathHelper.floor(posX + (motionX));
		    	int aheadEndY = MathHelper.floor(posY + (motionY));
		    	int aheadEndZ = MathHelper.floor(posZ + (motionZ));
		    	
		    	IBlockState id = world.getBlockState(new BlockPos(aheadEndX, aheadEndY, aheadEndZ));
	        	//System.out.println(idCurPos);
		    	
		    	if (isSolid(id)) {
		    		Vec3 motion = new Vec3(motionX, motionY, motionZ);
			    	double aheadDistEnd = motion.lengthVector();
			    	motion = motion.normalize();
			    	
			    	for (double curDist = 0; curDist < aheadDistEnd; curDist += 0.5D) {
			    		int aheadX = MathHelper.floor(posX + (motion.xCoord*curDist));
				    	int aheadY = MathHelper.floor(posY + (motion.yCoord*curDist));
				    	int aheadZ = MathHelper.floor(posZ + (motion.zCoord*curDist));
				    	IBlockState idCheck = world.getBlockState(new BlockPos(aheadX, aheadY, aheadZ));
			    		
			    		if (isSolid(idCheck)) {
			    			if (curDist < 1D) {
			    				//System.out.println("new solidify close!");
			    				//blockify(aheadX, aheadY, aheadZ);
			    				//break;
			    			} else {
			    				
			    			}
			    			
			    			curDist -= 0.5D;
			    			
			    			int tryX = MathHelper.floor(posX + (motion.xCoord*curDist));
					    	int tryY = MathHelper.floor(posY + (motion.yCoord*curDist));
					    	int tryZ = MathHelper.floor(posZ + (motion.zCoord*curDist));
				    		IBlockState idTry = world.getBlockState(new BlockPos(tryX, tryY, tryZ));
				    		if (!isSolid(idTry)) {
				    			//System.out.println("new solidify pull back!");
				    			blockify(tryX, tryY, tryZ);
				    		} else {
				    			//fail
				    			//System.out.println("solidify fail");
				    			this.setDead();
				    		}
				    		break;
			    		}
			    	}
		    		
			    	//blockify(coordsLastAir.posX, coordsLastAir.posY, coordsLastAir.posZ);
		    		//System.out.println("blockify - " + curX + " - " + curY + " - " + curZ);
		    	}
		    	
		    	
		    		
	        }
	        
	    	/*if (idCurPos == 0 || Block.blocksList[idCurPos].blockMaterial == Material.snow || Block.blocksList[idCurPos].blockMaterial == Material.plants) {
	    		coordsLastAir = new BlockCoord(curX, curY, curZ);
	    	}
	    	
	    	if (blockifyDelay != -1) {
		    	if (idCurPos != 0 && worldObj.getBlockTileEntity(curX, curY, curZ) == null && isSolid(idCurPos)) {
		    		//final check to help verify its falling into solid ground
		    		if (worldObj.getBlockId(curX, curY+1, curZ) != 0) {
		    			blockify(curX, curY, curZ);
			    		//System.out.println("blockify safety - " + curX + " - " + curY + " - " + curZ);
		    		}
		    		
		    	}
	    	}*/
	    	
	        tickCollisionEntities();
	        
	    	
	        //temp
	        //setDead();
	        
        } else {
        	//temp?
        	//motionY = 0.00F;
        }
        
        //if (gravity > 0) setDead();
        //setDead();
    	
        //so temp
    	//posY = 72F;
    	
        //onGround = true;
        
        //taken from super onUpdate()
    }
    
    public void tickCollisionEntities() {
    	blockToEntCollision = true;
    	
    	if (blockToEntCollision) {
        	double size = 0.5D;
	        List entities = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(size, size, size));
	        
	        for (int i = 0; entities != null && i < entities.size(); ++i)
	        {
	            Entity var10 = (Entity)entities.get(i);
	            
	            if (var10 != null) {
	            	if (!var10.isDead) {
			            if (var10 instanceof EntityLivingBase) {
			            	var10.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this), 4);
			            } else {
			            	double speed = Math.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
			            	//double speed2 = Math.sqrt(var10.motionX * var10.motionX + var10.motionY * var10.motionY + var10.motionZ * var10.motionZ);
			            	if (speed < 0.3D) {
			            		moveAway(this, var10, (float)speed * 0.5F);
				            	//break; //hmmmmm
			            	}
			            }
	            	}
	            }
	        }
    	}
        
    }
    
    public boolean isSolid(IBlockState id) {
    	return (id.getMaterial() != Material.WATER && id.getMaterial() != Material.CIRCUITS && id.getMaterial() != Material.SNOW && id.getMaterial() != Material.PLANTS && id.getMaterial().isSolid());
    }
    
    public void setPositionAndRotation(double par1, double par3, double par5, float par7, float par8, float parRoll)
    {
        this.prevPosX = this.posX = par1;
        this.prevPosY = this.posY = par3;
        this.prevPosZ = this.posZ = par5;
        this.prevRotationYaw = this.rotationYaw = this.rotationYawB = par7;
        this.prevRotationPitch = this.rotationPitch = this.rotationPitchB = par8;
        this.prevRotationRoll = this.rotationRoll = parRoll;
        //this.ySize = 0.0F;
        double d3 = (double)(this.prevRotationYaw - par7);

        if (d3 < -180.0D)
        {
            this.prevRotationYaw += 360.0F;
        }

        if (d3 >= 180.0D)
        {
            this.prevRotationYaw -= 360.0F;
        }

        this.setPosition(this.posX, this.posY, this.posZ);
        this.setRotation(par7, par8, parRoll);
    }
    
    protected void setRotation(float par1, float par2, float parRoll)
    {
        this.rotationYaw = par1 % 360.0F;
        this.rotationPitch = par2 % 360.0F;
        this.rotationRoll = parRoll % 360.0F;
    }
    
    public Vec3 getMoveAwayVector(Entity ent, Entity targ) {
    	float vecX = (float) (ent.posX - targ.posX);
    	float vecY = (float) (ent.posY - targ.posY);
    	float vecZ = (float) (ent.posZ - targ.posZ);

    	float dist2 = (float)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		return new Vec3(vecX / dist2, vecY / dist2, vecZ / dist2);
    }
    
    /*public Vector3f getMoveAwayVector3f(Entity ent, Entity targ) {
    	float vecX = (float) (ent.posX - targ.posX);
    	float vecY = (float) (ent.posY - targ.posY);
    	float vecZ = (float) (ent.posZ - targ.posZ);

    	float dist2 = (float)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		return new Vector3f(vecX / dist2, vecY / dist2, vecZ / dist2);
    }*/
    
    public void moveAway(Entity ent, Entity targ, float speed) {
		double vecX = ent.posX - targ.posX;
		double vecY = ent.posY - targ.posY;
		double vecZ = ent.posZ - targ.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
    
    public void moveTowards(Entity ent, Entity targ, float speed) {
		double vecX = targ.posX - ent.posX;
		double vecY = targ.posY - ent.posY;
		double vecZ = targ.posZ - ent.posZ;

		double dist2 = (double)Math.sqrt(vecX * vecX + vecY * vecY + vecZ * vecZ);
		ent.motionX += vecX / dist2 * speed;
		ent.motionY += vecY / dist2 * speed;
		ent.motionZ += vecZ / dist2 * speed;
	}
    
    public void triggerOwnerDied() {
    	blockifyDelay = 1;
    	gravity = 0.03F;
    	double speed = 0.3F;
    	motionX = rand.nextGaussian()*speed - rand.nextGaussian()*speed;
    	motionY = 0.3F + rand.nextGaussian()*speed - rand.nextGaussian()*speed;
    	motionZ = rand.nextGaussian()*speed - rand.nextGaussian()*speed;
    }
    
    public void blockify(int x, int y, int z) {
    	world.setBlockState(new BlockPos(x, y, z), Block.getBlockById(blockID).getDefaultState(), 3);
    	setDead();
    }
    
    @Override
    public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
    	setDead();
    	return super.attackEntityFrom(par1DamageSource, par2);
    }
    
    @SideOnly(Side.CLIENT)
    public void spawnParticles() {
    	/*for (int i = 0; i < 1; i++) {
    		
    		float speed = 0.1F;
    		float randPos = 8.0F;
    		float ahead = 2.5F;
    		
    		EntityMeteorTrailFX particle = new EntityMeteorTrailFX(worldObj, 
    				posX, 
    				posY, 
    				posZ, motionX, 0.25F, motionZ, 0, posX, posY, posZ);
    		
    		particle.maxScale = 3F;
    		particle.setMaxAge(100);
    		particle.motionX = (rand.nextFloat()*2-1) * speed;
    		particle.motionY = (rand.nextFloat()*2-1) * 0.1F;
    		particle.motionZ = (rand.nextFloat()*2-1) * speed;

    		particle.spawnAsWeatherEffect();
    	}*/
    }

	@Override
	protected void readEntityFromNBT(NBTTagCompound data) {
		blockID = data.getInteger("blockID");
		blockMeta = data.getInteger("blockMeta");
		blockifyDelay = data.getInteger("blockifyDelay");
		gravity = data.getFloat("gravity");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound data) {
		data.setInteger("blockID", blockID);
		data.setInteger("blockMeta", blockMeta);
		data.setInteger("blockifyDelay", blockifyDelay);
		data.setFloat("gravity", gravity);
	}
}
