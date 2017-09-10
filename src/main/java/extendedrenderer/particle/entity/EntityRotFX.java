package extendedrenderer.particle.entity;

import CoroUtil.api.weather.IWindHandler;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.behavior.ParticleBehaviors;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

@SideOnly(Side.CLIENT)
public class EntityRotFX extends Particle implements IWindHandler
{
    public boolean weatherEffect = false;

    public float spawnY = -1;

    //this field and 2 methods below are for backwards compatibility with old particle system from the new icon based system
    public int particleTextureIndexInt = 0;

    public float brightness = 0.7F;

    public ParticleBehaviors pb = null; //designed to be a reference to the central objects particle behavior

    public boolean callUpdateSuper = true;
    public boolean callUpdatePB = true;

    public float renderRange = 128F;

    //used in RotatingEffectRenderer to assist in solving some transparency ordering issues, eg, tornado funnel before clouds
    public int renderOrder = 0;

    //not a real entity ID now, just used for making rendering of entities slightly unique
    private int entityID = 0;

    public int debugID = 0;

    public float rotationYaw;
    public float rotationPitch;

    public float windWeight = 5;
    public int particleDecayExtra = 0;
    public boolean isTransparent = true;

    public boolean killOnCollide = false;

	public boolean facePlayer = false;

	public boolean vanillaMotionDampen = true;

    //for particle behaviors
    public double aboveGroundHeight = 4.5D;
    public boolean checkAheadToBounce = true;
    public boolean collisionSpeedDampen = true;

    public double bounceSpeed = 0.05D;
    public double bounceSpeedMax = 0.15D;
    public double bounceSpeedAhead = 0.35D;
    public double bounceSpeedMaxAhead = 0.25D;

    public boolean spinFast = false;

    public boolean isCollided = false;

	public EntityRotFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        super(par1World, par2, par4, par6, par8, par10, par12);
        setSize(0.3F, 0.3F);
        //this.isImmuneToFire = true;
        //this.setMaxAge(100);

