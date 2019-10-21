package CoroUtil.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.io.IOUtils;

public class CoroUtilFile {
	public static String lastWorldFolder = "";
    
	public static CompoundNBT getExtraWorldNBT(String fileName) {
		CompoundNBT data = new CompoundNBT();
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
	
	public static void setExtraWorldNBT(String fileName, CompoundNBT data) {
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
			lastWorldFolder = ((ServerWorld)world).getChunkSaveLocation().getName();
			return lastWorldFolder + File.separator;
		}
		
		return lastWorldFolder + File.separator;
	}
	
	public static String getSaveFolderPath() {
    	if (FMLCommonHandler.instance().getMinecraftServerInstance() == null || FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) {
    		return getClientSidePath() + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    	
    }
	
	public static String getMinecraftSaveFolderPath() {
    	if (FMLCommonHandler.instance().getMinecraftServerInstance() == null || FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) {
    		return getClientSidePath() + File.separator + "config" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator + "config" + File.separator;
    	}
    }
	
	public static String getWorldSaveFolderPath() {
    	if (FMLCommonHandler.instance().getMinecraftServerInstance() == null || FMLCommonHandler.instance().getMinecraftServerInstance().isSinglePlayer()) {
    		return getClientSidePath() + File.separator + "saves" + File.separator;
    	} else {
    		return new File(".").getAbsolutePath() + File.separator;
    	}
    }
    
    @OnlyIn(Dist.CLIENT)
	public static String getClientSidePath() {
		return FMLClientHandler.instance().getClient().mcDataDir/*getAppDir("minecraft")*/.getPath();
	}
    
    public static void writeCoords(String name, BlockCoord coords, CompoundNBT nbt) {
    	nbt.setInteger(name + "X", coords.posX);
    	nbt.setInteger(name + "Y", coords.posY);
    	nbt.setInteger(name + "Z", coords.posZ);
    }
    
    public static BlockCoord readCoords(String name, CompoundNBT nbt) {
    	if (nbt.hasKey(name + "X")) {
    		return new BlockCoord(nbt.getInteger(name + "X"), nbt.getInteger(name + "Y"), nbt.getInteger(name + "Z"));
    	} else {
    		return null;
    	}
    }

    @OnlyIn(Dist.CLIENT)
    public static String getContentsFromResourceLocation(ResourceLocation resourceLocation) {
		try {
			IResourceManager resourceManager = Minecraft.getMinecraft().entityRenderer.resourceManager;
			IResource iresource = resourceManager.getResource(resourceLocation);
			String contents = IOUtils.toString(iresource.getInputStream(), StandardCharsets.UTF_8);
			return contents;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}
}
