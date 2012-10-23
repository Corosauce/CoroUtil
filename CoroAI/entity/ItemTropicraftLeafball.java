package CoroAI.entity;

import java.util.Random;

import net.minecraft.src.*;

public class ItemTropicraftLeafball extends Item
{
    public ItemTropicraftLeafball(int i)
    {
        super(i);
        maxStackSize = 16;
    }

    public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityplayer)
    {
        if (!entityplayer.capabilities.isCreativeMode)
        {
            itemstack.stackSize--;
        }
        world.playSoundAtEntity(entityplayer, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        if (!world.isRemote)
        {
            //world.spawnEntityInWorld(new EntityTropicraftLeafball(world, entityplayer));
        }
        return itemstack;
    }
}
