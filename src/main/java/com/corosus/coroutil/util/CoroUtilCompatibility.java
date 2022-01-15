package com.corosus.coroutil.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

public class CoroUtilCompatibility {

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
        //TODO: serene seasons compat again
        return biome.getTemperature(pos);
    }

}
