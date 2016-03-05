package CoroUtil.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;

public class BlockCoord extends ChunkCoordinates {

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
    
    public BlockCoord(Entity ent) {
    	
    	this.posX = MathHelper.floor_double(ent.posX);
    	this.posY = MathHelper.floor_double(ent.posY);
    	this.posZ = MathHelper.floor_double(ent.posZ);
    	
    }
    
    public boolean equals(Object p_equals_1_)
    {
        if (!(p_equals_1_ instanceof BlockCoord))
        {
            return false;
        }
        else
        {
        	BlockCoord chunkcoordinates = (BlockCoord)p_equals_1_;
            return this.posX == chunkcoordinates.posX && this.posY == chunkcoordinates.posY && this.posZ == chunkcoordinates.posZ;
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
