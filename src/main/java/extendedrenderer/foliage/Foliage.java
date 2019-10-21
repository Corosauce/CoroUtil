package extendedrenderer.foliage;

import CoroUtil.util.CoroUtilBlockLightCache;
import extendedrenderer.shader.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.Random;

public class Foliage implements IShaderRenderedEntity {

    public double posX;
    public double posY;
    public double posZ;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;

    public float width = 1F;
    public float height = 1F;

    public float particleScale = 1F;

    /** The red amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0. */
    public float particleRed = 1F;
    /** The green amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0. */
    public float particleGreen = 1F;
    /** The blue amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0. */
    public float particleBlue = 1F;

    /** Particle alpha */
    public float particleAlpha = 1F;
    public TextureAtlasSprite particleTexture;

    public float rotationYaw;
    public float rotationPitch;

    public Quaternion rotation = new Quaternion();

    public boolean rotateOrderXY = false;

    public float brightnessCache = 0.5F;

    public int animationID = 0;
    public int heightIndex = 0;
    public float looseness = 1;

    private static final Random rand = new Random(439875L);

    private static final PerlinNoiseGenerator angleNoise = new PerlinNoiseGenerator(rand, 1);
    private static final PerlinNoiseGenerator delayNoise = new PerlinNoiseGenerator(rand, 3);

    public Foliage(TextureAtlasSprite sprite) {
        particleTexture = sprite;
    }

    public void setPosition(BlockPos pos) {
        posX = pos.getX();
        posY = pos.getY();
        posZ = pos.getZ();
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
    }

    public BlockPos getPos() {
        return new BlockPos(posX, posY, posZ);
    }

    @Override
    public Vector3f getPosition() {
        return new Vector3f((float)posX, (float)posY, (float)posZ);
    }

    @Override
    public Quaternion getQuaternion() {
        return rotation;
    }

    //TODO: implement prev quat
    @Override
    public Quaternion getQuaternionPrev() {
        //return rotation;
        return null;
    }

    @Override
    public float getScale() {
        return particleScale;
    }

    public void updateQuaternion(Entity camera) {

        /*if (this.facePlayer) {
            this.rotationYaw = camera.rotationYaw;
            this.rotationPitch = camera.rotationPitch;
        } else if (facePlayerYaw) {
            this.rotationYaw = camera.rotationYaw;
        }*/

        Quaternion qY = new Quaternion();
        Quaternion qX = new Quaternion();
        qY.setFromAxisAngle(new Vector4f(0, 1, 0, (float)Math.toRadians(-this.rotationYaw - 180F)));
        qX.setFromAxisAngle(new Vector4f(1, 0, 0, (float)Math.toRadians(-this.rotationPitch)));
        if (this.rotateOrderXY) {
            Quaternion.mul(qX, qY, this.rotation);
        } else {
            Quaternion.mul(qY, qX, this.rotation);
        }
    }

    public void renderForShaderVBO1(InstancedMeshFoliage mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                        float partialTicks) {

        if (mesh.curBufferPosVBO1 >= mesh.numInstances) {
            return;
        }

        mesh.instanceDataBufferVBO1.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPosVBO1), particleAlpha);

        //TEMP
        //mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), (float)Minecraft.getInstance().player.getDistance(this.posX, this.posY, this.posZ) - 2.5F);

        float brightness;
        brightness = CoroUtilBlockLightCache.getBrightnessCached(Minecraft.getInstance().world, (float)this.posX, (float)this.posY, (float)this.posZ);
        //brightness = brightnessCache;
        //brightness = CoroUtilBlockLightCache.brightnessPlayer;

        /*int r = (int)(0.2F * 255.0F);
        int g = (int)(0.2F * 255.0F);
        int b = (int)(1F * 255.0F);
        float brightnessTest = -16777216 | r << 16 | g << 8 | b;*/

        //brightness = -0.1F;
        //brightness = brightnessTest;

        //System.out.println(brightnessTest);
        //System.out.println(String.format("%.12f", brightnessTest));

        mesh.instanceDataBufferVBO1.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPosVBO1) + 1, brightness);

        mesh.curBufferPosVBO1++;

    }

    public void renderForShaderVBO2(InstancedMeshFoliage mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                            float partialTicks) {

        boolean autoGrowBuffer = false;
        if (mesh.curBufferPosVBO2 >= mesh.numInstances) {

            //cant quite get this to work correctly without lots of missing renders ingame until next thread tick, why?

            if (autoGrowBuffer) {
                mesh.numInstances *= 2;
                System.out.println("hit max mesh count, doubling in size to " + mesh.numInstances);
                //double vbo2 and copy data
                FloatBuffer newBuffer = BufferUtils.createFloatBuffer(mesh.numInstances * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM);
                //newBuffer.clear();
                //doesnt actually clear
                mesh.instanceDataBufferVBO2.rewind();
                newBuffer.put(mesh.instanceDataBufferVBO2);
                mesh.instanceDataBufferVBO2.rewind();
                newBuffer.flip();
                mesh.instanceDataBufferVBO2 = newBuffer;
                mesh.instanceDataBufferVBO2.position(mesh.curBufferPosVBO2 * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM);

                //double vbo1 and copy data

                newBuffer = BufferUtils.createFloatBuffer(mesh.numInstances * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS);
                newBuffer.clear();
                //doesnt actually clear
                //mesh.instanceDataBufferVBO1.position(0);
                //newBuffer.put(mesh.instanceDataBufferVBO1);
                mesh.instanceDataBufferVBO1 = newBuffer;
                //mesh.instanceDataBufferVBO1.position(mesh.curBufferPosVBO1 * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS);
            } else {
                //System.out.println("hitting max mesh count");
                return;
            }
        }

        //camera relative positions, for world position, remove the interpPos values
        float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - mesh.interpPosXThread);
        float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - mesh.interpPosYThread);
        float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - mesh.interpPosZThread);
        //Vector3f pos = new Vector3f((float) (entityIn.posX - particle.posX), (float) (entityIn.posY - particle.posY), (float) (entityIn.posZ - particle.posZ));
        Vector3f pos = new Vector3f(posX, posY, posZ);

        Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos, partialTicks);

        //adjust to perspective and camera
        //Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        //upload to buffer
        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2), mesh.instanceDataBufferVBO2);

        int floatIndex = 0;
        mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (floatIndex++), this.particleRed);
        mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (floatIndex++), this.particleGreen);
        mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (floatIndex++), this.particleBlue);
        //using yaw here instead, alpha in other VBO
        mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (floatIndex++), this.rotationYaw);

        //index, aka buffer pos?
        /*mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), mesh.curBufferPosVBO2);*/
        /*mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), (((MathHelper.floor(this.posX) * 15)+(MathHelper.floor(this.posX) * 15))));*/
        mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (floatIndex++), (float)delayNoise.get(this.posX, this.posZ));


        mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (floatIndex++), animationID);

        mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (floatIndex++), heightIndex);

        mesh.instanceDataBufferVBO2.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPosVBO2) + mesh.MATRIX_SIZE_FLOATS
                + (floatIndex++), looseness);

        mesh.curBufferPosVBO2++;
    }
}

