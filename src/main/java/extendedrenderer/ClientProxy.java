package extendedrenderer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.particle.entity.EntityRotFX;
import extendedrenderer.particle.entity.EntityTexBiomeColorFX;
import extendedrenderer.particle.entity.EntityTexFX;
import extendedrenderer.render.RenderNull;
import extendedrenderer.render.RotatingEffectRenderer;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    public static Minecraft mc;

    public ClientProxy()
    {
        mc = FMLClientHandler.instance().getClient();
    }
    
    @Override
    public void preInit()
    {
    	super.preInit();
    }
    
    @Override
    public void postInit()
    {
    	super.postInit();
    	ExtendedRenderer.rotEffRenderer = new RotatingEffectRenderer(mc.theWorld, mc.renderEngine);
    }

    @Override
    public void init()
    {
        super.init();
        
        //rr.registerEntityRenderingHandler(StormCluster.class, new RenderNull());
        RenderingRegistry.registerEntityRenderingHandler(EntityTexFX.class, new RenderNull(Minecraft.getMinecraft().getRenderManager()));
        RenderingRegistry.registerEntityRenderingHandler(EntityTexBiomeColorFX.class, new RenderNull(Minecraft.getMinecraft().getRenderManager()));
        RenderingRegistry.registerEntityRenderingHandler(EntityRotFX.class, new RenderNull(Minecraft.getMinecraft().getRenderManager()));
        //rr.registerEntityRenderingHandler(EntityFallingRainFX.class, new RenderNull());
        //rr.registerEntityRenderingHandler(EntityWaterfallFX.class, new RenderNull());
        //rr.registerEntityRenderingHandler(EntitySnowFX.class, new RenderNull());
    }
    
}
