package extendedrenderer.render;

import CoroUtil.config.ConfigCoroAI;
import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.foliage.Foliage;
import extendedrenderer.foliage.FoliageClutter;
import extendedrenderer.foliage.ParticleTallGrassTemp;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.shader.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import java.util.*;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

public class FoliageRenderer {

    private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");

    private final TextureManager renderer;

    public static FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    public static FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);

    public Transformation transformation;

    public boolean needsUpdate = true;

    public List<Foliage> listFoliage = new ArrayList<>();
    public HashMap<BlockPos, List<Foliage>> lookupPosToFoliage = new HashMap<>();

    public float windDir = 0;
    public float windSpeed = 0;

    public FoliageRenderer(TextureManager rendererIn) {
        this.renderer = rendererIn;
        transformation = new Transformation();
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

            int mip_min = 0;
            int mip_mag = 0;

            if (!ConfigCoroAI.disableMipmapFix) {
                mip_min = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER);
                mip_mag = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER);
                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            }

            GlStateManager.depthMask(true);

            GlStateManager.disableCull();

            renderJustShaders(entityIn, partialTicks);

            GlStateManager.enableCull();

            GlStateManager.depthMask(true);

            if (!ConfigCoroAI.disableMipmapFix) {
                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, mip_min);
                GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, mip_mag);
            }

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
        //dont use for now, see RotatingParticleManager notes
        boolean distantRendering = false;
        if (distantRendering) {
            float zNear = 0.05F;
            float zFar = (float) (mc.gameSettings.renderDistanceChunks * 16) * 4F;
            projectionMatrix.m22 = ((zFar + zNear) / (zNear - zFar));
            projectionMatrix.m32 = ((zFar + zFar) * zNear / (zNear - zFar));
        }

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

        //Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);

        //fix for camera model matrix being 0 0 0 on positions, readjusts to world positions for static rendering instanced meshes
        float interpX = (float)(entityIn.prevPosX + (entityIn.posX - entityIn.prevPosX) * partialTicks);
        float interpY = (float)(entityIn.prevPosY + (entityIn.posY - entityIn.prevPosY) * partialTicks);
        float interpZ = (float)(entityIn.prevPosZ + (entityIn.posZ - entityIn.prevPosZ) * partialTicks);
        Matrix4fe matrixFix = new Matrix4fe();
        matrixFix = matrixFix.translationRotateScale(
                -interpX, -interpY, -interpZ,
                0, 0, 0, 1,
                1, 1, 1);

        boolean test1 = false;

        ShaderProgram shaderProgram = ShaderEngine.renderer.getShaderProgram("foliage");
        //transformation = ShaderEngine.renderer.transformation;
        shaderProgram.bind();

        //testing determined i can save frames by baking projectionMatrix into modelViewMatrixCamera, might have to revert for more complex shaders
        //further testing its just barely faster, if at all...
        //shaderProgram.setUniform("projectionMatrix", mat);
        if (test1) {
            try {
                shaderProgram.setUniformEfficient("projectionMatrix", projectionMatrix, projectionMatrixBuffer);
            } catch (Exception ex) {
                //ignore optimization in testing
            }
        }


        if (test1) {
            matrixFix = viewMatrix.mul(matrixFix);
        } else {
            Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
            matrixFix = modelViewMatrix.mul(matrixFix);
        }

        shaderProgram.setUniformEfficient("modelViewMatrixCamera", matrixFix, viewMatrixBuffer);

        shaderProgram.setUniform("texture_sampler", 0);

        try {
            shaderProgram.setUniform("time", (int) world.getTotalWorldTime());
        } catch (Exception ex) {
            //ignore optimization in testing
        }

        shaderProgram.setUniform("partialTick", partialTicks);
        shaderProgram.setUniform("windDir", windDir);
        shaderProgram.setUniform("windSpeed", windSpeed);

        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.tallgrass);
        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.tallgrass_hd);
        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(ParticleRegistry.tallgrass);

        mesh.initRender();
        mesh.initRenderVBO1();
        mesh.initRenderVBO2();


        boolean skipUpdate = false;
        boolean updateFoliageObjects = false;
        boolean updateVBO1 = true;
        boolean updateVBO2 = false;
        boolean add = true;
        boolean trim = true;

        /**
         * TODO: lazy foliage management done, now to make it play nicer with VBO and not break ordering
         */

        if (!skipUpdate || needsUpdate) {
            //also resets position
            mesh.instanceDataBuffer.clear();
            mesh.instanceDataBufferSeldom.clear();
            mesh.curBufferPos = 0;
        }

        BlockPos pos = entityIn.getPosition();



        Random rand = new Random();
        rand.setSeed(5);
        int range = 150;

        int amount = 70000;
        int adjAmount = amount;

        boolean subTest = false;

        if (subTest) {
            adjAmount = amount;
            //adjAmount = 50;
        }

        CoroUtilBlockLightCache.brightnessPlayer = CoroUtilBlockLightCache.getBrightnessNonLightmap(world, (float)entityIn.posX, (float)entityIn.posY, (float)entityIn.posZ);

        int radialRange = 15;

        int xzRange = radialRange;
        int yRange = 10;

        boolean dirtyVBO2 = false;

        //scan and add foliage around player
        //TODO: firstly, dont do this per render tick geeze, secondly, thread it just like weather leaf block scan
        //time here is a hack for now
        if (add && world.getTotalWorldTime() % 5 == 0) {
            for (int x = -xzRange; x <= xzRange; x++) {
                for (int z = -xzRange; z <= xzRange; z++) {
                    for (int y = -yRange; y <= yRange; y++) {
                        BlockPos posScan = pos.add(x, y, z);
                        IBlockState state = entityIn.world.getBlockState(posScan.down());
                        if (!lookupPosToFoliage.containsKey(posScan)) {
                            if (state.getMaterial() == Material.GRASS) {
                                if (entityIn.getDistanceSq(posScan) <= radialRange * radialRange) {
                                    List<Foliage> listClutter = new ArrayList<>();
                                    for (int i = 0; i < FoliageClutter.clutterSize; i++) {
                                        Foliage foliage = new Foliage();
                                        foliage.setPosition(posScan);
                                        foliage.posY += 0.5F;
                                        foliage.prevPosY = foliage.posY;
                                        foliage.posX += 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.8F;
                                        foliage.prevPosX = foliage.posX;
                                        foliage.posZ += 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.8F;
                                        foliage.prevPosZ = foliage.posZ;
                                        foliage.rotationYaw = 0;
                                        //foliage.rotationYaw = 90;
                                        foliage.rotationYaw = world.rand.nextInt(360);
                                        //foliage.rotationPitch = rand.nextInt(90) - 45;
                                        foliage.particleScale /= 0.2;

                                        int color = Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, entityIn.world, posScan.down(), 0);
                                        foliage.particleRed = (float) (color >> 16 & 255) / 255.0F;
                                        foliage.particleGreen = (float) (color >> 8 & 255) / 255.0F;
                                        foliage.particleBlue = (float) (color & 255) / 255.0F;

                                        foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer;

                                        listClutter.add(foliage);
                                    }

                                    lookupPosToFoliage.put(posScan, listClutter);

                                    dirtyVBO2 = true;
                                }
                            }
                        } else {

                        }
                    }
                }
            }
        }

        //cleanup list
        if (trim) {
            Iterator<Map.Entry<BlockPos, List<Foliage>>> it = lookupPosToFoliage.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, List<Foliage>> entry = it.next();
                IBlockState state = entityIn.world.getBlockState(entry.getKey().down());
                if (state.getMaterial() != Material.GRASS) {
                    it.remove();
                    dirtyVBO2 = true;
                } else if (entityIn.getDistanceSq(entry.getKey()) > radialRange * radialRange) {
                    it.remove();
                    dirtyVBO2 = true;
                }
            }
        }

        if (!skipUpdate || needsUpdate) {

            if (updateVBO1 || needsUpdate) {
                for (List<Foliage> listFoliage : lookupPosToFoliage.values()) {
                    for (Foliage foliage : listFoliage) {
                        float distMax = 3F;
                        double dist = entityIn.getDistance(foliage.posX, foliage.posY, foliage.posZ);
                        if (dist < distMax) {
                            foliage.particleAlpha = (float) (dist) / distMax;
                        } else {
                            foliage.particleAlpha = 1F;
                        }

                        foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer + 0.0F;

                        //update vbo1
                        foliage.renderForShaderVBO1(mesh, transformation, viewMatrix, entityIn, partialTicks);
                    }
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
                    //GL15.glMapBuffer()
                }
            }

            mesh.curBufferPos = 0;

            if (updateVBO2 || needsUpdate || dirtyVBO2) {
                for (List<Foliage> listFoliage : lookupPosToFoliage.values()) {
                    for (Foliage foliage : listFoliage) {
                        foliage.updateQuaternion(entityIn);

                        //update vbo2
                        foliage.renderForShaderVBO2(mesh, transformation, viewMatrix, entityIn, partialTicks);
                    }
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
        }

        needsUpdate = false;

        ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT, 0, lookupPosToFoliage.size() * FoliageClutter.clutterSize);

        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, 0);

        mesh.endRenderVBO1();
        mesh.endRenderVBO2();
        mesh.endRender();

        ShaderEngine.renderer.getShaderProgram("foliage").unbind();
    }

}
