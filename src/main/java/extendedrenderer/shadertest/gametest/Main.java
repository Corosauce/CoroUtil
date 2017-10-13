package extendedrenderer.shadertest.gametest;

import com.google.common.base.Strings;

import java.io.File;
import java.lang.reflect.Field;

public class Main {

    public static DummyGame gameLogic;
    public static GameEngine gameEngine;
 
    public static void main(String[] args) {
        try {

            hackNatives();

            init();
            gameEngine.start();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     *
     * Pointless to have and use unless i actually make the old window wrapper work
     *
     */
    private static void hackNatives()
    {
        String paths = System.getProperty("java.library.path");
        String nativesDir = "/home/corosus/.gradle/caches/minecraft/net/minecraft/natives/1.10.2";

        if (Strings.isNullOrEmpty(paths))
            paths = nativesDir;
        else
            paths += File.pathSeparator + nativesDir;

        System.setProperty("java.library.path", paths);

        // hack the classloader now.
        try
        {
            final Field sysPathsField = ClassLoader.class.getDeclaredField("sys_paths");
            sysPathsField.setAccessible(true);
            sysPathsField.set(null, null);
        }
        catch(Throwable t) {};
    }

    public static void init() {
        try {
            boolean vSync = true;
            gameLogic = new DummyGame();
            gameEngine = new GameEngine("GAME", 600, 480, vSync, gameLogic);
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }

    }

    public static boolean initUnthreaded() {
        try {
            init();
            gameEngine.init();
            return true;
        } catch (Exception excp) {
            excp.printStackTrace();
            //System.exit(-1);
        }
        return false;
    }
}