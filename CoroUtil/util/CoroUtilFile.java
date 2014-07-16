package CoroUtil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CoroUtilFile {
	public static String lastWorldFolder = "";
    
	public static NBTTagCompound getExtraWorldNBT(String fileName) {
		NBTTagCompound data = new NBTTagCompound();
		//try load
		
		String saveFolder = getWorldSaveFolderPath() + getWorldFolderName();
		
		if ((new File(saveFolder + fileName)).exists()) {
			try {
				data = CompressedStreamTools.readCompressed(new FileInputStream(saveFolder + fileName));
			} catch (Exception ex) {
				System.out.println("CoroUtilFile: getExtraWorldNBT: Error loading " + saveFolder + fileName);
			}
			
			//NBTTagList var14 = gameData.getTagList("playerData");
		}
		
		return data;
	}
	
	public static void setExtraWorldNBT(String fileName, NBTTagCompound data) {
		try {
    		
    		String saveFolder = getWorldSaveFolderPath() + getWorldFolderName();
    		
    		//Write out to file
    		FileOutputStream fos = new FileOutputStream(saveFolder + fileName);
	    	CompressedStreamTools.writeCompressed(data, fos);
	    	fos.close();
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	//this must be used while server is active
    public static String getWorldFolderName() {
		World world = DimensionManager.getWorld(0);
		
		if (world != null) {
			lastWorldFolder = ((WorldServer)world).getChunkSaveLocation().getName();
			return lastWorldFolder + File.separator;
		}
		
		return lastWorldFolder + File.separator;
	}
	
	public static String getSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
	
	public static String getMinecraftSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator + "config" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator + "config" + File.separator;
    	}
    }
	
	public static String getWorldSaveFolderPath() {
    	if (MinecraftServer.getServer() == null || MinecraftServer.getServer().isSinglePlayer()) {
    		return getClientSidePath() + File.separator + "saves" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    }
    
    @SideOnly(Side.CLIENT)
	public static String getClientSidePath() {
		return FMLClientHandler.instance().getClient().mcDataDir/*getAppDir("minecraft")*/.getPath();
	}
    
    public static void writeCoords(String name, ChunkCoordinates coords, NBTTagCompound nbt) {
    	nbt.setInteger(name + "X", coords.posX);
    	nbt.setInteger(name + "Y", coords.posY);
    	nbt.setInteger(name + "Z", coords.posZ);
    }
    
    public static ChunkCoordinates readCoords(String name, NBTTagCompound nbt) {
    	if (nbt.hasKey(name + "X")) {
    		return new ChunkCoordinates(nbt.getInteger(name + "X"), nbt.getInteger(name + "Y"), nbt.getInteger(name + "Z"));
    	} else {
    		return null;
    	}
    }
}
