package CoroUtil.quest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.event.Event;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import CoroUtil.quest.quests.ActiveQuest;
import CoroUtil.quest.quests.ItemQuest;
import CoroUtil.quest.quests.KillEntityQuest;
import CoroUtil.util.CoroUtilFile;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PlayerQuests {

	public PlayerQuestManager playerQuestMan;
	public String playerName = "Player";
	//public EntityPlayer playerInstance;
	
	//public ArrayList<ActiveQuest> questInstances;
	public List<ActiveQuest> activeQuests;
	
	public PlayerQuests(PlayerQuestManager parMan, String parName) {
		playerQuestMan = parMan;
		//playerInstance = player;
		playerName = parName;
		activeQuests = new ArrayList();
		
		//TEMP
		//giveQuest(0);
	}
	
	public static ActiveQuest createQuestFromString(String parFullClassName) {
		
		try {
			Class createClass = Class.forName(parFullClassName);
			Constructor constructor = createClass.getConstructor();
			Object createObject = constructor.newInstance();
			if (createObject instanceof ActiveQuest) {
				return (ActiveQuest) createObject;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
	//warning, might become invalid upon player teleport, other scenarios, perhaps track player dimension id and username only, instead of instance
	public World getWorld() {
		return getPlayer().worldObj;
	}
	
	public EntityPlayer getPlayer() {
		return FMLCommonHandler.instance().getMinecraftServerInstance().getServerConfigurationManager(FMLCommonHandler.instance().getMinecraftServerInstance()).getPlayerForUsername(playerName);
	}
	
	public void tick(World parWorld) {
		for (int i = 0; i < activeQuests.size(); i++) {
			activeQuests.get(i).tick();
			if (activeQuests.get(i).isComplete()) onComplete(parWorld, i, true);
		}
	}
	
	public void saveAndSyncAllPlayers() {
		boolean syncAllPlayers = true;
		if (syncAllPlayers) {
			playerQuestMan.saveData(true, false);
		} else {
			saveAndSyncPlayer();
		}
	}
	
	public void saveAndSyncPlayer() {
		sync();
		diskSaveToFile();
	}
	
	public void sync() {
		if (MinecraftServer.getServer() != null)
        {
			
			NBTTagCompound nbt = new NBTTagCompound();
			
			nbtSave(nbt);

            try
            {
            	byte[] data = CompressedStreamTools.compress(nbt);
            	
            	
            	//System.out.println("packet byte count: " + data.length);
            	
            	ByteArrayOutputStream bos = new ByteArrayOutputStream((Byte.SIZE * data.length) + Short.SIZE);
                DataOutputStream dos = new DataOutputStream(bos);

                writeNBTTagCompound(nbt, dos, data);
                
                //dos.write(data);

	            Packet250CustomPayload pkt = new Packet250CustomPayload();
	            pkt.channel = "CoroUtilQuest";
	            pkt.data = bos.toByteArray();
	            pkt.length = bos.size();
	            MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(playerName).playerNetServerHandler.sendPacketToPlayer(pkt);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
	}
	
	protected static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutputStream par1DataOutputStream, byte[] data) throws IOException
    {
        if (par0NBTTagCompound == null)
        {
            par1DataOutputStream.writeShort(-1);
        }
        else
        {
            //byte[] var2 = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutputStream.writeShort((short)data.length);
            par1DataOutputStream.write(data);
        }
    }
	
	public void onComplete(World parWorld, int questIndex, boolean completeForAllInWorld) {
		if (completeForAllInWorld) {
			playerQuestMan.markQuestCompleteForAll(parWorld, activeQuests.get(questIndex));
		} else {
			questRemove(activeQuests.get(questIndex));
		}
		sync();
	}
	
	/*public void giveQuest(int questID) {
		boolean exists = false;
		for (int i = 0; i < activeQuests.size(); i++) {
			if (activeQuests.get(i).questID == questID) {
				exists = true;
			}
		}
		
		if (!exists) {
			activeQuests.add(QuestCreator.getNewQuestInstance(this, questID));
		}
	}*/
	
	/*public ActiveQuest getQuestByID(int parID) {
		//boolean exists = false;
		for (int i = 0; i < activeQuests.size(); i++) {
			if (activeQuests.get(i).questID == parID) {
				return activeQuests.get(i);
			}
		}
		
		return null;
	}*/
	
	public ActiveQuest getFirstQuestByStatus(EnumQuestState questState) {
		for (int i = 0; i < activeQuests.size(); i++) {
			if (activeQuests.get(i).curState == questState) {
				return activeQuests.get(i);
			}
		}
		
		return null;
	}
	
	public void questAdd(ActiveQuest quest) {
		activeQuests.add(quest);
	}
	
	public void questRemove(ActiveQuest quest) {
		for (int i = 0; i < activeQuests.size(); i++) {
			if (activeQuests.get(i)/*.questID*/ == quest/*.questID*/) {
				activeQuests.get(i).reset();
				activeQuests.remove(i);
			}
		}
	}
	
	public void questsClearAll() {
		for (int i = 0; i < activeQuests.size(); i++) {
			activeQuests.get(i).reset();
		}
		activeQuests.clear();
	}
	
	public void onEvent(Event event) {
		for (int i = 0; i < activeQuests.size(); i++) {
			if (event instanceof LivingDeathEvent) {
				if (activeQuests.get(i) instanceof KillEntityQuest) {
					((KillEntityQuest)activeQuests.get(i)).deathEvent((LivingDeathEvent)event);
				}
			} else if (event instanceof EntityItemPickupEvent) {
				if (activeQuests.get(i) instanceof ItemQuest) {
					((ItemQuest)activeQuests.get(i)).pickupEvent((EntityItemPickupEvent)event);
				}
			}
		}
	}
	
	@SideOnly(Side.CLIENT)
	public void renderQuestOverlay() {
		//System.out.println("quests: " + activeQuests.size());
		
		int startX = 10;
		int startY = 10;
		
		Minecraft mc = FMLClientHandler.instance().getClient();
		mc.fontRenderer.drawStringWithShadow("Quests:", startX, startY, 0xFFFFFF);
		
		startY += 10;
		
		for (int i = 0; i < activeQuests.size(); i++) {
			//System.out.println("client side active quest id: " + activeQuests.get(i).questID);
			
			String qStr = "";
			String qStr2 = "";
			
			if (activeQuests.get(i) instanceof KillEntityQuest) {
				qStr = "Kill some " + EntityList.classToStringMapping.get(((KillEntityQuest)activeQuests.get(i)).neededMob);
				qStr2 = "Killed: " + ((KillEntityQuest)activeQuests.get(i)).curKillCount + " / " + ((KillEntityQuest)activeQuests.get(i)).neededKillCount;
			}
			
			mc.fontRenderer.drawStringWithShadow(qStr, startX, startY + (i+1 * 10), 0xFFFFFF);
			mc.fontRenderer.drawStringWithShadow(qStr2, startX, startY + 10 + (i+1 * 10), 0xFFFFFF);
		}
	}
	
	public void diskLoadFromFile() {
		FileInputStream fis = null;
		
    	try {
    		String URL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroUtilQuests" + File.separator;
    		
    		File file = new File(URL + playerName + ".dat");
    		if (file.exists()) {
		    	fis = new FileInputStream(file);
		    	
		    	NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(fis);
		    	
		    	nbtLoad(nbttagcompound);
				
				if (fis != null) {
	    			fis.close();
	    		}
    		}
			
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	} finally {
    		
    		
    	}
	}
	
	public void diskSaveToFile() {
		try {
			
			NBTTagCompound nbt = new NBTTagCompound();
			
			nbtSave(nbt);
			
			String URL = CoroUtilFile.getWorldSaveFolderPath() + CoroUtilFile.getWorldFolderName() + "CoroUtilQuests" + File.separator;
			
			File fl = new File(URL);
			if (!fl.exists()) fl.mkdirs();
				
			FileOutputStream fos = new FileOutputStream(URL + playerName + ".dat");
			
	    	CompressedStreamTools.writeCompressed(nbt, fos);
	    	
	    	fos.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void syncUpdateQuests(NBTTagCompound parNBT) {
		
	}
	
	public void nbtLoad(NBTTagCompound parNBT) {
		
		if (!parNBT.hasNoTags()) {
			Iterator it = parNBT.getTags().iterator();
		
			while (it.hasNext()) {
				NBTTagCompound data = (NBTTagCompound)it.next();
				
				String classNamePath = data.getString("classNamePath");
				
				ActiveQuest quest = PlayerQuests.createQuestFromString(classNamePath);
				if (quest != null) {
					quest.initCreateObject(this);
					quest.load(data);
					quest.initCreateLoad();
					questAdd(quest);
				} else {
					System.out.println("CoroUtil was unable to deserialize quest with classname path: " + classNamePath + ", this might be due to a code structure change or a bug, quest not readded");
				}
			}
		} else {
			//no data to read in
		}
	}
	
	/*public void load(NBTTagCompound parNBT) {
		
		//FIX!!! iterates over active quests, not quests to load from nbt!!!!
		
		for (int i = 0; i < QuestManager.maxQuestCount; i++) {
			NBTTagCompound questNBT = parNBT.getCompoundTag("q" + i);
			if (!questNBT.hasNoTags()) {
				ActiveQuest quest = getQuestByID(i);
				if (quest == null) {
					quest = QuestManager.getNewQuestInstance(this, i);
					activeQuests.add(quest);
				}
				quest.load(questNBT);
				//activeQuests.get(i).load(questNBT);
			}
		}
	}*/
	
	public void nbtSave(NBTTagCompound parNBT) {
		for (int i = 0; i < activeQuests.size(); i++) {
			NBTTagCompound questNBT = new NBTTagCompound();
			activeQuests.get(i).save(questNBT);
			parNBT.setTag("qIndex" + i/*activeQuests.get(i).questID*/, questNBT);
		}
	}
	
	public void reset() {
		questsClearAll();		
	}
}
