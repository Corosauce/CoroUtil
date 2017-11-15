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

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

public class FoliageRenderer {

    private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");

    private final TextureManager renderer;

    public static FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    public FoliageRenderer(TextureManager rendererIn) {
        this.renderer = rendererIn;
    }

    public void render(Entity entityIn, float partialTicks)
    {
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

            Transformation transformation = null;
            Matrix4fe viewMatrix = null;

            ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("foliage");
            transformation = ShaderEngine.renderer.transformation;
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

            this.renderer.bindTexture(PARTICLE_TEXTURES);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            mesh.initRender();
            mesh.initRenderVBO1();
            mesh.initRenderVBO2();

            mesh.instanceDataBuffer.clear();
            mesh.curBufferPos = 0;

            BlockPos pos = entityIn.getPosition();

            //make obj
            Foliage foliage = new Foliage();
            foliage.setPosition(pos);
            foliage.updateQuaternion(entityIn);
            foliage.renderForShaderVBO1(mesh, transformation, viewMatrix, entityIn, partialTicks);
            //update vbo1


            mesh.instanceDataBuffer.limit(mesh.curBufferPos * mesh.INSTANCE_SIZE_FLOATS);

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO);
            ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBuffer, GL_DYNAMIC_DRAW);

            mesh.curBufferPos = 0;

            //update vbo2
            foliage.renderForShaderVBO2(mesh, transformation, viewMatrix, entityIn, partialTicks);

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBOSeldom);
            ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferSeldom, GL_DYNAMIC_DRAW);

            ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0, mesh.curBufferPos);

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, 0);

            mesh.endRenderVBO1();
            mesh.endRenderVBO2();
            mesh.endRender();

            ShaderEngine.renderer.getShaderProgram("foliage").unbind();


            GlStateManager.enableCull();

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1F);
        }
    }

}
