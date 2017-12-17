package CoroUtil.util;

import net.minecraft.client.Minecraft;
import net.minecraft.init.MobEffects;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.awt.*;
import java.util.HashMap;

/**
 * Created by corosus on 01/06/17.
 */
public class CoroUtilBlockLightCache {

    /**
     * For reference:
     * no light lookups = 145 fps
     * 3D coord hashed light lookups = 120 fps
     * triple hashmap lookups = 105 fps
     */

    public static HashMap<Long, Float> lookupPosToBrightness = new HashMap<>();
    public static HashMap<Integer, HashMap<Integer, HashMap<Integer, Float>>> lookupPosToBrightness2 = new HashMap<>();

    private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Z_BITS = NUM_X_BITS;
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
    private static final int Y_SHIFT = 0 + NUM_Z_BITS;
    private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
    private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

    public static float brightnessPlayer = 0F;

    public static Color[] lightmapColors = new Color[255];

    public static float getBrightnessCached(World world, float x, float y, float z) {
        //if (true) return 0.5F;

        boolean crazy = false;

        int xx = MathHelper.floor(x);
        int yy = MathHelper.floor(y);
        int zz = MathHelper.floor(z);

        if (crazy) {
            HashMap<Integer, HashMap<Integer, Float>> xxx = lookupPosToBrightness2.get(xx);
            HashMap<Integer, Float> yyy = null;
            if (xxx != null) {
                yyy = xxx.get(yy);
                if (yyy != null) {
                    Object brightness = yyy.get(zz);
                    if (brightness != null) {
                        return (Float) brightness;
                    }
                }
            }

            float brightnesss = getBrightnessNonLightmap(world, x, y, z);

            if (xxx == null) {
                xxx = new HashMap<>();
            }

            if (yyy == null) {
                yyy = new HashMap<>();
            }

            //lookupPosToBrightness.put(hash, brightnesss + 0.001F);
            yyy.put(zz, brightnesss);
            xxx.put(yy, yyy);
            lookupPosToBrightness2.put(xx, xxx);
            return brightnesss;
        } else {
            long hash;
            //very slow
            //hash = PathPoint.makeHash(xx, yy, zz);
            //slightly less slow
            //BlockPos.toLong, more accurate
            hash = ((long)xx & X_MASK) << X_SHIFT | ((long)yy & Y_MASK) << Y_SHIFT | ((long)zz & Z_MASK) << 0;
            //hash = (xx + zz * 31) * 31 + yy;
            //hmm, issues
            //hash = xx + (15 * yy) + (30 * zz);
            //inaccurate but fast?
            //hash = xx+yy+zz;
            /*int sum = x + z;
            return sum * (sum + 1)/2 + x;*/
            boolean containsWay = false;
            if (containsWay) {
                if (lookupPosToBrightness.containsKey(hash)) {
                    return lookupPosToBrightness.get(hash);
                } else {
                    float brightnesss = getBrightnessNonLightmap(world, x, y, z);
                    lookupPosToBrightness.put(hash, brightnesss + 0.001F);
                    return brightnesss;
                }
            } else {
                Object brightness = lookupPosToBrightness.get(hash);
                if (brightness != null) {
                    return (Float) brightness;
                } else {
                    float brightnesss = getBrightnessFromLightmap(world, x, y, z);
                    lookupPosToBrightness.put(hash, brightnesss/* + 0.001F*/);
                    return brightnesss;
                }
            }
        }
    }

    public static void clear() {
        lookupPosToBrightness.clear();
        lookupPosToBrightness2.clear();
    }

    public static float getBrightnessNonLightmap(World world, float x, float y, float z) {

        //cached in lightmap, wont be needed in future
        float brightnessSky = world.getSunBrightness(1F);

        float brightnessBlock = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, new BlockPos(x, y, z)) / 15F;

