package CoroUtil.bt.selector;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.Vec3;

public class SelectorMoveToCoords extends Selector {

	public IBTAgent ent;
	public BlockCoord[] coordsRef;
	public int closeDist;
	public boolean ignoreY = false;

	public boolean helpMonitor = false;
	public boolean noMoveReset = true;
	public int noMoveTicks = 0;
	public int noMoveTicksMax = 80;
	public double noMoveTicksThreshold = 0.1D;
	public Vec3 noMoveTicksLastPos;
	public boolean useSight = true;

	public SelectorMoveToCoords(Behavior parParent, IBTAgent parEnt, BlockCoord[] parCoordsRef, int parCloseDist, boolean parIgnoreY, boolean parHelpMonitor) {
		this(parParent, parEnt, parCoordsRef, parCloseDist);
		ignoreY = parIgnoreY;
		helpMonitor = parHelpMonitor;
	}

	public SelectorMoveToCoords(Behavior parParent, IBTAgent parEnt, BlockCoord[] parCoordsRef, int parCloseDist) {
		super(parParent);
		ent = parEnt;
		coordsRef = parCoordsRef;
		closeDist = parCloseDist;
	}

	@Override
	public EnumBehaviorState tick() {
		if (coordsRef[0] != null) {

			double dist;

			EntityLivingBase entL = ((EntityLivingBase)ent);

			if (ignoreY) {
				dist = ((EntityLivingBase)ent).getDistance(coordsRef[0].posX, ((EntityLivingBase)ent).posY, coordsRef[0].posZ);
			} else {
				dist = ((EntityLivingBase)ent).getDistance(coordsRef[0].posX, coordsRef[0].posY, coordsRef[0].posZ);
			}

			//closeDist = 10;

			//dbg("moveto dist: " + dist);
			if (dist < closeDist && (!useSight || canBeSeen(new Vec3d(coordsRef[0].posX, coordsRef[0].posY+1.1D, coordsRef[0].posZ)))) {

				noMoveTicks = 0;
				//keep in mind, having this set to clear really broke the ai when job hunt is firing....
				//((EntityLivingBase)ent).getNavigator().clearPathEntity();
				return children.get(1).tick();
			} else {

				//for 5th gen AI, removed nopath check, path timeouts managed internally now

				//if (((EntityLiving)ent).getNavigator().noPath() && ((EntityLiving)ent).world.getWorldTime() % 20 == 0) {
					//dbg("moveto trying to set path, cur dist: " + dist);
					//dbg("moveto: " + coordsRef[0].posX + ", " + coordsRef[0].posY + ", " + coordsRef[0].posZ + " - " + (int)dist);
					//ent.getAIBTAgent().moveTo(coordsRef[0]);
					ent.getAIBTAgent().blackboard.setMoveTo(new Vec3(coordsRef[0].posX, coordsRef[0].posY, coordsRef[0].posZ));
					noMoveTicks = 0;
				//}
				//timeout check go here maybe?

				if (helpMonitor) {
					if (noMoveTicksLastPos != null) {
						double posDiff = entL.getDistance(noMoveTicksLastPos.xCoord, entL.posY, noMoveTicksLastPos.zCoord);
						noMoveTicksThreshold = 0.01D;
						if (posDiff < noMoveTicksThreshold) {
							noMoveTicks++;
							//dbg("noMoveTicks: " + noMoveTicks + " posDiff: " + posDiff);
							if (noMoveTicks > noMoveTicksMax) {
								noMoveTicks = 0;
								dbg("no move ticks max triggered, nulling coords");
								coordsRef[0] = null;
								return children.get(0).tick();
							}
						}
					}
					noMoveTicksLastPos = new Vec3(entL.posX, entL.posY, entL.posZ);
				}

				return EnumBehaviorState.RUNNING;
			}
		} else {
			//dbg("null moveto coords");
			return children.get(0).tick();
		}
	}

	public boolean canBeSeen(Vec3d pos)
    {
		EntityLivingBase entL = ((EntityLivingBase)ent);
        return entL.world.rayTraceBlocks(new Vec3d(entL.posX, entL.posY + (double)entL.getEyeHeight(), entL.posZ), pos) == null;
    }

}
