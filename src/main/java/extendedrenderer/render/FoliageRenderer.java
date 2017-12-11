package extendedrenderer.render;

import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.foliage.Foliage;
import extendedrenderer.particle.ParticleRegistry;
import extendedrenderer.particle.ShaderManager;
import extendedrenderer.shader.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;

public class FoliageRenderer {

    //private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");

    private final TextureManager renderer;

    public static FloatBuffer projectionMatrixBuffer = BufferUtils.createFloatBuffer(16);
    public static FloatBuffer viewMatrixBuffer = BufferUtils.createFloatBuffer(16);
    //public static FloatBuffer viewMatrixClassicBuffer = BufferUtils.createFloatBuffer(16);

    public Transformation transformation;

    //public List<Foliage> listFoliage = new ArrayList<>();

    /**
     * ordered mesh list -> renderables should be enough for no translucency
     *
     * if in future i need translucency, keep above list and make a new one for translucency with:
     *
     * render order list to help lack of use of depth mask
     * - TextureAtlasSprite to Foliage
     *
     *
     * possible solution to alpha sorting for translucency layer?
     * "batch all physobj renders into a single BufferBuilder draw call, call sortVertexData before draw()
     * remember vanilla is regularly sorting all translucent quads in the worldl
     * and all FastTESR quads are sorted"
     *
     * vanilla model -> mesh:
     *
     * instead of doing different sway by singling out certain vertex ids, use:
     * - sway amount = height index + vertex height
     * - will make meshes work more generically for more complex models
     *
     * get the more complex model via:
     * - ModelBakeEvent
     * - instead of json editing models and losing their model
     * -- leave json alone
     * -- hook into event, steal model for my shader, override vanilla model/render with blank
     * -- BufferBuilder.addVertexData eg
     *
     *
     *
     *
     */

    public ConcurrentHashMap<TextureAtlasSprite, List<Foliage>> foliage = new ConcurrentHashMap<>();

    public float windDir = 0;
    public float windSpeed = 0;

    //public static int vbo2BufferPos = 0;

    public Lock lockVBO2 = new ReentrantLock();

    public static int radialRange = 60;
    public static boolean testStaticLimit = false;

    public FoliageRenderer(TextureManager rendererIn) {
        this.renderer = rendererIn;
        transformation = new Transformation();


    }

    public List<Foliage> getFoliageForSprite(TextureAtlasSprite sprite) {
        List<Foliage> list;
        if (!foliage.containsKey(sprite)) {
            list = new ArrayList<>();
            foliage.put(sprite, list);
        }
        return foliage.get(sprite);
    }

