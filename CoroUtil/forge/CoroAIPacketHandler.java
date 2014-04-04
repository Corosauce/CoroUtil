package CoroUtil.forge;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroUtil.componentAI.ICoroAI;
import CoroUtil.entity.IEntityPacket;
import CoroUtil.packet.INBTPacketHandler;
import CoroUtil.packet.NBTDataManager;
import CoroUtil.tile.ITilePacket;
import CoroUtil.tile.TileDataWatcher;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CoroAIPacketHandler implements IPacketHandler {

	public CoroAIPacketHandler() {
	}

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
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			if ("CoroAI_Inv".equals(packet.channel)) {
				int entID = dis.readInt();
				ItemStack is = Packet.readItemStack(dis);
				
				Entity entity = getClientWorld().getEntityByID(entID);
				if (entity instanceof ICoroAI) {
					if (entity instanceof EntityLivingBase) {
						((EntityLivingBase) entity).setCurrentItemOrArmor(0, is);
					}
				}
			} else if ("CoroAI_TEntCmd".equals(packet.channel)) {
				int dimID = dis.readInt();
				int xCoord = dis.readInt();
				int yCoord = dis.readInt();
				int zCoord = dis.readInt();
				NBTTagCompound nbt = Packet.readNBTTagCompound(dis);
				World world = DimensionManager.getWorld(dimID);
				if (world != null) {
					TileEntity tEnt = world.getBlockTileEntity(xCoord, yCoord, zCoord);
					if (tEnt instanceof ITilePacket) {
						((ITilePacket) tEnt).handleClientSentNBT(((EntityPlayer)player).username, nbt);
					}
				}
			} else if (packet.channel.equals("CoroAI_TEntDW")) {
				int dimID = dis.readInt(); //kinda not needed for client side
				int xCoord = dis.readInt();
				int yCoord = dis.readInt();
				int zCoord = dis.readInt();
				List dwList = TileDataWatcher.readWatchableObjects(dis);
				if (side == Side.CLIENT) {
					World world = getClientWorld();
					if (world != null) {
						TileEntity tEnt = world.getBlockTileEntity(xCoord, yCoord, zCoord);
						if (tEnt instanceof ITilePacket) {
							((ITilePacket) tEnt).handleServerSentDataWatcherList(dwList);
						}
					}
				} else {
					World world = DimensionManager.getWorld(dimID);
					if (world != null) {
						TileEntity tEnt = world.getBlockTileEntity(xCoord, yCoord, zCoord);
						if (tEnt instanceof ITilePacket) {
							((ITilePacket) tEnt).handleClientSentDataWatcherList(((EntityPlayer)player).username, dwList);
						}
					}
				}
			} else if (packet.channel.equals("NBTData_GUI")) {
				//interface locations:
				//client: open gui, server: ???
				
				//given the fact of saving data to disk..... perhaps a managed data system for this is also needed
				
				//a global server side handler, gets username still just in case for future
				//if client needs more consistant cache it should also use this data storage, a client side version
				
				//should still run through whatever implements the interfaces i guess so they can get callbacks still?
				
				if (side == Side.CLIENT) {
					NBTDataManager.nbtDataFromServer(Packet.readNBTTagCompound(dis));
				} else {
					EntityPlayer entP = (EntityPlayer)player;
					NBTDataManager.nbtDataFromClient(entP.username, Packet.readNBTTagCompound(dis));
				}
				
			} else if (packet.channel.equals("NBTData_CONT")) {
				//interface locations:
				//client: open gui, server: open container
				
				//for client to server, use the username, get instance, then you can lookup the players openContainer and require an interface to give the container the callback
				//both client and server use same interface, different methods to receive, determined here
				
				//to use INBTPacketHandler with stuff other than containers/guis, add another channel and more rules in that code block to get those interfaces
				if (side == Side.CLIENT) {
					INBTPacketHandler nbtHandler = getClientDataInterface();
					if (nbtHandler != null) {
						nbtHandler.nbtDataFromServer(Packet.readNBTTagCompound(dis));
					}
				} else {
					EntityPlayer entP = (EntityPlayer)player;
					if (entP.openContainer instanceof INBTPacketHandler) {
						((INBTPacketHandler)entP.openContainer).nbtDataFromClient(entP.username, Packet.readNBTTagCompound(dis));
					}
				}
			} else if ("CoroAI_Ent".equals(packet.channel)) {
				if (side == Side.CLIENT) {
					NBTTagCompound data = Packet.readNBTTagCompound(dis);
					int entID = data.getInteger("entityID");//dis.readInt();
					
					Entity entity = getClientWorld().getEntityByID(entID);
					if (entity instanceof IEntityPacket) {
						((IEntityPacket) entity).handleNBTFromServer(data);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
