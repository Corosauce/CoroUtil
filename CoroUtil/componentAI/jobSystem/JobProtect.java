package CoroUtil.componentAI.jobSystem;

import CoroUtil.entity.EnumJobState;
import net.minecraft.entity.player.EntityPlayer;

public class JobProtect extends JobBase {
	
	public String playerName = "";
	public float minDist = 2F;
	public float maxDist = 8F;
	
	
	public JobProtect(JobManager jm) {
		super(jm);
	}
	
	@Override
	public boolean shouldExecute() {
		
		
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		/*EntityPlayer entP = ent.worldObj.getPlayerEntityByName(playerName);
		if (entP != null) {
			//System.out.println(ent.getDistanceToEntity(entP));
			if (ent.getDistanceToEntity(entP) > maxDist) {
				return false;
			}
		}*/
		return true;
	}
	
	public void checkPlayer() {
		EntityPlayer entP;
		if (playerName == "") {
			entP = ent.worldObj.getClosestPlayerToEntity(ent, 16F);
			if (entP != null) {
				playerName = entP.username;
			}
		}
	}
	
	@Override
	public void tick() {
		//consider this function broken after the refactoring!!!!!!!!!!!!!!!!!! FIX ME EVENTUALLY!
		
		// getClosestPlayerToEntity(ent, 16F);
		EntityPlayer entP;
		entP = ent.worldObj.getPlayerEntityByName(playerName);
		int pX = (int)(entP.posX-0.5F);
		int pY = (int)entP.posY;
		int pZ = (int)(entP.posZ-0.5F);
		
		if (state == EnumJobState.IDLE) {
			ai.walkTo(ent, pX, pY, pZ, ai.maxPFRange, 600);
			setJobState(EnumJobState.W1);
		} else if (state == EnumJobState.W1) {
			if (ent.getDistanceToEntity(entP)/*ent.getDistance(pX, pY, pZ)*/ <= minDist) {
				//ent.setPathExToEntity(null);
				ent.getNavigator().setPath(null, 0F);
				//ent.faceEntity(entP, 30F, 30F);
				
				//setJobState(EnumJobState.W2);
			} else if (walkingTimeout <= 0 || ent.getNavigator().getPath() == null || ent.getNavigator().getPath().isFinished()) {
				//ent.setPathExToEntity(null);
				ai.walkTo(ent, pX, pY, pZ, ai.maxPFRange, 600);
			}
		} else if (state == EnumJobState.W2) {
			
			
			
		}
	}
	
}
