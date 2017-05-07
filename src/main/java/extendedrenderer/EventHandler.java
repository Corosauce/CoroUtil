package extendedrenderer;

import java.nio.FloatBuffer;

import CoroUtil.config.ConfigCoroAI;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.particle.ParticleRegistry;
import CoroUtil.forge.CoroUtil;
import extendedrenderer.shadertest.Renderer;

public class EventHandler {

	
	public long lastWorldTime;
    public World lastWorld;

    public static Renderer shaderTest;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void tickRenderScreen(TickEvent.RenderTickEvent event) {
        /*if (event.phase == TickEvent.Phase.END) {
            if (shaderTest == null) {
                shaderTest = new Renderer();
                try {
                    shaderTest.init();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            shaderTest.render(null);
        }*/
    }

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public void worldRender(RenderWorldLastEvent event)
    {



		Minecraft mc = Minecraft.getMinecraft();

        //update world reference and clear old effects on world change or on no world
        if (lastWorld != mc.theWorld) {
            CoroUtil.dbg("CoroUtil: resetting rotating particle renderer");
            ExtendedRenderer.rotEffRenderer.clearEffects(mc.theWorld);
            lastWorld = mc.theWorld;
        }

		if (mc.theWorld != null) {

            if (mc.theWorld.getTotalWorldTime() != lastWorldTime) {
                lastWorldTime = mc.theWorld.getTotalWorldTime();

                if (!isPaused()) {
                    ExtendedRenderer.rotEffRenderer.updateEffects();
                }
            }
        }

        if (ConfigCoroAI.disableParticleRenderer) return;

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
        //GlStateManager.matrixMode(5889);
        //GlStateManager.loadIdentity();
        //Project.gluPerspective(90F/*er.getFOVModifier((float)event.getPartialTicks(), true)*/, (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, (float)(mc.gameSettings.renderDistanceChunks * 16) * MathHelper.SQRT_2 * 5);
        //GlStateManager.matrixMode(5888);
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
