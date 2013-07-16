package CoroAI.componentAI.jobSystem;

import java.util.Random;

import CoroAI.componentAI.AITamable;
import CoroAI.entity.EnumActState;


public class JobPlay extends JobBase {
	
	public int playTime = 0;
	public int playTimeMax = 400;
	public int playTimeRandomSize = 20;
	
	public float rotationYaw = 0;
	
	public JobPlay(JobManager jm) {
		super(jm);
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return playTime == 0;
	}
	
	public void tick() {
		
		playTimeRandomSize = 20;
		
		if (playTime > 0) {
			playTime--;
			if (playTime == 0) playReset();
		}
		
		double distStart = 1D;
		double distEnd = 3.0D;
		double distStep = 0.5D;
		double lookStartStop = 90;
		double lookStep = 90D;
		
		if (playTime > 0) {
			if (ent.getNavigator().noPath()) {
				
				
				if (isMovementSafe(true, true, false, distStart, distEnd, distStep, lookStartStop, lookStep)) {
					
					int randChange = 60;
					distStart = 0.5D;
					distEnd = 1.0D;
					lookStartStop = 0;
					
					if (!isMovementSafe(false, false, true, distStart, distEnd, distStep, lookStartStop, lookStep)) {
						//randChange = 90;
						if (ent.worldObj.getWorldTime() % 40 < 20) {
							rotationYaw -= 45;
							//System.out.println("hit wall! 1");
						} else {
							rotationYaw += 45;
							//System.out.println("hit wall! 2");
						}
						
					} else {
						
					}
					
					
					
					Random rand = new Random();
					
					rotationYaw += rand.nextInt(randChange) - randChange/2;
					float speed = 0.38F;
					
					double vecX = -Math.cos(rotationYaw*0.01745329D);
					double vecZ = Math.sin(rotationYaw*0.01745329D);
					
					ent.getMoveHelper().setMoveTo(ent.posX+vecX, ent.posY, ent.posZ+vecZ, speed);
					if (rand.nextInt(20) == 0) ent.getJumpHelper().setJumping();
				} else {
					//System.out.println("not safe to play, stopping!");
					
					playReset();
				}
			} else {
				//System.out.println("pathing play stop, stopping!");
				playReset();
			}
		} else {
			AITamable tamable = entInt.getAIAgent().jobMan.getPrimaryJob().tamable;
			if (tamable.isTame()) {
				Random rand = new Random();
				if (ent.getNavigator().noPath() && rand.nextInt(playTimeRandomSize) == 0) {
					//System.out.println("play start");
					playStart();
				}
			}
		}
	}

	public void playStart() {
		ai.setState(EnumActState.PLAYING);
		playTime = playTimeMax;
	}
	
	public void playReset() {
		ai.setState(EnumActState.IDLE);
		playTime = 0; //redundant in 1 case, but not the other
	}
	
}
