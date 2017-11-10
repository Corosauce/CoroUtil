package extendedrenderer.shadertest.gametest;

import CoroUtil.util.CoroUtilParticle;
import extendedrenderer.particle.ShaderManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import javax.vecmath.Vector3f;
import java.nio.FloatBuffer;
import java.util.List;


import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

public class InstancedMesh extends Mesh {

    public static final int FLOAT_SIZE_BYTES = 4;

    public static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;

    public static final int MATRIX_SIZE_FLOATS = 4 * 4;

    public static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;

    public static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES + FLOAT_SIZE_BYTES * 5/* * 2 + FLOAT_SIZE_BYTES * 2*/;

    //extra + 4 for test
    public static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS + 1 + 4;// * 2 + 2;

    //public static final int INSTANCE_SIZE_FLOATS_TEST = 4;

    public final int numInstances;

    public final int instanceDataVBO;
    public final int instanceDataVBOTest;

    public FloatBuffer instanceDataBuffer;
    public FloatBuffer instanceDataBufferTest;

    public int curBufferPos = 0;

    /**
     * TODO: despite the mesh only being a size of 2 vbos instead of 5, lowering this to 2 breaks something somehow (no rendering)
     * need to figure out where to fix so i can optimize memory usage
     * not even sure if the memory is unoptimized, theres just gaps in the memory used probably
     *
     * fixed, didnt account for attrib location values in shader program
     */
    public static int vboSizeMesh = 2;

    public InstancedMesh(float[] positions, float[] textCoords, int[] indices, int numInstances) {
        super(positions, textCoords, indices);

        this.numInstances = numInstances;

        ShaderManager.glBindVertexArray(vaoId);

        // Model View Matrix
        instanceDataVBO = glGenBuffers();
        vboIdList.add(instanceDataVBO);
        instanceDataBuffer = BufferUtils.createFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS);//MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
        int start = vboSizeMesh;
        int strideStart = 0;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            ShaderManager.glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        //TODO: might become UV lightmap coord in future
        //brightness
        glVertexAttribPointer(start, 1, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += FLOAT_SIZE_BYTES;

        /**
         * TODO: rbg and alpha for colorization
         * storm darkening uses lower rgb values to darken
         * everything uses alpha for fading in and out
         *
         */

        //rgba
        glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += VECTOR4F_SIZE_BYTES;

        // Light view matrix
        /*for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }*/

        // Texture offsets
        /*glVertexAttribPointer(start, 2, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        glVertexAttribDivisor(start, 1);*/

        //test color to its own vbo
        instanceDataBufferTest = BufferUtils.createFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS);

        FloatBuffer colorBuffer = null;
        instanceDataVBOTest = OpenGlHelper.glGenBuffers();
        vboIdList.add(instanceDataVBOTest);
        colorBuffer = BufferUtils.createFloatBuffer(4);
        float[] floats = new float[4];
        floats[0] = 1F;
        floats[1] = 1F;
        floats[2] = 1F;
        floats[3] = 1F;

