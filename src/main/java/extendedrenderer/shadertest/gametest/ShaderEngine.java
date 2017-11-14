package extendedrenderer.shadertest.gametest;

import com.google.common.base.Strings;
import extendedrenderer.shadertest.Renderer;

import java.io.File;
import java.lang.reflect.Field;

public class ShaderEngine {

    //public static DummyGame gameLogic;
    //public static GameEngine gameEngine;

    public static Renderer renderer;

    public static void init() {
        try {
            renderer = new Renderer();
            /*boolean vSync = true;
            gameLogic = new DummyGame();
            gameEngine = new GameEngine("GAME", 600, 480, vSync, gameLogic);*/
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }

    }

    public static boolean initUnthreaded() {
        try {
            init();
            //gameEngine.init();
            renderer.init();
            return true;
        } catch (Exception excp) {
            excp.printStackTrace();
            //System.exit(-1);
        }
        return false;
    }

    public static void cleanup() {
        renderer.cleanup();
    }
}