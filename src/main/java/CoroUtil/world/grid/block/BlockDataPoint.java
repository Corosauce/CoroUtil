package CoroUtil.world.grid.block;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class BlockDataPoint
{
	public BlockDataGrid grid;
	
	//Mandatory data
    public int xCoord;
    public int yCoord;
    public int zCoord;
    public final int hash;

    //Feature specific data
    
    //runtime instance data
    public float health;
    public float digDamage;
    public long lastTickTimeDig; //uses gettotalworldtime
    /** currently used by desire paths **/
    public long lastTickTimeGrass; //uses gettotalworldtime

    //this is the sub progression stage value used when going between actual block stages
    public float walkedOnAmount;

    public byte creationType;
    
    //static/dependant on source block type data
    public Block blockID; //cache for quick weight type lookup
    public int blockMeta;

    /*
    public float totalPathDistance;
    public float distanceToNext;
    public float distanceToTarget;
    public VecPoint previous;
    public boolean isFirst;*/
    public static byte CREATETYPE_UNKNOWN = 0; //default no marking needed
    public static byte CREATETYPE_EPOCH = 1; //for when npcs or epoch standards place (generation)
    public static byte CREATETYPE_PLAYER = 2; //for when players place
    //public static byte CREATETYPE_RES_UNKNOWN = 0; //default no marking needed
    //public static byte CREATETYPE_RES_EPOCH = 1; //for when generation sets a resource block
    
    //placement type used for:
    //- resource blocks
    //- marking WIP buildings with npc/player
    

    public BlockDataPoint(BlockDataGrid parGrid, int i, int j, int k)
    {
    	grid = parGrid;
        xCoord = i;
        yCoord = j;
        zCoord = k;
        hash = makeHash(i, j, k);
        updateCache();

        //dont waste cpu on this until its actually used
        //health = BlockStaticDataMap.getBlockMaxHealth(blockID);

        //System.out.println("new block data, setting health to " + health);
        //Vec3 vec = Vec3.createVectorHelper(xCoord-centerX, yCoord-centerY, zCoord-centerZ); vec.rotateAroundY((float) Math.toRadians(90));
    }

    public void updateCache()
    {
    	IBlockState state = grid.world.getBlockState(new BlockPos(xCoord, yCoord, zCoord));
    	blockID = state.getBlock();
    	blockMeta = state.getBlock().getMetaFromState(state);
    }

    /**
     * Optimization method, checks if theres any point in keeping this data to disk, if everything is defaults, theres no point and we can remove it from the map
     * @return
     */
    public boolean isRemovable() {
    	/*if (health < BlockStaticDataMap.getBlockMaxHealth(blockID)) {
    		return false;
    	}*/
        if (digDamage > 0F) {
            return false;
        }
    	/** lets count this is keepable data, so that when grass changes stage, we can still scale the rate of growth without a reset
         * **/
        if (lastTickTimeGrass > 0F) {
            return false;
        }
    	if (walkedOnAmount > 0F) {
    		return false;
    	}
    	if (creationType > 0) {
    		return false;
    	}
    	return true;
    }

    public static int makeHash(int i, int j, int k)
    {
        return j & 0xff | (i & 0x7fff) << 8 | (k & 0x7fff) << 24 | (i >= 0 ? 0 : 0x80000000) | (k >= 0 ? 0 : 0x8000);
    }

    public float distanceTo(BlockDataPoint pathpoint)
    {
        float f = pathpoint.xCoord - xCoord;
        float f1 = pathpoint.yCoord - yCoord;
        float f2 = pathpoint.zCoord - zCoord;
        return MathHelper.sqrt(f * f + f1 * f1 + f2 * f2);
    }

    public boolean equals(Object obj)
    {
        if (obj instanceof BlockDataPoint)
        {
            BlockDataPoint pathpoint = (BlockDataPoint)obj;
            return hash == pathpoint.hash && xCoord == pathpoint.xCoord && yCoord == pathpoint.yCoord && zCoord == pathpoint.zCoord;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        return hash;
    }

    public String toString()
    {
        return (new StringBuilder()).append(xCoord).append(", ").append(yCoord).append(", ").append(zCoord).toString();
    }
    
    public void readFromNBT(NBTTagCompound nbt) {
    	
    	blockID = Block.getBlockById(nbt.getInteger("blockID"));
    	blockMeta = nbt.getInteger("blockMeta");
    	
    	health = nbt.getFloat("health");
        digDamage = nbt.getFloat("digDamage");
    	lastTickTimeGrass = nbt.getLong("lastTickTimeGrass");
        lastTickTimeDig = nbt.getLong("lastTickTimeDig");
    	creationType = nbt.getByte("creationType");
    	walkedOnAmount = nbt.getFloat("walkedOnAmount");
    	/*xCoord = nbt.getInteger("xCoord");
    	yCoord = nbt.getInteger("yCoord");  -- read in from init
    	zCoord = nbt.getInteger("zCoord");*/
    }
    
    public NBTTagCompound writeToNBT() {
    	NBTTagCompound nbt = new NBTTagCompound();
    	
    	//TODO: ((int value) & 15) will give you the lower four bits, and ((int value) >> 4) will give you the upper 12 bits 
    	
    	nbt.setInteger("blockID", Block.getIdFromBlock(blockID));
    	nbt.setInteger("blockMeta", blockMeta);
    	
    	nbt.setFloat("health", health);
        nbt.setFloat("digDamage", digDamage);
    	nbt.setLong("lastTickTimeGrass", lastTickTimeGrass);
        nbt.setLong("lastTickTimeDig", lastTickTimeDig);
    	nbt.setByte("creationType", creationType);
    	nbt.setFloat("walkedOnAmount", walkedOnAmount);
    	nbt.setInteger("xCoord", xCoord);
    	nbt.setInteger("yCoord", yCoord);
    	nbt.setInteger("zCoord", zCoord);
    	
    	return nbt;
    }
    
    public void tickUpdate() {
    	long curTickTime = grid.world.getTotalWorldTime();
    	
    	//code that scales based on ticktime diff goes here

    	
    	lastTickTimeGrass = curTickTime;
    }
    
    public void cleanup() {
    	grid = null;
    }
}
