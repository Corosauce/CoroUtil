package extendedrenderer.shader;

import extendedrenderer.shader.Renderer;

public class ShaderEngine {

    public static Renderer renderer;

    public static boolean init() {
        try {
            renderer = new Renderer();
            renderer.init();
            return true;
        } catch (Exception excp) {
            excp.printStackTrace();
        }
        return false;

    }

    public static void cleanup() {
        renderer.cleanup();
    }
}