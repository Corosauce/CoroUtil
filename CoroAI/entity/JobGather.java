package CoroAI.entity;

import java.util.ArrayList;
import java.util.List;

import CoroAI.PFQueue;
import CoroAI.PathEntityEx;
import net.minecraft.src.*;

public class JobGather extends JobBase {
	
	public float maxCastStr = 1; 
	
	public int fishingTimeout;
	
	public int dryCastX;
	public int dryCastY;
	public int dryCastZ;
	
	public int miningTimeout;
	public List<Integer> gatherables;
	InfoResource ir;
	public int closeTryCount;
	
	public JobGather(JobManager jm) {
		super(jm);
		gatherables = new ArrayList();
		gatherables.add(Block.wood.blockID);
		closeTryCount = 0;
	}
	
	@Override
	public void onJobRemove() {
		if (this.ent.fishEntity != null) {
			this.ent.fishEntity.catchFish();
		}
	}
	
	@Override
	public void tick() {
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
	
	@Override
	public void onIdleTick() {
		super.onIdleTick();
	}
	
	protected void jobFisherman() {
		//System.out.println(ent.getClass().toString() + occupationState);
		//ent.setDead();
		
		//if (!(state == EnumJobState.IDLE)) { ent.setEntityToAttack(null); }
		//if (true) return;
		//Finding water, might need delay
		if (state == EnumJobState.IDLE) {
			//moveSpeed = oldMoveSpeed;
			//temp disable
			if (!findResources(10, 45)) {
				if (findResources(120, 45)) {
					setJobState(EnumJobState.W1);
				} else {
					if (ent.rand.nextInt(150) == 0 && !ent.hasPath()) {
						//ent.updateWanderPath();
					}
				}
			} else {
				setJobState(EnumJobState.W1);
			}
		//walking to source
		} else if (state == EnumJobState.W1) {
			
			ent.setState(EnumActState.WALKING);
			//moveSpeed = oldMoveSpeed;
			//if (!ent.isInWater()) {
				if (walkingTimeout <= 0 || ent.getNavigator().getPath() == null) {
					float tdist = (float)ent.getDistance((int)ent.targX, (int)ent.targY, (int)ent.targZ);
					/*if (ent.name.startsWith("Akamu")) {
						int ee = 1;
					}*/
					findResources(10, 45);
					//ent.walkTo(ent, (int)ent.targX, (int)ent.targY, (int)ent.targZ, ent.maxPFRange, 600);
				}
			//} else {
				/*if (findLandClose()) {
					setJobState(EnumJobState.W4);
				}*/
			//}
			
			if (ent.getDistanceXZ(ent.targX, ent.targZ) < 3F/* || ent.isInWater() || ent.facingWater || nextNodeWater()*/) {
				//Aim at location
				//ent.rotationPitch -= 35;
				//if (ent.canCoordBeSeenFromFeet((int)ent.targX, (int)ent.targY, (int)ent.targZ)) {
					
					ent.setState(EnumActState.IDLE);
					setJobState(EnumJobState.W2);
					ent.setPathExToEntity(null);
					ent.getNavigator().clearPathEntity();
					//ent.walkTo(ent, (int)ent.targX, (int)ent.targY, (int)ent.targZ, ent.maxPFRange, 600);
					//castLine();
					
					//???
					//KoaTribeAI.fishing(this);
				/*} else {
					setJobState(EnumJobState.IDLE);
				}*/
				
				
			}
			
		//Waiting on fish
		} else if (state == EnumJobState.W2) {
			ent.setPathToEntity((PathEntityEx)null);
			ent.getNavigator().clearPathEntity();
			ent.faceCoord((int)ent.targX, (int)ent.targY, (int)ent.targZ, 90, 90);
			if (miningTimeout <= 0) {
				harvestBlockInstant();
				ent.rightClickItem();
				
				if (getWoodCount() > 10 || (ent.rand.nextInt(1) == 0 && getWoodCount() >= 8)) {
					//return to base!
					ent.walkTo(ent, ent.homeX, ent.homeY, ent.homeZ, ent.maxPFRange, 600);
					setJobState(EnumJobState.W3);
				} else {
					if (findResources(5, 10)) {
						//setJobState(EnumJobState.IDLE);
					}
					ent.walkTo(ent, (int)ent.targX, (int)ent.targY, (int)ent.targZ, ent.maxPFRange, 600);
					setJobState(EnumJobState.W4);
					
				}
				
			} else {
				miningTimeout--;
			}
			
		//Return to base
		} else if (state == EnumJobState.W3) {
			//moveSpeed = oldMoveSpeed;
			if (walkingTimeout <= 0 || ent.getNavigator().getPath() == null) {
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
			
			if (ent.getDistance(ent.targX, ent.targY, ent.targZ) <= 1.5F/* || ent.isInWater() || ent.facingWater || nextNodeWater()*/) {
				//Aim at location
				//ent.rotationPitch -= 35;
				//if (ent.canCoordBeSeenFromFeet((int)ent.targX, (int)ent.targY, (int)ent.targZ)) {
					
					ent.setState(EnumActState.IDLE);
					setJobState(EnumJobState.IDLE);
					ent.setPathExToEntity(null);
					ent.getNavigator().clearPathEntity();
					//ent.walkTo(ent, (int)ent.targX, (int)ent.targY, (int)ent.targZ, ent.maxPFRange, 600);
					//castLine();
					
					//???
					//KoaTribeAI.fishing(this);
				/*} else {
					setJobState(EnumJobState.IDLE);
				}*/
				
				
			} else if (walkingTimeout <= 0 || ent.getNavigator().getPath() == null) {
				setJobState(EnumJobState.IDLE);
			}
			
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
	
	
	
	protected void harvestBlockInstant() {
		int id = ent.worldObj.getBlockId(ent.targX, ent.targY, ent.targZ);
		int meta = ent.worldObj.getBlockMetadata(ent.targX, ent.targY, ent.targZ);
		//System.out.println("x: " + ent.targX + "y: " + ent.targY);
		if (id != 0) {
			Block.blocksList[id].harvestBlock(ent.worldObj, ent.fakePlayer, ent.targX, ent.targY, ent.targZ, meta);
			ir.mine();
			ent.worldObj.setBlockAndMetadataWithNotify(ent.targX, ent.targY, ent.targZ, 0, 0);
			miningTimeout = 10;
		} else {
			ir.mine();
			System.out.println("nnnnnnnnnaaaaaaayyyyyyyyyy");
		}
		
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
	
	protected int getWoodCount() {
		//Add other fish here as there are more to actually get
		return ent.getItemCount(Block.wood.blockID);
	}
	
	protected boolean isFish(int id) {
		//Add other fish here as there are more to actually get
		return id == Item.fishRaw.shiftedIndex;
	}
	
	protected void transferJobItems(int x, int y, int z) {
		
		if (ent.isChest(ent.worldObj.getBlockId(x, y-1, z))) {
			y--;
		}
		boolean transferred = false;
		if (ent.isChest(ent.worldObj.getBlockId(x, y, z))) {
			TileEntityChest chest = (TileEntityChest)ent.worldObj.getBlockTileEntity(x, y, z);
			if (chest != null) {
				ent.openHomeChest();
				/*int count = 0;
				for(int j = 0; j < inventory.mainInventory.length; j++)
		        {
		            if(inventory.mainInventory[j] != null && isFish(inventory.mainInventory[j].itemID))
		            {
		            	ItemStack ourStack = inventory.mainInventory[j];
		            	for (int k = 0; k < chest.getSizeInventory(); k++) {
		            		ItemStack chestStack = chest.getStackInSlot(k);
		            		if(chestStack == null) {
		            			//no problem
		            			chestStack = ourStack.copy();
		            			chest.setInventorySlotContents(k, chestStack);
		            			inventory.mainInventory[j] = null;
		            			break;
		            			
		            		} else if (ourStack.itemID == chestStack.itemID && chestStack.stackSize < chestStack.getMaxStackSize()) {
		            			int space = chestStack.getMaxStackSize() - chestStack.stackSize;
		            			
		            			int addCount = ourStack.stackSize;
		            			
		            			if (space < ourStack.stackSize) addCount = space;
		            			
		            			//transfer! the sexyness! lol haha i typ so gut ikr
		            			ourStack.stackSize -= addCount;
		            			chestStack.stackSize += addCount;
		            			
		            			if (ourStack.stackSize == 0) {
			            			inventory.mainInventory[j] = null;
			            			break;
		            			}
		            			
		            		}
		            		
		            	}
		            }
		        }*/
				transferItems(ent.inventory, chest, -1, -1, true);
			}
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
		
		c_CoroAIUtil.setItems_JobGather(ent);
		
	}
	
	public boolean findResources(int size, int tries) {
		return findResources(size, tries, false);
	}
	
	public boolean findResources(int size, int tries, boolean forceNew) {
		
		if (ir == null || ir.mined) {
			ir = (InfoResource) GroupInfo.getFirstResource(EnumResource.WOOD);
		}
		
		if (!forceNew && (ir != null && !ir.mined)) {
			ent.setState(EnumActState.WALKING);
			walkingTimeout = 300;
			ent.targX = ir.x;
			ent.targY = ir.y;
			ent.targZ = ir.z;
			PFQueue.getPath(ent, ent.targX, ent.targY-1, ent.targZ, 64);
			//System.out.println("ir: " + ir);
			//System.out.println("ir x: " + ent.targX + "y: " + ent.targY);
			return true;
		} else {
			int scanSize = ent.maxPFRange;
			int scanSizeY = size;
			
			int tryX;// = ((int)this.posX) + rand.nextInt(scanSize)-scanSize/2;
			int tryY = ((int)ent.posY) - 1;
			int tryZ;// = ((int)this.posZ) + rand.nextInt(scanSize)-scanSize/2;
			
			int i = tryY + ent.rand.nextInt(scanSizeY)-scanSizeY/2;
			
			//System.out.println(tryX + " " + i + " " + tryZ);
			for (int ii = 0; ii <= tries; ii++) {
				tryX = ((int)ent.posX) + ent.rand.nextInt(scanSize)-scanSize/2;
				i = tryY + ent.rand.nextInt(scanSizeY)-scanSizeY/2;
				tryZ = ((int)ent.posZ) + ent.rand.nextInt(scanSize)-scanSize/2;
				int id = ent.worldObj.getBlockId(tryX, i, tryZ);
				
				if (isResourceBlock(id)/* && ent.canSeeBlock(tryX, i, tryZ)*/) {
					//System.out.println("found water");
					
					int newY = i;
					
					ir = GroupInfo.addResource(tryX, i, tryZ, EnumResource.WOOD);
					
					/*while (ent.worldObj.getBlockId(tryX, newY, tryZ) != 0) {
						newY++;
					}*/
					
					PFQueue.getPath(ent, tryX, newY-1, tryZ, scanSize/2+6);
					//this.setPathToEntity(worldObj.getEntityPathToXYZ(this, tryX, newY-1, tryZ, scanSize/2+6));
					
					//POST PATHFIND PATH LIMITER CODE GOES HERE
					//scan through pathnodes, look for where it goes from sand to water
					
					
					//if (!this.hasPath()) { System.out.println("no path"); }
					
					ent.setState(EnumActState.WALKING);
					walkingTimeout = 300;
					ent.targX = tryX;
					ent.targY = newY;
					ent.targZ = tryZ;
					//System.out.println("x: " + ent.targX + "y: " + ent.targY);
					//System.out.println(tryX + " " + i + " " + tryZ);
					return true;
					
				} else {
					//System.out.println("no water");
				}
			}
		}
		
		return false;
	}
	
	public boolean isResourceBlock(int id) {
		for (int i = 0; i < gatherables.size(); i++) {
			if (gatherables.get(i) == id) {
				return true;
			}
		}
		return false;
	}
	
}
