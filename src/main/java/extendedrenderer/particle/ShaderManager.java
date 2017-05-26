package extendedrenderer.particle;

/**
 * Created by corosus on 25/05/17.
 */
public class ShaderManager {

    private static boolean canUseShaders = true;

    public static void init() {
        /**
         * TODO: GLCaps shader tech check
         * - GLSL 3.1 (3.5?)
         * - instanced element rendering
         */
    }

    public static boolean canUseShaders() {
        return canUseShaders;
    }

}
