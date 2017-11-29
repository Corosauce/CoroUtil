package extendedrenderer.shader;


import CoroUtil.forge.CoroUtil;
import CoroUtil.util.CoroUtilFile;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;

public class Renderer {

    //public ShaderProgram shaderProgram;
    private HashMap<String, ShaderProgram> lookupNameToProgram = new HashMap<>();

    //might be worth relocating
    public Transformation transformation;

    public Renderer() {
        transformation = new Transformation();
    }

    public void init() throws Exception {
        ShaderProgram shaderProgram = new ShaderProgramParticle("particle");

        //String folderShaders = "/mnt/e/git/CoroUtil_1.10.2/src/main/resources/assets/coroutil/shaders/";
        String vertex = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, "shaders/particle.vs"));
        String fragment = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, "shaders/particle.fs"));

        shaderProgram.createVertexShader(vertex);
        shaderProgram.createFragmentShader(fragment);
        shaderProgram.link();

        //shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrixCamera");
        //shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
        //0 = linear, 1 = exp, 2 = exp2
        shaderProgram.createUniform("fogmode");

        lookupNameToProgram.put(shaderProgram.getName(), shaderProgram);

        shaderProgram = new ShaderProgramFoliage("foliage");

        //String folderShaders = "/mnt/e/git/CoroUtil_1.10.2/src/main/resources/assets/coroutil/shaders/";
        vertex = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, "shaders/foliage.vs"));
        fragment = CoroUtilFile.getContentsFromResourceLocation(new ResourceLocation(CoroUtil.modID, "shaders/foliage.fs"));

        shaderProgram.createVertexShader(vertex);
        shaderProgram.createFragmentShader(fragment);
        shaderProgram.link();

        //shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("modelViewMatrixCamera");
        //shaderProgram.createUniform("modelViewMatrixClassic");
        //shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
        shaderProgram.createUniform("time");
        shaderProgram.createUniform("partialTick");
        shaderProgram.createUniform("windDir");
        shaderProgram.createUniform("windSpeed");
        shaderProgram.createUniform("fogmode");
        shaderProgram.createUniform("stipple");

        lookupNameToProgram.put(shaderProgram.getName(), shaderProgram);
    }

    public void clear() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    public void cleanup() {
        for (ShaderProgram shaderProgram : lookupNameToProgram.values()) {
            shaderProgram.cleanup();
        }
        lookupNameToProgram.clear();
    }

    public ShaderProgram getShaderProgram(String name) {
        return lookupNameToProgram.get(name);
    }
}