    public void render(Entity entityIn, float partialTicks)
    {

        if (RotatingParticleManager.useShaders) {

            Minecraft mc = Minecraft.getMinecraft();
            EntityRenderer er = mc.entityRenderer;
            World world = mc.world;

            GlStateManager.depthMask(true);

            mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);

            renderJustShaders(entityIn, partialTicks);
        }
    }

    public boolean getFlag(InstancedMeshFoliage mesh) {
        return mesh.dirtyVBO2Flag;
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
        /**
         * new problem, accuracy diminishes the more you are from 0 0 0 causing vertex flicker
         * idea:
         * - set a new point of reference each time you have to do a full vbo update
         * -- if it works, it maintains speed and precision
         * procedure:
         * - on vbo refresh, set new "0 0 0" point
         * - offset interp values by it
         * - matrixFix = -(interp - VBOUpdateCamPos)
         * - foliagepos = interp - VBOUpdateCamPos
         */
        boolean threadedVBOUpdate = true;

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

        //shaderProgram.setUniformEfficient("modelViewMatrixCamera", matrixFix, viewMatrixBuffer);
        //shaderProgram.setUniformEfficient("modelViewMatrixClassic", viewMatrix, viewMatrixClassicBuffer);

        shaderProgram.setUniform("texture_sampler", 0);
        int glFogMode = GL11.glGetInteger(GL11.GL_FOG_MODE);
        int modeIndex = 0;
        if (glFogMode == GL11.GL_LINEAR) {
            modeIndex = 0;
        } else if (glFogMode == GL11.GL_EXP) {
            modeIndex = 1;
        } else if (glFogMode == GL11.GL_EXP2) {
            modeIndex = 2;
        }
        shaderProgram.setUniform("fogmode", modeIndex);

        /*GLfloat v[10] = {...};
        glUniform1fv(glGetUniformLocation(program, "v"), 10, v);*/

        Random rand = new Random(5);

        /*
        //CACHE ME
        IntBuffer buffer = BufferUtils.createIntBuffer(64);
        for (int i = 0; i < 64; i++) {
            buffer.put(i, rand.nextInt(255));
        }*/
        //buffer.flip();

        //OpenGlHelper.glUniform1(shaderProgram.uniforms.get("stipple"), buffer);

        try {
            shaderProgram.setUniform("time", (int) world.getTotalWorldTime());
        } catch (Exception ex) {
            //ignore optimization in testing
        }

        shaderProgram.setUniform("partialTick", partialTicks);
        shaderProgram.setUniform("windDir", windDir);

        //temp
        windSpeed = 0.5F;

        //temp override vars
        FoliageRenderer.radialRange = 20;

        shaderProgram.setUniform("windSpeed", windSpeed);

        //TODO: temp allocations
        /*MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.tallgrass);
        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.double_plant_bottom);
        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.double_plant_top);
        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.tallgrass_hd);*/
        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.potato);
        MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.chicken);
        for (int i = 0; i < ParticleRegistry.listFish.size(); i++) {
            MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.listFish.get(i));
        }
        for (int i = 0; i < ParticleRegistry.listSeaweed.size(); i++) {
            MeshBufferManagerFoliage.setupMeshIfMissing(ParticleRegistry.listSeaweed.get(i));
        }

        int meshCount = 0;

        /*TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite sprite = map.getAtlasSprite("minecraft:blocks/wheat_stage_7");*/

        for (Map.Entry<TextureAtlasSprite, List<Foliage>> entry : foliage.entrySet()) {
        //for (int iii = 0; iii < 1; iii++) {
            InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(entry.getKey());

            //InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);

            if (mesh == null) {
                System.out.println("NULL MESH FOR: " + entry.getKey().toString());
            }

            mesh.initRender();
            mesh.initRenderVBO1();
            mesh.initRenderVBO2();

            boolean updateVBO1 = true;

            if (lockVBO2.tryLock()) {
                try {

                    /*mesh.instanceDataBufferVBO2.clear();
                    mesh.curBufferPosVBO2 = 0;*/

                    if (getFlag(mesh)) {

                        mesh.interpPosX = mesh.interpPosXThread;
                        mesh.interpPosY = mesh.interpPosYThread;
                        mesh.interpPosZ = mesh.interpPosZThread;

                        //System.out.println("main thread: mesh.curBufferPosVBO2: " + mesh.curBufferPosVBO2);
                        //System.out.println("vbo 2 bind");

                        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO2);
                        ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferVBO2, GL_DYNAMIC_DRAW);

                        mesh.dirtyVBO2Flag = false;

                        mesh.curBufferPosVBO2Thread = mesh.curBufferPosVBO2;
                    }

                    if (updateVBO1) {

                        List<Foliage> listFoliage = entry.getValue();

                        mesh.instanceDataBufferVBO1.clear();
                        mesh.curBufferPosVBO1 = 0;

                        //System.out.println("vbo 1 update");

                        for (Foliage foliage : listFoliage) {
                            boolean doAlpha = false;

                            if (doAlpha) {
                                //close fade
                                float distMax = 3F;
                                double distFadeRange = 20;
                                int rangeAdj = radialRange - (int)distFadeRange;
                                double dist = entityIn.getDistance(foliage.posX, foliage.posY, foliage.posZ);
                                if (dist > rangeAdj - distFadeRange) {

                                    double diff = dist - ((double) rangeAdj - distFadeRange);
                                    foliage.particleAlpha = (float) (1F - (diff / distFadeRange));
                                    if (foliage.particleAlpha < 0F) foliage.particleAlpha = 0F;

                                } else {
                                    foliage.particleAlpha = 1F;
                                }
                            } else {
                                foliage.particleAlpha = 1F;
                            }

                            foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer + 0.0F;

                            //update vbo1
                            foliage.renderForShaderVBO1(mesh, transformation, viewMatrix, entityIn, partialTicks);
                        }

                        //System.out.println("main thread: mesh.curBufferPosVBO1: " + mesh.curBufferPosVBO1);

                        //System.out.println("vbo 1 bind");

                        if (testStaticLimit) {
                            mesh.instanceDataBufferVBO1.limit(30000 * mesh.INSTANCE_SIZE_FLOATS);
                        } else {
                            mesh.instanceDataBufferVBO1.limit(mesh.curBufferPosVBO1 * mesh.INSTANCE_SIZE_FLOATS);
                        }


                        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, mesh.instanceDataVBO1);

                        ShaderManager.glBufferData(GL_ARRAY_BUFFER, mesh.instanceDataBufferVBO1, GL_DYNAMIC_DRAW);
                    }
                } finally {
                    lockVBO2.unlock();
                }

            }

            float interpX = (float)((entityIn.prevPosX + (entityIn.posX - entityIn.prevPosX) * partialTicks) - mesh.interpPosX);
            float interpY = (float)((entityIn.prevPosY + (entityIn.posY - entityIn.prevPosY) * partialTicks) - mesh.interpPosY);
            float interpZ = (float)((entityIn.prevPosZ + (entityIn.posZ - entityIn.prevPosZ) * partialTicks) - mesh.interpPosZ);

            Matrix4fe matrixFix = new Matrix4fe();
            matrixFix = matrixFix.translationRotateScale(
                    -interpX, -interpY, -interpZ,
                    0, 0, 0, 1,
                    1, 1, 1);

            projectionMatrix = new Matrix4fe();
            buf = BufferUtils.createFloatBuffer(16);
            GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, buf);
            buf.rewind();
            Matrix4fe.get(projectionMatrix, 0, buf);

            Matrix4fe modelViewMatrix = projectionMatrix.mul(viewMatrix);
            matrixFix = modelViewMatrix.mul(matrixFix);

            shaderProgram.setUniformEfficient("modelViewMatrixCamera", matrixFix, viewMatrixBuffer);

            if (mesh.curBufferPosVBO2Thread > 0) {
                //System.out.println("draw");
                ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, mesh.getVertexCount(), GL_UNSIGNED_INT,
                        0, mesh.curBufferPosVBO2Thread);

                meshCount += mesh.curBufferPosVBO2Thread;
            }

            OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, 0);

            mesh.endRenderVBO1();
            mesh.endRenderVBO2();
            mesh.endRender();
        }

        boolean debugMeshCount = false;
        if (debugMeshCount) {
            System.out.println("meshCount: " + meshCount);
        }

        ShaderEngine.renderer.getShaderProgram("foliage").unbind();
    }

}
