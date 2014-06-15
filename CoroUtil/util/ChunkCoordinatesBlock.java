package CoroUtil.util;

import net.minecraft.util.ChunkCoordinates;

public class ChunkCoordinatesBlock extends ChunkCoordinates {

	public int blockID = 0;
	public int meta = 0;
	
	public ChunkCoordinatesBlock(int par1, int par2, int par3, int parBlockID)
	{
		this(par1, par2, par3, parBlockID, 0);
	}
	
	public ChunkCoordinatesBlock(int par1, int par2, int par3, int parBlockID, int parMeta)
    {
        super(par1, par2, par3);
        blockID = parBlockID;
        meta = parMeta;
    }
	
	public ChunkCoordinatesBlock(ChunkCoordinates par1ChunkCoordinates, int parBlockID)
	{
		this(par1ChunkCoordinates, parBlockID, 0);
	}

    public ChunkCoordinatesBlock(ChunkCoordinates par1ChunkCoordinates, int parBlockID, int parMeta)
    {
        super(par1ChunkCoordinates);
        blockID = parBlockID;
        meta = parMeta;
    }
	
}
