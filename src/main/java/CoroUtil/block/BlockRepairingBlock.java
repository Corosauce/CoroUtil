package CoroUtil.block;

import CoroUtil.item.ItemRepairingGel;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.block.BlockRenderType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.FMLCommonHandler;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockRepairingBlock extends ContainerBlock
{
	public static final AxisAlignedBB AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    public static final AxisAlignedBB NO_COLLIDE_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
	
    public BlockRepairingBlock()
    {
        super(Material.GROUND);

        /**
         * To make water not flow into the block, a material with blocksMovement being true is required, its the only option in BlockDynamicLiquid.isBlocked(...)
         * - to make sure pathing still works, we override isPassable and return true
         */

        //stone, fallback default
        setHardness(1.5F);
        //this.setTickRandomly(true);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
        if (FMLCommonHandler.instance().getEffectiveSide() == Dist.CLIENT && source instanceof World) {
            return getSelectedBoundingBox(state, (World)source, pos);
        } else {
            return NO_COLLIDE_AABB;
        }
    }

    @Deprecated
    @OnlyIn(Dist.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(BlockState state, World worldIn, BlockPos pos)
    {

        //special behavior to let block only be selectable to repair if correct context

        if (Minecraft.getInstance().player != null &&
                (/*!Minecraft.getInstance().player.isSneaking() &&*/
                        Minecraft.getInstance().player.getHeldItem(Hand.MAIN_HAND).getItem() instanceof ItemRepairingGel)) {
            return AABB;
        } else {
            return NO_COLLIDE_AABB;
        }
    }

    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    @Override
    public boolean isOpaqueCube(BlockState state)
    {
        return false;
    }

    @Override
    public boolean canCollideCheck(BlockState state, boolean hitIfLiquid)
    {
        return super.canCollideCheck(state, hitIfLiquid);
    }

    @Override
    public boolean isFullCube(BlockState state)
    {
        return false;/*super.isFullCube(state);*/
    }

    @Override
    public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World var1, int meta)
    {
        return new TileEntityRepairingBlock();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public float getBlockHardness(BlockState blockState, World worldIn, BlockPos pos) {
        TileEntity tEnt = worldIn.getTileEntity(pos);

        if (tEnt instanceof TileEntityRepairingBlock) {
            return 0;//((TileEntityRepairingBlock) tEnt).getOrig_hardness();
        } else {
            return super.getBlockHardness(blockState, worldIn, pos);
        }
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
        TileEntity tEnt = world.getTileEntity(pos);

        if (tEnt instanceof TileEntityRepairingBlock) {
            //was preventing explosions from spreading more, so use 0, its not supposed to interfere
            return 0;//((TileEntityRepairingBlock) tEnt).getOrig_explosionResistance();
        } else {
            return super.getExplosionResistance(world, pos, exploder, explosion);
        }

    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        //deny from happening
        //super.onBlockExploded(world, pos, explosion);
    }

    /**
     * Called when a block (not water) wants to be placed into it ???
     *
     * @param worldIn
     * @param pos
     * @return
     */
    @Override
    public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        //System.out.println("isReplaceable called - " + worldIn.getBlockState(pos));
        return true;
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        //System.out.println("canPlaceBlockAt called - " + worldIn.getBlockState(pos));
        /*if (worldIn.getBlockState(pos).getMaterial().isLiquid()) {
            return false;
        }*/
        //return false;
        return super.canPlaceBlockAt(worldIn, pos);
    }

    @Override
    public BlockState getStateForPlacement(World worldIn, BlockPos pos,
                                           Direction facing, float hitX, float hitY, float hitZ, int meta,
                                           LivingEntity placer) {
        //worldIn.scheduleBlockUpdate(pos, this, 20*30, 1);
        return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state, Random rand)
    {
        if (world.isRemote) return;

        TileEntity tEnt = world.getTileEntity(pos);
        if (tEnt instanceof TileEntityRepairingBlock) {
            ((TileEntityRepairingBlock) tEnt).updateScheduledTick();
        }
        world.scheduleBlockUpdate(pos, this, 20*30, 1);
    }

    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, BlockState state) {
        //super.onBlockAdded(worldIn, pos, state);
        worldIn.scheduleBlockUpdate(pos, this, 20*30, 1);
    }

    @Override
    public Item getItemDropped(BlockState state, Random rand, int fortune) {
        return Items.AIR;
    }
}

