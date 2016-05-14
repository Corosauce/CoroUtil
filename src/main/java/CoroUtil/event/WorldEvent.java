package CoroUtil.event;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.entity.EnumJobState;
import CoroUtil.util.BlockCoord;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilFile;

public class WorldEvent {

	//WIP shared WorldEvent base class, needs more refactoring
	
	public int dimensionID;
	public BlockCoord coordSource;
	public BlockCoord coordDestination;
	public String mainPlayerName = "";
	
	public boolean invasionActive = true;
	public int ticksActive;
	public int ticksMaxActive;
	
	public EnumWorldEventType type;
	
	public EnumJobState state;
	
	public ArrayList<EntityLivingBase> invasionEntities = new ArrayList<EntityLivingBase>();
	public ArrayList<String> cursedPlayers = new ArrayList<String>();
	
	public int lastCheckedInvasionCount = 1; //prevent default 0 incase cache isnt generated right away
	//public float lastWavePlayerRating;
	public float currentWaveDifficultyRating;
	public int currentWavePlayerCount;
	public int currentWaveCountFromPortal;
	public int currentWaveSpawnedInvaders;
	
	public int waveCount = 0;
	//public int maxCooldown = 12000; //half a day
	public int curCooldown = 800; //initial waiting for fire to go away cooldown, should be set only if meteor i guess
	
	//for client
	public float lastLeaderDist;
	public int lastLeaderCount;
	
	public enum EnumWorldEventType {
		INV_PORTAL_CATACOMBS, INV_PORTAL_NETHER, INV_CAVE, BOSS_CATACOMBS;
		public String[] eventEnumToName = new String[] { "Portal Invasion", "Nether Invasion", "Cave Invasion", "Boss Event" };
		
		private static final Map<Integer, EnumWorldEventType> lookup = new HashMap<Integer, EnumWorldEventType>();
	    static { for(EnumWorldEventType e : EnumSet.allOf(EnumWorldEventType.class)) { lookup.put(e.ordinal(), e); } }
	    public static EnumWorldEventType get(int intValue) { return lookup.get(intValue); }
	}
	
	public WorldEvent() {
		ticksActive = 0;
		ticksMaxActive = 168000; //more of a safety, 1 minecraft week timeout, the invasion should play out and end earlier than this
		state = EnumJobState.IDLE;
	}
	
	public WorldEvent(int parDim, String parName, EnumWorldEventType parType, BlockCoord source, BlockCoord dest) {
		this();
		type = parType;
		coordSource = source;
		coordDestination = dest;
		mainPlayerName = parName;
		dimensionID = parDim;
		
		//new invasion first wave ever for players name
		//starts at 0 rating
		//scans area for players, saves list for using later
		updateCursedPlayersList(false);
		
		//at end of wave
		//scan area for MORE players to add, dont clear the existing list
		//update each players rating, it sets it to player nbt
		//invasion sets currentWavePlayerRating to the calculated average across all players
		
		//next wave or invasion eventually starts, has currentWavePlayerRating to work with
	}
	
	public static WorldEvent newInvasionFromNBT(NBTTagCompound par1NBTTagCompound) {
		EnumWorldEventType type = EnumWorldEventType.get(par1NBTTagCompound.getInteger("type"));
		
		WorldEvent inv = null;
		
		inv = new WorldEvent();/*
		
		if (type == EnumWorldEventType.INV_CAVE) {
			
		} else if (type == EnumWorldEventType.INV_PORTAL_CATACOMBS) {
			inv = new InvasionPortalCatacombs();
		} else if (type == EnumWorldEventType.BOSS_CATACOMBS) {
			inv = new BossCatacombs();
		}*/
			
		inv.readNBT(par1NBTTagCompound);
		return inv;
	}
	
	public void tick() {
		World world = DimensionManager.getWorld(dimensionID);
		EntityPlayer entP = world.getPlayerEntityByName(mainPlayerName);
		////////////if (entP != null) WorldDirector.getPlayerNBT(entP.username).setInteger("HWInvasionCooldown", ModConfigFields.coolDownBetweenInvasionsPortal + 1);
		//if (DimensionManager.getWorld(dimensionID).getWorldTime() % 40 == 0) updatePlayerStates();
		//invasionEnd();
		
		//TEMP!!!
		//System.out.println();
		//updatePlayerStates();
	}
	
	public boolean isComplete() {
		return !invasionActive;
	}
	
	public void setState(EnumJobState job) {
		state = job;
	}
    
    public boolean checkForActiveInvadersCached() {
    	if (lastCheckedInvasionCount == 0 || DimensionManager.getWorld(dimensionID).getWorldTime() % 100 == 0) {
	    	for (int i = 0; i < invasionEntities.size(); i++) {
	    		EntityLivingBase ent = invasionEntities.get(i);
	    		
	    		if (ent.isDead) {
	    			invasionEntities.remove(i);
	    		} else {
	    			//HostileWorlds.dbg("murrrrrrrr" + ((EntityLivingBase)ent).getDistance(coordDestination.posX, coordDestination.posY, coordDestination.posZ));
	    		}
	    	}
	    	
	    	if (lastCheckedInvasionCount != invasionEntities.size() && invasionEntities.size() == 0) onFirstDetectNoActiveInvaders();
	    	
	    	lastCheckedInvasionCount = invasionEntities.size();
	    	
	    	if (invasionEntities.size() == 0) {
	    		return false;
	    		//invasionEnd();
	    	}
	    	return true;
    	} else {
    		return lastCheckedInvasionCount > 0;
    	}
    }
    
