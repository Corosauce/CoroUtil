package com.corosus.coroutil.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import java.lang.reflect.Method;

public class CoroUtilCompatibility {

    private static boolean sereneSeasonsInstalled = false;
    private static boolean checksereneSeasons = true;

    private static Class class_SereneSeasons_ASMHelper = null;
    private static Method method_sereneSeasons_getFloatTemperature = null;

    /**
     * Used to contain compat with other mods, still used incase i add that back in
     * @param ent
     * @param x
     * @param y
     * @param z
     * @param speed
     * @return
     */
    public static boolean tryPathToXYZModCompat(Mob ent, int x, int y, int z, double speed) {
        return tryPathToXYZVanilla(ent, x, y, z, speed);
    }

    public static boolean tryPathToXYZVanilla(Mob ent, int x, int y, int z, double speed) {
        return ent.getNavigation().moveTo(x, y, z, speed);
    }

    public static float getAdjustedTemperature(Level world, Biome biome, BlockPos pos) {
        if (isSereneSeasonsInstalled()) {
            try {
                if (method_sereneSeasons_getFloatTemperature == null) {
                    method_sereneSeasons_getFloatTemperature = class_SereneSeasons_ASMHelper.getDeclaredMethod("getBiomeTemperature", Level.class, Holder.class, BlockPos.class);
                }
                Holder<Biome> biomeHolder = world.getBiome(pos);
                return (float) method_sereneSeasons_getFloatTemperature.invoke(null, world, biomeHolder, pos);
            } catch (Exception ex) {
                ex.printStackTrace();
                //prevent error spam
                sereneSeasonsInstalled = false;
                return biome.getTemperature(pos);
            }
        } else {
            return biome.getTemperature(pos);
        }
    }

    /**
     * Check if Serene Seasons is installed
     *
     * @return
     */
    public static boolean isSereneSeasonsInstalled() {
        if (checksereneSeasons) {
            try {
                checksereneSeasons = false;
                class_SereneSeasons_ASMHelper = Class.forName("sereneseasons.season.SeasonHooks");
                if (class_SereneSeasons_ASMHelper != null) {
                    sereneSeasonsInstalled = true;
                }
            } catch (Exception ex) {
                //not installed
                //ex.printStackTrace();
            }

            CULog.log("CoroUtil detected Serene Seasons " + (sereneSeasonsInstalled ? "Installed" : "Not Installed") + " for use");
        }

        return sereneSeasonsInstalled;
    }

    public static boolean coldEnoughToSnow(Biome biome, BlockPos pos, Level levelReader) {
        return !warmEnoughToRain(biome, pos, levelReader);
    }

    public static boolean warmEnoughToRain(Biome biome, BlockPos pos, Level levelReader) {
        return getAdjustedTemperature(levelReader, biome, pos) >= 0.15F;
    }

}
