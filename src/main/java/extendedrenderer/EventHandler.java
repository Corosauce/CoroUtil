package extendedrenderer;

import CoroUtil.forge.CoroUtil;
import extendedrenderer.particle.ParticleRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EventHandler {


	public long lastWorldTime;
    public World lastWorld;

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

		if (mc.world != null) {

            if (mc.world.getWorldInfo().getWorldTime() != lastWorldTime) {
                lastWorldTime = mc.world.getWorldInfo().getWorldTime();

                if (!isPaused()) {
                    ExtendedRenderer.rotEffRenderer.updateEffects();
                }
            }
        }

        //Rotating particles hook, copied and adjusted code from ParticleManagers render context in EntityRenderer
		EntityRenderer er = mc.entityRenderer;
		er.enableLightmap();
        mc.mcProfiler.endStartSection("litParticles");
        //particlemanager.renderLitParticles(entity, partialTicks);
        ExtendedRenderer.rotEffRenderer.renderLitParticles(mc.getRenderViewEntity(), event.getPartialTicks());
        RenderHelper.disableStandardItemLighting();
        //private method, cant use.... for now
        //er.setupFog(0, event.getPartialTicks());
        mc.mcProfiler.endStartSection("particles");
        //particlemanager.renderParticles(entity, partialTicks);
        //GlStateManager.matrixMode(5889);
        //GlStateManager.loadIdentity();
        //Project.gluPerspective(90F/*er.getFOVModifier((float)event.getPartialTicks(), true)*/, (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, (float)(mc.gameSettings.renderDistanceChunks * 16) * MathHelper.SQRT_2 * 5);
        //GlStateManager.matrixMode(5888);
        ExtendedRenderer.rotEffRenderer.renderParticles(mc.getRenderViewEntity(), event.getPartialTicks());
        er.disableLightmap();

        //old code call
        //ExtendedRenderer.rotEffRenderer.renderParticles((Entity)mc.getRenderViewEntity(), (float)event.getPartialTicks());
    }

	@SideOnly(Side.CLIENT)
    public boolean isPaused() {
    	//if (FMLClientHandler.instance().getClient().getIntegratedServer() != null && FMLClientHandler.instance().getClient().getIntegratedServer().getServerListeningThread() != null && FMLClientHandler.instance().getClient().getIntegratedServer().getServerListeningThread().isGamePaused()) return true;
    	return false;
    }

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureStitchEvent.Pre event) {
		ParticleRegistry.init(event);
	}
}
