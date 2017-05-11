package extendedrenderer.shadertest;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import extendedrenderer.shadertest.gametest.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Matrix4f;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer {

    private static final float FOV = (float) Math.toRadians(60.0f);

    private static final float Z_NEAR = 0.01f;

    private static final float Z_FAR = 1000.f;

    private ShaderProgram shaderProgram;

    private Transformation transformation;

    public Renderer() {
        transformation = new Transformation();
    }

    public void init() throws Exception {
        shaderProgram = new ShaderProgram();

        String folderShaders = "/mnt/e/git/CoroUtil_1.10.2/src/main/resources/assets/coroutil/shaders/";
        String vertex = Files.toString(new File(folderShaders + "/" + "vertex.vs"), Charsets.UTF_8);
        String fragment = Files.toString(new File(folderShaders + "/" + "fragment.fs"), Charsets.UTF_8);

        /*String vertex = "#version 120\n" +
                "\n" +
                "in vec3 position;\n" +
                "uniform mat4 projectionMatrix;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "\tgl_Position = vec4(position, 1.0);\n" +
                "}";
        String fragment = "#version 120\n" +
                "\n" +
                "varying out vec4 fragColor;\n" +
                "\n" +
                "void main()\n" +
                "{\n" +
                "\tfragColor = vec4(0.0, 0.5, 0.5, 1.0);\n" +
                "}";*/
        shaderProgram.createVertexShader(vertex);
        shaderProgram.createFragmentShader(fragment);
        shaderProgram.link();

        shaderProgram.createUniform("projectionMatrix");
        shaderProgram.createUniform("worldMatrix");
        shaderProgram.createUniform("texture_sampler");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, GameItem[] gameItems) {
        //clear();

        //if (true) return;

        if (window != null && window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        Minecraft mc = Minecraft.getMinecraft();

        float aspectRatio = (float)mc.displayWidth / (float)mc.displayHeight;//(float) window.getWidth() / window.getHeight();
        Matrix4fe projectionMatrix = transformation.getProjectionMatrix(FOV, aspectRatio, Z_NEAR, Z_FAR);
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        shaderProgram.setUniform("texture_sampler", 0);

        //testCode();

        // Render each gameItem
        for(GameItem gameItem : gameItems) {

            /*Vector3f itemPos = gameItem.getPosition();
            float posx = itemPos.x + displxInc * 0.01f;
            float posy = itemPos.y + displyInc * 0.01f;
            float posz = itemPos.z + displzInc * 0.01f;
            gameItem.setPosition(posx, posy, posz);

            // Update scale
            float scale = gameItem.getScale();
            scale += scaleInc * 0.05f;
            if ( scale < 0 ) {
                scale = 0;
            }
            gameItem.setScale(scale);*/

            float zz = (float)Math.sin(Math.toRadians((mc.theWorld.getTotalWorldTime() * 5) % 360));

            gameItem.setPosition(0, 0, (zz * 25) - 50);

            gameItem.setScale(10F);

            // Update rotation angle
            float rotation = gameItem.getRotation().z + 1.5f;
            if ( rotation > 360 ) {
                rotation = 0;
            }
            gameItem.setRotation(0, 0, rotation);

            // Set world matrix for this item
            Matrix4fe worldMatrix = transformation.getWorldMatrix(
                    gameItem.getPosition(),
                    gameItem.getRotation(),
                    gameItem.getScale());
            shaderProgram.setUniform("worldMatrix", worldMatrix);
            // Render the mes for this game item
            gameItem.getMesh().render();
        }

        shaderProgram.unbind();
    }

    public void cleanup() {
        if (shaderProgram != null) {
            shaderProgram.cleanup();
        }


    }

    public static void testCode() {
        //1
        float FOV = (float)Math.toRadians(60F);
        float z_near = 0.01F;
        float z_far = 1000F;
        float aspectRatio = 600F / 480F;//(float) window.getWidth() / window.getHeight();
        Matrix4fe mat = new Matrix4fe().setPerspective(FOV, aspectRatio, z_near, z_far);

        //2
        /*Matrix4fe mat2 = new Matrix4fe();
        FloatBuffer fb = BufferUtils.createFloatBuffer(16);
        mat2.get(fb);
        GL20.glUniformMatrix4(uniforms.get(uniformName), false, fb);*/

        /*try (MemoryStack stack = MemoryStack.stackPush()) {
            // Dump the matrix into a float buffer
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            GL20.glUniformMatrix4(uniforms.get(uniformName), false, fb);
        }*/
    }
}
