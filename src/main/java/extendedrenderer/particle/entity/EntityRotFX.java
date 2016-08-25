package extendedrenderer.particle.entity;

import java.util.List;

import CoroUtil.api.weather.IWindHandler;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.particle.behavior.ParticleBehaviors;

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
    public int renderOrder = -1;
    
    //not a real entity ID now, just used for making rendering of entities slightly unique
    private int entityID = 0;
    
    public float rotationYaw;
    public float rotationPitch;
    
    public float windWeight = 5;
    public int particleDecayExtra = 0;
    public boolean isTransparent = true;
    
    public boolean killOnCollide = false;
	
	public boolean facePlayer = false;
    
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
    	/*if (callUpdateSuper) super.onUpdate();
    	this.setPrevPosX(this.getPosX());
    	this.setPrevPosZ(this.getPosY());
    	this.setPrevPosY(this.getPosZ());
        
        if (callUpdatePB && pb != null) pb.tickUpdate(this);
        
        //calling required stuff the super did
        if (!callUpdateSuper) {
        	this.setPrevPosX(this.getPosX());
            this.setPrevPosY(this.getPosY());
            this.setPrevPosZ(this.getPosZ());

            if (this.particleAge++ >= this.particleMaxAge)
            {
                this.setExpired();
            }
            
            if (spawnY != -1) {
            	setPosition(getPosX(), spawnY, getPosZ());
            }
            
            this.moveEntity(this.getMotionX(), this.getMotionY(), this.getMotionZ());
        }*/
    	if (killOnCollide) {
    		if (this.isCollided()) {
    			this.setExpired();
    		}
    		
    	}
    }
    
    public void setParticleTextureIndex(int par1)
    {
        this.particleTextureIndexInt = par1;
        if (this.getFXLayer() == 0) super.setParticleTextureIndex(par1);
    }
    
    public int getFXLayer()
    {
        return 5;
    }
    
    /*@Override
    public void setParticleIcon(TextureAtlasSprite p_110125_1_)
    {
        if (this.getFXLayer() == 1)
        {
            this.particleIcon = p_110125_1_;
        }
        else
        {
            if (this.getFXLayer() != 2)
            {
                throw new RuntimeException("Invalid call to Particle.setTex, use coordinate methods");
            }

            this.particleIcon = p_110125_1_;
        }
    }*/

    public void spawnAsWeatherEffect()
    {
        weatherEffect = true;
        ExtendedRenderer.rotEffRenderer.addEffect(this);
        //RELOCATED TO CODE AFTER CALLING spawnAsWeatherEffect(), also uses list in WeatherManagerClient
        //this.worldObj.addWeatherEffect(this);
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
		return this.worldObj;
	}
	
	public void setCanCollide(boolean val) {
		this.field_190017_n = val;
	}
	
	public boolean getCanCollide() {
		return this.field_190017_n;
	}
	
	public boolean isCollided() {
		return this.isCollided;
	}
	
	public double getDistance(double x, double y, double z)
    {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        return (double)MathHelper.sqrt_double(d0 * d0 + d1 * d1 + d2 * d2);
    }
	
	@Override
	public void renderParticle(VertexBuffer worldRendererIn, Entity entityIn,
			float partialTicks, float rotationX, float rotationZ,
			float rotationYZ, float rotationXY, float rotationXZ) {
		
		//override rotations
		if (!facePlayer) {
			rotationX = MathHelper.cos(this.rotationYaw * (float)Math.PI / 180.0F);
			rotationYZ = MathHelper.sin(this.rotationYaw * (float)Math.PI / 180.0F);
	        rotationXY = -rotationYZ * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
	        rotationXZ = rotationX * MathHelper.sin(this.rotationPitch * (float)Math.PI / 180.0F);
	        rotationZ = MathHelper.cos(this.rotationPitch * (float)Math.PI / 180.0F);
		}
		
		super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX,
				rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	@Override
	public float getWindWeight() {
		return windWeight;
	}

	@Override
	public int getParticleDecayExtra() {
		return particleDecayExtra;
	}
    
    @Override
    public boolean isTransparent() {
    	return isTransparent;
    }
    
    public void setKillOnCollide(boolean val) {
    	this.killOnCollide = val;
    }
    
    //override to fix isCollided check
    @Override
    public void moveEntity(double x, double y, double z)
    {
        double d0 = y;

        if (this.field_190017_n)
        {
            List<AxisAlignedBB> list = this.worldObj.getCollisionBoxes((Entity)null, this.getEntityBoundingBox().addCoord(x, y, z));

            for (AxisAlignedBB axisalignedbb : list)
            {
                y = axisalignedbb.calculateYOffset(this.getEntityBoundingBox(), y);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, y, 0.0D));

            for (AxisAlignedBB axisalignedbb1 : list)
            {
                x = axisalignedbb1.calculateXOffset(this.getEntityBoundingBox(), x);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, 0.0D, 0.0D));

            for (AxisAlignedBB axisalignedbb2 : list)
            {
                z = axisalignedbb2.calculateZOffset(this.getEntityBoundingBox(), z);
            }

            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(0.0D, 0.0D, z));
        }
        else
        {
            this.setEntityBoundingBox(this.getEntityBoundingBox().offset(x, y, z));
        }

        this.resetPositionToBB();
        this.isCollided = d0 != y && d0 < 0.0D;

        if (x != x)
        {
            this.motionX = 0.0D;
        }

        if (z != z)
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
}
