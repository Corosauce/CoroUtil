package CoroUtil.forge;

import CoroUtil.block.BlockBlank;
import CoroUtil.block.BlockRepairingBlock;
import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.entity.EntityBatSmart;
import CoroUtil.item.ItemRepairingGel;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

@Mod.EventBusSubscriber(modid = CoroUtil.modID)
public class CommonProxy implements IGuiHandler
{
    public World mainWorld;
    private int entityId = 0;

    public CoroUtil mod;

    public static final String item_repairing_gel_name = "item_repairing_gel";
    public static final String block_repairing_name = "repairing_block";

    @GameRegistry.ObjectHolder(CoroUtil.modID + ":" + block_repairing_name)
    public static Block blockRepairingBlock;

    @GameRegistry.ObjectHolder(CoroUtil.modID + ":blank")
    public static Block blockBlank;

    @GameRegistry.ObjectHolder(CoroUtil.modID + ":" + item_repairing_gel_name)
    public static Item itemRepairingGel;

    public CommonProxy()
    {
    }

    public void init(CoroUtil pMod)
    {
        mod = pMod;

        addMapping(EntityBatSmart.class, "bat_smart", 0, 64, 2, true);
    }

	@Override
	public Object getServerGuiElement(int ID, PlayerEntity player, World world,
                                      int x, int y, int z) {
		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, PlayerEntity player, World world,
                                      int x, int y, int z) {
		return null;
	}

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        //used for replacing foliage with blank for shaders
        CoroUtil.proxy.addBlock(event, new BlockBlank(Material.AIR), "blank");
        CoroUtil.proxy.addBlock(event, new BlockRepairingBlock(), TileEntityRepairingBlock.class, block_repairing_name);

    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        CoroUtil.proxy.addItemBlock(event, new BlockItem(blockRepairingBlock).setRegistryName(blockRepairingBlock.getRegistryName()));
        CoroUtil.proxy.addItem(event, new ItemRepairingGel(), item_repairing_gel_name);
    }

    public void postInit() {
        ResourceLocation group = new ResourceLocation(CoroUtil.modID, "hw_invasion");

        /*GameRegistry.addShapedRecipe(new ResourceLocation(CoroUtil.modID, item_repairing_gel_name), group,
                new ItemStack(itemRepairingGel, 1), new Object[] {"X X", "   ", "XXX", 'X', Items.GOLD_INGOT});*/
    }

    public void addBlock(RegistryEvent.Register<Block> event, Block block, Class tEnt, String translationKey) {
        addBlock(event, block, tEnt, translationKey, true);
    }

    public void addBlock(RegistryEvent.Register<Block> event, Block block, Class tEnt, String translationKey, boolean creativeTab) {
        addBlock(event, block, translationKey, creativeTab);
        GameRegistry.registerTileEntity(tEnt, CoroUtil.modID + ":" + translationKey);
    }

    public void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String translationKey) {
        addBlock(event, parBlock, translationKey, true);
    }

    public void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String translationKey, boolean creativeTab) {
        //GameRegistry.registerBlock(parBlock, translationKey);

        parBlock.setUnlocalizedName(getNameUnlocalized(translationKey));
        parBlock.setRegistryName(getNameDomained(translationKey));

        parBlock.setCreativeTab(ItemGroup.MISC);

        if (event != null) {
            event.getRegistry().register(parBlock);
        }
    }

    public void addItemBlock(RegistryEvent.Register<Item> event, Item item) {
        event.getRegistry().register(item);
    }

    public void addItem(RegistryEvent.Register<Item> event, Item item, String name) {
        item.setUnlocalizedName(getNameUnlocalized(name));
        //item.setRegistryName(new ResourceLocation(Weather.modID, name));
        item.setRegistryName(getNameDomained(name));

        item.setCreativeTab(ItemGroup.MISC);

        if (event != null) {
            event.getRegistry().register(item);
        } else {
            //GameRegistry.register(item);
        }

        //registerItemVariantModel(item, name, 0);

        //return item;
    }

    public String getNameUnlocalized(String name) {
        return CoroUtil.modID + "." + name;
    }

    public String getNameDomained(String name) {
        return CoroUtil.modID + ":" + name;
    }

    public void addMapping(Class par0Class, String par1Str, int entityId, int distSync, int tickRateSync, boolean syncMotion) {
        EntityRegistry.registerModEntity(new ResourceLocation(CoroUtil.modID, par1Str), par0Class, par1Str, entityId, CoroUtil.instance, distSync, tickRateSync, syncMotion);
    }
}
