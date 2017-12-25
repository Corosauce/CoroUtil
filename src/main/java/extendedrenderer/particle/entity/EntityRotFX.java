package extendedrenderer.particle.entity;

import java.util.List;

import CoroUtil.api.weather.IWindHandler;
import CoroUtil.util.CoroUtilBlockLightCache;
import CoroUtil.util.Vec3;
import extendedrenderer.render.RotatingParticleManager;
import extendedrenderer.shader.IShaderRenderedEntity;
import extendedrenderer.shader.InstancedMeshParticle;
import extendedrenderer.shader.Matrix4fe;
import extendedrenderer.shader.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
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
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

import javax.vecmath.Vector3f;

@SideOnly(Side.CLIENT)
public class EntityRotFX extends Particle implements IWindHandler, IShaderRenderedEntity
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

	//facePlayer will override this
    public boolean facePlayerYaw = false;
	
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

    private float ticksFadeInMax = 0;
    private float ticksFadeOutMax = 0;

    private boolean dontRenderUnderTopmostBlock = false;

    private boolean killWhenUnderTopmostBlock = false;

    public int killWhenUnderCameraAtLeast = 0;

    public int killWhenFarFromCameraAtLeast = 0;

    private float ticksFadeOutMaxOnDeath = -1;
    private float ticksFadeOutCurOnDeath = 0;
    protected boolean fadingOut = false;

    public float avoidTerrainAngle = 0;

    //halp
    public float rotationAroundCenter = 0;
    public float rotationSpeedAroundCenter = 0;

    private boolean slantParticleToWind = false;

    public Quaternion rotation;

    public boolean fastLight = false;

    public float brightnessCache = 0.5F;

    public boolean rotateOrderXY = false;

    public float extraYRotation = 0;

    public EntityRotFX(World par1World, double par2, double par4, double par6, double par8, double par10, double par12)
    {
        super(par1World, par2, par4, par6, par8, par10, par12);
        setSize(0.3F, 0.3F);
        //this.isImmuneToFire = true;
        //this.setMaxAge(100);
        
        this.entityID = par1World.rand.nextInt(100000);

        rotation = new Quaternion();

        brightnessCache = CoroUtilBlockLightCache.getBrightnessNonLightmap(world, (float)posX, (float)posY, (float)posZ);
    }

    public boolean isSlantParticleToWind() {
        return slantParticleToWind;
    }

    public void setSlantParticleToWind(boolean slantParticleToWind) {
        this.slantParticleToWind = slantParticleToWind;
    }

    public float getTicksFadeOutMaxOnDeath() {
        return ticksFadeOutMaxOnDeath;
    }

    public void setTicksFadeOutMaxOnDeath(float ticksFadeOutMaxOnDeath) {
        this.ticksFadeOutMaxOnDeath = ticksFadeOutMaxOnDeath;
    }

    public boolean isKillWhenUnderTopmostBlock() {
        return killWhenUnderTopmostBlock;
    }

    public void setKillWhenUnderTopmostBlock(boolean killWhenUnderTopmostBlock) {
        this.killWhenUnderTopmostBlock = killWhenUnderTopmostBlock;
    }

    public boolean isDontRenderUnderTopmostBlock() {
        return dontRenderUnderTopmostBlock;
    }

    public void setDontRenderUnderTopmostBlock(boolean dontRenderUnderTopmostBlock) {
        this.dontRenderUnderTopmostBlock = dontRenderUnderTopmostBlock;
    }

    public float getTicksFadeInMax() {
        return ticksFadeInMax;
    }

    public void setTicksFadeInMax(float ticksFadeInMax) {
        this.ticksFadeInMax = ticksFadeInMax;
    }

    public float getTicksFadeOutMax() {
        return ticksFadeOutMax;
    }

    public void setTicksFadeOutMax(float ticksFadeOutMax) {
        this.ticksFadeOutMax = ticksFadeOutMax;
    }
    
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

        //if (this.entityID % 400 == 0) System.out.println("onUpdate time: " + this.worldObj.getTotalWorldTime());
    	
    	if (!isVanillaMotionDampen()) {
    		//cancel motion dampening (which is basically air resistance)
    		//keep this up to date with the inverse of whatever Particle.onUpdate uses
        	this.motionX /= 0.9800000190734863D;
            this.motionY /= 0.9800000190734863D;
            this.motionZ /= 0.9800000190734863D;
    	}

    	if (!this.isExpired && !fadingOut) {
            if (killOnCollide) {
                if (this.isCollided()) {
                    startDeath();
                }

            }

            if (killWhenUnderTopmostBlock) {
                int height = this.world.getPrecipitationHeight(new BlockPos(this.posX, this.posY, this.posZ)).getY();
                if (this.posY <= height) {
                    startDeath();
                }
            }

            //case: when on high pillar and rain is falling far below you, start killing it / fading it out
            if (killWhenUnderCameraAtLeast != 0) {
                Entity ent = Minecraft.getMinecraft().getRenderViewEntity();
                if (this.posY < ent.posY - killWhenUnderCameraAtLeast) {
                    startDeath();
                }
            }

            if (killWhenFarFromCameraAtLeast != 0) {
                if (getAge() > 20 && getAge() % 5 == 0) {
                    Entity ent = Minecraft.getMinecraft().getRenderViewEntity();
                    if (ent.getDistance(this.posX, this.posY, this.posZ) > killWhenFarFromCameraAtLeast) {
                        //System.out.println("far kill");
                        startDeath();
                    }
                }
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

        if (!fadingOut) {
            if (ticksFadeInMax > 0 && this.getAge() < ticksFadeInMax) {
                //System.out.println("particle.getAge(): " + particle.getAge());
                this.setAlphaF((float)this.getAge() / ticksFadeInMax);
                //particle.setAlphaF(1);
            } else if (ticksFadeOutMax > 0 && this.getAge() > this.getMaxAge() - ticksFadeOutMax) {
                float count = this.getAge() - (this.getMaxAge() - ticksFadeOutMax);
                float val = (ticksFadeOutMax - (count)) / ticksFadeOutMax;
                //System.out.println(val);
                this.setAlphaF(val);
                //make sure fully visible otherwise
            } else if (ticksFadeInMax > 0 || ticksFadeOutMax > 0) {
                this.setAlphaF(1F);
            }
        } else {
    	    if (ticksFadeOutCurOnDeath < ticksFadeOutMaxOnDeath) {
                ticksFadeOutCurOnDeath++;
            } else {
    	        this.setExpired();
            }
            float val = 1F - (ticksFadeOutCurOnDeath / ticksFadeOutMaxOnDeath);
            //System.out.println(val);
            this.setAlphaF(val);
        }

        if (world.getTotalWorldTime() % 5 == 0) {
            brightnessCache = CoroUtilBlockLightCache.getBrightnessCached(world, (float)posX, (float)posY, (float)posZ);
        }

        rotationAroundCenter += rotationSpeedAroundCenter;
        if (rotationAroundCenter >= 360) {
            rotationAroundCenter -= 360;
        }

        if (slantParticleToWind) {
            double motionXZ = Math.sqrt(motionX * motionX + motionZ * motionZ);
            rotationPitch = (float)Math.atan2(motionY, motionXZ);
        }

        updateQuaternion(null);
    }

    public void startDeath() {
        if (ticksFadeOutMaxOnDeath > 0) {
            ticksFadeOutCurOnDeath = 0;//ticksFadeOutMaxOnDeath;
            fadingOut = true;
        } else {
            this.setExpired();
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

    @Override
    public Vector3f getPosition() {
        return new Vector3f((float)posX, (float)posY, (float)posZ);
    }

    @Override
    public Quaternion getQuaternion() {
        return this.rotation;
    }

    @Override
    public float getScale() {
    	return particleScale;
    }

    public Vec3 getPos() {
        return new Vec3(posX, posY, posZ);
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
	
	public void setCanCollide(boolean val) {
		this.canCollide = val;
	}
	
	public boolean getCanCollide() {
		return this.canCollide;
	}
	
	public boolean isCollided() {
		return this.onGround;
	}
	
	public double getDistance(double x, double y, double z)
    {
        double d0 = this.posX - x;
        double d1 = this.posY - y;
        double d2 = this.posZ - z;
        return (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }
	
	@Override
	public void renderParticle(BufferBuilder worldRendererIn, Entity entityIn,
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
		
		/*IBlockState state = this.getWorld().getBlockState(new BlockPos(posX, posY, posZ));
		if (state.getBlock() != Blocks.AIR) {
			System.out.println("particle in: " + state);
		}*/
		
		super.renderParticle(worldRendererIn, entityIn, partialTicks, rotationX,
				rotationZ, rotationYZ, rotationXY, rotationXZ);
	}

	public void renderParticleForShader(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                        float partialTicks, float rotationX, float rotationZ,
                                        float rotationYZ, float rotationXY, float rotationXZ) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

        //camera relative positions, for world position, remove the interpPos values
        float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - this.interpPosX);
        float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - this.interpPosY);
        float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - this.interpPosZ);
        //Vector3f pos = new Vector3f((float) (entityIn.posX - particle.posX), (float) (entityIn.posY - particle.posY), (float) (entityIn.posZ - particle.posZ));
        Vector3f pos = new Vector3f(posX, posY, posZ);

        Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos);

        //adjust to perspective and camera
        //Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        //upload to buffer
        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), mesh.instanceDataBuffer);

        //brightness
        float brightness;
        //brightness = CoroUtilBlockLightCache.getBrightnessCached(world, (float)this.posX, (float)this.posY, (float)this.posZ);
        brightness = brightnessCache;
        //brightness = -1F;
        //brightness = CoroUtilBlockLightCache.brightnessPlayer;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS, brightness);

        int rgbaIndex = 0;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getRedColorF());
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getGreenColorF());
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getBlueColorF());
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos)
                + mesh.MATRIX_SIZE_FLOATS + 1 + (rgbaIndex++), this.getAlphaF());

        mesh.curBufferPos++;
        
    }

    /*public void renderParticleForShaderTest(InstancedMeshParticle mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                            float partialTicks, float rotationX, float rotationZ,
                                            float rotationYZ, float rotationXY, float rotationXZ) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

        int rgbaIndex = 0;
        mesh.instanceDataBufferTest.put(mesh.INSTANCE_SIZE_FLOATS_TEST * (mesh.curBufferPos)
                + (rgbaIndex++), this.getRedColorF());
        mesh.instanceDataBufferTest.put(mesh.INSTANCE_SIZE_FLOATS_TEST * (mesh.curBufferPos)
                + (rgbaIndex++), this.getGreenColorF());
        mesh.instanceDataBufferTest.put(mesh.INSTANCE_SIZE_FLOATS_TEST * (mesh.curBufferPos)
                + (rgbaIndex++), this.getBlueColorF());
        mesh.instanceDataBufferTest.put(mesh.INSTANCE_SIZE_FLOATS_TEST * (mesh.curBufferPos)
                + (rgbaIndex++), this.getAlphaF());

        mesh.curBufferPos++;
    }*/

	@Override
	public float getWindWeight() {
		return windWeight;
	}

	@Override
	public int getParticleDecayExtra() {
		return particleDecayExtra;
	}
    
    @Override
    public boolean shouldDisableDepth() {
    	return isTransparent;
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
            List<AxisAlignedBB> list = this.world.getCollisionBoxes((Entity)null, this.getBoundingBox().grow(x, y, z));

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
        //this.isCollided = yy != y && yy < 0.0D;
        this.onGround = yy != y || xx != x || zz != z;

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

    public void updateQuaternion(Entity camera) {

        if (camera != null) {
            if (this.facePlayer) {
                this.rotationYaw = camera.rotationYaw;
                this.rotationPitch = camera.rotationPitch;
            } else if (facePlayerYaw) {
                this.rotationYaw = camera.rotationYaw;
            }
        }

        Quaternion qY = new Quaternion();
        Quaternion qX = new Quaternion();
        qY.setFromAxisAngle(new Vector4f(0, 1, 0, (float)Math.toRadians(-this.rotationYaw - 180F)));
        qX.setFromAxisAngle(new Vector4f(1, 0, 0, (float)Math.toRadians(-this.rotationPitch)));
        if (this.rotateOrderXY) {
            Quaternion.mul(qX, qY, this.rotation);
        } else {
            Quaternion.mul(qY, qX, this.rotation);
        }
    }

    @Override
    public void setRBGColorF(float particleRedIn, float particleGreenIn, float particleBlueIn) {
        super.setRBGColorF(particleRedIn, particleGreenIn, particleBlueIn);
        RotatingParticleManager.markDirtyVBO2();
    }

    @Override
    public void setAlphaF(float alpha) {
        super.setAlphaF(alpha);
        RotatingParticleManager.markDirtyVBO2();
    }
}
