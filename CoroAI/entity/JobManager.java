package CoroAI.entity;

import java.util.HashMap;
import java.util.LinkedList;

import net.minecraft.src.*;

public class JobManager {
	
	public HashMap<EnumJob, JobBase> jobTypes = new HashMap();
	
	//Entity reference
	public c_EnhAI ent = null;
	
	//Only 1 active job for now, but setup in a list for future
	public LinkedList<EnumJob> curJobs = new LinkedList();
	public EnumJob priJob;
	
	public EnumJob lastJobRun;
	public int lastJobRunID;
	//shared job info goes here
	
	
	public String debug = "";
	
	
	public JobManager(c_EnhAI entRef) {
		ent = entRef;
		
		jobTypes.put(EnumJob.UNEMPLOYED, new JobIdle(this));
		jobTypes.put(EnumJob.FISHERMAN, new JobFish(this));
		jobTypes.put(EnumJob.HUNTER, new JobHunt(this));
		jobTypes.put(EnumJob.FINDFOOD, new JobFindFood(this));
		jobTypes.put(EnumJob.INVADER, new JobInvade(this));
		jobTypes.put(EnumJob.PROTECT, new JobProtect(this));
		jobTypes.put(EnumJob.GATHERER, new JobGather(this));
		
		
		setPrimaryJob(EnumJob.UNEMPLOYED);
		
		//swapJob(priJob);
	}
	
	public void tick() {
		debug = "";
		for (int i = 0; i < curJobs.size(); i++) {
			JobBase job = enumToJob(curJobs.get(i));
			if (job.shouldExecute()) {
				debug = debug + job.toString() + " | ";
				job.tick();
				lastJobRun = curJobs.get(i);
				lastJobRunID = i;
			}
			if (!job.shouldContinue()) {
				break;
			}
		}
	}
	
	
	
	
	public void setPrimaryJob(EnumJob job) {
		priJob = job;
	}
	
	public EnumJob getJob() {
		if (curJobs.size() > 0) return curJobs.getFirst();
		return null;
	}
	
	public JobBase getJobClass() {
		if (curJobs.size() > 0) return enumToJob(priJob);
		return null;
	}
	
	public EnumJobState getJobState() {
		return getJobClass().state;
	}
	
	
	
	//use this mainly since we dont need to 'add' jobs yet
	public void swapJob(EnumJob newJob) {
		for (int i = 0; i < curJobs.size(); i++) {
			enumToJob(curJobs.get(i)).onJobRemove();
		}
		clearJobs();
		addJob(newJob);
	}
	
	public void addJob(EnumJob newJob) {
		addJob(newJob, -1);
	}
	
	public void addJob(EnumJob newJob, int priority) {
		if (priority != -1) {
			curJobs.add(priority, newJob);
		} else {
			curJobs.add(newJob);
		}
	}
	
	public JobBase enumToJob(EnumJob job) {
		JobBase temp = jobTypes.get(job);
		if (temp != null) {
			return temp;
		} else {
			return jobTypes.get(EnumJob.UNEMPLOYED);
		}
	}
	
	public void clearJobs() {
		curJobs.clear();
	}
	
}
