package CoroAI.componentAI.jobSystem;

import java.util.List;


import CoroAI.componentAI.ICoroAI;
import CoroAI.formation.Formation;
import CoroAI.formation.Manager;

import net.minecraft.entity.Entity;


public class JobFormation extends JobBase {
	
	public JobFormation(JobManager jm) {
		super(jm);
	}
	
	public void tick() {
		if (!shouldTickFormation()) {
			if (ent.worldObj.getWorldTime() % 40 == 0) {
				lookForOthers();
			}
		} else {
			//target what leader is targetting if no target
			if (ai.entityToAttack == null && ai.activeFormation.leaderTarget != null) {
				//System.out.println("targeting what leader is targeting!");
				ai.entityToAttack = ai.activeFormation.leaderTarget;
			}
		}
	}
	
	public void lookForOthers() {
		int huntRange = 16;
		List list = ent.worldObj.getEntitiesWithinAABBExcludingEntity(ent, ent.boundingBox.expand(huntRange, huntRange/2, huntRange));
		
		Formation foundFormation = null;
		int formationSize = 0;
		
		for(int j = 0; j < list.size(); j++)
        {
            Entity entity1 = (Entity)list.get(j);
            if(entity1 instanceof ICoroAI) {
            	ICoroAI otherEnt = (ICoroAI)entity1;
	            if(entity1 instanceof ICoroAI && otherEnt.getAIAgent().jobMan.getPrimaryJob().shouldTickFormation()) {
	            	if (otherEnt.getAIAgent().activeFormation.listEntities.size() > formationSize) {
	            		formationSize = otherEnt.getAIAgent().activeFormation.listEntities.size();
	            		foundFormation = otherEnt.getAIAgent().activeFormation;
	            	}
	            }
            }
        }
		
		if (foundFormation != null) {
			foundFormation.join(entInt);
		} else {
	        for(int j = 0; j < list.size(); j++)
	        {
	            Entity entity1 = (Entity)list.get(j);
	            
	            //needs the diplomacy here
	            if(entity1 instanceof ICoroAI && ((ICoroAI) entity1).getAIAgent().jobMan.getPrimaryJob().canJoinFormations()) {
	            	ICoroAI otherEnt = (ICoroAI)entity1;
	            	//if theyre in formation, join, else, start one since we're not in one
	            	if (!otherEnt.getAIAgent().jobMan.getPrimaryJob().isInFormation()) {
	            		otherEnt.getAIAgent().activeFormation = Formation.newFormation(entInt, otherEnt);
	            		Manager.addFormation(otherEnt.getAIAgent().activeFormation);
	            		break;
	            	}
	            }
	        }
		}
        
	}

}
