package CoroUtil.forge;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{

    public ClientProxy()
    {
    	
    }

    @Override
    public void init(CoroUtil pMod)
    {
        super.init(pMod);
        
        ClientCommandHandler.instance.registerCommand(new CommandCoroUtilClient());
    }

    public void registerItemsHook(RegistryEvent.Register<Item> event) {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(CoroUtil.proxy.blockRepairingBlock), 0, new ModelResourceLocation(CoroUtil.modID + ":" + "repairing_block", "inventory"));
    }
}
