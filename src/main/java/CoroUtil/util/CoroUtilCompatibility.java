package CoroUtil.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.lang.reflect.Method;

public class CoroUtilCompatibility {

    private static boolean tanInstalled = false;
    private static boolean checkTAN = true;

    private static boolean sereneSeasonsInstalled = false;
    private static boolean checksereneSeasons = true;

    private static Class class_TAN_ASMHelper = null;
    private static Method method_TAN_getFloatTemperature = null;

    private static Class class_SereneSeasons_ASMHelper = null;
    private static Method method_sereneSeasons_getFloatTemperature = null;

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

        //TODO: consider caching results in a blockpos,float hashmap for a second or 2
        if (isTANInstalled()) {
            try {
                if (method_TAN_getFloatTemperature == null) {
                    method_TAN_getFloatTemperature = class_TAN_ASMHelper.getDeclaredMethod("getFloatTemperature", Biome.class, BlockPos.class);
                }
                return (float) method_TAN_getFloatTemperature.invoke(null, biome, pos);
            } catch (Exception ex) {
                ex.printStackTrace();
                return biome.getFloatTemperature(pos);
            }
        } else if (isSereneSeasonsInstalled()) {
            try {
                if (method_sereneSeasons_getFloatTemperature == null) {
                    method_sereneSeasons_getFloatTemperature = class_SereneSeasons_ASMHelper.getDeclaredMethod("getFloatTemperature", Biome.class, BlockPos.class);
                }
                return (float) method_sereneSeasons_getFloatTemperature.invoke(null, biome, pos);
            } catch (Exception ex) {
                ex.printStackTrace();
                return biome.getFloatTemperature(pos);
            }
        } else {
            return biome.getFloatTemperature(pos);
        }
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
                class_TAN_ASMHelper = Class.forName("toughasnails.season.SeasonASMHelper");
                if (class_TAN_ASMHelper != null) {
                    tanInstalled = true;
                }
            } catch (Exception ex) {
                //not installed
                //ex.printStackTrace();
            }
        }

        return tanInstalled;
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
                class_SereneSeasons_ASMHelper = Class.forName("sereneseasons.season.SeasonASMHelper");
                if (class_SereneSeasons_ASMHelper != null) {
                    sereneSeasonsInstalled = true;
                }
            } catch (Exception ex) {
                //not installed
                //ex.printStackTrace();
            }
        }

        return sereneSeasonsInstalled;
    }

    /**
     * power in / out with simulates:
     * EIO:
     * - ILegacyPowerReceiver extends ILegacyPoweredTile
     * -- int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate);
     *
     * CoFH:
     * - IEnergyStorage
     * -- int receiveEnergy(int maxReceive, boolean simulate);
     * -- int extractEnergy(int maxExtract, boolean simulate);
     *
     * storage:
     *
     * Forge:
     * net.minecraftforge.energy.IEnergyStorage
     * - the cap, also uses that
     * EIO uses it, cofh too:
     * - https://github.com/CoFH/ThermalExpansion/blob/5340e2df6e328a1cc140a8b94692c8d09c5f06a7/src/main/java/cofh/thermalexpansion/block/TilePowered.java#L194
     *
     *
     * EIO:
     * - crazypants.enderio.base.power.ILegacyPoweredTile
     * -- int getEnergyStored();
     * - crazypants.enderio.base.machine.gui.IPowerBarData
     * -- int getEnergyStored();
     *
     * CoFH:
     * - cofh.redstoneflux.api.IEnergyStorage
     * -- int getEnergyStored();
     *
     *
     * Best idea: track relative power storage changes
     * - while EIO and CoFH/RF has a simulate boolean for extractEnergy (CoFH)
     *
     */

    public static int lastPowerVal = -1;

    public static void testPowerInfo(EntityPlayer player, BlockPos pos) {
        TileEntity tEnt = player.world.getTileEntity(pos);
        if (tEnt != null) {

            int value = -1;

            IEnergyStorage cap = tEnt.getCapability(CapabilityEnergy.ENERGY, null);

            if (cap != null) {
                value = cap.getEnergyStored();
                player.sendMessage(new TextComponentString("cap power stored: " + value));
                if (lastPowerVal != -1) {
                    player.sendMessage(new TextComponentString("relative power change: " + (value - lastPowerVal)));
                }
                lastPowerVal = value;
            } else {
                try {
                    boolean success = true;
                    Class classTry = Class.forName("crazypants.enderio.base.power.ILegacyPoweredTile");
                    if (classTry == null) {
                        player.sendMessage(new TextComponentString("EIO class not found, trying cofh"));
                        classTry = Class.forName("cofh.redstoneflux.api.IEnergyStorage");
                        if (classTry == null) {
                            player.sendMessage(new TextComponentString("cofh class not found"));
                        }
                    }

                    if (classTry != null) {
                        Method method = classTry.getDeclaredMethod("getEnergyStored");
                        if (method != null) {
                            value = (int) method.invoke(tEnt);
                        } else {
                            player.sendMessage(new TextComponentString("method not found"));
                            success = false;
                        }
                    } else {
                        success = false;
                    }

                    if (success) {
                        player.sendMessage(new TextComponentString("non cap power stored: " + value));
                        if (lastPowerVal != -1) {
                            player.sendMessage(new TextComponentString("relative power change: " + (value - lastPowerVal)));
                        }
                        lastPowerVal = value;
                    }


                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }


        }
    }

}
