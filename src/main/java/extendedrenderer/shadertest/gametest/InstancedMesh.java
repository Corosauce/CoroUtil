package extendedrenderer.shadertest.gametest;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.ARBDrawInstanced.glDrawElementsInstancedARB;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL31.glDrawElementsInstanced;
import static org.lwjgl.opengl.GL33.glVertexAttribDivisor;

public class InstancedMesh extends Mesh {

    private static final int FLOAT_SIZE_BYTES = 4;

    private static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;

    private static final int MATRIX_SIZE_FLOATS = 4 * 4;
    
    private static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;

    private static final int INSTANCE_SIZE_BYTES = MATRIX_SIZE_BYTES * 2 + FLOAT_SIZE_BYTES * 2;
    
    private static final int INSTANCE_SIZE_FLOATS = MATRIX_SIZE_FLOATS * 2 + 2;

    private final int numInstances;

    private final int instanceDataVBO;

    private FloatBuffer instanceDataBuffer;

    public InstancedMesh(float[] positions, float[] textCoords, int[] indices, int numInstances) {
        super(positions, textCoords, indices);

        this.numInstances = numInstances;

        glBindVertexArray(vaoId);

        // Model View Matrix
        instanceDataVBO = glGenBuffers();
        vboIdList.add(instanceDataVBO);
        instanceDataBuffer = BufferUtils.createFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS);//MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
        glBindBuffer(GL_ARRAY_BUFFER, instanceDataVBO);
        int start = 5;
        int strideStart = 0;
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        // Light view matrix
        for (int i = 0; i < 4; i++) {
            glVertexAttribPointer(start, 4, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
            glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        // Texture offsets
        glVertexAttribPointer(start, 2, GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        glVertexAttribDivisor(start, 1);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.instanceDataBuffer != null) {
            //MemoryUtil.memFree(this.instanceDataBuffer);
            this.instanceDataBuffer = null;
        }
    }

    @Override
    protected void initRender() {
        super.initRender();

        int start = 5;
        int numElements = 4 * 2 + 1;
        for (int i = 0; i < numElements; i++) {
            glEnableVertexAttribArray(start + i);
        }
    }

    @Override
    protected void endRender() {
        int start = 5;
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
        //this.instanceDataBuffer.clear();

        int i = 0;
        int index = 0;

        //temp
        boolean billBoard = false;



        //Texture text = getMaterial().getTexture();
        for (int ii = 0; ii < gameItems.size(); ii++) {
        //for (GameItem gameItem : gameItems) {
            GameItem gameItem = gameItems.get(ii);

            Matrix4fe modelMatrix = transformation.buildModelMatrix(gameItem);
            if (viewMatrix != null) {
                if (billBoard) {
                    viewMatrix.transpose3x3(modelMatrix);
                }

                //if (index != 0) {
                for (int iii = 0; iii < posExtra.size(); iii++) {
                    Matrix4fe modelViewMatrix = transformation.buildModelViewMatrix(modelMatrix, viewMatrix);
                    modelViewMatrix.mul(posExtra.get(iii));
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
        glBufferData(GL_ARRAY_BUFFER, instanceDataBuffer, GL_DYNAMIC_DRAW);

        glDrawElementsInstanced(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0, i);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
