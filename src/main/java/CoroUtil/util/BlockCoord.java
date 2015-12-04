package CoroUtil.util;

public class BlockCoord {
	
	public int posX;
	public int posY;
	public int posZ;

	public BlockCoord(int p_i1354_1_, int p_i1354_2_, int p_i1354_3_)
    {
        this.posX = p_i1354_1_;
        this.posY = p_i1354_2_;
        this.posZ = p_i1354_3_;
    }

    public BlockCoord(BlockCoord p_i1355_1_)
    {
        this.posX = p_i1355_1_.posX;
        this.posY = p_i1355_1_.posY;
        this.posZ = p_i1355_1_.posZ;
    }
    
    public boolean equals(Object p_equals_1_)
    {
        if (!(p_equals_1_ instanceof BlockCoord))
        {
            return false;
        }
        else
        {
        	BlockCoord BlockCoord = (BlockCoord)p_equals_1_;
            return this.posX == BlockCoord.posX && this.posY == BlockCoord.posY && this.posZ == BlockCoord.posZ;
        }
    }

    public int hashCode()
    {
        return this.posX + this.posZ << 8 + this.posY << 16;
    }
    
    public String toString()
    {
        return "Pos{x=" + this.posX + ", y=" + this.posY + ", z=" + this.posZ + '}';
    }
    
    public int getX() {
    	return posX;
    }
    
    public int getY() {
    	return posY;
    }
    
    public int getZ() {
    	return posZ;
    }
	
}
