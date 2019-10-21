package modconfig.forge;

import modconfig.ConfigMod;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class PacketHelper {

    public static FMLProxyPacket getModConfigPacketMenu() {
    	CompoundNBT nbt = new CompoundNBT();
		nbt.putString("command", "openGUI");
    	return CoroUtil.packet.PacketHelper.getNBTPacket(nbt, ConfigMod.eventChannelName);
    }
    
    public static FMLProxyPacket getModConfigPacket(String modid) {
    	CompoundNBT nbt = new CompoundNBT();
    	
    	nbt.putString("command", "setData");
    	nbt.putString("modID", modid);
    	ConfigMod.populateData(modid);
    	
    	CompoundNBT nbtEntries = new CompoundNBT();
    	
    	for (int i = 0; i < ConfigMod.configLookup.get(modid).configData.size(); i++) {
    		CompoundNBT nbtEntry = new CompoundNBT();
    		nbtEntry.putString("name", (String)ConfigMod.configLookup.get(modid).configData.get(i).name);
    		nbtEntry.putString("value", String.valueOf(ConfigMod.configLookup.get(modid).configData.get(i).value));
    		nbtEntries.setTag("entry_" + i, nbtEntry);
    	}
    	
    	nbt.setTag("entries", nbtEntries);
    	
    	return CoroUtil.packet.PacketHelper.getNBTPacket(nbt, ConfigMod.eventChannelName);
    }
    
    public static FMLProxyPacket getModConfigPacketForClientToServer(String data) {
    	CompoundNBT nbt = new CompoundNBT();
    	
    	nbt.putString("command", "setData");
    	//nbt.putString("modID", modid);
    	nbt.putString("data", data);
    	//ConfigMod.populateData(modid);
    	
    	/*NBTTagCompound nbtEntries = new NBTTagCompound();
    	
    	for (int i = 0; i < ConfigMod.configLookup.get(modid).configData.size(); i++) {
    		NBTTagCompound nbtEntry = new NBTTagCompound();
    		nbtEntry.putString("name", (String)ConfigMod.configLookup.get(modid).configData.get(i).name);
    		nbtEntry.putString("value", String.valueOf(ConfigMod.configLookup.get(modid).configData.get(i).value));
    		nbtEntries.setTag("entry_" + i, nbtEntry);
    	}
    	
    	nbt.setTag("entries", nbtEntries);*/
    	
    	return CoroUtil.packet.PacketHelper.getNBTPacket(nbt, ConfigMod.eventChannelName);
    }
    
    //REVISE ME TO NBT AND SUB COMPOUND FOR ENTRIES and the entries themselves are nbt compounds
    /*public static Packet250CustomPayload getModConfigPacket(String modid, int count, List entries) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(Integer.SIZE + Character.SIZE * 64 * configLookup.get(modid).configData.size());
        DataOutputStream dos = new DataOutputStream(bos);

        try
        {
        	dos.writeInt(0);
        	dos.writeUTF(modid);
        	ConfigMod.populateData(modid);
        	dos.writeInt(configLookup.get(modid).configData.size());
        	for (int i = 0; i < configLookup.get(modid).configData.size(); i++) {
        		dos.writeUTF((String)configLookup.get(modid).configData.get(i).name);
        		dos.writeUTF(String.valueOf(configLookup.get(modid).configData.get(i).value));
        		//dos.writeUTF(configLookup.get(modid).configData.get(i).comment);
        	}
            
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        Packet250CustomPayload pkt = new Packet250CustomPayload();
        pkt.channel = "ModConfig";
        pkt.data = bos.toByteArray();
        pkt.length = bos.size();
        
        return pkt;
	}*/
    
}

