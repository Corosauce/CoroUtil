package CoroUtil.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.lang.reflect.Method;

public class CoroUtilCompatibility {

    private static boolean tanInstalled = false;
    private static boolean checkTAN = true;

    public static boolean shouldSnowAt(World world, BlockPos pos) {
        /**
         * ISeasonData data = SeasonHelper.getSeasonData(world);
         * boolean canSnow = SeasonASMHelper.canSnowAtInSeason(world, pos, false, data.getSeason());
         *
         * or:
         *
         * float temp = SeasonASMHelper.getFloatTemperature(world, pos);
         * boolean canSnow = temp <= 0;
         */
        return false;
    }

    public static float getAdjustedTemperature(World world, Biome biome, BlockPos pos) {

        //TODO: cache method/class reference, and maybe results in a blockpos,float hashmap for a second or 2
        if (isTANInstalled()) {
            try {
                Class clazz = Class.forName("toughasnails.season.SeasonASMHelper");
                Method method = clazz.getDeclaredMethod("getFloatTemperature", Biome.class, BlockPos.class);
                return (float)method.invoke(null, biome, pos);
                //ReflectionHelper.findMethod()
            } catch (Exception ex) {
                ex.printStackTrace();
                return biome.getFloatTemperature(pos);
            }
            //Method method = Clas
        } else {
            return biome.getFloatTemperature(pos);
        }
        //float temp = SeasonASMHelper.getFloatTemperature(biome, pos)


    }

    /**
     * Check if tough as nails is installed
     *
     * @return
     */
    public static boolean isTANInstalled() {
        if (checkTAN) {
            try {
                checkTAN = false;
                Class clazz = Class.forName("toughasnails.season.SeasonASMHelper");
                if (clazz != null) {
                    tanInstalled = true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return tanInstalled;
    }

}
