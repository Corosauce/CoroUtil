package extendedrenderer;

import CoroUtil.config.ConfigCoroAI;
import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.render.FoliageRenderer;
import extendedrenderer.render.RotatingParticleManager;
import extendedrenderer.shader.ShaderEngine;
import extendedrenderer.shader.ShaderListenerRegistry;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.particle.ParticleRegistry;
import CoroUtil.forge.CoroUtil;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class EventHandler {

	
	//public long lastWorldTime;
    public static World lastWorld;
    //public static Renderer shaderTest;

    public static int mip_min = 0;
    public static int mip_mag = 0;


    //a hack to enable fog for particles when weather2 sandstorm is active
    public static float sandstormFogAmount = 0F;

    public boolean lastFoliageUse = ConfigCoroAI.foliageShaders;

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

                    if (mc.world.getTotalWorldTime() % 2 == 0) {
                        CoroUtilBlockLightCache.clear();
                    }
                }
                //if (mc.theWorld.getTotalWorldTime() != lastWorldTime) {
                    //lastWorldTime = mc.theWorld.getTotalWorldTime();


                //}
            }

            if (ConfigCoroAI.foliageShaders != lastFoliageUse) {
                lastFoliageUse = ConfigCoroAI.foliageShaders;
                Minecraft.getMinecraft().refreshResources();
            }
        }
    }

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
        if (!ConfigCoroAI.useEntityRenderHookForShaders) {
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

    @SideOnly(Side.CLIENT)
    public static void hookRenderShaders(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();

        if (mc.world == null || mc.player == null) return;

        //update world reference and clear old effects on world change or on no world
        if (lastWorld != mc.world) {
            CoroUtil.dbg("CoroUtil: resetting rotating particle renderer");
            ExtendedRenderer.rotEffRenderer.clearEffects(mc.world);
            lastWorld = mc.world;
        }

        EntityRenderer er = mc.entityRenderer;

        if (!ConfigCoroAI.disableParticleRenderer) {

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



            RotatingParticleManager.useShaders = ShaderManager.canUseShadersInstancedRendering();

            if (ConfigCoroAI.forceShadersOff) {
                RotatingParticleManager.useShaders = false;
            }

            if (RotatingParticleManager.forceShaderReset) {
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
                //currently for if shader compiling fails, which is an ongoing issue for some machines...
                if (!ShaderEngine.init()) {
                    ShaderManager.disableShaders();
                    RotatingParticleManager.useShaders = false;
                } else {
                    System.out.println("Extended Renderer: Initialized instanced rendering shaders");
                    ShaderListenerRegistry.postInit();
                }
            }

            preShaderRender(mc.getRenderViewEntity(), partialTicks);

            if (ConfigCoroAI.foliageShaders) {
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
        GlStateManager.alphaFunc(516, 0.003921569F);

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
        if (!ConfigCoroAI.disableMipmapFix) {
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
        if (!ConfigCoroAI.disableMipmapFix) {
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mip_min);
            GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mip_mag);
        }

        GlStateManager.enableCull();

        boolean fog = true;
        if (fog) {
            GlStateManager.disableFog();
        }

        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.alphaFunc(516, 0.1F);
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
