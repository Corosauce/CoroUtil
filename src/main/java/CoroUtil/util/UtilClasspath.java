package CoroUtil.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UtilClasspath {

    public static String getContentsFromResourceLocation(ResourceLocation resourceLocation) {
        try {
            IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
            IResource iresource = resourceManager.getResource(resourceLocation);
            String contents = IOUtils.toString(iresource.getInputStream(), StandardCharsets.UTF_8);
            return contents;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return "";
    }


    public static NBTTagCompound getNBTFromResourceLocation(ResourceLocation resourceLocation) {
        try {
            URL url = UtilClasspath.class.getResource("/assets/" + resourceLocation.getResourceDomain() + "/" + resourceLocation.getResourcePath());
            NBTTagCompound nbttagcompound = CompressedStreamTools.readCompressed(url.openStream());
            return nbttagcompound;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
