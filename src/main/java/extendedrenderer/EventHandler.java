package extendedrenderer;

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

public class EventHandler {

	
	public long lastWorldTime;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {
		Minecraft mc = Minecraft.getMinecraft();
		
		if (mc.theWorld != null && mc.theWorld.getWorldInfo().getWorldTime() != lastWorldTime)
        {
            lastWorldTime = mc.theWorld.getWorldInfo().getWorldTime();

            if (!isPaused())
            {
                ExtendedRenderer.rotEffRenderer.updateEffects();
            }
        }

        //Rotating particles hook, copied and adjusted code from ParticleManagers render context in EntityRenderer
		EntityRenderer er = mc.entityRenderer;
		er.enableLightmap();
        mc.mcProfiler.endStartSection("litParticles");
        //particlemanager.renderLitParticles(entity, partialTicks);
        ExtendedRenderer.rotEffRenderer.renderLitParticles((Entity)mc.getRenderViewEntity(), (float)event.getPartialTicks());
        RenderHelper.disableStandardItemLighting();
        //private method, cant use.... for now
        //er.setupFog(0, event.getPartialTicks());
        mc.mcProfiler.endStartSection("particles");
        //particlemanager.renderParticles(entity, partialTicks);
        ExtendedRenderer.rotEffRenderer.renderParticles((Entity)mc.getRenderViewEntity(), (float)event.getPartialTicks());
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
