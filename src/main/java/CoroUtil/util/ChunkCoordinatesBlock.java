package CoroUtil.util;

import net.minecraft.block.Block;

public class BlockCoordBlock extends BlockCoord {

	public Block block = null;
	public int meta = 0;
	
	public BlockCoordBlock(int par1, int par2, int par3, Block parBlockID)
	{
		this(par1, par2, par3, parBlockID, 0);
	}
	
	public BlockCoordBlock(int par1, int par2, int par3, Block parBlockID, int parMeta)
    {
        super(par1, par2, par3);
        block = parBlockID;
        meta = parMeta;
    }
	
	public BlockCoordBlock(BlockCoord par1BlockCoord, Block parBlockID)
	{
		this(par1BlockCoord, parBlockID, 0);
	}

    public BlockCoordBlock(BlockCoord par1BlockCoord, Block parBlockID, int parMeta)
    {
        super(par1BlockCoord);
        block = parBlockID;
        meta = parMeta;
    }
	
}
