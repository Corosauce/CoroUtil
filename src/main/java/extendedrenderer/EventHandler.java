package extendedrenderer;

import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.forge.CULog;
import CoroUtil.forge.CoroUtil;
import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.render.RotatingParticleManager;
import extendedrenderer.shader.ShaderEngine;
import extendedrenderer.shader.ShaderListenerRegistry;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.particle.ParticleRegistry;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class EventHandler {

	
	//public long lastWorldTime;
    public static World lastWorld;
    //public static Renderer shaderTest;

    public static int mip_min = 0;
    public static int mip_mag = 0;


    //a hack to enable fog for particles when weather2 sandstorm is active
    public static float sandstormFogAmount = 0F;

    public boolean lastFoliageUse = ConfigCoroUtil.foliageShaders;

    public static boolean flagFoliageUpdate = false;

    public static boolean lastLightningBoltLightState = false;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void tickRenderScreen(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            tickShaderTest();
        }
    }

    public static void tickShaderTest() {

    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void tickClient(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {

            Minecraft mc = Minecraft.getMinecraft();
            if (mc.world != null) {
                if (!isPaused()) {
                    ExtendedRenderer.rotEffRenderer.updateEffects();

                    boolean lightningActive = mc.world.getLastLightningBolt() > 0;

                    if (mc.world.getTotalWorldTime() % 2 == 0 || lightningActive != lastLightningBoltLightState) {
                        CoroUtilBlockLightCache.clear();
                    }

                    lastLightningBoltLightState = lightningActive;
                }
                //if (mc.theWorld.getTotalWorldTime() != lastWorldTime) {
                    //lastWorldTime = mc.theWorld.getTotalWorldTime();


                //}
            }

            if (ConfigCoroUtil.foliageShaders != lastFoliageUse) {
                flagFoliageUpdate = true;
            }

            if (flagFoliageUpdate) {
                CULog.dbg("CoroUtil detected a need to reload resource packs, initiating");
                flagFoliageUpdate = false;
                lastFoliageUse = ConfigCoroUtil.foliageShaders;
                Minecraft.getMinecraft().refreshResources();
            }
        }
    }

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
        if (!ConfigCoroUtil.useEntityRenderHookForShaders) {
            EventHandler.hookRenderShaders(event.getPartialTicks());
        }
    }

    //for test added hook added in EntityRenderer.renderWorldPass, before "if (flag && this.mc.objectMouseOver"
    /*@SubscribeEvent(priority = EventPriority.HIGHEST)
    @SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldBlockEvent event)
    {
        if (!ConfigCoroAI.useEntityRenderHookForShaders) {
            EventHandler.hookRenderShaders(event.getPartialTicks());
        }
    }*/

    public static boolean queryUseOfShaders() {
        RotatingParticleManager.useShaders = ShaderManager.canUseShadersInstancedRendering();

        if (ConfigCoroUtil.forceShadersOff) {
            RotatingParticleManager.useShaders = false;
        }

        return RotatingParticleManager.useShaders;
    }

    @SideOnly(Side.CLIENT)
    public static void hookRenderShaders(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.world == null || mc.player == null) return;

        //update world reference and clear old effects on world change or on no world
        if (lastWorld != mc.world) {
            CULog.log("CoroUtil: resetting rotating particle renderer");
            ExtendedRenderer.rotEffRenderer.clearEffects(mc.world);
            lastWorld = mc.world;
        }

        EntityRenderer er = mc.entityRenderer;

        if (!ConfigCoroUtil.disableParticleRenderer) {

            //Rotating particles hook, copied and adjusted code from ParticleManagers render context in EntityRenderer

            er.enableLightmap();
            mc.mcProfiler.endStartSection("litParticles");
            //particlemanager.renderLitParticles(entity, partialTicks);
            ExtendedRenderer.rotEffRenderer.renderLitParticles((Entity) mc.getRenderViewEntity(), (float) partialTicks);
            RenderHelper.disableStandardItemLighting();
            //private method, cant use.... for now
            //er.setupFog(0, event.getPartialTicks());
            mc.mcProfiler.endStartSection("particles");
            //particlemanager.renderParticles(entity, partialTicks);
            //GlStateManager.matrixMode(5889);
            //GlStateManager.loadIdentity();
            //Project.gluPerspective(90F/*er.getFOVModifier((float)event.getPartialTicks(), true)*/, (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, (float)(mc.gameSettings.renderDistanceChunks * 16) * MathHelper.SQRT_2 * 5);
            //GlStateManager.matrixMode(5888);



            queryUseOfShaders();

            if (RotatingParticleManager.forceShaderReset) {

                CULog.log("Extended Renderer: Resetting shaders");

                RotatingParticleManager.forceShaderReset = false;
                ShaderEngine.cleanup();
                ShaderListenerRegistry.postReset();
                ExtendedRenderer.foliageRenderer.foliage.clear();
                ShaderEngine.renderer = null;
                //ExtendedRenderer.foliageRenderer.needsUpdate = true;
                //ExtendedRenderer.foliageRenderer.vbo2BufferPos = 0;
                ShaderManager.resetCheck();
            }

            if (RotatingParticleManager.useShaders && ShaderEngine.renderer == null) {

                boolean simulateFail = false;

                //currently for if shader compiling fails, which is an ongoing issue for some machines...
                if (!ShaderEngine.init() || simulateFail) {

                    CULog.log("Extended Renderer: Shaders failed to initialize");

                    ShaderManager.disableShaders();
                    RotatingParticleManager.useShaders = false;
                } else {
                    CULog.log("Extended Renderer: Initialized instanced rendering shaders");
                    ShaderListenerRegistry.postInit();
                }
            }

            preShaderRender(mc.getRenderViewEntity(), partialTicks);

            if (ConfigCoroUtil.foliageShaders) {
                ExtendedRenderer.foliageRenderer.render(mc.getRenderViewEntity(), partialTicks);
            }


            ExtendedRenderer.rotEffRenderer.renderParticles(mc.getRenderViewEntity(), partialTicks);


            postShaderRender(mc.getRenderViewEntity(), partialTicks);

            er.disableLightmap();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void preShaderRender(Entity entityIn, float partialTicks) {

        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer er = mc.entityRenderer;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.004F);

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
                //GlStateManager.setFogStart(0F);
                //GlStateManager.setFogEnd(Math.max(40F, 1000F * fogScaleInvert));
                //GlStateManager.setFogEnd(30F);
                /**/
            }

            /*GlStateManager.setFogStart(0);
            GlStateManager.setFogEnd(100);*/
        }

        GlStateManager.disableCull();

        CoroUtilBlockLightCache.brightnessPlayer = CoroUtilBlockLightCache.getBrightnessFromLightmap(mc.world, (float)entityIn.posX, (float)entityIn.posY, (float)entityIn.posZ);



        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mip_min = 0;
        mip_mag = 0;

        //fix mipmapping making low alpha transparency particles dissapear based on distance, window size, particle size
        if (!ConfigCoroUtil.disableMipmapFix) {
            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            //                                  3553                10241
            mip_min = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
            //                                                      10240
            mip_mag = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
            //System.out.println(mip_min + " - " + mip_mag);
            //                                                                                  9728
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void postShaderRender(Entity entityIn, float partialTicks) {

        //restore original mipmap state
        if (!ConfigCoroUtil.disableMipmapFix) {
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mip_min);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mip_mag);
        }

        GlStateManager.enableCull();

        boolean fog = true;
        if (fog) {
            GlStateManager.disableFog();
        }

        GlStateManager.depthMask(false);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
    }
	
	@SideOnly(Side.CLIENT)
    public boolean isPaused() {
        if (FMLClientHandler.instance().getClient().isGamePaused()) return true;
    	//if (FMLClientHandler.instance().getClient().getIntegratedServer() != null && FMLClientHandler.instance().getClient().getIntegratedServer().getServerListeningThread() != null && FMLClientHandler.instance().getClient().getIntegratedServer().getServerListeningThread().isGamePaused()) return true;
    	return false;
    }
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Pre event) {
		ParticleRegistry.init(event);
	}

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void registerIconsPost(TextureStitchEvent.Post event) {
        ParticleRegistry.initPost(event);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void modelBake(ModelBakeEvent event) {

        if (true) return;

        /*Map<ModelResourceLocation, IModel> stateModels = ReflectionHelper.getPrivateValue(ModelLoader.class, event.getModelLoader(), "stateModels");
        for (ModelResourceLocation mrl : event.getModelRegistry().getKeys()) {
            IModel model = stateModels.get(mrl);
        }*/

        IBakedModel blank = event.getModelRegistry().getObject(new ModelResourceLocation("coroutil:blank", "normal"));

        for (ModelResourceLocation res : event.getModelRegistry().getKeys()) {

            System.out.println(res.toString());

            IBakedModel model = event.getModelRegistry().getObject(res);

            String domain = res.getResourceDomain();
            String blockName = res.getResourcePath();
            String variant = res.getVariant();

            if (blockName.equals("wheat")) {

                if (!res.getVariant().equals("inventory")) {


                    //List<BakedQuad> quads = model.getQuads(Blocks.WHEAT.getDefaultState().withProperty(BlockCrops.AGE, 5), null, 0);



                    //System.out.println(quads);

                    /*for (BakedQuad quad : quads) {
                        ReflectionHelper.setPrivateValue(BakedQuad.class, quad, new int[1], "field_178215_a", "vertexData");
                    }*/

                    //System.out.println(quads);

                    event.getModelRegistry().putObject(res, blank);
                }
            }

            if (blockName.equals("tall_grass")) {
                System.out.println(res.toString());

                List<BakedQuad> quads = model.getQuads(Blocks.TALLGRASS.getDefaultState()/*.withProperty(BlockTallGrass.TYPE, BlockTallGrass.EnumType.GRASS)*/, null, 0);
            }


        }
    }
}
