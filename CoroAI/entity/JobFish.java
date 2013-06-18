package CoroAI.entity;

import net.minecraft.item.Item;

import CoroAI.PathEntityEx;
import CoroAI.c_CoroAIUtil;

public class JobFish extends JobBase {
	
	public float maxCastStr = 1; 
	
	public int fishingTimeout;
	
	public int dryCastX;
	public int dryCastY;
	public int dryCastZ;
	
	public JobFish(JobManager jm) {
		super(jm);
	}
	
	@Override
	public void onJobRemove() {
		if (this.ent.fishEntity != null) {
			this.ent.fishEntity.catchFish();
		}
	}
	
	@Override
	public void tick() {
		super.tick();
		jobFisherman();
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}
	
	@Override
	public boolean shouldContinue() {
		return true;
	}
	

	@Override
	public void onLowHealth() {
		super.onLowHealth();
		if (ent.fishEntity != null) ent.fishEntity.catchFish();
		if (ent.rand.nextInt(5) == 0) {
			ent.entityToAttack = null;
		} else {
			
		}
		
		
	}
	
	protected void jobFisherman() {
		//System.out.println(ent.getClass().toString() + occupationState);
		//setEntityDead();
		
		ent.maxDistanceFromHome = 16F;
		
		//if (!(state == EnumJobState.IDLE)) { ent.setEntityToAttack(null); }
		
		//Finding water, might need delay
		if (state == EnumJobState.IDLE) {
			//moveSpeed = oldMoveSpeed;
			//temp disable
			if (findWater()) {
				setJobState(EnumJobState.W1);
			} else {
				if (ent.rand.nextInt(150) == 0 && ent.getNavigator().noPath()) {
					ent.updateWanderPath();
				}
			}
		//walking to source
		} else if (state == EnumJobState.W1) {
			
			//setState(EnumKoaActivity.WALKING);
			//moveSpeed = oldMoveSpeed;
			if (!ent.isInWater()) {
				if (walkingTimeout <= 0 || ent.getNavigator().noPath()) {
					float tdist = (float)ent.getDistance((int)ent.targX, (int)ent.targY, (int)ent.targZ);
					/*if (ent.name.startsWith("Akamu")) {
						int ee = 1;
					}*/
					ent.walkTo(ent, (int)ent.targX, (int)ent.targY, (int)ent.targZ, ent.maxPFRange, 600);
				}
			} else {
				if (findLandClose()) {
					setJobState(EnumJobState.W4);
				}
			}
			
			//System.out.println("ent.getDistance(ent.targX, ent.targY, ent.targZ) " + ent.getDistance(ent.targX, ent.targY, ent.targZ));
			
			if (ent.getDistance(ent.targX, ent.targY, ent.targZ) < 8F || ent.isInWater() || ent.facingWater || nextNodeWater()) {
				//Aim at location
				//ent.rotationPitch -= 35;
				if (ent.canCoordBeSeenFromFeet((int)ent.targX, (int)ent.targY, (int)ent.targZ)) {
					
					ent.setState(EnumActState.IDLE);
					setJobState(EnumJobState.W2);
					ent.setPathExToEntity(null);
					ent.getNavigator().clearPathEntity();
					castLine();
					
					//???
					//KoaTribeAI.fishing(this);
				} else {
					setJobState(EnumJobState.IDLE);
				}
				
				
			}
			
		//Waiting on fish
		} else if (state == EnumJobState.W2) {
			//moveSpeed = 0F;
			if (!ent.isInWater()) {
				ent.setPathToEntity((PathEntityEx)null);
				ent.getNavigator().clearPathEntity();
				ent.faceCoord((int)ent.targX, (int)ent.targY, (int)ent.targZ, 90, 90);
			} else {
				
				//walkTo(this, homeX, homeY, homeZ, maxPFRange, 600);
				//setJobState(EnumJobState.W3);
				
				/*if (findLandClose()) {
					if (ent.fishEntity != null) {
						ent.fishEntity.catchFish();
					}
					setJobState(EnumJobState.W4);
				}*/
			}
			//ent.faceCoord((int)homeX, (int)homeY, (int)homeZ, 180, 180);

			if (ent.fishEntity == null) {
				//System.out.println("?!?!?!");
				//aimAtEnt(ent.fishEntity);
				//faceEntity(ent.fishEntity, 30F, 30F);
			}
			if (ent.fishEntity == null/* || ent.fishEntity.bobber == null*/ || fishingTimeout <= 0 || (ent.fishEntity != null && (ent.fishEntity.inGround || (ent.fishEntity.ticksCatchable > 0 && ent.fishEntity.ticksCatchable < 10)))) {
				if (ent.fishEntity != null) {
					ent.fishEntity.catchFish();
				}
				
				//System.out.println(getFishCount());
					
				if (getFishCount() > 4 || (ent.rand.nextInt(1) == 0 && getFishCount() >= 2)) {
					//return to base!
					ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, ent.maxPFRange, 600);
					setJobState(EnumJobState.W3);
				} else {
					if (ent.rand.nextInt(2) == 0) {
						//sets back to find water, maybe new location
						setJobState(EnumJobState.IDLE);
					} else {
						//sets back to walking to water, forces an instant recast of lure - CHANGED!
						//setJobState(EnumJobState.W1);
						castLine();
					}
				}
				
				ent.setState(EnumActState.IDLE);
				//ent.moveSpeed = 0.7F;
			} else {
				fishingTimeout--;
			}
			
		//Return to base
		} else if (state == EnumJobState.W3) {
			if (ent.fishEntity != null) {
				ent.fishEntity.catchFish();
			}
			//moveSpeed = oldMoveSpeed;
			if (walkingTimeout <= 0 || ent.getNavigator().noPath()) {
				//ent.setPathExToEntity(null);
				ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, ent.maxPFRange, 600);
			}
			if (ent.getDistance(ent.homeX, ent.homeY, ent.homeZ) < 2F) {
				//ent.setPathExToEntity(null);
				//drop off fish in nearby tile entity chest, assumably where homeXYZ is
				ent.faceCoord((int)(ent.homeX-0.5F), (int)ent.homeY, (int)(ent.homeZ-0.5F), 180, 180);
				transferJobItems(ent.homeX, ent.homeY, ent.homeZ);
				//System.out.println(homeX + " - " + homeZ);
				//set to idle, which will go back to fishing mode
				setJobState(EnumJobState.IDLE);
			}
		//Get back to dry cast spot and cast
		} else if (state == EnumJobState.W4) {
			
			if (walkingTimeout <= 0 || ent.getNavigator().noPath()) {
				//ent.setPathExToEntity(null);
				if (ent.getDistance(dryCastX, dryCastY, dryCastZ) < 10F) {
					ent.walkTo(ent, dryCastX, dryCastY, dryCastZ, ent.maxPFRange, 600);
				} else {
					ent.walkTo(ent, ent.targX, ent.targY, ent.targZ, ent.maxPFRange, 600);
				}
				
			} else {
				if (ent.getDistance(dryCastX, dryCastY, dryCastZ) < 1F) {
					ent.setState(EnumActState.IDLE);
					ent.setPathExToEntity(null);
					ent.getNavigator().clearPathEntity();
					castLine();
					setJobState(EnumJobState.W2);
				}
			}
			//distance check because findLand might set it to far distance if first path to drycastx attempt fails
			
			//if (ent.getDistance(targX, targY, targZ) < 1F) {
				//ent.setPathExToEntity(null);
				//drop off fish in nearby tile entity chest, assumably where homeXYZ is
				//ent.faceCoord((int)(homeX-0.5F), (int)homeY, (int)(homeZ-0.5F), 180, 180);
				//transferJobItems(homeX, homeY, homeZ);
				//System.out.println(homeX + " - " + homeZ);
				//set to idle, which will go back to fishing mode
				
			//}
			
			/*if (findLand()) {
				setJobState(EnumJobState.W3);
			}*/
			//PFQueue.getPath(this, homeX, homeY, homeZ, maxPFRange);
			
		}
	}
	
	protected boolean findLandClose() {
		if (ent.getDistance(dryCastX, dryCastY, dryCastZ) < 16F) {
			//targX = dryCastX;
			//targY = dryCastY;
			//targZ = dryCastZ;
			ent.walkTo(ent, dryCastX, dryCastY, dryCastZ, 32F, 100);
			setJobState(EnumJobState.W4);
			return true;
		} else if (findLand()) {
			return true;
		}
		return false;
	}
	
	protected void castLine() {
		if (!ent.isInWater()) {
			dryCastX = (int)Math.floor(ent.posX+0.5);
			dryCastY = (int)ent.boundingBox.minY;
			dryCastZ = (int)Math.floor(ent.posZ+0.5);
		}
		double dist = ent.getDistance((int)ent.targX, (int)ent.targY, (int)ent.targZ);
		
		ent.faceCoord((int)ent.targX, (int)ent.targY, (int)ent.targZ, 180, 180);
		//ent.faceCoord((int)homeX, (int)homeY, (int)homeZ, 180, 180);
		ent.castingStrength = (float)dist/17F;
		if (ent.castingStrength < 0.25) ent.castingStrength = 0.25F;
		if (ent.castingStrength > maxCastStr) {
			ent.castingStrength = maxCastStr;
		}
		ent.rotationPitch -= 25F;//dist*1.5;
		
		//System.out.println(castingStrength);
		
		//Select fishing rod
		c_CoroAIUtil.equipFishingRod(ent);
		
		//Shoot lure
		fishingTimeout = 400;
		if (ent.fishEntity != null) {
			ent.fishEntity.catchFish();
		}
		if (ent.fishEntity != null) {
			ent.fishEntity.setDead();
		}
		ent.fishEntity = null;
		ent.rightClickItem();
	}
	
	protected int getFishCount() {
		//Add other fish here as there are more to actually get
		return ent.getItemCount(Item.fishRaw.itemID);
	}
	
	protected boolean isFish(int id) {
		//Add other fish here as there are more to actually get
		return id == Item.fishRaw.itemID;
	}
	
	protected void transferJobItems(int x, int y, int z) {
		
		if (ent.isChest(ent.worldObj.getBlockId(x, y-1, z))) {
			y--;
		}
		boolean transferred = false;
		if (c_CoroAIUtil.tryTransferToChest(ent, x, y, z)) {
		//if (ent.isChest(ent.worldObj.getBlockId(x, y, z))) {
			
			
		} else {
			//if (mod_EntMover.debug) System.out.println("no chest for items");
			tossItems();
		}
	}
	
	public void tossItems() {
		for(int j = 0; j < ent.inventory.mainInventory.length; j++)
        {
            if(ent.inventory.mainInventory[j] != null && isFish(ent.inventory.mainInventory[j].itemID))
            {
            	ent.dropPlayerItemWithRandomChoice(ent.inventory.decrStackSize(j, ent.inventory.mainInventory[j].stackSize), false);
            }
        }
	}

	@Override
	public void setJobItems() {
		
		c_CoroAIUtil.setItems_JobFish(ent);
		
	}
	
	
	
}
