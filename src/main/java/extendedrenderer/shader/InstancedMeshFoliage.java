package extendedrenderer.shader;

import extendedrenderer.particle.ShaderManager;
import net.minecraft.client.renderer.OpenGlHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

import java.nio.FloatBuffer;

public class InstancedMeshFoliage extends Mesh {

    public static final int FLOAT_SIZE_BYTES = 4;

    public static final int VECTOR4F_SIZE_BYTES = 4 * FLOAT_SIZE_BYTES;

    public static final int MATRIX_SIZE_FLOATS = 4 * 4;

    public static final int MATRIX_SIZE_BYTES = MATRIX_SIZE_FLOATS * FLOAT_SIZE_BYTES;

    public static final int INSTANCE_SIZE_BYTES = FLOAT_SIZE_BYTES * 2;

    public static final int INSTANCE_SIZE_FLOATS = 2;

    public static final int INSTANCE_SIZE_BYTES_SELDOM = MATRIX_SIZE_BYTES + FLOAT_SIZE_BYTES * 7;

    public static final int INSTANCE_SIZE_FLOATS_SELDOM = MATRIX_SIZE_FLOATS + 7;

    public final int numInstances;

    public final int instanceDataVBO;
    public final int instanceDataVBOSeldom;

    public FloatBuffer instanceDataBufferVBO1;
    public FloatBuffer instanceDataBufferVBO2;
    public FloatBuffer instanceDataBufferSeldom2;

    public int curBufferPos = 0;
    public int curBufferPosVBO2 = 0;

    public static int vboSizeMesh = 2;

    public InstancedMeshFoliage(float[] positions, float[] textCoords, int[] indices, int numInstances) {
        super(positions, textCoords, indices);

        this.numInstances = numInstances;

        ShaderManager.glBindVertexArray(vaoId);

        /** VBO 1 START **/

        instanceDataVBO = GL15.glGenBuffers();
        vboIdList.add(instanceDataVBO);
        instanceDataBufferVBO1 = BufferUtils.createFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS);//MemoryUtil.memAllocFloat(numInstances * INSTANCE_SIZE_FLOATS);
        OpenGlHelper.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceDataVBO);
        int start = vboSizeMesh;
        int strideStart = 0;

        //alpha, brightness
        GL20.glVertexAttribPointer(start, 2, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += FLOAT_SIZE_BYTES;

        //brightness
        /*GL20.glVertexAttribPointer(start, 1, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += FLOAT_SIZE_BYTES;*/

        /** VBO 2 START **/

        //test
        //start = vboSizeMesh;
        strideStart = 0;

        instanceDataVBOSeldom = OpenGlHelper.glGenBuffers();
        vboIdList.add(instanceDataVBOSeldom);
        instanceDataBufferVBO2 = BufferUtils.createFloatBuffer(numInstances * INSTANCE_SIZE_FLOATS_SELDOM);
        OpenGlHelper.glBindBuffer(GL15.GL_ARRAY_BUFFER, instanceDataVBOSeldom);

        // Model Matrix
        for (int i = 0; i < 4; i++) {
            GL20.glVertexAttribPointer(start, 4, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
            ShaderManager.glVertexAttribDivisor(start, 1);
            start++;
            strideStart += VECTOR4F_SIZE_BYTES;
        }

        //red, green, blue, rotation
        GL20.glVertexAttribPointer(start, 4, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += VECTOR4F_SIZE_BYTES;

        //instance index, animation ID, height index
        GL20.glVertexAttribPointer(start, 3, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;
        strideStart += VECTOR4F_SIZE_BYTES;

        //instance index
        /*GL20.glVertexAttribPointer(start, 1, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;*/
/*
        //animation ID
        GL20.glVertexAttribPointer(start, 1, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;

        //height from base piece
        GL20.glVertexAttribPointer(start, 1, GL11.GL_FLOAT, false, INSTANCE_SIZE_BYTES_SELDOM, strideStart);
        ShaderManager.glVertexAttribDivisor(start, 1);
        start++;*/

        OpenGlHelper.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        ShaderManager.glBindVertexArray(0);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if (this.instanceDataBufferVBO1 != null) {
            //MemoryUtil.memFree(this.instanceDataBuffer);
            this.instanceDataBufferVBO1 = null;
        }

        if (this.instanceDataBufferVBO2 != null) {
            //MemoryUtil.memFree(this.instanceDataBuffer);
            this.instanceDataBufferVBO2 = null;
        }
    }

    @Override
    public void initRender() {
        super.initRender();
    }

    @Override
    public void endRender() {

        super.endRender();
    }

    public int getAttribSizeVBO1() {
        //alphaBrightness
        return 1;
    }

    public int getAttribSizeVBO2() {
        //model matrix + rgbrot + index,animationID,heightIndex
        return 6;
    }

    public void initRenderVBO1() {
        int start = vboSizeMesh;
        for (int i = 0; i < getAttribSizeVBO1(); i++) {
            GL20.glEnableVertexAttribArray(start + i);
        }
    }

    public void endRenderVBO1() {
        int start = vboSizeMesh;
        for (int i = 0; i < getAttribSizeVBO1(); i++) {
            GL20.glDisableVertexAttribArray(start + i);
        }
    }

    public void initRenderVBO2() {
        int start = vboSizeMesh;
        for (int i = 0; i < getAttribSizeVBO2(); i++) {
            GL20.glEnableVertexAttribArray(start + getAttribSizeVBO1() + i);
        }
    }

    public void endRenderVBO2() {
        int start = vboSizeMesh;
        for (int i = 0; i < getAttribSizeVBO2(); i++) {
            GL20.glDisableVertexAttribArray(start + getAttribSizeVBO1() + i);
        }
    }
}
