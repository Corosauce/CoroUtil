package CoroUtil.forge;

import CoroUtil.blocks.BlockBlank;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = "coroutil")
public class CommonProxy implements IGuiHandler
{
    public World mainWorld;
    private int entityId = 0;

    public CoroUtil mod;

    @GameRegistry.ObjectHolder(CoroUtil.modID + ":blank")
    public static Block blockBlank;

    public CommonProxy()
    {
    }

    public void init(CoroUtil pMod)
    {
        mod = pMod;
    }

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world,
			int x, int y, int z) {
		return null;
	}

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        CoroUtil.proxy.addBlock(event, blockBlank = (new BlockBlank(Material.AIR)), "blank");
    }

    public void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName) {
        addBlock(event, parBlock, unlocalizedName, true);
    }

    public void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName, boolean creativeTab) {
        //vanilla calls
        //GameRegistry.registerBlock(parBlock, unlocalizedName);

        parBlock.setUnlocalizedName(CoroUtil.modID + "." + unlocalizedName);
        parBlock.setRegistryName(/*Weather.modID + ":" + */unlocalizedName);

        if (event != null) {
            event.getRegistry().register(parBlock);
        } else {
            //GameRegistry.register(parBlock);
        }

        //ForgeRegistries.BLOCKS.register(parBlock);
        //LanguageRegistry.addName(parBlock, blockNameBase);
    }
}
