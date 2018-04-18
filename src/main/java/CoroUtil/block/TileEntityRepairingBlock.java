package CoroUtil.block;

import CoroUtil.difficulty.DynamicDifficulty;
import CoroUtil.forge.CommonProxy;
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
import net.minecraft.world.World;

import java.util.List;

public class TileEntityRepairingBlock extends TileEntity implements ITickable
{

    private IBlockState orig_blockState;
    private float orig_hardness = 1;
    private float orig_explosionResistance = 1;

    private int ticksRepairCount;
    private int ticksRepairMax = 20*60*5;

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
                        //System.out.println("restoring: " + orig_blockState);
                        getWorld().setBlockState(this.getPos(), orig_blockState);
                    }
                } else {
                    ticksRepairCount++;
                }
            }
    	}
    }

    public void setBlockData(IBlockState state) {
        //System.out.println(this + " - setting orig block as " + state);
        this.orig_blockState = state;
    }

    public IBlockState getOrig_blockState() {
        return orig_blockState;
    }

    /*@Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
    	return new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 3, getPos().getZ() + 1);
    }*/

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound var1)
    {
        String str = Block.REGISTRY.getNameForObject(this.orig_blockState.getBlock()).toString();
        var1.setString("orig_blockName", str);
        var1.setInteger("orig_blockMeta", this.orig_blockState.getBlock().getMetaFromState(this.orig_blockState));
        var1.setInteger("ticksRepairCount", ticksRepairCount);

        var1.setFloat("orig_hardness", orig_hardness);
        var1.setFloat("orig_explosionResistance", orig_explosionResistance);

        return super.writeToNBT(var1);
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        ticksRepairCount = var1.getInteger("ticksRepairCount");
        Block block = Block.getBlockFromName(var1.getString("orig_blockName"));
        if (block != null) {
            int meta = var1.getInteger("orig_blockMeta");
            this.orig_blockState = block.getStateFromMeta(meta);
        }

        orig_hardness = var1.getFloat("orig_hardness");
        orig_explosionResistance = var1.getFloat("orig_explosionResistance");
    }

    @Override
    public void invalidate() {
        if (!this.getWorld().isRemote) {

        }
        super.invalidate();
    }

    /**
     *
     * Some mod blocks might require getting data only while their block is still around, so we get it here and save it rather than on the fly later
     *
     * @param world
     * @param pos
     */
    public static void replaceBlockAndBackup(World world, BlockPos pos) {
        IBlockState oldState = world.getBlockState(pos);
        float oldHardness = oldState.getBlockHardness(world, pos);
        float oldExplosionResistance = 1;
        try {
            oldExplosionResistance = oldState.getBlock().getExplosionResistance(world, pos, null, null);
        } catch (Exception ex) {

        }

        world.setBlockState(pos, CommonProxy.blockRepairingBlock.getDefaultState());
        TileEntity tEnt = world.getTileEntity(pos);
        if (tEnt instanceof TileEntityRepairingBlock) {
            TileEntityRepairingBlock repairing = ((TileEntityRepairingBlock) tEnt);
            repairing.setBlockData(oldState);
            repairing.setOrig_hardness(oldHardness);
            repairing.setOrig_explosionResistance(oldExplosionResistance);
        }
    }

    public float getOrig_hardness() {
        return orig_hardness;
    }

    public void setOrig_hardness(float orig_hardness) {
        this.orig_hardness = orig_hardness;
    }

    public float getOrig_explosionResistance() {
        return orig_explosionResistance;
    }

    public void setOrig_explosionResistance(float orig_explosionResistance) {
        this.orig_explosionResistance = orig_explosionResistance;
    }
}
