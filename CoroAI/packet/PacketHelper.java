package CoroAI.packet;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.WatchableObject;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.tileentity.TileEntity;
import CoroAI.ITilePacket;
import CoroAI.tile.TileDataWatcher;
import CoroAI.tile.TileHandler;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketHelper {

	@SideOnly(Side.CLIENT)
	public static void sendClientPacket(Packet packet) {
		FMLClientHandler.instance().getClient().thePlayer.sendQueue.addToSendQueue(packet);
	}
	
	public static void writeNBTTagCompound(NBTTagCompound par0NBTTagCompound, DataOutputStream par1DataOutputStream) throws IOException
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
    }

	public static void writeTEntToPacket(TileEntity tEnt, DataOutputStream dos) {
		try {
			dos.writeInt(tEnt.worldObj.provider.dimensionId);
	    	dos.writeInt(tEnt.xCoord);
	    	dos.writeInt(tEnt.yCoord);
	    	dos.writeInt(tEnt.zCoord);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//fail, so fail, need the bos object outside of this
	public static Packet250CustomPayload createPacketForTEnt(TileEntity tEnt) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		//pkt.channel = "CoroAI_TEntDW";
		//pkt.data = bos.toByteArray();
		//pkt.length = bos.size();
		return pkt;
	}
	
	public static Packet250CustomPayload createPacketForNBTHandler(String parChannel, NBTTagCompound parNBT) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		try {
			writeNBTTagCompound(parNBT, dos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = parChannel;
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}
	
	public static Packet250CustomPayload createPacketForTEntDWClient(TileEntity tEnt, String name, Object val) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		TileHandler tileHandler = ((ITilePacket)tEnt).getTileHandler();
		
		WatchableObject wo = new WatchableObject((Integer)tileHandler.tileDataWatcher.dataTypes.get(val.getClass()), tileHandler.mapNameToID.get(name), val);
		
		try {
			writeTEntToPacket(tEnt, dos);
			TileDataWatcher.writeWatchableObject(dos, wo);
			
			//this is the watchable object terminator indicator required for proper packet reading
			dos.writeByte(127);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "CoroAI_TEntDW";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}
	
	public static Packet250CustomPayload createPacketForTEntDWServer(TileEntity tEnt) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		
		TileDataWatcher tileDataWatcher = ((ITilePacket)tEnt).getTileHandler().tileDataWatcher;
		
		try {
			writeTEntToPacket(tEnt, dos);
			TileDataWatcher.writeObjectsInListToStream(tileDataWatcher.unwatchAndReturnAllWatched(), dos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Packet250CustomPayload pkt = new Packet250CustomPayload();
		pkt.channel = "CoroAI_TEntDW";
		pkt.data = bos.toByteArray();
		pkt.length = bos.size();
		pkt.isChunkDataPacket = false;
		return pkt;
	}
	
	public static Packet250CustomPayload createPacketForTEntCommand(TileEntity tEnt, NBTTagCompound data) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); //was ...(140);
        DataOutputStream dos = new DataOutputStream(bos);

        try
        {
        	dos.writeInt(tEnt.worldObj.provider.dimensionId);
        	dos.writeInt(tEnt.xCoord);
        	dos.writeInt(tEnt.yCoord);
        	dos.writeInt(tEnt.zCoord);
        	writeNBTTagCompound(data, dos);
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Packet250CustomPayload pkt = new Packet250CustomPayload();
        pkt.channel = "CoroAI_TEntCmd";
        pkt.data = bos.toByteArray();
        pkt.length = bos.size();
        
        return pkt;
	}
	
}
