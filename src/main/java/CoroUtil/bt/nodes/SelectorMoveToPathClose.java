package CoroUtil.bt.nodes;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import CoroUtil.bt.Behavior;
import CoroUtil.bt.BlackboardBase;
import CoroUtil.bt.EnumBehaviorState;
import CoroUtil.bt.IBTAgent;
import CoroUtil.bt.selector.Selector;
import CoroUtil.util.CoroUtilBlock;

public class SelectorMoveToPathClose extends Selector {

	public IBTAgent entInt;
	public EntityLiving ent;
	public BlackboardBase blackboard;
	
	//public int pathfindRange = 12;
	public int repathDelay = 20*5;
	public int repathDelayFailAdd = 60;
	public long lastPathTime = 0;
	public int closeDist;
	public boolean partialPathing = false;
	public boolean lastAttemptFailed = false;
	
	public int retryStage = 0;
	public int retryStageMax = 15;
	
	public SelectorMoveToPathClose(Behavior parParent, IBTAgent parEnt, BlackboardBase parBB, int parCloseDist, boolean parPartialPathing) {
		super(parParent);
		blackboard = parBB;
		entInt = parEnt;
		ent = (EntityLiving)parEnt;
		closeDist = parCloseDist;
		partialPathing = parPartialPathing;
	}
	
	@Override
	public EnumBehaviorState tick() {

		int pathfindRange = blackboard.distMed.getValue() + 8;
		
		if (blackboard.posMoveTo != null) {
			double distToPos = blackboard.agent.ent.getDistance(blackboard.posMoveTo.xCoord, blackboard.posMoveTo.yCoord, blackboard.posMoveTo.zCoord);
			
			if (distToPos < closeDist) {
				if (children.size() > 0) return children.get(0).tick();
			} else {
				if (ent.onGround || ent.isInWater()) {
					if ((entInt.getAIBTAgent().pathNav.noPath() && !lastAttemptFailed) || (lastPathTime + repathDelay < ent.worldObj.getTotalWorldTime() && ent.worldObj.getTotalWorldTime() % 20 == 0)) {
						//System.out.println(distToPos + " insta pathing - " + ent.entityId + " - " + (lastPathTime + repathDelay - ent.worldObj.getTotalWorldTime()));
						lastPathTime = ent.worldObj.getTotalWorldTime();
						if (partialPathing) {
							if (lastPathTime % 5 == 0) {
								//PFQueue based retry code goes here
								//System.out.println("trying partial pf - " + ent);
								Random rand = new Random();
								Vec3 vec = new Vec3(blackboard.posMoveTo.xCoord - ent.posX, blackboard.posMoveTo.yCoord - ent.posY, blackboard.posMoveTo.zCoord - ent.posZ);
								vec = vec.normalize();
								float dist = 16;//+rand.nextInt(10);
								float distY = retryStage;
								float randY = rand.nextFloat()*distY - rand.nextFloat()*distY;
								int coordX = MathHelper.floor_double(ent.posX+(vec.xCoord*dist));
								int coordY = MathHelper.floor_double(ent.posY+((vec.yCoord*dist) + randY));
								int coordZ = MathHelper.floor_double(ent.posZ+(vec.zCoord*dist));
		    			        Block id = ent.worldObj.getBlockState(new BlockPos(coordX, coordY, coordZ)).getBlock();
		    			        int tries = 0;
		    			        if (CoroUtilBlock.isAir(id)) {
		    			        	Block idUp = ent.worldObj.getBlockState(new BlockPos(coordX, coordY+1, coordZ)).getBlock();
		    			        	Block idDown = ent.worldObj.getBlockState(new BlockPos(coordX, coordY-1, coordZ)).getBlock();
		    			        	if (CoroUtilBlock.isAir(idDown) && CoroUtilBlock.isAir(idUp)) {
		    			        		//System.out.println("trying partial");
		    			        		PathEntity result = ent.worldObj.getEntityPathToXYZ(ent, coordX, coordY, coordZ, pathfindRange, false, false, true, true);
		    							if (result == null || result.isFinished() || result.getCurrentPathLength() <= 2) {
		    								//System.out.println("try failed");
		    								lastAttemptFailed = true;
		    								lastPathTime = ent.worldObj.getTotalWorldTime() + repathDelayFailAdd; //add on penalty
		    							} else {
			    			        		retryStage = 0;
		    								//System.out.println("try success");
		    								lastAttemptFailed = false;
		    							}
		    							blackboard.pathMoveToPath = result;
		    							entInt.getAIBTAgent().pathNav.setPath(blackboard.pathMoveToPath, entInt.getAIBTAgent().moveSpeed);
		    			        	}
		    			        }
		    			        retryStage++;
		    			        if (retryStage >= retryStageMax) retryStage = 0;
							}
						} else {
							PathEntity result = ent.worldObj.getEntityPathToXYZ(ent, MathHelper.floor_double(blackboard.posMoveTo.xCoord), MathHelper.floor_double(blackboard.posMoveTo.yCoord), MathHelper.floor_double(blackboard.posMoveTo.zCoord), pathfindRange, false, false, true, true);
							if (result == null || result.isFinished() || result.getCurrentPathLength() <= 2) {
								//System.out.println("try failed");
								lastAttemptFailed = true;
								lastPathTime = ent.worldObj.getTotalWorldTime() + repathDelayFailAdd; //add on penalty
							} else {
								//System.out.println("try success");
								lastAttemptFailed = false;
							}
							blackboard.pathMoveToPath = result;
							entInt.getAIBTAgent().pathNav.setPath(blackboard.pathMoveToPath, entInt.getAIBTAgent().moveSpeed);
						}
					}
				}
				//entInt.getAIBTAgent().pathNav.setCanSwim(true);
				entInt.getAIBTAgent().pathNav.onUpdateNavigation();
			}
		}
		
		return EnumBehaviorState.SUCCESS;
		
	}
	
}
