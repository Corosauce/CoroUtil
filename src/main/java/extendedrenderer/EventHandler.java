package extendedrenderer;

import CoroUtil.config.ConfigCoroAI;
import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.render.FoliageRenderer;
import extendedrenderer.render.RotatingParticleManager;
import extendedrenderer.shader.ShaderEngine;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.particle.ParticleRegistry;
import CoroUtil.forge.CoroUtil;

public class EventHandler {

	
	//public long lastWorldTime;
    public World lastWorld;
    //public static Renderer shaderTest;

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
            ExtendedRenderer.rotEffRenderer.renderLitParticles((Entity) mc.getRenderViewEntity(), (float) event.getPartialTicks());
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

            ExtendedRenderer.foliageRenderer.render(mc.getRenderViewEntity(), event.getPartialTicks());

            ExtendedRenderer.rotEffRenderer.renderParticles(mc.getRenderViewEntity(), event.getPartialTicks());

            er.disableLightmap();
        }

        /*er.enableLightmap();
        RenderHelper.disableStandardItemLighting();

        er.disableLightmap();*/

        
        //old code call
        //ExtendedRenderer.rotEffRenderer.renderParticles((Entity)mc.getRenderViewEntity(), (float)event.getPartialTicks());
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