        colorBuffer.put(floats).flip();
        OpenGlHelper.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceDataVBOTest);
        ShaderManager.glBufferData(GL15.GL_ARRAY_BUFFER, colorBuffer, GL15.GL_DYNAMIC_DRAW);
        GL20.glVertexAttribPointer(start++, 4, GL11.GL_FLOAT, false, 0, 0);

        OpenGlHelper.glBindBuffer(GL_ARRAY_BUFFER, 0);
        ShaderManager.glBindVertexArray(0);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.instanceDataBuffer != null) {
            //MemoryUtil.memFree(this.instanceDataBuffer);
            this.instanceDataBuffer = null;
        }

        if (this.instanceDataBufferTest != null) {
            //MemoryUtil.memFree(this.instanceDataBuffer);
            this.instanceDataBufferTest = null;
        }
    }

    @Override
    public void initRender() {
        super.initRender();

        int start = vboSizeMesh;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glEnableVertexAttribArray(start + i);
        }
    }

    @Override
    public void endRender() {
        int start = vboSizeMesh;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glDisableVertexAttribArray(start + i);
        }

        super.endRender();
    }

    /*public void renderListInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4fe viewMatrix) {
        renderListInstanced(gameItems, transformation, viewMatrix);
    }*/

    public void renderListInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4fe viewMatrix) {
        initRender();

        int chunkSize = numInstances;
        int length = gameItems.size();
        for (int i = 0; i < length; i += chunkSize) {
            int end = Math.min(length, i + chunkSize);
            List<GameItem> subList = gameItems.subList(i, end);
            renderChunkInstanced(subList, transformation, viewMatrix);
        }

        //renderChunkInstanced(gameItems, transformation, viewMatrix);

        endRender();
    }

    private void renderChunkInstanced(List<GameItem> gameItems, Transformation transformation, Matrix4fe viewMatrix) {

        boolean testSkip = false;

        if (!testSkip) this.instanceDataBuffer.clear();

        int i = 0;
        int index = 0;

        //temp
        boolean billBoard = false;


        int amountToRender = gameItems.size() * Mesh.extraRenders;

        //Texture text = getMaterial().getTexture();
        for (int ii = 0; /*!testSkip && */ii < gameItems.size(); ii++) {
        //for (GameItem gameItem : gameItems) {
            GameItem gameItem = gameItems.get(ii);



            if (viewMatrix != null) {


                //if (index != 0) {
                for (int iii = 0; iii < Mesh.extraRenders; iii++) {

                    //CoroUtilParticle.rainPositions[ii].xCoord;

                    Vector3f pos = gameItem.getPosition();
                    Vector3f posCustom = null;
                    Matrix4fe modelMatrix = null;

                    if (iii != 0) {
                        posCustom = new Vector3f(pos.getX() + (float)CoroUtilParticle.rainPositions[iii].xCoord,
                                pos.getY() + (float)CoroUtilParticle.rainPositions[iii].yCoord,
                                pos.getZ() + (float)CoroUtilParticle.rainPositions[iii].zCoord);
                        modelMatrix = gameItem.modelMatrix;//new Matrix4fe(gameItem.modelMatrix);
                        modelMatrix._m30(posCustom.getX());
                        modelMatrix._m31(posCustom.getY());
                        modelMatrix._m32(posCustom.getZ());
                    } else {
                        gameItem.modelMatrix = transformation.buildModelMatrix(gameItem, pos);
                        modelMatrix = gameItem.modelMatrix;
                    }

                    //get model matrix with extra render position factored in
                    //modelMatrix = transformation.buildModelMatrix(gameItem, posCustom);

                    if (billBoard) {
                        viewMatrix.transpose3x3(modelMatrix);
                    }

                    //adjust to perspective and camera
                    Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
                    //upload to buffer
                    modelViewMatrix.get(INSTANCE_SIZE_FLOATS * i, instanceDataBuffer);


                    i++;
                    index++;
                }


                //}

            }
            /*if (lightViewMatrix != null) {
                Matrix4fe modelLightViewMatrix = transformation.buildModelLightViewMatrix(modelMatrix, lightViewMatrix);
                modelLightViewMatrix.get(INSTANCE_SIZE_FLOATS * i + MATRIX_SIZE_FLOATS, this.instanceDataBuffer);
            }*/
            /*if (text != null) {
                int col = gameItem.getTextPos() % text.getNumCols();
                int row = gameItem.getTextPos() / text.getNumCols();
                float textXOffset = (float) col / text.getNumCols();
                float textYOffset = (float) row / text.getNumRows();
                int buffPos = INSTANCE_SIZE_FLOATS * i + MATRIX_SIZE_FLOATS * 2;
                this.instanceDataBuffer.put(buffPos, textXOffset);
                this.instanceDataBuffer.put(buffPos + 1, textYOffset);
            }*/
        }

        glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
        if (!testSkip) glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_DYNAMIC_DRAW);

        ShaderManager.glDrawElementsInstanced(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, amountToRender);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
