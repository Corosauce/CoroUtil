package CoroUtil.item;

import CoroUtil.block.TileEntityRepairingBlock;
import CoroUtil.util.UtilMining;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRepairingGel extends Item
{

    public ItemRepairingGel()
    {
        this.setMaxDamage(0);
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }

    /**
     * Returns the unlocalized name of this item. This version accepts an ItemStack so different stacks can have
     * different names based on their damage or NBT.
     */
    public String getUnlocalizedName(ItemStack stack)
    {
        int i = stack.getMetadata();
        return super.getUnlocalizedName() + "." + EnumDyeColor.byDyeDamage(i).getUnlocalizedName();
    }

    /**
     * Called when a Block is right-clicked with this Item
     */
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack itemstack = player.getHeldItem(hand);

        if (!player.canPlayerEdit(pos.offset(facing), facing, itemstack))
        {
            return EnumActionResult.FAIL;
        }
        else
        {
            if (!worldIn.isRemote) {
                if (player.isCreative() && player.isSneaking()) {
                    IBlockState state = worldIn.getBlockState(pos);
                    if (UtilMining.canMineBlock(worldIn, pos, state.getBlock())/* &&
                            UtilMining.canConvertToRepairingBlock(worldIn, state)*/) {
                        TileEntityRepairingBlock.replaceBlockAndBackup(worldIn, pos);
                    }
                } else {
                    TileEntity tEnt = worldIn.getTileEntity(pos);
                    if (tEnt instanceof TileEntityRepairingBlock) {

                        ((TileEntityRepairingBlock) tEnt).restoreBlock();

                        if (!player.capabilities.isCreativeMode) {
                            itemstack.shrink(1);
                        }

                        return EnumActionResult.SUCCESS;
                    } else {
                        return EnumActionResult.PASS;
                    }
                }
            }
        }

        return EnumActionResult.PASS;
    }
}