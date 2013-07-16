package CoroAI.entity;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

import CoroAI.componentAI.AIFakePlayer;

public class ItemTropicalFishingRod extends Item 
{

    public ItemTropicalFishingRod(int var1) {
        super(var1);
        this.setMaxDamage(64);
        this.setMaxStackSize(1);
    }
    
    @Override
	public void registerIcons(IconRegister iconRegistry) {
		this.itemIcon = Item.fishingRod.getIconFromDamage(0);
	}

    public boolean isFull3D() {
        return true;
    }

    public boolean shouldRotateAroundWhenRendering() {
        return true;
    }
    
    public ItemStack onItemRightClick3(ItemStack var1, World var2, AIFakePlayer var3, float speed) {
    	//System.out.println(System.currentTimeMillis() + " - try cast item, fishEntity:" + var3.fishEntity);
        if(var3.fishEntity != null) {
            int var4 = var3.fishEntity.catchFish();
            //var1.damageItem(var4, var3);
            var3.ai.ent.swingItem();
        } else {
            var2.playSoundAtEntity(var3.ai.ent, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if(!var2.isRemote) {
                var2.spawnEntityInWorld(new EntityTropicalFishHook(var2, var3.ai.ent, speed));
                
            }

            var3.ai.ent.swingItem();
        }

        return var1;
    }
    
    public ItemStack onItemRightClick2(ItemStack var1, World var2, c_PlayerProxy var3, float speed) {
    	//System.out.println(System.currentTimeMillis() + " - try cast item, fishEntity:" + var3.fishEntity);
        if(var3.fishEntity != null) {
            int var4 = var3.fishEntity.catchFish();
            //var1.damageItem(var4, var3);
            var3.swingItem();
        } else {
            var2.playSoundAtEntity(var3, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if(!var2.isRemote) {
                var2.spawnEntityInWorld(new EntityTropicalFishHook(var2, var3, speed));
            }

            var3.swingItem();
        }

        return var1;
    }

    public ItemStack onItemRightClick(ItemStack var1, World var2, EntityPlayer var3) {
        if(var3.fishEntity != null) {
            int var4 = var3.fishEntity.catchFish();
            var1.damageItem(var4, var3);
            var3.swingItem();
        } else {
            var2.playSoundAtEntity(var3, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            if(!var2.isRemote) {
                var2.spawnEntityInWorld(new EntityTropicalFishHook(var2, var3, 2F));
            }

            var3.swingItem();
        }

        return var1;
    }

}
