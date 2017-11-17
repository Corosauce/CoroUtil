package extendedrenderer.render;

import java.nio.FloatBuffer;
import java.util.*;

import javax.annotation.Nullable;

import CoroUtil.config.ConfigCoroAI;
import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.ExtendedRenderer;
import extendedrenderer.foliage.Foliage;
import extendedrenderer.shader.MeshBufferManagerParticle;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.shader.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEmitter;
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

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;

@SideOnly(Side.CLIENT)
public class RotatingParticleManager
{
    private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
    /** Reference to the World object. */
    protected World world;
    /**
     * Second dimension: 0 = GlStateManager.depthMask false aka transparent textures, 1 = true
     */
    public final LinkedHashMap<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> fxLayers = new LinkedHashMap<>();
    private final Queue<ParticleEmitter> particleEmitters = Queues.<ParticleEmitter>newArrayDeque();
    private final TextureManager renderer;
    private final Map<Integer, IParticleFactory> particleTypes = Maps.<Integer, IParticleFactory>newHashMap();
    private final Queue<Particle> queueEntityFX = Queues.<Particle>newArrayDeque();
    
    //ExtendedRenderer Additions
    
    private final FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);

    //a hack to enable fog for particles when weather2 sandstorm is active
    public static float sandstormFogAmount = 0F;

    public static int debugParticleRenderCount;

    public static int lastAmountToRender;

    public static boolean useShaders;

    public static FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    public static FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    public static boolean forceShaderReset = false;

    //mostly failed idea, particle system needs rework if this is going to have a proper benefit
    private static boolean forceVBO2Update = false;

    public static void markDirtyVBO2() {
        forceVBO2Update = true;
    }

    public RotatingParticleManager(World worldIn, TextureManager rendererIn)
    {
        this.world = worldIn;
        this.renderer = rendererIn;



        /*shaderTest = new Renderer();
        try {
            shaderTest.init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/


        //this.registerVanillaParticles();
    }

    public void initNewArrayData(TextureAtlasSprite sprite) {
        List<ArrayDeque<Particle>[][]> list = new ArrayList<>();

        //main default layer
        list.add(0, new ArrayDeque[4][]);

        //layer for tornado funnel
        list.add(1, new ArrayDeque[4][]);

        //close up stuff like precipitation
        list.add(2, new ArrayDeque[4][]);

        for (ArrayDeque<Particle>[][] entry : list) {
            for (int i = 0; i < 4; ++i)
            {
                entry[i] = new ArrayDeque[2];

                for (int j = 0; j < 2; ++j)
                {
                    entry[i][j] = Queues.newArrayDeque();
                }
            }
        }

        fxLayers.put(sprite, list);
    }

    public void registerParticle(int id, IParticleFactory particleFactory)
    {
        this.particleTypes.put(Integer.valueOf(id), particleFactory);
    }

    public void emitParticleAtEntity(Entity entityIn, EnumParticleTypes particleTypes)
    {
        this.particleEmitters.add(new ParticleEmitter(this.world, entityIn, particleTypes));
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
            Particle particle = iparticlefactory.createParticle(particleId, this.world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed, parameters);

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

            RotatingParticleManager.markDirtyVBO2();
            for (Particle particle = (Particle)this.queueEntityFX.poll(); particle != null; particle = (Particle)this.queueEntityFX.poll())
            {
                int j = particle.getFXLayer();
                int k = particle.shouldDisableDepth() ? 0 : 1;

                int renderOrder = 0;
                if (particle instanceof EntityRotFX) {
                    renderOrder = ((EntityRotFX) particle).renderOrder;
                }

                if (!fxLayers.containsKey(particle.particleTexture)) {
                    initNewArrayData(particle.particleTexture);
                }

                ArrayDeque<Particle>[][] entry = fxLayers.get(particle.particleTexture).get(renderOrder);

                if (entry[j][k].size() >= 16384) {
                    //fix bug of particles not being cleaned up from other lists
                    entry[j][k].getFirst().setExpired();
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
        //this.world.theProfiler.startSection(layer + "");

        for (int i = 0; i < 2; ++i)
        {
            //this.worldObj.theProfiler.startSection(i + "");
            for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
                for (ArrayDeque<Particle>[][] entry2 : entry1.getValue()) {
                    this.tickParticleList(entry2[layer][i]);
                }
            }

            //this.worldObj.theProfiler.endSection();
        }

        //this.world.theProfiler.endSection();
    }

    private void tickParticleList(Queue<Particle> p_187240_1_)
    {
        if (!p_187240_1_.isEmpty())
        {
            Iterator<Particle> iterator = p_187240_1_.iterator();

            while (iterator.hasNext())
            {
                Particle particle = iterator.next();
                this.tickParticle(particle);

                if (!particle.isAlive())
                {
                    iterator.remove();

                    RotatingParticleManager.markDirtyVBO2();
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
            crashreportcategory.addDetail("Rotating Particle", new ICrashReportDetail<String>()
            {
                public String call() throws Exception
                {
                    return particle.toString();
                }
            });
            crashreportcategory.addDetail("Particle Type", new ICrashReportDetail<String>()
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


        //if (true) return;


        float f = ActiveRenderInfo.getRotationX();
        float f1 = ActiveRenderInfo.getRotationZ();
        float f2 = ActiveRenderInfo.getRotationYZ();
        float f3 = ActiveRenderInfo.getRotationXY();
        float f4 = ActiveRenderInfo.getRotationXZ();
        Particle.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
        Particle.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
        Particle.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;
        Particle.cameraViewDir = entityIn.getLook(partialTicks);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        //GlStateManager.blendFunc(GlStateManager.SourceFactor.DST_ALPHA, GlStateManager.DestFactor.ONE_MINUS_DST_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        //GlStateManager.alphaFunc(GL11.GL_LESS, 0.2F);
        //GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);
        


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
	            IBlockState iblockstate = ActiveRenderInfo.getBlockStateAtEntityViewpoint(mc.world, entity, partialTicks);
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

        //GlStateManager.depthMask(false);

        //testing no blending (so far notice no fps change)
        /*GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);*/


        //screen door transparency
        //GL11.glEnable(GL11.GL_POLYGON_STIPPLE);

        /*useShaders = ShaderManager.canUseShadersInstancedRendering();

        if (ConfigCoroAI.forceShadersOff) {
            useShaders = false;
        }

        if (forceShaderReset) {
            forceShaderReset = false;
            ShaderEngine.cleanup();
            ShaderEngine.renderer = null;
        }

        if (useShaders && ShaderEngine.renderer == null) {
            //currently for if shader compiling fails, which is an ongoing issue for some machines...
            if (!ShaderEngine.init()) {
                ShaderManager.disableShaders();
                useShaders = false;
            } else {
                System.out.println("Extended Renderer: Initialized instanced rendering shaders");
            }
        }*/

        //useShaders = false;

        if (useShaders) {
            //temp render ordering setup, last to first
            //background stuff
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_test);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.downfall2);
            //foreground stuff
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.downfall3);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.cloud256_6); //ground splash
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.rain_white_trans);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.rain_white);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.snow);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.leaf);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_1);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_2);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.debris_3);
            MeshBufferManagerParticle.setupMeshForParticleIfMissing(ParticleRegistry.tumbleweed);


            //EventHandler.shaderTest = new extendedrenderer.shadertest.Renderer();
            try {
                //EventHandler.shaderTest.init();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        GlStateManager.disableCull();
        //Main.gameLogic.renderer.render(null, Main.gameLogic.camera, Main.gameLogic.gameItems);

        //if (true) return;

        Transformation transformation = null;
        Matrix4fe viewMatrix = null;

        if (world.getTotalWorldTime() % 20 < 10) {
            //useShaders = false;
        }

        //useShaders = false;

        //useShaders = !useShaders;

        //



        int glCalls = 0;
        int trueRenderCount = 0;
        int bufferSize = 0;
        int particles = 0;

        //if (useShaders) {
            ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("particle");
            transformation = ShaderEngine.renderer.transformation;
            shaderProgram.bind();
            Matrix4fe projectionMatrix = new Matrix4fe();
            FloatBuffer buf = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
            buf.rewind();
            Matrix4fe.get(projectionMatrix, 0, buf);

            //TODO: this interferes with drawing order more and more as you move away from things
            //1 solution might be to make vanilla also use this change, but without changing mc.gameSettings.renderDistanceChunks itself
            //have to be carefull about a way to fix without hurting vanilla performance
            //modify far distance, 4x as far
            boolean distantRendering = false;
            if (distantRendering) {
                float zNear = 0.05F;
                float zFar = (float) (mc.gameSettings.renderDistanceChunks * 16) * 4F;
                projectionMatrix.m22 = ((zFar + zNear) / (zNear - zFar));
                projectionMatrix.m32 = ((zFar + zFar) * zNear / (zNear - zFar));
            }

            //testing determined i can save frames by baking projectionMatrix into modelViewMatrixCamera, might have to revert for more complex shaders
            //further testing its just barely faster, if at all...
            //shaderProgram.setUniform("projectionMatrix", mat);
            //shaderProgram.setUniformEfficient("projectionMatrix", mat, projectionMatrixBuffer);

            boolean alternateCameraCapture = true;
            if (alternateCameraCapture) {
                viewMatrix = new Matrix4fe();
                FloatBuffer buf2 = BufferUtils.createFloatBuffer(16);
                GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf2);
                buf2.rewind();
                Matrix4fe.get(viewMatrix, 0, buf2);
            }

            //return viewMatrix.mulAffine(modelMatrix, modelViewMatrix);
            Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
            //Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(viewMatrix, projectionMatrix);

            shaderProgram.setUniformEfficient("modelViewMatrixCamera", modelViewMatrix, viewMatrixBuffer);

            shaderProgram.setUniform("texture_sampler", 0);

            CoroUtilBlockLightCache.brightnessPlayer = CoroUtilBlockLightCache.getBrightnessNonLightmap(world, (float)entityIn.posX, (float)entityIn.posY, (float)entityIn.posZ);
        //}

        //do sprite/mesh list
        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {

            InstancedMeshParticle mesh = MeshBufferManagerParticle.getMesh(entry1.getKey());

            //if (entry1.getKey() != ParticleRegistry.test_texture && entry1.getKey() != ParticleRegistry.rain_white_trans) continue;

            //TODO: register if missing, maybe relocate this
            if (mesh == null) {
                MeshBufferManagerParticle.setupMeshForParticle(entry1.getKey());
                mesh = MeshBufferManagerParticle.getMesh(entry1.getKey());
            }

            if (mesh != null) {
                //do cloud layer, then funnel layer
                for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                    //do each texture mode, 0 and 1 are the only ones used now
                    for (int i_nf = 0; i_nf < 3; ++i_nf) {
                        final int i = i_nf;

                        //do non depth mask (for transparent ones), then depth mask
                        for (int j = 0; j < 2; ++j) {
                            if (!entry[i][j].isEmpty()) {
                                switch (j) {

                                    /**
                                     * TODO: make sure alpha test toggling doesnt interfere with anything else
                                     * with it on, it speeds up rendering of non transparent particles, does it also allow for full transparent particle pixels?
                                     */

                                    case 0:
                                        GlStateManager.depthMask(false);
                                        //GL11.glDisable(GL11.GL_DEPTH_TEST);
                                        //GL11.glEnable(GL11.GL_DEPTH_TEST);
                                        /*GL11.glEnable(GL11.GL_ALPHA_TEST);
                                        GL11.glEnable(GL11.GL_BLEND);*/
                                        break;
                                    case 1:
                                        GlStateManager.depthMask(true);
                                        //GL11.glEnable(GL11.GL_DEPTH_TEST);
                                        /*GL11.glDisable(GL11.GL_ALPHA_TEST);
                                        GL11.glDisable(GL11.GL_BLEND);*/
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

                                if (useShaders) {

                                    //all VBO VertexAttribArrays must be enabled for so glDrawElementsInstanced can use them, so might as well enable all at same time
                                    mesh.initRender();
                                    mesh.initRenderVBO1();
                                    mesh.initRenderVBO2();

                                    //also resets position
                                    mesh.instanceDataBuffer.clear();
                                    mesh.curBufferPos = 0;
                                    particles = entry[i][j].size();

                                    if (true) {
                                        for (final Particle particle : entry[i][j]) {
                                            if (particle instanceof EntityRotFX) {
                                                EntityRotFX part = (EntityRotFX) particle;

                                                part.updateQuaternion(entityIn);

                                                //CoroUtilMath.rotation(part.rotation, (float)Math.toRadians(-part.rotationPitch), (float)Math.toRadians(-part.rotationYaw), 0);
                                                part.renderParticleForShader(mesh, transformation, viewMatrix, entityIn, partialTicks, f, f4, f1, f2, f3);

                                            }
                                        }

                                        mesh.instanceDataBuffer.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS);
                                        //test
                                        //mesh.instanceDataBuffer.limit(ParticleMeshBufferManager.numInstances * mesh.INSTANCE_SIZE_FLOATS);


                                        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO);
                                        ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

                                        //not working right yet, something not flagging it correctly, only like 10% fps gain atm anyways
                                        //actually, the dynamic render amounts used in TexExtraRender completely breaks the sync
                                        if (true || forceVBO2Update) {
                                            mesh.curBufferPos = 0;

                                            //test
                                            for (final Particle particle : entry[i][j]) {
                                                if (particle instanceof EntityRotFX) {
                                                    EntityRotFX part = (EntityRotFX) particle;

                                                    part.renderParticleForShaderTest(mesh, transformation, viewMatrix, entityIn, partialTicks, f, f4, f1, f2, f3);

                                                }
                                            }
                                        } else {
                                            //System.out.println("skipped render");
                                        }

                                        //TODO: added, unverified
                                        //mesh.instanceDataBufferTest.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS_TEST);

                                        //this is supposed to be used only when needed for proper implementation
                                        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBOTest);
                                        ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferTest, GL_DYNAMIC_DRAW);


                                    }

                                    ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0, mesh.curBufferPos);

                                    glCalls++;
                                    trueRenderCount += mesh.curBufferPos;
                                    //bufferSize = mesh.instanceDataBuffer.capacity();

                                    OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, 0);

                                    mesh.endRenderVBO1();
                                    mesh.endRenderVBO2();
                                    mesh.endRender();
                                } else {
                                    Tessellator tessellator = Tessellator.getInstance();
                                    BufferBuilder vertexbuffer = tessellator.getBuffer();
                                    vertexbuffer.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);

                                    for (final Particle particle : entry[i][j]) {
                                        //try {

                                        if (particle instanceof EntityRotFX) {
                                            EntityRotFX part = (EntityRotFX) particle;
                                            //part.rotationPitch = 0;
                                            //part.rotationYaw = 45;
                                            //part.rotationPitch = 90;
                                            //part.rotationYaw = 0;
                                        }
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
                }
            } else {
                //didnt register all atlas sprites, ok for now
                //System.out.println("MESH NULL, SHOULDNT HAPPEN!");
            }


        }


        forceVBO2Update = false;

        if (useShaders) {
            ShaderEngine.renderer.getShaderProgram("particle").unbind();
        }

        //ExtendedRenderer.foliageRenderer.renderJustShaders(entityIn, partialTicks);

        boolean test = false;
        if (test) {
            shaderProgram = ShaderEngine.renderer.getShaderProgram("foliage");
            //transformation = ShaderEngine.renderer.transformation;
            shaderProgram.bind();

            shaderProgram.setUniformEfficient("modelViewMatrixCamera", modelViewMatrix, viewMatrixBuffer);

            shaderProgram.setUniform("texture_sampler", 0);

            MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.tallgrass);
            InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(ParticleRegistry.tallgrass);

            mesh.initRender();
            mesh.initRenderVBO1();
            mesh.initRenderVBO2();

            //also resets position
            mesh.instanceDataBuffer.clear();
            mesh.instanceDataBufferSeldom.clear();
            mesh.curBufferPos = 0;

            BlockPos pos = entityIn.getPosition();

            List<Foliage> listFoliage = new ArrayList<>();

            Random rand = new Random();
            int range = 10;

            //make obj
            for (int i = 0; i < 100; i++) {
                Foliage foliage = new Foliage();
                int randX = rand.nextInt(range) - range / 2;
                int randY = rand.nextInt(range) - range / 2;
                int randZ = rand.nextInt(range) - range / 2;
                foliage.setPosition(new BlockPos(pos).up(4).add(randX, randY, randZ));
                foliage.rotationYaw = 1;
                foliage.rotationPitch = 1;
                foliage.particleScale = 100;
                listFoliage.add(foliage);
            }

            for (Foliage foliage : listFoliage) {
                foliage.updateQuaternion(entityIn);

                //update vbo1
                foliage.renderForShaderVBO1(mesh, transformation, viewMatrix, entityIn, partialTicks);
            }

            //mesh.instanceDataBuffer.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS);

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO);
            ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

            mesh.curBufferPos = 0;

            for (Foliage foliage : listFoliage) {

                //update vbo2
                foliage.renderForShaderVBO2(mesh, transformation, viewMatrix, entityIn, partialTicks);
            }

            //wasnt used in particle renderer and even crashes it :o
            //mesh.instanceDataBufferSeldom.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS_SELDOM);

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBOSeldom);
            ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferSeldom, GL_DYNAMIC_DRAW);

            ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0, mesh.curBufferPos);

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, 0);

            mesh.endRenderVBO1();
            mesh.endRenderVBO2();
            mesh.endRender();

            ShaderEngine.renderer.getShaderProgram("foliage").unbind();
        }

        if (ConfigCoroAI.debugShaders && world.getTotalWorldTime() % 60 == 0) {
            System.out.println("particles: " + particles);
            System.out.println("debugParticleRenderCount: " + debugParticleRenderCount);
            System.out.println("trueRenderCount: " + trueRenderCount);
            System.out.println("glCalls: " + glCalls);
        }
        
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

        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
            for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                for (int i = 0; i < 2; ++i) {
                    Queue<Particle> queue = entry[3][i];
                    if (!queue.isEmpty()) {
                        Tessellator tessellator = Tessellator.getInstance();
                        BufferBuilder vertexbuffer = tessellator.getBuffer();

                        for (Particle particle : queue) {
                            particle.renderParticle(vertexbuffer, entityIn, partialTick, f1, f5, f2, f3, f4);
                        }
                        }
                    }
            }
        }
    }

    public void clearEffects(@Nullable World worldIn)
    {
        this.world = worldIn;

        //shader way
        for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : fxLayers.entrySet()) {
            for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                for (int i = 0; i < entry.length; i++) {
                    for (int j = 0; j < entry[i].length; j++) {
                        if (entry[i][j] != null) {
                            entry[i][j].clear();
                        }
                    }
                }

            }
        }

        //shader way
        /*for (Map.Entry<TextureAtlasSprite, List<ArrayDeque<Particle>[][]>> entry1 : ExtendedRenderer.rotEffRenderer.fxLayers.entrySet()) {
            for (ArrayDeque<Particle>[][] entry : entry1.getValue()) {
                for (int i = 0; i < entry.length; i++) {
                    for (int j = 0; j < entry[i].length; j++) {
                        if (entry[i][j] != null) {
                            entry[i][j].clear();
                        }
                    }

                }
            }
        }*/

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