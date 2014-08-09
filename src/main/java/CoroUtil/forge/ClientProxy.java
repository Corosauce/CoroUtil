package CoroUtil.forge;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

    public ClientProxy()
    {
    	
    }

    @Override
    public void init(CoroAI pMod)
    {
        super.init(pMod);
    }
}
