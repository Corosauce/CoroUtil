package CoroAI.componentAI;

import CoroAI.c_IEnhAI;

public interface IAdvPF {

	public boolean canClimbWalls();
	public boolean canClimbLadders();
	public int getDropSize();
	public int overrideBlockPathOffset(ICoroAI ent, int id, int meta, int x, int y, int z);
}
