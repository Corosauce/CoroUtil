package extendedrenderer.shadertest;


import CoroUtil.forge.CoroUtil;
import CoroUtil.util.CoroUtilFile;
import extendedrenderer.shadertest.gametest.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class Renderer {

    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;

    public ShaderProgram shaderProgram;

    public Transformation transformation;

    public Renderer() {
        transformation = new Transformation();
    }

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();

        //String folderShaders = "/mnt/e/git/CoroUtil_1.10.2/src/main/resources/assets/coroutil/shaders/";
        String vertex = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, "shaders/particle.vs"));
        String fragment = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, "shaders/particle.fs"));

        shaderProgram.createVertexShader(vertex);
        shaderProgram.createFragmentShader(fragment);
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");
        //shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }
    }
}
