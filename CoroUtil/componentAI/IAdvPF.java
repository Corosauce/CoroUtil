package CoroUtil.componentAI;

import net.minecraft.block.Block;


public interface IAdvPF {

	public boolean canClimbWalls();
	public boolean canClimbLadders();
	public int getDropSize();
	public int overrideBlockPathOffset(ICoroAI ent, Block block, int meta, int x, int y, int z);
}
