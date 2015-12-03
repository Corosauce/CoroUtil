package build;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class BuildClientProxy extends BuildCommonProxy
{

    public BuildClientProxy()
    {
    	
    }

    @Override
    public void init(BuildMod pMod)
    {
        super.init(pMod);
        //TickRegistry.registerTickHandler(new BuildClientTicks(), Side.CLIENT);
    }
    
    @Override
    public void registerRenderInformation()
    {
    	
    }

    @Override
    public void registerTileEntitySpecialRenderer()
    {
    	
    }
}
