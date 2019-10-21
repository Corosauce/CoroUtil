package CoroUtil.bt.actions;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.LivingEntity;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.leaf.LeafAction;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.Vec3;
import net.minecraft.entity.MobEntity;

public class ActionMoveToCoords extends LeafAction {

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
	
	public ActionMoveToCoords(Behavior parParent, IBTAgent parEnt, BlockCoord[] parCoordsRef, int parCloseDist, boolean parIgnoreY, boolean parHelpMonitor) {
		super(parParent);
		ent = parEnt;
		coordsRef = parCoordsRef;
		closeDist = parCloseDist;
		ignoreY = parIgnoreY;
		helpMonitor = parHelpMonitor;
	}
	
	@Override
	public EnumBehaviorState tick() {
		if (coordsRef[0] != null) {
			
			double dist;
			
			LivingEntity entL = ((LivingEntity)ent);
			
			if (ignoreY) {
				dist = ((LivingEntity)ent).getDistance(coordsRef[0].posX, ((LivingEntity)ent).posY, coordsRef[0].posZ);
			} else {
				dist = ((LivingEntity)ent).getDistance(coordsRef[0].posX, coordsRef[0].posY, coordsRef[0].posZ);
			}
			
			//closeDist = 10;
			
			//dbg("moveto dist: " + dist);
			if (dist < closeDist) {
				
				noMoveTicks = 0;
				//keep in mind, having this set to clear really broke the ai when job hunt is firing....
				//((EntityLivingBase)ent).getNavigator().clearPathEntity();
				return EnumBehaviorState.SUCCESS;
			} else {
				if (((MobEntity)ent).getNavigator().noPath() && ((MobEntity)ent).world.getDayTime() % 20 == 0) {
					//dbg("moveto trying to set path, cur dist: " + dist);
					//dbg("moveto: " + coordsRef[0].posX + ", " + coordsRef[0].posY + ", " + coordsRef[0].posZ + " - " + (int)dist);
					//TODO: 1.10 fix, used to use ICoroAI for some reason despite being a BT class
					//ent.getAIBTAgent().moveTo(coordsRef[0]);
					noMoveTicks = 0;
				}
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
								return EnumBehaviorState.FAILURE;
							}
						}
					}
					noMoveTicksLastPos = new Vec3(entL.posX, entL.posY, entL.posZ);
				}
				
				return EnumBehaviorState.RUNNING;
			}
		} else {
			//dbg("null moveto coords");
			return EnumBehaviorState.FAILURE;
		}
	}

}

