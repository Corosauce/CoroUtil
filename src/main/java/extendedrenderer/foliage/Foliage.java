package extendedrenderer.foliage;

import extendedrenderer.shader.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector4f;

import javax.vecmath.Vector3f;

public class Foliage implements IShaderRenderedEntity {

    public double posX;
    public double posY;
    public double posZ;
    public double prevPosX;
    public double prevPosY;
    public double prevPosZ;
    public static double interpPosX;
    public static double interpPosY;
    public static double interpPosZ;

    public float width = 1F;
    public float height = 1F;

    public float particleScale = 1F;

    /** The red amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0. */
    public float particleRed;
    /** The green amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0. */
    public float particleGreen = 1F;
    /** The blue amount of color. Used as a percentage, 1.0 = 255 and 0.0 = 0. */
    public float particleBlue;

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

    public Foliage() {

    }

    public void setPosition(BlockPos pos) {
        posX = pos.getX();
        posY = pos.getY();
        posZ = pos.getZ();
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
    }

    public BlockPos getBlockPosition() {
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

        if (mesh.curBufferPos >= mesh.numInstances) return;

        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), particleAlpha);

        //TEMP
        //mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos), (float)Minecraft.getMinecraft().player.getDistance(this.posX, this.posY, this.posZ) - 2.5F);

        float brightness;
        //brightness = CoroUtilBlockLightCache.getBrightnessCached(worldObj, pos.x, pos.y, pos.z);
        brightness = brightnessCache;
        mesh.instanceDataBuffer.put(mesh.INSTANCE_SIZE_FLOATS * (mesh.curBufferPos) + 1, brightness);

        mesh.curBufferPos++;

    }

    public void renderForShaderVBO2(InstancedMeshFoliage mesh, Transformation transformation, Matrix4fe viewMatrix, Entity entityIn,
                                            float partialTicks) {

        if (mesh.curBufferPos >= mesh.numInstances) return;

        //camera relative positions, for world position, remove the interpPos values
        float posX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - this.interpPosX);
        float posY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - this.interpPosY);
        float posZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - this.interpPosZ);
        //Vector3f pos = new Vector3f((float) (entityIn.posX - particle.posX), (float) (entityIn.posY - particle.posY), (float) (entityIn.posZ - particle.posZ));
        Vector3f pos = new Vector3f(posX, posY, posZ);

        Matrix4fe modelMatrix = transformation.buildModelMatrix(this, pos);

        //adjust to perspective and camera
        //Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
        //upload to buffer
        modelMatrix.get(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPos), mesh.instanceDataBufferSeldom);

        int rgbaIndex = 0;
        mesh.instanceDataBufferSeldom.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), this.particleRed);
        mesh.instanceDataBufferSeldom.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), this.particleGreen);
        mesh.instanceDataBufferSeldom.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), this.particleBlue);
        //using yaw here instead, alpha in other VBO
        mesh.instanceDataBufferSeldom.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), this.rotationYaw);

        //index, aka buffer pos?
        mesh.instanceDataBufferSeldom.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), mesh.curBufferPos);

        mesh.instanceDataBufferSeldom.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), animationID);

        mesh.instanceDataBufferSeldom.put(mesh.INSTANCE_SIZE_FLOATS_SELDOM * (mesh.curBufferPos) + mesh.MATRIX_SIZE_FLOATS
                + (rgbaIndex++), heightIndex);

        mesh.curBufferPos++;
    }
}
