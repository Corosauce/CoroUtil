package CoroUtil.util;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class UtilClasspath {

    public static String getContentsFromResourceLocation(ResourceLocation resourceLocation) {
        try {
            //client side only way
            /*IResourceManager resourceManager = Minecraft.getMinecraft().getResourceManager();
            IResource iresource = resourceManager.getResource(resourceLocation);*/
            //server side compatible way
            String str = "assets/" + resourceLocation.toString().replace(":", "/");
            InputStream in = UtilClasspath.class.getClassLoader().getResourceAsStream(str);
            String contents = IOUtils.toString(in, StandardCharsets.UTF_8);
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
