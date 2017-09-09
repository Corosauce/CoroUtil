package CoroUtil.bt;

import java.util.ArrayList;

import net.minecraft.entity.Entity;
import net.minecraft.pathfinding.Path;
import net.minecraft.util.math.MathHelper;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;

import CoroUtil.OldUtil;
import CoroUtil.pathfinding.IPFCallback;
import CoroUtil.pathfinding.PFCallbackItem;
import CoroUtil.pathfinding.PFJobData;
import CoroUtil.pathfinding.PFQueue;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.Vec3;

public class BlackboardBase implements IPFCallback {

	public AIBTAgent agent;
	
	//main moveTo, gets copied onto movehelper
	public Vec3 posMoveTo;
	
	public ArrayList<PFCallbackItem> listCallbackPaths = new ArrayList<PFCallbackItem>();
	
	//public Vec3 posMoveToClose;
	//public Vec3 posMoveToPath;
	//public Vec3 posMoveToPathFar;
	
	public Path pathMoveToPath; //instant return data
	public Path pathMoveToPathFar; //threaded return data
	public MutableBoolean isWaitingForPath = new MutableBoolean(false); //set to true when request sent, set to false when callback has path
	public MutableBoolean isPathReceived = new MutableBoolean(false); //set to true when callback has path, set to false when movement bt puts it to use
	
	//public int movementState = 0; //0 = close combat, 1 = close pathing, 2 = far pathing
	
	private int targetID; //network id
	private Entity target; //private so it can detect null and reupdate

	public MutableInt moveCondition = new MutableInt(0);
	
	//these booleans serve as a sort of cache for generic trees

	//Movement
	public MutableBoolean isPathSafe = new MutableBoolean(true);
	public MutableBoolean isLongPath = new MutableBoolean(true);
	public MutableBoolean isClosePath = new MutableBoolean(true);
	public MutableBoolean isSafeOrClosePath = new MutableBoolean(true);
	public MutableBoolean isMoving = new MutableBoolean(true);
	
	//Combat
	public MutableBoolean isFighting = new MutableBoolean(false);
	public MutableBoolean shouldTrySurvival = new MutableBoolean(false);
	public MutableBoolean shouldChaseTarget = new MutableBoolean(false);
	public MutableBoolean shouldWander = new MutableBoolean(false);
	public MutableBoolean isUsingMelee = new MutableBoolean(false);
	public MutableBoolean isUsingRanged = new MutableBoolean(false);
	
	public MutableBoolean shouldFollowOrders = new MutableBoolean(false);
	
	
	public BlockCoord fleeToCoords = new BlockCoord(0, 0, 0);
	public long lastTickSafetyCheck = 0;
	
	public MutableInt distMed = new MutableInt(18);
	public MutableInt distClose = new MutableInt(3);
	
	public Entity lastFleeTarget;
	
	//Tracking timeouts
	public long lastTimeRequestedPFThreaded = 0;
	public long PFThreadedTimeout = 5000;
	
	//testing flying and swimming pathing
	public MutableBoolean canFlyPath = new MutableBoolean(false);
	public MutableBoolean canSwimPath = new MutableBoolean(false);
	public MutableBoolean canFlyStraitToTarget = new MutableBoolean(false);
	
	public BlackboardBase(AIBTAgent parAgent) {
		agent = parAgent;
	}
	
	public Entity getTarget() {
		if (target != null) {
			return target;		
		} else {
			if (targetID != -1) {
				Entity ent = agent.ent.world.getEntityByID(targetID);
				if (ent != null) {
					setTarget(ent);
				}
			}
		}
		return target;
	}
	
	public void setTarget(int parID) {
		Entity ent = agent.ent.world.getEntityByID(parID);
		if (ent != null) {
			setTarget(ent);
		}
	}
	
	public void setTarget(Entity parTarget) {
		target = parTarget;
		if (parTarget != null) {
			targetID = parTarget.getEntityId();
		} else {
			targetID = -1;
		}
	}
	
	public void trackTarget() {
		trackTarget(false);
	}
	
	public void trackTarget(boolean resetPathData) {
		if (target != null) {
			//System.out.println("tracking target");
			setMoveTo(new Vec3(target.posX, target.posY, target.posZ), resetPathData);
		}
	}
	
	//should only be used per tick within close combat range, or when paths are detected old enough to override
	public void setMoveAndPathTo(Vec3 parMoveTo) {
		setMoveTo(parMoveTo, true);
	}
	
	public void setMoveTo(Vec3 parMoveTo) {
		setMoveTo(parMoveTo, false);
	}
	
	public void setMoveTo(Vec3 parMoveTo, boolean resetPathData) {
		if (!agent.tamable.shouldStayStill()) {
			posMoveTo = parMoveTo;
			if (resetPathData) {
				//System.out.println("reset path data");
				pathMoveToPath = null;
				pathMoveToPathFar = null;
				isWaitingForPath.setValue(false);
				isPathReceived.setValue(false);
				//agent.pathNav.clearPathEntity();
			}
		}
	}
	
	public void setPathFar(Path parPath) {
		//System.out.println("callback pf set: " + parPath);
		pathMoveToPathFar = parPath;
		isWaitingForPath.setValue(false);
		isPathReceived.setValue(true);
	}
	
	public void requestPathFar(Vec3 parPos, int pathRange) {
		if (canFlyPath.getValue() || canSwimPath.getValue()) {
			//System.out.println("request TEST FLYING/SWIMMING PATH pf set: " + parPos);
			PFJobData job = new PFJobData(agent.ent, MathHelper.floor(parPos.xCoord), MathHelper.floor(parPos.yCoord), MathHelper.floor(parPos.zCoord), pathRange);
			job.callback = this;
			job.canUseLadder = true;
			job.useFlyPathfinding = canFlyPath.getValue();
			job.useSwimPathfinding = canSwimPath.getValue();
			PFQueue.tryPath(job);
		} else {
			//System.out.println("request pf set: " + parPos);
			PFQueue.tryPath(agent.ent, MathHelper.floor(parPos.xCoord), MathHelper.floor(parPos.yCoord), MathHelper.floor(parPos.zCoord), pathRange, 0, this);
		}
		isWaitingForPath.setValue(true);
		isPathReceived.setValue(false);
		lastTimeRequestedPFThreaded = System.currentTimeMillis();
	}
	
	public void resetReceived() {
		isPathReceived.setValue(false);
		isWaitingForPath.setValue(false); //hmm
	}

	@Override
	public void pfComplete(PFCallbackItem ci) {
		listCallbackPaths.add(ci);
	}

	@Override
	public void manageCallbackQueue() {
		ArrayList<PFCallbackItem> list = getQueue();
		
		try {
			for (int i = 0; i < list.size(); i++) {
				PFCallbackItem item = list.get(i);
				
				if (!item.ent.isDead && OldUtil.chunkExists(item.ent.world, MathHelper.floor(item.ent.posX / 16), MathHelper.floor(item.ent.posZ / 16))) {
					setPathFar(item.pe);
					//item.ent.getNavigator().setPath(item.pe, item.speed);
				}
			}
		} catch (Exception ex) {
			System.out.println("Crash in Epoch AI Blackboard for PF Callback");
			ex.printStackTrace();
		}
		
		listCallbackPaths.clear();
	}

	@Override
	public ArrayList<PFCallbackItem> getQueue() {
		return listCallbackPaths;
	}
}
