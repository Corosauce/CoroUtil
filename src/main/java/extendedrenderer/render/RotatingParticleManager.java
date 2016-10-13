package extendedrenderer.render;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUConstants;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.Barrier;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleBlockDust;
import net.minecraft.client.particle.ParticleBreaking;
import net.minecraft.client.particle.ParticleBubble;
import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.client.particle.ParticleCrit;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleDragonBreath;
import net.minecraft.client.particle.ParticleDrip;
import net.minecraft.client.particle.ParticleEmitter;
import net.minecraft.client.particle.ParticleEnchantmentTable;
import net.minecraft.client.particle.ParticleEndRod;
import net.minecraft.client.particle.ParticleExplosion;
import net.minecraft.client.particle.ParticleExplosionHuge;
import net.minecraft.client.particle.ParticleExplosionLarge;
import net.minecraft.client.particle.ParticleFallingDust;
import net.minecraft.client.particle.ParticleFirework;
import net.minecraft.client.particle.ParticleFlame;
import net.minecraft.client.particle.ParticleFootStep;
import net.minecraft.client.particle.ParticleHeart;
import net.minecraft.client.particle.ParticleLava;
import net.minecraft.client.particle.ParticleMobAppearance;
import net.minecraft.client.particle.ParticleNote;
import net.minecraft.client.particle.ParticlePortal;
import net.minecraft.client.particle.ParticleRain;
import net.minecraft.client.particle.ParticleRedstone;
import net.minecraft.client.particle.ParticleSmokeLarge;
import net.minecraft.client.particle.ParticleSmokeNormal;
import net.minecraft.client.particle.ParticleSnowShovel;
import net.minecraft.client.particle.ParticleSpell;
import net.minecraft.client.particle.ParticleSplash;
import net.minecraft.client.particle.ParticleSuspend;
import net.minecraft.client.particle.ParticleSuspendedTown;
import net.minecraft.client.particle.ParticleSweepAttack;
import net.minecraft.client.particle.ParticleWaterWake;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

import extendedrenderer.ExtendedRenderer;

@SideOnly(Side.CLIENT)
public class RotatingParticleManager
{
    private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
    /** Reference to the World object. */
    protected World worldObj;
    public final ArrayDeque<Particle>[][] fxLayers = new ArrayDeque[4][];
    private final Queue<ParticleEmitter> particleEmitters = Queues.<ParticleEmitter>newArrayDeque();
    private final TextureManager renderer;
    /** RNG. */
    private final Random rand = new Random();
    private final Map<Integer, IParticleFactory> particleTypes = Maps.<Integer, IParticleFactory>newHashMap();
    private final Queue<Particle> queueEntityFX = Queues.<Particle>newArrayDeque();
    
    //ExtendedRenderer Additions
    
    //leaves, rain, unused, snow
    public static final ResourceLocation resLayer5 = new ResourceLocation(ExtendedRenderer.modid + ":textures/particles/particles_16.png");

    public RotatingParticleManager(World worldIn, TextureManager rendererIn)
    {
        this.worldObj = worldIn;
        this.renderer = rendererIn;

        for (int i = 0; i < 4; ++i)
        {
            this.fxLayers[i] = new ArrayDeque[2];

            for (int j = 0; j < 2; ++j)
            {
                this.fxLayers[i][j] = Queues.newArrayDeque();
            }
        }

        this.registerVanillaParticles();
    }

