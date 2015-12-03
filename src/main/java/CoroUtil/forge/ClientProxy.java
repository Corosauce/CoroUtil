package CoroUtil.forge;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
