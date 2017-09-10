package CoroUtil.packet;

import CoroUtil.forge.CoroUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;

public class PacketHelper {

	//1.7 plan, for the existing packets, we add the old 'channel' as a command string, so we can use 1 event channel, then just convert existing code to buffer way

	//in handler, WE MUST MODIFY TO READ THE STRING FOR COMMAND!

	//dont forget to test tile datawatchers

	//modify to be fully nbt! less headache!

	@SideOnly(Side.CLIENT)
	public static void sendClientPacket(Packet packet) {
		FMLClientHandler.instance().getClient().player.connection.sendPacket(packet);
	}

	/*public static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutputStream par1DataOutputStream) throws IOException
    {
        if (par0NBTTagCompound == null)
        {
            par1DataOutputStream.writeShort(-1);
        }
        else
        {
            byte[] abyte = CompressedStreamTools.compress(par0NBTTagCompound);
            par1DataOutputStream.writeShort((short)abyte.length);
            par1DataOutputStream.write(abyte);
        }
    }*/

	public static void writeTEntToPacket(TileEntity tEnt, NBTTagCompound nbt) {
		try {
			nbt.setInteger("dimID", tEnt.getWorld().provider.getDimension());
			nbt.setInteger("x", tEnt.getPos().getX());
			nbt.setInteger("y", tEnt.getPos().getY());
			nbt.setInteger("z", tEnt.getPos().getZ());
			/*buff.writeInt(tEnt.getWorldObj().provider.dimensionId);
			buff.writeInt(tEnt.xCoord);
	    	buff.writeInt(tEnt.yCoord);
	    	buff.writeInt(tEnt.zCoord);*/
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static FMLProxyPacket createPacketForNBTHandler(String parChannel, String packetChannel, NBTTagCompound parNBT) {
		/*ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);*/

		ByteBuf byteBuf = Unpooled.buffer();

		try {
			ByteBufUtils.writeUTF8String(byteBuf, parChannel);
			//writeNBTTagCompound(parNBT, dos);
			ByteBufUtils.writeTag(byteBuf, parNBT);
		} catch (Exception e) {
			e.printStackTrace();
		}

		/*Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = parChannel;
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;*/
		return new FMLProxyPacket(new PacketBuffer(byteBuf), packetChannel/*CoroAI.eventChannelName*/);
	}

	public static FMLProxyPacket createPacketForTEntDWClient(TileEntity tEnt, String name, Object val) {
		/*ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);*/

		CoroUtil.dbg("createPacketForTEntDWClient incomplete");
		ByteBuf byteBuf = Unpooled.buffer();

		/*TileHandler tileHandler = ((ITilePacket)tEnt).getTileHandler();

		WatchableObject wo = new WatchableObject((Integer)tileHandler.tileDataWatcher.dataTypes.get(val.getClass()), tileHandler.mapNameToID.get(name), val);

		try {
			ByteBufUtils.writeUTF8String(byteBuf, "CoroAI_TEntDW");
			writeTEntToPacket(tEnt, byteBuf);
			TileDataWatcher.writeWatchableObject(byteBuf, wo);

			//this is the watchable object terminator indicator required for proper packet reading
			dos.writeByte(127);
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		/*Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "CoroAI_TEntDW";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;*/
		return new FMLProxyPacket(new PacketBuffer(byteBuf), CoroUtil.eventChannelName);
	}

	public static FMLProxyPacket createPacketForTEntDWServer(TileEntity tEnt) {
		/*ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);*/

		CoroUtil.dbg("createPacketForTEntDWServer incomplete");
		ByteBuf byteBuf = Unpooled.buffer();

		/*TileDataWatcher tileDataWatcher = ((ITilePacket)tEnt).getTileHandler().tileDataWatcher;

		try {
			ByteBufUtils.writeUTF8String(byteBuf, "CoroAI_TEntDW");
			writeTEntToPacket(tEnt, dos);
			TileDataWatcher.writeObjectsInListToStream(tileDataWatcher.unwatchAndReturnAllWatched(), dos);
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		/*Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "CoroAI_TEntDW";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;*/
		return new FMLProxyPacket(new PacketBuffer(byteBuf), CoroUtil.eventChannelName);
	}

	public static FMLProxyPacket createPacketForTEntCommand(TileEntity tEnt, NBTTagCompound data) {
		/*ByteArrayOutputStream bos = new ByteArrayOutputStream(); //was ...(140);
        DataOutputStream dos = new DataOutputStream(bos);*/

        ByteBuf byteBuf = Unpooled.buffer();
        NBTTagCompound nbtSendData = new NBTTagCompound();

        try
        {
        	nbtSendData.setString("command", "CoroAI_TEntCmd");
        	nbtSendData.setInteger("dimID", tEnt.getWorld().provider.getDimension());
        	nbtSendData.setInteger("x", tEnt.getPos().getX());
        	nbtSendData.setInteger("y", tEnt.getPos().getY());
        	nbtSendData.setInteger("z", tEnt.getPos().getZ());
        	nbtSendData.setTag("data", data);
        	//ByteBufUtils.writeUTF8String(byteBuf, "CoroAI_TEntCmd");
        	/*byteBuf.writeInt(tEnt.getWorldObj().provider.dimensionId);
        	byteBuf.writeInt(tEnt.xCoord);
        	byteBuf.writeInt(tEnt.yCoord);
        	byteBuf.writeInt(tEnt.zCoord);*/
        	ByteBufUtils.writeTag(byteBuf, nbtSendData);
        	//writeNBTTagCompound(data, dos);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        /*Packet250CustomPayload pkt = new Packet250CustomPayload();
        pkt.channel = "CoroAI_TEntCmd";
        pkt.data = bos.toByteArray();
        pkt.length = bos.size();*/

        return new FMLProxyPacket(new PacketBuffer(byteBuf), CoroUtil.eventChannelName);
	}

	public static NBTTagCompound readNBTTagCompound(ByteBuf fullBuffer) throws IOException
    {
		return ByteBufUtils.readTag(fullBuffer);
        /*short short1 = fullBuffer.readShort();//par0DataInput.readShort();

        if (short1 < 0)
        {
            return null;
        }
        else
        {
            byte[] abyte = new byte[short1];
            fullBuffer.readBytes(abyte);
            return CompressedStreamTools.func_152457_a(abyte, new NBTSizeTracker(2097152L));
            //return CompressedStreamTools.decompress(abyte);
        }*/
    }

	public static FMLProxyPacket getNBTPacket(NBTTagCompound parNBT, String parChannel) {
        ByteBuf byteBuf = Unpooled.buffer();

        try {
        	//byteBuf.writeBytes(CompressedStreamTools.compress(parNBT));
        	ByteBufUtils.writeTag(byteBuf, parNBT);
        } catch (Exception ex) {
        	ex.printStackTrace();
        }

        return new FMLProxyPacket(new PacketBuffer(byteBuf), parChannel);
    }

	public static FMLProxyPacket getPacketForRelativeMotion(Entity ent, double motionX, double motionY, double motionZ) {
		NBTTagCompound data = new NBTTagCompound();
		data.setString("command", "Ent_Motion");
		data.setInteger("entityID", ent.getEntityId());
		data.setDouble("motionX", motionX);
		data.setDouble("motionY", motionY);
		data.setDouble("motionZ", motionZ);
		return getNBTPacket(data, CoroUtil.eventChannelName);
	}

}
