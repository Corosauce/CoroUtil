package CoroAI.entity;


public class JobFindFood extends JobBase {
	
	public JobFindFood(JobManager jm) {
		super(jm);
	}
	
	@Override
	public void tick() {
		jobFindHealth();
	}
	
	@Override
	public boolean shouldExecute() {
		return ent.getFoodLevel() <= 17;
	}
	
	@Override
	public boolean shouldContinue() {
		return ent.getFoodLevel() > 17;
	}
	
	protected void jobFindHealth() {
		
		if (!(state == EnumJobState.IDLE)) { ent.setEntityToAttack(null); }
		
		//if (fakePlayer.foodStats.getFoodLevel() > 17) { swapJob(EnumJob.mFISHERMAN); }
		//if (ent.getFoodLevel() > 17) { ent.swapJob(jm.priJob); }
		
		if (state == EnumJobState.IDLE) {
			ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, ent.maxPFRange, 600);
			setJobState(EnumJobState.W1);
		} else if (state == EnumJobState.W1) {
			
			if (ent.getDistance(ent.homeX, ent.homeY, ent.homeZ) < 2F) {
				ent.faceCoord((int)(ent.homeX-0.5F), (int)ent.homeY, (int)(ent.homeZ-0.5F), 180, 180);
				ent.takeItems(ent.homeX, ent.homeY, ent.homeZ, -1, true);
				ent.eat();
				//ent.swapJob(jm.priJob);
			} else if (walkingTimeout <= 0 || !ent.hasPath()) {
				//this.setPathExToEntity(null);
				ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, ent.maxPFRange, 600);
			}
		}
	}
	
}