    public void onFirstDetectNoActiveInvaders() {
    	updateCursedPlayersList(false);
    	updatePlayerStates();
    	calculatePlayerRatingData();
    }
    
    public void invasionStart() {
    	//HostileWorlds.dbg("Invasion started for: " + coordSource.posX + ", " + coordSource.posY + ", " + coordSource.posZ);
    	invasionActive = true;
    	//invasionEntities.clear();
    }
    
    public void invasionEnd() {
    	//HostileWorlds.dbg("Invasion ended for: " + coordSource.posX + ", " + coordSource.posY + ", " + coordSource.posZ);
    	invasionActive = false;
    	//invasionLastTime = System.currentTimeMillis();
    	//cursedPlayers.clear();
    }
    
    public void registerWithInvasion(EntityLivingBase ent) {
    	//HostileWorlds.dbg("ent registered with invasion: " + ent);
    	invasionEntities.add(ent);
    }
    
    /*public void setEntityInvasionInfo(EntityInvader ent) {
    	ent.HWDifficulty = this.currentWaveDifficultyRating;
    	ent.primaryTarget = mainPlayerName;
    }*/
    
    public EntityPlayer tryGetCursedPlayer(String username) {
    	EntityPlayer entP = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(username);
    	//EntityPlayer entP = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(username);
    	
    	if (entP != null && entP.worldObj.provider.getDimensionId() == DimensionManager.getWorld(dimensionID).provider.getDimensionId()) {
    		return entP;
    	}
    	
    	return null;
    }
    
    public void calculatePlayerRatingData() {
		
		World worldObj = DimensionManager.getWorld(dimensionID);

		float playersFound = 0;
		float totalRating = 0;
		
		for (int i = 0; i < cursedPlayers.size(); i++) {
			EntityPlayer entP = tryGetCursedPlayer(cursedPlayers.get(i));
			
			if (entP != null) {
				playersFound++;
				////////////totalRating += WorldDirector.getPlayerNBT(entP.username).getInteger("HWPlayerRating");
			}
		}
		
		int waveCount = 0;
		
		/*TileEntity tEnt = DimensionManager.getWorld(dimensionID).getBlockTileEntity(coordSource.posX, coordSource.posY, coordSource.posZ);
		if (tEnt instanceof TileEntityHWPortal) {
			TileEntityHWPortal portal = ((TileEntityHWPortal)tEnt).getMainTileEntity();
			
			waveCount = portal.numOfWavesSpawned;
		}*/
		
		////////////waveCount = WorldDirector.getPlayerNBT(mainPlayerName).getInteger("numOfWavesSpawned");
		
		float playerCountAdditiveFactor = 3;
		float waveCountFactor = 2;
		
		int averagedRating = (int) ((totalRating / playersFound) + (playersFound * playerCountAdditiveFactor) + (waveCount * waveCountFactor));
		
		//HostileWorlds.dbg("HW averaged rating: " + averagedRating + " for " + playersFound + " players, waveCount at: " + waveCount);
		
		currentWaveDifficultyRating = averagedRating;
		currentWavePlayerCount = (int) playersFound;
		currentWaveCountFromPortal = waveCount;
    }
    
    public void updateCursedPlayersList(boolean clearList) {
    	float maxDist = 96F;
		
		World worldObj = DimensionManager.getWorld(dimensionID);
		
		if (clearList) cursedPlayers.clear();
		
		for (int i = 0; i < worldObj.playerEntities.size(); i++) {
			EntityPlayer entP = (EntityPlayer)worldObj.playerEntities.get(i);
			
			if (!cursedPlayers.contains(CoroUtilEntity.getName(entP)) && entP.getDistance(coordDestination.posX, coordDestination.posY, coordDestination.posZ) < maxDist) {
				cursedPlayers.add(CoroUtilEntity.getName(entP));
			}
		}
    }
    
