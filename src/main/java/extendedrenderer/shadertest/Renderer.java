package extendedrenderer.shadertest;


import com.google.common.base.Charsets;
import com.google.common.io.Files;
import extendedrenderer.particle.ParticleMeshBufferManager;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.shadertest.gametest.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.util.glu.Project;
import org.lwjgl.util.vector.Vector4f;

import javax.vecmath.Matrix4f;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.file.Paths;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

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
        //shaderProgram.createUniform("modelViewMatrix");
        shaderProgram.createUniform("texture_sampler");
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(Window window, Camera camera, List<GameItem> gameItems) {
        //clear();

        //if (true) return;

        if (window != null && window.isResized()) {
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        shaderProgram.bind();

        Minecraft mc = Minecraft.getMinecraft();

        if (mc.theWorld == null || mc.thePlayer == null) return;

        //FOV = 90F;

        float fovwat = 99.000015F;
        float fov = 89.2F;
        fov = 90F;
        fov = mc.entityRenderer.getFOVModifier(mc.getRenderPartialTicks(), true);

        //mc.entityRenderer.debugView = false;

        Matrix4fe mat = new Matrix4fe();

        float aspectRatio = (float)mc.displayWidth / (float)mc.displayHeight;//(float) window.getWidth() / window.getHeight();
        //mat = transformation.getProjectionMatrix(fov, aspectRatio, Z_NEAR, Z_FAR * 1F);

        //get existing modelview matrix and set it in shader
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
        buf.rewind();
        Matrix4fe.get(mat, 0, buf);
        shaderProgram.setUniform("projectionMatrix", mat);

        Matrix4fe viewMatrix = transformation.getViewMatrix(camera);

        shaderProgram.setUniform("texture_sampler", 0);

        //testCode();

        float rotation = camera.getRotation().y + 1.5f;
        if ( rotation > 360 ) {
            rotation = 0;
        }

        if (mc.getRenderViewEntity() != null) {

            float posScale = 1.0F;

            camera.setPosition((float) mc.getRenderViewEntity().posX * posScale,
                    (float) -mc.getRenderViewEntity().posY/* * posScale*/,
                    (float) mc.getRenderViewEntity().posZ * posScale);

            //always 0 apparently, maybe for spectator mode
            float pitch = (float)mc.entityRenderer.cameraPitch;
            float yaw = (float)mc.entityRenderer.cameraYaw;

            pitch = mc.getRenderViewEntity().rotationPitch;
            yaw = mc.getRenderViewEntity().rotationYaw;

            camera.setRotation(pitch, yaw, 0);

            //camera.setRotation(0, 0, 0);

            //temp
            /*GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            Project.gluPerspective(fov, (float)mc.displayWidth / (float)mc.displayHeight, 0.05F, mc.entityRenderer.farPlaneDistance * MathHelper.SQRT_2);
            GlStateManager.matrixMode(5888);*/
        }

        //temp hack to test until redesign
        InstancedMesh mesh = null;

        mesh = ParticleMeshBufferManager.getMesh(ParticleRegistry.rain_white);

        if (mesh == null) {
            mesh = (InstancedMesh) gameItems.get(0).getMesh();
        }

        int i = 0;

        // Render each gameItem
        if (true) {
            for (GameItem gameItem : gameItems) {

                //mesh = (InstancedMesh) gameItem.getMesh();

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

                float zz = (float) Math.sin(Math.toRadians(((mc.theWorld.getTotalWorldTime()+(i*2)) * 3) % 360)) * 3F;

                //gameItem.setPosition((float)mc.thePlayer.posX, (float)mc.thePlayer.posY, (float)mc.thePlayer.posZ + (zz * 25) - 50);

                float wat = 1F;

                gameItem.setPosition(291F * wat, 78F, (4930F * wat)/* + (zz * 25) - 50*/);

                gameItem.setPosition(-0F * wat, -100F, (0F * wat)/* + (zz * 25) - 50*/);

                float x = (float) (10D - mc.getRenderManager().renderPosX);
                float y = (float) (100D - mc.getRenderManager().renderPosY);
                float z = (float) (10D - mc.getRenderManager().renderPosZ);

                gameItem.setPosition(x, y, z);

                gameItem.setPosition(-0, 0, 3);

                float xxx = (float) Math.sin(Math.PI * ((float) (i+zz*0.5F) * 0.02F)) * ((float) i * 0.022F);
                float zzz = (float) Math.cos(Math.PI * ((float) (i+zz*0.5F) * 0.02F)) * ((float) i * 0.022F);
                float yyy = zz;

                gameItem.setPosition(10 + xxx, 109 + yyy + (i * 0.02F), 0 + zzz);

                //gameItem.setPosition(10 + ((float)i*1), 109 + ((float)i*0), 0 + ((float)i*1));

                gameItem.setScale(3F);


                // Update rotation angle
                /*float rotation = gameItem.getRotation().z + 1.5f;
                if ( rotation > 360 ) {
                    rotation = 0;
                }
                gameItem.setRotation(rotation, rotation, rotation);*/

                /*gameItem.getRotation().x += 0.2F;
                gameItem.getRotation().z += 0.4F;
                gameItem.getRotation().y += 0.6F;*/

                gameItem.getRotation().setFromAxisAngle(new Vector4f(1, 0, 0, (float)Math.toRadians(45)));
                //gameItem.getRotation().setFromAxisAngle(new Vector4f(0, 0, 15, (float)Math.toRadians(15)));

                /*if (gameItem.getRotation().x > 360) {
                    gameItem.getRotation().x = 0;
                }

                if (gameItem.getRotation().y > 360) {
                    gameItem.getRotation().y = 0;
                }

                if (gameItem.getRotation().z > 360) {
                    gameItem.getRotation().z = 0;
                }*/

                //gameItem.setRotation(0, 0, 0);

                /*Matrix4fe matOffset = new Matrix4fe();
                matOffset.identity();
                matOffset.translate(0, 0, 0);*/

                //Matrix4fe modelViewMatrix = transformation.getModelViewMatrixMC(gameItem);
                //Matrix4fe modelViewMatrix = transformation.getModelViewMatrixOffset(gameItem, viewMatrix, matOffset);
                //shaderProgram.setUniform("modelViewMatrix", modelViewMatrix);
                // Render the mes for this game item

                //GL11.glCullFace(1);

                //gameItem.getMesh().render();

                i++;
            }
        }

        mesh.renderListInstanced(gameItems, transformation, viewMatrix);

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