    private void registerVanillaParticles()
    {
        this.registerParticle(EnumParticleTypes.EXPLOSION_NORMAL.getParticleID(), new ParticleExplosion.Factory());
        this.registerParticle(EnumParticleTypes.WATER_BUBBLE.getParticleID(), new ParticleBubble.Factory());
        this.registerParticle(EnumParticleTypes.WATER_SPLASH.getParticleID(), new ParticleSplash.Factory());
        this.registerParticle(EnumParticleTypes.WATER_WAKE.getParticleID(), new ParticleWaterWake.Factory());
        this.registerParticle(EnumParticleTypes.WATER_DROP.getParticleID(), new ParticleRain.Factory());
        this.registerParticle(EnumParticleTypes.SUSPENDED.getParticleID(), new ParticleSuspend.Factory());
        this.registerParticle(EnumParticleTypes.SUSPENDED_DEPTH.getParticleID(), new ParticleSuspendedTown.Factory());
        this.registerParticle(EnumParticleTypes.CRIT.getParticleID(), new ParticleCrit.Factory());
        this.registerParticle(EnumParticleTypes.CRIT_MAGIC.getParticleID(), new ParticleCrit.MagicFactory());
        this.registerParticle(EnumParticleTypes.SMOKE_NORMAL.getParticleID(), new ParticleSmokeNormal.Factory());
        this.registerParticle(EnumParticleTypes.SMOKE_LARGE.getParticleID(), new ParticleSmokeLarge.Factory());
        this.registerParticle(EnumParticleTypes.SPELL.getParticleID(), new ParticleSpell.Factory());
        this.registerParticle(EnumParticleTypes.SPELL_INSTANT.getParticleID(), new ParticleSpell.InstantFactory());
        this.registerParticle(EnumParticleTypes.SPELL_MOB.getParticleID(), new ParticleSpell.MobFactory());
        this.registerParticle(EnumParticleTypes.SPELL_MOB_AMBIENT.getParticleID(), new ParticleSpell.AmbientMobFactory());
        this.registerParticle(EnumParticleTypes.SPELL_WITCH.getParticleID(), new ParticleSpell.WitchFactory());
        this.registerParticle(EnumParticleTypes.DRIP_WATER.getParticleID(), new ParticleDrip.WaterFactory());
        this.registerParticle(EnumParticleTypes.DRIP_LAVA.getParticleID(), new ParticleDrip.LavaFactory());
        this.registerParticle(EnumParticleTypes.VILLAGER_ANGRY.getParticleID(), new ParticleHeart.AngryVillagerFactory());
        this.registerParticle(EnumParticleTypes.VILLAGER_HAPPY.getParticleID(), new ParticleSuspendedTown.HappyVillagerFactory());
        this.registerParticle(EnumParticleTypes.TOWN_AURA.getParticleID(), new ParticleSuspendedTown.Factory());
        this.registerParticle(EnumParticleTypes.NOTE.getParticleID(), new ParticleNote.Factory());
        this.registerParticle(EnumParticleTypes.PORTAL.getParticleID(), new ParticlePortal.Factory());
        this.registerParticle(EnumParticleTypes.ENCHANTMENT_TABLE.getParticleID(), new ParticleEnchantmentTable.EnchantmentTable());
        this.registerParticle(EnumParticleTypes.FLAME.getParticleID(), new ParticleFlame.Factory());
        this.registerParticle(EnumParticleTypes.LAVA.getParticleID(), new ParticleLava.Factory());
        this.registerParticle(EnumParticleTypes.FOOTSTEP.getParticleID(), new ParticleFootStep.Factory());
        this.registerParticle(EnumParticleTypes.CLOUD.getParticleID(), new ParticleCloud.Factory());
        this.registerParticle(EnumParticleTypes.REDSTONE.getParticleID(), new ParticleRedstone.Factory());
        this.registerParticle(EnumParticleTypes.FALLING_DUST.getParticleID(), new ParticleFallingDust.Factory());
        this.registerParticle(EnumParticleTypes.SNOWBALL.getParticleID(), new ParticleBreaking.SnowballFactory());
        this.registerParticle(EnumParticleTypes.SNOW_SHOVEL.getParticleID(), new ParticleSnowShovel.Factory());
        this.registerParticle(EnumParticleTypes.SLIME.getParticleID(), new ParticleBreaking.SlimeFactory());
        this.registerParticle(EnumParticleTypes.HEART.getParticleID(), new ParticleHeart.Factory());
        this.registerParticle(EnumParticleTypes.BARRIER.getParticleID(), new Barrier.Factory());
        this.registerParticle(EnumParticleTypes.ITEM_CRACK.getParticleID(), new ParticleBreaking.Factory());
        this.registerParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), new ParticleDigging.Factory());
        this.registerParticle(EnumParticleTypes.BLOCK_DUST.getParticleID(), new ParticleBlockDust.Factory());
        this.registerParticle(EnumParticleTypes.EXPLOSION_HUGE.getParticleID(), new ParticleExplosionHuge.Factory());
        this.registerParticle(EnumParticleTypes.EXPLOSION_LARGE.getParticleID(), new ParticleExplosionLarge.Factory());
        this.registerParticle(EnumParticleTypes.FIREWORKS_SPARK.getParticleID(), new ParticleFirework.Factory());
        this.registerParticle(EnumParticleTypes.MOB_APPEARANCE.getParticleID(), new ParticleMobAppearance.Factory());
        this.registerParticle(EnumParticleTypes.DRAGON_BREATH.getParticleID(), new ParticleDragonBreath.Factory());
        this.registerParticle(EnumParticleTypes.END_ROD.getParticleID(), new ParticleEndRod.Factory());
        this.registerParticle(EnumParticleTypes.DAMAGE_INDICATOR.getParticleID(), new ParticleCrit.DamageIndicatorFactory());
        this.registerParticle(EnumParticleTypes.SWEEP_ATTACK.getParticleID(), new ParticleSweepAttack.Factory());
    }

    public void registerParticle(int id, IParticleFactory particleFactory)
    {
        this.particleTypes.put(Integer.valueOf(id), particleFactory);
    }

    public void emitParticleAtEntity(Entity entityIn, EnumParticleTypes particleTypes)
    {
        this.particleEmitters.add(new ParticleEmitter(this.worldObj, entityIn, particleTypes));
    }

    /**
     * Spawns the relevant particle according to the particle id.
     */
    @Nullable
    public Particle spawnEffectParticle(int particleId, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters)
    {
        IParticleFactory iparticlefactory = (IParticleFactory)this.particleTypes.get(Integer.valueOf(particleId));

        if (iparticlefactory != null)
        {
            Particle particle = iparticlefactory.getEntityFX(particleId, this.worldObj, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);

            if (particle != null)
            {
                this.addEffect(particle);
                return particle;
            }
        }

        return null;
    }

    public void addEffect(Particle effect)
    {
        if (effect == null) return; //Forge: Prevent modders from being bad and adding nulls causing untraceable NPEs.
        this.queueEntityFX.add(effect);
    }

    public void updateEffects()
    {
        for (int i = 0; i < 4; ++i)
        {
            this.updateEffectLayer(i);
        }

        if (!this.particleEmitters.isEmpty())
        {
            List<ParticleEmitter> list = Lists.<ParticleEmitter>newArrayList();

            for (ParticleEmitter particleemitter : this.particleEmitters)
            {
                particleemitter.onUpdate();

                if (!particleemitter.isAlive())
                {
                    list.add(particleemitter);
                }
            }

            this.particleEmitters.removeAll(list);
        }

        if (!this.queueEntityFX.isEmpty())
        {
            for (Particle particle = (Particle)this.queueEntityFX.poll(); particle != null; particle = (Particle)this.queueEntityFX.poll())
            {
                int j = particle.getFXLayer();
                int k = particle.isTransparent() ? 0 : 1;

                if (this.fxLayers[j][k].size() >= 16384)
                {
                    this.fxLayers[j][k].removeFirst();
                }

                this.fxLayers[j][k].add(particle);
            }
        }
    }

    private void updateEffectLayer(int layer)
    {
        //this.worldObj.theProfiler.startSection(layer + "");

        for (int i = 0; i < 2; ++i)
        {
            //this.worldObj.theProfiler.startSection(i + "");
            this.tickParticleList(this.fxLayers[layer][i]);
            //this.worldObj.theProfiler.endSection();
        }

        //this.worldObj.theProfiler.endSection();
    }

    private void tickParticleList(Queue<Particle> p_187240_1_)
    {
        if (!p_187240_1_.isEmpty())
        {
            Iterator<Particle> iterator = p_187240_1_.iterator();

            while (iterator.hasNext())
            {
                Particle particle = (Particle)iterator.next();
                this.tickParticle(particle);

                if (!particle.isAlive())
                {
                    iterator.remove();
                }
            }
        }
    }

    private void tickParticle(final Particle particle)
    {
        try
        {
            particle.onUpdate();
        }
        catch (Throwable throwable)
        {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Ticking Rotating Particle");
            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being ticked");
            final int i = particle.getFXLayer();
            crashreportcategory.setDetail("Rotating Particle", new ICrashReportDetail<String>()
            {
                public String call() throws Exception
                {
                    return particle.toString();
                }
            });
            crashreportcategory.setDetail("Particle Type", new ICrashReportDetail<String>()
            {
                public String call() throws Exception
                {
                    return i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i));
                }
            });
            throw new ReportedException(crashreport);
        }
    }

    /**
     * Renders all current particles. Args player, partialTickTime
     */
    public void renderParticles(Entity entityIn, float partialTicks)
    {
        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        Particle.field_190016_K = entityIn.getLook(partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        //GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);
        
        GlStateManager.disableCull();
        
        //fix mipmapping making low alpha transparency particles dissapear based on distance, window size, particle size
        int mip_min = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
        int mip_mag = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

        for (int i_nf = 0; i_nf < 3; ++i_nf)
        {
            final int i = i_nf;

            for (int j = 0; j < 2; ++j)
            {
                if (!this.fxLayers[i][j].isEmpty())
                {
                    switch (j)
                    {
                        case 0:
                            GlStateManager.depthMask(false);
                            break;
                        case 1:
                            GlStateManager.depthMask(true);
                    }

                    switch (i)
                    {
                        case 0:
                        default:
                            this.renderer.bindTexture(PARTICLE_TEXTURES);
                            break;
                        case 1:
                            this.renderer.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
                    }

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    Tessellator tessellator = Tessellator.getInstance();
                    VertexBuffer vertexbuffer = tessellator.getBuffer();
                    vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

                    for (final Particle particle : this.fxLayers[i][j])
                    {
                        try
                        {
                            particle.renderParticle(vertexbuffer, entityIn, partialTicks, f, f4, f1, f2, f3);
                        }
                        catch (Throwable throwable)
                        {
                            CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                            CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                            crashreportcategory.setDetail("Particle", new ICrashReportDetail<String>()
                            {
                                public String call() throws Exception
                                {
                                    return particle.toString();
                                }
                            });
                            crashreportcategory.setDetail("Particle Type", new ICrashReportDetail<String>()
                            {
                                public String call() throws Exception
                                {
                                    return i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i));
                                }
                            });
                            throw new ReportedException(crashreport);
                        }
                    }

                    tessellator.draw();
                }
            }
        }
        
        //restore original mipmap state
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mip_min);
        GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mip_mag);
        
        GlStateManager.enableCull();

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
    }

    public void renderLitParticles(Entity entityIn, float partialTick)
    {
        float f = 0.017453292F;
        float f1 = MathHelper.cos(entityIn.rotationYaw * 0.017453292F);
        float f2 = MathHelper.sin(entityIn.rotationYaw * 0.017453292F);
        float f3 = -f2 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
        float f4 = f1 * MathHelper.sin(entityIn.rotationPitch * 0.017453292F);
        float f5 = MathHelper.cos(entityIn.rotationPitch * 0.017453292F);

        for (int i = 0; i < 2; ++i)
        {
            Queue<Particle> queue = this.fxLayers[3][i];

            if (!queue.isEmpty())
            {
                Tessellator tessellator = Tessellator.getInstance();
                VertexBuffer vertexbuffer = tessellator.getBuffer();

                for (Particle particle : queue)
                {
                    particle.renderParticle(vertexbuffer, entityIn, partialTick, f1, f5, f2, f3, f4);
                }
            }
        }
    }

    public void clearEffects(@Nullable World worldIn)
    {
        this.worldObj = worldIn;

        for (int i = 0; i < 4; ++i)
        {
            for (int j = 0; j < 2; ++j)
            {
                this.fxLayers[i][j].clear();
            }
        }

        this.particleEmitters.clear();
    }
    
    public String getStatistics()
    {
    	int count = 0;
    	/*for (int i = 0; i < layers; i++) {
    		count += fxLayers[i].size();
    	}*/
    	//item sheet seems only one used now
        return "" + count;
    }
}