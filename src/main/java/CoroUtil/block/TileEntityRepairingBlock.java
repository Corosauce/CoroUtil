package CoroUtil.block;

import CoroUtil.difficulty.DynamicDifficulty;
import CoroUtil.util.BlockCoord;
import CoroUtil.world.WorldDirector;
import CoroUtil.world.WorldDirectorManager;
import CoroUtil.world.location.ISimulationTickable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class TileEntityRepairingBlock extends TileEntity implements ITickable
{

    private IBlockState orig_blockState;

    private int ticksRepairCount;
    private int ticksRepairMax = 20*20;

	@Override
    public void update()
    {
    	if (!worldObj.isRemote) {

            if (orig_blockState == null || orig_blockState == this.getBlockType().getDefaultState()) {
                getWorld().setBlockState(this.getPos(), Blocks.AIR.getDefaultState());
            } else {

                ticksRepairCount++;
                //System.out.println("ticksRepairCount = " + ticksRepairCount);

                if (ticksRepairCount >= ticksRepairMax) {
                    //if (this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, orig_blockState.getBoundingBox(this.getWorld(), this.getPos())) == null)
                    //{
                        System.out.println("restoring: " + orig_blockState);
                        getWorld().setBlockState(this.getPos(), orig_blockState);
                    //}
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
