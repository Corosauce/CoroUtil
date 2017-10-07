package CoroUtil.util;

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

    public static HashMap<Integer, Float> lookupPosToBrightness = new HashMap<>();
    public static HashMap<Integer, HashMap<Integer, HashMap<Integer, Float>>> lookupPosToBrightness2 = new HashMap<>();

    public static float brightnessPlayer = 0F;

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
            int hash;
            //very slow
            //hash = PathPoint.makeHash(xx, yy, zz);
            //slightly less slow
            hash = (xx + zz * 31) * 31 + yy;
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
                    float brightnesss = getBrightnessNonLightmap(world, x, y, z);
                    lookupPosToBrightness.put(hash, brightnesss + 0.001F);
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

}
