package CoroUtil.util;

import net.minecraft.client.Minecraft;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import net.minecraft.world.LightType;
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

        float brightnessBlock = world.getLightFromNeighborsFor(LightType.BLOCK, new BlockPos(x, y, z)) / 15F;

        float brightness = brightnessSky;
        if (brightnessBlock > brightnessSky) {
            brightness = brightnessBlock;
        }
        return brightness;

    }

    public static float getBrightnessFromLightmap(World world, float x, float y, float z) {

        BlockPos pos = new BlockPos(x, y, z);
        int i = world.getLightFromNeighborsFor(LightType.SKY, pos);
        int j = world.getLightFromNeighborsFor(LightType.BLOCK, pos);

        int[] texData = Minecraft.getMinecraft().entityRenderer.lightmapTexture.getTextureData();

        int color = texData[(i * 16) + j];

        return color;

    }

}
