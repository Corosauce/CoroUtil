package extendedrenderer.render;

import java.nio.FloatBuffer;
import java.util.*;

import javax.annotation.Nullable;

import CoroUtil.config.ConfigCoroAI;
import extendedrenderer.EventHandler;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.shadertest.Renderer;
import extendedrenderer.shadertest.gametest.Main;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEmitter;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GLAllocation;
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

@SideOnly(Side.CLIENT)
public class RotatingParticleManager
{
    private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
    /** Reference to the World object. */
    protected World worldObj;
    /**
     * Second dimension: 0 = GlStateManager.depthMask true aka transparent textures, 1 = false
     */
    public final List<ArrayDeque<Particle>[][]> fxLayers = new ArrayList<>();
    private final Queue<ParticleEmitter> particleEmitters = Queues.<ParticleEmitter>newArrayDeque();
    private final TextureManager renderer;
    private final Map<Integer, IParticleFactory> particleTypes = Maps.<Integer, IParticleFactory>newHashMap();
    private final Queue<Particle> queueEntityFX = Queues.<Particle>newArrayDeque();
    
    //ExtendedRenderer Additions
    
    private final FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);

    //a hack to enable fog for particles when weather2 sandstorm is active
    public static float sandstormFogAmount = 0F;

    public static int debugParticleRenderCount;

    public RotatingParticleManager(World worldIn, TextureManager rendererIn)
    {
        this.worldObj = worldIn;
        this.renderer = rendererIn;

        //main default layer
        fxLayers.add(0, new ArrayDeque[4][]);

        //layer for tornado funnel
        fxLayers.add(1, new ArrayDeque[4][]);

        for (ArrayDeque<Particle>[][] entry : fxLayers) {
            for (int i = 0; i < 4; ++i)
            {
                entry[i] = new ArrayDeque[2];

                for (int j = 0; j < 2; ++j)
                {
                    entry[i][j] = Queues.newArrayDeque();
                }
            }
        }

        /*shaderTest = new Renderer();
        try {
            shaderTest.init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/


        //this.registerVanillaParticles();
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

                int renderOrder = 0;
                if (particle instanceof EntityRotFX) {
                    renderOrder = ((EntityRotFX) particle).renderOrder;
                }

                ArrayDeque<Particle>[][] entry = fxLayers.get(renderOrder);

                if (entry[j][k].size() >= 16384) {
                    entry[j][k].removeFirst();
                }

                entry[j][k].add(particle);

                //for (ArrayDeque<Particle>[][] entry : fxLayers) {

                //}
            }
        }
    }

    private void updateEffectLayer(int layer)
    {
        //this.worldObj.theProfiler.startSection(layer + "");

        for (int i = 0; i < 2; ++i)
        {
            //this.worldObj.theProfiler.startSection(i + "");
            for (ArrayDeque<Particle>[][] entry : fxLayers) {
                this.tickParticleList(entry[layer][i]);
            }
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

        int mip_min = 0;
        int mip_mag = 0;

        //fix mipmapping making low alpha transparency particles dissapear based on distance, window size, particle size
        if (!ConfigCoroAI.disableMipmapFix) {
            mip_min = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
            mip_mag = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
        
        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer er = mc.entityRenderer;
        
        //TODO: requires AT for EntityRenderer
        boolean testGLUOverride = false;
        if (testGLUOverride) {
	        /*GlStateManager.matrixMode(5889);
	        GlStateManager.loadIdentity();
	        Project.gluPerspective(er.getFOVModifier(partialTicks, true), (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, er.farPlaneDistance * 4.0F);
	        GlStateManager.matrixMode(5888);*/
        }
        
        boolean fog = true;
        if (fog) {
        	boolean ATmode = true;
        	
        	//TODO: make match other fog states
        	
        	if (ATmode) {
        		//TODO: add AT if this will be used

                er.setupFog(0, partialTicks);

                float fogScaleInvert = 1F - sandstormFogAmount;

                //customized
                //GlStateManager.setFogDensity(0F);
                GlStateManager.setFogStart(0F);
                GlStateManager.setFogEnd(Math.max(40F, 1000F * fogScaleInvert));
                //GlStateManager.setFogEnd(30F);
                /**/
        	} else {
        		//incomplete copy
	        	float fogColorRed = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175080_Q");
	        	float fogColorGreen = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175082_R");
	        	float fogColorBlue = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175081_S");
	        	GlStateManager.glFog(2918, this.setFogColorBuffer(fogColorRed, fogColorGreen, fogColorBlue, 1.0F));
	            GlStateManager.glNormal3f(0.0F, -1.0F, 0.0F);
	            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	            
	            Entity entity = mc.getRenderViewEntity();
	            IBlockState iblockstate = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.theWorld, entity, partialTicks);
	            /*float hook = net.minecraftforge.client.ForgeHooksClient.getFogDensity(er, entity, iblockstate, partialTicks, 0.1F);
	            if (hook >= 0) GlStateManager.setFogDensity(hook);*/
	            
	            GlStateManager.setFogDensity(1F);
	            
	            GlStateManager.enableColorMaterial();
	            GlStateManager.enableFog();
	            GlStateManager.colorMaterial(1028, 4608);
        	}
            
            /*GlStateManager.setFogStart(0);
            GlStateManager.setFogEnd(100);*/
        }

        //ArrayDeque<Particle>[][] entry = fxLayers.get(1);

        debugParticleRenderCount = 0;

        for (ArrayDeque<Particle>[][] entry : fxLayers) {
            for (int i_nf = 0; i_nf < 3; ++i_nf) {
                final int i = i_nf;

                for (int j = 0; j < 2; ++j) {
                    if (!entry[i][j].isEmpty()) {
                        switch (j) {
                            case 0:
                                GlStateManager.depthMask(false);
                                break;
                            case 1:
                                GlStateManager.depthMask(true);
                        }

                        switch (i) {
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

                        for (final Particle particle : entry[i][j]) {
                            //try {
                                particle.renderParticle(vertexbuffer, entityIn, partialTicks, f, f4, f1, f2, f3);
                                debugParticleRenderCount++;
                            /*} catch (Throwable throwable) {
                                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Rendering Particle");
                                CrashReportCategory crashreportcategory = crashreport.makeCategory("Particle being rendered");
                                crashreportcategory.setDetail("Particle", new ICrashReportDetail<String>() {
                                    public String call() throws Exception {
                                        return particle.toString();
                                    }
                                });
                                crashreportcategory.setDetail("Particle Type", new ICrashReportDetail<String>() {
                                    public String call() throws Exception {
                                        return i == 0 ? "MISC_TEXTURE" : (i == 1 ? "TERRAIN_TEXTURE" : (i == 3 ? "ENTITY_PARTICLE_TEXTURE" : "Unknown - " + i));
                                    }
                                });
                                throw new ReportedException(crashreport);
                            }*/
                        }

                        tessellator.draw();
                    }
                }
            }
        }

        //System.out.println("debugParticleRenderCount: " + debugParticleRenderCount);
        
        if (fog) {
        	GlStateManager.disableFog();
        }
        
        //restore original mipmap state
        if (!ConfigCoroAI.disableMipmapFix) {
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mip_min);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mip_mag);
        }
        
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

        for (ArrayDeque<Particle>[][] entry : fxLayers) {
            for (int i = 0; i < 2; ++i) {
                Queue<Particle> queue = entry[3][i];

                if (!queue.isEmpty()) {
                    Tessellator tessellator = Tessellator.getInstance();
                    VertexBuffer vertexbuffer = tessellator.getBuffer();

                    for (Particle particle : queue) {
                        particle.renderParticle(vertexbuffer, entityIn, partialTick, f1, f5, f2, f3, f4);
                    }
                }
            }
        }
    }

    public void clearEffects(@Nullable World worldIn)
    {
        this.worldObj = worldIn;

        for (ArrayDeque<Particle>[][] entry : fxLayers) {
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 2; ++j) {
                    entry[i][j].clear();
                }
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
    
    private FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha)
    {
        this.fogColorBuffer.clear();
        this.fogColorBuffer.put(red).put(green).put(blue).put(alpha);
        this.fogColorBuffer.flip();
        return this.fogColorBuffer;
    }
}