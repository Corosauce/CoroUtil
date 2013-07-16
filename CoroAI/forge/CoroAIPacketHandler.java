package CoroAI.forge;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import CoroAI.IPacketNBT;
import CoroAI.componentAI.ICoroAI;
import CoroAI.entity.c_EnhAI;
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
					if (entity instanceof EntityLiving) {
						((EntityLiving) entity).setCurrentItemOrArmor(0, is);
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
					if (tEnt instanceof IPacketNBT) {
						((IPacketNBT) tEnt).handleClientSentNBT(nbt);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
