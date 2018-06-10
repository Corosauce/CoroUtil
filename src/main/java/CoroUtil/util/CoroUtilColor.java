package CoroUtil.util;

import java.awt.image.BufferedImage;

import CoroUtil.repack.de.androidpit.colorthief.ColorThief;
import extendedrenderer.foliage.FoliageData;
import it.unimi.dsi.fastutil.ints.IntArrays;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.common.property.IExtendedBlockState;

public class CoroUtilColor {
    
    @SuppressWarnings("null")
    public static int[] getColors(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            state = ((IExtendedBlockState) state).getClean();
        }
        IBakedModel model;

        //used when foliage shader is on
        if (FoliageData.backupBakedModelStore.containsKey(state)) {
            model = FoliageData.backupBakedModelStore.get(state);
        } else {
            model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(state);
        }

        if (model != null && !model.isBuiltInRenderer()) {
            TextureAtlasSprite sprite = model.getParticleTexture();
            if (sprite != null && sprite != Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
                return getColors(model.getParticleTexture());
            }
        }
        return IntArrays.EMPTY_ARRAY;
    }

    public static int[] getColors(TextureAtlasSprite sprite) {
        int width = sprite.getIconWidth();
        int height = sprite.getIconHeight();
        int frames = sprite.getFrameCount();
        
        BufferedImage img = new BufferedImage(width, height * frames, BufferedImage.TYPE_4BYTE_ABGR);
        for (int i = 0; i < frames; i++) {
            img.setRGB(0, i * height, width, height, sprite.getFrameTextureData(0)[0], 0, width);
        }
        
        int[][] colorData = ColorThief.getPalette(img, 6, 5, true);
        if (colorData != null) {
            int[] ret = new int[colorData.length];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = getColor(colorData[i]);
            }
            return ret;
        }
        return IntArrays.EMPTY_ARRAY;
    }
    
    private static int getColor(int[] colorData) {
        float mr = 1F;//((multiplier >>> 16) & 0xFF) / 255f;
        float mg = 1F;//((multiplier >>> 8) & 0xFF) / 255f;
        float mb = 1F;//(multiplier & 0xFF) / 255f;

        return 0xFF000000 | (((int) (colorData[0] * mr)) << 16) | (((int) (colorData[1] * mg)) << 8) | (int) (colorData[2] * mb);
    }

}