        this.entityID = par1World.rand.nextInt(100000);
    }/*

    public EntityRotFX(World var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, int colorIndex)
    {
        super(var1, var2, var4, var6, var8, var10, var12);
        setSize(0.3F, 0.3F);
        this.isImmuneToFire = true;
    }

    public EntityRotFX(World var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, int texIDs[])
    {
        super(var1, var2, var4, var6, var8, var10, var12);
        setSize(0.3F, 0.3F);
        this.isImmuneToFire = true;
    }

    public EntityRotFX(World worldIn, double posXIn, double posYIn, double posZIn) {
    	super(worldIn, posXIn, posYIn, posZIn);
	}*/

    public int getParticleTextureIndex()
    {
        return this.particleTextureIndexInt;
    }

    public void setMaxAge(int par) {
    	particleMaxAge = par;
    }

    public float getAlphaF()
    {
        return this.particleAlpha;
    }

    @Override
    public void setExpired() {
    	if (pb != null) pb.particles.remove(this);
    	super.setExpired();
    }

    @Override
    public void onUpdate() {
    	super.onUpdate();

    	if (!isVanillaMotionDampen()) {
    		//cancel motion dampening (which is basically air resistance)
    		//keep this up to date with the inverse of whatever Particle.onUpdate uses
        	this.motionX /= 0.9800000190734863D;
            this.motionY /= 0.9800000190734863D;
            this.motionZ /= 0.9800000190734863D;
    	}

    	if (killOnCollide) {
    		if (this.onGround) {
    			this.setExpired();
    		}

    	}

    	if (!collisionSpeedDampen) {
            //if (this.isCollided()) {
            if (this.onGround) {
                this.motionX /= 0.699999988079071D;
                this.motionZ /= 0.699999988079071D;
            }
        }

        if (spinFast) {
            this.rotationPitch += this.entityID % 2 == 0 ? 10 : -10;
            this.rotationYaw += this.entityID % 2 == 0 ? -10 : 10;
        }
    }

    public void setParticleTextureIndex(int par1)
    {
        this.particleTextureIndexInt = par1;
        if (this.getFXLayer() == 0) super.setParticleTextureIndex(par1);
    }

    @Override
    public int getFXLayer()
    {
        return 5;
    }

    public void spawnAsWeatherEffect()
    {
        weatherEffect = true;
        ExtendedRenderer.rotEffRenderer.addEffect(this);
        //RELOCATED TO CODE AFTER CALLING spawnAsWeatherEffect(), also uses list in WeatherManagerClient
        //this.world.addWeatherEffect(this);
    }

    public int getAge()
    {
        return particleAge;
    }

    public void setAge(int age)
    {
        particleAge = age;
    }

    public int getMaxAge()
    {
        return particleMaxAge;
    }

    public void setSize(float par1, float par2)
    {
        super.setSize(par1, par2);
    }

    public void setGravity(float par) {
    	particleGravity = par;
    }

    public float maxRenderRange() {
    	return renderRange;
    }

    public void setScale(float parScale) {
    	particleScale = parScale;
    }

    public float getScale() {
    	return particleScale;
    }

	public double getPosX() {
		return posX;
	}

	public void setPosX(double posX) {
		this.posX = posX;
	}

	public double getPosY() {
		return posY;
	}

	public void setPosY(double posY) {
		this.posY = posY;
	}

	public double getPosZ() {
		return posZ;
	}

	public void setPosZ(double posZ) {
		this.posZ = posZ;
	}

	public double getMotionX() {
		return motionX;
	}

	public void setMotionX(double motionX) {
		this.motionX = motionX;
	}

	public double getMotionY() {
		return motionY;
	}

	public void setMotionY(double motionY) {
		this.motionY = motionY;
	}

	public double getMotionZ() {
		return motionZ;
	}

	public void setMotionZ(double motionZ) {
		this.motionZ = motionZ;
	}

	public double getPrevPosX() {
		return prevPosX;
	}

	public void setPrevPosX(double prevPosX) {
		this.prevPosX = prevPosX;
	}

	public double getPrevPosY() {
		return prevPosY;
	}

	public void setPrevPosY(double prevPosY) {
		this.prevPosY = prevPosY;
	}

	public double getPrevPosZ() {
		return prevPosZ;
	}

	public void setPrevPosZ(double prevPosZ) {
		this.prevPosZ = prevPosZ;
	}

	public int getEntityId() {
		return entityID;
	}

	public World getWorld() {
		return this.world;
	}



	public boolean isCollided() {
		return this.isCollided;
	}

	public double getDistance(double x, double y, double z)
    {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {

		//override rotations
		if (!facePlayer) {
			rotationX = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
			rotationYZ = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
	        rotationXY = -rotationYZ * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
	        rotationXZ = rotationX * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
	        rotationZ = MathHelper.cos(this.rotationPitch * (float)Math.PI / 180.0F);
		}

		/*IBlockState state = this.getWorld().getBlockState(new BlockPos(posX, posY, posZ));
		if (state.getBlock() != Blocks.AIR) {
			System.out.println("particle in: " + state);
		}*/

		super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	@Override
	public float getWindWeight() {
		return windWeight;
	}

	@Override
	public int getParticleDecayExtra() {
		return particleDecayExtra;
	}

    public void setKillOnCollide(boolean val) {
    	this.killOnCollide = val;
    }

    //override to fix isCollided check
    @Override
    public void move(double x, double y, double z)
    {
        double yy = y;
        double xx = x;
        double zz = z;

        if (this.canCollide)
        {
            List<AxisAlignedBB> list = this.world.getCollisionBoxes(null, this.getBoundingBox().expand(x, y, z));

            for (AxisAlignedBB axisalignedbb : list)
            {
                y = axisalignedbb.calculateYOffset(this.getBoundingBox(), y);
            }

            this.setBoundingBox(this.getBoundingBox().offset(0.0D, y, 0.0D));

            for (AxisAlignedBB axisalignedbb1 : list)
            {
                x = axisalignedbb1.calculateXOffset(this.getBoundingBox(), x);
            }

            this.setBoundingBox(this.getBoundingBox().offset(x, 0.0D, 0.0D));

            for (AxisAlignedBB axisalignedbb2 : list)
            {
                z = axisalignedbb2.calculateZOffset(this.getBoundingBox(), z);
            }

            this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, z));
        }
        else
        {
            this.setBoundingBox(this.getBoundingBox().offset(x, y, z));
        }

        this.resetPositionToBB();
        //was y != y before
        this.onGround = yy != y && yy < 0.0D;
        this.isCollided = yy != y || xx != x || zz != z;

        if (xx != x)
        {
            this.motionX = 0.0D;
        }

        if (zz != z)
        {
            this.motionZ = 0.0D;
        }
    }

    public void setFacePlayer(boolean val) {
    	this.facePlayer = val;
    }

    public TextureAtlasSprite getParticleTexture() {
    	return this.particleTexture;
    }

    public boolean isVanillaMotionDampen() {
		return vanillaMotionDampen;
	}

	public void setVanillaMotionDampen(boolean motionDampen) {
		this.vanillaMotionDampen = motionDampen;
	}

    @Override
    public int getBrightnessForRender(float p_189214_1_) {
        return super.getBrightnessForRender(p_189214_1_);//(int)((float)super.getBrightnessForRender(p_189214_1_))/* * this.world.getSunBrightness(1F))*/;
    }

    public void setCanCollide(boolean canCollide) {
        this.canCollide = canCollide;
    }
}
