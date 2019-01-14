package CoroUtil.forge;

import CoroUtil.block.BlockBlank;
import CoroUtil.entity.EntityBatSmart;
import CoroUtil.entity.render.RenderBatSmart;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderBat;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
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

        addMapping(EntityBatSmart.class, new RenderBatSmart(Minecraft.getMinecraft().getRenderManager()));
    }

    @Override
    public void addItem(RegistryEvent.Register<Item> event, Item item, String name) {
        super.addItem(event, item, name);

        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(CoroUtil.modID + ":" + name, "inventory"));
    }

    @Override
    public void addBlock(RegistryEvent.Register<Block> event, Block parBlock, String unlocalizedName, boolean creativeTab) {
        super.addBlock(event, parBlock, unlocalizedName, creativeTab);

        if (!(parBlock instanceof BlockBlank)) {
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(parBlock), 0, new ModelResourceLocation(CoroUtil.modID + ":" + unlocalizedName, "inventory"));
        }
    }

    @Override
    public void addItemBlock(RegistryEvent.Register<Item> event, Item item) {
        super.addItemBlock(event, item);

        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private static void addMapping(Class<? extends Entity> entityClass, Render render) {
        RenderingRegistry.registerEntityRenderingHandler(entityClass, render);
    }
}
