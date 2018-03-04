package CoroUtil.block;

import CoroUtil.difficulty.DynamicDifficulty;
import CoroUtil.util.BlockCoord;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.ISimulationTickable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class TileEntityRepairingBlock extends TileEntity implements ITickable
{

    private IBlockState orig_blockState;

    private int ticksRepairCount;
    private int ticksRepairMax = 20*20;

	@Override
    public void update()
    {
    	if (!world.isRemote) {

            //if for some reason data is invalid, remove block
            if (orig_blockState == null || orig_blockState == this.getBlockType().getDefaultState()) {
                getWorld().setBlockState(this.getPos(), Blocks.AIR.getDefaultState());

                //temp
                //setBlockData(Blocks.STONE.getDefaultState());
            } else {


                //System.out.println("ticksRepairCount = " + ticksRepairCount);

                //AxisAlignedBB aabb = new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());

                //System.out.println(listTest.size());

                if (ticksRepairCount >= ticksRepairMax) {
                    AxisAlignedBB aabb = this.getBlockType().getDefaultState().getBoundingBox(this.getWorld(), this.getPos());
                    aabb = aabb.offset(this.getPos());
                    List<EntityLivingBase> listTest = this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, aabb);
                    if (listTest.size() == 0)
                    {
                        System.out.println("restoring: " + orig_blockState);
                        getWorld().setBlockState(this.getPos(), orig_blockState);
                    }
                } else {
                    ticksRepairCount++;
                }
            }
    	}
    }

    public void setBlockData(IBlockState state) {
        System.out.println(this + " - setting orig block as " + state);
        this.orig_blockState = state;
    }
    
    /*@Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
    	return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 3, getPos().getZ() + 1);
    }*/

    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
        String str = Block.REGISTRY.getNameForObject(this.orig_blockState.getBlock()).toString();
        var1.setString("orig_blockName", str);
        var1.setInteger("orig_blockMeta", this.orig_blockState.getBlock().getMetaFromState(this.orig_blockState));
        var1.setInteger("ticksRepairCount", ticksRepairCount);

        return super.writeToNBT(var1);
    }

    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        ticksRepairCount = var1.getInteger("ticksRepairCount");
        Block block = Block.getBlockFromName(var1.getString("orig_blockName"));
        if (block != null) {
            int meta = var1.getInteger("orig_blockMeta");
            this.orig_blockState = block.getStateFromMeta(meta);
        }
    }

    @Override
    public void invalidate() {
        if (!this.getWorld().isRemote) {

        }
        super.invalidate();
    }
}
