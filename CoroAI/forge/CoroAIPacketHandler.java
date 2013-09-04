package CoroAI.forge;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroAI.ITilePacket;
import CoroAI.componentAI.ICoroAI;
import CoroAI.entity.c_EnhAI;
import CoroAI.tile.TileDataWatcher;
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
	
	@Override
	public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player player) {
		Side side = FMLCommonHandler.instance().getEffectiveSide();
		DataInputStream dis = new DataInputStream(new ByteArrayInputStream(packet.data));
		try {
			if ("CoroAI_Inv".equals(packet.channel)) {
				int entID = dis.readInt();
				ItemStack is = Packet.readItemStack(dis);
				
				Entity entity = getClientWorld().getEntityByID(entID);
				if (entity instanceof c_EnhAI) {
					((c_EnhAI) entity).inventory.mainInventory[0] = is;
					((c_EnhAI) entity).setCurrentSlot(0);
				} else if (entity instanceof ICoroAI) {
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
						((ITilePacket) tEnt).handleClientSentNBT(nbt);
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
							((ITilePacket) tEnt).handleClientSentDataWatcherList(dwList);
						}
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
}