    public void updatePlayerStates() {
    	
		for (int i = 0; i < cursedPlayers.size(); i++) {
			float armorValue = 0;
			float bestWeaponValue = 0;
			boolean hasGlove = false;
			
			EntityPlayer entP = tryGetCursedPlayer(cursedPlayers.get(i));
			
			if (entP != null) {
				for (int armorIndex = 0; armorIndex < 4; armorIndex++) {
					if (entP.inventory.armorInventory[armorIndex] != null && entP.inventory.armorInventory[armorIndex].getItem() instanceof ItemArmor) {
						armorValue += EnchantmentHelper.getEnchantmentModifierDamage(entP.inventory.armorInventory, DamageSource.generic);
					}
				}
				
				//new plan here for 1.6
				//remove attrib from ent for current item
				//for each item
				//- remove attrib from prev (????) - unneeded step
				//- apply attrib
				//- get damage
				//- remove attrib to reset this part
				//finally readd current weapon attrib onto ent to undo any weird manip
				
				//initial removal of current weap attrib
				ItemStack itemstack = entP.inventory.getCurrentItem();
				if (itemstack != null) entP.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers());
				
				for (int slotIndex = 0; slotIndex < entP.inventory.mainInventory.length; slotIndex++) {
					if (entP.inventory.mainInventory[slotIndex] != null) {
						
						itemstack = entP.inventory.mainInventory[slotIndex];

	                    if (itemstack != null)
	                    {
	                    	//add attrib
	                    	entP.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers());
	                    }
	                    
	                    //get val
	                    float f = (float)entP.getEntityAttribute(SharedMonsterAttributes.attackDamage).getAttributeValue();
	                    float f1 = 0.0F;

	                    if (entP instanceof EntityLivingBase)
	                    {
	                    	//these need to have a target entity passed to them, hmmmmmmm, use own reference for now like old code apparently did
	                    	//TODO: update for 1.8 somehow... 
	                        //f1 = EnchantmentHelper.getEnchantmentModifierLiving(entP, (EntityLivingBase)entP);
	                        //i += EnchantmentHelper.getKnockbackModifier(this, (EntityLivingBase)par1Entity);
	                    }
	                    
	                    float dmg = f + f1;

						if (itemstack != null)
	                    {
							//remove attrib
							entP.getAttributeMap().removeAttributeModifiers(itemstack.getAttributeModifiers());
	                    }
						
	                    if (dmg > bestWeaponValue) {
							bestWeaponValue = dmg;
						}
					}
				}
				
				//readd of current weapon attrib
				itemstack = entP.inventory.getCurrentItem();
				if (itemstack != null) entP.getAttributeMap().applyAttributeModifiers(itemstack.getAttributeModifiers());
				
				//System.out.println("calculated bestWeaponValue: " + bestWeaponValue);
				/////////////////WorldDirector.getPlayerNBT(entP.username).setInteger("HWPlayerRating", (int)(armorValue + bestWeaponValue + (hasGlove ? 20 : 0)));
			}
		}
    }
	
	public void writeNBT(NBTTagCompound data) {
		//data.setFloat("lastWavePlayerRating", lastWavePlayerRating);
		data.setFloat("currentWaveDifficultyRating", currentWaveDifficultyRating);
		data.setInteger("dimensionID", dimensionID);
    	data.setInteger("type", type.ordinal());
    	if (coordSource != null) CoroUtilFile.writeCoords("source", coordSource, data);
    	if (coordDestination != null) CoroUtilFile.writeCoords("dest", coordDestination, data);
    	data.setInteger("ticksActive", ticksActive);
    	data.setInteger("ticksMaxActive", ticksMaxActive);
    	data.setInteger("waveCount", waveCount);
    	data.setString("mainPlayerName", mainPlayerName);
    	data.setInteger("state", state.ordinal());
    	
    	//for client syncing
    	data.setInteger("currentWaveCountFromPortal", currentWaveCountFromPortal);
    	data.setInteger("currentWaveSpawnedInvaders", currentWaveSpawnedInvaders);
    	data.setInteger("currentWavePlayerCount", currentWavePlayerCount);
    	int dist = -1;
    	if (coordDestination != null && invasionEntities.size() > 0) dist = (int) ((EntityLivingBase)invasionEntities.get(0)).getDistance(coordDestination.posX, coordDestination.posY, coordDestination.posZ);
    	data.setInteger("lastLeaderDist", dist);
    	data.setInteger("lastLeaderCount", invasionEntities.size());
    	data.setInteger("curCooldown", curCooldown);
    }
	
	public void readNBT(NBTTagCompound data) {
		//lastWavePlayerRating = data.getFloat("lastWavePlayerRating");
		currentWaveDifficultyRating = data.getFloat("currentWaveDifficultyRating");
		dimensionID = data.getInteger("dimensionID");
    	type = EnumWorldEventType.get(data.getInteger("type"));
    	coordSource = CoroUtilFile.readCoords("source", data);
    	coordDestination = CoroUtilFile.readCoords("dest", data);
    	ticksActive = data.getInteger("ticksActive");
    	ticksMaxActive = data.getInteger("ticksMaxActive");
    	waveCount = data.getInteger("waveCount");
    	mainPlayerName = data.getString("mainPlayerName");
    	state = EnumJobState.get(data.getInteger("state"));
    	
    	//for client syncing
    	currentWaveCountFromPortal = data.getInteger("currentWaveCountFromPortal");
    	currentWaveSpawnedInvaders = data.getInteger("currentWaveSpawnedInvaders");
    	currentWavePlayerCount = data.getInteger("currentWavePlayerCount");
    	lastLeaderDist = data.getInteger("lastLeaderDist");
    	lastLeaderCount = data.getInteger("lastLeaderCount");
    	curCooldown = data.getInteger("curCooldown");
    }
	
	public void init() {
		
	}

	public void cleanup() {
		
	}
}
