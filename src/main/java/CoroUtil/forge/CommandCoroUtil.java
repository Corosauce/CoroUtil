package CoroUtil.forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.OldUtil;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.pathfinding.PFQueue;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.quest.quests.ActiveQuest;
import CoroUtil.quest.quests.ItemQuest;
import CoroUtil.util.CoroUtil;
import CoroUtil.util.CoroUtilEntity;
import CoroUtil.util.CoroUtilItem;

public class CommandCoroUtil extends CommandBase {

	@Override
	public String getCommandName() {
		return "coroutil";
	}

	@Override
	public void processCommand(ICommandSender var1, String[] var2) {
		
		try {
			if(var1 instanceof EntityPlayerMP)
			{
				EntityPlayer player = getCommandSenderAsPlayer(var1);
				
				if (var2[0].equals("testquest")) {
					String createQuestStr = "CoroUtil.quest.quests.ItemQuest";
					PlayerQuests plQuests = PlayerQuestManager.i().getPlayerQuests(player);
					ActiveQuest aq = PlayerQuests.createQuestFromString(createQuestStr);
					
					System.out.println("trying to create quest from str: " + createQuestStr);
					
					if (aq != null) {
						aq.initCreateObject(plQuests);
						
						aq.initFirstTime(player.worldObj.provider.dimensionId);
						((ItemQuest)aq).initCustomData(CoroUtilItem.getNameByItem(Items.diamond), 5, false);
						
						PlayerQuestManager.i().getPlayerQuests(CoroUtilEntity.getName(player)).questAdd(aq);
						System.out.println("create success type: " + aq.questType);
					} else {
						System.out.println("failed to create quest " + createQuestStr);
					}
					
					plQuests.saveAndSyncPlayer();
				} else if (var2[0].equals("aitest")) {
					/*System.out.println("AI TEST MODIFY!");
					BehaviorModifier.test(player.worldObj, Vec3.createVectorHelper(player.posX, player.posY, player.posZ), CoroUtilEntity.getName(player));*/
				} else if (var2[0].equalsIgnoreCase("spawn")) {
					
					String prefix = "";
					String mobToSpawn = var2[1];
					
					int count = 1;
					
					if (var2.length > 2) {
						count = Integer.valueOf(var2[2]);
					}

					for (int i = 0; i < count; i++) {
						Entity ent = EntityList.createEntityByName(prefix + mobToSpawn, player.worldObj);
						
						if (ent == null) ent = EntityList.createEntityByName(mobToSpawn, player.worldObj);
						
						if (ent == null) {
							List<String> entsToSpawn = listEntitiesSpawnable(mobToSpawn);
							if (entsToSpawn.size() > 0) {
								for (int j = 0; j < entsToSpawn.size(); j++) {
									Entity ent2 = EntityList.createEntityByName(entsToSpawn.get(j), player.worldObj);
									if (ent2 != null) {
										CoroUtil.sendPlayerMsg((EntityPlayerMP) player, "spawned: " + CoroUtilEntity.getName(ent2));
										spawnEntity(player, ent2);
									}
								}
							} else {
								CoroUtil.sendPlayerMsg((EntityPlayerMP) player, "found nothing to spawn");
							}
						} else {
							if (ent != null) {
								
								CoroUtil.sendPlayerMsg((EntityPlayerMP) player, "spawned: " + CoroUtilEntity.getName(ent));
								spawnEntity(player, ent);
								
							}
						}
					}
				} else if (var2[0].equalsIgnoreCase("get")) {
	        		if (var2[1].equalsIgnoreCase("count")) {
	        			boolean exact = false;
	        			if (var2.length > 3) exact = var2[3].equals("exact");
	        			CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, var2[2] + " count: " + getEntityCount(var2[2], false, exact, ((EntityPlayer)var1).dimension));
	        		} else if (var2[1].equalsIgnoreCase("PFQueue")) {
	        			if (var2[2].equalsIgnoreCase("lastpf")) {
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, "last PF Time: " + ((System.currentTimeMillis() - PFQueue.lastSuccessPFTime) / 1000F));
	        				//var1.sendChatToPlayer(var2[2] + " set to: " + c_CoroAIUtil.getPrivateValueBoth(PFQueue.class, PFQueue.instance, var2[2], var2[2]));
	        			} if (var2[2].equalsIgnoreCase("stats")) {
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, "PFQueue Stats");
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, "-------------");
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, PFQueue.lastQueueSize + " - " + "PF queue size");
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, PFQueue.lastChunkCacheCount + " - " + "Cached chunks");
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, PFQueue.statsPerSecondPath + " - " + "Pathfinds / 10 sec");
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, PFQueue.statsPerSecondPathSkipped + " - " + "Old PF Skips / 10 sec");
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, PFQueue.statsPerSecondNodeMaxIter + " - " + "Big PF Skips / 10 sec");
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, PFQueue.statsPerSecondNode + " - " + "Nodes ran / 10 sec");
	        					        				
	        				
	        				
	        			} else {
	        				//var1.sendChatToPlayer("Last chunk cache count: " + PFQueue.lastChunkCacheCount);
	        				CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, var2[2] + " set to: " + OldUtil.getPrivateValueBoth(PFQueue.class, PFQueue.instance, var2[2], var2[2]));
	        			}
	        		}
	        	} else if (var2[0].equalsIgnoreCase("kill")) {
	        		boolean exact = false;
	        		int dim = ((EntityPlayer)var1).dimension;
        			//if (var2.length > 2) exact = var2[2].equals("exact");
	        		if (var2.length > 2) dim = Integer.valueOf(var2[1]);
	        		CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, var2[1] + " count killed: " + getEntityCount(var2[1], true, exact, dim));
	        	} else if (var2[0].equalsIgnoreCase("list")) {
	        		String param = null;
	        		int dim = ((EntityPlayer)var1).dimension;
	        		if (var2.length > 1) dim = Integer.valueOf(var2[1]);
	        		if (var2.length > 2) param = var2[2];
	        		HashMap<String, Integer> entNames = listEntities(param, dim);
	                
	        		CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, "List for dimension id: " + dim);
	        		
	                Iterator it = entNames.entrySet().iterator();
	                while (it.hasNext()) {
	                    Map.Entry pairs = (Map.Entry)it.next();
	                    CoroUtil.sendPlayerMsg((EntityPlayerMP) var1, pairs.getKey() + " = " + pairs.getValue());
	                    //System.out.println(pairs.getKey() + " = " + pairs.getValue());
	                    it.remove();
	                }
	        	}
			}
		} catch (Exception ex) {
			System.out.println("Exception handling CoroUtil command");
			ex.printStackTrace();
		}
		
	}
	
	public void spawnEntity(EntityPlayer player, Entity ent) {
		double dist = 1D;
		
		double finalX = player.posX - (Math.sin(player.rotationYaw * 0.01745329F) * dist);
		double finalZ = player.posZ + (Math.cos(player.rotationYaw * 0.01745329F) * dist);
		
		double finalY = player.posY;
		
		ent.setPosition(finalX, finalY, finalZ);
		
		
		
		//temp
		//ent.setPosition(69, player.worldObj.getHeightValue(69, 301), 301);
		//((JobGroupHorde)((ICoroAI) ent).getAIAgent().jobMan.priJob).attackCoord = new ChunkCoordinates(44, player.worldObj.getHeightValue(44, 301), 301);
		
		player.worldObj.spawnEntityInWorld(ent);
		if (ent instanceof EntityLiving) ((EntityLiving)ent).onSpawnWithEgg(null); //moved to after spawn, so client has an entity at least before syncs fire
		if (ent instanceof ICoroAI) ((ICoroAI) ent).getAIAgent().spawnedOrNBTReloadedInit();
	}
	
	public List<String> listEntitiesSpawnable(String entName) {
		List<String> entNames = new ArrayList<String>();
        
		Iterator it = EntityList.stringToClassMapping.keySet().iterator();
		
		while (it.hasNext()) {
			String entry = (String) it.next();
			
			if (entry.toLowerCase().contains(entName.toLowerCase())) {
				entNames.add(entry);
			}
		}
		
		return entNames;
	}
	
	public HashMap<String, Integer> listEntities(String entName, int dim) {
		HashMap<String, Integer> entNames = new HashMap<String, Integer>();
        
		World world = DimensionManager.getWorld(dim);
		
        for (int var33 = 0; var33 < world.loadedEntityList.size(); ++var33)
        {
            Entity ent = (Entity)world.loadedEntityList.get(var33);
            
            if (EntityList.getEntityString(ent) != null && (entName == null || EntityList.getEntityString(ent).toLowerCase().contains(entName.toLowerCase()))) {
	            int val = 1;
	            if (entNames.containsKey(EntityList.getEntityString(ent))) {
	            	val = entNames.get(EntityList.getEntityString(ent))+1;
	            }
	            entNames.put(EntityList.getEntityString(ent), val);
            }
            
        }
        
        entNames.put("!ALL", world.loadedEntityList.size());
        
        return entNames;
	}
	
	public int getEntityCount(String entName, boolean killEntities, boolean exact, int dim) {
		int count = 0;
		
        for (int var33 = 0; var33 < DimensionManager.getWorld(dim).loadedEntityList.size(); ++var33)
        {
            Entity ent = (Entity)DimensionManager.getWorld(dim).loadedEntityList.get(var33);
            
            if (EntityList.getEntityString(ent) != null && (EntityList.getEntityString(ent).equals(entName) || (!exact && EntityList.getEntityString(ent).toLowerCase().contains(entName.toLowerCase())))) {
            	count++;
            	if (killEntities) ent.setDead();
            }
        }
        
        return count;
	}
	
	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender)
    {
        return par1ICommandSender.canCommandSenderUseCommand(this.getRequiredPermissionLevel(), this.getCommandName());
    }

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "";
	}

}
