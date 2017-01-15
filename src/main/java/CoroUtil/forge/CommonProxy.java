package CoroUtil.forge;

import CoroUtil.block.BlockRepairingBlock;
import CoroUtil.block.TileEntityRepairingBlock;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CommonProxy implements IGuiHandler
{
    public World mainWorld;
    private int entityId = 0;

    public CoroUtil mod;

    public static Block blockRepairingBlock;

    public CommonProxy()
    {

    }

    public void init(CoroUtil pMod)
    {
        mod = pMod;
        addBlock(blockRepairingBlock = (new BlockRepairingBlock()), TileEntityRepairingBlock.class, "repairing_block");
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
        //vanilla calls

		/*parBlock.setRegistryName(new ResourceLocation(Weather.modID, unlocalizedName));*/
        //GameRegistry.register(parBlock);
        GameRegistry.registerBlock(parBlock, unlocalizedName);
		/*parBlock.setBlockName(Weather.modID + ":" + unlocalizedName);
		parBlock.setBlockTextureName(Weather.modID + ":" + unlocalizedName);*/

        parBlock.setUnlocalizedName(getNamePrefixed(unlocalizedName));

        /*if (creativeTab) {
            parBlock.setCreativeTab(tab);
        } else {
            parBlock.setCreativeTab(null);
        }*/

        parBlock.setCreativeTab(CreativeTabs.MISC);
        //LanguageRegistry.addName(parBlock, blockNameBase);
    }

    public String getNamePrefixed(String name) {
        return CoroUtil.modID + "." + name;
    }
}
