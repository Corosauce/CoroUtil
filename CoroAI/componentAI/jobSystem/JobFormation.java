package CoroAI.componentAI.jobSystem;

import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import CoroAI.componentAI.ICoroAI;
import CoroAI.formation.Formation;
import CoroAI.formation.Manager;


public class JobFormation extends JobBase {
	
	//add leader logic, make leader wander further maybe, dont override idle tick just add a new wander thing
	//maybe for here as well: once group > #, start really seeking out things to attack (players)
	
	public JobFormation(JobManager jm) {
		super(jm);
		jm.ai.canJoinFormations = true;
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
			
			//if a Group of 10 or more, do advanced wandering
			if (ai.activeFormation.listEntities.size() >= 6 && ai.activeFormation.leader == entInt) {
				Random rand = new Random();
				if (ent.getNavigator().noPath() && rand.nextInt(20) == 0) {
					
					int size = 96;
					int randX = (int)ent.posX + rand.nextInt(size) - size/2;
					int randY = (int)ent.posY + rand.nextInt(size/4) - size/8;
					int randZ = (int)ent.posZ + rand.nextInt(size) - size/2;
					
					int idGround = ent.worldObj.getBlockId(randX, ent.worldObj.getHeightValue(randX, randZ) - 1, randZ);
					int id1 = ent.worldObj.getBlockId(randX, ent.worldObj.getHeightValue(randX, randZ), randZ);
					int id2 = ent.worldObj.getBlockId(randX, ent.worldObj.getHeightValue(randX, randZ) + 1, randZ);
					
					if (idGround != 0 && Block.blocksList[idGround].blockMaterial != Material.water) {
						if (id1 == 0 && id2 == 0) {
							//System.out.println("LEADER EXTENDED IDLE PATHING!");
							ai.walkTo(ent, randX, randY, randZ, ai.maxPFRange, 600);
						}
					}
				}
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
