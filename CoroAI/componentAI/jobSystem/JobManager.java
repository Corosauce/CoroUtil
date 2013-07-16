package CoroAI.componentAI.jobSystem;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import CoroAI.componentAI.AIAgent;

public class JobManager {
	
	//public HashMap<EnumJob, JobBase> jobTypes = new HashMap();
	//public ArrayList<JobBase> jobTypes = new ArrayList();
	
	//Entity reference
	public AIAgent ai = null;
	
	public ArrayList<JobBase> curJobs = new ArrayList();
	public JobBase priJob;
	public JobBase lastJobRun;
	public int lastJobRunID;
	
	public String debug = "";
	
	public JobManager(AIAgent entRef) {
		ai = entRef;
		
		
		//setPrimaryJob(JobIdle());
		
		//swapJob(priJob);
	}
	
	//Process hit across all jobs, cancels chain if job returns false
	public boolean hookHit(DamageSource par1DamageSource, int par2) {
		boolean result = true;
		for (int i = 0; i < curJobs.size(); i++) {
			JobBase job = curJobs.get(i);
			if (!job.hookHit(par1DamageSource, par2)) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public boolean hookInteract(EntityPlayer par1EntityPlayer) {
		boolean result = true;
		for (int i = 0; i < curJobs.size(); i++) {
			JobBase job = curJobs.get(i);
			if (!job.hookInteract(par1EntityPlayer)) {
				result = false;
				break;
			}
		}
		return result;
	}
	
	public void tick() {
		debug = "";
		for (int i = 0; i < curJobs.size(); i++) {
			JobBase job = curJobs.get(i);
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
		//System.out.println(debug);
	}
	
	public void addPrimaryJob(JobBase job) {
		priJob = job;
		addJob(job, -1);
	}
	
	public JobBase getPrimaryJob() {
		return priJob;
	}
	
	public JobBase getFirstJobByClass(Class clazz) {
		for (int i = 0; i < curJobs.size(); i++) {
			JobBase job = curJobs.get(i);
			if (clazz.isAssignableFrom(job.getClass())) {
				return job;
			}
		}
		return null;
	}
	
	public void addJob(JobBase newJob) {
		addJob(newJob, -1);
	}
	
	public void addJob(JobBase newJob, int priority) {
		if (priority != -1) {
			curJobs.add(priority, newJob); //this may be flawed, injects job at index, but doesnt exactly guarantee priority ordering
		} else {
			curJobs.add(newJob);
		}
	}
	
	public void clearJobs() {
		curJobs.clear();
	}
	
	public void cleanup() {
		for (int i = 0; i < curJobs.size(); i++) {
			JobBase job = curJobs.get(i);
			job.cleanup();
		}
		clearJobs();
		ai = null;
	}
	
}
