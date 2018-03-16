package CoroUtil.forge;

import CoroUtil.block.BlockRepairingBlock;
import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.blocks.BlockBlank;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;

@Mod.EventBusSubscriber(modid = CoroUtil.modID)
public class CommonProxy implements IGuiHandler
{
    public World mainWorld;
    private int entityId = 0;

    public CoroUtil mod;

    public static Block blockRepairingBlock;

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
        IForgeRegistry<Block> registry = event.getRegistry();

        CoroUtil.proxy.addBlock(blockRepairingBlock = (new BlockRepairingBlock()), TileEntityRepairingBlock.class, "repairing_block");
        registry.register(blockRepairingBlock);

    }

    public void addBlock(Block block, Class tEnt, String unlocalizedName) {
        addBlock(block, tEnt, unlocalizedName, true);
    }

    public void addBlock(Block block, Class tEnt, String unlocalizedName, boolean creativeTab) {
        addBlock(block, unlocalizedName, creativeTab);
        GameRegistry.registerTileEntity(tEnt, unlocalizedName);
    }

    public void addBlock(Block parBlock, String unlocalizedName) {
        addBlock(parBlock, unlocalizedName, true);
    }

    public void addBlock(Block parBlock, String unlocalizedName, boolean creativeTab) {
        //GameRegistry.registerBlock(parBlock, unlocalizedName);

        parBlock.setUnlocalizedName(getNamePrefixed(unlocalizedName));

        parBlock.setCreativeTab(CreativeTabs.MISC);
    }

    public String getNamePrefixed(String name) {
        return CoroUtil.modID + "." + name;
    }
}
