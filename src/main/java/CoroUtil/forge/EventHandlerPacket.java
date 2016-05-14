package CoroUtil.forge;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.entity.IEntityPacket;
import CoroUtil.packet.INBTPacketHandler;
import CoroUtil.packet.NBTDataManager;
import CoroUtil.packet.PacketHelper;
import CoroUtil.quest.PlayerQuestManager;
import CoroUtil.quest.PlayerQuests;
import CoroUtil.tile.ITilePacket;
import CoroUtil.util.CoroUtilEntity;

public class EventHandlerPacket {
	
	//if im going to load nbt, i probably should package it at the VERY end of the packet so it loads properly
	//does .payload continue from where i last read or is it whole thing?
	//maybe i should just do nbt only
	
	//changes from 1.6.4 to 1.7.2:
	//all nbt now:
	//- inv writes stack to nbt, dont use buffer
	//- any sending code needs a full reverification that it matches up with how its received in this class
	//- READ ABOVE ^
	//- CoroAI_Inv could be factored out and replaced with CoroAI_Ent, epoch entities use it this way

	@SideOnly(Side.CLIENT)
	public World getClientWorld() {
		return Minecraft.getMinecraft().theWorld;
	}
	
	@SideOnly(Side.CLIENT)
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().thePlayer;
	}
	
	@SideOnly(Side.CLIENT)
	public INBTPacketHandler getClientDataInterface() {
		if (Minecraft.getMinecraft().currentScreen instanceof INBTPacketHandler) {
			return (INBTPacketHandler)Minecraft.getMinecraft().currentScreen;
		}
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onPacketFromServer(FMLNetworkEvent.ClientCustomPacketEvent event) {
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.packet.payload());
			
			String command = nbt.getString("command");
			
			//System.out.println("CoroUtil packet command from server: " + command);
			
			if (command.equals("CoroAI_Inv")) {
				int entID = nbt.getInteger("entID");
				ItemStack is = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("itemstack"));
				
				Entity entity = getClientWorld().getEntityByID(entID);
				if (entity instanceof ICoroAI) {
					if (entity instanceof EntityLivingBase) {
						((EntityLivingBase) entity).setCurrentItemOrArmor(0, is);
					}
				}
			} else if (command.equals("CoroAI_TEntDW")) {
				//UNFINISHED
			} else if (command.equals("NBTData_GUI")) {
				NBTDataManager.nbtDataFromServer(nbt);
			} else if (command.equals("NBTData_CONT")) {
				INBTPacketHandler nbtHandler = getClientDataInterface();
				if (nbtHandler != null) {
					nbtHandler.nbtDataFromServer(nbt);
				}
				
			} else if (command.equals("CoroAI_Ent")) {
				
				int entID = nbt.getInteger("entityID");
				
				Entity entity = getClientWorld().getEntityByID(entID);
				if (entity instanceof IEntityPacket) {
					((IEntityPacket) entity).handleNBTFromServer(nbt/*.getCompoundTag("abilities")*/);
				}
				
				/*NBTTagCompound abilities = nbt.getCompoundTag("abilities");
				Iterator it = abilities.func_150296_c().iterator();
                while (it.hasNext()) {
                	String tagName = (String) it.next();
                	NBTTagCompound entry = abilities.getCompoundTag(tagName);
                }*/
			} else if (command.equals("QuestData")) {
				//receiving quest data for a specific player
				NBTTagCompound data = nbt.getCompoundTag("data");
				
				PlayerQuests quests = PlayerQuestManager.i().getPlayerQuests(getClientPlayer());
				
				//clear quests since we reload fully to sync
				quests.reset();
				quests.nbtLoad(data);
			} else if (command.equals("Ent_Motion")) {
				
				int entID = nbt.getInteger("entityID");
				
				Entity entity = getClientWorld().getEntityByID(entID);
				if (entity != null) {
					entity.motionX += nbt.getDouble("motionX");
					entity.motionY += nbt.getDouble("motionY");
					entity.motionZ += nbt.getDouble("motionZ");
				}
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@SubscribeEvent
	public void onPacketFromClient(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayer entP = ((NetHandlerPlayServer)event.handler).playerEntity;
		
		try {
			NBTTagCompound nbt = PacketHelper.readNBTTagCompound(event.packet.payload());
			
			String command = nbt.getString("command");
			
			//System.out.println("CoroUtil packet command from client: " + command);
			
			if (command.equals("CoroAI_TEntCmd")) {
				int dimID = nbt.getInteger("dimID");
				int x = nbt.getInteger("x");
				int y = nbt.getInteger("y");
				int z = nbt.getInteger("z");
				NBTTagCompound nbtData = nbt.getCompoundTag("data");
				
				World world = DimensionManager.getWorld(dimID);
				if (world != null) {
					TileEntity tEnt = world.getTileEntity(new BlockPos(x, y, z));
					if (tEnt instanceof ITilePacket) {
						System.out.println("CONFIRM THIS SHOULD BE nbtData and not just nbt var");
						((ITilePacket) tEnt).handleClientSentNBT(CoroUtilEntity.getName(entP), nbtData);
					}
				}
			} else if (command.equals("NBTData_CONT")) {
				if (entP.openContainer instanceof INBTPacketHandler) {
					((INBTPacketHandler)entP.openContainer).nbtDataFromClient(CoroUtilEntity.getName(entP), nbt);
				}
			} else if (command.equals("NBTData_GUI")) {
				NBTDataManager.nbtDataFromClient(CoroUtilEntity.getName(entP), nbt);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
