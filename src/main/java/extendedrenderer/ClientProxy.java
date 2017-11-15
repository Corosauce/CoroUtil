package extendedrenderer;

import extendedrenderer.render.FoliageRenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import extendedrenderer.render.RotatingParticleManager;

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
    	ExtendedRenderer.rotEffRenderer = new RotatingParticleManager(mc.world, mc.renderEngine);
        ExtendedRenderer.foliageRenderer = new FoliageRenderer(mc.renderEngine);
    }

    @Override
    public void init()
    {
        super.init();
        
        //rr.registerEntityRenderingHandler(StormCluster.class, new RenderNull());
        //rr.registerEntityRenderingHandler(EntityFallingRainFX.class, new RenderNull());
        //rr.registerEntityRenderingHandler(EntityWaterfallFX.class, new RenderNull());
        //rr.registerEntityRenderingHandler(EntitySnowFX.class, new RenderNull());
        
        //TODO: confirm not needed for 1.10.2 since we cant add to weathereffects anymore
        /*RenderingRegistry.registerEntityRenderingHandler(EntityTexFX.class, new RenderNull(Minecraft.getMinecraft().getRenderManager()));
        RenderingRegistry.registerEntityRenderingHandler(EntityTexBiomeColorFX.class, new RenderNull(Minecraft.getMinecraft().getRenderManager()));
        RenderingRegistry.registerEntityRenderingHandler(EntityRotFX.class, new RenderNull(Minecraft.getMinecraft().getRenderManager()));*/
    }
    
}
