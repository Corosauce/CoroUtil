package com.corosus.coroutil.util;

import com.corosus.modconfig.IConfigCategory;
import com.corosus.modconfig.ModConfigData;

public class MultiLoaderUtil {

    //thread safe eager initialization
    private static final MultiLoaderUtil instance = new MultiLoaderUtil();

    private boolean checkForge = true;
    private boolean isForge = false;

    private boolean checkFabric = true;
    private boolean isFabric = false;

    private MultiLoaderUtil() {

    }

    public static synchronized MultiLoaderUtil instance() {
        return instance;
    }

    public synchronized boolean isForge() {
        if (checkForge) {
            try {
                checkForge = false;
                //isForge = Class.forName("net.minecraftforge.fml.common.Mod") != null;
                isForge = Class.forName("com.corosus.coroutil.loader.forge.ConfigModForge") != null;
                if (isForge) {
                    CULog.log("forge loader environment detected");
                }
            } catch (ClassNotFoundException ex) {
                //CULog.log("no forge loader environment detected");
                //ex.printStackTrace();
                //loader not detected
            }
        }
        //for my build_dev.gradle that has both loaders classes present, might be best to check if fabric installed first, then forge, since dev uses fabric
        if (isForge && isFabric()) {
            CULog.err("ERROR: DETECTED FABRIC AND FORGE BOTH PRESENT, THIS MIGHT BREAK THIS LOGIC, should only happen when using build_dev.gradle");
        }
        return isForge;
    }

    public synchronized boolean isFabric() {
        if (checkFabric) {
            try {
                checkFabric = false;
                //isFabric = Class.forName("net.fabricmc.api.ModInitializer") != null;
                isFabric = Class.forName("com.corosus.coroutil.loader.fabric.ConfigModFabric") != null;
                if (isFabric) {
                    CULog.log("fabric loader environment detected");
                }
            } catch (ClassNotFoundException ex) {
                //CULog.log("no fabric loader environment detected");
                //ex.printStackTrace();
                //loader not detected
            }
        }
        return isFabric;
    }

    public synchronized ModConfigData makeLoaderSpecificConfigData(String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
        if (isFabric()) {
            return constructLoaderSpecificConfigData("com.corosus.coroutil.loader.fabric.ModConfigDataFabric", savePath, parStr, parClass, parConfig);
        } else if (isForge()) {
            return constructLoaderSpecificConfigData("com.corosus.coroutil.loader.forge.ModConfigDataForge", savePath, parStr, parClass, parConfig);
        }
        return null;
    }

    private ModConfigData constructLoaderSpecificConfigData(String clazz, String savePath, String parStr, Class parClass, IConfigCategory parConfig) {
        //CULog.dbg("constructing " + clazz);
        try {
            Class classToLoad = Class.forName(clazz);

            Class[] cArg = new Class[4];
            cArg[0] = String.class;
            cArg[1] = String.class;
            cArg[2] = Class.class;
            cArg[3] = IConfigCategory.class;

            return (ModConfigData) classToLoad.getDeclaredConstructor(cArg).newInstance(savePath, parStr, parClass, parConfig);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
