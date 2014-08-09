package CoroUtil.util;

import net.minecraft.block.Block;
import net.minecraft.util.ChunkCoordinates;

public class ChunkCoordinatesBlock extends ChunkCoordinates {

	public Block block = null;
	public int meta = 0;
	
	public ChunkCoordinatesBlock(int par1, int par2, int par3, Block parBlockID)
	{
		this(par1, par2, par3, parBlockID, 0);
	}
	
	public ChunkCoordinatesBlock(int par1, int par2, int par3, Block parBlockID, int parMeta)
    {
        super(par1, par2, par3);
        block = parBlockID;
        meta = parMeta;
    }
	
	public ChunkCoordinatesBlock(ChunkCoordinates par1ChunkCoordinates, Block parBlockID)
	{
		this(par1ChunkCoordinates, parBlockID, 0);
	}

    public ChunkCoordinatesBlock(ChunkCoordinates par1ChunkCoordinates, Block parBlockID, int parMeta)
    {
        super(par1ChunkCoordinates);
        block = parBlockID;
        meta = parMeta;
    }
	
}
