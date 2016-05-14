package CoroUtil.formation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.util.BlockCoord;

public class Formation {

	public double distMax = 96D;
	public List<ICoroAI> listEntities = new ArrayList<ICoroAI>();
	//Used for updating where the formation wants to go
	public ICoroAI leader;
	public EntityLiving leaderEnt;
	
	//Leader based stuff
	public Vec3 targ = null;
	
	public Vec3 pos = null;
	public double yaw;
	//rename this to shared target, other entities can set it if its null
	public EntityLivingBase leaderTarget;
	public double smoothYaw;
	
	public double speedSlowest = 0.28D;
	public PathNavigateFormation pathNav = null;
	
	public int sizeColumns = 10;
	public int sizeRows = 10;
	
	public static Formation newFormation(ICoroAI parLeader, ICoroAI secondEnt) {
		Formation fm = new Formation();
		fm.joinLeader(parLeader);
		if (secondEnt != null) fm.join(secondEnt);
		//unused
		fm.pathNav = new PathNavigateFormation(fm.leaderEnt.width, fm.leaderEnt.height, fm.leaderEnt.worldObj, 256);
		//System.out.println("new formation: " + fm);
		return fm;
	}
	
	public Formation() {
		
	}
	
	public void cleanup() {
		listEntities.clear();
	}
	
	//used for maintaining the active list of entities, if ones stray out and dont follow protocal and call leave, this could trim them out based on range
	public void tickUpdate() {
		//every n ticks
		//check list for
		//- distant entities
		//- 
		
		sizeColumns = 10;
		sizeRows = 10;
		
	
		if (leaderTarget != null && leaderTarget.isDead) leaderTarget = null;
		if (leaderEnt != null && leaderEnt.isDead) {
			listEntities.remove(leader);
			checkLeader(leader);
			leader = null;
			leaderEnt = null;
		}
		
		if (leaderEnt != null) {
			pos = getPosition(leaderEnt, 1F);
			yaw = leaderEnt.rotationYaw;
			if (leaderTarget == null) {
				leaderTarget = leaderEnt.getAttackTarget();
			}
			
			float bestMove = MathHelper.wrapAngleTo180_float((float) (smoothYaw - yaw));
			float camRotateSpeed = 0.6F/*bestMove * 0.03F*/;
			if (camRotateSpeed < 0.03F) camRotateSpeed = 0F;
			if (bestMove > camRotateSpeed * 1) {
				if (bestMove < camRotateSpeed * 2) {
					smoothYaw = yaw;
				} else {
					smoothYaw -= camRotateSpeed;
				}
			} else if (bestMove < camRotateSpeed * 1) {
				if (bestMove > camRotateSpeed * 2) {
					smoothYaw = yaw;
				} else {
					smoothYaw += camRotateSpeed;
				}
				
			}
			while (smoothYaw >= 180.0F) smoothYaw -= 360.0F;
            while (smoothYaw < -180.0F) smoothYaw += 360.0F;
			
			if (leaderEnt.worldObj.getWorldTime() % 40 == 0) {
				for (int i = 0; i < listEntities.size(); i++) {
					ICoroAI ent = listEntities.get(i);
					
					if (ent != null) {
						if (((EntityLivingBase)ent).isDead || ((EntityLivingBase)ent).getDistance(pos.xCoord, pos.yCoord, pos.zCoord) > distMax) {
							leave(ent);
						}
					}
				}
			}
		} else {
			checkLeader(null);
		}
	}
	
	public Vec3 getPosition(Entity ent, float par1)
    {
        if (par1 == 1.0F)
        {
            return new Vec3(ent.posX, ent.posY, ent.posZ);
        }
        else
        {
            double d0 = ent.prevPosX + (ent.posX - ent.prevPosX) * (double)par1;
            double d1 = ent.prevPosY + (ent.posY - ent.prevPosY) * (double)par1;
            double d2 = ent.prevPosZ + (ent.posZ - ent.prevPosZ) * (double)par1;
            return new Vec3(d0, d1, d2);
        }
    }
	
	public void setDestination(BlockCoord coords) {
		System.out.println("TODO");
	}
	
	//this should be called by entities already added, to get their position thats managed by this class
	public Vec3 getPosition(Entity ent) {
		if (listEntities.contains(ent)) {
			if (pos != null) {
				int squareSize = (int) Math.sqrt(listEntities.size());
				
				sizeColumns = squareSize;
				
				int index = listEntities.indexOf(ent);
				int row = index / sizeColumns;
				int col = index % sizeColumns;
				
				int rows = listEntities.size() / sizeColumns;
				
				double spacing = 1.5D/* + Math.sin(ent.worldObj.getWorldTime() * 0.1D)*/;
				
				double adjAngle = 0D;//listEntities.indexOf(ent) % 2 == 0 ? 0D : 180D;
				double dist = spacing * row - ((spacing*rows)/2);//(listEntities.indexOf(ent));
				adjAngle -= 90D;
				
				//depth wise, rows
				double posX = Math.cos((-smoothYaw + adjAngle) * 0.01745329D) * dist;
				double posZ = Math.sin((-smoothYaw + adjAngle) * 0.01745329D) * dist;
				
				dist = (spacing*col) - ((spacing*(sizeColumns+1))/2);
				adjAngle += 90D;
				
				//length wise, colums
				posX += Math.cos((-smoothYaw + adjAngle) * 0.01745329D) * dist;
				posZ += Math.sin((-smoothYaw + adjAngle) * 0.01745329D) * dist;
				
				
				
				posX = pos.xCoord - posX;
				double posY = (pos.yCoord/* - 0.3D - Math.sin((center.rotationPitch) / 180.0F * 3.1415927F) * dist*/);
				posZ = pos.zCoord + posZ;
				
				Vec3 tryPos = new Vec3(posX, posY, posZ);
				
				Block tryID = ent.worldObj.getBlockState(new BlockPos(MathHelper.floor_double(tryPos.xCoord), (int)tryPos.yCoord + 1, MathHelper.floor_double(tryPos.zCoord))).getBlock();
				
				//if clear (check ent height too), if not return center formation for safety
				if (!tryID.getMaterial().isSolid()) {
					
				} else {
					Random rand = new Random();
					double range = 3D;
					return pos.addVector(rand.nextDouble()*range - rand.nextDouble()*range, 0D, rand.nextDouble()*range - rand.nextDouble()*range);
				}
				
				//to fix movehelper trying to make them jump, might need a better fix
				tryPos = new Vec3(posX, ent.posY, posZ);
				return tryPos;
			} else {
				//System.out.println("DO I REPEAT FOR SAME ENT?!" + ent);
				return null;
			}
		} else {
			return null;
		}
	}
	
	public void joinLeader(ICoroAI ent) {
		join(ent);
		leader = ent;
		leaderEnt = (EntityLiving)ent;
		//System.out.println("Formation: leader assigned to " + this);
	}
	
	public void join(ICoroAI ent) {
		listEntities.add(ent);
		ent.getAIAgent().activeFormation = this;
		//System.out.println("Formation: ent joined " + this);
	}
	
	public void leave(ICoroAI ent) {
		listEntities.remove(ent);
		if (ent.getAIAgent() != null) ent.getAIAgent().activeFormation = null;
		checkLeader(ent);
		//System.out.println("listEntities.size(): " + listEntities.size());
	}
	
	public void checkLeader(ICoroAI ent) {
		if (/*ent == leader && */listEntities.size() > 0) {
			
			leader = listEntities.get(0);
			leaderEnt = (EntityLiving)leader;
			//System.out.println("Formation: new leader assigned! " + leader);
		}
	}
}