        float brightness = brightnessSky;
        if (brightnessBlock > brightnessSky) {
            brightness = brightnessBlock;
        }
        return brightness;

    }

    public static float getBrightnessFromLightmap(World world, float x, float y, float z) {


        BlockPos pos = new BlockPos(x, y, z);
        int i = world.getLightFromNeighborsFor(EnumSkyBlock.SKY, pos);
        int j = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos);

        int[] texData = Minecraft.getMinecraft().entityRenderer.lightmapTexture.getTextureData();

        //not sure if correct
        //just return what i think is just sky val
        int color = texData[(i * 16) + j];

        return color;

        /*int k = (int)(f8 * 255.0F);
        int l = (int)(f9 * 255.0F);
        int i1 = (int)(f10 * 255.0F);
        this.lightmapColors[i] = -16777216 | k << 16 | l << 8 | i1;*/

        /*int r = (int)(0.2F * 255.0F);
        int g = (int)(0.2F * 255.0F);
        int b = (int)(1F * 255.0F);
        return -16777216 | r << 16 | g << 8 | b;*/

        /*int r = (int)(1F * 255.0F);
        int g = (int)(1F * 255.0F);
        int b = (int)(1F * 255.0F);
        return -16777216 | r << 16 | g << 8 | b;*/

        /*int kk = lightmapColors[i] >> 16 & 255;
        int ll = lightmapColors[i] >> 8 & 255;
        int ii = lightmapColors[i] & 255;*/

        //cached in lightmap, wont be needed in future
        /*float brightnessSky = world.getSunBrightness(1F);

        float brightnessBlock = world.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, pos) / 15F;

        float brightness = brightnessSky;
        if (brightnessBlock > brightnessSky) {
            brightness = brightnessBlock;
        }
        return brightness;*/

    }

    public static void updateLightmap(float partialTicks)
    {
        World world = Minecraft.getMinecraft().world;

        if (world != null)
        {
            float f = world.getSunBrightness(1.0F);
            float f1 = f * 0.95F + 0.05F;

            for (int i = 0; i < 256; ++i)
            {
                float f2 = world.provider.getLightBrightnessTable()[i / 16] * f1;
                float f3 = world.provider.getLightBrightnessTable()[i % 16]/* * (this.torchFlickerX * 0.1F + 1.5F)*/;

                if (world.getLastLightningBolt() > 0)
                {
                    f2 = world.provider.getLightBrightnessTable()[i / 16];
                }

                float f4 = f2 * (f * 0.65F + 0.35F);
                float f5 = f2 * (f * 0.65F + 0.35F);
                float f6 = f3 * ((f3 * 0.6F + 0.4F) * 0.6F + 0.4F);
                float f7 = f3 * (f3 * f3 * 0.6F + 0.4F);
                float f8 = f4 + f3;
                float f9 = f5 + f6;
                float f10 = f2 + f7;
                f8 = f8 * 0.96F + 0.03F;
                f9 = f9 * 0.96F + 0.03F;
                f10 = f10 * 0.96F + 0.03F;

                /*if (this.bossColorModifier > 0.0F)
                {
                    float f11 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * partialTicks;
                    f8 = f8 * (1.0F - f11) + f8 * 0.7F * f11;
                    f9 = f9 * (1.0F - f11) + f9 * 0.6F * f11;
                    f10 = f10 * (1.0F - f11) + f10 * 0.6F * f11;
                }*/

                if (world.provider.getDimensionType().getId() == 1)
                {
                    f8 = 0.22F + f3 * 0.75F;
                    f9 = 0.28F + f6 * 0.75F;
                    f10 = 0.25F + f7 * 0.75F;
                }

                float[] colors = {f8, f9, f10};
                world.provider.getLightmapColors(partialTicks, f, f2, f3, colors);
                f8 = colors[0]; f9 = colors[1]; f10 = colors[2];

                // Forge: fix MC-58177
                f8 = MathHelper.clamp(f8, 0f, 1f);
                f9 = MathHelper.clamp(f9, 0f, 1f);
                f10 = MathHelper.clamp(f10, 0f, 1f);

                /*if (this.mc.player.isPotionActive(MobEffects.NIGHT_VISION))
                {
                    float f15 = this.getNightVisionBrightness(this.mc.player, partialTicks);
                    float f12 = 1.0F / f8;

                    if (f12 > 1.0F / f9)
                    {
                        f12 = 1.0F / f9;
                    }

                    if (f12 > 1.0F / f10)
                    {
                        f12 = 1.0F / f10;
                    }

                    f8 = f8 * (1.0F - f15) + f8 * f12 * f15;
                    f9 = f9 * (1.0F - f15) + f9 * f12 * f15;
                    f10 = f10 * (1.0F - f15) + f10 * f12 * f15;
                }*/

                if (f8 > 1.0F)
                {
                    f8 = 1.0F;
                }

                if (f9 > 1.0F)
                {
                    f9 = 1.0F;
                }

                if (f10 > 1.0F)
                {
                    f10 = 1.0F;
                }

                float f16 = Minecraft.getMinecraft().gameSettings.gammaSetting;
                float f17 = 1.0F - f8;
                float f13 = 1.0F - f9;
                float f14 = 1.0F - f10;
                f17 = 1.0F - f17 * f17 * f17 * f17;
                f13 = 1.0F - f13 * f13 * f13 * f13;
                f14 = 1.0F - f14 * f14 * f14 * f14;
                f8 = f8 * (1.0F - f16) + f17 * f16;
                f9 = f9 * (1.0F - f16) + f13 * f16;
                f10 = f10 * (1.0F - f16) + f14 * f16;
                f8 = f8 * 0.96F + 0.03F;
                f9 = f9 * 0.96F + 0.03F;
                f10 = f10 * 0.96F + 0.03F;

                if (f8 > 1.0F)
                {
                    f8 = 1.0F;
                }

                if (f9 > 1.0F)
                {
                    f9 = 1.0F;
                }

                if (f10 > 1.0F)
                {
                    f10 = 1.0F;
                }

                if (f8 < 0.0F)
                {
                    f8 = 0.0F;
                }

                if (f9 < 0.0F)
                {
                    f9 = 0.0F;
                }

                if (f10 < 0.0F)
                {
                    f10 = 0.0F;
                }

                int j = 255;
                int k = (int)(f8 * 255.0F);
                int l = (int)(f9 * 255.0F);
                int i1 = (int)(f10 * 255.0F);
                //lightmapColors[i] = -16777216 | k << 16 | l << 8 | i1;
                lightmapColors[i] = new Color(k, l, i1);
            }
        }
    }

}
