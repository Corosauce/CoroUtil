package extendedrenderer.shadertest.gametest;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * Created by corosus on 08/05/17.
 */
public class Mesh {

    protected int vaoId;

    //private int posVboId;

    //private int colourVboId;

    //private int idxVboId;

    protected List<Integer> vboIdList = new ArrayList<>();

    private int vertexCount;

    public static final int MAX_WEIGHTS = 4;

    //public List<Matrix4fe> posExtra = new ArrayList<>();

    public static int extraRenders = 10;

    public Mesh(float[] positions, float[] textCoords, int[] indices) {

        float radius = 10;
        Random rand = new Random();
        /*for (int i = 0; i < extraRenders; i++) {
            Matrix4fe matOffset = new Matrix4fe();
            matOffset.identity();
            matOffset.translate(rand.nextFloat() * radius - rand.nextFloat() * radius, rand.nextFloat() * radius - rand.nextFloat() * radius, rand.nextFloat() * radius - rand.nextFloat() * radius);
            posExtra.add(matOffset);
        }*/

        /*float[] normals = createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0);
        int[] jointIndices = createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0);
        float[] weights = createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0);*/

        vertexCount = indices.length;

        FloatBuffer verticesBuffer = null;
        IntBuffer indicesBuffer = null;
        FloatBuffer textCoordsBuffer = null;
        /*FloatBuffer vecNormalsBuffer = null;
        IntBuffer jointIndicesBuffer = null;
        FloatBuffer weightsBuffer = null;*/
        try {



            // Create the VAO and bind to it
            vaoId = glGenVertexArrays();
            glBindVertexArray(vaoId);



            // Create the VBO and bind to it
            int posVboId = glGenBuffers();
            vboIdList.add(posVboId);
            verticesBuffer = BufferUtils.createFloatBuffer(positions.length);//MemoryUtil.memAllocFloat(vertices.length);
            verticesBuffer.put(positions).flip();
            glBindBuffer(GL_ARRAY_BUFFER, posVboId);
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
            // Define structure of the data
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
            //might not be needed, but added when downgrading to GLSL 120
            //glEnableVertexAttribArray(0);

            //tex vbo
            int texVboId = glGenBuffers();
            vboIdList.add(texVboId);
            textCoordsBuffer = BufferUtils.createFloatBuffer(textCoords.length);
            textCoordsBuffer.put(textCoords).flip();
            glBindBuffer(GL_ARRAY_BUFFER, texVboId);
            glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            //????
            //glEnableVertexAttribArray(1);

            // Vertex normals VBO
            /*int vboId = glGenBuffers();
            vboIdList.add(vboId);
            vecNormalsBuffer = BufferUtils.createFloatBuffer(normals.length);
            vecNormalsBuffer.put(normals).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, vecNormalsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);*/

            // Weights
            /*vboId = glGenBuffers();
            vboIdList.add(vboId);
            weightsBuffer = BufferUtils.createFloatBuffer(weights.length);
            weightsBuffer.put(weights).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);*/

            // Joint indices
            /*vboId = glGenBuffers();
            vboIdList.add(vboId);
            jointIndicesBuffer = BufferUtils.createIntBuffer(jointIndices.length);
            jointIndicesBuffer.put(jointIndices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, jointIndicesBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);*/

            //index vbo
            int idxVboId = glGenBuffers();
            vboIdList.add(idxVboId);
            indicesBuffer = BufferUtils.createIntBuffer(indices.length);
            indicesBuffer.put(indices).flip();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            // Unbind the VBO
            glBindBuffer(GL_ARRAY_BUFFER, 0);

            // Unbind the VAO
            glBindVertexArray(0);
        } finally {
            /**
             * TODO: test if we need to actually free the memory since we have to use BufferUtils.createFloatBuffer instead of MemoryUtil.memAllocFloat
             * "It's not trivial because I want to make it optional and using jemalloc requires explicit je_free calls to avoid leaking memory.
             * Existing usages of BufferUtils do not have that requirement and will have to be adjusted accordingly."
             *
             * BufferUtils is more automatic, doesnt need freeing, but can be slower and risks memory fragmentation, MemoryUtil gives more control and responsibility
             */
            if (verticesBuffer != null) {
                //MemoryUtil.memFree(verticesBuffer);
            }
        }
    }

    protected void initRender() {
        /*Texture texture = material.getTexture();
        if (texture != null) {
            // Activate first texture bank
            glActiveTexture(GL_TEXTURE0);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, texture.getId());
        }
        Texture normalMap = material.getNormalMap();
        if ( normalMap != null ) {
            // Activate first texture bank
            glActiveTexture(GL_TEXTURE1);
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, normalMap.getId());
        }*/

        // Draw the mesh
        glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        //glEnableVertexAttribArray(2);
        //glEnableVertexAttribArray(3);
        //glEnableVertexAttribArray(4);
    }

    protected void endRender() {
        // Restore state
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        //glDisableVertexAttribArray(2);
        //glDisableVertexAttribArray(3);
        //glDisableVertexAttribArray(4);
        glBindVertexArray(0);

        //glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void render() {
        //System.out.println("render start");
        // Draw the mesh
        /*glBindVertexArray(getVaoId());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);*/
        initRender();

        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        // Restore state
        endRender();
        /*glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);*/
        //System.out.println("render end");
    }

    public int getVaoId() {
        return vaoId;
    }

    public void setVaoId(int vaoId) {
        this.vaoId = vaoId;
    }

    public int getVertexCount() {
        return vertexCount;
    }

    public void setVertexCount(int vertexCount) {
        this.vertexCount = vertexCount;
    }

    public void cleanup() {
        glDisableVertexAttribArray(0);

        // Delete the VBO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboId : vboIdList) {
            glDeleteBuffers(vboId);
        }

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }

    protected static float[] createEmptyFloatArray(int length, float defaultValue) {
        float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    protected static int[] createEmptyIntArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }
}
