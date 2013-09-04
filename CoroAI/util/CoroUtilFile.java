package CoroAI.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumOS;
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
    
    //this is here just in case, new best way is now Minecraft.mcDataDir
    public static File getAppDir(String par0Str)
    {
        String s1 = System.getProperty("user.home", ".");
        File file1;

        switch (EnumOSHelper.field_90049_a[getOs().ordinal()])
        {
            case 1:
            case 2:
                file1 = new File(s1, '.' + par0Str + '/');
                break;
            case 3:
                String s2 = System.getenv("APPDATA");

                if (s2 != null)
                {
                    file1 = new File(s2, "." + par0Str + '/');
                }
                else
                {
                    file1 = new File(s1, '.' + par0Str + '/');
                }

                break;
            case 4:
                file1 = new File(s1, "Library/Application Support/" + par0Str);
                break;
            default:
                file1 = new File(s1, par0Str + '/');
        }

        if (!file1.exists() && !file1.mkdirs())
        {
            throw new RuntimeException("The working directory could not be created: " + file1);
        }
        else
        {
            return file1;
        }
    }

    public static EnumOS getOs()
    {
        String s = System.getProperty("os.name").toLowerCase();
        return s.contains("win") ? EnumOS.WINDOWS : (s.contains("mac") ? EnumOS.MACOS : (s.contains("solaris") ? EnumOS.SOLARIS : (s.contains("sunos") ? EnumOS.SOLARIS : (s.contains("linux") ? EnumOS.LINUX : (s.contains("unix") ? EnumOS.LINUX : EnumOS.UNKNOWN)))));
    }
}
