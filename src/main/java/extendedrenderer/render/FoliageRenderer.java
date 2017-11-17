package extendedrenderer.render;

import CoroUtil.config.ConfigCoroAI;
import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.foliage.Foliage;
import extendedrenderer.foliage.ParticleTallGrassTemp;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.shader.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

public class FoliageRenderer {

    private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");

    private final TextureManager renderer;

    public static FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    public Transformation transformation;

    public boolean needsUpdate = true;

    public FoliageRenderer(TextureManager rendererIn) {
        this.renderer = rendererIn;
        transformation = new Transformation();
    }

    public void render(Entity entityIn, float partialTicks)
    {

        RotatingParticleManager.useShaders = ShaderManager.canUseShadersInstancedRendering();

        if (ConfigCoroAI.forceShadersOff) {
            RotatingParticleManager.useShaders = false;
        }

        if (RotatingParticleManager.forceShaderReset) {
            RotatingParticleManager.forceShaderReset = false;
            ShaderEngine.cleanup();
            ShaderEngine.renderer = null;
            needsUpdate = true;
            ShaderManager.resetCheck();
        }

        if (RotatingParticleManager.useShaders && ShaderEngine.renderer == null) {
            //currently for if shader compiling fails, which is an ongoing issue for some machines...
            if (!ShaderEngine.init()) {
                ShaderManager.disableShaders();
                RotatingParticleManager.useShaders = false;
            } else {
                System.out.println("Extended Renderer: Initialized instanced rendering shaders");
            }
        }

        if (RotatingParticleManager.useShaders) {

            Minecraft mc = Minecraft.getMinecraft();
            EntityRenderer er = mc.entityRenderer;
            World world = mc.world;

            Foliage.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double)partialTicks;
            Foliage.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double)partialTicks;
            Foliage.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double)partialTicks;

            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            GlStateManager.alphaFunc(516, 0.003921569F);

            GlStateManager.disableCull();

            GlStateManager.depthMask(true);

            renderJustShaders(entityIn, partialTicks);

            GlStateManager.enableCull();

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
        }
    }

    public void renderJustShaders(Entity entityIn, float partialTicks)
    {

        Minecraft mc = Minecraft.getMinecraft();
        EntityRenderer er = mc.entityRenderer;
        World world = mc.world;

        Matrix4fe projectionMatrix = new Matrix4fe();
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
        buf.rewind();
        Matrix4fe.get(projectionMatrix, 0, buf);

        //modify far distance, 4x as far
        boolean distantRendering = true;
        if (distantRendering) {
            float zNear = 0.05F;
            float zFar = (float) (mc.gameSettings.renderDistanceChunks * 16) * 4F;
            projectionMatrix.m22 = ((zFar + zNear) / (zNear - zFar));
            projectionMatrix.m32 = ((zFar + zFar) * zNear / (zNear - zFar));
        }

        //testing determined i can save frames by baking projectionMatrix into modelViewMatrixCamera, might have to revert for more complex shaders
        //further testing its just barely faster, if at all...
        //shaderProgram.setUniform("projectionMatrix", mat);
        //shaderProgram.setUniformEfficient("projectionMatrix", mat, projectionMatrixBuffer);

        Matrix4fe viewMatrix = new Matrix4fe();
        FloatBuffer buf2 = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf2);
        buf2.rewind();
        Matrix4fe.get(viewMatrix, 0, buf2);

        //m03, m13, m23 ?
        //30 31 32
        //bad context to do in i think...
        /*viewMatrix.m30 = (float) entityIn.posX;
        viewMatrix.m31 = (float) entityIn.posY;
        viewMatrix.m32 = (float) entityIn.posZ;*/

        Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);

        ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("foliage");
        //transformation = ShaderEngine.renderer.transformation;
        shaderProgram.bind();

        //fix for camera model matrix being 0 0 0 on positions, readjusts to world positions for static rendering instanced meshes
        float interpX = (float)(entityIn.prevPosX + (entityIn.posX - entityIn.prevPosX) * partialTicks);
        float interpY = (float)(entityIn.prevPosY + (entityIn.posY - entityIn.prevPosY) * partialTicks);
        float interpZ = (float)(entityIn.prevPosZ + (entityIn.posZ - entityIn.prevPosZ) * partialTicks);
        Matrix4fe matrixFix = new Matrix4fe();
        matrixFix = matrixFix.translationRotateScale(
                -interpX, -interpY, -interpZ,
                0, 0, 0, 1,
                1, 1, 1);
        matrixFix = modelViewMatrix.mul(matrixFix);

        shaderProgram.setUniformEfficient("modelViewMatrixCamera", matrixFix, viewMatrixBuffer);

        shaderProgram.setUniform("texture_sampler", 0);

        shaderProgram.setUniform("time", (int)world.getTotalWorldTime());

        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.tallgrass);
        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(ParticleRegistry.tallgrass);

        mesh.initRender();
        mesh.initRenderVBO1();
        mesh.initRenderVBO2();

        //also resets position

        boolean skipUpdate = false;

        if (!skipUpdate || needsUpdate) {
            mesh.instanceDataBuffer.clear();
            mesh.instanceDataBufferSeldom.clear();
            mesh.curBufferPos = 0;
        }

        BlockPos pos = entityIn.getPosition();

        List<Foliage> listFoliage = new ArrayList<>();

        Random rand = new Random();
        rand.setSeed(5);
        int range = 150;

        int amount = 35000;
        int adjAmount = amount;

        boolean subTest = true;

        if (subTest) {
            adjAmount = 50;
        }

        //make obj
        if (!skipUpdate || needsUpdate) {
            for (int i = 0; i < adjAmount; i++) {
                Foliage foliage = new Foliage();
                int randX = rand.nextInt(range) - range / 2;
                int randY = 0;//rand.nextInt(range) - range / 2;
                int randZ = rand.nextInt(range) - range / 2;
                foliage.setPosition(new BlockPos(pos).up(0).add(randX, randY, randZ));
                foliage.posY += 0.5F;
                foliage.prevPosY = foliage.posY;
                foliage.posX += rand.nextFloat();
                foliage.prevPosX = foliage.posX;
                foliage.posZ += rand.nextFloat();
                foliage.prevPosZ = foliage.posZ;
                foliage.rotationYaw = rand.nextInt(360);
                //foliage.rotationPitch = rand.nextInt(90) - 45;
                foliage.particleScale /= 0.2;
                listFoliage.add(foliage);
            }

            for (Foliage foliage : listFoliage) {
                foliage.updateQuaternion(entityIn);

                //update vbo1
                foliage.renderForShaderVBO1(mesh, transformation, viewMatrix, entityIn, partialTicks);
            }

            if (!subTest) {
                mesh.instanceDataBuffer.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS);
            } else {
                mesh.instanceDataBuffer.limit(adjAmount * mesh.INSTANCE_SIZE_FLOATS);
            }

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO);

            if (!subTest) {
                ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);
            } else {
                GL15.glBufferSubData(GL_ARRAY_BUFFER, 0, mesh.instanceDataBuffer);
            }

            mesh.curBufferPos = 0;

            for (Foliage foliage : listFoliage) {

                //update vbo2
                foliage.renderForShaderVBO2(mesh, transformation, viewMatrix, entityIn, partialTicks);
            }

            //wasnt used in particle renderer and even crashes it :o
            if (!subTest) {
                mesh.instanceDataBufferSeldom.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS_SELDOM);
            } else {
                mesh.instanceDataBufferSeldom.limit(adjAmount * mesh.INSTANCE_SIZE_FLOATS_SELDOM);
            }

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBOSeldom);

            if (!subTest) {
                ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferSeldom, GL_DYNAMIC_DRAW);
            } else {
                GL15.glBufferSubData(GL_ARRAY_BUFFER, 0, mesh.instanceDataBufferSeldom);
            }
        }

        needsUpdate = false;

        ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0, amount);

        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, 0);

        mesh.endRenderVBO1();
        mesh.endRenderVBO2();
        mesh.endRender();

        ShaderEngine.renderer.getShaderProgram("foliage").unbind();

        /*//Transformation transformation = null;
        Matrix4fe viewMatrix = null;

        ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("foliage");
        //transformation = ShaderEngine.renderer.transformation;
        shaderProgram.bind();
        Matrix4fe projectionMatrix = new Matrix4fe();
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
        buf.rewind();
        Matrix4fe.get(projectionMatrix, 0, buf);

        //modify far distance, 4x as far
        boolean distantRendering = true;
        if (distantRendering) {
            float zNear = 0.05F;
            float zFar = (float) (mc.gameSettings.renderDistanceChunks * 16) * 4F;
            projectionMatrix.m22 = ((zFar + zNear) / (zNear - zFar));
            projectionMatrix.m32 = ((zFar + zFar) * zNear / (zNear - zFar));
        }

        //testing determined i can save frames by baking projectionMatrix into modelViewMatrixCamera, might have to revert for more complex shaders
        //further testing its just barely faster, if at all...
        //shaderProgram.setUniform("projectionMatrix", mat);
        //shaderProgram.setUniformEfficient("projectionMatrix", mat, projectionMatrixBuffer);

        boolean alternateCameraCapture = true;
        if (alternateCameraCapture) {
            viewMatrix = new Matrix4fe();
            FloatBuffer buf2 = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf2);
            buf2.rewind();
            Matrix4fe.get(viewMatrix, 0, buf2);
        }

        //return viewMatrix.mulAffine(modelMatrix, modelViewMatrix);
        Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
        //Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(viewMatrix, projectionMatrix);

        shaderProgram.setUniformEfficient("modelViewMatrixCamera", modelViewMatrix, viewMatrixBuffer);

        shaderProgram.setUniform("texture_sampler", 0);

        CoroUtilBlockLightCache.brightnessPlayer = CoroUtilBlockLightCache.getBrightnessNonLightmap(world, (float)entityIn.posX, (float)entityIn.posY, (float)entityIn.posZ);


        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.tallgrass);
        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(ParticleRegistry.tallgrass);

        //test
        //GlStateManager.depthMask(false);

        this.renderer.bindTexture(PARTICLE_TEXTURES);

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        mesh.initRender();
        mesh.initRenderVBO1();
        mesh.initRenderVBO2();

        //also resets position
        mesh.instanceDataBuffer.clear();
        mesh.instanceDataBufferSeldom.clear();
        mesh.curBufferPos = 0;

        BlockPos pos = entityIn.getPosition();

        List<Foliage> listFoliage = new ArrayList<>();

        Random rand = new Random();
        int range = 10;

        //make obj
        for (int i = 0; i < 100; i++) {
            Foliage foliage = new Foliage();
            int randX = rand.nextInt(range) - range/2;
            int randY = rand.nextInt(range) - range/2;
            int randZ = rand.nextInt(range) - range/2;
            foliage.setPosition(new BlockPos(pos).add(randX, randY, randZ));
            foliage.rotationYaw = 1;
            foliage.rotationPitch = 1;
            foliage.particleScale = 100;
            listFoliage.add(foliage);
        }

        for (Foliage foliage : listFoliage) {
            foliage.updateQuaternion(entityIn);

            //update vbo1
            foliage.renderForShaderVBO1(mesh, transformation, modelViewMatrix, entityIn, partialTicks);
        }

        //mesh.instanceDataBuffer.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS);

        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO);
        ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

        mesh.curBufferPos = 0;

        for (Foliage foliage : listFoliage) {

            //update vbo2
            foliage.renderForShaderVBO2(mesh, transformation, modelViewMatrix, entityIn, partialTicks);
        }

        //wasnt used in particle renderer and even crashes it :o
        //mesh.instanceDataBufferSeldom.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS_SELDOM);

        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBOSeldom);
        ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferSeldom, GL_DYNAMIC_DRAW);

        ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0, mesh.curBufferPos);

        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, 0);

        mesh.endRenderVBO1();
        mesh.endRenderVBO2();
        mesh.endRender();

        ShaderEngine.renderer.getShaderProgram("foliage").unbind();*/
    }

}
