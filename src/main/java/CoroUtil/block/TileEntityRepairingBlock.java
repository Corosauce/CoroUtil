package CoroUtil.block;

import CoroUtil.config.ConfigCoroUtil;
import CoroUtil.config.ConfigCoroUtilAdvanced;
import CoroUtil.config.ConfigDynamicDifficulty;
import CoroUtil.forge.CULog;
import CoroUtil.forge.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class TileEntityRepairingBlock extends TileEntity
{

    private IBlockState orig_blockState;
    private float orig_hardness = 1;
    private float orig_explosionResistance = 1;

    /*private int ticksRepairCount;
    private int ticksRepairMax = 20*60*5;*/
    private long timeToRepairAt = 0;

    public void updateScheduledTick()
    {
    	if (!world.isRemote) {

            //if for some reason data is invalid, remove block
            if (orig_blockState == null || orig_blockState == this.getBlockType().getDefaultState()) {
                CULog.dbg("invalid state for repairing block, removing, orig_blockState: " + orig_blockState + " vs " + this.getBlockType().getDefaultState());
                getWorld().setBlockState(this.getPos(), Blocks.AIR.getDefaultState());

                //temp
                //setBlockData(Blocks.STONE.getDefaultState());
            } else {


                //System.out.println("ticksRepairCount = " + ticksRepairCount);

                //AxisAlignedBB aabb = new AxisAlignedBB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX(), this.getPos().getY(), this.getPos().getZ());

                //System.out.println(listTest.size());

                if (world.getTotalWorldTime() > timeToRepairAt || ConfigCoroUtilAdvanced.repairBlockNextRandomTick) {
                    AxisAlignedBB aabb = this.getBlockType().getDefaultState().getBoundingBox(this.getWorld(), this.getPos());
                    //i think its using no collide AABB so this fixes it
                    aabb = Block.FULL_BLOCK_AABB;
                    aabb = aabb.offset(this.getPos());
                    List<EntityLivingBase> listTest = this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, aabb);
                    if (listTest.size() == 0)
                    {
                        //System.out.println("restoring: " + orig_blockState);
                        restoreBlock();
                    }
                }
            }
    	}
    }

    @Override
    public void onLoad() {
        super.onLoad();

        //i dont currently see any clean ways to init the tile entity with the orig_blockState before onLoad is called, so we cant do this here
        /*if (orig_blockState == null || orig_blockState == this.getBlockType().getDefaultState()) {
            getWorld().setBlockState(this.getPos(), Blocks.AIR.getDefaultState());
        }*/
    }

    public void restoreBlock() {
        //CULog.dbg("restoring block to state: " + orig_blockState + " at " + this.getPos());
        getWorld().setBlockState(this.getPos(), orig_blockState);

        //try to untrigger leaf decay for those large trees too far from wood source//also undo it for neighbors around it
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos posFix = pos.add(x, y, z);
                    IBlockState state = world.getBlockState(posFix);
                    if (state.getBlock() instanceof BlockLeaves) {
                        try {
                            //CULog.dbg("restoring leaf to non decay state at pos: " + posFix);
                            world.setBlockState(posFix, state.withProperty(BlockLeaves.CHECK_DECAY, false), 4);
                        } catch (Exception ex) {
                            //must be a modded block that doesnt use decay
                            if (ConfigCoroUtil.useLoggingDebug) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            }
        }


        //getWorld().setBlockState(this.getPos(), Blocks.STONE.getDefaultState());
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
        if (orig_blockState != null) {
            String str = Block.REGISTRY.getNameForObject(this.orig_blockState.getBlock()).toString();
            var1.setString("orig_blockName", str);
            var1.setInteger("orig_blockMeta", this.orig_blockState.getBlock().getMetaFromState(this.orig_blockState));
        }
        var1.setLong("timeToRepairAt", timeToRepairAt);

        var1.setFloat("orig_hardness", orig_hardness);
        var1.setFloat("orig_explosionResistance", orig_explosionResistance);

        return super.writeToNBT(var1);
    }

    @Override
    public void readFromNBT(NBTTagCompound var1)
    {
        super.readFromNBT(var1);
        timeToRepairAt = var1.getLong("timeToRepairAt");
        try {
            Block block = Block.getBlockFromName(var1.getString("orig_blockName"));
            if (block != null) {
                int meta = var1.getInteger("orig_blockMeta");
                this.orig_blockState = block.getStateFromMeta(meta);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            this.orig_blockState = Blocks.AIR.getDefaultState();
        }


        orig_hardness = var1.getFloat("orig_hardness");
        orig_explosionResistance = var1.getFloat("orig_explosionResistance");
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

    public static TileEntityRepairingBlock replaceBlockAndBackup(World world, BlockPos pos) {
        return replaceBlockAndBackup(world, pos, ConfigDynamicDifficulty.ticksToRepairBlock);
    }

    /**
     *
     * Some mod blocks might require getting data only while their block is still around, so we get it here and save it rather than on the fly later
     *
     * @param world
     * @param pos
     */
    public static TileEntityRepairingBlock replaceBlockAndBackup(World world, BlockPos pos, int ticksToRepair) {
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
            IBlockState state = world.getBlockState(pos);
            //CULog.dbg("set repairing block for pos: " + pos + ", " + oldState.getBlock());
            TileEntityRepairingBlock repairing = ((TileEntityRepairingBlock) tEnt);
            repairing.setBlockData(oldState);
            repairing.setOrig_hardness(oldHardness);
            repairing.setOrig_explosionResistance(oldExplosionResistance);
            repairing.timeToRepairAt = world.getTotalWorldTime() + ticksToRepair;
            //world.scheduleBlockUpdate(pos, state.getBlock(), 20*5, 1);
            return (TileEntityRepairingBlock) tEnt;
        } else {
            CULog.dbg("failed to set repairing block for pos: " + pos);
            return null;
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
