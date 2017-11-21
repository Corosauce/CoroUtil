package extendedrenderer;

import CoroUtil.config.ConfigCoroAI;
import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.render.FoliageRenderer;
import extendedrenderer.render.RotatingParticleManager;
import extendedrenderer.shader.ShaderEngine;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.particle.ParticleRegistry;
import CoroUtil.forge.CoroUtil;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;

public class EventHandler {

	
	//public long lastWorldTime;
    public static World lastWorld;
    //public static Renderer shaderTest;

    public static int mip_min = 0;
    public static int mip_mag = 0;

    private static final FloatBuffer fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);


    //a hack to enable fog for particles when weather2 sandstorm is active
    public static float sandstormFogAmount = 0F;

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

                    if (mc.world.getTotalWorldTime() % 60 == 0) {
                        CoroUtilBlockLightCache.clear();
                    }
                }
                //if (mc.theWorld.getTotalWorldTime() != lastWorldTime) {
                    //lastWorldTime = mc.theWorld.getTotalWorldTime();


                //}
            }
        }
    }

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {


        Minecraft mc = Minecraft.getMinecraft();

        //EventHandler.hookRenderShaders(event.getPartialTicks());

        /*er.enableLightmap();
        RenderHelper.disableStandardItemLighting();

        er.disableLightmap();*/

        
        //old code call
        //ExtendedRenderer.rotEffRenderer.renderParticles((Entity)mc.getRenderViewEntity(), (float)event.getPartialTicks());
    }

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
                ShaderEngine.renderer = null;
                ExtendedRenderer.foliageRenderer.needsUpdate = true;
                ShaderManager.resetCheck();
            }

            if (RotatingParticleManager.useShaders && ShaderEngine.renderer == null) {
                //currently for if shader compiling fails, which is an ongoing issue for some machines...
                if (!ShaderEngine.init()) {
                    ShaderManager.disableShaders();
                    RotatingParticleManager.useShaders = false;
                } else {
                    System.out.println("Extended Renderer: Initialized instanced rendering shaders");
                }
            }

            preShaderRender(mc.getRenderViewEntity(), partialTicks);

            ExtendedRenderer.foliageRenderer.render(mc.getRenderViewEntity(), partialTicks);

            ExtendedRenderer.rotEffRenderer.renderParticles(mc.getRenderViewEntity(), partialTicks);



            postShaderRender(mc.getRenderViewEntity(), partialTicks);

            er.disableLightmap();
        }
    }

    public static void preShaderRender(Entity entityIn, float partialTicks) {

        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer er = mc.entityRenderer;

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        //GlStateManager.blendFunc(GlStateManager.SourceFactor.DST_ALPHA, GlStateManager.DestFactor.ONE_MINUS_DST_ALPHA);
        GlStateManager.alphaFunc(516, 0.003921569F);
        //GlStateManager.alphaFunc(GL11.GL_LESS, 0.2F);
        //GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.0F);

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
            } else {
                //incomplete copy
                float fogColorRed = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175080_Q");
                float fogColorGreen = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175082_R");
                float fogColorBlue = ObfuscationReflectionHelper.getPrivateValue(EntityRenderer.class, Minecraft.getMinecraft().entityRenderer, "field_175081_S");
                GlStateManager.glFog(GL11.GL_FOG_COLOR, setFogColorBuffer(fogColorRed, fogColorGreen, fogColorBlue, 1.0F));
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

        GlStateManager.disableCull();

        CoroUtilBlockLightCache.brightnessPlayer = CoroUtilBlockLightCache.getBrightnessNonLightmap(mc.world, (float)entityIn.posX, (float)entityIn.posY, (float)entityIn.posZ);

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

    private static FloatBuffer setFogColorBuffer(float red, float green, float blue, float alpha)
    {
        fogColorBuffer.clear();
        fogColorBuffer.put(red).put(green).put(blue).put(alpha);
        fogColorBuffer.flip();
        return fogColorBuffer;
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
}
